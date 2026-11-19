const pool = require('../config/db');

// Hàm tiện ích xử lý lỗi trả về từ MySQL Trigger / Stored Procedure
const handleSqlError = (res, error) => {
    if (error.sqlState === '45000' || error.errno === 1644) {
        return res.status(400).json({
            success: false,
            message: error.sqlMessage || error.message
        });
    }
    if (error.errno === 1451) {
        return res.status(400).json({
            success: false,
            message: 'Không thể xóa nhân viên do đã có lịch sử giao dịch bán/nhập hàng!'
        });
    }
    return res.status(500).json({
        success: false,
        message: error.message
    });
};

const getNhanVien = async (req, res) => {
    try {
        const querySQL = `
            SELECT nv.MaNV, nv.HoTen, nv.ChucVu, nv.SoDienThoai, nv.TrangThai, tk.TenDangNhap, tk.Quyen
            FROM NhanVien nv
            LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaNV
            ORDER BY nv.MaNV DESC
        `;
        const [rows] = await pool.query(querySQL);
        return res.status(200).json({ success: true, data: rows });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const themNhanVien = async (req, res) => {
    const { hoTen, chucVu, soDienThoai, tenDangNhap, matKhau, quyen } = req.body;
    try {
        await pool.query(
            'CALL sp_ThemNhanVien(?, ?, ?, ?, ?, ?)',
            [hoTen ?? '', chucVu ?? '', soDienThoai ?? '', tenDangNhap ?? '', matKhau ?? '', quyen ?? 'BANHANG']
        );
        return res.status(201).json({ success: true, message: 'Thêm nhân viên và tài khoản thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const suaNhanVien = async (req, res) => {
    const { id } = req.params;
    const { hoTen, chucVu, soDienThoai, quyen } = req.body;
    try {
        await pool.query(
            'CALL sp_SuaNhanVien(?, ?, ?, ?, ?)',
            [id, hoTen ?? '', chucVu ?? '', soDienThoai ?? '', quyen ?? '']
        );
        return res.status(200).json({ success: true, message: 'Cập nhật nhân viên thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const xoaNhanVien = async (req, res) => {
    const { id } = req.params;
    try {
        await pool.query('CALL sp_XoaNhanVien(?)', [id]);
        return res.status(200).json({ success: true, message: 'Xóa nhân viên thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

module.exports = { getNhanVien, themNhanVien, suaNhanVien, xoaNhanVien };