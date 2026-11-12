package views;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private String tenDangNhap;
    private String hoTen;
    private String quyen;

    private JPanel panelContent;
    private CardLayout cardLayout;

    // Các nút chức năng trên Sidebar
    private JButton btnThongKe;
    private JButton btnNhapKho;
    private JButton btnBanHang;
    private JButton btnThuocKH;
    private JButton btnDangXuat;
    private JButton btnDanhMuc;
    private JButton btnNhanVien;

    public MainDashboard(String tenDangNhap, String hoTen, String quyen) {
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.quyen = quyen;

        setTitle("HỆ THỐNG QUẢN LÝ NHÀ THUỐC NỘI BỘ");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. THANH HEADER TRÊN CÙNG ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(40, 58, 90));
        panelHeader.setPreferredSize(new Dimension(1000, 50));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblBrand = new JLabel("PHẦN MỀM QUẢN LÝ NHÀ THUỐC");
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel lblUserInfo = new JLabel("Xin chào: " + hoTen + " (" + quyen + ")");
        lblUserInfo.setForeground(new Color(220, 220, 220));
        lblUserInfo.setFont(new Font("Arial", Font.ITALIC, 14));

        panelHeader.add(lblBrand, BorderLayout.WEST);
        panelHeader.add(lblUserInfo, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. THANH SIDEBAR MENU BÊN TRÁI ---
        JPanel panelSidebar = new JPanel();
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));
        panelSidebar.setBackground(new Color(240, 242, 245));
        panelSidebar.setPreferredSize(new Dimension(220, 600));
        panelSidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        btnThongKe = taoNutMenu(" Báo cáo và Thống kê");
        btnNhapKho = taoNutMenu(" Quản lý Nhập kho");
        btnBanHang = taoNutMenu(" Bán hàng");
        btnThuocKH = taoNutMenu(" Quản lý Thuốc và KH");
        btnDangXuat = taoNutMenu(" Đăng xuất");
        btnDanhMuc = taoNutMenu(" Quản lý Danh Mục ");
        btnNhanVien = taoNutMenu(" Quản lý Nhân Viên ");

        btnDangXuat.setForeground(Color.RED);

        panelSidebar.add(btnThongKe);
        panelSidebar.add(Box.createVerticalStrut(8));
        panelSidebar.add(btnDanhMuc); // Cho Admin & Thủ Kho
        panelSidebar.add(Box.createVerticalStrut(8));
        panelSidebar.add(btnNhanVien); // Cho Admin
        panelSidebar.add(Box.createVerticalStrut(8));
        panelSidebar.add(btnNhapKho);
        panelSidebar.add(btnNhapKho);
        panelSidebar.add(Box.createVerticalStrut(8));
        panelSidebar.add(btnBanHang);
        panelSidebar.add(Box.createVerticalStrut(8));
        panelSidebar.add(btnThuocKH);
        panelSidebar.add(Box.createVerticalGlue()); // Đẩy nút đăng xuất xuống đáy
        panelSidebar.add(btnDangXuat);

        add(panelSidebar, BorderLayout.WEST);

        // --- 3. VÙNG NỘI DUNG CHÍNH (CENTER) ---
        cardLayout = new CardLayout();
        panelContent = new JPanel(cardLayout);

        // Tạo các Panel giao diện đại diện cho 4 Module của 4 thành viên
        panelContent.add(new ThongKePanel(), "THONG_KE");
        panelContent.add(taoPanelModule("MAN_HINH_NHAP_KHO", "MODULE 2: QUẢN LÝ KHO & NHẬP HÀNG (TV2)"), "NHAP_KHO");
        panelContent.add(new BanHangPanel(tenDangNhap), "BAN_HANG");
        panelContent.add(taoPanelModule("MAN_HINH_THUOC_KH", "MODULE 4: DỮ LIỆU THUỐC & KHÁCH HÀNG (TV4)"), "THUOC_KH");
        panelContent.add(new DanhMucPanel(), "DANH_MUC");
        panelContent.add(new NhanVienPanel(), "NHAN_VIEN");
        add(panelContent, BorderLayout.CENTER);

        // --- 4. ÁP DỤNG PHÂN QUYỀN VÀ BẮT SỰ KIỆN NÚT BẤM ---
        apDungPhanQuyen();
        ganSuKien();
    }

    private JButton taoNutMenu(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private JPanel taoPanelModule(String id, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(title, JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private void apDungPhanQuyen() {
        // Mặc định ẩn tất cả nút bấm
        btnThongKe.setVisible(false);
        btnNhapKho.setVisible(false);
        btnBanHang.setVisible(false);
        btnThuocKH.setVisible(false);
        btnDanhMuc.setVisible(false);
        btnNhanVien.setVisible(false);
        if ("ADMIN".equalsIgnoreCase(quyen)) {
            // ADMIN thấy toàn bộ chức năng
            btnThongKe.setVisible(true);
            btnNhapKho.setVisible(true);
            btnBanHang.setVisible(true);
            btnThuocKH.setVisible(true);
            btnDanhMuc.setVisible(true);
            btnNhanVien.setVisible(true);
            cardLayout.show(panelContent, "THONG_KE");
        } else if ("THUKHO".equalsIgnoreCase(quyen)) {
            // THUKHO chỉ thấy Nhập kho và Quản lý Thuốc
            btnNhapKho.setVisible(true);
            btnThuocKH.setVisible(true);
            btnDanhMuc.setVisible(true);
            cardLayout.show(panelContent, "NHAP_KHO");
        } else if ("BANHANG".equalsIgnoreCase(quyen)) {
            // DƯỢC SĨ BÁN HÀNG chỉ thấy POS và Khách hàng
            btnBanHang.setVisible(true);
            btnThuocKH.setVisible(true);
            cardLayout.show(panelContent, "BAN_HANG");
        }
    }

    private void ganSuKien() {
        btnThongKe.addActionListener(e -> cardLayout.show(panelContent, "THONG_KE"));
        btnNhapKho.addActionListener(e -> cardLayout.show(panelContent, "NHAP_KHO"));
        btnBanHang.addActionListener(e -> cardLayout.show(panelContent, "BAN_HANG"));
        btnThuocKH.addActionListener(e -> cardLayout.show(panelContent, "THUOC_KH"));
        btnDanhMuc.addActionListener(e -> cardLayout.show(panelContent, "DANH_MUC"));
        btnNhanVien.addActionListener(e -> cardLayout.show(panelContent, "NHAN_VIEN"));
        btnDangXuat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // Đóng cửa sổ chính
                new LoginForm().setVisible(true); // Mở lại cửa sổ Đăng nhập
            }
        });
    }
}