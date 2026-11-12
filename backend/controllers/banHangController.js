const pool = require('../config/db');

// Tạo lỗi có mã HTTP để phân biệt lỗi nghiệp vụ và lỗi hệ thống
function taoLoi(message, status = 400) {
    const error = new Error(message);
    error.status = status;
    return error;
}

// Kiểm tra số nguyên dương
function laSoNguyenDuong(value) {
    return Number.isSafeInteger(value) && value > 0;
}

// POST /api/ban-hang/hoa-don
const taoHoaDon = async (req, res) => {
    let connection;
    let daBatDauTransaction = false;

    try {
        const { MaNV, MaKH, chiTiet } = req.body;

        // 1. KIỂM TRA DỮ LIỆU ĐẦU VÀO
       
        const maNV = Number(MaNV);

        if (!laSoNguyenDuong(maNV)) {
            throw taoLoi('Mã nhân viên không hợp lệ');
        }

        let maKH = null;

        if (MaKH !== undefined && MaKH !== null && MaKH !== '') {
            maKH = Number(MaKH);

            if (!laSoNguyenDuong(maKH)) {
                throw taoLoi('Mã khách hàng không hợp lệ');
            }
        }

        if (!Array.isArray(chiTiet) || chiTiet.length === 0) {
            throw taoLoi('Hóa đơn phải có ít nhất một loại thuốc');
        }

        // Gộp các thuốc trùng mã
        const danhSachThuoc = new Map();

        for (const item of chiTiet) {
            const maThuoc = Number(item.MaThuoc);
            const soLuong = Number(item.SoLuongBan);

            if (!laSoNguyenDuong(maThuoc)) {
                throw taoLoi('Mã thuốc không hợp lệ');
            }

            if (!laSoNguyenDuong(soLuong)) {
                throw taoLoi('Số lượng bán phải là số nguyên dương');
            }

            const soLuongCu = danhSachThuoc.get(maThuoc) || 0;
            const tongSoLuong = soLuongCu + soLuong;

            if (!Number.isSafeInteger(tongSoLuong)) {
                throw taoLoi('Tổng số lượng thuốc không hợp lệ');
            }

            danhSachThuoc.set(maThuoc, tongSoLuong);
        }

        // Sắp xếp mã thuốc để giảm nguy cơ deadlock khi nhiều người bán cùng lúc
        const danhSachDaGop = [...danhSachThuoc.entries()]
            .sort((a, b) => a[0] - b[0])
            .map(([MaThuoc, SoLuongBan]) => ({
                MaThuoc,
                SoLuongBan
            }));

        connection = await pool.getConnection();

        // 2. BẮT ĐẦU TRANSACTION
     
        await connection.beginTransaction();
        daBatDauTransaction = true;

        // 3. KIỂM TRA NHÂN VIÊN=

        const [nhanVien] = await connection.execute(
            `SELECT MaNV
             FROM nhanvien
             WHERE MaNV = ? AND TrangThai = 1
             FOR UPDATE`,
            [maNV]
        );

        if (nhanVien.length === 0) {
            throw taoLoi('Nhân viên không tồn tại hoặc đã ngừng hoạt động');
        }


        // 4. KIỂM TRA KHÁCH HÀNG
  

        if (maKH !== null) {
            const [khachHang] = await connection.execute(
                `SELECT MaKH
                 FROM khachhang
                 WHERE MaKH = ?
                 FOR UPDATE`,
                [maKH]
            );

            if (khachHang.length === 0) {
                throw taoLoi('Khách hàng không tồn tại');
            }
        }
        // 5. TẠO HÓA ĐƠN
        const [ketQuaHoaDon] = await connection.execute(
            `INSERT INTO hoadon (MaNV, MaKH, TongTien)
             VALUES (?, ?, 0)`,
            [maNV, maKH]
        );

        const maHD = ketQuaHoaDon.insertId;

        let tongTien = 0;
        const chiTietHoaDon = [];


        // 6. XỬ LÝ TỪNG LOẠI THUỐC

        for (const item of danhSachDaGop) {
            const { MaThuoc, SoLuongBan } = item;
            // 6.1 Khóa dòng thuốc trong bảng thuoc

            const [thuocRows] = await connection.execute(
                `SELECT MaThuoc, TenThuoc, GiaBan, SoLuongTon
                 FROM thuoc
                 WHERE MaThuoc = ?
                 FOR UPDATE`,
                [MaThuoc]
            );

            if (thuocRows.length === 0) {
                throw taoLoi(`Thuốc có mã ${MaThuoc} không tồn tại`);
            }

            const thuoc = thuocRows[0];

            const giaBan = Number(thuoc.GiaBan);
            const tonKhoThuoc = Number(thuoc.SoLuongTon);

            if (!Number.isFinite(giaBan) || giaBan < 0) {
                throw taoLoi(`Giá bán của thuốc ${thuoc.TenThuoc} không hợp lệ`, 500);
            }

            if (tonKhoThuoc < SoLuongBan) {
                throw taoLoi(
                    `Thuốc ${thuoc.TenThuoc} không đủ tồn kho. Hiện còn ${tonKhoThuoc}`
                );
            }

            // 6.2 Lấy các lô theo FEFO
            // Lô hết hạn hoặc đã hết hàng sẽ không được xuất.
            // FOR UPDATE giúp khóa các lô trong quá trình bán.

            const [danhSachLo] = await connection.execute(
                `SELECT MaLo, SoLo, HanSuDung, SoLuongTon
                 FROM lothuoc
                 WHERE MaThuoc = ?
                   AND HanSuDung >= CURDATE()
                   AND SoLuongTon > 0
                 ORDER BY HanSuDung ASC, MaLo ASC
                 FOR UPDATE`,
                [MaThuoc]
            );

            const tongTonTheoLo = danhSachLo.reduce(
                (tong, lo) => tong + Number(lo.SoLuongTon),
                0
            );

            if (tongTonTheoLo < SoLuongBan) {
                throw taoLoi(
                    `Thuốc ${thuoc.TenThuoc} không đủ hàng còn hạn sử dụng. Hiện còn ${tongTonTheoLo}`
                );
            }
            // 6.3 Phân bổ số lượng theo FEFO

            let soLuongCanBan = SoLuongBan;

            for (const lo of danhSachLo) {
                if (soLuongCanBan <= 0) {
                    break;
                }

                const tonKhoLo = Number(lo.SoLuongTon);

                // Số lượng lấy từ lô hiện tại
                const soLuongXuat = Math.min(
                    soLuongCanBan,
                    tonKhoLo
                );

                const thanhTien = soLuongXuat * giaBan;

                if (!Number.isSafeInteger(thanhTien)) {
                    throw taoLoi('Thành tiền vượt giới hạn cho phép');
                }
                // 6.4 Thêm chi tiết hóa đơn
                // --------------------------------
                // ThanhTien là GENERATED COLUMN nên không INSERT.

                await connection.execute(
                    `INSERT INTO chitiethoadon
                        (MaHD, MaLo, SoLuongBan, GiaBan)
                     VALUES (?, ?, ?, ?)`,
                    [
                        maHD,
                        lo.MaLo,
                        soLuongXuat,
                        giaBan
                    ]
                );
                // 6.5 Cập nhật tồn kho lô thuốc

                const [ketQuaCapNhatLo] = await connection.execute(
                    `UPDATE lothuoc
                     SET SoLuongTon = SoLuongTon - ?
                     WHERE MaLo = ?
                       AND SoLuongTon >= ?`,
                    [
                        soLuongXuat,
                        lo.MaLo,
                        soLuongXuat
                    ]
                );

                if (ketQuaCapNhatLo.affectedRows !== 1) {
                    throw taoLoi(
                        `Không thể cập nhật tồn kho lô ${lo.SoLo}`,
                        500
                    );
                }

                // Lưu chi tiết để trả về frontend
                chiTietHoaDon.push({
                    MaThuoc: thuoc.MaThuoc,
                    TenThuoc: thuoc.TenThuoc,
                    MaLo: lo.MaLo,
                    SoLo: lo.SoLo,
                    HanSuDung: lo.HanSuDung,
                    SoLuongBan: soLuongXuat,
                    GiaBan: giaBan,
                    ThanhTien: thanhTien
                });

                tongTien += thanhTien;
                soLuongCanBan -= soLuongXuat;
            }

            // 6.6 Cập nhật tổng tồn kho thuốc

            const [ketQuaCapNhatThuoc] = await connection.execute(
                `UPDATE thuoc
                 SET SoLuongTon = SoLuongTon - ?
                 WHERE MaThuoc = ?
                   AND SoLuongTon >= ?`,
                [
                    SoLuongBan,
                    MaThuoc,
                    SoLuongBan
                ]
            );

            if (ketQuaCapNhatThuoc.affectedRows !== 1) {
                throw taoLoi(
                    `Không thể cập nhật tồn kho thuốc ${thuoc.TenThuoc}`,
                    500
                );
            }
        }

        // ==============================
        // 7. CẬP NHẬT TỔNG TIỀN HÓA ĐƠN
        // ==============================

        await connection.execute(
            `UPDATE hoadon
             SET TongTien = ?
             WHERE MaHD = ?`,
            [
                tongTien,
                maHD
            ]
        );

        // Lấy ngày lập (do MySQL gán) để frontend in hóa đơn, khỏi truy cập DB trực tiếp
        const [ngayLapRows] = await connection.execute(
            `SELECT DATE_FORMAT(NgayLap, '%Y-%m-%d %H:%i:%s') AS NgayLap
             FROM hoadon
             WHERE MaHD = ?`,
            [maHD]
        );
        const ngayLap = ngayLapRows[0].NgayLap;

        // ==============================
        // 8. COMMIT TRANSACTION
        // ==============================

        await connection.commit();
        daBatDauTransaction = false;

        // ==============================
        // 9. TRẢ KẾT QUẢ
        // ==============================

        return res.status(201).json({
            success: true,
            message: 'Tạo hóa đơn thành công',
            data: {
                MaHD: maHD,
                MaNV: maNV,
                MaKH: maKH,
                NgayLap: ngayLap,
                TongTien: tongTien,
                ChiTiet: chiTietHoaDon
            }
        });

    } catch (error) {
        // Nếu có lỗi thì hoàn tác toàn bộ thao tác
        if (connection && daBatDauTransaction) {
            await connection.rollback();
        }

        console.error('Lỗi tạo hóa đơn:', error);

        return res.status(error.status || 500).json({
            success: false,
            message: error.status
                ? error.message
                : 'Lỗi hệ thống khi tạo hóa đơn'
        });

    } finally {
        if (connection) {
            connection.release();
        }
    }
};

// GET /api/ban-hang/hoa-don  (?limit=200)
// Lấy lịch sử hóa đơn bán hàng, mới nhất trước
const layLichSuHoaDon = async (req, res) => {
    try {
        let limit = Number(req.query.limit);
        if (!Number.isInteger(limit) || limit <= 0 || limit > 1000) {
            limit = 200;
        }

        const [rows] = await pool.query(
            `SELECT h.MaHD,
                    DATE_FORMAT(h.NgayLap, '%Y-%m-%d %H:%i:%s') AS NgayLap,
                    n.HoTen AS TenNV,
                    COALESCE(k.TenKH, 'Khách vãng lai') AS TenKH,
                    h.TongTien
             FROM hoadon h
             JOIN nhanvien n ON n.MaNV = h.MaNV
             LEFT JOIN khachhang k ON k.MaKH = h.MaKH
             ORDER BY h.NgayLap DESC, h.MaHD DESC
             LIMIT ?`,
            [limit]
        );

        // Thứ tự khóa cố định (frontend Java đọc theo thứ tự này)
        const data = rows.map((r) => ({
            MaHD: r.MaHD,
            NgayLap: r.NgayLap,
            TenNV: r.TenNV,
            TenKH: r.TenKH,
            TongTien: Number(r.TongTien)
        }));

        return res.status(200).json({ success: true, data });
    } catch (error) {
        console.error('Lỗi lấy lịch sử hóa đơn:', error);
        return res.status(500).json({
            success: false,
            message: 'Lỗi hệ thống khi lấy lịch sử hóa đơn'
        });
    }
};

// GET /api/ban-hang/hoa-don/:id
// Xem chi tiết một hóa đơn (mỗi lô xuất kho là một dòng)
const layChiTietHoaDon = async (req, res) => {
    try {
        const maHD = Number(req.params.id);
        if (!laSoNguyenDuong(maHD)) {
            return res.status(400).json({ success: false, message: 'Mã hóa đơn không hợp lệ' });
        }

        const [hoaDon] = await pool.query(
            `SELECT h.MaHD,
                    DATE_FORMAT(h.NgayLap, '%Y-%m-%d %H:%i:%s') AS NgayLap,
                    n.HoTen AS TenNV,
                    COALESCE(k.TenKH, 'Khách vãng lai') AS TenKH,
                    h.TongTien
             FROM hoadon h
             JOIN nhanvien n ON n.MaNV = h.MaNV
             LEFT JOIN khachhang k ON k.MaKH = h.MaKH
             WHERE h.MaHD = ?`,
            [maHD]
        );

        if (hoaDon.length === 0) {
            return res.status(404).json({ success: false, message: 'Không tìm thấy hóa đơn' });
        }

        const [chiTiet] = await pool.query(
            `SELECT t.TenThuoc, l.MaLo, l.SoLo,
                    DATE_FORMAT(l.HanSuDung, '%Y-%m-%d') AS HanSuDung,
                    c.SoLuongBan, c.GiaBan, c.ThanhTien
             FROM chitiethoadon c
             JOIN lothuoc l ON l.MaLo = c.MaLo
             JOIN thuoc t ON t.MaThuoc = l.MaThuoc
             WHERE c.MaHD = ?
             ORDER BY c.MaCTHD`,
            [maHD]
        );

        const h = hoaDon[0];
        return res.status(200).json({
            success: true,
            data: {
                MaHD: h.MaHD,
                NgayLap: h.NgayLap,
                TenNV: h.TenNV,
                TenKH: h.TenKH,
                TongTien: Number(h.TongTien),
                ChiTiet: chiTiet.map((c) => ({
                    TenThuoc: c.TenThuoc,
                    MaLo: c.MaLo,
                    SoLo: c.SoLo,
                    HanSuDung: c.HanSuDung,
                    SoLuongBan: Number(c.SoLuongBan),
                    GiaBan: Number(c.GiaBan),
                    ThanhTien: Number(c.ThanhTien)
                }))
            }
        });
    } catch (error) {
        console.error('Lỗi lấy chi tiết hóa đơn:', error);
        return res.status(500).json({
            success: false,
            message: 'Lỗi hệ thống khi lấy chi tiết hóa đơn'
        });
    }
};

// GET /api/ban-hang/thuoc
// Danh sách thuốc cho màn hình POS, kèm tồn kho tính theo các lô CÒN HẠN
// (cùng điều kiện với FEFO khi tạo hóa đơn)
const layDanhSachThuoc = async (req, res) => {
    try {
        const [rows] = await pool.query(
            `SELECT t.MaThuoc, t.TenThuoc,
                    COALESCE(t.DonViTinh, '') AS DonViTinh,
                    t.GiaBan,
                    COALESCE((SELECT SUM(l.SoLuongTon)
                              FROM lothuoc l
                              WHERE l.MaThuoc = t.MaThuoc
                                AND l.HanSuDung >= CURDATE()
                                AND l.SoLuongTon > 0), 0) AS TonConHan
             FROM thuoc t
             ORDER BY t.TenThuoc`
        );

        // Thứ tự khóa cố định (frontend Java đọc theo thứ tự này)
        const data = rows.map((r) => ({
            MaThuoc: r.MaThuoc,
            TenThuoc: r.TenThuoc,
            DonViTinh: r.DonViTinh,
            GiaBan: Number(r.GiaBan),
            TonConHan: Number(r.TonConHan)
        }));

        return res.status(200).json({ success: true, data });
    } catch (error) {
        console.error('Lỗi lấy danh sách thuốc:', error);
        return res.status(500).json({ success: false, message: 'Lỗi hệ thống khi lấy danh sách thuốc' });
    }
};

// GET /api/ban-hang/khach-hang
const layDanhSachKhachHang = async (req, res) => {
    try {
        const [rows] = await pool.query(
            `SELECT MaKH, TenKH, COALESCE(SoDienThoai, '') AS SoDienThoai
             FROM khachhang
             ORDER BY TenKH`
        );

        const data = rows.map((r) => ({
            MaKH: r.MaKH,
            TenKH: r.TenKH,
            SoDienThoai: r.SoDienThoai
        }));

        return res.status(200).json({ success: true, data });
    } catch (error) {
        console.error('Lỗi lấy danh sách khách hàng:', error);
        return res.status(500).json({ success: false, message: 'Lỗi hệ thống khi lấy danh sách khách hàng' });
    }
};

// GET /api/ban-hang/nhan-vien/:tenDangNhap
// Đổi tên đăng nhập -> MaNV + HoTen (không trả mật khẩu / quyền)
const layNhanVienTheoTaiKhoan = async (req, res) => {
    try {
        const tenDangNhap = String(req.params.tenDangNhap || '').trim();
        if (!tenDangNhap) {
            return res.status(400).json({ success: false, message: 'Thiếu tên đăng nhập' });
        }

        const [rows] = await pool.query(
            `SELECT n.MaNV, n.HoTen
             FROM taikhoan t
             JOIN nhanvien n ON n.MaNV = t.MaNV
             WHERE t.TenDangNhap = ? AND n.TrangThai = 1`,
            [tenDangNhap]
        );

        if (rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Không tìm thấy nhân viên cho tài khoản này'
            });
        }

        return res.status(200).json({
            success: true,
            data: { MaNV: rows[0].MaNV, HoTen: rows[0].HoTen }
        });
    } catch (error) {
        console.error('Lỗi lấy nhân viên:', error);
        return res.status(500).json({ success: false, message: 'Lỗi hệ thống khi lấy nhân viên' });
    }
};

module.exports = {
    taoHoaDon,
    layLichSuHoaDon,
    layChiTietHoaDon,
    layDanhSachThuoc,
    layDanhSachKhachHang,
    layNhanVienTheoTaiKhoan
};