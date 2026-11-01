package views;

import utils.ApiClient;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;

    public LoginForm() {
        // Cấu hình cửa sổ chính
        setTitle("HỆ THỐNG QUẢN LÝ NHÀ THUỐC - ĐĂNG NHẬP");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề Form
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // Tên đăng nhập
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Tên đăng nhập:"), gbc);

        txtTenDangNhap = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtTenDangNhap, gbc);

        // Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Mật khẩu:"), gbc);

        txtMatKhau = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(txtMatKhau, gbc);

        // Nút Đăng nhập
        btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.setBackground(new Color(0, 123, 255));
        btnDangNhap.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(btnDangNhap, gbc);

        // Bắt sự kiện click nút Đăng nhập
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
    }

    private void xuLyDangNhap() {
        String user = txtTenDangNhap.getText().trim();
        String pass = new String(txtMatKhau.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Chuẩn bị chuỗi JSON gửi lên Backend API
            String jsonPayload = String.format("{\"tenDangNhap\": \"%s\", \"matKhau\": \"%s\"}", user, pass);

            // Gọi API /api/auth/login
            String response = ApiClient.sendPost("/auth/login", jsonPayload);

            // Kiểm tra kết quả trả về từ Backend
            if (response.contains("\"success\":true")) {
                // Trích xuất thông tin Họ tên và Quyền từ JSON trả về
                String quyen = extractJsonValue(response, "Quyen");
                String hoTen = extractJsonValue(response, "HoTen");

                if (quyen.isEmpty())
                    quyen = "BANHANG";
                if (hoTen.isEmpty())
                    hoTen = user;

                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);

                // Mở màn hình MainDashboard và truyền thông tin phân quyền
                MainDashboard dashboard = new MainDashboard(user, hoTen, quyen);
                dashboard.setVisible(true);

                // Đóng form đăng nhập
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Tài khoản hoặc mật khẩu không đúng!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến Server Backend! Kiểm tra xem Node.js đã chạy chưa.\nLỗi: " + ex.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hàm phụ trợ trích xuất giá trị từ chuỗi JSON đơn giản
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "";
            start += pattern.length();
            int end = json.indexOf("\"", start);
            if (end == -1)
                return "";
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}