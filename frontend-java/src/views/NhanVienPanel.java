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
        pnlForm.add(new JLabel("Họ và Tên (*):"));
        pnlForm.add(txtHoTen);
        pnlForm.add(new JLabel("Chức Vụ:"));
        pnlForm.add(cbChucVu);
        pnlForm.add(new JLabel("Số Điện Thoại:"));
        pnlForm.add(txtSDT);

        pnlForm.add(new JLabel("Tên Đăng Nhập (*):"));
        pnlForm.add(txtTenDangNhap);
        pnlForm.add(new JLabel("Mật Khẩu (*):"));
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
                new String[] { "Mã NV", "Họ Tên", "Chức Vụ", "Số Điện Thoại", "Tài Khoản", "Quyền" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNhanVien = new JTable(tableModel);

        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            int row = tblNhanVien.getSelectedRow();
            if (row >= 0) {
                txtMaNV.setText(getCellStr(row, 0));
                txtHoTen.setText(getCellStr(row, 1));
                cbChucVu.setSelectedItem(getCellStr(row, 2));
                txtSDT.setText(getCellStr(row, 3));
                txtTenDangNhap.setText(getCellStr(row, 4));
                String quyen = getCellStr(row, 5);
                if (!quyen.isEmpty()) {
                    cbQuyen.setSelectedItem(quyen);
                }
                txtTenDangNhap.setEditable(false);
                txtMatKhau.setEditable(false);
            }
        });

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(tblNhanVien), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());
        btnLamMoi.addActionListener(e -> {
            xoaForm();
            taiDuLieuNhanVien();
        });

        taiDuLieuNhanVien();
    }

    private String getCellStr(int row, int col) {
        Object val = tableModel.getValueAt(row, col);
        return val == null ? "" : val.toString();
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
                        tableModel.addRow(new Object[] {
                                id,
                                ten,
                                cv,
                                "null".equals(sdt) ? "" : sdt,
                                "null".equals(tk) ? "" : tk,
                                "null".equals(q) ? "" : q
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themNhanVien() {
        try {
            String hoTen = esc(txtHoTen.getText().trim());
            String chucVu = esc(String.valueOf(cbChucVu.getSelectedItem()));
            String sdt = esc(txtSDT.getText().trim());
            String tenDN = esc(txtTenDangNhap.getText().trim());
            String matKhau = esc(new String(txtMatKhau.getPassword()).trim());
            String quyen = esc(String.valueOf(cbQuyen.getSelectedItem()));

            String payload = String.format(
                    "{\"hoTen\":\"%s\",\"chucVu\":\"%s\",\"soDienThoai\":\"%s\",\"tenDangNhap\":\"%s\",\"matKhau\":\"%s\",\"quyen\":\"%s\"}",
                    hoTen, chucVu, sdt, tenDN, matKhau, quyen);
            String res = ApiClient.sendPost("/nhan-vien", payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm nhân viên thành công!" : msg);
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm nhân viên thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                    esc(txtHoTen.getText().trim()),
                    esc(String.valueOf(cbChucVu.getSelectedItem())),
                    esc(txtSDT.getText().trim()),
                    esc(String.valueOf(cbQuyen.getSelectedItem())));
            String res = ApiClient.sendPut("/nhan-vien/" + id, payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật nhân viên thành công!" : msg);
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa nhân viên thành công!" : msg);
                taiDuLieuNhanVien();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractVal(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "";
            start += pattern.length();
            while (start < json.length() && json.charAt(start) == ' ') {
                start++;
            }
            if (start >= json.length())
                return "";

            if (json.charAt(start) == '"') {
                start++;
                int end = start;
                while (end < json.length() && json.charAt(end) != '"') {
                    if (json.charAt(end) == '\\' && end + 1 < json.length()) {
                        end += 2;
                    } else {
                        end++;
                    }
                }
                return json.substring(start, end).trim();
            } else {
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}'
                        && json.charAt(end) != ']') {
                    end++;
                }
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }
}