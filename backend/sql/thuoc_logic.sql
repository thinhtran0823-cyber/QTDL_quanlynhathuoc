USE quanlynhathuoc;
-- 2. Trigger BEFORE INSERT: Kiểm tra dữ liệu và luôn ép tồn kho ban đầu = 0
DROP TRIGGER IF EXISTS trg_Thuoc_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_Thuoc_BI_KiemTra
BEFORE INSERT ON Thuoc
FOR EACH ROW
BEGIN
    IF NEW.TenThuoc IS NULL OR TRIM(NEW.TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF NEW.DonViTinh IS NULL OR TRIM(NEW.DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF NEW.GiaBan IS NULL OR NEW.GiaBan < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    SET NEW.TenThuoc = TRIM(NEW.TenThuoc);
    SET NEW.DonViTinh = TRIM(NEW.DonViTinh);
    -- Tồn kho khi tạo thuốc mới luôn bằng 0 (chỉ tăng qua nghiệp vụ Nhập kho)
    SET NEW.SoLuongTon = 0;
END$$
DELIMITER ;

-- 3. Trigger BEFORE UPDATE: Kiểm tra điều kiện khi cập nhật thông tin thuốc
DROP TRIGGER IF EXISTS trg_Thuoc_BU_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_Thuoc_BU_KiemTra
BEFORE UPDATE ON Thuoc
FOR EACH ROW
BEGIN
    IF NEW.TenThuoc IS NULL OR TRIM(NEW.TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF NEW.DonViTinh IS NULL OR TRIM(NEW.DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF NEW.GiaBan IS NULL OR NEW.GiaBan < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    SET NEW.TenThuoc = TRIM(NEW.TenThuoc);
    SET NEW.DonViTinh = TRIM(NEW.DonViTinh);
END$$
DELIMITER ;

-- =========================================================================
-- 2. TRIGGER TOÀN VẸN CHO BẢNG LoaiThuoc (CHỐNG TRÙNG & CHẶN XÓA KHI CÓ THUỐC)
-- =========================================================================
DROP TRIGGER IF EXISTS trg_LoaiThuoc_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_LoaiThuoc_BI_KiemTra
BEFORE INSERT ON LoaiThuoc
FOR EACH ROW
BEGIN
    SET NEW.TenLoai = TRIM(COALESCE(NEW.TenLoai, ''));
    SET NEW.DonViTinh = TRIM(COALESCE(NEW.DonViTinh, ''));

    IF NEW.TenLoai = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc không được để trống!';
    END IF;

    IF NEW.DonViTinh = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính mặc định của loại thuốc không được để trống!';
    END IF;

    IF EXISTS (SELECT 1 FROM LoaiThuoc WHERE LOWER(TenLoai) = LOWER(NEW.TenLoai)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc này đã tồn tại trong hệ thống!';
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_LoaiThuoc_BU_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_LoaiThuoc_BU_KiemTra
BEFORE UPDATE ON LoaiThuoc
FOR EACH ROW
BEGIN
    SET NEW.TenLoai = TRIM(COALESCE(NEW.TenLoai, ''));
    SET NEW.DonViTinh = TRIM(COALESCE(NEW.DonViTinh, ''));

    IF NEW.TenLoai = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc không được để trống!';
    END IF;

    IF NEW.DonViTinh = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính mặc định không được để trống!';
    END IF;

    IF EXISTS (
        SELECT 1 FROM LoaiThuoc 
        WHERE LOWER(TenLoai) = LOWER(NEW.TenLoai) AND MaLoai <> OLD.MaLoai
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc bị trùng với loại thuốc khác!';
    END IF;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_LoaiThuoc_BD_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_LoaiThuoc_BD_KiemTra
BEFORE DELETE ON LoaiThuoc
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Thuoc WHERE MaLoai = OLD.MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa loại thuốc này vì đang có thuốc thuộc loại này trong danh mục!';
    END IF;
END$$
DELIMITER ;

-- =========================================================================
-- 3. CẬP NHẬT STORED PROCEDURE sp_ThemThuoc & sp_CapNhatThuoc (THÊM p_MaLoai)
-- =========================================================================
DROP PROCEDURE IF EXISTS sp_ThemThuoc;
DELIMITER $$
CREATE PROCEDURE sp_ThemThuoc(
    IN p_TenThuoc VARCHAR(150),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT,
    IN p_MaLoai INT
)
BEGIN
    DECLARE v_GiaBan DECIMAL(15,2);

    IF TRIM(COALESCE(p_GiaBan, '')) = '' OR p_GiaBan NOT REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán phải là số hợp lệ và không được âm!';
    END IF;

    SET v_GiaBan = CAST(p_GiaBan AS DECIMAL(15,2));

    IF p_MaHangSX IS NOT NULL AND NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hãng sản xuất được chọn không tồn tại!';
    END IF;

    IF p_MaLoai IS NOT NULL AND NOT EXISTS (SELECT 1 FROM LoaiThuoc WHERE MaLoai = p_MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loại thuốc được chọn không tồn tại!';
    END IF;

    INSERT INTO Thuoc (TenThuoc, DonViTinh, GiaBan, SoLuongTon, MaHangSX, MaLoai)
    VALUES (p_TenThuoc, p_DonViTinh, v_GiaBan, 0, p_MaHangSX, p_MaLoai);

    SELECT LAST_INSERT_ID() AS MaThuoc;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_CapNhatThuoc;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatThuoc(
    IN p_MaThuoc INT,
    IN p_TenThuoc VARCHAR(150),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT,
    IN p_MaLoai INT
)
BEGIN
    DECLARE v_GiaBan DECIMAL(15,2);

    IF NOT EXISTS (SELECT 1 FROM Thuoc WHERE MaThuoc = p_MaThuoc) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy thuốc cần cập nhật!';
    END IF;

    IF TRIM(COALESCE(p_GiaBan, '')) = '' OR p_GiaBan NOT REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán phải là số hợp lệ và không được âm!';
    END IF;

    SET v_GiaBan = CAST(p_GiaBan AS DECIMAL(15,2));

    IF p_MaHangSX IS NOT NULL AND NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hãng sản xuất được chọn không tồn tại!';
    END IF;

    IF p_MaLoai IS NOT NULL AND NOT EXISTS (SELECT 1 FROM LoaiThuoc WHERE MaLoai = p_MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loại thuốc được chọn không tồn tại!';
    END IF;

    UPDATE Thuoc
    SET TenThuoc = p_TenThuoc,
        DonViTinh = p_DonViTinh,
        GiaBan = v_GiaBan,
        MaHangSX = p_MaHangSX,
        MaLoai = p_MaLoai
    WHERE MaThuoc = p_MaThuoc;
END$$
DELIMITER ;