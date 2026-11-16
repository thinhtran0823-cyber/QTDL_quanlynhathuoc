package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Swing Quản lý Nhập Kho (Module 2)
 * Thực hiện bởi: Phan Anh Ngữ
 * Bao gồm 3 tab:
 *  1. Lập phiếu nhập (chọn NCC, NV, Thuốc; nhập lô, hạn dùng, SL, giá; lưu phiếu)
 *  2. Lịch sử phiếu nhập (xem danh sách chi tiết các phiếu nhập)
 *  3. Lô thuốc (tích hợp LoThuocPanel)
 */
public class NhapKhoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // --- Tab 1: Lập phiếu nhập ---
    private JComboBox<NCCItem> cbNCC;
    private JComboBox<NhanVienItem> cbNhanVien;
    private JComboBox<ThuocItem> cbThuoc;

    private JTextField txtSoLo;
    private JTextField txtNgaySanXuat;
    private JTextField txtHanSuDung;
    private JTextField txtSoLuong;
    private JTextField txtGiaNhap;

    private JTable tblChiTietTam;
    private DefaultTableModel modelChiTietTam;
    private JLabel lblTongTienTam;
    private JButton btnThemDong;
    private JButton btnXoaDong;
    private JButton btnLamMoiForm;
    private JButton btnLuuPhieu;

    // --- Tab 2: Lịch sử phiếu nhập ---
    private JTable tblLichSu;
    private DefaultTableModel modelLichSu;
    private JButton btnLamMoiLichSu;

    // --- Tab 3: Lô thuốc ---
    private LoThuocPanel panelLoThuoc;

    // Bộ định dạng số và ngày
    private final DecimalFormat dfTien = new DecimalFormat("#,###");
    private final DecimalFormat dfSo = new DecimalFormat("0.##");

    public NhapKhoPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // 3 tab theo đúng yêu cầu đặc tả
        tabbedPane.addTab(" Lập phiếu nhập ", taoTabLapPhieuNhap());
        tabbedPane.addTab(" Lịch sử phiếu nhập ", taoTabLichSuPhieuNhap());
        tabbedPane.addTab(" Lô thuốc ", taoTabLoThuoc());

        // Lắng nghe chuyển tab để tự động nạp lại dữ liệu
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) {
                taiLichSuPhieuNhap();
            } else if (selectedIndex == 2) {
                lamMoiLoThuoc();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu cho các combobox và bảng lịch sử
        taiDuLieuCombobox();
        taiLichSuPhieuNhap();
    }

    // =========================================================================
    // TAB 1: LẬP PHIẾU NHẬP
    // =========================================================================
    private JPanel taoTabLapPhieuNhap() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- KHU VỰC NHẬP LIỆU PHÍA TRÊN ---
        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));

        // 1. Thông tin chung phiếu nhập (NCC, Nhân viên)
        JPanel pnlPhieu = new JPanel(new GridBagLayout());
        pnlPhieu.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Thông tin phiếu nhập", 0, 0,
                new Font("Arial", Font.BOLD, 13), new Color(40, 58, 90)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbNCC = new JComboBox<>();
        cbNhanVien = new JComboBox<>();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        pnlPhieu.add(new JLabel("Nhà cung cấp:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        pnlPhieu.add(cbNCC, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        pnlPhieu.add(new JLabel("Nhân viên lập phiếu:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        pnlPhieu.add(cbNhanVien, gbc);

        // 2. Thông tin chi tiết lô thuốc nhập
        JPanel pnlChiTiet = new JPanel(new GridBagLayout());
        pnlChiTiet.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Thông tin chi tiết lô thuốc", 0, 0,
                new Font("Arial", Font.BOLD, 13), new Color(40, 58, 90)));

        cbThuoc = new JComboBox<>();
        txtSoLo = new JTextField();
        txtNgaySanXuat = new JTextField();
        txtNgaySanXuat.setToolTipText("Định dạng yyyy-MM-dd (có thể để trống)");
        txtHanSuDung = new JTextField();
        txtHanSuDung.setToolTipText("Định dạng yyyy-MM-dd (bắt buộc)");
        txtSoLuong = new JTextField();
        txtGiaNhap = new JTextField();

        // Tự động điền giá nhập khi chọn thuốc
        cbThuoc.addActionListener(e -> {
            ThuocItem selected = (ThuocItem) cbThuoc.getSelectedItem();
            if (selected != null) {
                txtGiaNhap.setText(dfSo.format(selected.giaNhap));
            }
        });

        // Dòng 1 chi tiết: Thuốc, Số Lô, Ngày SX
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Thuốc:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.28;
        pnlChiTiet.add(cbThuoc, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Số lô:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.18;
        pnlChiTiet.add(txtSoLo, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Ngày SX:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.18;
        pnlChiTiet.add(txtNgaySanXuat, gbc);

        // Dòng 2 chi tiết: Hạn sử dụng, Số lượng, Giá nhập
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Hạn sử dụng (*):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.28;
        pnlChiTiet.add(txtHanSuDung, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Số lượng (*):"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.18;
        pnlChiTiet.add(txtSoLuong, gbc);

        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.12;
        pnlChiTiet.add(new JLabel("Giá nhập (VNĐ):"), gbc);
        gbc.gridx = 5; gbc.gridy = 1; gbc.weightx = 0.18;
        pnlChiTiet.add(txtGiaNhap, gbc);

        // Nút thao tác trên dòng chi tiết
        JPanel pnlBtnsChiTiet = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnThemDong = new JButton("Thêm dòng");
        btnXoaDong = new JButton("Xóa dòng");
        btnLamMoiForm = new JButton("Làm mới form");

        btnThemDong.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoaDong.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLamMoiForm.setFont(new Font("Arial", Font.PLAIN, 12));

        pnlBtnsChiTiet.add(btnThemDong);
        pnlBtnsChiTiet.add(btnXoaDong);
        pnlBtnsChiTiet.add(btnLamMoiForm);

        pnlTop.add(pnlPhieu, BorderLayout.NORTH);
        JPanel pnlGiua = new JPanel(new BorderLayout());
        pnlGiua.add(pnlChiTiet, BorderLayout.CENTER);
        pnlGiua.add(pnlBtnsChiTiet, BorderLayout.SOUTH);
        pnlTop.add(pnlGiua, BorderLayout.CENTER);

        // --- KHU VỰC BẢNG TẠM CHI TIẾT PHIẾU NHẬP ---
        String[] cotChiTiet = { "Mã Thuốc", "Tên Thuốc", "Số Lô", "Ngày SX", "Hạn Sử Dụng", "Số Lượng", "Đơn Giá Nhập (VNĐ)", "Thành Tiền (VNĐ)" };
        modelChiTietTam = new DefaultTableModel(cotChiTiet, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng tạm
            }
        };

        tblChiTietTam = new JTable(modelChiTietTam);
        tblChiTietTam.setRowHeight(24);
        tblChiTietTam.setFont(new Font("Arial", Font.PLAIN, 13));
        tblChiTietTam.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        // Căn giữa mã, số lô, ngày, số lượng; căn phải giá tiền
        canGiuaVaPhaiChoBang(tblChiTietTam, new int[] { 0, 2, 3, 4, 5 }, new int[] { 6, 7 });

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Danh sách thuốc trong phiếu nhập tạm tính", 0, 0,
                new Font("Arial", Font.BOLD, 13), new Color(40, 58, 90)));
        pnlCenter.add(new JScrollPane(tblChiTietTam), BorderLayout.CENTER);

        // --- KHU VỰC DƯỚI CÙNG: TỔNG TIỀN VÀ LƯU PHIẾU ---
        JPanel pnlBottom = new JPanel(new BorderLayout(15, 10));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        lblTongTienTam = new JLabel("Tổng tiền tạm tính: 0 VNĐ");
        lblTongTienTam.setFont(new Font("Arial", Font.BOLD, 16));
        lblTongTienTam.setForeground(new Color(180, 40, 20));

        btnLuuPhieu = new JButton("  LƯU PHIẾU NHẬP  ");
        btnLuuPhieu.setFont(new Font("Arial", Font.BOLD, 14));
        btnLuuPhieu.setBackground(new Color(40, 120, 70));
        btnLuuPhieu.setForeground(Color.WHITE);
        btnLuuPhieu.setFocusPainted(false);
        btnLuuPhieu.setPreferredSize(new Dimension(200, 38));

        pnlBottom.add(lblTongTienTam, BorderLayout.WEST);
        pnlBottom.add(btnLuuPhieu, BorderLayout.EAST);

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(pnlCenter, BorderLayout.CENTER);
        panel.add(pnlBottom, BorderLayout.SOUTH);

        // Gắn sự kiện các nút
        btnThemDong.addActionListener(e -> themDongChiTiet());
        btnXoaDong.addActionListener(e -> xoaDongChiTiet());
        btnLamMoiForm.addActionListener(e -> xoaFormNhap());
        btnLuuPhieu.addActionListener(e -> luuPhieuNhap());

        return panel;
    }

    // =========================================================================
    // TAB 2: LỊCH SỬ PHIẾU NHẬP
    // =========================================================================
    private JPanel taoTabLichSuPhieuNhap() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Thanh công cụ phía trên bảng lịch sử
        JPanel pnlHeader = new JPanel(new BorderLayout());
        JLabel lblTieuDe = new JLabel("LỊCH SỬ CÁC PHIẾU NHẬP KHO & CHI TIẾT");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 15));
        lblTieuDe.setForeground(new Color(40, 58, 90));

        btnLamMoiLichSu = new JButton("Làm mới dữ liệu");
        btnLamMoiLichSu.setFont(new Font("Arial", Font.PLAIN, 13));
        btnLamMoiLichSu.addActionListener(e -> taiLichSuPhieuNhap());

        pnlHeader.add(lblTieuDe, BorderLayout.WEST);
        pnlHeader.add(btnLamMoiLichSu, BorderLayout.EAST);

        String[] cotLichSu = {
                "Mã PN", "Ngày Nhập", "Nhà Cung Cấp", "Nhân Viên Lập", "Tổng Tiền Phiếu (VNĐ)",
                "Tên Thuốc", "Số Lô", "Hạn Sử Dụng", "SL Nhập", "Đơn Giá Nhập (VNĐ)", "Thành Tiền (VNĐ)"
        };
        modelLichSu = new DefaultTableModel(cotLichSu, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLichSu = new JTable(modelLichSu);
        tblLichSu.setRowHeight(24);
        tblLichSu.setFont(new Font("Arial", Font.PLAIN, 13));
        tblLichSu.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        canGiuaVaPhaiChoBang(tblLichSu, new int[] { 0, 1, 6, 7, 8 }, new int[] { 4, 9, 10 });

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblLichSu), BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 3: LÔ THUỐC (NẠP ĐỘNG LOTHUOCPANEL)
    // =========================================================================
    private Component taoTabLoThuoc() {
        panelLoThuoc = new LoThuocPanel();
        return panelLoThuoc;
    }

    private void lamMoiLoThuoc() {
        if (panelLoThuoc != null) panelLoThuoc.taiDuLieu();
    }

    // =========================================================================
    // XỬ LÝ NGHIỆP VỤ & GỌI API CLIENT
    // =========================================================================

    /**
     * Nạp dữ liệu các combobox: Nhà cung cấp, Nhân viên, Thuốc
     */
    private void taiDuLieuCombobox() {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                return new String[] {
                        ApiClient.sendGet("/danh-muc/nha-cung-cap"),
                        ApiClient.sendGet("/nhan-vien"),
                        ApiClient.sendGet("/thuoc")
                };
            }

            @Override
            protected void done() {
                try {
                    String[] responses = get();
                    napComboboxNhaCungCap(responses[0]);
                    napComboboxNhanVien(responses[1]);
                    napComboboxThuoc(responses[2]);
                } catch (Exception e) {
                    System.err.println("Lỗi nạp dữ liệu combobox: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void napComboboxNhaCungCap(String res) {
        try {
            cbNCC.removeAllItems();
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaNCC");
                    String ten = extractVal(item, "TenNCC");
                    if (!id.isEmpty()) {
                        cbNCC.addItem(new NCCItem(Integer.parseInt(id), ten));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp danh sách Nhà cung cấp: " + e.getMessage());
        }
    }

    private void napComboboxNhanVien(String res) {
        try {
            cbNhanVien.removeAllItems();
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaNV");
                    String ten = extractVal(item, "HoTen");
                    if (!id.isEmpty()) {
                        cbNhanVien.addItem(new NhanVienItem(Integer.parseInt(id), ten));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp danh sách Nhân viên: " + e.getMessage());
        }
    }

    private void napComboboxThuoc(String res) {
        try {
            cbThuoc.removeAllItems();
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String id = extractVal(item, "MaThuoc");
                    String ten = extractVal(item, "TenThuoc");
                    String dvt = extractVal(item, "DonViTinh");
                    String giaStr = extractVal(item, "GiaNhap");
                    double gia = 0;
                    try {
                        if (!giaStr.isEmpty()) gia = Double.parseDouble(giaStr);
                    } catch (Exception ignored) {}

                    if (!id.isEmpty()) {
                        cbThuoc.addItem(new ThuocItem(Integer.parseInt(id), ten, dvt, gia));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp danh sách Thuốc: " + e.getMessage());
        }
    }

    /**
     * Nạp bảng Lịch sử phiếu nhập: GET /nhap-kho/phieu-nhap
     */
    private void taiLichSuPhieuNhap() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.sendGet("/nhap-kho/phieu-nhap");
            }

            @Override
            protected void done() {
                try {
                    hienThiLichSu(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(NhapKhoPanel.this, "Lỗi kết nối máy chủ khi nạp lịch sử phiếu nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void hienThiLichSu(String res) {
        try {
            modelLichSu.setRowCount(0);
            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String maPN = extractVal(item, "MaPN");
                    if (maPN.isEmpty()) continue;

                    String ngayNhap = extractVal(item, "NgayNhap");
                    if (ngayNhap.contains("T")) {
                        ngayNhap = ngayNhap.substring(0, 10) + " " + ngayNhap.substring(11, Math.min(19, ngayNhap.length()));
                    }

                    String tenNCC = extractVal(item, "TenNCC");
                    String hoTen = extractVal(item, "HoTen");
                    String tongTien = dinhDangTien(extractVal(item, "TongTien"));
                    String tenThuoc = extractVal(item, "TenThuoc");
                    String soLo = extractVal(item, "SoLo");
                    String hanSuDung = extractVal(item, "HanSuDung");
                    if (hanSuDung.contains("T")) {
                        hanSuDung = hanSuDung.substring(0, 10);
                    }

                    String soLuongNhap = extractVal(item, "SoLuongNhap");
                    String donGiaNhap = dinhDangTien(extractVal(item, "DonGiaNhap"));
                    String thanhTien = dinhDangTien(extractVal(item, "ThanhTien"));

                    modelLichSu.addRow(new Object[] {
                            maPN, ngayNhap, tenNCC, hoTen, tongTien,
                            tenThuoc, soLo, hanSuDung, soLuongNhap, donGiaNhap, thanhTien
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể đọc dữ liệu lịch sử phiếu nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Thêm một dòng chi tiết thuốc vào bảng tạm
     */
    private void themDongChiTiet() {
        ThuocItem thuoc = (ThuocItem) cbThuoc.getSelectedItem();
        if (thuoc == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String soLo = txtSoLo.getText().trim();

        String ngaySX = txtNgaySanXuat.getText().trim();
        if (!ngaySX.isEmpty() && !kiemTraDinhDangNgay(ngaySX)) {
            JOptionPane.showMessageDialog(this, "Ngày sản xuất không đúng định dạng yyyy-MM-dd (hoặc để trống)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtNgaySanXuat.requestFocus();
            return;
        }

        String hanSD = txtHanSuDung.getText().trim();
        if (hanSD.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập hạn sử dụng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtHanSuDung.requestFocus();
            return;
        }
        if (!kiemTraDinhDangNgay(hanSD)) {
            JOptionPane.showMessageDialog(this, "Hạn sử dụng phải đúng định dạng yyyy-MM-dd!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtHanSuDung.requestFocus();
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            if (soLuong <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương (> 0)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtSoLuong.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ! Vui lòng nhập số nguyên dương.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSoLuong.requestFocus();
            return;
        }

        double giaNhap;
        try {
            giaNhap = Double.parseDouble(txtGiaNhap.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá nhập không hợp lệ! Vui lòng nhập số.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtGiaNhap.requestFocus();
            return;
        }

        double thanhTien = soLuong * giaNhap;

        modelChiTietTam.addRow(new Object[] {
                thuoc.maThuoc,
                thuoc.tenThuoc,
                soLo,
                ngaySX,
                hanSD,
                soLuong,
                dfTien.format(giaNhap),
                dfTien.format(thanhTien)
        });

        capNhatTongTienTam();

        // Xóa form nhập chi tiết để người dùng nhập dòng tiếp theo
        txtSoLo.setText("");
        txtNgaySanXuat.setText("");
        txtHanSuDung.setText("");
        txtSoLuong.setText("");
    }

    /**
     * Xóa dòng chi tiết được chọn khỏi bảng tạm
     */
    private void xoaDongChiTiet() {
        int selectedRow = tblChiTietTam.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trong bảng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modelChiTietTam.removeRow(selectedRow);
        capNhatTongTienTam();
    }

    /**
     * Xóa trắng các trường nhập
     */
    private void xoaFormNhap() {
        txtSoLo.setText("");
        txtNgaySanXuat.setText("");
        txtHanSuDung.setText("");
        txtSoLuong.setText("");
        if (cbThuoc.getItemCount() > 0) {
            cbThuoc.setSelectedIndex(0);
        }
        tblChiTietTam.clearSelection();
    }

    /**
     * Tính lại tổng tiền tạm tính của phiếu nhập
     */
    private void capNhatTongTienTam() {
        double tong = 0;
        for (int i = 0; i < modelChiTietTam.getRowCount(); i++) {
            Object val = modelChiTietTam.getValueAt(i, 7);
            if (val != null) {
                try {
                    String clean = val.toString().replaceAll("[^0-9.]", "");
                    tong += Double.parseDouble(clean);
                } catch (Exception ignored) {}
            }
        }
        lblTongTienTam.setText("Tổng tiền tạm tính: " + dfTien.format(tong) + " VNĐ");
    }

    /**
     * Lưu phiếu nhập: Gửi POST /api/nhap-kho/phieu-nhap
     */
    private void luuPhieuNhap() {
        NCCItem ncc = (NCCItem) cbNCC.getSelectedItem();
        if (ncc == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NhanVienItem nv = (NhanVienItem) cbNhanVien.getSelectedItem();
        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên lập phiếu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (modelChiTietTam.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Phiếu nhập chưa có mặt hàng nào! Vui lòng thêm ít nhất một dòng chi tiết.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn lưu phiếu nhập này (" + modelChiTietTam.getRowCount() + " mặt hàng)?",
                "Xác nhận lưu phiếu", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        // Dựng chuỗi JSON gửi lên server
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"maNCC\":").append(ncc.maNCC).append(",");
        sb.append("\"maNV\":").append(nv.maNV).append(",");
        sb.append("\"chiTiet\":[");

        for (int i = 0; i < modelChiTietTam.getRowCount(); i++) {
            if (i > 0) sb.append(",");
            int maThuoc = Integer.parseInt(modelChiTietTam.getValueAt(i, 0).toString());
            String soLo = esc(modelChiTietTam.getValueAt(i, 2).toString().trim());
            String nsx = modelChiTietTam.getValueAt(i, 3).toString().trim();
            String hsd = esc(modelChiTietTam.getValueAt(i, 4).toString().trim());
            int sl = Integer.parseInt(modelChiTietTam.getValueAt(i, 5).toString().trim());
            String giaStr = modelChiTietTam.getValueAt(i, 6).toString().replaceAll("[^0-9.]", "").trim();
            double gia = Double.parseDouble(giaStr);

            sb.append("{");
            sb.append("\"maThuoc\":").append(maThuoc).append(",");
            sb.append("\"soLo\":\"").append(soLo).append("\",");
            if (nsx.isEmpty()) {
                sb.append("\"ngaySanXuat\":null,");
            } else {
                sb.append("\"ngaySanXuat\":\"").append(esc(nsx)).append("\",");
            }
            sb.append("\"hanSuDung\":\"").append(hsd).append("\",");
            sb.append("\"soLuong\":").append(sl).append(",");
            sb.append("\"giaNhap\":").append(String.format(Locale.US, "%.2f", gia));
            sb.append("}");
        }
        sb.append("]}");

        final String requestBody = sb.toString();
        btnLuuPhieu.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.sendPost("/nhap-kho/phieu-nhap", requestBody);
            }

            @Override
            protected void done() {
                btnLuuPhieu.setEnabled(true);
                try {
                    String res = get();
            if (res.contains("\"success\":true")) {
                String maPN = extractVal(res, "MaPN");
                if (maPN.isEmpty()) maPN = extractVal(res, "maPN");

                String msg = "Tạo phiếu nhập thành công!";
                if (!maPN.isEmpty()) {
                    msg += " (Mã phiếu: " + maPN + ")";
                }
                JOptionPane.showMessageDialog(NhapKhoPanel.this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                // Xóa bảng tạm và làm mới dữ liệu
                modelChiTietTam.setRowCount(0);
                capNhatTongTienTam();
                xoaFormNhap();

                // Nạp lại danh sách lịch sử và lô thuốc
                taiLichSuPhieuNhap();
                lamMoiLoThuoc();
            } else {
                // Hiển thị chính xác thông báo lỗi từ máy chủ / trigger SQLSTATE 45000
                String msg = extractVal(res, "message");
                if (msg.isEmpty()) msg = "Tạo phiếu nhập thất bại!";
                JOptionPane.showMessageDialog(NhapKhoPanel.this, msg, "Lỗi từ máy chủ", JOptionPane.ERROR_MESSAGE);
            }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(NhapKhoPanel.this, "Lỗi kết nối máy chủ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    // HÀM TIỆN ÍCH HỖ TRỢ (PARSE JSON, FORMAT, VALIDATION)
    // =========================================================================

    /**
     * Tách giá trị trường JSON: hỗ trợ chuỗi có dấu nháy và giá trị số/boolean
     */
    private String extractVal(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();

            while (start < json.length() && json.charAt(start) == ' ') {
                start++;
            }
            if (start >= json.length()) return "";

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
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
                    end++;
                }
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean kiemTraDinhDangNgay(String strDate) {
        if (strDate == null || !strDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(strDate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String dinhDangTien(String val) {
        if (val == null || val.isEmpty()) return "0";
        try {
            double d = Double.parseDouble(val);
            return dfTien.format(d);
        } catch (Exception e) {
            return val;
        }
    }

    private void canGiuaVaPhaiChoBang(JTable table, int[] colGiua, int[] colPhai) {
        DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
        renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : colGiua) {
            if (col < table.getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(renderCenter);
            }
        }

        DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int col : colPhai) {
            if (col < table.getColumnCount()) {
                table.getColumnModel().getColumn(col).setCellRenderer(renderRight);
            }
        }
    }

    // =========================================================================
    // CÁC LỚP ITEM CHO COMBOBOX
    // =========================================================================
    private static class NCCItem {
        final int maNCC;
        final String tenNCC;

        NCCItem(int maNCC, String tenNCC) {
            this.maNCC = maNCC;
            this.tenNCC = tenNCC;
        }

        @Override
        public String toString() {
            return maNCC + " - " + tenNCC;
        }
    }

    private static class NhanVienItem {
        final int maNV;
        final String hoTen;

        NhanVienItem(int maNV, String hoTen) {
            this.maNV = maNV;
            this.hoTen = hoTen;
        }

        @Override
        public String toString() {
            return maNV + " - " + hoTen;
        }
    }

    private static class ThuocItem {
        final int maThuoc;
        final String tenThuoc;
        final String donViTinh;
        final double giaNhap;

        ThuocItem(int maThuoc, String tenThuoc, String donViTinh, double giaNhap) {
            this.maThuoc = maThuoc;
            this.tenThuoc = tenThuoc;
            this.donViTinh = donViTinh;
            this.giaNhap = giaNhap;
        }

        @Override
        public String toString() {
            return maThuoc + " - " + tenThuoc + (donViTinh.isEmpty() ? "" : " (" + donViTinh + ")");
        }
    }
}
