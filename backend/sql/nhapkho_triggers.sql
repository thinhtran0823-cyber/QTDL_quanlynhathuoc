-- ============================================================================
-- DỰ ÁN QUẢN LÝ NHÀ THUỐC - MODULE 2: QUẢN LÝ KHO & NHẬP HÀNG
-- Tác giả: Phan Anh Ngữ (TV2)
-- File: backend/sql/nhapkho_triggers.sql
-- Mô tả: Trigger và View cho phân hệ Nhập kho theo chuẩn bài thực hành TH3
-- ============================================================================

USE quanlynhathuoc;

-- ============================================================================
-- 1. TRIGGER CHO BẢNG LoThuoc
-- ============================================================================

-- Trigger 1: Kiểm tra tính hợp lệ của dữ liệu lô thuốc trước khi thêm mới
-- - Số lô không rỗng sau khi cắt khoảng trắng thừa
-- - Hạn sử dụng phải sau ngày hiện tại (CURDATE())
-- - Nếu có ngày sản xuất thì hạn sử dụng phải sau ngày sản xuất
-- - Số lượng tồn phải lớn hơn 0
-- - Giá nhập không được âm
-- - Không trùng cặp (MaThuoc, SoLo)
-- - Chuẩn hóa SoLo và gán TrangThai mặc định là 1 (Hoạt động)
DROP TRIGGER IF EXISTS trg_LT_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_LT_BI_KiemTra
BEFORE INSERT ON LoThuoc
FOR EACH ROW
BEGIN
    -- Kiểm tra số lô không được rỗng
    IF NEW.SoLo IS NULL OR TRIM(NEW.SoLo) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lô không được để trống';
    END IF;

    -- Chuẩn hóa dữ liệu số lô và trạng thái
    SET NEW.SoLo = TRIM(NEW.SoLo);
    SET NEW.TrangThai = 1;

    -- Kiểm tra hạn sử dụng phải sau ngày hiện tại
    IF NEW.HanSuDung IS NULL OR NEW.HanSuDung <= CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hạn sử dụng phải sau ngày hiện tại';
    END IF;

    -- Kiểm tra hạn sử dụng phải sau ngày sản xuất (nếu có ngày sản xuất)
    IF NEW.NgaySanXuat IS NOT NULL AND NEW.HanSuDung <= NEW.NgaySanXuat THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hạn sử dụng phải sau ngày sản xuất';
    END IF;

    -- Kiểm tra số lượng tồn ban đầu phải > 0
    IF NEW.SoLuongTon IS NULL OR NEW.SoLuongTon <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng tồn của lô phải lớn hơn 0';
    END IF;

    -- Kiểm tra giá nhập không được âm
    IF NEW.GiaNhap IS NULL OR NEW.GiaNhap < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá nhập của lô không được âm';
    END IF;

    -- Kiểm tra trùng lặp số lô cho cùng một loại thuốc
    IF EXISTS (
        SELECT 1 FROM LoThuoc
        WHERE MaThuoc = NEW.MaThuoc AND SoLo = NEW.SoLo
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lô đã tồn tại cho thuốc này';
    END IF;
END$$
DELIMITER ;

-- Trigger 2: Tự động cộng dồn số lượng tồn vào bảng Thuoc sau khi nhập lô mới
-- Tương ứng Câu 3 TH3: Tự động cập nhật tổng tồn kho cho thuốc
DROP TRIGGER IF EXISTS trg_LT_AI_CongTonThuoc;
DELIMITER $$
CREATE TRIGGER trg_LT_AI_CongTonThuoc
AFTER INSERT ON LoThuoc
FOR EACH ROW
BEGIN
    UPDATE Thuoc
    SET SoLuongTon = SoLuongTon + NEW.SoLuongTon
    WHERE MaThuoc = NEW.MaThuoc;
END$$
DELIMITER ;


-- ============================================================================
-- 2. TRIGGER CHO BẢNG ChiTietPhieuNhap
-- ============================================================================

-- Trigger 3: Kiểm tra tính hợp lệ của chi tiết phiếu nhập trước khi thêm
-- - Số lượng nhập phải > 0
-- - Đơn giá nhập không được âm (>= 0)
DROP TRIGGER IF EXISTS trg_CTPN_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_CTPN_BI_KiemTra
BEFORE INSERT ON ChiTietPhieuNhap
FOR EACH ROW
BEGIN
    -- Kiểm tra số lượng nhập
    IF NEW.SoLuongNhap IS NULL OR NEW.SoLuongNhap <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng nhập phải lớn hơn 0';
    END IF;

    -- Kiểm tra đơn giá nhập
    IF NEW.DonGiaNhap IS NULL OR NEW.DonGiaNhap < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn giá nhập không được âm';
    END IF;
END$$
DELIMITER ;

-- Trigger 4: Tự động tính lại tổng tiền của Phiếu nhập sau khi thêm chi tiết
-- Cột ThanhTien là GENERATED ALWAYS AS (SoLuongNhap * DonGiaNhap) STORED
DROP TRIGGER IF EXISTS trg_CTPN_AI_TongTien;
DELIMITER $$
CREATE TRIGGER trg_CTPN_AI_TongTien
AFTER INSERT ON ChiTietPhieuNhap
FOR EACH ROW
BEGIN
    UPDATE PhieuNhap
    SET TongTien = (
        SELECT COALESCE(SUM(ThanhTien), 0)
        FROM ChiTietPhieuNhap
        WHERE MaPN = NEW.MaPN
    )
    WHERE MaPN = NEW.MaPN;
END$$
DELIMITER ;


-- ============================================================================
-- 3. TRIGGER CHO BẢNG PhieuNhap
-- ============================================================================

-- Trigger 5: Kiểm tra nhân viên lập phiếu và gán ngày nhập mặc định
-- - Nhân viên lập phiếu phải còn đang hoạt động (TrangThai = 1)
-- - Nếu NgayNhap chưa có (NULL) thì tự động gán NOW()
DROP TRIGGER IF EXISTS trg_PN_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_PN_BI_KiemTra
BEFORE INSERT ON PhieuNhap
FOR EACH ROW
BEGIN
    -- Kiểm tra nhân viên còn hoạt động hay không
    IF NOT EXISTS (
        SELECT 1 FROM NhanVien
        WHERE MaNV = NEW.MaNV AND TrangThai = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nhân viên không còn hoạt động';
    END IF;

    -- Tự động điền ngày nhập nếu client không truyền
    IF NEW.NgayNhap IS NULL THEN
        SET NEW.NgayNhap = NOW();
    END IF;
END$$
DELIMITER ;


-- ============================================================================
-- 4. VIEW PHÂN LOẠI TÌNH TRẠNG LÔ THUỐC
-- ============================================================================

-- View: v_LoThuoc_TinhTrang
-- Phân loại hạn dùng của lô thuốc:
-- - HET_HAN: SoNgayConLai < 0
-- - SAP_HET_HAN: 0 <= SoNgayConLai <= 30
-- - CON_HAN: SoNgayConLai > 30
DROP VIEW IF EXISTS v_LoThuoc_TinhTrang;
CREATE VIEW v_LoThuoc_TinhTrang AS
SELECT 
    lt.MaLo,
    lt.MaThuoc,
    t.TenThuoc,
    lt.SoLo,
    lt.NgaySanXuat,
    lt.HanSuDung,
    lt.SoLuongTon,
    lt.GiaNhap,
    DATEDIFF(lt.HanSuDung, CURDATE()) AS SoNgayConLai,
    CASE 
        WHEN DATEDIFF(lt.HanSuDung, CURDATE()) < 0 THEN 'HET_HAN'
        WHEN DATEDIFF(lt.HanSuDung, CURDATE()) <= 30 THEN 'SAP_HET_HAN'
        ELSE 'CON_HAN'
    END AS PhanLoai
FROM LoThuoc lt
JOIN Thuoc t ON lt.MaThuoc = t.MaThuoc;
