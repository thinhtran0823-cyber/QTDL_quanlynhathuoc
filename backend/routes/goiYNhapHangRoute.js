const express = require('express');
const router = express.Router();
const { getGoiYNhapHang } = require('../controllers/goiYNhapHangcontroller');

router.get('/', getGoiYNhapHang);

module.exports = router;