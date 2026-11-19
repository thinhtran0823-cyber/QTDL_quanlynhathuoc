const pool = require('../config/db');

// Hàm tiện ích xử lý lỗi từ MySQL Trigger / Stored Procedure
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
            message: 'Không thể xóa vì dữ liệu đang được sử dụng ở bảng khác!'
        });
    }
    return res.status(500).json({
        success: false,
        message: error.message
    });
};

// ==========================================
// 1. QUẢN LÝ NHÀ CUNG CẤP
// ==========================================
const getNhaCungCap = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM NhaCungCap ORDER BY MaNCC DESC');
        return res.status(200).json({ success: true, data: rows });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const themNhaCungCap = async (req, res) => {
    const { tenNCC, soDienThoai } = req.body;
    try {
        await pool.query('CALL sp_ThemNhaCungCap(?, ?)', [tenNCC ?? '', soDienThoai ?? '']);
        return res.status(201).json({ success: true, message: 'Thêm nhà cung cấp thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const suaNhaCungCap = async (req, res) => {
    const { id } = req.params;
    const { tenNCC, soDienThoai } = req.body;
    try {
        await pool.query('CALL sp_SuaNhaCungCap(?, ?, ?)', [id, tenNCC ?? '', soDienThoai ?? '']);
        return res.status(200).json({ success: true, message: 'Cập nhật nhà cung cấp thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const xoaNhaCungCap = async (req, res) => {
    const { id } = req.params;
    try {
        await pool.query('CALL sp_XoaNhaCungCap(?)', [id]);
        return res.status(200).json({ success: true, message: 'Xóa nhà cung cấp thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

// ==========================================
// 2. QUẢN LÝ HÃNG SẢN XUẤT
// ==========================================
const getHangSX = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM HangSX ORDER BY MaHangSX DESC');
        return res.status(200).json({ success: true, data: rows });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const themHangSX = async (req, res) => {
    const { tenHang, quocGia } = req.body;
    try {
        await pool.query('CALL sp_ThemHangSX(?, ?)', [tenHang ?? '', quocGia ?? '']);
        return res.status(201).json({ success: true, message: 'Thêm hãng sản xuất thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const suaHangSX = async (req, res) => {
    const { id } = req.params;
    const { tenHang, quocGia } = req.body;
    try {
        await pool.query('CALL sp_SuaHangSX(?, ?, ?)', [id, tenHang ?? '', quocGia ?? '']);
        return res.status(200).json({ success: true, message: 'Cập nhật hãng sản xuất thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

const xoaHangSX = async (req, res) => {
    const { id } = req.params;
    try {
        await pool.query('CALL sp_XoaHangSX(?)', [id]);
        return res.status(200).json({ success: true, message: 'Xóa hãng sản xuất thành công!' });
    } catch (error) {
        return handleSqlError(res, error);
    }
};

module.exports = {
    getNhaCungCap, themNhaCungCap, suaNhaCungCap, xoaNhaCungCap,
    getHangSX, themHangSX, suaHangSX, xoaHangSX
};