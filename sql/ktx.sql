EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT ALL"
EXEC sp_MSforeachtable 'DROP TABLE ?'

SELECT @@SERVERNAME
SELECT username, password, role
FROM Users
WHERE username = 'admin';

GO
CREATE LOGIN sdms
WITH PASSWORD = '123456';
GO
USE [quanly-ktx];
GO

CREATE USER sdms FOR LOGIN sdms;
GO

ALTER ROLE db_owner ADD MEMBER sdms;
GO

-- 1. Rooms  ←  Room.java
--    Status (AVAILABLE/NEARLY_FULL/FULL/MAINTENANCE) tính ở Java
--    → DB chỉ lưu occupied, không lưu status

CREATE TABLE Rooms (
    id        VARCHAR(10)  NOT NULL,
    name      NVARCHAR(50) NOT NULL,
    type      NVARCHAR(20) NOT NULL,
    floor     INT          NOT NULL CONSTRAINT CHK_Room_floor CHECK (floor >= 1),
    capacity  INT          NOT NULL CONSTRAINT CHK_Room_cap   CHECK (capacity > 0),
    occupied  INT          NOT NULL DEFAULT 0
                                    CONSTRAINT CHK_Room_occ   CHECK (occupied >= 0),

    CONSTRAINT PK_Rooms         PRIMARY KEY (id),
    CONSTRAINT CHK_Room_occ_cap CHECK (occupied <= capacity)
);
GO

-- ================================================================
-- 2. Students  ←  Student.java
--    room_id NULL = chưa xếp phòng
--    status: "Đang ở" | "Chờ duyệt" | "Mới đăng ký" | "Đã rời"
-- ================================================================
CREATE TABLE Students (
    id          VARCHAR(15)   NOT NULL,
    full_name   NVARCHAR(100) NOT NULL,
    birth_date  VARCHAR(10)   NOT NULL,   -- "dd/MM/yyyy" khớp Java
    gender      NVARCHAR(5)   NOT NULL    CONSTRAINT CHK_SV_gender CHECK (gender IN (N'Nam', N'Nữ')),
    id_card     VARCHAR(15)   NULL        CONSTRAINT UQ_SV_IDCard  UNIQUE,
    phone       VARCHAR(15)   NULL        CONSTRAINT UQ_SV_Phone   UNIQUE,
    email       VARCHAR(50)   NULL        CONSTRAINT UQ_SV_Email   UNIQUE,
    university  NVARCHAR(100) NULL,
    faculty     NVARCHAR(50)  NULL,
    class_name  VARCHAR(20)   NULL,
    address     NVARCHAR(200) NULL,
    room_id     VARCHAR(10)   NULL,
    status      NVARCHAR(20)  NOT NULL DEFAULT N'Mới đăng ký'
                              CONSTRAINT CHK_SV_status CHECK (
                                  status IN (N'Đang ở', N'Chờ duyệt', N'Mới đăng ký', N'Đã rời')
                              ),

    CONSTRAINT PK_Students  PRIMARY KEY (id),
    CONSTRAINT FK_SV_Room   FOREIGN KEY (room_id) REFERENCES Rooms(id)
                            ON UPDATE CASCADE ON DELETE SET NULL
);
GO

-- ================================================================
-- 3. Users  ←  User.java
--    password: SHA-256 hex (64 ký tự)
--    student_id NULL nếu role = ADMIN
-- ================================================================
CREATE TABLE Users (
    username    VARCHAR(50)   NOT NULL,
    password    VARCHAR(64)   NOT NULL,
    role        VARCHAR(10)   NOT NULL CONSTRAINT CHK_User_role CHECK (role IN ('ADMIN', 'STUDENT')),
    full_name   NVARCHAR(100) NOT NULL,
    student_id  VARCHAR(15)   NULL,

    CONSTRAINT PK_Users        PRIMARY KEY (username),
    CONSTRAINT FK_User_Student FOREIGN KEY (student_id) REFERENCES Students(id)
                               ON UPDATE CASCADE ON DELETE SET NULL
);
GO

-- ================================================================
-- 4. PendingAccounts  ←  DataStore.PendingAccount
--    Đơn đăng ký chờ admin duyệt
-- ================================================================
CREATE TABLE PendingAccounts (
    id            VARCHAR(10)   NOT NULL,
    username      VARCHAR(50)   NOT NULL,
    full_name     NVARCHAR(100) NOT NULL,
    phone         VARCHAR(15)   NULL,
    dob           VARCHAR(10)   NULL,     -- "dd/MM/yyyy"
    cccd          VARCHAR(15)   NULL,
    gender        NVARCHAR(5)   NULL      CONSTRAINT CHK_PA_gender CHECK (gender IN (N'Nam', N'Nữ')),
    registered_at NVARCHAR(20)  NOT NULL, -- "dd/MM/yyyy HH:mm"
    status        NVARCHAR(10)  NOT NULL DEFAULT N'Chờ duyệt'
                                CONSTRAINT CHK_PA_status CHECK (
                                    status IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối')
                                ),
    note          NVARCHAR(500) NOT NULL DEFAULT '',

    CONSTRAINT PK_PendingAccounts PRIMARY KEY (id)
);
GO

-- ================================================================
-- 5. Contracts  ←  Contract.java
--    Java tự refreshStatus() khi load, DB lưu trạng thái cuối
-- ================================================================
CREATE TABLE Contracts (
    id           NVARCHAR(10)  NOT NULL,   -- "HĐ0001" (Unicode)
    student_id   VARCHAR(15)   NOT NULL,
    student_name NVARCHAR(100) NOT NULL,
    room_id      VARCHAR(10)   NOT NULL,
    start_date   DATE          NOT NULL,
    end_date     DATE          NULL,
    monthly_fee  BIGINT        NOT NULL DEFAULT 0 CONSTRAINT CHK_CT_fee   CHECK (monthly_fee >= 0),
    note         NVARCHAR(500) NOT NULL DEFAULT '',
    status       VARCHAR(15)   NOT NULL DEFAULT 'PENDING'
                               CONSTRAINT CHK_CT_status CHECK (
                                   status IN ('ACTIVE', 'EXPIRED', 'TERMINATED', 'PENDING')
                               ),

    CONSTRAINT PK_Contracts  PRIMARY KEY (id),
    CONSTRAINT FK_CT_Student FOREIGN KEY (student_id) REFERENCES Students(id),
    CONSTRAINT FK_CT_Room    FOREIGN KEY (room_id)    REFERENCES Rooms(id),
    CONSTRAINT CHK_CT_dates  CHECK (end_date IS NULL OR end_date > start_date)
);
GO

-- ================================================================
-- 6. Utilities  ←  Utility.java
--    Mỗi phòng chỉ 1 bản ghi / tháng (UNIQUE room_id + month)
-- ================================================================
CREATE TABLE Utilities (
    id                  VARCHAR(10)   NOT NULL,  -- "UT0001"
    room_id             VARCHAR(10)   NOT NULL,
    month               VARCHAR(7)    NOT NULL,  -- "MM/YYYY"
    electric_prev       FLOAT         NOT NULL DEFAULT 0,
    electric_curr       FLOAT         NOT NULL DEFAULT 0,
    water_prev          FLOAT         NOT NULL DEFAULT 0,
    water_curr          FLOAT         NOT NULL DEFAULT 0,
    electric_unit_price BIGINT        NOT NULL DEFAULT 2000,
    water_unit_price    BIGINT        NOT NULL DEFAULT 6000,
    note                NVARCHAR(200) NOT NULL DEFAULT '',
    confirmed           BIT           NOT NULL DEFAULT 0,

    CONSTRAINT PK_Utilities     PRIMARY KEY (id),
    CONSTRAINT FK_UT_Room       FOREIGN KEY (room_id) REFERENCES Rooms(id),
    CONSTRAINT UQ_UT_RoomMonth  UNIQUE (room_id, month),
    CONSTRAINT CHK_UT_elec      CHECK (electric_curr >= electric_prev),
    CONSTRAINT CHK_UT_water     CHECK (water_curr    >= water_prev)
);
GO

-- ================================================================
-- 7. Invoices  ←  Invoice.java
--    utility_id NULL nếu nhập tay (không từ bảng Utilities)
--    UNIQUE (student_id, month): 1 hóa đơn / sinh viên / tháng
-- ================================================================
CREATE TABLE Invoices (
    id           VARCHAR(10)   NOT NULL,  -- "HD001"
    student_id   VARCHAR(15)   NOT NULL,
    student_name NVARCHAR(100) NOT NULL,
    room_id      VARCHAR(10)   NOT NULL,
    utility_id   VARCHAR(10)   NULL,
    month        VARCHAR(7)    NOT NULL,  -- "MM/YYYY"
    room_fee     BIGINT        NOT NULL DEFAULT 0,
    electric_fee BIGINT        NOT NULL DEFAULT 0,
    water_fee    BIGINT        NOT NULL DEFAULT 0,
    paid         BIT           NOT NULL DEFAULT 0,

    CONSTRAINT PK_Invoices    PRIMARY KEY (id),
    CONSTRAINT FK_INV_Student FOREIGN KEY (student_id) REFERENCES Students(id),
    CONSTRAINT FK_INV_Room    FOREIGN KEY (room_id)    REFERENCES Rooms(id),
    CONSTRAINT FK_INV_Utility FOREIGN KEY (utility_id) REFERENCES Utilities(id)
                              ON DELETE SET NULL,
    CONSTRAINT UQ_INV_SvMonth UNIQUE (student_id, month)
);
GO

-- ================================================================
-- 8. Violations  ←  Violation.java
-- ================================================================
CREATE TABLE Violations (
    id           VARCHAR(10)   NOT NULL,  -- "VP0001"
    student_id   VARCHAR(15)   NOT NULL,
    student_name NVARCHAR(100) NOT NULL,
    room_id      VARCHAR(10)   NOT NULL,
    vio_date     DATE          NOT NULL,
    type         NVARCHAR(100) NOT NULL,
    description  NVARCHAR(500) NOT NULL DEFAULT '',
    severity     VARCHAR(10)   NOT NULL DEFAULT 'LOW'
                               CONSTRAINT CHK_VP_sev CHECK (
                                   severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
                               ),
    fine         BIGINT        NOT NULL DEFAULT 0 CONSTRAINT CHK_VP_fine CHECK (fine >= 0),
    handled_by   NVARCHAR(100) NOT NULL DEFAULT '',
    status       VARCHAR(10)   NOT NULL DEFAULT 'PENDING'
                               CONSTRAINT CHK_VP_status CHECK (
                                   status IN ('PENDING', 'PROCESSED', 'APPEALING')
                               ),
    note         NVARCHAR(500) NOT NULL DEFAULT '',

    CONSTRAINT PK_Violations PRIMARY KEY (id),
    CONSTRAINT FK_VP_Student FOREIGN KEY (student_id) REFERENCES Students(id),
    CONSTRAINT FK_VP_Room    FOREIGN KEY (room_id)    REFERENCES Rooms(id)
);
GO

-- ================================================================
-- 9. Notifications  ←  Notification.java
-- ================================================================
CREATE TABLE Notifications (
    id          VARCHAR(10)   NOT NULL,  -- "TB0001"
    title       NVARCHAR(200) NOT NULL,
    content     NVARCHAR(MAX) NOT NULL,
    type        VARCHAR(10)   NOT NULL DEFAULT 'GENERAL'
                              CONSTRAINT CHK_NTF_type CHECK (
                                  type IN ('INVOICE','CONTRACT','VIOLATION','INSPECTION','GENERAL','URGENT')
                              ),
    target      VARCHAR(10)   NOT NULL DEFAULT 'ALL'
                              CONSTRAINT CHK_NTF_target CHECK (target IN ('ALL','ROOM','STUDENT')),
    target_id   VARCHAR(15)   NOT NULL DEFAULT '',
    created_at  DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    created_by  NVARCHAR(100) NOT NULL DEFAULT N'Quản trị viên',
    pinned      BIT           NOT NULL DEFAULT 0,

    CONSTRAINT PK_Notifications PRIMARY KEY (id)
);
GO

-- ================================================================
-- 10. NotificationReads  ←  (bảng mới)
--     Trạng thái đọc riêng từng sinh viên
--     Thay cho boolean read trong Notification (không thể dùng chung)
-- ================================================================
CREATE TABLE NotificationReads (
    notification_id VARCHAR(10)  NOT NULL,
    student_id      VARCHAR(15)  NOT NULL,
    read_at         DATETIME2    NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT PK_NtfReads     PRIMARY KEY (notification_id, student_id),
    CONSTRAINT FK_NtfR_Notif   FOREIGN KEY (notification_id) REFERENCES Notifications(id) ON DELETE CASCADE,
    CONSTRAINT FK_NtfR_Student FOREIGN KEY (student_id)      REFERENCES Students(id)      ON DELETE CASCADE
);
GO

-- ================================================================
-- 11. Settings  ←  SettingsPanel.java  (19 keys)
-- ================================================================
CREATE TABLE Settings (
    setting_key   VARCHAR(50)   NOT NULL,
    setting_value NVARCHAR(500) NOT NULL,
    description   NVARCHAR(200) NULL,

    CONSTRAINT PK_Settings PRIMARY KEY (setting_key)
);
GO

-- ================================================================
-- INDEXES
-- ================================================================
CREATE INDEX IX_SV_RoomId     ON Students(room_id);
CREATE INDEX IX_SV_FullName   ON Students(full_name);
CREATE INDEX IX_SV_Status     ON Students(status);
CREATE INDEX IX_CT_StudentId  ON Contracts(student_id);
CREATE INDEX IX_CT_Status     ON Contracts(status);
CREATE INDEX IX_UT_Month      ON Utilities(month);
CREATE INDEX IX_INV_StudentId ON Invoices(student_id);
CREATE INDEX IX_INV_Month     ON Invoices(month);
CREATE INDEX IX_INV_Paid      ON Invoices(paid);
CREATE INDEX IX_VP_StudentId  ON Violations(student_id);
CREATE INDEX IX_VP_Status     ON Violations(status);
CREATE INDEX IX_NTF_Target    ON Notifications(target, target_id);
CREATE INDEX IX_NTF_CreatedAt ON Notifications(created_at DESC);
GO


-- Tài khoản admin mặc định
-- Mật khẩu "admin123" → SHA-256
INSERT INTO Users (username, password, role, full_name, student_id)
VALUES ('admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'ADMIN', N'Quản trị viên', NULL);
GO

-- Cấu hình mặc định (khớp SettingsPanel.java)
INSERT INTO Settings (setting_key, setting_value, description) VALUES
('dorm_name',     N'Ký túc xá Đại học SDMS',               N'Tên ký túc xá'),
('address',       N'01 Đường Đại học, Quận 1, TP. Hà Nội', N'Địa chỉ'),
('phone',         N'',                                       N'Số điện thoại liên hệ'),
('email',         N'',                                       N'Email liên hệ'),
('director',      N'',                                       N'Tên Ban giám đốc'),
('academic_year', N'2025-2026',                              N'Năm học hiện tại'),
('semester',      N'Học kỳ 2',                              N'Học kỳ hiện tại'),
('electric_price','2000',                                    N'Đơn giá điện (đ/kWh)'),
('water_price',   '6000',                                    N'Đơn giá nước (đ/m³)'),
('room_fee_4',    '850000',                                  N'Tiền phòng 4 người (đ/tháng)'),
('room_fee_6',    '650000',                                  N'Tiền phòng 6 người (đ/tháng)'),
('curfew',        '23:00',                                   N'Giờ giới nghiêm'),
('guest_policy',  N'Khách thăm trước 21:00',                N'Quy định khách'),
('max_warning',   '3',                                       N'Số cảnh cáo tối đa'),
('auto_notify',   '1',                                       N'Tự động gửi thông báo'),
('auto_invoice',  '1',                                       N'Tự động tạo hóa đơn'),
('auto_contract', '0',                                       N'Tự động gia hạn hợp đồng'),
('admin_name',    N'Admin SDMS',                            N'Tên hiển thị Admin'),
('admin_email',   N'admin@sdms.edu.vn',                     N'Email Admin');
GO

PRINT N'✅ SDMS Database khởi tạo thành công — 11 bảng sẵn sàng.';
GO