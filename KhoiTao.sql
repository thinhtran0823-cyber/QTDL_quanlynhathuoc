CREATE DATABASE IF NOT EXISTS QuanLyNhaThuoc 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE QuanLyNhaThuoc;

-- 1. BẢNG NHÂN VIÊN & TÀI KHOẢN (Do Nhóm trưởng quản lý)
CREATE TABLE NhanVien (
    MaNV INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    ChucVu VARCHAR(50) NOT NULL, -- 'QuanLy', 'ThuKho', 'DuocSi'
    SoDienThoai VARCHAR(15),
    TrangThai BIT DEFAULT 1
);

CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50) PRIMARY KEY,
    MaNV INT UNIQUE NOT NULL,
    MatKhau VARCHAR(255) NOT NULL, -- Lưu hash password
    Quyen VARCHAR(20) NOT NULL,    -- 'ADMIN', 'THUKHO', 'BANHANG'
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV) ON DELETE CASCADE
);

-- 2. CÁC BẢNG DANH MỤC CỐT LÕI
CREATE TABLE HangSX (
    MaHangSX INT AUTO_INCREMENT PRIMARY KEY,
    TenHang VARCHAR(100) NOT NULL,
    QuocGia VARCHAR(50)
);

CREATE TABLE LoaiThuoc (
    MaLoai INT AUTO_INCREMENT PRIMARY KEY,
    TenLoai VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(20) NOT NULL
);

CREATE TABLE NhaCungCap (
    MaNCC INT AUTO_INCREMENT PRIMARY KEY,
    TenNCC VARCHAR(150) NOT NULL,
    SoDienThoai VARCHAR(15)
);

CREATE TABLE KhachHang (
    MaKH INT AUTO_INCREMENT PRIMARY KEY,
    TenKH VARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15),
    DiaChi VARCHAR(255),
    DiemTichLuy INT DEFAULT 0
);

-- 3. BẢNG THUỐC (Kho dữ liệu sản phẩm)
CREATE TABLE Thuoc (
    MaThuoc INT AUTO_INCREMENT PRIMARY KEY,
    TenThuoc VARCHAR(255) NOT NULL,
    DonViTinh VARCHAR(50) NOT NULL, -- Hộp, Vỉ, Viên, Chai
    GiaNhap DECIMAL(15, 2) NOT NULL DEFAULT 0, -- Giá nhập tham chiếu
    GiaBan DECIMAL(15, 2) NOT NULL DEFAULT 0,  -- Giá bán đề xuất
    SoLuongTon INT NOT NULL DEFAULT 0,        -- Tổng tồn kho (tích lũy từ tất cả các Lô)
    MaHangSX INT,
    FOREIGN KEY (MaHangSX) REFERENCES HangSX(MaHangSX) ON DELETE SET NULL
);
-- Bảng LOTHUOC (Quản lý chi tiết từng đợt hàng, HSD và số lượng tồn từng lô)
CREATE TABLE LoThuoc (
    MaLo INT AUTO_INCREMENT PRIMARY KEY,
    MaThuoc INT NOT NULL,
    SoLo VARCHAR(50) NOT NULL,               -- Mã lô sản xuất từ nhà máy (VD: LOT202601)
    NgaySanXuat DATE,
    HanSuDung DATE NOT NULL,                 -- Căn cứ để tự động trừ kho FEFO
    SoLuongTon INT NOT NULL DEFAULT 0,       -- Tồn kho thực tế của riêng Lô này
    GiaNhap DECIMAL(15, 2) NOT NULL DEFAULT 0,
    TrangThai TINYINT DEFAULT 1,             -- 1: Đang dùng, 0: Hết hạn / Khóa
    FOREIGN KEY (MaThuoc) REFERENCES Thuoc(MaThuoc) ON DELETE CASCADE
);

-- 4. BẢNG NHẬP KHO (Phieu nhap kho)
CREATE TABLE PhieuNhap (
    MaPN INT AUTO_INCREMENT PRIMARY KEY,
    MaNCC INT NOT NULL,                      -- Mã NCC quản lý ở cấp độ phiếu nhập
    MaNV INT NOT NULL,                       -- Nhân viên lập phiếu nhập (Thủ kho / Admin)
    NgayNhap DATETIME DEFAULT CURRENT_TIMESTAMP,
    TongTien DECIMAL(15, 2) DEFAULT 0,
    FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);

CREATE TABLE ChiTietPhieuNhap (
    MaCTPN INT AUTO_INCREMENT PRIMARY KEY,
    MaPN INT NOT NULL,
    MaLo INT NOT NULL,                       -- Nhập trực tiếp vào Lô thuốc
    SoLuongNhap INT NOT NULL,
    DonGiaNhap DECIMAL(15, 2) NOT NULL,
    ThanhTien DECIMAL(15, 2) GENERATED ALWAYS AS (SoLuongNhap * DonGiaNhap) STORED,
    FOREIGN KEY (MaPN) REFERENCES PhieuNhap(MaPN) ON DELETE CASCADE,
    FOREIGN KEY (MaLo) REFERENCES LoThuoc(MaLo)
);

-- 5. BẢNG BÁN HÀNG (Dành cho TV3)
CREATE TABLE HoaDon (
    MaHD INT AUTO_INCREMENT PRIMARY KEY,
    MaNV INT NOT NULL,
    MaKH INT DEFAULT NULL,
    NgayLap DATETIME DEFAULT CURRENT_TIMESTAMP,
    TongTien DECIMAL(15, 2) DEFAULT 0,
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);

-- Bảng ChiTietHoaDon (Liên kết trực tiếp tới MaLo)
CREATE TABLE ChiTietHoaDon (
    MaCTHD INT AUTO_INCREMENT PRIMARY KEY,
    MaHD INT NOT NULL,
    MaLo INT NOT NULL, -- Liên kết với Lô Thuốc để lấy Giá Nhập của lô đó
    SoLuongBan INT NOT NULL,
    GiaBan DECIMAL(15, 2) NOT NULL,
    ThanhTien DECIMAL(15, 2) GENERATED ALWAYS AS (SoLuongBan * GiaBan) STORED,
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    FOREIGN KEY (MaLo) REFERENCES LoThuoc(MaLo)
);
-- 1. Nạp danh mục Thuốc (Chưa tính tồn kho)
INSERT INTO Thuoc (MaThuoc, TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX) VALUES
(1, 'Paracetamol 500mg', 'Hộp', 45000.00, 65000.00, 0, 1),
(2, 'Amoxicillin 500mg', 'Hộp', 85000.00, 115000.00, 0, 1),
(3, 'Panadol Extra', 'Vỉ', 12000.00, 18000.00, 0, 2),
(4, 'Efferalgan 500mg', 'Hộp', 50000.00, 72000.00, 0, 2);

-- 2. Nạp Lô Thuốc (Có nhiều Lô cho cùng 1 loại thuốc để test FEFO)
-- Paracetamol có 2 Lô: Lô 1 HSD cận hơn (tháng 12/2026), Lô 2 HSD xa hơn (tháng 08/2027)
INSERT INTO LoThuoc (MaLo, MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap) VALUES
(1, 1, 'LOT-PARA-01', '2025-01-10', '2026-12-31', 50, 45000.00),
(2, 1, 'LOT-PARA-02', '2025-06-15', '2027-08-15', 100, 45000.00),
(3, 2, 'LOT-AMOX-01', '2025-02-01', '2027-02-01', 80, 85000.00),
(4, 3, 'LOT-PANA-01', '2025-03-20', '2027-03-20', 150, 12000.00),
(5, 4, 'LOT-EFFE-01', '2025-05-10', '2027-05-10', 60, 50000.00);

-- 3. Cập nhật Tổng số lượng tồn ở bảng Thuoc dựa vào Tổng tồn các Lô
UPDATE Thuoc t 
SET SoLuongTon = (SELECT IFNULL(SUM(SoLuongTon), 0) FROM LoThuoc l WHERE l.MaThuoc = t.MaThuoc);

-- 4. Tạo Phiếu Nhập hàng mẫu
INSERT INTO PhieuNhap (MaPN, MaNCC, MaNV, NgayNhap, TongTien) VALUES
(1, 1, 1, '2026-01-15 08:30:00', 6750000.00),
(2, 2, 2, '2026-02-10 14:15:00', 6800000.00);

-- 5. Tạo Chi Tiết Phiếu Nhập hàng mẫu
INSERT INTO ChiTietPhieuNhap (MaPN, MaLo, SoLuongNhap, DonGiaNhap) VALUES
(1, 1, 50, 45000.00),  -- Phiếu 1 nhập 50 hộp Paracetamol Lô 1
(1, 2, 100, 45000.00), -- Phiếu 1 nhập 100 hộp Paracetamol Lô 2
(2, 3, 80, 85000.00);  -- Phiếu 2 nhập 80 hộp Amoxicillin Lô 1

-- 6. Bảng Hãng sản xuất (HangSX)
INSERT INTO HangSX (TenHang, QuocGia) VALUES
('AstraZeneca', 'Anh'),
('Pfizer', 'Mỹ'),
('Dược Hậu Giang (DHG)', 'Việt Nam'),
('Sanofi', 'Pháp'),
('Traphaco', 'Việt Nam');

-- 7. Bảng Loại thuốc (LoaiThuoc)
INSERT INTO LoaiThuoc (TenLoai, DonViTinh) VALUES
('Kháng sinh', 'Hộp'),
('Giảm đau - Hạ sốt', 'Vỉ'),
('Vitamin & TTPCN', 'Chai'),
('Siro ho', 'Chai'),
('Dụng cụ y tế', 'Cái');

-- 8. Bảng Nhà cung cấp (NhaCungCap)
INSERT INTO NhaCungCap (TenNCC, SoDienThoai) VALUES
('Công ty Cổ phần Dược phẩm Trung ương 1', '02438254321'),
('Công ty TNHH Dược phẩm Vimedimex', '02839301234'),
('Công ty CP Dược Hậu Giang - Chi nhánh HN', '02923891433'),
('Nhà phân phối Dược phẩm Zuellig Pharma', '02839103000');

-- 9. Bảng Khách hàng (KhachHang)
INSERT INTO KhachHang (TenKH, SoDienThoai, DiaChi, DiemTichLuy) VALUES
('Nguyễn Văn An', '0988123456', '123 Lý Thường Kiệt, Q.10, TP.HCM', 15),
('Trần Thị Bích', '0912345678', '45 Lê Lợi, Q.1, TP.HCM', 50),
('Lê Hoàng Nam', '0903111222', '78 Nguyễn Trãi, Q.5, TP.HCM', 0),
('Phạm Thị Mai', '0977888999', '12 Đường số 5, Q.Bình Thạnh, TP.HCM', 100);

-- 10. Bảng Nhân viên (NhanVien)
-- (Lưu ý: Nhân viên MaNV = 1 đã tạo ở bước khởi tạo Admin)
INSERT INTO NhanVien (HoTen, ChucVu, SoDienThoai, TrangThai) VALUES
('Trần Thị Dược Sĩ', 'DuocSi', '0933444555', 1),
('Lê Văn Thủ Kho', 'ThuKho', '0944555666', 1),
('Nguyễn Mai Anh', 'DuocSi', '0955666777', 1);

-- 11. Bảng Tài khoản (TaiKhoan)
-- (Lưu ý: Tài khoản 'admin' gắn với MaNV = 1 đã có sẵn)
INSERT INTO TaiKhoan (TenDangNhap, MaNV, MatKhau, Quyen) VALUES
('duocsi01', 2, '123456', 'BANHANG'),
('thukho01', 3, '123456', 'THUKHO'),
('duocsi02', 4, '123456', 'BANHANG');

-- =============================================================
-- THÊM DỮ LIỆU NGHIỆP VỤ BÁN HÀNG (HoaDon & ChiTietHoaDon)
-- =============================================================

-- 12. Hóa đơn bán hàng 1 (Dược sĩ MaNV = 2 bán cho Khách hàng 1)
INSERT INTO HoaDon (MaHD, MaNV, NgayLap, TongTien) VALUES
(1, 1, '2026-10-20 09:00:00', 130000.00),
(2, 3, '2026-10-25 15:30:00', 230000.00);

-- Chi tiết: Bán 2 hộp Paracetamol (Lô 1, Giá nhập 45.000, Giá bán 65.000)
-- Chi tiết: Bán 2 hộp Amoxicillin (Lô 3, Giá nhập 85.000, Giá bán 115.000)
INSERT INTO ChiTietHoaDon (MaHD, MaLo, SoLuongBan, GiaBan) VALUES
(1, 1, 2, 65000.00),
(2, 3, 2, 115000.00);
-- 1. Xem danh sách thuốc kèm tên Loại và Hãng SX
SELECT 
    t.MaThuoc, t.TenThuoc, lt.TenLoai, h.TenHang, 
    t.GiaNhap, t.DonGia, t.SoLuongTon, t.HanSuDung
FROM Thuoc t
JOIN LoaiThuoc lt ON t.MaLoai = lt.MaLoai
JOIN HangSX h ON t.MaHangSX = h.MaHangSX;

-- 2. Xem các thuốc sắp hết hạn (dưới 30 ngày)
SELECT TenThuoc, HanSuDung, DATEDIFF(HanSuDung, CURDATE()) AS SoNgayConLai 
FROM Thuoc 
WHERE DATEDIFF(HanSuDung, CURDATE()) BETWEEN 0 AND 30;

-- 3. Xem danh sách hóa đơn cùng thông tin Nhân viên lập và Khách hàng mua
SELECT 
    hd.MaHD, hd.NgayLap, nv.HoTen AS NhanVienBan, 
    kh.TenKH AS KhachHang, hd.TongTien
FROM HoaDon hd
JOIN NhanVien nv ON hd.MaNV = nv.MaNV
JOIN KhachHang kh ON hd.MaKH = kh.MaKH;