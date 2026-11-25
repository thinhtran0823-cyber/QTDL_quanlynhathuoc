package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoiYNhapHangPanel extends JPanel {
    private JComboBox<String> cboBoLoc;
    private JSpinner spnNguongTon;
    private JButton btnTaiLai, btnXuatCSV;
    private JLabel lblThongKeNhanh;
    private JTable tblGoiY;
    private DefaultTableModel tableModel;

    public GoiYNhapHangPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        initUI();
        taiDuLieu();
    }

    private void initUI() {
        // --- THANH TIÊU ĐỀ & BỘ LỌC PHÍA TRÊN ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("GỢI Ý NHẬP HÀNG THÔNG MINH (CẢNH BÁO TỒN KHO & TOP BÁN CHẠY 30 NGÀY)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(0, 102, 153));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(new Color(245, 248, 250));
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        filterPanel.add(new JLabel("Chế độ lọc:"));
        cboBoLoc = new JComboBox<>(new String[] {
                "Cần nhập hàng (Tồn thấp + Bán chạy)",
                "Cảnh báo tồn kho khả dụng thấp",
                "Top thuốc bán chạy (30 ngày qua)",
                "Tất cả thuốc trong kho"
        });
        cboBoLoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterPanel.add(cboBoLoc);

        filterPanel.add(new JLabel("Ngưỡng tồn an toàn:"));
        spnNguongTon = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 5));
        spnNguongTon.setPreferredSize(new Dimension(65, 26));
        filterPanel.add(spnNguongTon);

        btnTaiLai = new JButton("Phân tích & Làm mới");
        btnTaiLai.setBackground(new Color(0, 123, 255));
        btnTaiLai.setForeground(Color.WHITE);
        btnTaiLai.setFocusPainted(false);
        filterPanel.add(btnTaiLai);

        btnXuatCSV = new JButton("Xuất phiếu dự trù (.CSV)");
        btnXuatCSV.setBackground(new Color(40, 167, 69));
        btnXuatCSV.setForeground(Color.WHITE);
        btnXuatCSV.setFocusPainted(false);
        filterPanel.add(btnXuatCSV);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- BẢNG HIỂN THỊ DỮ LIỆU ---
        String[] columns = {
                "Mã Thuốc", "Tên Thuốc", "ĐVT", "Hãng SX",
                "Tồn Khả Dụng", "Tồn Hết Hạn", "Đã Bán (30N)",
                "Đề Xuất Nhập", "Mức Độ Cảnh Báo"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblGoiY = new JTable(tableModel);
        tblGoiY.setRowHeight(28);
        tblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblGoiY.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblGoiY.getTableHeader().setBackground(new Color(230, 238, 245));

        // Tô màu cảnh báo cho cột Trạng thái
        tblGoiY.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));

                if (!isSelected) {
                    if (status.contains("HẾT HÀNG")) {
                        c.setBackground(new Color(255, 215, 215));
                        c.setForeground(new Color(180, 0, 0));
                    } else if (status.contains("TỒN THẤP")) {
                        c.setBackground(new Color(255, 240, 205));
                        c.setForeground(new Color(180, 95, 0));
                    } else if (status.contains("BÁN CHẠY")) {
                        c.setBackground(new Color(215, 245, 220));
                        c.setForeground(new Color(0, 120, 40));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.DARK_GRAY);
                    }
                }
                return c;
            }
        });

        add(new JScrollPane(tblGoiY), BorderLayout.CENTER);

        // --- THANH TRẠNG THÁI PHÍA DƯỚI ---
        lblThongKeNhanh = new JLabel("Đang tải dữ liệu phân tích...");
        lblThongKeNhanh.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        add(lblThongKeNhanh, BorderLayout.SOUTH);

        // --- SỰ KIỆN ---
        btnTaiLai.addActionListener(e -> taiDuLieu());
        cboBoLoc.addActionListener(e -> taiDuLieu());
        btnXuatCSV.addActionListener(e -> xuatFileCSV());
    }

    private void taiDuLieu() {
        String nhomParam = "CAN_NHAP";
        int idx = cboBoLoc.getSelectedIndex();
        if (idx == 1)
            nhomParam = "TON_THAP";
        else if (idx == 2)
            nhomParam = "BAN_CHAY";
        else if (idx == 3)
            nhomParam = "ALL";

        int nguongTon = (Integer) spnNguongTon.getValue();
        String endpoint = "/goi-y-nhap-hang?nhom=" + nhomParam + "&nguongTon=" + nguongTon;

        SwingWorker<List<String[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                String response = ApiClient.sendGet(endpoint);
                return parseJsonArray(response);
            }

            @Override
            protected void done() {
                try {
                    List<String[]> rows = get();
                    tableModel.setRowCount(0);
                    int soHetHang = 0, soTonThap = 0, soBanChay = 0;

                    for (String[] r : rows) {
                        String rawStatus = r[8];
                        String displayStatus = "An toàn";
                        if ("HET_HANG".equals(rawStatus)) {
                            displayStatus = "HẾT HÀNG KHẢ DỤNG";
                            soHetHang++;
                        } else if ("TON_THAP".equals(rawStatus)) {
                            displayStatus = "TỒN THẤP (< " + nguongTon + ")";
                            soTonThap++;
                        } else if ("BAN_CHAY".equals(rawStatus)) {
                            displayStatus = "BÁN CHẠY (30 NGÀY)";
                            soBanChay++;
                        }
                        r[8] = displayStatus;
                        tableModel.addRow(r);
                    }

                    lblThongKeNhanh.setText(String.format(
                            "Tổng cộng: %d mặt hàng hiển thị  |  Hết hàng: %d  |  Tồn thấp: %d  |  Bán chạy: %d",
                            rows.size(), soHetHang, soTonThap, soBanChay));
                } catch (Exception ex) {
                    lblThongKeNhanh.setText("Không thể tải dữ liệu gợi ý nhập hàng.");
                    JOptionPane.showMessageDialog(GoiYNhapHangPanel.this,
                            "Lỗi tải gợi ý nhập hàng: " + ex.getMessage(), "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private List<String[]> parseJsonArray(String json) {
        List<String[]> result = new ArrayList<>();
        if (json == null || !json.contains("\"success\":true"))
            return result;

        int dataIdx = json.indexOf("\"data\":");
        if (dataIdx == -1)
            return result;

        String dataPart = json.substring(dataIdx);
        Pattern objPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = objPattern.matcher(dataPart);

        while (matcher.find()) {
            String obj = matcher.group(1);
            result.add(new String[] {
                    extractField(obj, "MaThuoc"),
                    extractField(obj, "TenThuoc"),
                    extractField(obj, "DonViTinh"),
                    extractField(obj, "TenHang"),
                    extractField(obj, "TonKhaDung"),
                    extractField(obj, "TonHetHan"),
                    extractField(obj, "DaBan30Ngay"),
                    extractField(obj, "DeXuatNhapThem"),
                    extractField(obj, "TrangThaiCanhBao")
            });
        }
        return result;
    }

    private String extractField(String obj, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"?([^\",}]+)\"?");
        Matcher m = p.matcher(obj);
        return m.find() ? m.group(1).trim() : "";
    }

    private void xuatFileCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất!");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("DeXuatNhapHang.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile(), StandardCharsets.UTF_8)) {
                fw.write('\ufeff');
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    fw.write(tableModel.getColumnName(c) + (c < tableModel.getColumnCount() - 1 ? "," : "\n"));
                }
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        fw.write("\"" + tableModel.getValueAt(r, c) + "\""
                                + (c < tableModel.getColumnCount() - 1 ? "," : "\n"));
                    }
                }
                JOptionPane.showMessageDialog(this, "Xuất file dự trù nhập hàng thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage());
            }
        }
    }
}