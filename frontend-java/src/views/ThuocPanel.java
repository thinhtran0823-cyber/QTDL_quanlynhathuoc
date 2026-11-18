package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ThuocPanel extends JPanel {
    // Tab 1: Thuốc
    private JTextField txtMaThuoc, txtTenThuoc, txtDVTThuoc, txtGiaNhap, txtGiaBan, txtSoLuongTon, txtTimThuoc;
    private JComboBox<HangItem> cboHangSX;
    private JTable tblThuoc;
    private DefaultTableModel modelThuoc;
    private TableRowSorter<DefaultTableModel> sorterThuoc;

    // Tab 2: Loại thuốc
    private JTextField txtMaLoai, txtTenLoai, txtDVTLoai, txtTimLoai;
    private JTable tblLoai;
    private DefaultTableModel modelLoai;
    private TableRowSorter<DefaultTableModel> sorterLoai;

    public ThuocPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab(" Quản lý Danh mục Thuốc ", taoTabThuoc());
        tabbedPane.addTab(" Quản lý Loại Thuốc ", taoTabLoaiThuoc());

        add(tabbedPane, BorderLayout.CENTER);

        taiDanhSachHangSX();
        taiDuLieuThuoc();
        taiDuLieuLoaiThuoc();
    }

    // Class phụ lưu Mã & Tên Hãng SX cho JComboBox
    private static class HangItem {
        String id, ten;

        HangItem(String id, String ten) {
            this.id = id;
            this.ten = ten;
        }

        @Override
        public String toString() {
            return id + " - " + ten;
        }
    }

    // ==========================================
    // TAB 1: QUẢN LÝ THUỐC
    // ==========================================
    private JPanel taoTabThuoc() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(4, 4, 10, 8));
        txtMaThuoc = new JTextField();
        txtMaThuoc.setEditable(false);
        txtTenThuoc = new JTextField();
        txtDVTThuoc = new JTextField();
        txtGiaNhap = new JTextField("0");
        txtGiaBan = new JTextField("0");
        txtSoLuongTon = new JTextField("0");
        cboHangSX = new JComboBox<>();

        pnlForm.add(new JLabel("Mã Thuốc:"));
        pnlForm.add(txtMaThuoc);
        pnlForm.add(new JLabel("Tên Thuốc (*):"));
        pnlForm.add(txtTenThuoc);
        pnlForm.add(new JLabel("Đơn Vị Tính (*):"));
        pnlForm.add(txtDVTThuoc);
        pnlForm.add(new JLabel("Hãng Sản Xuất:"));
        pnlForm.add(cboHangSX);
        pnlForm.add(new JLabel("Giá Nhập:"));
        pnlForm.add(txtGiaNhap);
        pnlForm.add(new JLabel("Giá Bán:"));
        pnlForm.add(txtGiaBan);
        pnlForm.add(new JLabel("Số Lượng Tồn:"));
        pnlForm.add(txtSoLuongTon);
        pnlForm.add(new JLabel(""));
        pnlForm.add(new JLabel(""));

        JPanel pnlAction = new JPanel(new BorderLayout());
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtTimThuoc = new JTextField(18);
        JButton btnTim = new JButton("Tìm kiếm");
        pnlSearch.add(new JLabel("Tìm tên thuốc/hãng:"));
        pnlSearch.add(txtTimThuoc);
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

        modelThuoc = new DefaultTableModel(new String[] { "Mã", "Tên Thuốc", "ĐVT", "Giá Nhập", "Giá Bán", "Tồn Tổng",
                "Mã Hãng", "Hãng Sản Xuất" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblThuoc = new JTable(modelThuoc);
        sorterThuoc = new TableRowSorter<>(modelThuoc);
        tblThuoc.setRowSorter(sorterThuoc);

        tblThuoc.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = tblThuoc.getSelectedRow();
            if (viewRow >= 0) {
                int row = tblThuoc.convertRowIndexToModel(viewRow);
                txtMaThuoc.setText(modelThuoc.getValueAt(row, 0).toString());
                txtTenThuoc.setText(modelThuoc.getValueAt(row, 1).toString());
                txtDVTThuoc.setText(modelThuoc.getValueAt(row, 2).toString());
                txtGiaNhap.setText(modelThuoc.getValueAt(row, 3).toString());
                txtGiaBan.setText(modelThuoc.getValueAt(row, 4).toString());
                txtSoLuongTon.setText(modelThuoc.getValueAt(row, 5).toString());

                String maHang = modelThuoc.getValueAt(row, 6).toString();
                for (int i = 0; i < cboHangSX.getItemCount(); i++) {
                    if (cboHangSX.getItemAt(i).id.equals(maHang)) {
                        cboHangSX.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblThuoc), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themThuoc());
        btnSua.addActionListener(e -> suaThuoc());
        btnXoa.addActionListener(e -> xoaThuoc());
        btnLamMoi.addActionListener(e -> {
            xoaFormThuoc();
            taiDanhSachHangSX();
            taiDuLieuThuoc();
        });
        btnTim.addActionListener(e -> {
            String kw = txtTimThuoc.getText().trim();
            sorterThuoc.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw, 1, 7));
        });

        return panel;
    }

    private void taiDanhSachHangSX() {
        try {
            String res = ApiClient.sendGet("/danh-muc/hang-sx");
            cboHangSX.removeAllItems();
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaHangSX");
                    String ten = extractVal(item, "TenHang");
                    if (!id.isEmpty())
                        cboHangSX.addItem(new HangItem(id, ten));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void taiDuLieuThuoc() {
        try {
            String res = ApiClient.sendGet("/thuoc");
            modelThuoc.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaThuoc");
                    String ten = extractVal(item, "TenThuoc");
                    String dvt = extractVal(item, "DonViTinh");
                    String giaNhap = extractVal(item, "GiaNhap");
                    String giaBan = extractVal(item, "GiaBan");
                    String ton = extractVal(item, "SoLuongTon");
                    String maHang = extractVal(item, "MaHangSX");
                    String tenHang = extractVal(item, "TenHang");
                    if (!id.isEmpty()) {
                        modelThuoc.addRow(new Object[] { id, ten, dvt, giaNhap, giaBan, ton, maHang,
                                tenHang.equals("null") ? "" : tenHang });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themThuoc() {
        if (txtTenThuoc.getText().trim().isEmpty() || txtDVTThuoc.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên thuốc và đơn vị tính!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            HangItem selectedHang = (HangItem) cboHangSX.getSelectedItem();
            String maHang = selectedHang != null ? selectedHang.id : "null";
            String payload = String.format(
                    "{\"TenThuoc\":\"%s\",\"DonViTinh\":\"%s\",\"GiaNhap\":%s,\"GiaBan\":%s,\"SoLuongTon\":%s,\"MaHangSX\":%s}",
                    txtTenThuoc.getText().trim(), txtDVTThuoc.getText().trim(),
                    txtGiaNhap.getText().trim(), txtGiaBan.getText().trim(), txtSoLuongTon.getText().trim(), maHang);
            String res = ApiClient.sendPost("/thuoc", payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Thêm thuốc thành công!");
                taiDuLieuThuoc();
                xoaFormThuoc();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thuốc thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu hoặc kết nối server!");
        }
    }

    private void suaThuoc() {
        String id = txtMaThuoc.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc trong bảng để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            HangItem selectedHang = (HangItem) cboHangSX.getSelectedItem();
            String maHang = selectedHang != null ? selectedHang.id : "null";
            String payload = String.format(
                    "{\"TenThuoc\":\"%s\",\"DonViTinh\":\"%s\",\"GiaNhap\":%s,\"GiaBan\":%s,\"SoLuongTon\":%s,\"MaHangSX\":%s}",
                    txtTenThuoc.getText().trim(), txtDVTThuoc.getText().trim(),
                    txtGiaNhap.getText().trim(), txtGiaBan.getText().trim(), txtSoLuongTon.getText().trim(), maHang);
            String res = ApiClient.sendPut("/thuoc/" + id, payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thuốc thành công!");
                taiDuLieuThuoc();
                xoaFormThuoc();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thuốc!");
        }
    }

    private void xoaThuoc() {
        String id = txtMaThuoc.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa thuốc này khỏi danh mục?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            String res = ApiClient.sendDelete("/thuoc/" + id);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Xóa thuốc thành công!");
                taiDuLieuThuoc();
                xoaFormThuoc();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa thuốc đang có dữ liệu nhập/bán!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void xoaFormThuoc() {
        txtMaThuoc.setText("");
        txtTenThuoc.setText("");
        txtDVTThuoc.setText("");
        txtGiaNhap.setText("0");
        txtGiaBan.setText("0");
        txtSoLuongTon.setText("0");
        txtTimThuoc.setText("");
        sorterThuoc.setRowFilter(null);
        tblThuoc.clearSelection();
    }

    // ==========================================
    // TAB 2: QUẢN LÝ LOẠI THUỐC
    // ==========================================
    private JPanel taoTabLoaiThuoc() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(3, 2, 10, 10));
        txtMaLoai = new JTextField();
        txtMaLoai.setEditable(false);
        txtTenLoai = new JTextField();
        txtDVTLoai = new JTextField();

        pnlForm.add(new JLabel("Mã Loại:"));
        pnlForm.add(txtMaLoai);
        pnlForm.add(new JLabel("Tên Loại Thuốc (*):"));
        pnlForm.add(txtTenLoai);
        pnlForm.add(new JLabel("Đơn Vị Tính Mặc Định (*):"));
        pnlForm.add(txtDVTLoai);

        JPanel pnlAction = new JPanel(new BorderLayout());
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtTimLoai = new JTextField(18);
        JButton btnTim = new JButton("Tìm kiếm");
        pnlSearch.add(new JLabel("Tìm loại thuốc:"));
        pnlSearch.add(txtTimLoai);
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

        modelLoai = new DefaultTableModel(new String[] { "Mã Loại", "Tên Loại Thuốc", "Đơn Vị Tính" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLoai = new JTable(modelLoai);
        sorterLoai = new TableRowSorter<>(modelLoai);
        tblLoai.setRowSorter(sorterLoai);

        tblLoai.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = tblLoai.getSelectedRow();
            if (viewRow >= 0) {
                int row = tblLoai.convertRowIndexToModel(viewRow);
                txtMaLoai.setText(modelLoai.getValueAt(row, 0).toString());
                txtTenLoai.setText(modelLoai.getValueAt(row, 1).toString());
                txtDVTLoai.setText(modelLoai.getValueAt(row, 2).toString());
            }
        });

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblLoai), BorderLayout.CENTER);

        btnThem.addActionListener(e -> themLoai());
        btnSua.addActionListener(e -> suaLoai());
        btnXoa.addActionListener(e -> xoaLoai());
        btnLamMoi.addActionListener(e -> {
            xoaFormLoai();
            taiDuLieuLoaiThuoc();
        });
        btnTim.addActionListener(e -> {
            String kw = txtTimLoai.getText().trim();
            sorterLoai.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw, 1));
        });

        return panel;
    }

    private void taiDuLieuLoaiThuoc() {
        try {
            String res = ApiClient.sendGet("/loai-thuoc");
            modelLoai.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaLoai");
                    String ten = extractVal(item, "TenLoai");
                    String dvt = extractVal(item, "DonViTinh");
                    if (!id.isEmpty())
                        modelLoai.addRow(new Object[] { id, ten, dvt });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void themLoai() {
        if (txtTenLoai.getText().trim().isEmpty() || txtDVTLoai.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên loại và đơn vị tính!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = String.format("{\"TenLoai\":\"%s\",\"DonViTinh\":\"%s\"}", txtTenLoai.getText().trim(),
                    txtDVTLoai.getText().trim());
            String res = ApiClient.sendPost("/loai-thuoc", payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Thêm loại thuốc thành công!");
                taiDuLieuLoaiThuoc();
                xoaFormLoai();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void suaLoai() {
        String id = txtMaLoai.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại thuốc để sửa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String payload = String.format("{\"TenLoai\":\"%s\",\"DonViTinh\":\"%s\"}", txtTenLoai.getText().trim(),
                    txtDVTLoai.getText().trim());
            String res = ApiClient.sendPut("/loai-thuoc/" + id, payload);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                taiDuLieuLoaiThuoc();
                xoaFormLoai();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void xoaLoai() {
        String id = txtMaLoai.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại thuốc để xóa!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa loại thuốc này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            String res = ApiClient.sendDelete("/loai-thuoc/" + id);
            if (res.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                taiDuLieuLoaiThuoc();
                xoaFormLoai();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server!");
        }
    }

    private void xoaFormLoai() {
        txtMaLoai.setText("");
        txtTenLoai.setText("");
        txtDVTLoai.setText("");
        txtTimLoai.setText("");
        sorterLoai.setRowFilter(null);
        tblLoai.clearSelection();
    }

    private String extractVal(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "";
            start += pattern.length();
            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"'))
                start++;
            int end = start;
            while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}'
                    && json.charAt(end) != ']')
                end++;
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "";
        }
    }
}