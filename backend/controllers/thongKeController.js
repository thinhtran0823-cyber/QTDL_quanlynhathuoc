const pool = require('../config/db');

const getLoiNhuan = async (req, res) => {
    try {
        // 1. Tổng doanh thu & Tổng số hóa đơn
        const [[doanhThuRow]] = await pool.query(`
            SELECT 
                COUNT(MaHD) AS TongSoHoaDon,
                COALESCE(SUM(TongTien), 0) AS TongDoanhThu
            FROM HoaDon
        `);

        // 2. Tổng giá vốn hàng đã bán
        const [[giaVonRow]] = await pool.query(`
            SELECT COALESCE(SUM(cthd.SoLuongBan * lt.GiaNhap), 0) AS TongChiPhiVon
            FROM ChiTietHoaDon cthd
            JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
        `);

        // 3. Tổng chi nhập kho
        const [[nhapKhoRow]] = await pool.query(`
            SELECT COALESCE(SUM(TongTien), 0) AS TongChiNhapKho
            FROM PhieuNhap
        `);

        // 4. GỌI TRỰC TIẾP 2 FUNCTION TỪ MYSQL
        const [[fnRow]] = await pool.query(`
            SELECT 
                fn_TongThietHaiHetHan() AS ThietHaiHetHan,
                fn_TinhLoiNhuanRong() AS LoiNhuanRong
        `);

        return res.status(200).json({
            success: true,
            data: {
                TongSoHoaDon: doanhThuRow.TongSoHoaDon,
                TongDoanhThu: Number(doanhThuRow.TongDoanhThu),
                TongChiPhiVon: Number(giaVonRow.TongChiPhiVon),
                TongChiNhapKho: Number(nhapKhoRow.TongChiNhapKho),
                ThietHaiHetHan: Number(fnRow.ThietHaiHetHan),
                LoiNhuanRong: Number(fnRow.LoiNhuanRong)
            }
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const getDoanhThu = async (req, res) => {
    const { loai } = req.query; // 'NGAY', 'TUAN', 'THANG'
    try {
        const [rows] = await pool.query('CALL sp_ThongKeDoanhThu(?)', [loai || 'NGAY']);
        return res.status(200).json({ success: true, data: rows[0] });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = { getLoiNhuan, getDoanhThu };