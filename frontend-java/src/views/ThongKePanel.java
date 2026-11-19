package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ThongKePanel extends JPanel {
    private JLabel lblTongDoanhThu;
    private JLabel lblTongChiPhi;
    private JLabel lblTongChiNhapKho;
    private JLabel lblThietHaiHetHan;
    private JLabel lblLoiNhuanRong;
    private JPanel cardLoiNhuan;

    private JComboBox<String> cbLoaiBaoCao;
    private JTable tblDoanhThu;
    private DefaultTableModel tableModel;

    public ThongKePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- 1. PHẦN TRÊN: 5 THẺ TỔNG QUAN TÀI CHÍNH ---
        JPanel panelCards = new JPanel(new GridLayout(1, 5, 12, 0));

        lblTongDoanhThu = taoCard(panelCards, "TỔNG DOANH THU", "0 VNĐ", new Color(40, 167, 69));
        lblTongChiPhi = taoCard(panelCards, "GIÁ VỐN HÀNG BÁN", "0 VNĐ", new Color(230, 126, 34));
        lblTongChiNhapKho = taoCard(panelCards, "TỔNG CHI NHẬP KHO", "0 VNĐ", new Color(23, 162, 184));
        lblThietHaiHetHan = taoCard(panelCards, "THIỆT HẠI HẾT HẠN", "0 VNĐ", new Color(220, 53, 69));
        lblLoiNhuanRong = taoCard(panelCards, "LỢI NHUẬN THỰC TẾ", "0 VNĐ", new Color(0, 123, 255));
        cardLoiNhuan = (JPanel) lblLoiNhuanRong.getParent();

        add(panelCards, BorderLayout.NORTH);

        // --- 2. PHẦN GIỮA: BẢNG CHI TIẾT ---
        JPanel panelCenter = new JPanel(new BorderLayout(10, 10));

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFilter.add(new JLabel("Thống kê theo:"));

        cbLoaiBaoCao = new JComboBox<>(new String[] { "NGAY", "TUAN", "THANG" });
        panelFilter.add(cbLoaiBaoCao);

        JButton btnTaiLai = new JButton("Tải dữ liệu");
        btnTaiLai.setBackground(new Color(108, 117, 125));
        btnTaiLai.setForeground(Color.WHITE);
        panelFilter.add(btnTaiLai);

        panelCenter.add(panelFilter, BorderLayout.NORTH);

        String[] columnNames = { "Kỳ / Thời gian", "Số hóa đơn bán", "Doanh thu (VNĐ)" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDoanhThu = new JTable(tableModel);
        tblDoanhThu.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tblDoanhThu);
        panelCenter.add(scrollPane, BorderLayout.CENTER);

        add(panelCenter, BorderLayout.CENTER);

        btnTaiLai.addActionListener(e -> taiDuLieuThongKe());
        cbLoaiBaoCao.addActionListener(e -> taiDuLieuThongKe());

        taiDuLieuThongKe();
    }

    private JLabel taoCard(JPanel parent, String title, String defaultValue, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel lblValue = new JLabel(defaultValue);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Arial", Font.BOLD, 15));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        parent.add(card);
        return lblValue;
    }

    public void taiDuLieuThongKe() {
        try {
            // 1. Tải chỉ số Doanh thu, Giá vốn, Chi nhập, Thiệt hại hết hạn & Lợi nhuận
            String resLoiNhuan = ApiClient.sendGet("/thong-ke/loi-nhuan");
            if (resLoiNhuan.contains("\"success\":true")) {
                String dt = extractJsonNumber(resLoiNhuan, "TongDoanhThu");
                String cp = extractJsonNumber(resLoiNhuan, "TongChiPhiVon");
                String chiNhap = extractJsonNumber(resLoiNhuan, "TongChiNhapKho");
                String hetHan = extractJsonNumber(resLoiNhuan, "ThietHaiHetHan");
                String ln = extractJsonNumber(resLoiNhuan, "LoiNhuanRong");

                lblTongDoanhThu.setText(dinhDangTien(dt) + " VNĐ");
                lblTongChiPhi.setText(dinhDangTien(cp) + " VNĐ");
                lblTongChiNhapKho.setText(dinhDangTien(chiNhap) + " VNĐ");
                lblThietHaiHetHan.setText(dinhDangTien(hetHan) + " VNĐ");
                lblLoiNhuanRong.setText(dinhDangTien(ln) + " VNĐ");

                // Đổi màu thẻ Lợi nhuận sang đỏ đậm nếu bị lỗ (< 0)
                if (cardLoiNhuan != null) {
                    try {
                        double valLN = Double.parseDouble(ln);
                        cardLoiNhuan.setBackground(valLN < 0 ? new Color(183, 28, 28) : new Color(0, 123, 255));
                    } catch (Exception ignored) {
                    }
                }
            }

            // 2. Tải Bảng Doanh Thu
            String loai = (String) cbLoaiBaoCao.getSelectedItem();
            String resDoanhThu = ApiClient.sendGet("/thong-ke/doanh-thu?loai=" + loai);

            tableModel.setRowCount(0);

            if (resDoanhThu.contains("\"success\":true")) {
                hienThiLenBang(resDoanhThu, loai);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu thống kê: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThiLenBang(String json, String loai) {
        String[] items = json.split("\\}\\,\\{");
        for (String item : items) {
            String thoiGian = "";
            if (loai.equals("NGAY")) {
                thoiGian = extractJsonValue(item, "Ngay");
                if (thoiGian.contains("T"))
                    thoiGian = thoiGian.split("T")[0];
            } else if (loai.equals("TUAN")) {
                thoiGian = "Năm " + extractJsonNumber(item, "Nam") + " - Tuần " + extractJsonNumber(item, "TuanThu");
            } else if (loai.equals("THANG")) {
                thoiGian = "Tháng " + extractJsonNumber(item, "Thang") + "/" + extractJsonNumber(item, "Nam");
            }

            String soHD = extractJsonNumber(item, "TongSoHoaDon");
            String doanhThu = extractJsonNumber(item, "DoanhThu");

            if (!thoiGian.isEmpty() || !soHD.isEmpty()) {
                tableModel.addRow(new Object[] { thoiGian, soHD, dinhDangTien(doanhThu) + " VNĐ" });
            }
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "";
            start += pattern.length();
            int end = json.indexOf("\"", start);
            return (end == -1) ? "" : json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    private String extractJsonNumber(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1)
                return "0";
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
            return "0";
        }
    }

    private String dinhDangTien(String valStr) {
        try {
            double val = Double.parseDouble(valStr);
            return String.format("%,.0f", val);
        } catch (Exception e) {
            return "0";
        }
    }
}