const express = require('express');
const router = express.Router();
const {
    taoHoaDon,
    layLichSuHoaDon,
    layChiTietHoaDon,
    layDanhSachThuoc,
    layDanhSachKhachHang,
    layNhanVienTheoTaiKhoan
} = require('../controllers/banHangController');

// Mount tại server.js: app.use('/api/ban-hang', banHangRoute)
router.post('/hoa-don', taoHoaDon);            // POST /api/ban-hang/hoa-don
router.get('/hoa-don', layLichSuHoaDon);       // GET  /api/ban-hang/hoa-don
router.get('/hoa-don/:id', layChiTietHoaDon);  // GET  /api/ban-hang/hoa-don/:id

// Dữ liệu cho màn hình POS (thay cho việc frontend kết nối MySQL trực tiếp)
router.get('/thuoc', layDanhSachThuoc);                         // GET /api/ban-hang/thuoc
router.get('/khach-hang', layDanhSachKhachHang);                // GET /api/ban-hang/khach-hang
router.get('/nhan-vien/:tenDangNhap', layNhanVienTheoTaiKhoan); // GET /api/ban-hang/nhan-vien/:tenDangNhap

module.exports = router;