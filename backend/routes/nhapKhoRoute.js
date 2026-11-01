const express = require('express');
const router = express.Router();
const { taoPhieuNhap } = require('../controllers/nhapKhoController');

router.post('/', taoPhieuNhap);

module.exports = router;