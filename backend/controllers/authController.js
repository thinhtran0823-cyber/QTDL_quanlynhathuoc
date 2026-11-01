const pool = require('../config/db');

// API Đăng nhập hệ thống
const login = async (req, res) => {
    const { tenDangNhap, matKhau } = req.body;

    if (!tenDangNhap || !matKhau) {
        return res.status(400).json({
            success: false,
            message: 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!',
            data: null
        });
    }

    try {
        const [rows] = await pool.query(
            `SELECT tk.TenDangNhap, tk.Quyen, nv.MaNV, nv.HoTen, nv.ChucVu 
             FROM TaiKhoan tk 
             JOIN NhanVien nv ON tk.MaNV = nv.MaNV 
             WHERE tk.TenDangNhap = ? AND tk.MatKhau = ?`,
            [tenDangNhap, matKhau]
        );

        if (rows.length === 0) {
            return res.status(401).json({
                success: false,
                message: 'Tài khoản hoặc mật khẩu không chính xác!',
                data: null
            });
        }

        return res.status(200).json({
            success: true,
            message: 'Đăng nhập thành công!',
            data: rows[0]
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

module.exports = { login };