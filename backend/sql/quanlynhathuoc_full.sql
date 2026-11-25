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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chitiethoadon`
--

LOCK TABLES `chitiethoadon` WRITE;
/*!40000 ALTER TABLE `chitiethoadon` DISABLE KEYS */;
INSERT INTO `chitiethoadon` (`MaCTHD`, `MaHD`, `MaLo`, `SoLuongBan`, `GiaBan`) VALUES (1,1,1,2,65000.00),(2,2,3,2,115000.00),(3,3,3,1,115000.00),(4,3,4,1,18000.00),(5,4,6,2,18000.00),(6,4,4,28,18000.00),(7,4,5,20,72000.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chitietphieunhap`
--

LOCK TABLES `chitietphieunhap` WRITE;
/*!40000 ALTER TABLE `chitietphieunhap` DISABLE KEYS */;
INSERT INTO `chitietphieunhap` (`MaCTPN`, `MaPN`, `MaLo`, `SoLuongNhap`, `DonGiaNhap`) VALUES (1,1,1,50,45000.00),(2,1,2,100,45000.00),(3,2,3,80,85000.00),(4,3,6,2,12000.00),(5,6,7,5,12000.00),(6,7,8,500,85000.00);
/*!40000 ALTER TABLE `chitietphieunhap` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_CTPN_BI_KiemTra` BEFORE INSERT ON `chitietphieunhap` FOR EACH ROW BEGIN
    -- Kiểm tra số lượng nhập
    IF NEW.SoLuongNhap IS NULL OR NEW.SoLuongNhap <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng nhập phải lớn hơn 0';
    END IF;

    -- Kiểm tra đơn giá nhập
    IF NEW.DonGiaNhap IS NULL OR NEW.DonGiaNhap < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn giá nhập không được âm';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_CTPN_AI_TongTien` AFTER INSERT ON `chitietphieunhap` FOR EACH ROW BEGIN
    UPDATE PhieuNhap
    SET TongTien = (
        SELECT COALESCE(SUM(ThanhTien), 0)
        FROM ChiTietPhieuNhap
        WHERE MaPN = NEW.MaPN
    )
    WHERE MaPN = NEW.MaPN;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_HangSX_BI_KiemTra` BEFORE INSERT ON `hangsx` FOR EACH ROW BEGIN
    IF NEW.TenHang IS NULL OR TRIM(NEW.TenHang) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất không được để trống!';
    END IF;

    SET NEW.TenHang = TRIM(NEW.TenHang);

    IF EXISTS (SELECT 1 FROM HangSX WHERE LOWER(TenHang) = LOWER(NEW.TenHang)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.QuocGia IS NOT NULL AND TRIM(NEW.QuocGia) <> '' THEN
        SET NEW.QuocGia = TRIM(NEW.QuocGia);
        IF NEW.QuocGia REGEXP '^[0-9]+$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tên quốc gia không hợp lệ!';
        END IF;
    ELSE
        SET NEW.QuocGia = NULL;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_HangSX_BU_KiemTra` BEFORE UPDATE ON `hangsx` FOR EACH ROW BEGIN
    IF NEW.TenHang IS NULL OR TRIM(NEW.TenHang) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất không được để trống!';
    END IF;

    SET NEW.TenHang = TRIM(NEW.TenHang);

    IF EXISTS (SELECT 1 FROM HangSX WHERE LOWER(TenHang) = LOWER(NEW.TenHang) AND MaHangSX <> OLD.MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên hãng sản xuất đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.QuocGia IS NOT NULL AND TRIM(NEW.QuocGia) <> '' THEN
        SET NEW.QuocGia = TRIM(NEW.QuocGia);
        IF NEW.QuocGia REGEXP '^[0-9]+$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Tên quốc gia không hợp lệ!';
        END IF;
    ELSE
        SET NEW.QuocGia = NULL;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_HangSX_BD_KiemTra` BEFORE DELETE ON `hangsx` FOR EACH ROW BEGIN
    IF EXISTS (SELECT 1 FROM Thuoc WHERE MaHangSX = OLD.MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa vì hãng sản xuất đang có thuốc trong danh mục!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hoadon`
--

LOCK TABLES `hoadon` WRITE;
/*!40000 ALTER TABLE `hoadon` DISABLE KEYS */;
INSERT INTO `hoadon` VALUES (1,1,NULL,'2026-10-20 09:00:00',130000.00),(2,3,NULL,'2026-10-25 15:30:00',230000.00),(3,1,2,'2026-11-15 20:48:19',133000.00),(4,1,1,'2026-11-19 10:19:21',1980000.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khachhang`
--

LOCK TABLES `khachhang` WRITE;
/*!40000 ALTER TABLE `khachhang` DISABLE KEYS */;
INSERT INTO `khachhang` VALUES (1,'Nguyễn Văn An','0988123456','123 Lý Thường Kiệt, Q.10, TP.HCM',15),(2,'Trần Thị Bích','0912345678','45 Lê Lợi, Q.1, TP.HCM',50),(3,'Lê Hoàng Nam','0903111222','78 Nguyễn Trãi, Q.5, TP.HCM',0),(4,'Phạm Thị Mai','0977888999','12 Đường số 5, Q.Bình Thạnh, TP.HCM',100),(5,'Thu Trang','0786956078','34/4 An Hòa',0);
/*!40000 ALTER TABLE `khachhang` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_KH_BD_KiemTra` BEFORE DELETE ON `khachhang` FOR EACH ROW BEGIN
    IF EXISTS (SELECT 1 FROM HoaDon WHERE MaKH = OLD.MaKH) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa khách hàng này vì đã có lịch sử hóa đơn mua hàng!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loaithuoc`
--

LOCK TABLES `loaithuoc` WRITE;
/*!40000 ALTER TABLE `loaithuoc` DISABLE KEYS */;
INSERT INTO `loaithuoc` VALUES (1,'Kháng sinh','Hộp'),(2,'Giảm đau - Hạ sốt','Vỉ'),(3,'Vitamin & TTPCN','Chai'),(4,'Siro ho','Chai'),(5,'Dụng cụ y tế','Hộp'),(6,'Thuốc ABC','Chai');
/*!40000 ALTER TABLE `loaithuoc` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LoaiThuoc_BI_KiemTra` BEFORE INSERT ON `loaithuoc` FOR EACH ROW BEGIN
    SET NEW.TenLoai = TRIM(COALESCE(NEW.TenLoai, ''));
    SET NEW.DonViTinh = TRIM(COALESCE(NEW.DonViTinh, ''));

    IF NEW.TenLoai = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc không được để trống!';
    END IF;

    IF NEW.DonViTinh = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính mặc định của loại thuốc không được để trống!';
    END IF;

    IF EXISTS (SELECT 1 FROM LoaiThuoc WHERE LOWER(TenLoai) = LOWER(NEW.TenLoai)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc này đã tồn tại trong hệ thống!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LoaiThuoc_BU_KiemTra` BEFORE UPDATE ON `loaithuoc` FOR EACH ROW BEGIN
    SET NEW.TenLoai = TRIM(COALESCE(NEW.TenLoai, ''));
    SET NEW.DonViTinh = TRIM(COALESCE(NEW.DonViTinh, ''));

    IF NEW.TenLoai = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc không được để trống!';
    END IF;

    IF NEW.DonViTinh = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính mặc định không được để trống!';
    END IF;

    IF EXISTS (
        SELECT 1 FROM LoaiThuoc 
        WHERE LOWER(TenLoai) = LOWER(NEW.TenLoai) AND MaLoai <> OLD.MaLoai
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên loại thuốc bị trùng với loại thuốc khác!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LoaiThuoc_BD_KiemTra` BEFORE DELETE ON `loaithuoc` FOR EACH ROW BEGIN
    IF EXISTS (SELECT 1 FROM Thuoc WHERE MaLoai = OLD.MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa loại thuốc này vì đang có thuốc thuộc loại này trong danh mục!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lothuoc`
--

LOCK TABLES `lothuoc` WRITE;
/*!40000 ALTER TABLE `lothuoc` DISABLE KEYS */;
INSERT INTO `lothuoc` VALUES (1,1,'LOT-PARA-01','2025-01-10','2026-12-31',50,45000.00,1),(2,1,'LOT-PARA-02','2025-06-15','2027-08-15',100,45000.00,1),(3,2,'LOT-AMOX-01','2025-02-01','2027-02-01',79,85000.00,1),(4,3,'LOT-PANA-01','2025-03-20','2027-03-20',121,12000.00,1),(5,4,'LOT-EFFE-01','2025-05-10','2027-05-10',40,50000.00,1),(6,3,'LO_TO','2026-11-19','2026-12-23',0,12000.00,1),(7,3,'Lo_Hong','2026-11-19','2026-11-20',5,12000.00,1),(8,2,'Lo_Hong_2','2026-11-18','2026-11-20',500,85000.00,1);
/*!40000 ALTER TABLE `lothuoc` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LT_BI_KiemTra` BEFORE INSERT ON `lothuoc` FOR EACH ROW BEGIN
    -- Kiểm tra số lô không được rỗng
    IF NEW.SoLo IS NULL OR TRIM(NEW.SoLo) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lô không được để trống';
    END IF;

    -- Chuẩn hóa dữ liệu số lô và trạng thái
    SET NEW.SoLo = TRIM(NEW.SoLo);
    SET NEW.TrangThai = 1;

    -- Kiểm tra hạn sử dụng phải sau ngày hiện tại
    IF NEW.HanSuDung IS NULL OR NEW.HanSuDung <= CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hạn sử dụng phải sau ngày hiện tại';
    END IF;

    -- Kiểm tra hạn sử dụng phải sau ngày sản xuất (nếu có ngày sản xuất)
    IF NEW.NgaySanXuat IS NOT NULL AND NEW.HanSuDung <= NEW.NgaySanXuat THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hạn sử dụng phải sau ngày sản xuất';
    END IF;

    -- Kiểm tra số lượng tồn ban đầu phải > 0
    IF NEW.SoLuongTon IS NULL OR NEW.SoLuongTon <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lượng tồn của lô phải lớn hơn 0';
    END IF;

    -- Kiểm tra giá nhập không được âm
    IF NEW.GiaNhap IS NULL OR NEW.GiaNhap < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá nhập của lô không được âm';
    END IF;

    -- Kiểm tra trùng lặp số lô cho cùng một loại thuốc
    IF EXISTS (
        SELECT 1 FROM LoThuoc
        WHERE MaThuoc = NEW.MaThuoc AND SoLo = NEW.SoLo
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Số lô đã tồn tại cho thuốc này';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LT_AI_CongTonThuoc` AFTER INSERT ON `lothuoc` FOR EACH ROW BEGIN
    UPDATE Thuoc
    SET SoLuongTon = SoLuongTon + NEW.SoLuongTon
    WHERE MaThuoc = NEW.MaThuoc;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_LT_BD_KiemTra` BEFORE DELETE ON `lothuoc` FOR EACH ROW BEGIN
    IF EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE MaLo = OLD.MaLo) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa lô thuốc này vì đã có dữ liệu trong hóa đơn bán hàng!';
    END IF;

    IF EXISTS (SELECT 1 FROM ChiTietPhieuNhap WHERE MaLo = OLD.MaLo) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa lô thuốc này vì đã có dữ liệu trong phiếu nhập kho!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NCC_BI_KiemTra` BEFORE INSERT ON `nhacungcap` FOR EACH ROW BEGIN
    IF NEW.TenNCC IS NULL OR TRIM(NEW.TenNCC) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp không được để trống!';
    END IF;

    SET NEW.TenNCC = TRIM(NEW.TenNCC);

    IF EXISTS (SELECT 1 FROM NhaCungCap WHERE LOWER(TenNCC) = LOWER(NEW.TenNCC)) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhaCungCap WHERE SoDienThoai = NEW.SoDienThoai) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhà cung cấp khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NCC_BU_KiemTra` BEFORE UPDATE ON `nhacungcap` FOR EACH ROW BEGIN
    IF NEW.TenNCC IS NULL OR TRIM(NEW.TenNCC) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp không được để trống!';
    END IF;

    SET NEW.TenNCC = TRIM(NEW.TenNCC);

    IF EXISTS (SELECT 1 FROM NhaCungCap WHERE LOWER(TenNCC) = LOWER(NEW.TenNCC) AND MaNCC <> OLD.MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên nhà cung cấp đã tồn tại trong hệ thống!';
    END IF;

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhaCungCap WHERE SoDienThoai = NEW.SoDienThoai AND MaNCC <> OLD.MaNCC) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhà cung cấp khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NCC_BD_KiemTra` BEFORE DELETE ON `nhacungcap` FOR EACH ROW BEGIN
    IF EXISTS (SELECT 1 FROM PhieuNhap WHERE MaNCC = OLD.MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa vì nhà cung cấp này đã có phiếu nhập kho!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhanvien`
--

LOCK TABLES `nhanvien` WRITE;
/*!40000 ALTER TABLE `nhanvien` DISABLE KEYS */;
INSERT INTO `nhanvien` VALUES (1,'Quan Ly System','QuanLy','0900000000',_binary ''),(2,'Trần Thị Dược Sĩ','DuocSi','0933444555',_binary ''),(3,'Lê Văn Thủ Kho','ThuKho','0944555666',_binary ''),(4,'Nguyễn Mai Anh','DuocSi','0955666777',_binary ''),(5,'Mã Văn Tần','DuocSi','0766656789',_binary '');
/*!40000 ALTER TABLE `nhanvien` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NV_BI_KiemTra` BEFORE INSERT ON `nhanvien` FOR EACH ROW BEGIN
    IF NEW.HoTen IS NULL OR TRIM(NEW.HoTen) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Họ và tên nhân viên không được để trống!';
    END IF;

    IF NEW.ChucVu IS NULL OR TRIM(NEW.ChucVu) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chức vụ nhân viên không được để trống!';
    END IF;

    SET NEW.HoTen = TRIM(NEW.HoTen);
    SET NEW.ChucVu = TRIM(NEW.ChucVu);

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!';
        END IF;
        IF EXISTS (SELECT 1 FROM NhanVien WHERE SoDienThoai = NEW.SoDienThoai) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhân viên khác!';
        END IF;
    ELSE
        SET NEW.SoDienThoai = NULL;
    END IF;

    IF NEW.TrangThai IS NULL THEN
        SET NEW.TrangThai = 1;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NV_BU_KiemTra` BEFORE UPDATE ON `nhanvien` FOR EACH ROW BEGIN
    IF NEW.HoTen IS NULL OR TRIM(NEW.HoTen) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Họ và tên nhân viên không được để trống!';
    END IF;

    IF NEW.ChucVu IS NULL OR TRIM(NEW.ChucVu) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Chức vụ nhân viên không được để trống!';
    END IF;

    SET NEW.HoTen = TRIM(NEW.HoTen);
    SET NEW.ChucVu = TRIM(NEW.ChucVu);

    IF NEW.SoDienThoai IS NOT NULL AND TRIM(NEW.SoDienThoai) <> '' THEN
        SET NEW.SoDienThoai = TRIM(NEW.SoDienThoai);
        IF NEW.SoDienThoai NOT REGEXP '^(0|\\+84)[0-9]{8,10}$' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số điện thoại không hợp lệ (phải gồm 9-11 chữ số, bắt đầu bằng 0 hoặc +84)!'; END IF; IF EXISTS (SELECT 1 FROM NhanVien WHERE SoDienThoai = NEW.SoDienThoai AND MaNV <> OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số điện thoại đã thuộc về một nhân viên khác!'; END IF; ELSE SET NEW.SoDienThoai = NULL; END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_NV_BD_KiemTra` BEFORE DELETE ON `nhanvien` FOR EACH ROW BEGIN IF EXISTS (SELECT 1 FROM HoaDon WHERE MaNV = OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa nhân viên vì đã có lịch sử lập hóa đơn bán hàng!'; END IF;      IF EXISTS (SELECT 1 FROM PhieuNhap WHERE MaNV = OLD.MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không thể xóa nhân viên vì đã có lịch sử lập phiếu nhập kho!'; END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phieunhap`
--

LOCK TABLES `phieunhap` WRITE;
/*!40000 ALTER TABLE `phieunhap` DISABLE KEYS */;
INSERT INTO `phieunhap` VALUES (1,1,1,'2026-01-15 08:30:00',6750000.00),(2,2,2,'2026-02-10 14:15:00',6800000.00),(3,5,5,'2026-11-19 10:17:22',24000.00),(6,5,5,'2026-11-19 10:33:19',60000.00),(7,5,5,'2026-11-19 10:34:50',42500000.00);
/*!40000 ALTER TABLE `phieunhap` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_PN_BI_KiemTra` BEFORE INSERT ON `phieunhap` FOR EACH ROW BEGIN
    -- Kiểm tra nhân viên còn hoạt động hay không
    IF NOT EXISTS (
        SELECT 1 FROM NhanVien
        WHERE MaNV = NEW.MaNV AND TrangThai = 1
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Nhân viên không còn hoạt động';
    END IF;

    -- Tự động điền ngày nhập nếu client không truyền
    IF NEW.NgayNhap IS NULL THEN
        SET NEW.NgayNhap = NOW();
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_TK_BI_KiemTra` BEFORE INSERT ON `taikhoan` FOR EACH ROW BEGIN IF NEW.TenDangNhap IS NULL OR TRIM(NEW.TenDangNhap) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được để trống!'; END IF;      SET NEW.TenDangNhap = TRIM(NEW.TenDangNhap);      IF NEW.TenDangNhap REGEXP '[[:space:]]' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được chứa khoảng trắng!'; END IF;      IF EXISTS (SELECT 1 FROM TaiKhoan WHERE LOWER(TenDangNhap) = LOWER(NEW.TenDangNhap)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập đã tồn tại, vui lòng chọn tài khoản khác!'; END IF;      IF NEW.MatKhau IS NULL OR TRIM(NEW.MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      IF CHAR_LENGTH(NEW.MatKhau) < 4 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu phải có ít nhất 4 ký tự!'; END IF;      IF NEW.Quyen IS NULL OR TRIM(NEW.Quyen) = '' THEN SET NEW.Quyen = 'BANHANG'; END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_TK_BU_KiemTra` BEFORE UPDATE ON `taikhoan` FOR EACH ROW BEGIN IF NEW.MatKhau IS NULL OR TRIM(NEW.MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      IF NEW.Quyen IS NULL OR TRIM(NEW.Quyen) = '' THEN SET NEW.Quyen = OLD.Quyen; END IF; END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

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
  `GiaBan` decimal(15,2) NOT NULL DEFAULT '0.00',
  `SoLuongTon` int NOT NULL DEFAULT '0',
  `MaHangSX` int DEFAULT NULL,
  `MaLoai` int DEFAULT NULL,
  PRIMARY KEY (`MaThuoc`),
  KEY `MaHangSX` (`MaHangSX`),
  KEY `fk_Thuoc_LoaiThuoc` (`MaLoai`),
  CONSTRAINT `fk_Thuoc_LoaiThuoc` FOREIGN KEY (`MaLoai`) REFERENCES `loaithuoc` (`MaLoai`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `thuoc_ibfk_1` FOREIGN KEY (`MaHangSX`) REFERENCES `hangsx` (`MaHangSX`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuoc`
--

LOCK TABLES `thuoc` WRITE;
/*!40000 ALTER TABLE `thuoc` DISABLE KEYS */;
INSERT INTO `thuoc` VALUES (1,'Paracetamol 500mg','Hộp',65000.00,150,1,2),(2,'Amoxicillin 500mg','Hộp',115000.00,579,1,5),(3,'Panadol Extra','Vỉ',18000.00,126,2,1),(4,'Efferalgan 500mg','Hộp',72000.00,40,5,3),(7,'Payemal 500 gram','Hộp',54000.00,0,3,1);
/*!40000 ALTER TABLE `thuoc` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_Thuoc_BI_KiemTra` BEFORE INSERT ON `thuoc` FOR EACH ROW BEGIN
    IF NEW.TenThuoc IS NULL OR TRIM(NEW.TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF NEW.DonViTinh IS NULL OR TRIM(NEW.DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF NEW.GiaBan IS NULL OR NEW.GiaBan < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    SET NEW.TenThuoc = TRIM(NEW.TenThuoc);
    SET NEW.DonViTinh = TRIM(NEW.DonViTinh);
    -- Tồn kho khi tạo thuốc mới luôn bằng 0 (chỉ tăng qua nghiệp vụ Nhập kho)
    SET NEW.SoLuongTon = 0;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_Thuoc_BU_KiemTra` BEFORE UPDATE ON `thuoc` FOR EACH ROW BEGIN
    IF NEW.TenThuoc IS NULL OR TRIM(NEW.TenThuoc) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Tên thuốc không được để trống';
    END IF;

    IF NEW.DonViTinh IS NULL OR TRIM(NEW.DonViTinh) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Đơn vị tính không được để trống';
    END IF;

    IF NEW.GiaBan IS NULL OR NEW.GiaBan < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán không được nhỏ hơn 0';
    END IF;

    SET NEW.TenThuoc = TRIM(NEW.TenThuoc);
    SET NEW.DonViTinh = TRIM(NEW.DonViTinh);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_Thuoc_BD_KiemTra` BEFORE DELETE ON `thuoc` FOR EACH ROW BEGIN
    IF EXISTS (
        SELECT 1 
        FROM ChiTietHoaDon cthd
        JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
        WHERE lt.MaThuoc = OLD.MaThuoc
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đã phát sinh giao dịch trong hóa đơn bán hàng!';
    END IF;

    IF EXISTS (
        SELECT 1 
        FROM ChiTietPhieuNhap ctpn
        JOIN LoThuoc lt ON ctpn.MaLo = lt.MaLo
        WHERE lt.MaThuoc = OLD.MaThuoc
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đã có lịch sử nhập kho!';
    END IF;

    IF EXISTS (SELECT 1 FROM LoThuoc WHERE MaThuoc = OLD.MaThuoc) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không thể xóa thuốc này vì đang tồn tại lô thuốc trong hệ thống!';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Temporary view structure for view `v_canhbaonhaphang`
--

DROP TABLE IF EXISTS `v_canhbaonhaphang`;
/*!50001 DROP VIEW IF EXISTS `v_canhbaonhaphang`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_canhbaonhaphang` AS SELECT 
 1 AS `MaThuoc`,
 1 AS `TenThuoc`,
 1 AS `DonViTinh`,
 1 AS `TenHang`,
 1 AS `GiaBan`,
 1 AS `TonKhaDung`,
 1 AS `TonHetHan`,
 1 AS `DaBan30Ngay`,
 1 AS `DeXuatNhapThem`,
 1 AS `TrangThaiCanhBao`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_lothuoc_tinhtrang`
--

DROP TABLE IF EXISTS `v_lothuoc_tinhtrang`;
/*!50001 DROP VIEW IF EXISTS `v_lothuoc_tinhtrang`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_lothuoc_tinhtrang` AS SELECT 
 1 AS `MaLo`,
 1 AS `MaThuoc`,
 1 AS `TenThuoc`,
 1 AS `SoLo`,
 1 AS `NgaySanXuat`,
 1 AS `HanSuDung`,
 1 AS `SoLuongTon`,
 1 AS `GiaNhap`,
 1 AS `SoNgayConLai`,
 1 AS `PhanLoai`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'quanlynhathuoc'
--

--
-- Dumping routines for database 'quanlynhathuoc'
--
/*!50003 DROP FUNCTION IF EXISTS `fn_SoLuongBanTrongNgay` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_SoLuongBanTrongNgay`(p_MaThuoc INT, p_SoNgay INT) RETURNS int
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_TongBan INT DEFAULT 0;

    SELECT COALESCE(SUM(cthd.SoLuongBan), 0) INTO v_TongBan
    FROM ChiTietHoaDon cthd
    JOIN HoaDon hd ON cthd.MaHD = hd.MaHD
    JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo
    WHERE lt.MaThuoc = p_MaThuoc
      AND hd.NgayLap >= DATE_SUB(CURDATE(), INTERVAL p_SoNgay DAY);

    RETURN v_TongBan;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TinhLoiNhuanRong` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TinhLoiNhuanRong`() RETURNS decimal(15,2)
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_DoanhThu DECIMAL(15,2);
    DECLARE v_GiaVon DECIMAL(15,2);
    DECLARE v_ThietHai DECIMAL(15,2);

    SELECT COALESCE(SUM(TongTien), 0) INTO v_DoanhThu FROM HoaDon;

    SELECT COALESCE(SUM(cthd.SoLuongBan * lt.GiaNhap), 0)
    INTO v_GiaVon
    FROM ChiTietHoaDon cthd
    JOIN LoThuoc lt ON cthd.MaLo = lt.MaLo;

    SET v_ThietHai = fn_TongThietHaiHetHan();

    RETURN v_DoanhThu - v_GiaVon - v_ThietHai;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TongThietHaiHetHan` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TongThietHaiHetHan`() RETURNS decimal(15,2)
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_TongThietHai DECIMAL(15,2);
    SELECT COALESCE(SUM(SoLuongTon * GiaNhap), 0)
    INTO v_TongThietHai
    FROM LoThuoc
    WHERE HanSuDung < CURDATE() AND SoLuongTon > 0;
    RETURN v_TongThietHai;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fn_TrangThaiHanLoThuoc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_TrangThaiHanLoThuoc`(p_MaLo INT) RETURNS varchar(30) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    READS SQL DATA
    DETERMINISTIC
BEGIN
    DECLARE v_SoNgay INT;
    SELECT DATEDIFF(HanSuDung, CURDATE()) INTO v_SoNgay
    FROM LoThuoc WHERE MaLo = p_MaLo;

    IF v_SoNgay IS NULL THEN
        RETURN 'KHONG_TON_TAI';
    ELSEIF v_SoNgay < 0 THEN
        RETURN 'HET_HAN';
    ELSEIF v_SoNgay <= 30 THEN
        RETURN 'SAP_HET_HAN';
    ELSE
        RETURN 'CON_HAN';
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_CapNhatThuoc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_CapNhatThuoc`(
    IN p_MaThuoc INT,
    IN p_TenThuoc VARCHAR(150),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT,
    IN p_MaLoai INT
)
BEGIN
    DECLARE v_GiaBan DECIMAL(15,2);

    IF NOT EXISTS (SELECT 1 FROM Thuoc WHERE MaThuoc = p_MaThuoc) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy thuốc cần cập nhật!';
    END IF;

    IF TRIM(COALESCE(p_GiaBan, '')) = '' OR p_GiaBan NOT REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán phải là số hợp lệ và không được âm!';
    END IF;

    SET v_GiaBan = CAST(p_GiaBan AS DECIMAL(15,2));

    IF p_MaHangSX IS NOT NULL AND NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hãng sản xuất được chọn không tồn tại!';
    END IF;

    IF p_MaLoai IS NOT NULL AND NOT EXISTS (SELECT 1 FROM LoaiThuoc WHERE MaLoai = p_MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loại thuốc được chọn không tồn tại!';
    END IF;

    UPDATE Thuoc
    SET TenThuoc = p_TenThuoc,
        DonViTinh = p_DonViTinh,
        GiaBan = v_GiaBan,
        MaHangSX = p_MaHangSX,
        MaLoai = p_MaLoai
    WHERE MaThuoc = p_MaThuoc;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_SuaHangSX` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_SuaHangSX`(
    IN p_MaHangSX INT,
    IN p_TenHang VARCHAR(200),
    IN p_QuocGia VARCHAR(100)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy hãng sản xuất để cập nhật!';
    END IF;

    UPDATE HangSX
    SET TenHang = p_TenHang,
        QuocGia = p_QuocGia
    WHERE MaHangSX = p_MaHangSX;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_SuaNhaCungCap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_SuaNhaCungCap`(
    IN p_MaNCC INT,
    IN p_TenNCC VARCHAR(200),
    IN p_SoDienThoai VARCHAR(20)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE MaNCC = p_MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy nhà cung cấp để cập nhật!';
    END IF;

    UPDATE NhaCungCap
    SET TenNCC = p_TenNCC,
        SoDienThoai = p_SoDienThoai
    WHERE MaNCC = p_MaNCC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_SuaNhanVien` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_SuaNhanVien`( IN p_MaNV INT, IN p_HoTen VARCHAR(150), IN p_ChucVu VARCHAR(50), IN p_SoDienThoai VARCHAR(20), IN p_Quyen VARCHAR(30) )
BEGIN DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = p_MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy nhân viên để cập nhật!'; END IF;      START TRANSACTION; UPDATE NhanVien SET HoTen = p_HoTen, ChucVu = p_ChucVu, SoDienThoai = p_SoDienThoai WHERE MaNV = p_MaNV;          IF p_Quyen IS NOT NULL AND TRIM(p_Quyen) <> '' THEN UPDATE TaiKhoan SET Quyen = TRIM(p_Quyen) WHERE MaNV = p_MaNV; END IF; COMMIT; END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThemHangSX` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThemHangSX`(
    IN p_TenHang VARCHAR(200),
    IN p_QuocGia VARCHAR(100)
)
BEGIN
    INSERT INTO HangSX (TenHang, QuocGia)
    VALUES (p_TenHang, p_QuocGia);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThemNhaCungCap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThemNhaCungCap`(
    IN p_TenNCC VARCHAR(200),
    IN p_SoDienThoai VARCHAR(20)
)
BEGIN
    INSERT INTO NhaCungCap (TenNCC, SoDienThoai)
    VALUES (p_TenNCC, p_SoDienThoai);
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThemNhanVien` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThemNhanVien`( IN p_HoTen VARCHAR(150), IN p_ChucVu VARCHAR(50), IN p_SoDienThoai VARCHAR(20), IN p_TenDangNhap VARCHAR(50), IN p_MatKhau VARCHAR(100), IN p_Quyen VARCHAR(30) )
BEGIN DECLARE v_NewMaNV INT; DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF p_TenDangNhap IS NULL OR TRIM(p_TenDangNhap) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tên đăng nhập không được để trống!'; END IF;      IF p_MatKhau IS NULL OR TRIM(p_MatKhau) = '' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Mật khẩu không được để trống!'; END IF;      START TRANSACTION; INSERT INTO NhanVien (HoTen, ChucVu, SoDienThoai, TrangThai) VALUES (p_HoTen, p_ChucVu, p_SoDienThoai, 1);          SET v_NewMaNV = LAST_INSERT_ID();          INSERT INTO TaiKhoan (TenDangNhap, MaNV, MatKhau, Quyen) VALUES (p_TenDangNhap, v_NewMaNV, p_MatKhau, IFNULL(NULLIF(TRIM(p_Quyen), ''), 'BANHANG')); COMMIT; END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThemThuoc` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThemThuoc`(
    IN p_TenThuoc VARCHAR(150),
    IN p_DonViTinh VARCHAR(50),
    IN p_GiaBan VARCHAR(50),
    IN p_MaHangSX INT,
    IN p_MaLoai INT
)
BEGIN
    DECLARE v_GiaBan DECIMAL(15,2);

    IF TRIM(COALESCE(p_GiaBan, '')) = '' OR p_GiaBan NOT REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Giá bán phải là số hợp lệ và không được âm!';
    END IF;

    SET v_GiaBan = CAST(p_GiaBan AS DECIMAL(15,2));

    IF p_MaHangSX IS NOT NULL AND NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Hãng sản xuất được chọn không tồn tại!';
    END IF;

    IF p_MaLoai IS NOT NULL AND NOT EXISTS (SELECT 1 FROM LoaiThuoc WHERE MaLoai = p_MaLoai) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loại thuốc được chọn không tồn tại!';
    END IF;

    INSERT INTO Thuoc (TenThuoc, DonViTinh, GiaBan, SoLuongTon, MaHangSX, MaLoai)
    VALUES (p_TenThuoc, p_DonViTinh, v_GiaBan, 0, p_MaHangSX, p_MaLoai);

    SELECT LAST_INSERT_ID() AS MaThuoc;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_ThongKeDoanhThu` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ThongKeDoanhThu`(
    IN p_LoaiBaoCao VARCHAR(10)
)
BEGIN
    IF UPPER(p_LoaiBaoCao) = 'NGAY' THEN
        SELECT 
            DATE(NgayLap) AS Ngay,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY DATE(NgayLap)
        ORDER BY Ngay DESC;
        
    ELSEIF UPPER(p_LoaiBaoCao) = 'TUAN' THEN
        SELECT 
            YEAR(NgayLap) AS Nam,
            WEEK(NgayLap, 1) AS TuanThu,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY YEAR(NgayLap), WEEK(NgayLap, 1)
        ORDER BY Nam DESC, TuanThu DESC;
        
    ELSEIF UPPER(p_LoaiBaoCao) = 'THANG' THEN
        SELECT 
            YEAR(NgayLap) AS Nam,
            MONTH(NgayLap) AS Thang,
            COUNT(MaHD) AS TongSoHoaDon,
            IFNULL(SUM(TongTien), 0) AS DoanhThu
        FROM HoaDon
        GROUP BY YEAR(NgayLap), MONTH(NgayLap)
        ORDER BY Nam DESC, Thang DESC;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaHangSX` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaHangSX`(
    IN p_MaHangSX INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM HangSX WHERE MaHangSX = p_MaHangSX) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy hãng sản xuất để xóa!';
    END IF;

    DELETE FROM HangSX WHERE MaHangSX = p_MaHangSX;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaNhaCungCap` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaNhaCungCap`(
    IN p_MaNCC INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE MaNCC = p_MaNCC) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Không tìm thấy nhà cung cấp để xóa!';
    END IF;

    DELETE FROM NhaCungCap WHERE MaNCC = p_MaNCC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_XoaNhanVien` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_XoaNhanVien`( IN p_MaNV INT )
BEGIN DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;      IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = p_MaNV) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Không tìm thấy nhân viên để xóa!'; END IF;      START TRANSACTION; DELETE FROM TaiKhoan WHERE MaNV = p_MaNV; DELETE FROM NhanVien WHERE MaNV = p_MaNV; COMMIT; END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Current Database: `quanlynhathuoc`
--

USE `quanlynhathuoc`;

--
-- Final view structure for view `v_canhbaonhaphang`
--

/*!50001 DROP VIEW IF EXISTS `v_canhbaonhaphang`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_canhbaonhaphang` AS select `t`.`MaThuoc` AS `MaThuoc`,`t`.`TenThuoc` AS `TenThuoc`,`t`.`DonViTinh` AS `DonViTinh`,coalesce(`hsx`.`TenHang`,'Chưa rõ hãng') AS `TenHang`,`t`.`GiaBan` AS `GiaBan`,coalesce(sum((case when ((`lt`.`HanSuDung` > curdate()) and (`lt`.`TrangThai` = 1)) then `lt`.`SoLuongTon` else 0 end)),0) AS `TonKhaDung`,coalesce(sum((case when (`lt`.`HanSuDung` <= curdate()) then `lt`.`SoLuongTon` else 0 end)),0) AS `TonHetHan`,`fn_SoLuongBanTrongNgay`(`t`.`MaThuoc`,30) AS `DaBan30Ngay`,greatest(0,((30 + `fn_SoLuongBanTrongNgay`(`t`.`MaThuoc`,30)) - coalesce(sum((case when ((`lt`.`HanSuDung` > curdate()) and (`lt`.`TrangThai` = 1)) then `lt`.`SoLuongTon` else 0 end)),0))) AS `DeXuatNhapThem`,(case when (coalesce(sum((case when ((`lt`.`HanSuDung` > curdate()) and (`lt`.`TrangThai` = 1)) then `lt`.`SoLuongTon` else 0 end)),0) = 0) then 'HET_HANG' when (coalesce(sum((case when ((`lt`.`HanSuDung` > curdate()) and (`lt`.`TrangThai` = 1)) then `lt`.`SoLuongTon` else 0 end)),0) < 20) then 'TON_THAP' when (`fn_SoLuongBanTrongNgay`(`t`.`MaThuoc`,30) >= 20) then 'BAN_CHAY' else 'AN_TOAN' end) AS `TrangThaiCanhBao` from ((`thuoc` `t` left join `hangsx` `hsx` on((`t`.`MaHangSX` = `hsx`.`MaHangSX`))) left join `lothuoc` `lt` on((`t`.`MaThuoc` = `lt`.`MaThuoc`))) group by `t`.`MaThuoc`,`t`.`TenThuoc`,`t`.`DonViTinh`,`hsx`.`TenHang`,`t`.`GiaBan` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_lothuoc_tinhtrang`
--

/*!50001 DROP VIEW IF EXISTS `v_lothuoc_tinhtrang`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_lothuoc_tinhtrang` AS select `lt`.`MaLo` AS `MaLo`,`lt`.`MaThuoc` AS `MaThuoc`,`t`.`TenThuoc` AS `TenThuoc`,`lt`.`SoLo` AS `SoLo`,`lt`.`NgaySanXuat` AS `NgaySanXuat`,`lt`.`HanSuDung` AS `HanSuDung`,`lt`.`SoLuongTon` AS `SoLuongTon`,`lt`.`GiaNhap` AS `GiaNhap`,(to_days(`lt`.`HanSuDung`) - to_days(curdate())) AS `SoNgayConLai`,(case when ((to_days(`lt`.`HanSuDung`) - to_days(curdate())) < 0) then 'HET_HAN' when ((to_days(`lt`.`HanSuDung`) - to_days(curdate())) <= 30) then 'SAP_HET_HAN' else 'CON_HAN' end) AS `PhanLoai` from (`lothuoc` `lt` join `thuoc` `t` on((`lt`.`MaThuoc` = `t`.`MaThuoc`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-26  3:29:35
