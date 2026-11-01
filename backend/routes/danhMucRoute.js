const express = require('express');
const router = express.Router();
const {
    getNhaCungCap, themNhaCungCap, suaNhaCungCap, xoaNhaCungCap,
    getHangSX, themHangSX, suaHangSX, xoaHangSX
} = require('../controllers/danhMucController');

// Routes Nhà Cung Cấp
router.get('/nha-cung-cap', getNhaCungCap);
router.post('/nha-cung-cap', themNhaCungCap);
router.put('/nha-cung-cap/:id', suaNhaCungCap);
router.delete('/nha-cung-cap/:id', xoaNhaCungCap);

// Routes Hãng Sản Xuất
router.get('/hang-sx', getHangSX);
router.post('/hang-sx', themHangSX);
router.put('/hang-sx/:id', suaHangSX);
router.delete('/hang-sx/:id', xoaHangSX);

module.exports = router;