const pool = require('../config/db');

// ==========================================
// 1. QUẢN LÝ THUỐC
// ==========================================

// GET /api/thuoc
const getDanhSachThuoc = async (req, res) => {
    try {
        // Lấy thông tin thuốc kèm tên Hãng SX và tính tổng tồn kho thực tế từ bảng lothuoc (hoặc cột SoLuongTon)
        const sql = `
            SELECT 
                t.MaThuoc, 
                t.TenThuoc, 
                t.DonViTinh, 
                t.GiaNhap, 
                t.GiaBan, 
                COALESCE(SUM(l.SoLuongTon), t.SoLuongTon) AS SoLuongTon,
                t.MaHangSX, 
                h.TenHang, 
                h.QuocGia
            FROM thuoc t
            LEFT JOIN hangsx h ON t.MaHangSX = h.MaHangSX
            LEFT JOIN lothuoc l ON t.MaThuoc = l.MaThuoc AND l.TrangThai = 1
            GROUP BY t.MaThuoc, t.TenThuoc, t.DonViTinh, t.GiaNhap, t.GiaBan, t.SoLuongTon, t.MaHangSX, h.TenHang, h.QuocGia
            ORDER BY t.MaThuoc DESC
        `;
        const [rows] = await pool.query(sql);
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách thuốc thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// POST /api/thuoc
const createThuoc = async (req, res) => {
    try {
        const { TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX } = req.body;
        const sql = `
            INSERT INTO thuoc (TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX)
            VALUES (?, ?, ?, ?, ?, ?)
        `;
        const [result] = await pool.query(sql, [
            TenThuoc, 
            DonViTinh, 
            GiaNhap || 0, 
            GiaBan || 0, 
            SoLuongTon || 0, 
            MaHangSX || null
        ]);
        return res.status(201).json({
            success: true,
            message: 'Thêm thuốc thành công',
            data: { MaThuoc: result.insertId, TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX }
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// PUT /api/thuoc/:id
const updateThuoc = async (req, res) => {
    try {
        const { id } = req.params;
        const { TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX } = req.body;
        const sql = `
            UPDATE thuoc 
            SET TenThuoc = ?, DonViTinh = ?, GiaNhap = ?, GiaBan = ?, SoLuongTon = ?, MaHangSX = ?
            WHERE MaThuoc = ?
        `;
        const [result] = await pool.query(sql, [
            TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX || null, id
        ]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy thuốc', data: null });
        }
        return res.status(200).json({
            success: true,
            message: 'Cập nhật thuốc thành công',
            data: null
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// DELETE /api/thuoc/:id
const deleteThuoc = async (req, res) => {
    try {
        const { id } = req.params;
        const [result] = await pool.query('DELETE FROM thuoc WHERE MaThuoc = ?', [id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy thuốc', data: null });
        }
        return res.status(200).json({
            success: true,
            message: 'Xóa thuốc thành công',
            data: null
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// ==========================================
// 2. QUẢN LÝ LOẠI THUỐC
// ==========================================

// GET /api/loaithuoc
const getDanhSachLoaiThuoc = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM loaithuoc ORDER BY MaLoai DESC');
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách loại thuốc thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// POST /api/loaithuoc
const createLoaiThuoc = async (req, res) => {
    try {
        const { TenLoai, DonViTinh } = req.body;
        const [result] = await pool.query(
            'INSERT INTO loaithuoc (TenLoai, DonViTinh) VALUES (?, ?)',
            [TenLoai, DonViTinh]
        );
        return res.status(201).json({
            success: true,
            message: 'Thêm loại thuốc thành công',
            data: { MaLoai: result.insertId, TenLoai, DonViTinh }
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// PUT /api/loaithuoc/:id
const updateLoaiThuoc = async (req, res) => {
    try {
        const { id } = req.params;
        const { TenLoai, DonViTinh } = req.body;
        const [result] = await pool.query(
            'UPDATE loaithuoc SET TenLoai = ?, DonViTinh = ? WHERE MaLoai = ?',
            [TenLoai, DonViTinh, id]
        );
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy loại thuốc', data: null });
        }
        return res.status(200).json({
            success: true,
            message: 'Cập nhật loại thuốc thành công',
            data: null
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// DELETE /api/loaithuoc/:id
const deleteLoaiThuoc = async (req, res) => {
    try {
        const { id } = req.params;
        const [result] = await pool.query('DELETE FROM loaithuoc WHERE MaLoai = ?', [id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy loại thuốc', data: null });
        }
        return res.status(200).json({
            success: true,
            message: 'Xóa loại thuốc thành công',
            data: null
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// ==========================================
// 3. QUẢN LÝ KHÁCH HÀNG
// ==========================================

// GET /api/khach-hang
const getDanhSachKhachHang = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM khachhang ORDER BY MaKH DESC');
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách khách hàng thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// POST /api/khach-hang
const createKhachHang = async (req, res) => {
    try {
        const { TenKH, SoDienThoai, DiaChi, DiemTichLuy } = req.body;
        const [result] = await pool.query(
            'INSERT INTO khachhang (TenKH, SoDienThoai, DiaChi, DiemTichLuy) VALUES (?, ?, ?, ?)',
            [TenKH, SoDienThoai || null, DiaChi || null, DiemTichLuy || 0]
        );
        return res.status(201).json({
            success: true,
            message: 'Thêm khách hàng thành công',
            data: { MaKH: result.insertId, TenKH, SoDienThoai, DiaChi, DiemTichLuy: DiemTichLuy || 0 }
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// PUT /api/khach-hang/:id
const updateKhachHang = async (req, res) => {
    try {
        const { id } = req.params;
        const { TenKH, SoDienThoai, DiaChi, DiemTichLuy } = req.body;
        const [result] = await pool.query(
            'UPDATE khachhang SET TenKH = ?, SoDienThoai = ?, DiaChi = ?, DiemTichLuy = ? WHERE MaKH = ?',
            [TenKH, SoDienThoai, DiaChi, DiemTichLuy || 0, id]
        );
        if (result.affectedRows === 0) return res.status(404).json({ success: false, message: 'Không tìm thấy khách hàng' });
        return res.status(200).json({ success: true, message: 'Cập nhật thành công' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

// DELETE /api/khach-hang/:id
const deleteKhachHang = async (req, res) => {
    try {
        const { id } = req.params;
        const [result] = await pool.query('DELETE FROM khachhang WHERE MaKH = ?', [id]);
        if (result.affectedRows === 0) return res.status(404).json({ success: false, message: 'Không tìm thấy khách hàng' });
        return res.status(200).json({ success: true, message: 'Xóa thành công' });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
};

module.exports = {
    getDanhSachThuoc, createThuoc, updateThuoc, deleteThuoc,
    getDanhSachLoaiThuoc, createLoaiThuoc, updateLoaiThuoc, deleteLoaiThuoc,
    getDanhSachKhachHang, createKhachHang, updateKhachHang, deleteKhachHang
};