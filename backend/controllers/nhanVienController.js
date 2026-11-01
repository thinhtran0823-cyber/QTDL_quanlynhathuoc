const pool = require('../config/db');

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
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const [result] = await connection.query(
            'INSERT INTO NhanVien (HoTen, ChucVu, SoDienThoai, TrangThai) VALUES (?, ?, ?, 1)',
            [hoTen, chucVu, soDienThoai]
        );
        const newMaNV = result.insertId;

        if (tenDangNhap && matKhau) {
            await connection.query(
                'INSERT INTO TaiKhoan (TenDangNhap, MaNV, MatKhau, Quyen) VALUES (?, ?, ?, ?)',
                [tenDangNhap, newMaNV, matKhau, quyen || 'BANHANG']
            );
        }

        await connection.commit();
        return res.status(200).json({ success: true, message: 'Thêm nhân viên và tài khoản thành công!' });
    } catch (error) {
        await connection.rollback();
        return res.status(500).json({ success: false, message: error.message });
    } finally {
        connection.release();
    }
};

const suaNhanVien = async (req, res) => {
    const { id } = req.params;
    const { hoTen, chucVu, soDienThoai, quyen } = req.body;
    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        const [resNV] = await connection.query(
            'UPDATE NhanVien SET HoTen = ?, ChucVu = ?, SoDienThoai = ? WHERE MaNV = ?',
            [hoTen, chucVu, soDienThoai, id]
        );

        if (resNV.affectedRows === 0) {
            await connection.rollback();
            return res.status(404).json({ success: false, message: 'Không tìm thấy nhân viên để cập nhật!' });
        }

        if (quyen) {
            await connection.query('UPDATE TaiKhoan SET Quyen = ? WHERE MaNV = ?', [quyen, id]);
        }

        await connection.commit();
        return res.status(200).json({ success: true, message: 'Cập nhật nhân viên thành công!' });
    } catch (error) {
        await connection.rollback();
        return res.status(500).json({ success: false, message: error.message });
    } finally {
        connection.release();
    }
};

const xoaNhanVien = async (req, res) => {
    const { id } = req.params;
    try {
        await pool.query('DELETE FROM TaiKhoan WHERE MaNV = ?', [id]);
        const [result] = await pool.query('DELETE FROM NhanVien WHERE MaNV = ?', [id]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy nhân viên để xóa!' });
        }
        return res.status(200).json({ success: true, message: 'Xóa nhân viên thành công!' });
    } catch (error) {
        return res.status(500).json({ success: false, message: 'Không thể xóa nhân viên do đã có lịch sử giao dịch bán/nhập hàng!' });
    }
};

module.exports = { getNhanVien, themNhanVien, suaNhanVien, xoaNhanVien };