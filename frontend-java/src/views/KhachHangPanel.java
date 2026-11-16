package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class KhachHangPanel extends JPanel {
    private JTextField txtMaKH, txtTenKH, txtSDT, txtDiaChi, txtDiem, txtTimKiem;
    private JTable tblKhachHang;
    private DefaultTableModel modelKH;
    private TableRowSorter<DefaultTableModel> sorter;

    public KhachHangPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Form nhập liệu
        JPanel pnlForm = new JPanel(new GridLayout(3, 4, 10, 10));
        txtMaKH = new JTextField();
        txtMaKH.setEditable(false);
        txtTenKH = new JTextField();
        txtSDT = new JTextField();
        txtDiaChi = new JTextField();
        txtDiem = new JTextField("0");

        pnlForm.add(new JLabel("Mã Khách Hàng:"));
        pnlForm.add(txtMaKH);
        pnlForm.add(new JLabel("Tên Khách Hàng (*):"));
        pnlForm.add(txtTenKH);
        pnlForm.add(new JLabel("Số Điện Thoại:"));
        pnlForm.add(txtSDT);
        pnlForm.add(new JLabel("Điểm Tích Lũy:"));
        pnlForm.add(txtDiem);
        pnlForm.add(new JLabel("Địa Chỉ:"));
        pnlForm.add(txtDiaChi);
        pnlForm.add(new JLabel(""));
        pnlForm.add(new JLabel(""));

        // 2. Thanh Tìm kiếm & Nút chức năng
        JPanel pnlAction = new JPanel(new BorderLayout());
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtTimKiem = new JTextField(20);
        JButton btnTim = new JButton("Tìm kiếm");
        pnlSearch.add(new JLabel("Tìm tên/SĐT:"));
        pnlSearch.add(txtTimKiem);
        pnlSearch.add(btnTim);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");
        pnlBtns.add(btnThem);
        pnlBtns.add(btnSua);
        pnlBtns.add(btnXoa);
        pnlBtns.add(btnLamMoi);

        pnlAction.add(pnlSearch, BorderLayout.WEST);
        pnlAction.add(pnlBtns, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(5, 5));
        pnlTop.add(pnlForm, BorderLayout.CENTER);
        pnlTop.add(pnlAction, BorderLayout.SOUTH);

        // 3. Bảng dữ liệu
        modelKH = new DefaultTableModel(new String[]{"Mã KH", "Tên Khách Hàng", "Số Điện Thoại", "Địa Chỉ", "Điểm Tích Lũy"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKhachHang = new JTable(modelKH);
        sorter = new TableRowSorter<>(modelKH);
        tblKhachHang.setRowSorter(sorter);

        tblKhachHang.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = tblKhachHang.getSelectedRow();
            if (viewRow >= 0) {
                int row = tblKhachHang.convertRowIndexToModel(viewRow);
                txtMaKH.setText(modelKH.getValueAt(row, 0).toString());
                txtTenKH.setText(modelKH.getValueAt(row, 1).toString());
                txtSDT.setText(modelKH.getValueAt(row, 2).toString());
                txtDiaChi.setText(modelKH.getValueAt(row, 3).toString());
                txtDiem.setText(modelKH.getValueAt(row, 4).toString());
            }
        });

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(tblKhachHang), BorderLayout.CENTER);

        // 4. Gán sự kiện
        btnThem.addActionListener(e -> themKH());
        btnSua.addActionListener(e -> suaKH());
        btnXoa.addActionListener(e -> xoaKH());
        btnLamMoi.addActionListener(e -> { xoaForm(); taiDuLieuKH(); });
        btnTim.addActionListener(e -> timKiemKH());

        taiDuLieuKH();
    }

    private void taiDuLieuKH() {
        try {
            String res = ApiClient.sendGet("/khach-hang");
            modelKH.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaKH");
                    String ten = extractVal(item, "TenKH");
                    String sdt = extractVal(item, "SoDienThoai");
                    String dc = extractVal(item, "DiaChi");
                    String diem = extractVal(item, "DiemTichLuy");
                    if (!id.isEmpty()) {
                        modelKH.addRow(new Object[]{id, ten, sdt.equals("null") ? "" : sdt, dc.equals("null") ? "" : dc, diem});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themKH() {
        if (txtTenKH.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int diem = Integer.parseInt(txtDiem.getText().trim().isEmpty() ? "0" : txtDiem.getText().trim());
            String payload = String.format(
                "{\"TenKH\":\"%s\",\"SoDienThoai\":\"%s\",\"DiaChi\":\"%s\",\"DiemTichLuy\":%d}",
                txtTenKH.getText().trim(), txtSDT.getText().trim(), txtDiaChi.getText().trim(), diem
            );
            String res = ApiClient.sendPost("/khach-hang", payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                taiDuLieuKH();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số nguyên!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void suaKH() {
        String id = txtMaKH.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int diem = Integer.parseInt(txtDiem.getText().trim().isEmpty() ? "0" : txtDiem.getText().trim());
            String payload = String.format(
                "{\"TenKH\":\"%s\",\"SoDienThoai\":\"%s\",\"DiaChi\":\"%s\",\"DiemTichLuy\":%d}",
                txtTenKH.getText().trim(), txtSDT.getText().trim(), txtDiaChi.getText().trim(), diem
            );
            String res = ApiClient.sendPut("/khach-hang/" + id, payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                taiDuLieuKH();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật dữ liệu!");
        }
    }

    private void xoaKH() {
        String id = txtMaKH.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            String res = ApiClient.sendDelete("/khach-hang/" + id);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                taiDuLieuKH();
                xoaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa khách hàng đang có hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void timKiemKH() {
        String keyword = txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1, 2)); // Tìm theo cột Tên (1) hoặc SĐT (2)
        }
    }

    private void xoaForm() {
        txtMaKH.setText("");
        txtTenKH.setText("");
        txtSDT.setText("");
        txtDiaChi.setText("");
        txtDiem.setText("0");
        txtTimKiem.setText("");
        sorter.setRowFilter(null);
        tblKhachHang.clearSelection();
    }

    private String extractVal(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') end++;
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "";
        }
    }
}