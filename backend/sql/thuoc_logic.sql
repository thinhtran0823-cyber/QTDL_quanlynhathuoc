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

-- 4. Stored Procedure Thêm thuốc mới (xử lý toàn bộ kiểm tra đầu vào từ Backend)
DROP PROCEDURE IF EXISTS sp_ThemThuoc;
DELIMITER $$
CREATE PROCEDURE sp_ThemThuoc(
    IN p_TenThuoc VARCHAR(200),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT
)
BEGIN
    IF p_TenThuoc IS NULL OR TRIM(p_TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF p_DonViTinh IS NULL OR TRIM(p_DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF p_GiaBan IS NULL OR TRIM(p_GiaBan) = '' OR TRIM(p_GiaBan) NOT REGEXP '^-?[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được để trống và phải là số hợp lệ';
    END IF;

    IF CAST(TRIM(p_GiaBan) AS DECIMAL(15,2)) < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    INSERT INTO Thuoc (TenThuoc, DonViTinh, GiaBan, SoLuongTon, MaHangSX)
    VALUES (TRIM(p_TenThuoc), TRIM(p_DonViTinh), CAST(TRIM(p_GiaBan) AS DECIMAL(15,2)), 0, p_MaHangSX);

    SELECT LAST_INSERT_ID() AS MaThuoc;
END$$
DELIMITER ;

-- 5. Stored Procedure Cập nhật thông tin thuốc (không cho sửa tồn kho)
DROP PROCEDURE IF EXISTS sp_CapNhatThuoc;
DELIMITER $$
CREATE PROCEDURE sp_CapNhatThuoc(
    IN p_MaThuoc INT,
    IN p_TenThuoc VARCHAR(200),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Thuoc WHERE MaThuoc = p_MaThuoc) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy thuốc cần cập nhật';
    END IF;

    IF p_TenThuoc IS NULL OR TRIM(p_TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF p_DonViTinh IS NULL OR TRIM(p_DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF p_GiaBan IS NULL OR TRIM(p_GiaBan) = '' OR TRIM(p_GiaBan) NOT REGEXP '^-?[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được để trống và phải là số hợp lệ';
    END IF;

    IF CAST(TRIM(p_GiaBan) AS DECIMAL(15,2)) < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    UPDATE Thuoc
    SET TenThuoc = TRIM(p_TenThuoc),
        DonViTinh = TRIM(p_DonViTinh),
        GiaBan = CAST(TRIM(p_GiaBan) AS DECIMAL(15,2)),
        MaHangSX = p_MaHangSX
    WHERE MaThuoc = p_MaThuoc;
END$$
DELIMITER ;