***TÓM TẮT SỬA DỰ ÁN****

Thịnh: 
	Sửa lại controller và fronted tại các mục nhanvien, danhmuc. Đưa tất cả về Backend và tạo các logic hàm trong file SQL
	
	Bổ sung thêm các phương pháp thống kê tại phần thống kê

An Bình: 
	Không sửa gì hết, hệ thống chạy ổn

Ngữ:
	Bổ sung thêm phần nhập vào lịch tại NSX và HSD cho tiện lợi hơn 
Bảo:
	Bổ sung thêm logic phần kiểm tra tính hợp lệ tại thuoc_logic.sql

	Sửa lại đường dẫn api tại router và frontend

	Cập nhật lại thuoc.controller cho việc cập nhật thuốc và thêm thuốc(Xóa cột Gianhap, Ton_kho không được nhập 
	trực tiếp từ bàn phím mà chỉ có thể thông qua nhập kho và bán hàng (Default 0)

****LƯU Ý KHI CHẠY DỰ ÁN FULL****

	Trường hợp 1: Ae muốn giữ lại dữ liệu test dự án đã có sẵn trên máy mình:

	Vào backend/sql. Sau đó chạy lần lượt các lệnh sau:


		ALTER TABLE Thuoc DROP COLUMN GiaNhap;
		ALTER TABLE Thuoc MODIFY COLUMN SoLuongTon INT NOT NULL DEFAULT 0;

		Sau đó chạy lần lượt các file danhmuc_logic, nhanvien_logic, thuoc_logic, nhapkho_triggers

		Trường hợp này có thể có lỗi xung đột cấu trúc CSDL, nếu muốn tránh thì hơi lubu và nhờ trời độ

	Trường hợp 2: Ko cần giữ lại dự án, chỉ cần chạy được hệ thống và test

	Vào backend/sql. Sau đó chỉ cần chạy quanlynhathuoc_full

Sau đó, cứ việc chạy  các lệnh sau: 

	node server.js tại backend
	 
	javac -encoding UTF-8 -d bin src\utils\*.java src\views\*.java && java -cp bin views.LoginForm tại frontend-java

	Hoặc nhấn vào nút Run tại phần public static void main của file LoginForm.java

Đăng nhập thử bằng các tài khoản phân quyền đã có sẵn để kiểm tra kết nối API:

		Admin: admin / admin123

		Thủ kho: thukho1 / 123456

		Bán hàng: banhang1 / 123456

****** NẾU MUỐN SỬA DỰ ÁN *******

Quy trình tạo nhánh và đẩy code cá nhân (Git Workflow)

	Tuyệt đối KHÔNG làm việc hay commit trực tiếp trên nhánh main. Mỗi thành viên tuân thủ đúng 5 bước sau:

	1.Tạo nhánh riêng theo tên của bạn :
		git checkout -b feature/update_by_Thinh
	2. Lập trình và Lưu vết công việc (Commit) thường xuyên

	3.Sau khi viết/sửa xong một phần chức năng:
		git add .
		git commit -m "Mô tả ngắn gọn update đề xuất"
	4. Đẩy nhánh lên GitHub khi hoàn thành tính năng:
		git push origin <ten-nhanh-cua-ban>
	5.Tạo Pull Request (PR):
		Truy cập giao diện GitHub của dự án.
		Bấm nút Compare & pull request để gửi yêu cầu gộp code vào nhánh main.
		Nhắn cho Nhóm trưởng review và duyệt (Merge) vào dự án chung.
	6. Cập nhật code mới nhất từ Nhóm trưởng trước khi làm tính năng mới:
		git checkout main
		git pull origin main


Link gg Drive edit CSDL trên draw.io: https://drive.google.com/file/d/12GLMNNKOWXrL2QwOwVLcUF5YLSUuQG8D/view?usp=sharing