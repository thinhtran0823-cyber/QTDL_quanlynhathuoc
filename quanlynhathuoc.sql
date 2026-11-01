-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: quanlynhathuoc
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `quanlynhathuoc`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `quanlynhathuoc` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `quanlynhathuoc`;

--
-- Table structure for table `chitiethoadon`
--

DROP TABLE IF EXISTS `chitiethoadon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chitiethoadon` (
  `MaCTHD` int NOT NULL AUTO_INCREMENT,
  `MaHD` int NOT NULL,
  `MaLo` int NOT NULL,
  `SoLuongBan` int NOT NULL,
  `GiaBan` decimal(15,2) NOT NULL,
  `ThanhTien` decimal(15,2) GENERATED ALWAYS AS ((`SoLuongBan` * `GiaBan`)) STORED,
  PRIMARY KEY (`MaCTHD`),
  KEY `MaHD` (`MaHD`),
  KEY `MaLo` (`MaLo`),
  CONSTRAINT `chitiethoadon_ibfk_1` FOREIGN KEY (`MaHD`) REFERENCES `hoadon` (`MaHD`) ON DELETE CASCADE,
  CONSTRAINT `chitiethoadon_ibfk_2` FOREIGN KEY (`MaLo`) REFERENCES `lothuoc` (`MaLo`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chitiethoadon`
--

LOCK TABLES `chitiethoadon` WRITE;
/*!40000 ALTER TABLE `chitiethoadon` DISABLE KEYS */;
INSERT INTO `chitiethoadon` (`MaCTHD`, `MaHD`, `MaLo`, `SoLuongBan`, `GiaBan`) VALUES (1,1,1,2,65000.00),(2,2,3,2,115000.00);
/*!40000 ALTER TABLE `chitiethoadon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chitietphieunhap`
--

DROP TABLE IF EXISTS `chitietphieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chitietphieunhap` (
  `MaCTPN` int NOT NULL AUTO_INCREMENT,
  `MaPN` int NOT NULL,
  `MaLo` int NOT NULL,
  `SoLuongNhap` int NOT NULL,
  `DonGiaNhap` decimal(15,2) NOT NULL,
  `ThanhTien` decimal(15,2) GENERATED ALWAYS AS ((`SoLuongNhap` * `DonGiaNhap`)) STORED,
  PRIMARY KEY (`MaCTPN`),
  KEY `MaPN` (`MaPN`),
  KEY `MaLo` (`MaLo`),
  CONSTRAINT `chitietphieunhap_ibfk_1` FOREIGN KEY (`MaPN`) REFERENCES `phieunhap` (`MaPN`) ON DELETE CASCADE,
  CONSTRAINT `chitietphieunhap_ibfk_2` FOREIGN KEY (`MaLo`) REFERENCES `lothuoc` (`MaLo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chitietphieunhap`
--

LOCK TABLES `chitietphieunhap` WRITE;
/*!40000 ALTER TABLE `chitietphieunhap` DISABLE KEYS */;
INSERT INTO `chitietphieunhap` (`MaCTPN`, `MaPN`, `MaLo`, `SoLuongNhap`, `DonGiaNhap`) VALUES (1,1,1,50,45000.00),(2,1,2,100,45000.00),(3,2,3,80,85000.00);
/*!40000 ALTER TABLE `chitietphieunhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hangsx`
--

DROP TABLE IF EXISTS `hangsx`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hangsx` (
  `MaHangSX` int NOT NULL AUTO_INCREMENT,
  `TenHang` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `QuocGia` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`MaHangSX`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hangsx`
--

LOCK TABLES `hangsx` WRITE;
/*!40000 ALTER TABLE `hangsx` DISABLE KEYS */;
INSERT INTO `hangsx` VALUES (1,'AstraZeneca','Anh'),(2,'Pfizer','Mỹ'),(3,'Dược Hậu Giang (DHG)','Việt Nam'),(4,'Sanofi','Pháp'),(5,'Traphaco','Việt Nam'),(7,'Tunania','Úc');
/*!40000 ALTER TABLE `hangsx` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hoadon`
--

DROP TABLE IF EXISTS `hoadon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hoadon` (
  `MaHD` int NOT NULL AUTO_INCREMENT,
  `MaNV` int NOT NULL,
  `MaKH` int DEFAULT NULL,
  `NgayLap` datetime DEFAULT CURRENT_TIMESTAMP,
  `TongTien` decimal(15,2) DEFAULT '0.00',
  PRIMARY KEY (`MaHD`),
  KEY `MaNV` (`MaNV`),
  CONSTRAINT `hoadon_ibfk_1` FOREIGN KEY (`MaNV`) REFERENCES `nhanvien` (`MaNV`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoadon`
--

LOCK TABLES `hoadon` WRITE;
/*!40000 ALTER TABLE `hoadon` DISABLE KEYS */;
INSERT INTO `hoadon` VALUES (1,1,NULL,'2026-10-20 09:00:00',130000.00),(2,3,NULL,'2026-10-25 15:30:00',230000.00);
/*!40000 ALTER TABLE `hoadon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khachhang`
--

DROP TABLE IF EXISTS `khachhang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khachhang` (
  `MaKH` int NOT NULL AUTO_INCREMENT,
  `TenKH` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SoDienThoai` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DiaChi` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DiemTichLuy` int DEFAULT '0',
  PRIMARY KEY (`MaKH`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'Nguyễn Văn An','0988123456','123 Lý Thường Kiệt, Q.10, TP.HCM',15),(2,'Trần Thị Bích','0912345678','45 Lê Lợi, Q.1, TP.HCM',50),(3,'Lê Hoàng Nam','0903111222','78 Nguyễn Trãi, Q.5, TP.HCM',0),(4,'Phạm Thị Mai','0977888999','12 Đường số 5, Q.Bình Thạnh, TP.HCM',100);
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loaithuoc`
--

DROP TABLE IF EXISTS `loaithuoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loaithuoc` (
  `MaLoai` int NOT NULL AUTO_INCREMENT,
  `TenLoai` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DonViTinh` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`MaLoai`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loaithuoc`
--

LOCK TABLES `loaithuoc` WRITE;
/*!40000 ALTER TABLE `loaithuoc` DISABLE KEYS */;
INSERT INTO `loaithuoc` VALUES (1,'Kháng sinh','Hộp'),(2,'Giảm đau - Hạ sốt','Vỉ'),(3,'Vitamin & TTPCN','Chai'),(4,'Siro ho','Chai'),(5,'Dụng cụ y tế','Cái');
/*!40000 ALTER TABLE `loaithuoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lothuoc`
--

DROP TABLE IF EXISTS `lothuoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lothuoc` (
  `MaLo` int NOT NULL AUTO_INCREMENT,
  `MaThuoc` int NOT NULL,
  `SoLo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NgaySanXuat` date DEFAULT NULL,
  `HanSuDung` date NOT NULL,
  `SoLuongTon` int NOT NULL DEFAULT '0',
  `GiaNhap` decimal(15,2) NOT NULL DEFAULT '0.00',
  `TrangThai` tinyint DEFAULT '1',
  PRIMARY KEY (`MaLo`),
  KEY `MaThuoc` (`MaThuoc`),
  CONSTRAINT `lothuoc_ibfk_1` FOREIGN KEY (`MaThuoc`) REFERENCES `thuoc` (`MaThuoc`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lothuoc`
--

LOCK TABLES `lothuoc` WRITE;
/*!40000 ALTER TABLE `lothuoc` DISABLE KEYS */;
INSERT INTO `lothuoc` VALUES (1,1,'LOT-PARA-01','2025-01-10','2026-12-31',50,45000.00,1),(2,1,'LOT-PARA-02','2025-06-15','2027-08-15',100,45000.00,1),(3,2,'LOT-AMOX-01','2025-02-01','2027-02-01',80,85000.00,1),(4,3,'LOT-PANA-01','2025-03-20','2027-03-20',150,12000.00,1),(5,4,'LOT-EFFE-01','2025-05-10','2027-05-10',60,50000.00,1);
/*!40000 ALTER TABLE `lothuoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhacungcap`
--

DROP TABLE IF EXISTS `nhacungcap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhacungcap` (
  `MaNCC` int NOT NULL AUTO_INCREMENT,
  `TenNCC` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SoDienThoai` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`MaNCC`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhacungcap`
--

LOCK TABLES `nhacungcap` WRITE;
/*!40000 ALTER TABLE `nhacungcap` DISABLE KEYS */;
INSERT INTO `nhacungcap` VALUES (1,'Công ty Cổ phần Dược phẩm Trung ương 1','02438254321'),(2,'Công ty TNHH Dược phẩm Vimedimex','02839301234'),(3,'Công ty CP Dược Hậu Giang - Chi nhánh HN','02923891433'),(4,'Nhà phân phối Dược phẩm Zuellig Pharma','02839103000'),(5,'Công ty TNHH Tapacolrina','0122456378');
/*!40000 ALTER TABLE `nhacungcap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhanvien`
--

DROP TABLE IF EXISTS `nhanvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhanvien` (
  `MaNV` int NOT NULL AUTO_INCREMENT,
  `HoTen` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ChucVu` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `SoDienThoai` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TrangThai` bit(1) DEFAULT b'1',
  PRIMARY KEY (`MaNV`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Quan Ly System','QuanLy','0900000000',_binary ''),(2,'Trần Thị Dược Sĩ','DuocSi','0933444555',_binary ''),(3,'Lê Văn Thủ Kho','ThuKho','0944555666',_binary ''),(4,'Nguyễn Mai Anh','DuocSi','0955666777',_binary ''),(5,'Mã Văn Tần','DuocSi','0766656789',_binary '');
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phieunhap`
--

DROP TABLE IF EXISTS `phieunhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phieunhap` (
  `MaPN` int NOT NULL AUTO_INCREMENT,
  `MaNCC` int NOT NULL,
  `MaNV` int NOT NULL,
  `NgayNhap` datetime DEFAULT CURRENT_TIMESTAMP,
  `TongTien` decimal(15,2) DEFAULT '0.00',
  PRIMARY KEY (`MaPN`),
  KEY `MaNCC` (`MaNCC`),
  KEY `MaNV` (`MaNV`),
  CONSTRAINT `phieunhap_ibfk_1` FOREIGN KEY (`MaNCC`) REFERENCES `nhacungcap` (`MaNCC`),
  CONSTRAINT `phieunhap_ibfk_2` FOREIGN KEY (`MaNV`) REFERENCES `nhanvien` (`MaNV`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES (1,1,1,'2026-01-15 08:30:00',6750000.00),(2,2,2,'2026-02-10 14:15:00',6800000.00);
/*!40000 ALTER TABLE `phieunhap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `taikhoan`
--

DROP TABLE IF EXISTS `taikhoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `taikhoan` (
  `TenDangNhap` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `MaNV` int NOT NULL,
  `MatKhau` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Quyen` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`TenDangNhap`),
  UNIQUE KEY `MaNV` (`MaNV`),
  CONSTRAINT `taikhoan_ibfk_1` FOREIGN KEY (`MaNV`) REFERENCES `nhanvien` (`MaNV`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `taikhoan`
--

LOCK TABLES `taikhoan` WRITE;
/*!40000 ALTER TABLE `taikhoan` DISABLE KEYS */;
INSERT INTO `taikhoan` VALUES ('admin',1,'admin123','ADMIN'),('duocsi01',2,'123456','BANHANG'),('duocsi02',4,'123456','BANHANG'),('thukho01',3,'123456','THUKHO'),('thukho02',5,'123456','BANHANG');
/*!40000 ALTER TABLE `taikhoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thuoc`
--

DROP TABLE IF EXISTS `thuoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thuoc` (
  `MaThuoc` int NOT NULL AUTO_INCREMENT,
  `TenThuoc` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DonViTinh` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `GiaNhap` decimal(15,2) NOT NULL DEFAULT '0.00',
  `GiaBan` decimal(15,2) NOT NULL DEFAULT '0.00',
  `SoLuongTon` int NOT NULL DEFAULT '0',
  `MaHangSX` int DEFAULT NULL,
  PRIMARY KEY (`MaThuoc`),
  KEY `MaHangSX` (`MaHangSX`),
  CONSTRAINT `thuoc_ibfk_1` FOREIGN KEY (`MaHangSX`) REFERENCES `hangsx` (`MaHangSX`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuoc`
--

LOCK TABLES `thuoc` WRITE;
/*!40000 ALTER TABLE `thuoc` DISABLE KEYS */;
INSERT INTO `thuoc` VALUES (1,'Paracetamol 500mg','Hộp',45000.00,65000.00,150,1),(2,'Amoxicillin 500mg','Hộp',85000.00,115000.00,80,1),(3,'Panadol Extra','Vỉ',12000.00,18000.00,150,2),(4,'Efferalgan 500mg','Hộp',50000.00,72000.00,60,2);
/*!40000 ALTER TABLE `thuoc` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-01 15:01:19
