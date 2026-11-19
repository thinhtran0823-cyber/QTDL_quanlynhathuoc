USE quanlynhathuoc;

-- =========================================================================
-- PHẦN 1: LOGIC & RÀNG BUỘC CHO BẢNG NHÀ CUNG CẤP (NhaCungCap)
-- =========================================================================

DROP TRIGGER IF EXISTS trg_NCC_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_NCC_BI_KiemTra
BEFORE INSERT ON NhaCungCap
FOR EACH ROW
BEGIN
    IF NEW.TenNCC IS NULL OR TRIM(NEW.TenNCC) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp không được để trống!';
    END IF;

    SET NEW.TenNCC = TRIM(NEW.TenNCC);

    IF EXISTS (SELECT 1 FROM NhaCungCap WHERE LOWER(TenNCC) = LOWER(NEW.TenNCC)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhaCungCap WHERE SoDienThoai = NEW.SoDienThoai) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhà cung cấp khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_NCC_BU_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_NCC_BU_KiemTra
BEFORE UPDATE ON NhaCungCap
FOR EACH ROW
BEGIN
    IF NEW.TenNCC IS NULL OR TRIM(NEW.TenNCC) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp không được để trống!';
    END IF;

    SET NEW.TenNCC = TRIM(NEW.TenNCC);

    IF EXISTS (SELECT 1 FROM NhaCungCap WHERE LOWER(TenNCC) = LOWER(NEW.TenNCC) AND MaNCC <> OLD.MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhaCungCap WHERE SoDienThoai = NEW.SoDienThoai AND MaNCC <> OLD.MaNCC) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhà cung cấp khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_NCC_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_NCC_BD_KiemTra
BEFORE DELETE ON NhaCungCap
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM PhieuNhap WHERE MaNCC = OLD.MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa vì nhà cung cấp này đã có phiếu nhập kho!';
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_ThemNhaCungCap;
DELIMITER $$
CREATE PROCEDURE sp_ThemNhaCungCap(
    IN p_TenNCC VARCHAR(200),
    IN p_SoDienThoai VARCHAR(20)
)
BEGIN
    INSERT INTO NhaCungCap (TenNCC, SoDienThoai)
    VALUES (p_TenNCC, p_SoDienThoai);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_SuaNhaCungCap;
DELIMITER $$
CREATE PROCEDURE sp_SuaNhaCungCap(
    IN p_MaNCC INT,
    IN p_TenNCC VARCHAR(200),
    IN p_SoDienThoai VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE MaNCC = p_MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy nhà cung cấp để cập nhật!';
    END IF;

    UPDATE NhaCungCap
    SET TenNCC = p_TenNCC,
        SoDienThoai = p_SoDienThoai
    WHERE MaNCC = p_MaNCC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_XoaNhaCungCap;
DELIMITER $$
CREATE PROCEDURE sp_XoaNhaCungCap(
    IN p_MaNCC INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE MaNCC = p_MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy nhà cung cấp để xóa!';
    END IF;

    DELETE FROM NhaCungCap WHERE MaNCC = p_MaNCC;
END$$
DELIMITER ;

-- =========================================================================
-- PHẦN 2: LOGIC & RÀNG BUỘC CHO BẢNG HÃNG SẢN XUẤT (HangSX)
-- =========================================================================

DROP TRIGGER IF EXISTS trg_HangSX_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_HangSX_BI_KiemTra
BEFORE INSERT ON HangSX
FOR EACH ROW
BEGIN
    IF NEW.TenHang IS NULL OR TRIM(NEW.TenHang) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất không được để trống!';
    END IF;

    SET NEW.TenHang = TRIM(NEW.TenHang);

    IF EXISTS (SELECT 1 FROM HangSX WHERE LOWER(TenHang) = LOWER(NEW.TenHang)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.QuocGia IS NOT NULL AND TRIM(NEW.QuocGia) <> '' THEN
        SET NEW.QuocGia = TRIM(NEW.QuocGia);
        IF NEW.QuocGia REGEXP '^[0-9]+$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tên quốc gia không hợp lệ!';
        END IF;
    ELSE
        SET NEW.QuocGia = NULL;
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_HangSX_BU_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_HangSX_BU_KiemTra
BEFORE UPDATE ON HangSX
FOR EACH ROW
BEGIN
    IF NEW.TenHang IS NULL OR TRIM(NEW.TenHang) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất không được để trống!';
    END IF;

    SET NEW.TenHang = TRIM(NEW.TenHang);

    IF EXISTS (SELECT 1 FROM HangSX WHERE LOWER(TenHang) = LOWER(NEW.TenHang) AND MaHangSX <> OLD.MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.QuocGia IS NOT NULL AND TRIM(NEW.QuocGia) <> '' THEN
        SET NEW.QuocGia = TRIM(NEW.QuocGia);
        IF NEW.QuocGia REGEXP '^[0-9]+$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tên quốc gia không hợp lệ!';
        END IF;
    ELSE
        SET NEW.QuocGia = NULL;
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_HangSX_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_HangSX_BD_KiemTra
BEFORE DELETE ON HangSX
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Thuoc WHERE MaHangSX = OLD.MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa vì hãng sản xuất đang có thuốc trong danh mục!';
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_ThemHangSX;
DELIMITER $$
CREATE PROCEDURE sp_ThemHangSX(
    IN p_TenHang VARCHAR(200),
    IN p_QuocGia VARCHAR(100)
)
BEGIN
    INSERT INTO HangSX (TenHang, QuocGia)
    VALUES (p_TenHang, p_QuocGia);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_SuaHangSX;
DELIMITER $$
CREATE PROCEDURE sp_SuaHangSX(
    IN p_MaHangSX INT,
    IN p_TenHang VARCHAR(200),
    IN p_QuocGia VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy hãng sản xuất để cập nhật!';
    END IF;

    UPDATE HangSX
    SET TenHang = p_TenHang,
        QuocGia = p_QuocGia
    WHERE MaHangSX = p_MaHangSX;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_XoaHangSX;
DELIMITER $$
CREATE PROCEDURE sp_XoaHangSX(
    IN p_MaHangSX INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy hãng sản xuất để xóa!';
    END IF;

    DELETE FROM HangSX WHERE MaHangSX = p_MaHangSX;
END$$
DELIMITER ;