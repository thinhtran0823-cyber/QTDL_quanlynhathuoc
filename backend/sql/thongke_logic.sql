USE quanlynhathuoc;

-- ============================================================
-- 1. FUNCTION: TÍNH TỔNG SỐ LƯỢNG BÁN TRONG N NGÀY GẦN NHẤT
-- ============================================================

DROP FUNCTION IF EXISTS fn_SoLuongBanTrongNgay;

DELIMITER $$

CREATE FUNCTION fn_SoLuongBanTrongNgay(
    p_MaThuoc INT,
    p_SoNgay INT
)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_TongBan INT DEFAULT 0;

    SELECT COALESCE(SUM(cthd.SoLuongBan), 0)
    INTO v_TongBan
    FROM ChiTietHoaDon cthd
    JOIN HoaDon hd ON cthd.MaHD = hd.MaHD
    JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
    WHERE lt.MaThuoc = p_MaThuoc
      AND hd.NgayLap >= DATE_SUB(CURDATE(), INTERVAL p_SoNgay DAY);

    RETURN v_TongBan;
END$$

DELIMITER ;


-- ============================================================
-- 2. VIEW: GỢI Ý NHẬP HÀNG THÔNG MINH
-- ============================================================

DROP VIEW IF EXISTS v_CanhBaoNhapHang;

CREATE VIEW v_CanhBaoNhapHang AS
SELECT
    t.MaThuoc,
    t.TenThuoc,
    t.DonViTinh,

    COALESCE(hsx.TenHang, 'Chưa rõ hãng') AS TenHang,

    t.GiaBan,

    -- Tổng tồn kho khả dụng (các lô còn hạn và đang hoạt động)
    COALESCE(
        SUM(
            CASE
                WHEN lt.HanSuDung > CURDATE()
                     AND lt.TrangThai = 1
                THEN lt.SoLuongTon
                ELSE 0
            END
        ), 0
    ) AS TonKhaDung,

    -- Tổng tồn kho đã hết hạn
    COALESCE(
        SUM(
            CASE
                WHEN lt.HanSuDung <= CURDATE()
                THEN lt.SoLuongTon
                ELSE 0
            END
        ), 0
    ) AS TonHetHan,

    -- Tổng số lượng bán trong 30 ngày gần nhất
    fn_SoLuongBanTrongNgay(t.MaThuoc, 30) AS DaBan30Ngay,

    -- Đề xuất số lượng cần nhập thêm
    GREATEST(
        0,
        (
            30 + fn_SoLuongBanTrongNgay(t.MaThuoc, 30)
        ) -
        COALESCE(
            SUM(
                CASE
                    WHEN lt.HanSuDung > CURDATE()
                         AND lt.TrangThai = 1
                    THEN lt.SoLuongTon
                    ELSE 0
                END
            ), 0
        )
    ) AS DeXuatNhapThem,

    -- Phân loại mức độ cảnh báo
    CASE
        WHEN COALESCE(
            SUM(
                CASE
                    WHEN lt.HanSuDung > CURDATE()
                         AND lt.TrangThai = 1
                    THEN lt.SoLuongTon
                    ELSE 0
                END
            ), 0
        ) = 0
            THEN 'HET_HANG'

        WHEN COALESCE(
            SUM(
                CASE
                    WHEN lt.HanSuDung > CURDATE()
                         AND lt.TrangThai = 1
                    THEN lt.SoLuongTon
                    ELSE 0
                END
            ), 0
        ) < 20
            THEN 'TON_THAP'

        WHEN fn_SoLuongBanTrongNgay(t.MaThuoc, 30) >= 20
            THEN 'BAN_CHAY'

        ELSE 'AN_TOAN'
    END AS TrangThaiCanhBao

FROM Thuoc t
LEFT JOIN HangSX hsx
    ON t.MaHangSX = hsx.MaHangSX
LEFT JOIN LoThuoc lt
    ON t.MaThuoc = lt.MaThuoc

GROUP BY
    t.MaThuoc,
    t.TenThuoc,
    t.DonViTinh,
    hsx.TenHang,
    t.GiaBan;


-- ============================================================
-- 3. STORED PROCEDURE: THỐNG KÊ DOANH THU
-- ============================================================

DROP PROCEDURE IF EXISTS sp_ThongKeDoanhThu;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE sp_ThongKeDoanhThu(
    IN p_LoaiBaoCao VARCHAR(10)
)
BEGIN

    -- Thống kê doanh thu theo ngày
    IF UPPER(p_LoaiBaoCao) = 'NGAY' THEN

        SELECT
            DATE(NgayLap) AS Ngay,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY DATE(NgayLap)
        ORDER BY Ngay DESC;

    -- Thống kê doanh thu theo tuần
    ELSEIF UPPER(p_LoaiBaoCao) = 'TUAN' THEN

        SELECT
            YEAR(NgayLap) AS Nam,
            WEEK(NgayLap, 1) AS TuanThu,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY
            YEAR(NgayLap),
            WEEK(NgayLap, 1)
        ORDER BY
            Nam DESC,
            TuanThu DESC;

    -- Thống kê doanh thu theo tháng
    ELSEIF UPPER(p_LoaiBaoCao) = 'THANG' THEN

        SELECT
            YEAR(NgayLap) AS Nam,
            MONTH(NgayLap) AS Thang,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY
            YEAR(NgayLap),
            MONTH(NgayLap)
        ORDER BY
            Nam DESC,
            Thang DESC;

    -- Trường hợp nhập sai tham số
    ELSE

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'Loai bao cao khong hop le. Vui long chon NGAY, TUAN hoac THANG.';

    END IF;

END$$

DELIMITER ;

-- Function 1: Tính tổng giá trị thiệt hại của các lô thuốc đã hết hạn nhưng còn tồn kho
DROP FUNCTION IF EXISTS fn_TongThietHaiHetHan;
DELIMITER $$
CREATE FUNCTION fn_TongThietHaiHetHan()
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_TongThietHai DECIMAL(15,2);
    SELECT COALESCE(SUM(SoLuongTon * GiaNhap), 0)
    INTO v_TongThietHai
    FROM LoThuoc
    WHERE HanSuDung < CURDATE() AND SoLuongTon > 0;
    RETURN v_TongThietHai;
END$$
DELIMITER ;

-- Function 2: Tính tổng lợi nhuận ròng của toàn hệ thống (Doanh thu - Giá vốn hàng bán - Thiệt hại hết hạn)
DROP FUNCTION IF EXISTS fn_TinhLoiNhuanRong;
DELIMITER $$
CREATE FUNCTION fn_TinhLoiNhuanRong()
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_DoanhThu DECIMAL(15,2);
    DECLARE v_GiaVon DECIMAL(15,2);
    DECLARE v_ThietHai DECIMAL(15,2);

    SELECT COALESCE(SUM(TongTien), 0) INTO v_DoanhThu FROM HoaDon;

    SELECT COALESCE(SUM(cthd.SoLuongBan * lt.GiaNhap), 0)
    INTO v_GiaVon
    FROM ChiTietHoaDon cthd
    JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo;

    SET v_ThietHai = fn_TongThietHaiHetHan();

    RETURN v_DoanhThu - v_GiaVon - v_ThietHai;
END$$
DELIMITER ;

-- Function 3: Kiểm tra trạng thái hạn sử dụng của một lô thuốc theo MaLo
DROP FUNCTION IF EXISTS fn_TrangThaiHanLoThuoc;
DELIMITER $$
CREATE FUNCTION fn_TrangThaiHanLoThuoc(p_MaLo INT)
RETURNS VARCHAR(30)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_SoNgay INT;
    SELECT DATEDIFF(HanSuDung, CURDATE()) INTO v_SoNgay
    FROM LoThuoc WHERE MaLo = p_MaLo;

    IF v_SoNgay IS NULL THEN
        RETURN 'KHONG_TON_TAI';
    ELSEIF v_SoNgay < 0 THEN
        RETURN 'HET_HAN';
    ELSEIF v_SoNgay <= 30 THEN
        RETURN 'SAP_HET_HAN';
    ELSE
        RETURN 'CON_HAN';
    END IF;
END$$
DELIMITER ;