package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DanhMucPanel extends JPanel {
    private JTextField txtMaNCC, txtTenNCC, txtSDTNCC;
    private JTable tblNCC;
    private DefaultTableModel modelNCC;

    private JTextField txtMaHang, txtTenHang, txtQuocGia;
    private JTable tblHang;
    private DefaultTableModel modelHang;

    public DanhMucPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab(" Quản lý Nhà Cung Cấp ", taoTabNhaCungCap());
        tabbedPane.addTab(" Quản lý Hãng Sản Xuất ", taoTabHangSX());

        add(tabbedPane, BorderLayout.CENTER);

        taiDuLieuNCC();
        taiDuLieuHang();
    }

    // ==========================================
    // TAB 1: NHÀ CUNG CẤP
    // ==========================================
    private JPanel taoTabNhaCungCap() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(3, 2, 10, 10));
        txtMaNCC = new JTextField();
        txtMaNCC.setEditable(false);
        txtTenNCC = new JTextField();
        txtSDTNCC = new JTextField();

        pnlForm.add(new JLabel("Mã NCC:"));
        pnlForm.add(txtMaNCC);
        pnlForm.add(new JLabel("Tên Nhà Cung Cấp (*):"));
        pnlForm.add(txtTenNCC);
        pnlForm.add(new JLabel("Số Điện Thoại:"));
        pnlForm.add(txtSDTNCC);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");

        pnlBtns.add(btnThem);
        pnlBtns.add(btnSua);
        pnlBtns.add(btnXoa);
        pnlBtns.add(btnLamMoi);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlForm, BorderLayout.CENTER);
        pnlTop.add(pnlBtns, BorderLayout.SOUTH);

        modelNCC = new DefaultTableModel(new String[] { "Mã NCC", "Tên Nhà Cung Cấp", "Số Điện Thoại" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNCC = new JTable(modelNCC);
        tblNCC.getSelectionModel().addListSelectionListener(e -> {
            int row = tblNCC.getSelectedRow();
            if (row >= 0) {
                txtMaNCC.setText(modelNCC.getValueAt(row, 0).toString());
                txtTenNCC.setText(modelNCC.getValueAt(row, 1).toString());
                Object sdt = modelNCC.getValueAt(row, 2);
                txtSDTNCC.setText(sdt == null ? "" : sdt.toString());
            }
        });

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblNCC), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themNCC());
        btnSua.addActionListener(e -> suaNCC());
        btnXoa.addActionListener(e -> xoaNCC());
        btnLamMoi.addActionListener(e -> {
            xoaFormNCC();
            taiDuLieuNCC();
        });

        return panel;
    }

    private void taiDuLieuNCC() {
        try {
            String res = ApiClient.sendGet("/danh-muc/nha-cung-cap");
            modelNCC.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaNCC");
                    String ten = extractVal(item, "TenNCC");
                    String sdt = extractVal(item, "SoDienThoai");
                    if (!id.isEmpty()) {
                        modelNCC.addRow(new Object[] { id, ten, "null".equals(sdt) ? "" : sdt });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themNCC() {
        try {
            String payload = String.format("{\"tenNCC\":\"%s\",\"soDienThoai\":\"%s\"}",
                    esc(txtTenNCC.getText().trim()),
                    esc(txtSDTNCC.getText().trim()));
            String res = ApiClient.sendPost("/danh-muc/nha-cung-cap", payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm thành công!" : msg);
                taiDuLieuNCC();
                xoaFormNCC();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaNCC() {
        String id = txtMaNCC.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp trong bảng để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = String.format("{\"tenNCC\":\"%s\",\"soDienThoai\":\"%s\"}",
                    esc(txtTenNCC.getText().trim()),
                    esc(txtSDTNCC.getText().trim()));
            String res = ApiClient.sendPut("/danh-muc/nha-cung-cap/" + id, payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật thành công!" : msg);
                taiDuLieuNCC();
                xoaFormNCC();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaNCC() {
        String id = txtMaNCC.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp trong bảng để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhà cung cấp này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            String res = ApiClient.sendDelete("/danh-muc/nha-cung-cap/" + id);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa thành công!" : msg);
                taiDuLieuNCC();
                xoaFormNCC();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaFormNCC() {
        txtMaNCC.setText("");
        txtTenNCC.setText("");
        txtSDTNCC.setText("");
        tblNCC.clearSelection();
    }

    // ==========================================
    // TAB 2: HÃNG SẢN XUẤT
    // ==========================================
    private JPanel taoTabHangSX() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(3, 2, 10, 10));
        txtMaHang = new JTextField();
        txtMaHang.setEditable(false);
        txtTenHang = new JTextField();
        txtQuocGia = new JTextField();

        pnlForm.add(new JLabel("Mã Hãng:"));
        pnlForm.add(txtMaHang);
        pnlForm.add(new JLabel("Tên Hãng Sản Xuất (*):"));
        pnlForm.add(txtTenHang);
        pnlForm.add(new JLabel("Quốc Gia:"));
        pnlForm.add(txtQuocGia);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnLamMoi = new JButton("Làm mới");

        pnlBtns.add(btnThem);
        pnlBtns.add(btnSua);
        pnlBtns.add(btnXoa);
        pnlBtns.add(btnLamMoi);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlForm, BorderLayout.CENTER);
        pnlTop.add(pnlBtns, BorderLayout.SOUTH);

        modelHang = new DefaultTableModel(new String[] { "Mã Hãng", "Tên Hãng", "Quốc Gia" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHang = new JTable(modelHang);
        tblHang.getSelectionModel().addListSelectionListener(e -> {
            int row = tblHang.getSelectedRow();
            if (row >= 0) {
                txtMaHang.setText(modelHang.getValueAt(row, 0).toString());
                txtTenHang.setText(modelHang.getValueAt(row, 1).toString());
                Object qg = modelHang.getValueAt(row, 2);
                txtQuocGia.setText(qg == null ? "" : qg.toString());
            }
        });

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblHang), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themHang());
        btnSua.addActionListener(e -> suaHang());
        btnXoa.addActionListener(e -> xoaHang());
        btnLamMoi.addActionListener(e -> {
            xoaFormHang();
            taiDuLieuHang();
        });

        return panel;
    }

    private void taiDuLieuHang() {
        try {
            String res = ApiClient.sendGet("/danh-muc/hang-sx");
            modelHang.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaHangSX");
                    String ten = extractVal(item, "TenHang");
                    String qg = extractVal(item, "QuocGia");
                    if (!id.isEmpty()) {
                        modelHang.addRow(new Object[] { id, ten, "null".equals(qg) ? "" : qg });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themHang() {
        try {
            String payload = String.format("{\"tenHang\":\"%s\",\"quocGia\":\"%s\"}",
                    esc(txtTenHang.getText().trim()),
                    esc(txtQuocGia.getText().trim()));
            String res = ApiClient.sendPost("/danh-muc/hang-sx", payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm thành công!" : msg);
                taiDuLieuHang();
                xoaFormHang();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Thêm thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaHang() {
        String id = txtMaHang.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hãng sản xuất để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = String.format("{\"tenHang\":\"%s\",\"quocGia\":\"%s\"}",
                    esc(txtTenHang.getText().trim()),
                    esc(txtQuocGia.getText().trim()));
            String res = ApiClient.sendPut("/danh-muc/hang-sx/" + id, payload);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật thành công!" : msg);
                taiDuLieuHang();
                xoaFormHang();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Cập nhật thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaHang() {
        String id = txtMaHang.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hãng sản xuất để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hãng sản xuất này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            String res = ApiClient.sendDelete("/danh-muc/hang-sx/" + id);
            String msg = extractVal(res, "message");
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa thành công!" : msg);
                taiDuLieuHang();
                xoaFormHang();
            } else {
                JOptionPane.showMessageDialog(this, msg.isEmpty() ? "Xóa thất bại!" : msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaFormHang() {
        txtMaHang.setText("");
        txtTenHang.setText("");
        txtQuocGia.setText("");
        tblHang.clearSelection();
    }

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Hàm tách JSON: Xử lý đúng cho cả Số và Chuỗi
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