const express = require('express');
const router = express.Router();
const { getNhanVien, themNhanVien, suaNhanVien, xoaNhanVien } = require('../controllers/nhanVienController');

router.get('/', getNhanVien);
router.post('/', themNhanVien);
router.put('/:id', suaNhanVien);
router.delete('/:id', xoaNhanVien);

module.exports = router;