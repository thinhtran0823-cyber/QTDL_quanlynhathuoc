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
    MaLoai INT NOT NULL,
    MaHangSX INT NOT NULL,
    MaNCC INT NOT NULL,
    TenThuoc VARCHAR(150) NOT NULL,
    CongDung VARCHAR(255),
    GiaNhap DECIMAL(15, 2) NOT NULL DEFAULT 0,
    DonGia DECIMAL(15, 2) NOT NULL DEFAULT 0,
    SoLuongTon INT NOT NULL DEFAULT 0,
    HanSuDung DATE NOT NULL,
    FOREIGN KEY (MaLoai) REFERENCES LoaiThuoc(MaLoai),
    FOREIGN KEY (MaHangSX) REFERENCES HangSX(MaHangSX),
    FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC)
);

-- 4. BẢNG NHẬP KHO (Dành cho TV2)
CREATE TABLE PhieuNhap (
    MaPhieuNhap INT AUTO_INCREMENT PRIMARY KEY,
    MaNCC INT NOT NULL,
    MaNV INT NOT NULL,
    NgayNhap DATETIME DEFAULT CURRENT_TIMESTAMP,
    TongTienNhap DECIMAL(15, 2) DEFAULT 0,
    FOREIGN KEY (MaNCC) REFERENCES NhaCungCap(MaNCC),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);

CREATE TABLE ChiTietPhieuNhap (
    MaCTPN INT AUTO_INCREMENT PRIMARY KEY,
    MaPhieuNhap INT NOT NULL,
    MaThuoc INT NOT NULL,
    SoLuongNhap INT NOT NULL CHECK (SoLuongNhap > 0),
    DonGiaNhap DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhap(MaPhieuNhap) ON DELETE CASCADE,
    FOREIGN KEY (MaThuoc) REFERENCES Thuoc(MaThuoc)
);

-- 5. BẢNG BÁN HÀNG (Dành cho TV3)
CREATE TABLE HoaDon (
    MaHD INT AUTO_INCREMENT PRIMARY KEY,
    MaKH INT NOT NULL,
    MaNV INT NOT NULL,
    NgayLap DATETIME DEFAULT CURRENT_TIMESTAMP,
    TongTien DECIMAL(15, 2) DEFAULT 0,
    FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);

CREATE TABLE ChiTietHoaDon (
    MaCTHD INT AUTO_INCREMENT PRIMARY KEY,
    MaHD INT NOT NULL,
    MaThuoc INT NOT NULL,
    SoLuongBan INT NOT NULL CHECK (SoLuongBan > 0),
    GiaBan DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (MaHD) REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    FOREIGN KEY (MaThuoc) REFERENCES Thuoc(MaThuoc)
);
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