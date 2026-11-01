const express = require('express');
const router = express.Router();
const { getDoanhThu, getLoiNhuan } = require('../controllers/thongKeController');

router.get('/doanh-thu', getDoanhThu);
router.get('/loi-nhuan', getLoiNhuan);

module.exports = router;