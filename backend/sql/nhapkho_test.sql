-- Kiểm tra thủ công module Nhập kho. Chạy từng ca riêng trong MySQL.
USE quanlynhathuoc;

-- Ca 1: Hạn sử dụng đã qua.
-- Mong đợi: lỗi "Hạn sử dụng phải sau ngày hiện tại"; rollback không thay đổi dữ liệu.
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'TEST-HSD-QUA', NULL, '2020-01-01', 5, 1000.00);
ROLLBACK;

-- Ca 2: Số lượng bằng 0 (đổi thành -1 để kiểm tra trường hợp âm).
-- Mong đợi: lỗi "Số lượng tồn của lô phải lớn hơn 0"; rollback không thay đổi dữ liệu.
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'TEST-SL-KHONG', NULL, '2028-01-01', 0, 1000.00);
ROLLBACK;

-- Ca 3: Trùng cặp MaThuoc, SoLo với dữ liệu mẫu có sẵn.
-- Mong đợi: lỗi "Số lô đã tồn tại cho thuốc này"; rollback không thay đổi dữ liệu.
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-PARA-01', NULL, '2028-01-01', 5, 1000.00);
ROLLBACK;

-- Ca 4: Nhập hợp lệ với thuốc 1, nhà cung cấp 1, nhân viên hoạt động 3.
-- Mong đợi: Thuoc.SoLuongTon tăng 5; PhieuNhap.TongTien bằng SUM(ThanhTien) của phiếu.
START TRANSACTION;
SELECT SoLuongTon INTO @ton_truoc FROM Thuoc WHERE MaThuoc = 1;
INSERT INTO PhieuNhap (MaNCC, MaNV) VALUES (1, 3);
SET @ma_pn_test = LAST_INSERT_ID();
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'TEST-VALID-2026-01', NULL, '2028-01-01', 5, 1000.00);
SET @ma_lo_test = LAST_INSERT_ID();
INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap)
VALUES (@ma_pn_test, @ma_lo_test, 5, 1000.00);
SELECT @ton_truoc AS TonTruoc,
       SoLuongTon AS TonSau,
       SoLuongTon - @ton_truoc AS ChenhLechMongDoiLa5
FROM Thuoc WHERE MaThuoc = 1;
SELECT pn.TongTien,
       (SELECT SUM(ThanhTien) FROM ChiTietPhieuNhap WHERE MaPN = @ma_pn_test) AS TongThanhTien,
       pn.TongTien = (SELECT SUM(ThanhTien) FROM ChiTietPhieuNhap WHERE MaPN = @ma_pn_test) AS TongTienDung
FROM PhieuNhap pn WHERE pn.MaPN = @ma_pn_test;
ROLLBACK;

-- Ca 5: Nhân viên mẫu 3 tạm chuyển sang không hoạt động trong transaction.
-- Mong đợi: lỗi "Nhân viên không còn hoạt động"; rollback khôi phục trạng thái nhân viên.
START TRANSACTION;
UPDATE NhanVien SET TrangThai = 0 WHERE MaNV = 3;
INSERT INTO PhieuNhap (MaNCC, MaNV) VALUES (1, 3);
ROLLBACK;
