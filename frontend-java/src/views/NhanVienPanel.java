package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class NhanVienPanel extends JPanel {
    private JTextField txtMaNV, txtHoTen, txtSDT, txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cbChucVu, cbQuyen;
    private JTable tblNhanVien;
    private DefaultTableModel tableModel;

    public NhanVienPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(4, 4, 10, 10));

        txtMaNV = new JTextField();
        txtMaNV.setEditable(false);
        txtHoTen = new JTextField();
        txtSDT = new JTextField();
        cbChucVu = new JComboBox<>(new String[] { "DuocSi", "ThuKho", "QuanLy" });

        txtTenDangNhap = new JTextField();
        txtMatKhau = new JPasswordField();
        cbQuyen = new JComboBox<>(new String[] { "BANHANG", "THUKHO", "ADMIN" });

        pnlForm.add(new JLabel("Mã NV:"));
        pnlForm.add(txtMaNV);
        pnlForm.add(new JLabel("Họ và Tên:"));
        pnlForm.add(txtHoTen);
        pnlForm.add(new JLabel("Chức Vụ:"));
        pnlForm.add(cbChucVu);
        pnlForm.add(new JLabel("Số Điện Thoại:"));
        pnlForm.add(txtSDT);

        pnlForm.add(new JLabel("Tên Đăng Nhập:"));
        pnlForm.add(txtTenDangNhap);
        pnlForm.add(new JLabel("Mật Khẩu:"));
        pnlForm.add(txtMatKhau);
        pnlForm.add(new JLabel("Quyền Hệ Thống:"));
        pnlForm.add(cbQuyen);
        pnlForm.add(new JLabel(""));
        pnlForm.add(new JLabel(""));

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm NV");
        JButton btnSua = new JButton("Sửa NV");
        JButton btnXoa = new JButton("Xóa NV");
        JButton btnLamMoi = new JButton("Làm Mới");

        pnlBtns.add(btnThem);
        pnlBtns.add(btnSua);
        pnlBtns.add(btnXoa);
        pnlBtns.add(btnLamMoi);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlForm, BorderLayout.CENTER);
        pnlTop.add(pnlBtns, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new String[] { "Mã NV", "Họ Tên", "Chức Vụ", "Số Điện Thoại", "Tài Khoản", "Quyền" }, 0);
        tblNhanVien = new JTable(tableModel);

        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            int row = tblNhanVien.getSelectedRow();
            if (row >= 0) {
                txtMaNV.setText(tableModel.getValueAt(row, 0).toString());
                txtHoTen.setText(tableModel.getValueAt(row, 1).toString());
                cbChucVu.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                txtSDT.setText(tableModel.getValueAt(row, 3).toString());
                txtTenDangNhap.setText(tableModel.getValueAt(row, 4).toString());
                cbQuyen.setSelectedItem(tableModel.getValueAt(row, 5).toString());
                txtTenDangNhap.setEditable(false);
                txtMatKhau.setEditable(false);
            }
        });

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(tblNhanVien), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());
        btnLamMoi.addActionListener(e -> xoaForm());

        taiDuLieuNhanVien();
    }

    private void taiDuLieuNhanVien() {
        try {
            String res = ApiClient.sendGet("/nhan-vien");
            tableModel.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaNV");
                    String ten = extractVal(item, "HoTen");
                    String cv = extractVal(item, "ChucVu");
                    String sdt = extractVal(item, "SoDienThoai");
                    String tk = extractVal(item, "TenDangNhap");
                    String q = extractVal(item, "Quyen");

                    if (!id.isEmpty()) {
                        tableModel.addRow(new Object[] { id, ten, cv, sdt, tk, q });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themNhanVien() {
        String tenDN = txtTenDangNhap.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword()).trim();

        if (hoTen.isEmpty() || tenDN.isEmpty() || matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ Họ tên, Tên đăng nhập và Mật khẩu!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- KIỂM TRA TRÙNG TÊN TÀI KHOẢN TRÊN FRONTEND ---
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 4); // Cột 4 chứa Tên Đăng Nhập
            if (val != null && val.toString().equalsIgnoreCase(tenDN)) {
                JOptionPane.showMessageDialog(this, "Đã có nhân viên dùng tài khoản này!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            String payload = String.format(
                    "{\"hoTen\":\"%s\",\"chucVu\":\"%s\",\"soDienThoai\":\"%s\",\"tenDangNhap\":\"%s\",\"matKhau\":\"%s\",\"quyen\":\"%s\"}",
                    hoTen, cbChucVu.getSelectedItem(), txtSDT.getText().trim(),
                    tenDN, matKhau, cbQuyen.getSelectedItem());
            String res = ApiClient.sendPost("/nhan-vien", payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void suaNhanVien() {
        String id = txtMaNV.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên trong bảng để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = String.format(
                    "{\"hoTen\":\"%s\",\"chucVu\":\"%s\",\"soDienThoai\":\"%s\",\"quyen\":\"%s\"}",
                    txtHoTen.getText().trim(), cbChucVu.getSelectedItem(), txtSDT.getText().trim(),
                    cbQuyen.getSelectedItem());
            String res = ApiClient.sendPut("/nhan-vien/" + id, payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void xoaNhanVien() {
        String id = txtMaNV.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên trong bảng để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            String res = ApiClient.sendDelete("/nhan-vien/" + id);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên này do có lịch sử nhập/bán hàng!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void xoaForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        txtTenDangNhap.setText("");
        txtMatKhau.setText("");
        txtTenDangNhap.setEditable(true);
        txtMatKhau.setEditable(true);
        tblNhanVien.clearSelection();
    }

    private String extractVal(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "";
            start += pattern.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
                start++;
            }
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}'
                    && json.charAt(end) != ']') {
                end++;
            }
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "";
        }
    }
}