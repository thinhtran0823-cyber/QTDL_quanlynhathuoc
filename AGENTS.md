# QUY TẮC DỰ ÁN — MODULE NHẬP KHO (thành viên: Phan Anh Ngữ)

Đây là đồ án nhóm ĐÃ CÓ SẴN (Node.js + Express + mysql2 cho phần máy chủ, Java Swing cho phần màn hình, MySQL).
Chỉ làm module NHẬP KHO. Phần của các thành viên khác phải giữ NGUYÊN 100%.

## 0. Quy tắc chung (bắt buộc)

- File nào KHÔNG nằm trong mục 1 thì CẤM sửa, kể cả format, đổi tên, sửa chính tả, "tiện tay" dọn code.
- Mỗi task phải tạo hoặc sửa ít nhất 1 file trong mục 1. Task chỉ đọc/báo cáo sẽ bị tính là thất bại.
- Không chạy docker, mysql, npm, node, git (commit/checkout/push). Không sửa CSDL thật. Không tạo `.env`.
- Không tạo bộ kiểm thử tự động. Kiểm tra bằng file SQL chạy tay (mục 7).
- Không thêm thư viện, không sửa `package.json`. Không tạo bảng/cột mới (muốn đổi cấu trúc thì ghi đề xuất vào `NHAPKHO_GHICHU.md`).
- Không dùng từ "frontend", "web", "giao diện" trong tên và mô tả task; gọi là "màn hình Swing".
- Gặp mâu thuẫn hoặc cần sửa file bị cấm: DỪNG phần đó, ghi vào `backend/sql/NHAPKHO_GHICHU.md`, không tự sửa.

## 1. File được phép

Tạo mới:
- `backend/sql/nhapkho_triggers.sql`
- `backend/sql/nhapkho_test.sql`
- `backend/sql/NHAPKHO_GHICHU.md`
- `frontend-java/src/views/NhapKhoPanel.java`
- `frontend-java/src/views/LoThuocPanel.java`

Viết lại toàn bộ (hiện chỉ là bản nháp rỗng):
- `backend/controllers/nhapKhoController.js`
- `backend/routes/nhapKhoRoute.js`

Sửa đúng MỘT dòng:
- `frontend-java/src/views/MainDashboard.java`: thay dòng
  `panelContent.add(taoPanelModule("MAN_HINH_NHAP_KHO", "MODULE 2: QUẢN LÝ KHO & NHẬP HÀNG (TV2)"), "NHAP_KHO");`
  bằng `panelContent.add(new NhapKhoPanel(), "NHAP_KHO");`. Không đổi gì khác (kể cả dòng `panelSidebar.add(btnNhapKho)` bị lặp).

## 2. File CẤM đụng vào

`backend/server.js`, `backend/config/db.js`, `backend/package*.json`, `backend/.env*`,
mọi controller/route khác (`auth`, `thongKe`, `danhMuc`, `nhanVien`, `banHang`, `thuoc`),
`frontend-java/src/utils/ApiClient.java`, `LoginForm.java`, `DanhMucPanel.java`, `NhanVienPanel.java`, `ThongKePanel.java`,
`quanlynhathuoc.sql`, `KhoiTao.sql`, `KhoiTaoCSDL.sql`, `Readme.txt`, mọi file docx/pptx/pdf/jpg.

Của người khác (không viết trigger/API thay họ): bán hàng (FEFO, trừ tồn, hủy hóa đơn khôi phục tồn), danh mục thuốc/loại thuốc/khách hàng, thống kê.

## 2b. Nguồn sự thật về CSDL

Chỉ tin `quanlynhathuoc.sql`. `KhoiTaoCSDL.sql` là bản nháp cũ (có `Thuoc.DonGia`, `HanSuDung` — SAI), bỏ qua.

- `NhaCungCap(MaNCC, TenNCC, SoDienThoai)`
- `NhanVien(MaNV, HoTen, ChucVu, SoDienThoai, TrangThai BIT)`
- `Thuoc(MaThuoc, TenThuoc, DonViTinh, GiaNhap, GiaBan, SoLuongTon, MaHangSX)` — `SoLuongTon` = tổng tồn của mọi lô
- `LoThuoc(MaLo, MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap, TrangThai)`
- `PhieuNhap(MaPN, MaNCC, MaNV, NgayNhap, TongTien)`
- `ChiTietPhieuNhap(MaCTPN, MaPN, MaLo, SoLuongNhap, DonGiaNhap, ThanhTien)` — `ThanhTien` là cột GENERATED, KHÔNG insert

Tên bảng: giữ kiểu PascalCase như code của nhóm (`NhaCungCap`, `PhieuNhap`...). Nếu thấy nguy cơ lỗi hoa/thường trên MySQL chạy hệ điều hành phân biệt hoa thường, chỉ ghi chú vào `NHAPKHO_GHICHU.md`.

## 3. Trigger (áp dụng bài thực hành TH3)

File `backend/sql/nhapkho_triggers.sql`:
- Bắt đầu bằng `USE quanlynhathuoc;`, dùng `DELIMITER $$`, mỗi trigger có `DROP TRIGGER IF EXISTS` phía trước để chạy lại nhiều lần được.
- Tên theo mẫu TH3: `trg_<VIẾT TẮT BẢNG>_<BI|AI>_<MôTả>`.
- Báo lỗi bằng `SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '...'` (tiếng Việt, ngắn gọn).
- Có comment tiếng Việt mô tả từng trigger.

Danh sách:
1. `trg_LT_BI_KiemTra` — BEFORE INSERT trên `LoThuoc`: `TRIM(SoLo)` không rỗng; `HanSuDung` phải sau `CURDATE()` và (nếu `NgaySanXuat` khác NULL) sau `NgaySanXuat`; `SoLuongTon` > 0; `GiaNhap` >= 0; không trùng cặp (`MaThuoc`, `SoLo`) → "Số lô đã tồn tại cho thuốc này"; gán lại `NEW.SoLo = TRIM(NEW.SoLo)`, `NEW.TrangThai = 1`. (Giống Câu 1, 5 của TH3.)
2. `trg_LT_AI_CongTonThuoc` — AFTER INSERT trên `LoThuoc`: `UPDATE Thuoc SET SoLuongTon = SoLuongTon + NEW.SoLuongTon WHERE MaThuoc = NEW.MaThuoc`. (Giống Câu 3.)
3. `trg_CTPN_BI_KiemTra` — BEFORE INSERT trên `ChiTietPhieuNhap`: `SoLuongNhap` > 0, `DonGiaNhap` >= 0.
4. `trg_CTPN_AI_TongTien` — AFTER INSERT trên `ChiTietPhieuNhap`: gán `PhieuNhap.TongTien` = `SUM(ThanhTien)` của phiếu đó.
5. `trg_PN_BI_KiemTra` — BEFORE INSERT trên `PhieuNhap`: nhân viên lập phiếu phải đang hoạt động (`TrangThai = 1`), nếu không → "Nhân viên không còn hoạt động"; `NgayNhap` NULL thì gán `NOW()`.

Ranh giới trigger:
- CHỈ trigger INSERT trên `LoThuoc`, `ChiTietPhieuNhap`, `PhieuNhap`.
- KHÔNG tạo trigger UPDATE/DELETE trên `LoThuoc` hay `Thuoc`, và KHÔNG tạo trigger trên `HoaDon`, `ChiTietHoaDon`, `KhachHang` (người khác sẽ làm; tạo thêm sẽ cộng/trừ tồn hai lần).
- Không tạo trigger/bảng nhật ký mới.

View phân loại lô (cùng file): `v_LoThuoc_TinhTrang` gồm `MaLo, MaThuoc, TenThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap`, `SoNgayConLai = DATEDIFF(HanSuDung, CURDATE())`, và cột `PhanLoai` = `'HET_HAN'` (< 0 ngày), `'SAP_HET_HAN'` (0–30 ngày), `'CON_HAN'` (> 30). Không UPDATE cột `TrangThai` của lô.

## 4. Máy chủ Node.js

- Dùng `const pool = require('../config/db');` (mysql2/promise). Mẫu transaction đã có ở `nhanVienController.js`: `getConnection` → `beginTransaction` → `commit` / `rollback` → `release` trong `finally`.
- Mọi response dạng `{ success, message, data }` như code nhóm. Comment tiếng Việt, `async/await`.
- Lỗi do trigger (SQLSTATE 45000): trả HTTP 400 với `message = error.sqlMessage`. Lỗi khác: 500.

Route (`nhapKhoRoute.js`), server.js đã mount sẵn ở `/api/nhap-kho`:
- `router.post('/phieu-nhap', taoPhieuNhap)`
- `router.get('/phieu-nhap', getPhieuNhap)`
- `router.get('/lo-thuoc', getLoThuoc)`
- Controller export: `{ taoPhieuNhap, getPhieuNhap, getLoThuoc }`.

`POST /api/nhap-kho/phieu-nhap`, body:
```json
{ "maNCC": 1, "maNV": 3,
  "chiTiet": [ { "maThuoc": 1, "soLo": "LOT-001", "ngaySanXuat": "2026-01-01",
                 "hanSuDung": "2028-01-01", "soLuong": 50, "giaNhap": 45000 } ] }
```
Quy trình trong MỘT transaction: kiểm tra mảng `chiTiet` không rỗng → INSERT `PhieuNhap(MaNCC, MaNV)` → với mỗi dòng: INSERT `LoThuoc(MaThuoc, SoLo, NgaySanXuat, HanSuDung, SoLuongTon, GiaNhap)` rồi INSERT `ChiTietPhieuNhap(MaPN, MaLo, SoLuongNhap, DonGiaNhap)` với `MaLo` = `insertId` vừa có (`SoLuongNhap` = `SoLuongTon` của lô). Cộng tồn `Thuoc` và `TongTien` do TRIGGER làm: KHÔNG tự `UPDATE Thuoc` và KHÔNG tự tính tổng tiền bằng JS. Cuối cùng commit và trả về phiếu vừa tạo.

`GET /api/nhap-kho/phieu-nhap`: trả danh sách PHẲNG (mỗi dòng = 1 chi tiết, lặp thông tin phiếu) vì màn hình Swing không có thư viện JSON: `MaPN, NgayNhap, TenNCC, HoTen (nhân viên), TongTien, TenThuoc, SoLo, HanSuDung, SoLuongNhap, DonGiaNhap, ThanhTien`; sắp `MaPN DESC`.

`GET /api/nhap-kho/lo-thuoc`: đọc từ view `v_LoThuoc_TinhTrang`, sắp `HanSuDung ASC`; tùy chọn `?phanLoai=CON_HAN|SAP_HET_HAN|HET_HAN`.

## 5. Màn hình Java Swing

- Java 17 thuần, `package views;`, constructor không tham số. Phong cách giống `DanhMucPanel`: font Arial, `BorderLayout`, `JTabbedPane`, `JTable` + `DefaultTableModel`.
- Gọi máy chủ CHỈ qua `utils.ApiClient.sendGet / sendPost` (không sửa ApiClient).
- Không có thư viện JSON: tự viết hàm `extractVal` riêng trong class của mình (copy ý tưởng từ `DanhMucPanel`, KHÔNG sửa file đó); response phẳng có thể tách bằng `split("\\}\\,\\{")` như code nhóm. Khi dựng JSON gửi lên phải escape `"` và `\`.
- Dữ liệu chọn: nhà cung cấp `GET /danh-muc/nha-cung-cap` (`MaNCC`, `TenNCC`); thuốc `GET /thuoc` (`MaThuoc`, `TenThuoc`, `GiaNhap`); nhân viên `GET /nhan-vien` (`MaNV`, `HoTen`). `MainDashboard` không truyền `MaNV`, nên dùng ô chọn "Nhân viên lập phiếu" (ghi chú điều này trong `NHAPKHO_GHICHU.md`).

`NhapKhoPanel extends JPanel` — `JTabbedPane` 3 tab:
1. " Lập phiếu nhập ": chọn NCC, nhân viên, thuốc; nhập số lô, ngày sản xuất (yyyy-MM-dd, được để trống), hạn sử dụng (yyyy-MM-dd), số lượng, giá nhập (tự điền từ giá nhập của thuốc, sửa được); nút "Thêm dòng" vào bảng tạm, "Xóa dòng", hiện tổng tiền tạm tính; nút "Lưu phiếu nhập" gọi POST. Thành công → thông báo, xóa bảng tạm, nạp lại lịch sử và lô. Lỗi → hiện đúng `message` máy chủ trả về (quy tắc nghiệp vụ nằm ở trigger, client chỉ kiểm tra số nguyên dương và đúng định dạng ngày).
2. " Lịch sử phiếu nhập ": bảng từ `GET /nhap-kho/phieu-nhap`, nút "Làm mới".
3. " Lô thuốc ": `new LoThuocPanel()`.

`LoThuocPanel extends JPanel`: bảng từ `GET /nhap-kho/lo-thuoc`; ô lọc Tất cả / Còn hạn / Sắp hết hạn (≤ 30 ngày) / Hết hạn; tô màu dòng bằng `DefaultTableCellRenderer` (hết hạn: đỏ nhạt, sắp hết hạn: vàng nhạt); nút "Làm mới".

## 6. Phân quyền hiện có (không đổi)

`MainDashboard` đã ẩn/hiện nút Nhập kho theo quyền (ADMIN, THUKHO). Không sửa phân quyền.

## 7. File kiểm tra chạy tay — `backend/sql/nhapkho_test.sql`

Theo kiểu "dữ liệu kiểm tra" của TH3: mỗi ca có chú thích kết quả mong đợi; dùng `START TRANSACTION ... ROLLBACK` để không làm bẩn dữ liệu; agent KHÔNG chạy file này. Các ca:
- HSD đã qua ngày → báo lỗi
- Số lượng 0 hoặc âm → báo lỗi
- Trùng (`MaThuoc`, `SoLo`) → báo lỗi
- Nhập hợp lệ → `Thuoc.SoLuongTon` tăng đúng bằng số lượng nhập, `PhieuNhap.TongTien` = tổng `ThanhTien`
- Nhân viên không hoạt động → báo lỗi

## 8. Bàn giao — `backend/sql/NHAPKHO_GHICHU.md`

Ngắn gọn: danh sách file đã tạo/sửa; thứ tự chạy (nạp `quanlynhathuoc.sql` → chạy `nhapkho_triggers.sql` → `node server.js` → chạy `LoginForm`); lưu ý cho nhóm trưởng và các bạn khác (trigger đã tự cộng tồn `Thuoc` khi thêm lô nên phần bán hàng không được cộng lại; `MaNV` không được truyền từ `MainDashboard`); mọi mâu thuẫn hoặc đề xuất sửa file ngoài phạm vi.
