const express = require('express');
const router = express.Router();
const { 
    getDanhSachThuoc, createThuoc, updateThuoc, deleteThuoc,
    getDanhSachLoaiThuoc, createLoaiThuoc, updateLoaiThuoc, deleteLoaiThuoc,
    getDanhSachKhachHang, createKhachHang, updateKhachHang, deleteKhachHang
} = require('../controllers/thuocController');

// 1. Nhóm Thuốc
router.get('/danh-sach-thuoc', getDanhSachThuoc);
router.post('/tao-thuoc', createThuoc);
router.put('/:id', updateThuoc);
router.delete('/:id', deleteThuoc);

// 2. Nhóm Loại Thuốc
router.get('/loai-thuoc', getDanhSachLoaiThuoc);
router.post('/loai-thuoc', createLoaiThuoc);
router.put('/loai-thuoc/:id', updateLoaiThuoc);
router.delete('/loai-thuoc/:id', deleteLoaiThuoc);

// 3. Nhóm Khách Hàng
router.get('/khach-hang', getDanhSachKhachHang);
router.post('/khach-hang', createKhachHang);
router.put('/khach-hang/:id', updateKhachHang);
router.delete('/khach-hang/:id', deleteKhachHang);

module.exports = router;