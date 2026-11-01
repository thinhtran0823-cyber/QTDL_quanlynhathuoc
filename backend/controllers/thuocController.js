const pool = require('../config/db');

// TV4 sẽ viết code 
const getDanhSachThuoc = async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM Thuoc');
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách thuốc thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message, data: null });
    }
};

module.exports = { getDanhSachThuoc };