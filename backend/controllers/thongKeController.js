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
const getChiTietXuatExcel = async (req, res) => {
    const { loai, giaTri } = req.query;
    try {
        let whereClause = '';
        let params = [];

        if (loai === 'NGAY') {
            // giaTri: YYYY-MM-DD
            whereClause = 'WHERE DATE(hd.NgayLap) = ?';
            params = [giaTri];
        } else if (loai === 'THANG') {
            // giaTri: YYYY-MM
            whereClause = "WHERE DATE_FORMAT(hd.NgayLap, '%Y-%m') = ?";
            params = [giaTri];
        } else if (loai === 'NAM') {
            // giaTri: YYYY
            whereClause = 'WHERE YEAR(hd.NgayLap) = ?';
            params = [Number(giaTri)];
        } else {
            return res.status(400).json({ success: false, message: 'Định dạng thời gian không hợp lệ (NGAY, THANG, NAM)' });
        }

        const [rows] = await pool.query(`
            SELECT 
                hd.MaHD,
                DATE_FORMAT(hd.NgayLap, '%d/%m/%Y %H:%i:%s') AS NgayLap,
                nv.HoTen AS TenNV,
                COALESCE(kh.TenKH, 'Khách vãng lai') AS TenKH,
                hd.TongTien AS TongTienHD,
                t.TenThuoc,
                COALESCE(t.DonViTinh, '') AS DonViTinh,
                lt.SoLo,
                DATE_FORMAT(lt.HanSuDung, '%d/%m/%Y') AS HanSuDung,
                ct.SoLuongBan,
                ct.GiaBan,
                ct.ThanhTien
            FROM HoaDon hd
            JOIN NhanVien nv ON hd.MaNV = nv.MaNV
            LEFT JOIN KhachHang kh ON hd.MaKH = kh.MaKH
            JOIN ChiTietHoaDon ct ON hd.MaHD = ct.MaHD
            JOIN LoThuoc lt ON ct.MaLo = lt.MaLo
            JOIN Thuoc t ON lt.MaThuoc = t.MaThuoc
            ${whereClause}
            ORDER BY hd.NgayLap DESC, hd.MaHD DESC, ct.MaCTHD ASC
        `, params);

        return res.status(200).json({ success: true, data: rows });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = { getLoiNhuan, getDoanhThu, getChiTietXuatExcel };
