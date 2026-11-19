USE quanlynhathuoc;

-- =========================================================================
-- PHẦN 1: TRIGGER KIỂM TRA RÀNG BUỘC TRÊN BẢNG NHÂN VIÊN (NhanVien)
-- =========================================================================

DROP TRIGGER IF EXISTS trg_NV_BI_KiemTra;
DELIMITER $$
CREATE TRIGGER trg_NV_BI_KiemTra
BEFORE INSERT ON NhanVien
FOR EACH ROW
BEGIN
    IF NEW.HoTen IS NULL OR TRIM(NEW.HoTen) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Họ và tên nhân viên không được để trống!';
    END IF;

    IF NEW.ChucVu IS NULL OR TRIM(NEW.ChucVu) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chức vụ nhân viên không được để trống!';
    END IF;

    SET NEW.HoTen = TRIM(NEW.HoTen);
    SET NEW.ChucVu = TRIM(NEW.ChucVu);

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhanVien WHERE SoDienThoai = NEW.SoDienThoai) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhân viên khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;

    IF NEW.TrangThai IS NULL THEN
        SET NEW.TrangThai = 1;
    END IF;
END$$ DELIMITER ;  DROP TRIGGER IF EXISTS trg_NV_BU_KiemTra; DELIMITER $$
CREATE TRIGGER trg_NV_BU_KiemTra
BEFORE UPDATE ON NhanVien
FOR EACH ROW
BEGIN
    IF NEW.HoTen IS NULL OR TRIM(NEW.HoTen) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Họ và tên nhân viên không được để trống!';
    END IF;

    IF NEW.ChucVu IS NULL OR TRIM(NEW.ChucVu) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chức vụ nhân viên không được để trống!';
    END IF;

    SET NEW.HoTen = TRIM(NEW.HoTen);
    SET NEW.ChucVu = TRIM(NEW.ChucVu);

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!'; END IF; IF EXISTS (SELECT 1 FROM NhanVien WHERE SoDienThoai = NEW.SoDienThoai AND MaNV <> OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhân viên khác!'; END IF; ELSE SET NEW.SoDienThoai = NULL; END IF; END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_NV_BD_KiemTra;
DELIMITER $$ CREATE TRIGGER trg_NV_BD_KiemTra BEFORE DELETE ON NhanVien FOR EACH ROW BEGIN IF EXISTS (SELECT 1 FROM HoaDon WHERE MaNV = OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa nhân viên vì đã có lịch sử lập hóa đơn bán hàng!'; END IF;      IF EXISTS (SELECT 1 FROM PhieuNhap WHERE MaNV = OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa nhân viên vì đã có lịch sử lập phiếu nhập kho!'; END IF; END$$
DELIMITER ;

-- =========================================================================
-- PHẦN 2: TRIGGER KIỂM TRA RÀNG BUỘC TRÊN BẢNG TÀI KHOẢN (TaiKhoan)
-- =========================================================================

DROP TRIGGER IF EXISTS trg_TK_BI_KiemTra;
DELIMITER $$ CREATE TRIGGER trg_TK_BI_KiemTra BEFORE INSERT ON TaiKhoan FOR EACH ROW BEGIN IF NEW.TenDangNhap IS NULL OR TRIM(NEW.TenDangNhap) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được để trống!'; END IF;      SET NEW.TenDangNhap = TRIM(NEW.TenDangNhap);      IF NEW.TenDangNhap REGEXP '[[:space:]]' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được chứa khoảng trắng!'; END IF;      IF EXISTS (SELECT 1 FROM TaiKhoan WHERE LOWER(TenDangNhap) = LOWER(NEW.TenDangNhap)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập đã tồn tại, vui lòng chọn tài khoản khác!'; END IF;      IF NEW.MatKhau IS NULL OR TRIM(NEW.MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      IF CHAR_LENGTH(NEW.MatKhau) < 4 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu phải có ít nhất 4 ký tự!'; END IF;      IF NEW.Quyen IS NULL OR TRIM(NEW.Quyen) = '' THEN SET NEW.Quyen = 'BANHANG'; END IF; END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_TK_BU_KiemTra;
DELIMITER $$ CREATE TRIGGER trg_TK_BU_KiemTra BEFORE UPDATE ON TaiKhoan FOR EACH ROW BEGIN IF NEW.MatKhau IS NULL OR TRIM(NEW.MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      IF NEW.Quyen IS NULL OR TRIM(NEW.Quyen) = '' THEN SET NEW.Quyen = OLD.Quyen; END IF; END$$
DELIMITER ;

-- =========================================================================
-- PHẦN 3: STORED PROCEDURES XỬ LÝ NGHIỆP VỤ NHÂN VIÊN & TÀI KHOẢN
-- =========================================================================

DROP PROCEDURE IF EXISTS sp_ThemNhanVien;
DELIMITER $$ CREATE PROCEDURE sp_ThemNhanVien( IN p_HoTen VARCHAR(150), IN p_ChucVu VARCHAR(50), IN p_SoDienThoai VARCHAR(20), IN p_TenDangNhap VARCHAR(50), IN p_MatKhau VARCHAR(100), IN p_Quyen VARCHAR(30) ) BEGIN DECLARE v_NewMaNV INT; DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF p_TenDangNhap IS NULL OR TRIM(p_TenDangNhap) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được để trống!'; END IF;      IF p_MatKhau IS NULL OR TRIM(p_MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      START TRANSACTION; INSERT INTO NhanVien (HoTen, ChucVu, SoDienThoai, TrangThai) VALUES (p_HoTen, p_ChucVu, p_SoDienThoai, 1);          SET v_NewMaNV = LAST_INSERT_ID();          INSERT INTO TaiKhoan (TenDangNhap, MaNV, MatKhau, Quyen) VALUES (p_TenDangNhap, v_NewMaNV, p_MatKhau, IFNULL(NULLIF(TRIM(p_Quyen), ''), 'BANHANG')); COMMIT; END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_SuaNhanVien;
DELIMITER $$ CREATE PROCEDURE sp_SuaNhanVien( IN p_MaNV INT, IN p_HoTen VARCHAR(150), IN p_ChucVu VARCHAR(50), IN p_SoDienThoai VARCHAR(20), IN p_Quyen VARCHAR(30) ) BEGIN DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = p_MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy nhân viên để cập nhật!'; END IF;      START TRANSACTION; UPDATE NhanVien SET HoTen = p_HoTen, ChucVu = p_ChucVu, SoDienThoai = p_SoDienThoai WHERE MaNV = p_MaNV;          IF p_Quyen IS NOT NULL AND TRIM(p_Quyen) <> '' THEN UPDATE TaiKhoan SET Quyen = TRIM(p_Quyen) WHERE MaNV = p_MaNV; END IF; COMMIT; END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_XoaNhanVien;
DELIMITER $$ CREATE PROCEDURE sp_XoaNhanVien( IN p_MaNV INT ) BEGIN DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = p_MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy nhân viên để xóa!'; END IF;      START TRANSACTION; DELETE FROM TaiKhoan WHERE MaNV = p_MaNV; DELETE FROM NhanVien WHERE MaNV = p_MaNV; COMMIT; END$$
DELIMITER ;