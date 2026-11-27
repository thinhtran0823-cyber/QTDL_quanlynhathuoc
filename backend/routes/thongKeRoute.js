const express = require('express');
const router = express.Router();
const { getDoanhThu, getLoiNhuan, getChiTietXuatExcel } = require('../controllers/thongKeController');

router.get('/doanh-thu', getDoanhThu);
router.get('/loi-nhuan', getLoiNhuan);
router.get('/xuat-excel', getChiTietXuatExcel);
module.exports = router;