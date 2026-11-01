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

// Tính toán Lợi nhuận ròng = Doanh thu bán - Giá vốn nhập (Lấy theo từng Lô Thuốc)
const getLoiNhuan = async (req, res) => {
    try {
        const querySQL = `
            SELECT 
                IFNULL(SUM(cthd.SoLuongBan * cthd.GiaBan), 0) AS TongDoanhThu,
                IFNULL(SUM(cthd.SoLuongBan * lt.GiaNhap), 0) AS TongChiPhiVon,
                IFNULL(SUM(cthd.SoLuongBan * (cthd.GiaBan - lt.GiaNhap)), 0) AS LoiNhuanRong
            FROM ChiTietHoaDon cthd
            JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo;
        `;
        const [rows] = await pool.query(querySQL);
        return res.status(200).json({
            success: true,
            message: 'Tính toán lợi nhuận ròng thành công',
            data: rows[0]
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

module.exports = { getDoanhThu, getLoiNhuan };