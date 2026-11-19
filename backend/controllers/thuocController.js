const pool = require('../config/db');

// ==========================================
// 1. QUẢN LÝ THUỐC (Logic kiểm tra quy về SQL)
// ==========================================

// GET /api/thuoc
const getDanhSachThuoc = async (req, res) => {
    try {
        const sql = `
            SELECT 
                t.MaThuoc, 
                t.TenThuoc, 
                t.DonViTinh, 
                t.GiaBan, 
                t.SoLuongTon,
                t.MaHangSX, 
                h.TenHang, 
                h.QuocGia
            FROM thuoc t
            LEFT JOIN hangsx h ON t.MaHangSX = h.MaHangSX
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
        const { TenThuoc, DonViTinh, GiaBan, MaHangSX } = req.body;
        const giaBanParam = (GiaBan === undefined || GiaBan === null) ? '' : String(GiaBan);

        const [rows] = await pool.query(
            'CALL sp_ThemThuoc(?, ?, ?, ?)',
            [TenThuoc ?? '', DonViTinh ?? '', giaBanParam, MaHangSX || null]
        );
        const maThuoc = rows[0]?.[0]?.MaThuoc || null;

        return res.status(201).json({
            success: true,
            message: 'Thêm thuốc thành công',
            data: { MaThuoc: maThuoc, TenThuoc, DonViTinh, GiaBan: Number(GiaBan), SoLuongTon: 0, MaHangSX }
        });
    } catch (error) {
        if (error.sqlState === '45000' || error.errno === 1644) {
            return res.status(400).json({
                success: false,
                message: error.sqlMessage || error.message,
                data: null
            });
        }
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

// PUT /api/thuoc/:id
const updateThuoc = async (req, res) => {
    try {
        const { id } = req.params;
        const { TenThuoc, DonViTinh, GiaBan, MaHangSX } = req.body;
        const giaBanParam = (GiaBan === undefined || GiaBan === null) ? '' : String(GiaBan);

        await pool.query(
            'CALL sp_CapNhatThuoc(?, ?, ?, ?, ?)',
            [id, TenThuoc ?? '', DonViTinh ?? '', giaBanParam, MaHangSX || null]
        );

        return res.status(200).json({
            success: true,
            message: 'Cập nhật thuốc thành công',
            data: null
        });
    } catch (error) {
        if (error.sqlState === '45000' || error.errno === 1644) {
            return res.status(400).json({
                success: false,
                message: error.sqlMessage || error.message,
                data: null
            });
        }
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
// 2. QUẢN LÝ LOẠI THUỐC (Giữ nguyên)
// ==========================================

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
// 3. QUẢN LÝ KHÁCH HÀNG (Giữ nguyên)
// ==========================================

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