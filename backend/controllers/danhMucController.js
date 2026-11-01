const pool = require('../config/db');

// --- QUẢN LÝ NHÀ CUNG CẤP ---
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
        await pool.query('INSERT INTO NhaCungCap (TenNCC, SoDienThoai) VALUES (?, ?)', [tenNCC, soDienThoai]);
        return res.status(200).json({ success: true, message: 'Thêm nhà cung cấp thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const suaNhaCungCap = async (req, res) => {
    const { id } = req.params;
    const { tenNCC, soDienThoai } = req.body;
    try {
        const [result] = await pool.query('UPDATE NhaCungCap SET TenNCC = ?, SoDienThoai = ? WHERE MaNCC = ?', [tenNCC, soDienThoai, id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy nhà cung cấp để cập nhật!' });
        }
        return res.status(200).json({ success: true, message: 'Cập nhật thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const xoaNhaCungCap = async (req, res) => {
    const { id } = req.params;
    try {
        const [result] = await pool.query('DELETE FROM NhaCungCap WHERE MaNCC = ?', [id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy nhà cung cấp để xóa!' });
        }
        return res.status(200).json({ success: true, message: 'Xóa thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: 'Không thể xóa vì dữ liệu đang được sử dụng ở bảng khác!' });
    }
};

// --- QUẢN LÝ HÃNG SẢN XUẤT ---
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
        await pool.query('INSERT INTO HangSX (TenHang, QuocGia) VALUES (?, ?)', [tenHang, quocGia]);
        return res.status(200).json({ success: true, message: 'Thêm hãng sản xuất thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const suaHangSX = async (req, res) => {
    const { id } = req.params;
    const { tenHang, quocGia } = req.body;
    try {
        const [result] = await pool.query('UPDATE HangSX SET TenHang = ?, QuocGia = ? WHERE MaHangSX = ?', [tenHang, quocGia, id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy hãng sản xuất để cập nhật!' });
        }
        return res.status(200).json({ success: true, message: 'Cập nhật thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

const xoaHangSX = async (req, res) => {
    const { id } = req.params;
    try {
        const [result] = await pool.query('DELETE FROM HangSX WHERE MaHangSX = ?', [id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy hãng sản xuất để xóa!' });
        }
        return res.status(200).json({ success: true, message: 'Xóa thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: 'Không thể xóa vì hãng sản xuất đang có sản phẩm!' });
    }
};

module.exports = {
    getNhaCungCap, themNhaCungCap, suaNhaCungCap, xoaNhaCungCap,
    getHangSX, themHangSX, suaHangSX, xoaHangSX
};