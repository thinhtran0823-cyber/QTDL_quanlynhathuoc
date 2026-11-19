const pool = require('../config/db');

// Gọi Stored Procedure thống kê doanh thu theo NGAY / TUAN / THANG
const getDoanhThu = async (req, res) => {
    const { loai } = req.query;
    try {
        const [rows] = await pool.query('CALL sp_ThongKeDoanhThu(?)', [loai || 'NGAY']);
        return res.status(200).json({
            success: true,
            message: 'Lấy dữ liệu thống kê doanh thu thành công',
            data: rows[0]
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// Tính toán Doanh thu, Giá vốn hàng bán, Tổng chi nhập kho, Thiệt hại hết hạn & Lợi nhuận thực tế
const getLoiNhuan = async (req, res) => {
    try {
        const querySQL = `
            SELECT 
                COALESCE((
                    SELECT SUM(cthd.SoLuongBan * cthd.GiaBan)
                    FROM ChiTietHoaDon cthd
                ), 0) AS TongDoanhThu,
                COALESCE((
                    SELECT SUM(cthd.SoLuongBan * lt.GiaNhap)
                    FROM ChiTietHoaDon cthd
                    JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
                ), 0) AS TongChiPhiVon,
                COALESCE((
                    SELECT SUM(TongTien)
                    FROM PhieuNhap
                ), 0) AS TongChiNhapKho,
                COALESCE((
                    SELECT SUM(SoLuongTon * GiaNhap)
                    FROM LoThuoc
                    WHERE HanSuDung < CURDATE() AND SoLuongTon > 0
                ), 0) AS ThietHaiHetHan
        `;
        const [rows] = await pool.query(querySQL);
        const row = rows[0] || {};

        const tongDoanhThu = Number(row.TongDoanhThu || 0);
        const tongChiPhiVon = Number(row.TongChiPhiVon || 0);
        const tongChiNhapKho = Number(row.TongChiNhapKho || 0);
        const thietHaiHetHan = Number(row.ThietHaiHetHan || 0);
        const loiNhuanRong = tongDoanhThu - tongChiPhiVon - thietHaiHetHan;

        return res.status(200).json({
            success: true,
            message: 'Tính toán lợi nhuận thực tế thành công',
            data: {
                TongDoanhThu: tongDoanhThu,
                TongChiPhiVon: tongChiPhiVon,
                TongChiNhapKho: tongChiNhapKho,
                ThietHaiHetHan: thietHaiHetHan,
                LoiNhuanRong: loiNhuanRong
            }
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

module.exports = { getDoanhThu, getLoiNhuan };