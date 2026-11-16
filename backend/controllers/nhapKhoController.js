const pool = require('../config/db');

/**
 * Controller xử lý nghiệp vụ Nhập kho (Module 2)
 * TV2: Phan Anh Ngữ
 */

/**
 * POST /api/nhap-kho/phieu-nhap
 * Tạo phiếu nhập kho mới trong 1 transaction duy nhất:
 * 1. INSERT PhieuNhap(MaNCC, MaNV)
 * 2. Lặp chi tiết: INSERT LoThuoc -> INSERT ChiTietPhieuNhap
 * Lưu ý: Tăng tồn kho (Thuoc) và tính TongTien (PhieuNhap) do Trigger đảm nhiệm.
 */
const taoPhieuNhap = async (req, res) => {
    const maNCC = req.body.maNCC || req.body.MaNCC;
    const maNV = req.body.maNV || req.body.MaNV;
    const chiTiet = req.body.chiTiet || req.body.ChiTiet;

    // Kiểm tra thông tin bắt buộc
    if (!maNCC || !maNV) {
        return res.status(400).json({
            success: false,
            message: 'Nhà cung cấp và nhân viên lập phiếu không được để trống',
            data: null
        });
    }

    if (!Array.isArray(chiTiet) || chiTiet.length === 0) {
        return res.status(400).json({
            success: false,
            message: 'Danh sách chi tiết phiếu nhập không được để trống',
            data: null
        });
    }

    const connection = await pool.getConnection();
    try {
        await connection.beginTransaction();

        // 1. Tạo phiếu nhập mới (trigger trg_PN_BI_KiemTra kiểm tra nhân viên và gán NgayNhap)
        const [resPN] = await connection.query(
            'INSERT INTO PhieuNhap (MaNCC, MaNV) VALUES (?, ?)',
            [maNCC, maNV]
        );
        const maPN = resPN.insertId;

        // 2. Thêm từng lô thuốc và chi tiết phiếu nhập tương ứng
        for (const item of chiTiet) {
            const maThuoc = item.maThuoc !== undefined ? item.maThuoc : item.MaThuoc;
            const soLo = item.soLo !== undefined ? item.soLo : item.SoLo;
            const ngaySanXuat = item.ngaySanXuat !== undefined ? item.ngaySanXuat : item.NgaySanXuat;
            const hanSuDung = item.hanSuDung !== undefined ? item.hanSuDung : item.HanSuDung;
            const soLuong = item.soLuong !== undefined ? item.soLuong : item.SoLuong;
            const giaNhap = item.giaNhap !== undefined ? item.giaNhap : item.GiaNhap;

            // Chuẩn hóa ngày sản xuất: nếu rỗng thì lưu NULL
            const nsx = (ngaySanXuat && String(ngaySanXuat).trim() !== '') ? String(ngaySanXuat).trim() : null;

            // INSERT LoThuoc: trigger trg_LT_BI_KiemTra kiểm tra dữ liệu, trg_LT_AI_CongTonThuoc tự cộng tồn Thuoc
            const [resLT] = await connection.query(
                'INSERT INTO LoThuoc (MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap) VALUES (?, ?, ?, ?, ?, ?)',
                [maThuoc, soLo, nsx, hanSuDung, soLuong, giaNhap]
            );
            const maLo = resLT.insertId;

            // INSERT ChiTietPhieuNhap: ThanhTien là GENERATED column; trigger trg_CTPN_AI_TongTien tự cập nhật TongTien PhieuNhap
            await connection.query(
                'INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap) VALUES (?, ?, ?, ?)',
                [maPN, maLo, soLuong, giaNhap]
            );
        }

        // Lấy lại thông tin phiếu nhập vừa tạo (đã có TongTien do trigger cập nhật)
        const [rowsPN] = await connection.query(
            'SELECT MaPN, MaNCC, MaNV, NgayNhap, TongTien FROM PhieuNhap WHERE MaPN = ?',
            [maPN]
        );

        await connection.commit();

        return res.status(200).json({
            success: true,
            message: 'Tạo phiếu nhập thành công!',
            data: rowsPN[0] || { maPN }
        });
    } catch (error) {
        await connection.rollback();

        // Lỗi do Trigger ném qua SIGNAL SQLSTATE '45000' (errno 1644)
        if (error.sqlState === '45000' || error.errno === 1644) {
            return res.status(400).json({
                success: false,
                message: error.sqlMessage || error.message,
                data: null
            });
        }

        return res.status(500).json({
            success: false,
            message: error.message,
            data: null
        });
    } finally {
        connection.release();
    }
};

/**
 * GET /api/nhap-kho/phieu-nhap
 * Lấy danh sách phiếu nhập định dạng phẳng (phục vụ màn hình Swing không có thư viện JSON)
 * Sắp xếp MaPN DESC
 */
const getPhieuNhap = async (req, res) => {
    try {
        const querySQL = `
            SELECT 
                pn.MaPN,
                pn.NgayNhap,
                ncc.TenNCC,
                nv.HoTen,
                pn.TongTien,
                t.TenThuoc,
                lt.SoLo,
                lt.HanSuDung,
                ctpn.SoLuongNhap,
                ctpn.DonGiaNhap,
                ctpn.ThanhTien
            FROM PhieuNhap pn
            JOIN NhaCungCap ncc ON pn.MaNCC = ncc.MaNCC
            JOIN NhanVien nv ON pn.MaNV = nv.MaNV
            JOIN ChiTietPhieuNhap ctpn ON pn.MaPN = ctpn.MaPN
            JOIN LoThuoc lt ON ctpn.MaLo = lt.MaLo
            JOIN Thuoc t ON lt.MaThuoc = t.MaThuoc
            ORDER BY pn.MaPN DESC, ctpn.MaCTPN ASC
        `;
        const [rows] = await pool.query(querySQL);
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách phiếu nhập thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message,
            data: null
        });
    }
};

/**
 * GET /api/nhap-kho/lo-thuoc
 * Truy vấn danh sách lô thuốc từ view v_LoThuoc_TinhTrang
 * Sắp xếp HanSuDung ASC; Tùy chọn lọc ?phanLoai=CON_HAN|SAP_HET_HAN|HET_HAN
 */
const getLoThuoc = async (req, res) => {
    try {
        const { phanLoai } = req.query;
        let querySQL = 'SELECT * FROM v_LoThuoc_TinhTrang';
        const params = [];

        if (phanLoai) {
            querySQL += ' WHERE PhanLoai = ?';
            params.push(phanLoai.trim().toUpperCase());
        }

        querySQL += ' ORDER BY HanSuDung ASC';

        const [rows] = await pool.query(querySQL, params);
        return res.status(200).json({
            success: true,
            message: 'Lấy danh sách lô thuốc thành công',
            data: rows
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message,
            data: null
        });
    }
};

module.exports = {
    taoPhieuNhap,
    getPhieuNhap,
    getLoThuoc
};
