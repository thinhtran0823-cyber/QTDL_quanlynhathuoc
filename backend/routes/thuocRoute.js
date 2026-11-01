const express = require('express');
const router = express.Router();
const { getDanhSachThuoc } = require('../controllers/thuocController');

router.get('/', getDanhSachThuoc);

module.exports = router;