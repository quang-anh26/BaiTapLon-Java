-- ================================================================
-- MIGRATION cho PostgreSQL (Supabase) — chạy trong SQL Editor
-- Thêm cột "status" cho bảng invoices để hỗ trợ 3 trạng thái:
-- UNPAID (Chưa thanh toán) / PENDING (Chờ xử lí) / PAID (Đã thanh toán)
-- ================================================================

ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS status VARCHAR(10) NOT NULL DEFAULT 'UNPAID';

-- Đồng bộ dữ liệu cũ: hóa đơn nào paid=true thì status='PAID'
UPDATE invoices
SET status = CASE WHEN paid THEN 'PAID' ELSE 'UNPAID' END;

-- (Tuỳ chọn) ràng buộc giá trị hợp lệ cho cột status
ALTER TABLE invoices
    ADD CONSTRAINT chk_inv_status CHECK (status IN ('UNPAID', 'PENDING', 'PAID'));

-- Kiểm tra lại
SELECT id, student_name, month, paid, status FROM invoices ORDER BY id;
