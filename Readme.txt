*****Trước khi thực hiện dự án*****

1. Cài đặt các phần mềm nền tảng

	Node.js: Tải và cài đặt Node.js (Bản LTS) (khuyến nghị phiên bản 18 hoặc 20).

	Java Development Kit (JDK): Tải và cài đặt JDK 17 hoặc JDK 21 (ví dụ: Oracle JDK hoặc Eclipse Temurin).

	MySQL Server & Workbench: Tải và cài đặt MySQL Installer (bao gồm MySQL Server 8.0+ và MySQL Workbench). 
		Hãy ghi nhớ mật khẩu root đã cài.

2. Cài đặt các Extension bắt buộc trên VS Code

	Mở VS Code, truy cập mục Extensions (Ctrl + Shift + X) và cài đặt các tiện ích sau:

	Extension Pack for Java (của Microsoft) – Bộ công cụ biên dịch và chạy Java.

	Git Graph hoặc GitLens – Công cụ hỗ trợ theo dõi các nhánh (branch) và commit dễ dàng.

3. Nạp Cơ sở dữ liệu (MySQL Database)

	Mở MySQL Workbench và đăng nhập tài khoản root.

	Mở file quanlynhathuoc.sql (nằm trong thư mục database/ hoặc thư mục gốc của dự án vừa clone).

	Bôi đen toàn bộ nội dung file và bấm nút Execute (Hình tia sét) để tự động tạo Database quanlynhathuoc 
cùng các bảng và dữ liệu mẫu.

	Đảm bảo ở danh sách SCHEMAS bên trái xuất hiện database quanlynhathuoc

	Hình cấu trúc CSDL cũng nằm trong dự án file SoDoCSDL.jpg

4.Cấu hình và chạy Server Backend (Node.js)
	Trong VS Code, mở Terminal mới (Ctrl + ~) và di chuyển vào thư mục backend:
		cd backend
	Cài đặt các gói thư viện cần thiết:
		npm install
	Tạo file cấu hình môi trường .env:
		Đổi tên hoặc sao chép file .env.example thành .env.
		Mở file .env và cập nhật thông tin kết nối Database của máy:

			PORT=5000
			DB_HOST=localhost
			DB_USER=root
			DB_PASS=nhap_mat_khau_mysql_cua_ban_vao_day
			DB_NAME=quanlynhathuoc
		Khởi chạy Server Node.js:
			node server.js
		(Màn hình báo Server running on port 5000 và Connected to MySQL database là hoàn tất).

5. Kiểm tra và chạy Frontend Java Swing

	Mở file src/views/LoginForm.java trong VS Code.

	Bấm nút Run (hoặc phím F5) trên VS Code để khởi chạy giao diện Đăng nhập.

	Đăng nhập thử bằng các tài khoản phân quyền đã có sẵn để kiểm tra kết nối API:

		Admin: admin / admin123

		Thủ kho: thukho1 / 123456

		Bán hàng: banhang1 / 123456

6. Quy trình tạo nhánh và đẩy code cá nhân (Git Workflow)

	Tuyệt đối KHÔNG làm việc hay commit trực tiếp trên nhánh main. Mỗi thành viên tuân thủ đúng 5 bước sau:

	1.Tạo nhánh riêng theo tính năng được phân công:

		# Thành viên 2 (Nhập kho)
		git checkout -b feature/nhap-kho

		# Thành viên 3 (Bán hàng POS)
		git checkout -b feature/ban-hang

		# Thành viên 4 (Danh mục Thuốc & Khách hàng)
		git checkout -b feature/quan-ly-thuoc
	2. Lập trình và Lưu vết công việc (Commit) thường xuyên

	3.Sau khi viết/sửa xong một phần chức năng:
		git add .
		git commit -m "Mô tả ngắn gọn công việc vừa xong (VD: Hoàn thành API tạo phiếu nhập)"
	4. Đẩy nhánh lên GitHub khi hoàn thành tính năng:
		git push origin <ten-nhanh-cua-ban>
	5.Tạo Pull Request (PR):
		Truy cập giao diện GitHub của dự án.
		Bấm nút Compare & pull request để gửi yêu cầu gộp code vào nhánh main.
		Nhắn cho Nhóm trưởng review và duyệt (Merge) vào dự án chung.
	6. Cập nhật code mới nhất từ Nhóm trưởng trước khi làm tính năng mới:
		git checkout main
		git pull origin main
7. Các lưu ý chung:

	Các bạn làm phần nào thì sửa lại Controller và thêm vào giao diện Java Fontend của phần đó

	Nếu muốn thêm Controller hoặc giao diện khác, cứ việc thêm thoải mái, nhắn tui trước là được. Nhưng lưu ý phần 
thêm vào không được trùng nội dung của các bạn khác đang làm

	Nếu các bạn muốn thay đổi cấu trúc bảng cơ sở dữ liệu, có thể truy cập vào link draw.io(ở cuối file) và sửa vào
 


=====================================================================================================================

1. Thành viên 2 (Module: Kho & Nhập hàng) - Ngữ 

	***Nhiệm vụ Backend (controllers/nhapKhoController.js):

	POST /api/nhap-kho/phieu-nhap: Tiếp nhận thông tin phiếu nhập->Thêm dòng vào PhieuNhap ->Thêm dòng vào LoThuoc
(ghi nhận SoLo, HanSuDung, SoLuongTon, GiaNhap)-> Thêm chi tiết vào ChiTietPhieuNhap->Tự động cộng số lượng tồn 
vào bảng Thuoc.

	GET /api/nhap-kho/phieu-nhap: Lấy danh sách phiếu nhập và chi tiết phiếu nhập.

	***Nhiệm vụ Frontend 

(views/NhapKhoPanel.java):Giao diện tạo phiếu nhập (chọn NhaCungCap, chọn Thuoc, nhập SoLo, HanSuDung, SoLuong, GiaNhap) và hiển thị bảng 
lịch sử phiếu nhập.

(views/LoThuocPanel.java):Giao diện xem các lô thuốc, xác định các lô thuốc còn hạn, các lô thuốc hết hạn

=====================================================================================================================

2. Thành viên 3 (Module: POS Bán hàng & Hóa đơn) - An Bình

	***Nhiệm vụ Backend (controllers/banHangController.js):

	POST /api/ban-hang/hoa-don: Tạo hóa đơn mới. Thực hiện quy trình FEFO: Truy vấn LoThuoc có HanSuDung >= CURDATE() 
và SoLuongTon > 0 của thuốc được chọn (sắp xếp HanSuDung ASC), tự động trừ SoLuongTon của lô gần hết hạn nhất trước
 
-> Thêm vào ChiTietHoaDon (lưu MaLo và GiaBan) -> Trừ tổng số lượng tồn ở bảng Thuoc.

	GET /api/ban-hang/hoa-don: Lấy lịch sử hóa đơn bán hàng.

	***Nhiệm vụ Frontend 

	(views/BanHangPanel.java): Giao diện POS bán hàng (Tìm kiếm thuốc, thêm vào giỏ hàng, tính tổng tiền, 
bấm thanh toán và in/xem hóa đơn ra file excel).

=====================================================================================================================

3. Thành viên 4 (Module: Quản lý Thuốc & Khách hàng) - Bảo

	***Nhiệm vụ Backend (controllers/thuocController.js):

	GET /api/thuoc: Lấy danh sách toàn bộ thuốc kèm số lượng tồn tổng và thông tin hãng sản xuất.

	POST /api/thuoc, PUT /api/thuoc/:id, DELETE /api/thuoc/:id: Thêm, sửa, xóa danh mục thuốc.

	GET /api/loaithuoc: Lấy thông tin loại thuốc

	POST /api/loaithuoc, PUT /api/thuoc/:id, DELETE /api/thuoc/:id: Thêm, sửa, xóa danh mục loại thuốc.

	GET /api/khach-hang, POST /api/khach-hang: Quản lý danh sách khách hàng thân thiết.

	***Nhiệm vụ Frontend 

	(views/ThuocPanel.java):Giao diện quản lý thông tin thuốc, loại thuốc
	
	(views/KhachHangPanel.java):Giao diện quản lý thông tin khách hàng

	(Có các nút Thêm, Sửa, Xóa, Tìm kiếm).


Link gg Drive edit CSDL trên draw.io: https://drive.google.com/file/d/12GLMNNKOWXrL2QwOwVLcUF5YLSUuQG8D/view?usp=sharing