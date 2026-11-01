const pool = require('../config/db');

// TV3 sẽ viết code xử lý 
const taoHoaDon = async (req, res) => {
    return res.status(200).json({
        success: true,
        message: 'API Tạo hóa đơn bán hàng (TV3 đang phát triển)',
        data: null
    });
};

module.exports = { taoHoaDon };