require('dotenv').config();
const express = require('express');
const cors = require('cors');
const pool = require('./config/db');

// Import các Route===========================================================

const authRoute = require('./routes/authRoute');
const thongKeRoute = require('./routes/thongKeRoute');
const nhapKhoRoute = require('./routes/nhapKhoRoute'); // TV2
const banHangRoute = require('./routes/banHangRoute'); // TV3
const thuocRoute = require('./routes/thuocRoute');     // TV4
const danhMucRoute = require('./routes/danhMucRoute');
const nhanVienRoute = require('./routes/nhanVienRoute');
//============================================================================

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware đọc dữ liệu JSON gửi lên từ Client và cho phép CORS
app.use(cors());
app.use(express.json());


// Khai báo Endpoint API chính================================================

app.use('/api/auth', authRoute);
app.use('/api/thong-ke', thongKeRoute);
app.use('/api/nhap-kho', nhapKhoRoute); //TV2
app.use('/api/ban-hang', banHangRoute); //TV3
app.use('/api/thuoc', thuocRoute); //TV4
app.use('/api/danh-muc', danhMucRoute);
app.use('/api/nhan-vien', nhanVienRoute);

//============================================================================

// API Test kết nối Database (Health Check)
app.get('/api/ping', async (req, res) => {
    try {
        // Chạy câu lệnh SQL đơn giản kiểm tra số lượng nhân viên
        const [rows] = await pool.query('SELECT COUNT(*) AS TongSoNhanVien FROM NhanVien');
        return res.status(200).json({
            success: true,
            message: 'Kết nối MySQL Workbench thành công!',
            data: {
                soNhanVien: rows[0].TongSoNhanVien
            }
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: 'Lỗi kết nối MySQL: ' + error.message,
            data: null
        });
    }
});

// Khởi chạy Web Server
app.listen(PORT, () => {
    console.log(`>>> Server đang chạy tại: http://localhost:${PORT}`);
});