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


-- ================================================================
-- 1. ROOMS (15 phòng)
-- ================================================================
INSERT INTO Rooms (id, name, type, floor, capacity, occupied) VALUES
('P101', N'Phòng 101', N'4 người', 1, 4, 4),
('P102', N'Phòng 102', N'4 người', 1, 4, 3),
('P103', N'Phòng 103', N'4 người', 1, 4, 2),
('P104', N'Phòng 104', N'6 người', 1, 6, 6),
('P201', N'Phòng 201', N'4 người', 2, 4, 4),
('P202', N'Phòng 202', N'4 người', 2, 4, 1),
('P203', N'Phòng 203', N'6 người', 2, 6, 5),
('P204', N'Phòng 204', N'6 người', 2, 6, 0),
('P301', N'Phòng 301', N'4 người', 3, 4, 2),
('P302', N'Phòng 302', N'4 người', 3, 4, 4),
('P303', N'Phòng 303', N'6 người', 3, 6, 3),
('P304', N'Phòng 304', N'6 người', 3, 6, 6),
('P401', N'Phòng 401', N'4 người', 4, 4, 0),
('P402', N'Phòng 402', N'4 người', 4, 4, 2),
('P403', N'Phòng 403', N'6 người', 4, 6, 0);
GO

-- ================================================================
-- 2. STUDENTS (20 sinh viên)
-- ================================================================
INSERT INTO Students (id, full_name, birth_date, gender, id_card, phone, email, university, faculty, class_name, address, room_id, status) VALUES
('SV001249', N'Nguyễn Văn An',     '15/03/2004', N'Nam', '079204001234', '0901234001', 'an.nguyen01@gmail.com',  N'ĐH Bách Khoa Hà Nội', N'CNTT',        'CNTT01-K65', N'Nam Định',  'P101', N'Đang ở'),
('SV001250', N'Trần Thị Bình',     '22/07/2004', N'Nữ',  '079204001235', '0901234002', 'binh.tran02@gmail.com',  N'ĐH Bách Khoa Hà Nội', N'Điện - Điện tử', 'DTVT01-K65', N'Hà Nam', 'P101', N'Đang ở'),
('SV001251', N'Lê Văn Cường',      '01/01/2004', N'Nam', '079204001236', '0901234003', 'cuong.le03@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'Cơ khí',     'CK02-K65',   N'Thanh Hóa', 'P101', N'Đang ở'),
('SV001252', N'Phạm Thị Dung',     '12/05/2004', N'Nữ',  '079204001237', '0901234004', 'dung.pham04@gmail.com',  N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT02-K65', N'Hải Dương', 'P101', N'Đang ở'),
('SV001253', N'Hoàng Văn Em',      '30/09/2003', N'Nam', '079203001238', '0901234005', 'em.hoang05@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'Hóa học',    'HH01-K64',   N'Bắc Giang', 'P102', N'Đang ở'),
('SV001254', N'Vũ Thị Giang',      '18/02/2004', N'Nữ',  '079204001239', '0901234006', 'giang.vu06@gmail.com',   N'ĐH Kinh tế Quốc dân', N'Marketing',  'MK03-K65',   N'Phú Thọ', 'P102', N'Đang ở'),
('SV001255', N'Đỗ Văn Hùng',       '05/11/2003', N'Nam', '079203001240', '0901234007', 'hung.do07@gmail.com',    N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT03-K64', N'Vĩnh Phúc', 'P102', N'Đang ở'),
('SV001256', N'Ngô Thị Lan',       '09/04/2004', N'Nữ',  '079204001241', '0901234008', 'lan.ngo08@gmail.com',    N'ĐH Ngoại thương',     N'Kinh tế đối ngoại', 'KTDN02-K65', N'Ninh Bình', 'P103', N'Đang ở'),
('SV001257', N'Bùi Văn Khánh',     '25/06/2004', N'Nam', '079204001242', '0901234009', 'khanh.bui09@gmail.com',  N'ĐH Bách Khoa Hà Nội', N'Điện - Điện tử', 'DTVT02-K65', N'Hưng Yên', 'P103', N'Đang ở'),
('SV001258', N'Đặng Thị Mai',      '14/08/2004', N'Nữ',  '079204001243', '0901234010', 'mai.dang10@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT04-K65', N'Thái Bình', 'P104', N'Đang ở'),
('SV001259', N'Phan Văn Nam',      '03/12/2003', N'Nam', '079203001244', '0901234011', 'nam.phan11@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'Cơ khí',     'CK03-K64',   N'Quảng Ninh', 'P104', N'Đang ở'),
('SV001260', N'Lý Thị Oanh',       '27/03/2004', N'Nữ',  '079204001245', '0901234012', 'oanh.ly12@gmail.com',    N'ĐH Bách Khoa Hà Nội', N'Hóa học',    'HH02-K65',   N'Hải Phòng', 'P104', N'Đang ở'),
('SV001261', N'Trịnh Văn Phúc',    '19/10/2004', N'Nam', '079204001246', '0901234013', 'phuc.trinh13@gmail.com', N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT05-K65', N'Nghệ An', 'P104', N'Đang ở'),
('SV001262', N'Vương Thị Quỳnh',   '08/01/2004', N'Nữ',  '079204001247', '0901234014', 'quynh.vuong14@gmail.com',N'ĐH Kinh tế Quốc dân', N'Tài chính',  'TC01-K65',   N'Hà Tĩnh', 'P104', N'Đang ở'),
('SV001263', N'Đinh Văn Sơn',      '21/05/2003', N'Nam', '079203001248', '0901234015', 'son.dinh15@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'Điện - Điện tử', 'DTVT03-K64', N'Thanh Hóa', 'P201', N'Đang ở'),
('SV001264', N'Tô Thị Thảo',       '11/07/2004', N'Nữ',  '079204001249', '0901234016', 'thao.to16@gmail.com',    N'ĐH Ngoại thương',     N'Quản trị kinh doanh', 'QTKD01-K65', N'Nam Định', 'P201', N'Đang ở'),
('SV001265', N'Mai Văn Tùng',      '02/02/2004', N'Nam', '079204001250', '0901234017', 'tung.mai17@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT06-K65', N'Bắc Ninh', 'P201', N'Đang ở'),
('SV001266', N'Cao Thị Uyên',      '29/09/2004', N'Nữ',  '079204001251', '0901234018', 'uyen.cao18@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'Cơ khí',     'CK04-K65',   N'Hòa Bình', 'P201', N'Đang ở'),
('SV001267', N'Hồ Văn Việt',       '16/04/2004', N'Nam', '079204001252',   '0901234019', 'viet.ho19@gmail.com',   N'ĐH Bách Khoa Hà Nội', N'CNTT',       'CNTT07-K65', N'Sơn La', NULL, N'Chờ duyệt'),
('SV001268', N'Lương Thị Xuân',    '07/06/2004', N'Nữ',  '079204001253',   '0901234020', 'xuan.luong20@gmail.com',N'ĐH Bách Khoa Hà Nội', N'Hóa học',    'HH03-K65',   N'Lào Cai', NULL, N'Mới đăng ký');
GO

-- ================================================================
-- 3. USERS (tài khoản sinh viên — password mặc định "123456" SHA-256)
--    SHA-256("123456") = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
-- ================================================================
INSERT INTO Users (username, password, role, full_name, student_id) VALUES
('SV001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Nguyễn Văn An', 'SV001249'),
('SV002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Trần Thị Bình', 'SV001250'),
('SV003', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Lê Văn Cường', 'SV001251'),
('SV004', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Phạm Thị Dung', 'SV001252'),
('SV007', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Hoàng Văn Em', 'SV001253'),
('SV006', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Vũ Thị Giang', 'SV001254'),
('SV008', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Đỗ Văn Hùng', 'SV001255'),
('SV001256', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Ngô Thị Lan', 'SV001256'),
('SV001257', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Bùi Văn Khánh', 'SV001257'),
('SV001258', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'STUDENT', N'Đặng Thị Mai', 'SV001258');
GO

-- ================================================================


-- ================================================================
-- 5. CONTRACTS (hợp đồng cho sinh viên đang ở)
-- ================================================================
INSERT INTO Contracts (id, student_id, student_name, room_id, start_date, end_date, monthly_fee, note, status) VALUES
(N'HĐ0001', 'SV001249', N'Nguyễn Văn An',   'P101', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0002', 'SV001250', N'Trần Thị Bình',   'P101', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0003', 'SV001251', N'Lê Văn Cường',    'P101', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0004', 'SV001252', N'Phạm Thị Dung',   'P101', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0005', 'SV001253', N'Hoàng Văn Em',    'P102', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0006', 'SV001254', N'Vũ Thị Giang',    'P102', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0007', 'SV001255', N'Đỗ Văn Hùng',     'P102', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0008', 'SV001256', N'Ngô Thị Lan',     'P103', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0009', 'SV001257', N'Bùi Văn Khánh',   'P103', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0010', 'SV001258', N'Đặng Thị Mai',    'P104', '2025-09-01', '2026-08-31', 650000, '', 'ACTIVE'),
(N'HĐ0011', 'SV001259', N'Phan Văn Nam',    'P104', '2025-09-01', '2026-08-31', 650000, '', 'ACTIVE'),
(N'HĐ0012', 'SV001260', N'Lý Thị Oanh',     'P104', '2025-09-01', '2026-08-31', 650000, '', 'ACTIVE'),
(N'HĐ0013', 'SV001261', N'Trịnh Văn Phúc',  'P104', '2025-09-01', '2026-08-31', 650000, '', 'ACTIVE'),
(N'HĐ0014', 'SV001262', N'Vương Thị Quỳnh', 'P104', '2025-09-01', '2026-08-31', 650000, '', 'ACTIVE'),
(N'HĐ0015', 'SV001263', N'Đinh Văn Sơn',    'P201', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0016', 'SV001264', N'Tô Thị Thảo',     'P201', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0017', 'SV001265', N'Mai Văn Tùng',    'P201', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
(N'HĐ0018', 'SV001266', N'Cao Thị Uyên',    'P201', '2025-09-01', '2026-08-31', 850000, '', 'ACTIVE'),
-- một hợp đồng cũ đã hết hạn (ví dụ minh họa)
(N'HĐ0019', 'SV001249', N'Nguyễn Văn An',   'P101', '2024-09-01', '2025-08-31', 800000, N'Hợp đồng năm trước', 'EXPIRED');
GO

-- ================================================================
-- 6. UTILITIES (chỉ số điện nước tháng 05/2026 cho các phòng có người)
-- ================================================================
INSERT INTO Utilities (id, room_id, month, electric_prev, electric_curr, water_prev, water_curr, electric_unit_price, water_unit_price, note, confirmed) VALUES
('UT0001', 'P101', '05/2026', 1200, 1340, 80, 92, 2000, 6000, '', 1),
('UT0002', 'P102', '05/2026', 980,  1100, 60, 70, 2000, 6000, '', 1),
('UT0003', 'P103', '05/2026', 760,  860,  45, 53, 2000, 6000, '', 1),
('UT0004', 'P104', '05/2026', 1500, 1690, 95, 110, 2000, 6000, '', 1),
('UT0005', 'P201', '05/2026', 1100, 1230, 70, 81, 2000, 6000, '', 1),
('UT0006', 'P203', '05/2026', 1300, 1450, 88, 100, 2000, 6000, N'Cần kiểm tra lại đồng hồ nước', 0),
('UT0007', 'P301', '05/2026', 500,  560,  30, 35, 2000, 6000, '', 1),
('UT0008', 'P302', '05/2026', 1400, 1580, 92, 105, 2000, 6000, '', 1),
('UT0009', 'P303', '05/2026', 700,  790,  40, 47, 2000, 6000, '', 1),
('UT0010', 'P304', '05/2026', 1600, 1820, 100, 118, 2000, 6000, '', 1);
GO

-- ================================================================
-- 7. INVOICES (hóa đơn tháng 05/2026, một số đã thanh toán)
--    electric_fee = (curr-prev)*don_gia / so_nguoi_o_phong (ước tính)
-- ================================================================
INSERT INTO Invoices (id, student_id, student_name, room_id, utility_id, month, room_fee, electric_fee, water_fee, paid) VALUES
('HD0001', 'SV001249', N'Nguyễn Văn An',   'P101', 'UT0001', '05/2026', 850000, 70000, 18000, 1),
('HD0002', 'SV001250', N'Trần Thị Bình',   'P101', 'UT0001', '05/2026', 850000, 70000, 18000, 1),
('HD0003', 'SV001251', N'Lê Văn Cường',    'P101', 'UT0001', '05/2026', 850000, 70000, 18000, 0),
('HD0004', 'SV001252', N'Phạm Thị Dung',   'P101', 'UT0001', '05/2026', 850000, 70000, 18000, 0),
('HD0005', 'SV001253', N'Hoàng Văn Em',    'P102', 'UT0002', '05/2026', 850000, 80000, 20000, 1),
('HD0006', 'SV001254', N'Vũ Thị Giang',    'P102', 'UT0002', '05/2026', 850000, 80000, 20000, 1),
('HD0007', 'SV001255', N'Đỗ Văn Hùng',     'P102', 'UT0002', '05/2026', 850000, 80000, 20000, 0),
('HD0008', 'SV001256', N'Ngô Thị Lan',     'P103', 'UT0003', '05/2026', 850000, 100000, 24000, 1),
('HD0009', 'SV001257', N'Bùi Văn Khánh',   'P103', 'UT0003', '05/2026', 850000, 100000, 24000, 0),
('HD0010', 'SV001258', N'Đặng Thị Mai',    'P104', 'UT0004', '05/2026', 650000, 63000, 15000, 1),
('HD0011', 'SV001259', N'Phan Văn Nam',    'P104', 'UT0004', '05/2026', 650000, 63000, 15000, 1),
('HD0012', 'SV001260', N'Lý Thị Oanh',     'P104', 'UT0004', '05/2026', 650000, 63000, 15000, 0),
('HD0013', 'SV001261', N'Trịnh Văn Phúc',  'P104', 'UT0004', '05/2026', 650000, 63000, 15000, 0),
('HD0014', 'SV001262', N'Vương Thị Quỳnh', 'P104', 'UT0004', '05/2026', 650000, 63000, 15000, 1),
('HD0015', 'SV001263', N'Đinh Văn Sơn',    'P201', 'UT0005', '05/2026', 850000, 65000, 16500, 1),
('HD0016', 'SV001264', N'Tô Thị Thảo',     'P201', 'UT0005', '05/2026', 850000, 65000, 16500, 0),
('HD0017', 'SV001265', N'Mai Văn Tùng',    'P201', 'UT0005', '05/2026', 850000, 65000, 16500, 1),
('HD0018', 'SV001266', N'Cao Thị Uyên',    'P201', 'UT0005', '05/2026', 850000, 65000, 16500, 0);
GO

-- ================================================================
-- 8. VIOLATIONS (vi phạm nội quy)
-- ================================================================
INSERT INTO Violations (id, student_id, student_name, room_id, vio_date, type, description, severity, fine, handled_by, status, note) VALUES
('VP0001', 'SV001251', N'Lê Văn Cường',  'P101', '2026-05-10', N'Gây tiếng ồn',        N'Mở nhạc lớn sau giờ giới nghiêm', 'LOW',      0,      N'Quản trị viên', 'PROCESSED', N'Đã nhắc nhở'),
('VP0002', 'SV001255', N'Đỗ Văn Hùng',   'P102', '2026-05-15', N'Sử dụng thiết bị cấm', N'Đun nấu trong phòng bằng bếp điện', 'MEDIUM',  100000, N'Quản trị viên', 'PROCESSED', ''),
('VP0003', 'SV001257', N'Bùi Văn Khánh', 'P103', '2026-05-20', N'Vi phạm giờ giới nghiêm', N'Về phòng sau 23h không báo trước', 'LOW', 0, N'Quản trị viên', 'PENDING', ''),
('VP0004', 'SV001259', N'Phan Văn Nam',  'P104', '2026-05-28', N'Hút thuốc trong phòng', N'Bị phát hiện hút thuốc trong phòng ở', 'HIGH', 200000, N'Quản trị viên', 'APPEALING', N'Sinh viên đang khiếu nại'),
('VP0005', 'SV001265', N'Mai Văn Tùng',  'P201', '2026-06-02', N'Khách ở qua đêm',      N'Cho người ngoài ở lại qua đêm không đăng ký', 'MEDIUM', 100000, '', 'PENDING', '');
GO

-- ================================================================
-- 9. NOTIFICATIONS (thông báo)
-- ================================================================
INSERT INTO Notifications (id, title, content, type, target, target_id, created_at, created_by, pinned) VALUES
('TB0001', N'Thông báo đóng tiền phòng tháng 05/2026',
 N'Đề nghị các bạn sinh viên hoàn tất thanh toán hóa đơn tháng 05/2026 trước ngày 15/06/2026.',
 'INVOICE', 'ALL', '', '2026-06-01 08:00:00', N'Quản trị viên', 1),
('TB0002', N'Lịch kiểm tra phòng định kỳ',
 N'Ban quản lý sẽ tiến hành kiểm tra vệ sinh và an toàn phòng ở vào ngày 20/06/2026.',
 'INSPECTION', 'ALL', '', '2026-06-05 09:00:00', N'Quản trị viên', 1),
('TB0003', N'Nhắc nhở vi phạm nội quy',
 N'Phòng P102 đã có sinh viên vi phạm quy định sử dụng thiết bị điện. Đề nghị các phòng chú ý.',
 'VIOLATION', 'ROOM', 'P102', '2026-05-16 10:30:00', N'Quản trị viên', 0),
('TB0004', N'Thông báo gia hạn hợp đồng',
 N'Các hợp đồng thuê phòng năm học 2025-2026 sẽ hết hạn vào 31/08/2026, sinh viên cần đăng ký gia hạn trước 01/08/2026.',
 'CONTRACT', 'ALL', '', '2026-06-10 14:00:00', N'Quản trị viên', 0),
('TB0005', N'Thông báo khẩn: Cắt nước sửa chữa',
 N'Khu nhà sẽ tạm ngừng cấp nước từ 8h-12h ngày 16/06/2026 để sửa chữa đường ống.',
 'URGENT', 'ALL', '', '2026-06-13 18:00:00', N'Quản trị viên', 1);
GO

-- ================================================================
-- 10. NOTIFICATION READS (một số sinh viên đã đọc)
-- ================================================================
INSERT INTO NotificationReads (notification_id, student_id, read_at) VALUES
('TB0001', 'SV001249', '2026-06-01 09:00:00'),
('TB0001', 'SV001250', '2026-06-01 09:05:00'),
('TB0001', 'SV001253', '2026-06-01 10:00:00'),
('TB0002', 'SV001249', '2026-06-05 11:00:00'),
('TB0004', 'SV001249', '2026-06-10 15:00:00'),
('TB0004', 'SV001256', '2026-06-10 16:00:00');
GO

PRINT N'✅ Đã chèn dữ liệu ảo (mock data) thành công.';
GO