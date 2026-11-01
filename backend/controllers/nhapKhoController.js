const pool = require('../config/db');

// TV2 sẽ viết code xử lý 
const taoPhieuNhap = async (req, res) => {
    return res.status(200).json({
        success: true,
        message: 'API Tạo phiếu nhập kho',
        data: null
    });
};

module.exports = { taoPhieuNhap };