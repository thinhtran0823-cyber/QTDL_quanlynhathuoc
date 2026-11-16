# GHI CHÚ BÀN GIAO MODULE NHẬP KHO (MODULE 2)

**Thành viên phụ trách:** Phan Anh Ngữ (TV2)  
**Thời gian cập nhật:** 2026-11-16

---

## 1. Danh sách file đã tạo và chỉnh sửa

### File tạo mới:
- `backend/sql/nhapkho_triggers.sql`: Chứa 5 triggers nghiệp vụ kiểm tra và cập nhật tự động (chuẩn bài thực hành TH3) và 1 view `v_LoThuoc_TinhTrang` phân loại hạn sử dụng lô thuốc.
- `backend/sql/nhapkho_test.sql`: File chứa các kịch bản kiểm thử thủ công (chạy tay trong MySQL Workbench/DBeaver) có bọc `START TRANSACTION ... ROLLBACK`.
- `backend/sql/NHAPKHO_GHICHU.md`: Tài liệu ghi chú bàn giao kỹ thuật, thứ tự chạy và lưu ý tích hợp.
- `frontend-java/src/views/NhapKhoPanel.java`: Màn hình Swing Quản lý Nhập Kho (3 tab: Lập phiếu nhập, Lịch sử phiếu nhập, Lô thuốc).
- `frontend-java/src/views/LoThuocPanel.java`: Màn hình Swing Theo dõi Lô Thuốc (lọc hạn dùng, tô màu cảnh báo theo dòng).

### File viết lại toàn bộ:
- `backend/controllers/nhapKhoController.js`: Controller xử lý transaction tạo phiếu nhập và các endpoint lấy phiếu nhập, lô thuốc.
- `backend/routes/nhapKhoRoute.js`: Khai báo 3 route Express cho module Nhập kho.

### File sửa đúng một dòng:
- `frontend-java/src/views/MainDashboard.java`: Thay thế placeholder màn hình nhập kho bằng `new NhapKhoPanel()`.

---

## 2. Thứ tự triển khai và chạy hệ thống

1. **Khởi tạo cơ sở dữ liệu:**
   - Chạy file schema và dữ liệu gốc: `quanlynhathuoc.sql`.
2. **Cài đặt Trigger và View module Nhập kho:**
   - Chạy file: `backend/sql/nhapkho_triggers.sql`.
3. **Kiểm thử thủ công cơ sở dữ liệu (tùy chọn):**
   - Mở file `backend/sql/nhapkho_test.sql` trong MySQL Workbench để chạy từng test case đối chiếu kết quả.
4. **Khởi động Backend:**
   - Chạy lệnh: `cd backend && node server.js`.
5. **Khởi động ứng dụng màn hình Swing:**
   - Chạy lớp `LoginForm.java` hoặc `MainDashboard.java`.

---

## 3. Lưu ý quan trọng cho nhóm trưởng và các thành viên khác

### 3.1. Về cơ chế cộng trừ tồn kho:
- Khi thêm lô thuốc mới (`LoThuoc`), trigger `trg_LT_AI_CongTonThuoc` **đã tự động cộng dồn số lượng tồn vào `Thuoc.SoLuongTon`**.
- Do đó, phần backend Node.js khi tạo phiếu nhập **không tự thực hiện lệnh `UPDATE Thuoc`** để tránh cộng tồn hai lần.
- **Lưu ý cho TV3 (Module Bán hàng):** Phần bán hàng trừ tồn thuốc và khôi phục tồn khi hủy hóa đơn cần thống nhất cơ chế (nếu dùng trigger thì chú ý không viết trigger chồng chéo lên sự kiện INSERT của `LoThuoc`/`ChiTietPhieuNhap`).

### 3.2. Về tính tổng tiền phiếu nhập:
- Trigger `trg_CTPN_AI_TongTien` tự động tính tổng `ThanhTien` từ các dòng chi tiết `ChiTietPhieuNhap` và cập nhật vào `PhieuNhap.TongTien`.
- Cột `ChiTietPhieuNhap.ThanhTien` là cột `GENERATED ALWAYS AS (SoLuongNhap * DonGiaNhap) STORED`, tuyệt đối không insert thủ công giá trị vào cột này.

### 3.3. Về định dạng tên bảng trên MySQL (Case Sensitivity):
- Trong CSDL mẫu `quanlynhathuoc.sql`, tên bảng được viết thường (`nhanvien`, `phieunhap`, `thuoc`, `lothuoc`, `chitietphieunhap`...).
- Theo quy ước của nhóm trong backend Node.js (`nhanVienController.js`), các câu query sử dụng kiểu PascalCase (`NhanVien`, `Thuoc`, `LoThuoc`, `PhieuNhap`, `ChiTietPhieuNhap`).
- Trên hệ điều hành Windows hoặc MySQL cấu hình `lower_case_table_names = 1`, tên bảng không phân biệt hoa thường. Tuy nhiên trên Linux/macOS với cấu hình mặc định (`lower_case_table_names = 0`), MySQL có thể phân biệt hoa thường. Đề xuất nhóm trưởng thiết lập cấu hình MySQL đồng bộ hoặc lưu ý khi deploy trên Linux.

### 3.4. Về thông tin nhân viên (`MaNV`):
- `MainDashboard.java` hiện chưa truyền `MaNV` phiên đăng nhập sang các panel module. Vì vậy, trên màn hình Swing lập phiếu nhập sẽ cung cấp combobox chọn "Nhân viên lập phiếu" từ API `GET /api/nhan-vien`.

## 4. Mâu thuẫn và đề xuất

- `quanlynhathuoc.sql` khai báo tên bảng chữ thường (`nhanvien`, `phieunhap`, `thuoc`, `lothuoc`, `chitietphieunhap`, `nhacungcap`), trong khi code nhóm dùng PascalCase. Trên Linux với `lower_case_table_names=0`, tên bảng có thể phân biệt hoa thường; cần thống nhất cấu hình hoặc tên truy vấn trước khi triển khai.
- `MainDashboard` không truyền `MaNV` vào màn hình Nhập kho, nên màn hình dùng combobox nhân viên từ API.
- API nhà cung cấp trả `MaNCC`, `TenNCC`; API thuốc trả `MaThuoc`, `TenThuoc`, `GiaNhap`; API nhân viên trả `MaNV`, `HoTen`. Tên trường khớp với phần đọc JSON của màn hình Swing.
- Schema mẫu có `MaThuoc=1`, `MaNCC=1`, `MaNV=3` đang hoạt động. Giá trị tồn trong dump không khớp tổng số lượng các lô hiện có; ca nhập hợp lệ kiểm tra phần chênh lệch trước/sau trong cùng transaction.
- Các API nhân viên hiện tại không lọc trạng thái; trigger vẫn từ chối nhân viên nghỉ việc. Controller ngoài module Nhập kho không thuộc phạm vi chỉnh sửa.
- `MainDashboard` có dòng thêm `btnNhapKho` vào sidebar lặp lại; theo phạm vi bàn giao chỉ kiểm tra dòng chuyển panel, không chỉnh phần sidebar.
