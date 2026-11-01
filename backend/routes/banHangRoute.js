const express = require('express');
const router = express.Router();
const { taoHoaDon } = require('../controllers/banHangController');

router.post('/', taoHoaDon);

module.exports = router;