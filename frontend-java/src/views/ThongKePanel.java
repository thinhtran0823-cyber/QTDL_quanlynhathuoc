package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ThongKePanel extends JPanel {
    private JLabel lblTongDoanhThu;
    private JLabel lblTongChiPhi;
    private JLabel lblLoiNhuanRong;
    private JComboBox<String> cbLoaiBaoCao;
    private JTable tblDoanhThu;
    private DefaultTableModel tableModel;

    public ThongKePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- 1. PHẦN TRÊN: 3 THẺ TỔNG QUAN ---
        JPanel panelCards = new JPanel(new GridLayout(1, 3, 15, 0));

        lblTongDoanhThu = taoCard(panelCards, "TỔNG DOANH THU", "0 VNĐ", new Color(40, 167, 69));
        lblTongChiPhi = taoCard(panelCards, "TỔNG GIÁ VỐN", "0 VNĐ", new Color(220, 53, 69));
        lblLoiNhuanRong = taoCard(panelCards, "LỢI NHUẬN RÒNG", "0 VNĐ", new Color(0, 123, 255));

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
        tableModel = new DefaultTableModel(columnNames, 0);
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
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblValue = new JLabel(defaultValue);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Arial", Font.BOLD, 18));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        parent.add(card);
        return lblValue;
    }

    public void taiDuLieuThongKe() {
        try {
            // 1. Tải Lợi nhuận
            String resLoiNhuan = ApiClient.sendGet("/thong-ke/loi-nhuan");
            if (resLoiNhuan.contains("\"success\":true")) {
                String dt = extractJsonNumber(resLoiNhuan, "TongDoanhThu");
                String cp = extractJsonNumber(resLoiNhuan, "TongChiPhiVon");
                String ln = extractJsonNumber(resLoiNhuan, "LoiNhuanRong");

                lblTongDoanhThu.setText(dinhDangTien(dt) + " VNĐ");
                lblTongChiPhi.setText(dinhDangTien(cp) + " VNĐ");
                lblLoiNhuanRong.setText(dinhDangTien(ln) + " VNĐ");
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