package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Màn hình Swing Quản lý Lô Thuốc (thuộc Module 2 - Nhập Kho)
 * Thực hiện bởi: Phan Anh Ngữ
 * Chức năng:
 *  - Hiển thị danh sách lô thuốc từ view v_LoThuoc_TinhTrang (sắp xếp theo HanSuDung ASC)
 *  - Bộ lọc tình trạng: Tất cả / Còn hạn / Sắp hết hạn (<= 30 ngày) / Hết hạn
 *  - Tô màu trực quan dòng bảng: Hết hạn (đỏ nhạt), Sắp hết hạn (vàng nhạt)
 *  - Nút "Làm mới" dữ liệu
 */
public class LoThuocPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> cbLoc;
    private JTable tblLoThuoc;
    private DefaultTableModel modelLoThuoc;
    private JButton btnLamMoi;

    private JLabel lblTongLo;
    private JLabel lblConHan;
    private JLabel lblSapHetHan;
    private JLabel lblHetHan;

    private final DecimalFormat dfTien = new DecimalFormat("#,###");
    private final DecimalFormat dfSo = new DecimalFormat("0.##");

    public LoThuocPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        khoiTaoGiaoDien();
        taiDuLieu();
    }

    private void khoiTaoGiaoDien() {
        // --- 1. THANH CÔNG CỤ TRÊN CÙNG (TIÊU ĐỀ, BỘ LỌC, NÚT BẤM) ---
        JPanel pnlTop = new JPanel(new BorderLayout(10, 10));
        pnlTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Tiêu đề & tóm tắt
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel lblTitle = new JLabel("THEO DÕI TÌNH TRẠNG HẠN DÙNG LÔ THUỐC");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblTitle.setForeground(new Color(40, 58, 90));

        JPanel pnlThongKe = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        lblTongLo = new JLabel("Tổng số lô: 0");
        lblTongLo.setFont(new Font("Arial", Font.BOLD, 12));

        lblConHan = new JLabel("Còn hạn: 0");
        lblConHan.setFont(new Font("Arial", Font.BOLD, 12));
        lblConHan.setForeground(new Color(46, 125, 50));

        lblSapHetHan = new JLabel("Sắp hết hạn: 0");
        lblSapHetHan.setFont(new Font("Arial", Font.BOLD, 12));
        lblSapHetHan.setForeground(new Color(230, 81, 0));

        lblHetHan = new JLabel("Hết hạn: 0");
        lblHetHan.setFont(new Font("Arial", Font.BOLD, 12));
        lblHetHan.setForeground(new Color(198, 40, 40));

        pnlThongKe.add(lblTongLo);
        pnlThongKe.add(lblConHan);
        pnlThongKe.add(lblSapHetHan);
        pnlThongKe.add(lblHetHan);

        pnlTitle.add(lblTitle);
        pnlTitle.add(pnlThongKe);

        // Bộ lọc và các nút điều khiển bên phải
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JLabel lblLoc = new JLabel("Lọc theo tình trạng:");
        lblLoc.setFont(new Font("Arial", Font.BOLD, 13));

        cbLoc = new JComboBox<>(new String[] {
                "Tất cả",
                "Còn hạn (> 30 ngày)",
                "Sắp hết hạn (≤ 30 ngày)",
                "Hết hạn"
        });
        cbLoc.setFont(new Font("Arial", Font.PLAIN, 13));
        cbLoc.setPreferredSize(new Dimension(190, 32));
        cbLoc.addActionListener(e -> taiDuLieu());

        btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(new Font("Arial", Font.BOLD, 13));
        btnLamMoi.setBackground(new Color(70, 130, 180));
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setPreferredSize(new Dimension(100, 32));
        btnLamMoi.addActionListener(e -> taiDuLieu());

        pnlControls.add(lblLoc);
        pnlControls.add(cbLoc);
        pnlControls.add(btnLamMoi);

        pnlTop.add(pnlTitle, BorderLayout.WEST);
        pnlTop.add(pnlControls, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. BẢNG HIỂN THỊ DANH SÁCH LÔ THUỐC ---
        String[] columnNames = {
                "Mã Lô", "Mã Thuốc", "Tên Thuốc", "Số Lô",
                "Ngày Sản Xuất", "Hạn Sử Dụng", "Số Lượng Tồn",
                "Giá Nhập (VNĐ)", "Số Ngày Còn", "Tình Trạng"
        };

        modelLoThuoc = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLoThuoc = new JTable(modelLoThuoc);
        tblLoThuoc.setFont(new Font("Arial", Font.PLAIN, 13));
        tblLoThuoc.setRowHeight(26);
        tblLoThuoc.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tblLoThuoc.getTableHeader().setBackground(new Color(230, 235, 245));
        tblLoThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Cài đặt độ rộng ưu tiên cho các cột
        tblLoThuoc.getColumnModel().getColumn(0).setPreferredWidth(60);   // Mã Lô
        tblLoThuoc.getColumnModel().getColumn(1).setPreferredWidth(70);   // Mã Thuốc
        tblLoThuoc.getColumnModel().getColumn(2).setPreferredWidth(200);  // Tên Thuốc
        tblLoThuoc.getColumnModel().getColumn(3).setPreferredWidth(100);  // Số Lô
        tblLoThuoc.getColumnModel().getColumn(4).setPreferredWidth(100);  // Ngày SX
        tblLoThuoc.getColumnModel().getColumn(5).setPreferredWidth(100);  // Hạn SD
        tblLoThuoc.getColumnModel().getColumn(6).setPreferredWidth(90);   // SL Tồn
        tblLoThuoc.getColumnModel().getColumn(7).setPreferredWidth(110);  // Giá Nhập
        tblLoThuoc.getColumnModel().getColumn(8).setPreferredWidth(95);   // Số Ngày Còn
        tblLoThuoc.getColumnModel().getColumn(9).setPreferredWidth(110);  // Tình Trạng

        // Thiết lập Renderer tô màu cảnh báo theo dòng và căn lề
        caiDatRendererToMau(tblLoThuoc);

        JScrollPane scrollPane = new JScrollPane(tblLoThuoc);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Chi tiết các lô thuốc trong kho",
                0, 0, new Font("Arial", Font.BOLD, 13), new Color(40, 58, 90)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. THANH CHÚ THÍCH MÀU SẮC PHÍA DƯỚI ---
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        pnlLegend.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        pnlLegend.add(taoMauChuThich(new Color(255, 205, 210), new Color(183, 28, 28), "Hết hạn sử dụng (SoNgayConLai < 0)"));
        pnlLegend.add(taoMauChuThich(new Color(255, 249, 196), new Color(130, 119, 23), "Sắp hết hạn (0 - 30 ngày)"));
        pnlLegend.add(taoMauChuThich(Color.WHITE, Color.DARK_GRAY, "Còn hạn (> 30 ngày)"));

        add(pnlLegend, BorderLayout.SOUTH);
    }

    private JPanel taoMauChuThich(Color bg, Color border, String moTa) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(20, 14));
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(border));

        JLabel lbl = new JLabel(moTa);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));

        pnl.add(box);
        pnl.add(lbl);
        return pnl;
    }

    /**
     * Cài đặt Renderer tô màu dòng theo phân loại hạn dùng và căn chỉnh lề các cột
     */
    private void caiDatRendererToMau(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            final int colIndex = col;
            table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                public Component getTableCellRendererComponent(
                        JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                    // Căn chỉnh lề theo từng loại cột
                    if (colIndex == 2) {
                        setHorizontalAlignment(SwingConstants.LEFT); // Tên thuốc
                    } else if (colIndex == 6 || colIndex == 7) {
                        setHorizontalAlignment(SwingConstants.RIGHT); // Số lượng tồn, Giá nhập
                    } else {
                        setHorizontalAlignment(SwingConstants.CENTER); // Mã, Số lô, Ngày, Tình trạng
                    }

                    if (!isSelected) {
                        String tinhTrang = "";
                        Object valTinhTrang = tbl.getValueAt(row, 9);
                        if (valTinhTrang != null) {
                            tinhTrang = valTinhTrang.toString().trim();
                        }

                        // Đỏ nhạt: Hết hạn; Vàng nhạt: Sắp hết hạn; Trắng: Còn hạn
                        if (tinhTrang.contains("Hết hạn") || "HET_HAN".equalsIgnoreCase(tinhTrang)) {
                            c.setBackground(new Color(255, 205, 210));
                            c.setForeground(new Color(183, 28, 28));
                        } else if (tinhTrang.contains("Sắp hết") || "SAP_HET_HAN".equalsIgnoreCase(tinhTrang)) {
                            c.setBackground(new Color(255, 249, 196));
                            c.setForeground(new Color(130, 119, 23));
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(new Color(33, 33, 33));
                        }
                    } else {
                        c.setBackground(tbl.getSelectionBackground());
                        c.setForeground(tbl.getSelectionForeground());
                    }

                    return c;
                }
            });
        }
    }

    /**
     * Nạp dữ liệu lô thuốc từ server qua GET /api/nhap-kho/lo-thuoc
     */
    public void taiDuLieu() {
        String endpoint = "/nhap-kho/lo-thuoc";
        int selectedIdx = cbLoc != null ? cbLoc.getSelectedIndex() : 0;
        if (selectedIdx == 1) endpoint += "?phanLoai=CON_HAN";
        else if (selectedIdx == 2) endpoint += "?phanLoai=SAP_HET_HAN";
        else if (selectedIdx == 3) endpoint += "?phanLoai=HET_HAN";

        final String url = endpoint;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ApiClient.sendGet(url);
            }

            @Override
            protected void done() {
                try {
                    capNhatBang(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoThuocPanel.this,
                            "Lỗi nạp danh sách lô thuốc: " + e.getMessage(),
                            "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void capNhatBang(String res) {
        try {
            modelLoThuoc.setRowCount(0);

            int cntTong = 0;
            int cntConHan = 0;
            int cntSapHet = 0;
            int cntHetHan = 0;

            if (res.contains("\"success\":true")) {
                String[] items = res.split("\\}\\,\\{");
                for (String item : items) {
                    String maLo = extractVal(item, "MaLo");
                    if (maLo.isEmpty()) continue;

                    String maThuoc = extractVal(item, "MaThuoc");
                    String tenThuoc = extractVal(item, "TenThuoc");
                    String soLo = extractVal(item, "SoLo");

                    String nsx = extractVal(item, "NgaySanXuat");
                    if (nsx.contains("T")) {
                        nsx = nsx.substring(0, 10);
                    }
                    if (nsx.isEmpty() || "null".equalsIgnoreCase(nsx)) {
                        nsx = "-";
                    }

                    String hsd = extractVal(item, "HanSuDung");
                    if (hsd.contains("T")) {
                        hsd = hsd.substring(0, 10);
                    }

                    String slTon = extractVal(item, "SoLuongTon");
                    try {
                        slTon = dfSo.format(Double.parseDouble(slTon));
                    } catch (Exception ignored) {}

                    String giaNhap = dinhDangTien(extractVal(item, "GiaNhap"));
                    String soNgay = extractVal(item, "SoNgayConLai");
                    String phanLoai = extractVal(item, "PhanLoai");

                    String tinhTrangHienThi;
                    if ("HET_HAN".equalsIgnoreCase(phanLoai)) {
                        tinhTrangHienThi = "Hết hạn";
                        cntHetHan++;
                    } else if ("SAP_HET_HAN".equalsIgnoreCase(phanLoai)) {
                        tinhTrangHienThi = "Sắp hết hạn";
                        cntSapHet++;
                    } else {
                        tinhTrangHienThi = "Còn hạn";
                        cntConHan++;
                    }
                    cntTong++;

                    modelLoThuoc.addRow(new Object[] {
                            maLo, maThuoc, tenThuoc, soLo,
                            nsx, hsd, slTon, giaNhap, soNgay, tinhTrangHienThi
                    });
                }
            }

            // Cập nhật nhãn thống kê
            if (lblTongLo != null) lblTongLo.setText("Tổng số lô: " + cntTong);
            if (lblConHan != null) lblConHan.setText("Còn hạn: " + cntConHan);
            if (lblSapHetHan != null) lblSapHetHan.setText("Sắp hết hạn: " + cntSapHet);
            if (lblHetHan != null) lblHetHan.setText("Hết hạn: " + cntHetHan);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi nạp danh sách lô thuốc: " + e.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Phương thức tương thích để các màn hình khác hoặc reflection gọi
     */
    public void taiDuLieuLoThuoc() {
        taiDuLieu();
    }

    public void lamMoi() {
        taiDuLieu();
    }

    private String dinhDangTien(String val) {
        if (val == null || val.isEmpty() || "null".equalsIgnoreCase(val)) return "0";
        try {
            double d = Double.parseDouble(val);
            return dfTien.format(d);
        } catch (Exception e) {
            return val;
        }
    }

    /**
     * Hàm trích xuất giá trị JSON đơn giản tương thích định dạng API Express
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
}
