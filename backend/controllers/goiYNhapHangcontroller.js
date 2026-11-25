const pool = require('../config/db');

/**
 * GET /api/goi-y-nhap-hang
 * Truy vấn dữ liệu từ View v_CanhBaoNhapHang
 * Hỗ trợ lọc theo query ?nhom=ALL | CAN_NHAP | TON_THAP | BAN_CHAY
 */
const getGoiYNhapHang = async (req, res) => {
    try {
        const nhom = (req.query.nhom || 'CAN_NHAP').trim().toUpperCase();
        const nguongTon = parseInt(req.query.nguongTon, 10) || 20;

        let querySQL = `
            SELECT 
                MaThuoc,
                TenThuoc,
                DonViTinh,
                TenHang,
                GiaBan,
                TonKhaDung,
                TonHetHan,
                DaBan30Ngay,
                DeXuatNhapThem,
                TrangThaiCanhBao
            FROM v_CanhBaoNhapHang
        `;
        const params = [];

        if (nhom === 'TON_THAP') {
            querySQL += ' WHERE TonKhaDung < ?';
            params.push(nguongTon);
        } else if (nhom === 'BAN_CHAY') {
            querySQL += ' WHERE DaBan30Ngay > 0 ORDER BY DaBan30Ngay DESC, TonKhaDung ASC LIMIT 20';
        } else if (nhom === 'CAN_NHAP') {
            // Lấy cả 2 nhóm: Tồn khả dụng dưới ngưỡng an toàn HOẶC Thuốc bán chạy
            querySQL += ' WHERE TonKhaDung < ? OR DaBan30Ngay >= 15';
            params.push(nguongTon);
        }

        if (nhom !== 'BAN_CHAY') {
            querySQL += ' ORDER BY TonKhaDung ASC, DaBan30Ngay DESC';
        }

        const [rows] = await pool.query(querySQL, params);

        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách gợi ý nhập hàng thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message,
            data: null
        });
    }
};

module.exports = {
    getGoiYNhapHang
};