USE quanlynhathuoc;

-- =========================================================================
-- 1. TRIGGER CHẶN XÓA THUỐC (Thuoc) KHI ĐÃ CÓ LÔ THUỐC / NHẬP KHO / HÓA ĐƠN
-- =========================================================================
DROP TRIGGER IF EXISTS trg_Thuoc_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_Thuoc_BD_KiemTra
BEFORE DELETE ON Thuoc
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM ChiTietHoaDon cthd
        JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
        WHERE lt.MaThuoc = OLD.MaThuoc
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đã phát sinh giao dịch trong hóa đơn bán hàng!';
    END IF;

    IF EXISTS (
        SELECT 1 
        FROM ChiTietPhieuNhap ctpn
        JOIN LoThuoc lt ON ctpn.MaLo = lt.MaLo
        WHERE lt.MaThuoc = OLD.MaThuoc
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đã có lịch sử nhập kho!';
    END IF;

    IF EXISTS (SELECT 1 FROM LoThuoc WHERE MaThuoc = OLD.MaThuoc) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đang tồn tại lô thuốc trong hệ thống!';
    END IF;
END$$
DELIMITER ;

-- =========================================================================
-- 2. TRIGGER CHẶN XÓA KHÁCH HÀNG (KhachHang) KHI ĐÃ CÓ HÓA ĐƠN MUA HÀNG
-- =========================================================================
DROP TRIGGER IF EXISTS trg_KH_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_KH_BD_KiemTra
BEFORE DELETE ON KhachHang
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM HoaDon WHERE MaKH = OLD.MaKH) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa khách hàng này vì đã có lịch sử hóa đơn mua hàng!';
    END IF;
END$$
DELIMITER ;

-- =========================================================================
-- 3. TRIGGER CHẶN XÓA LÔ THUỐC (LoThuoc) KHI ĐÃ CÓ CHI TIẾT NHẬP / BÁN
-- =========================================================================
DROP TRIGGER IF EXISTS trg_LT_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_LT_BD_KiemTra
BEFORE DELETE ON LoThuoc
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE MaLo = OLD.MaLo) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa lô thuốc này vì đã có dữ liệu trong hóa đơn bán hàng!';
    END IF;

    IF EXISTS (SELECT 1 FROM ChiTietPhieuNhap WHERE MaLo = OLD.MaLo) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa lô thuốc này vì đã có dữ liệu trong phiếu nhập kho!';
    END IF;
END$$
DELIMITER ;