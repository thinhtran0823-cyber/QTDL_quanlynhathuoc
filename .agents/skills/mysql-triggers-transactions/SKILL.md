---
name: mysql-triggers-transactions
description: Write MySQL 8 triggers, transactions, SIGNAL validation and stored procedures with error handling. Use for any task that mentions mysql, trigger, triggers, transaction, stored procedure, SIGNAL SQLSTATE, EXIT HANDLER, savepoint, deadlock, or automatic stock updates written in .sql files, even if the user does not ask for a skill.
tags: [mysql, sql, trigger, transaction]
---

# MySQL trigger và transaction

## Bố cục file .sql
- Mở đầu `USE <tên_csdl>;` rồi `DELIMITER $$` ... `DELIMITER ;`. Mỗi đối tượng có `DROP TRIGGER IF EXISTS` (hoặc `DROP VIEW/PROCEDURE IF EXISTS`) ngay trước để chạy lại nhiều lần không lỗi. MySQL không có `CREATE OR REPLACE TRIGGER`.
- Tên trigger: `trg_<VIẾT TẮT BẢNG>_<BI|BU|BD|AI|AU|AD>_<MôTả>`. Thủ tục: `usp_<MôTả>`.
- Comment tiếng Việt: trigger gắn bảng nào, bắt sự kiện nào, làm gì.

## Trigger kiểm tra (BEFORE)
- Kiểm tra bằng `IF ... THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thông báo tiếng Việt ngắn'; END IF;`.
- Chuẩn hóa dữ liệu bằng cách gán lại `NEW.cột` (`TRIM`, `UPPER`, `ROUND`).
- Xử lý NULL một cách tường minh (`NEW.diem IS NOT NULL AND ...`).
- Không INSERT cột GENERATED (ví dụ `ThanhTien`).

## Trigger cập nhật (AFTER)
- Dùng để cộng/trừ bảng khác hoặc ghi nhật ký. Không UPDATE chính bảng đang kích hoạt trigger (lỗi 1442).
- Trigger không được gọi `COMMIT`/`ROLLBACK` (lỗi 1422); lỗi trong trigger làm hủy cả câu lệnh gây ra nó.
- MỖI đại lượng (ví dụ tồn kho) chỉ do MỘT nơi cập nhật. Trước khi viết, tìm `CREATE TRIGGER` trong các file .sql hiện có; không tạo trigger UPDATE/DELETE trên bảng dùng chung nếu task không yêu cầu, tránh cộng/trừ hai lần.

## Transaction và xử lý lỗi (thủ tục)
```sql
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
  GET DIAGNOSTICS CONDITION 1 @loi = MESSAGE_TEXT;
  ROLLBACK;
  SELECT CONCAT('Thất bại: ', @loi) AS ThongBao;
END;
START TRANSACTION;
-- ... các thao tác ...
COMMIT;
```
- Dùng `SAVEPOINT`/`ROLLBACK TO` khi cần hủy một phần.
- Khóa dòng bằng `SELECT ... FOR UPDATE` và luôn khóa theo CÙNG thứ tự giữa các giao tác để tránh deadlock (lỗi 1213).
- Lỗi do `SIGNAL` 45000 có mã lỗi 1644; ứng dụng Node đọc `error.sqlMessage`.

## File kiểm tra chạy tay
- Mỗi ca ghi rõ `-- Mong đợi: ...`. Bọc trong `START TRANSACTION; ... ROLLBACK;` để không làm bẩn dữ liệu.
- Phủ cả ca lỗi (vi phạm từng điều kiện) và ca hợp lệ, rồi `SELECT` đối chiếu số liệu trước/sau.

## Tự rà trước khi xong
Không chạy mysql/docker nếu task không cho phép. Tự kiểm: `DELIMITER` đủ cặp, mỗi khối kết thúc `END$$`, tên cột/bảng đúng file schema gốc, mọi chuỗi đóng nháy.
