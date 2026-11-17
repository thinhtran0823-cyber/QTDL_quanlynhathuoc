const express = require('express');
const router = express.Router();
const {
    taoPhieuNhap,
    getPhieuNhap,
    getLoThuoc
} = require('../controllers/nhapKhoController');

// Route quản lý phiếu nhập kho
router.post('/phieu-nhap', taoPhieuNhap);
router.get('/phieu-nhap', getPhieuNhap);

// Route tra cứu tình trạng hạn sử dụng lô thuốc
router.get('/lo-thuoc', getLoThuoc);

module.exports = router;
