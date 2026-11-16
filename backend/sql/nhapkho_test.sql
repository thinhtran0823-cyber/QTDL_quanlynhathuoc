-- ============================================================================
-- DỰ ÁN QUẢN LÝ NHÀ THUỐC - MODULE 2: QUẢN LÝ KHO & NHẬP HÀNG
-- Tác giả: Phan Anh Ngữ (TV2)
-- File: backend/sql/nhapkho_test.sql
-- Mô tả: Kịch bản kiểm thử thủ công (chạy tay) cơ sở dữ liệu cho module Nhập kho
-- Hướng dẫn: Mở file trong MySQL Workbench hoặc DBeaver, bôi đen từng ca kiểm thử
--            để chạy và đối chiếu với kết quả mong đợi.
-- Lưu ý: Mọi ca đều được bọc trong START TRANSACTION ... ROLLBACK để không làm bẩn dữ liệu.
-- ============================================================================

USE quanlynhathuoc;

-- ============================================================================
-- TEST CASE 1: HẠN SỬ DỤNG ĐÃ QUA NGÀY
-- Mong đợi: Báo lỗi "Hạn sử dụng phải sau ngày hiện tại" (SQLSTATE 45000)
-- ============================================================================
START TRANSACTION;
-- Thử thêm lô thuốc có hạn sử dụng là ngày trong quá khứ (2020-01-01)
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-EXP-PAST', '2019-01-01', '2020-01-01', 50, 45000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 2: HẠN SỬ DỤNG TRƯỚC HOẶC BẰNG NGÀY SẢN XUẤT
-- Mong đợi: Báo lỗi "Hạn sử dụng phải sau ngày sản xuất" (SQLSTATE 45000)
-- ============================================================================
START TRANSACTION;
-- Thử thêm lô thuốc có hạn sử dụng (2027-01-01) trước ngày sản xuất (2027-06-01)
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-EXP-INVALID', '2027-06-01', '2027-01-01', 50, 45000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 3: SỐ LÔ ĐỂ TRỐNG HOẶC CHỈ CHỨA KHOẢNG TRẮNG
-- Mong đợi: Báo lỗi "Số lô không được để trống" (SQLSTATE 45000)
-- ============================================================================
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, '   ', '2026-01-01', '2028-01-01', 50, 45000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 4: SỐ LƯỢNG 0 HOẶC ÂM
-- ============================================================================
-- 4a. Số lượng tồn của LoThuoc <= 0
-- Mong đợi: Báo lỗi "Số lượng tồn của lô phải lớn hơn 0" (SQLSTATE 45000)
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-QTY-ZERO', '2026-01-01', '2028-01-01', 0, 45000.00);
ROLLBACK;

-- 4b. Số lượng nhập của ChiTietPhieuNhap <= 0
-- Mong đợi: Báo lỗi "Số lượng nhập phải lớn hơn 0" (SQLSTATE 45000)
START TRANSACTION;
INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap)
VALUES (1, 1, -10, 45000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 5: ĐƠN GIÁ HOẶC GIÁ NHẬP ÂM
-- ============================================================================
-- 5a. Giá nhập của LoThuoc < 0
-- Mong đợi: Báo lỗi "Giá nhập của lô không được âm" (SQLSTATE 45000)
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-PRICE-NEG', '2026-01-01', '2028-01-01', 50, -5000.00);
ROLLBACK;

-- 5b. Đơn giá nhập của ChiTietPhieuNhap < 0
-- Mong đợi: Báo lỗi "Đơn giá nhập không được âm" (SQLSTATE 45000)
START TRANSACTION;
INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap)
VALUES (1, 1, 10, -1000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 6: TRÙNG CẶP (MaThuoc, SoLo)
-- Mong đợi: Báo lỗi "Số lô đã tồn tại cho thuốc này" (SQLSTATE 45000)
-- (Trong CSDL mẫu, thuốc MaThuoc=1 đã có số lô 'LOT-PARA-01')
-- ============================================================================
START TRANSACTION;
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-PARA-01', '2026-01-01', '2028-01-01', 30, 45000.00);
ROLLBACK;


-- ============================================================================
-- TEST CASE 7: NHÂN VIÊN KHÔNG HOẠT ĐỘNG (TrangThai = 0) LẬP PHIẾU
-- Mong đợi: Báo lỗi "Nhân viên không còn hoạt động" (SQLSTATE 45000)
-- ============================================================================
START TRANSACTION;
-- Thêm tạm một nhân viên đã nghỉ việc (TrangThai = 0)
INSERT INTO NhanVien (MaNV, HoTen, ChucVu, SoDienThoai, TrangThai)
VALUES (999, 'Nhân Viên Nghỉ Việc', 'NhanVien', '0999999999', 0);

-- Thử lập phiếu nhập với nhân viên này
INSERT INTO PhieuNhap (MaNCC, MaNV, NgayNhap)
VALUES (1, 999, NOW());
ROLLBACK;


-- ============================================================================
-- TEST CASE 8: NHẬP KHO HỢP LỆ (HAPPY PATH)
-- Mong đợi:
-- 1. LoThuoc được thêm thành công, chuẩn hóa số lô và TrangThai = 1
-- 2. Trigger trg_LT_AI_CongTonThuoc: Thuoc.SoLuongTon tăng đúng số lượng nhập (+50)
-- 3. ChiTietPhieuNhap được thêm thành công
-- 4. Trigger trg_CTPN_AI_TongTien: PhieuNhap.TongTien tự tính = 50 * 45,000 = 2,250,000
-- ============================================================================
START TRANSACTION;

-- 1. Ghi nhận số lượng tồn ban đầu của thuốc MaThuoc = 1
SELECT MaThuoc, TenThuoc, SoLuongTon AS SoLuongTonTruocKhiNhap 
FROM Thuoc 
WHERE MaThuoc = 1;

-- 2. Thêm phiếu nhập mới với nhân viên hợp lệ (MaNV = 3 đang hoạt động)
INSERT INTO PhieuNhap (MaNCC, MaNV, NgayNhap)
VALUES (1, 3, NOW());
SET @new_mapn = LAST_INSERT_ID();

-- 3. Thêm lô thuốc mới (số lượng nhập 50, giá nhập 45,000)
INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)
VALUES (1, 'LOT-TEST-VALID-01', '2026-01-01', '2028-01-01', 50, 45000.00);
SET @new_malo = LAST_INSERT_ID();

-- 4. Thêm chi tiết phiếu nhập
INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap)
VALUES (@new_mapn, @new_malo, 50, 45000.00);

-- 5. ĐỐI CHIẾU KẾT QUẢ:
-- 5a. Kiểm tra tồn kho của thuốc: phải tăng đúng 50 đơn vị so với trước khi nhập
SELECT MaThuoc, TenThuoc, SoLuongTon AS SoLuongTonSauKhiNhap 
FROM Thuoc 
WHERE MaThuoc = 1;

-- 5b. Kiểm tra tổng tiền của phiếu nhập: phải bằng 2,250,000.00
SELECT MaPN, MaNCC, MaNV, NgayNhap, TongTien 
FROM PhieuNhap 
WHERE MaPN = @new_mapn;

-- 5c. Kiểm tra dòng trong view phân loại tình trạng lô thuốc
SELECT * FROM v_LoThuoc_TinhTrang WHERE MaLo = @new_malo;

ROLLBACK; -- Hoàn tác dữ liệu thử nghiệm, không làm bẩn CSDL


-- ============================================================================
-- TEST CASE 9: KIỂM TRA VIEW v_LoThuoc_TinhTrang
-- Mong đợi: Hiển thị danh sách lô thuốc kèm số ngày còn lại và phân loại (CON_HAN, SAP_HET_HAN, HET_HAN)
-- ============================================================================
SELECT 
    MaLo, 
    TenThuoc, 
    SoLo, 
    NgaySanXuat, 
    HanSuDung, 
    SoLuongTon, 
    GiaNhap, 
    SoNgayConLai, 
    PhanLoai
FROM v_LoThuoc_TinhTrang
ORDER BY HanSuDung ASC;
