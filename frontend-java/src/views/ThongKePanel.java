package views;

import utils.ApiClient;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    // Class lưu từng dòng chi tiết hóa đơn để xuất Excel
    private static class DongChiTietHD {
        String maHD;
        String ngayLap;
        String tenNV;
        String tenKH;
        long tongTienHD;
        String tenThuoc;
        String dvt;
        String soLo;
        String hsd;
        int soLuong;
        long giaBan;
        long thanhTien;
    }

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

        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFilter.add(new JLabel("Thống kê theo:"));

        cbLoaiBaoCao = new JComboBox<>(new String[] { "NGAY", "TUAN", "THANG" });
        panelFilter.add(cbLoaiBaoCao);

        JButton btnTaiLai = new JButton("Tải dữ liệu");
        btnTaiLai.setBackground(new Color(108, 117, 125));
        btnTaiLai.setForeground(Color.WHITE);
        btnTaiLai.setFocusPainted(false);
        panelFilter.add(btnTaiLai);

        // NÚT MỚI: XUẤT EXCEL CHI TIẾT HÓA ĐƠN
        JButton btnXuatExcel = new JButton("Xuất Excel chi tiết");
        btnXuatExcel.setBackground(new Color(33, 136, 56));
        btnXuatExcel.setForeground(Color.WHITE);
        btnXuatExcel.setFont(new Font("Arial", Font.BOLD, 12));
        btnXuatExcel.setFocusPainted(false);
        panelFilter.add(btnXuatExcel);

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
        btnXuatExcel.addActionListener(e -> moHopThoaiXuatExcel());

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

    // =========================================================================
    // CHỨC NĂNG XUẤT EXCEL CHI TIẾT HÓA ĐƠN THEO NGÀY / THÁNG / NĂM
    // =========================================================================
    private void moHopThoaiXuatExcel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JComboBox<String> cboDinhDang = new JComboBox<>(new String[] {
                "Theo Ngày (dd/MM/yyyy)",
                "Theo Tháng (MM/yyyy)",
                "Theo Năm (yyyy)"
        });
        JTextField txtGiaTri = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JLabel lblGoiY = new JLabel("Ví dụ: 27/11/2026");
        lblGoiY.setFont(new Font("Arial", Font.ITALIC, 11));
        lblGoiY.setForeground(Color.DARK_GRAY);

        cboDinhDang.addActionListener(e -> {
            int idx = cboDinhDang.getSelectedIndex();
            LocalDate now = LocalDate.now();
            if (idx == 0) {
                txtGiaTri.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                lblGoiY.setText("Định dạng: dd/MM/yyyy (VD: 27/11/2026)");
            } else if (idx == 1) {
                txtGiaTri.setText(now.format(DateTimeFormatter.ofPattern("MM/yyyy")));
                lblGoiY.setText("Định dạng: MM/yyyy (VD: 11/2026)");
            } else {
                txtGiaTri.setText(String.valueOf(now.getYear()));
                lblGoiY.setText("Định dạng: yyyy (VD: 2026)");
            }
        });

        panel.add(new JLabel("Chọn định dạng báo cáo:"));
        panel.add(cboDinhDang);
        panel.add(new JLabel("Nhập thời gian cần xuất:"));
        panel.add(txtGiaTri);
        panel.add(new JLabel("Hướng dẫn:"));
        panel.add(lblGoiY);

        int res = JOptionPane.showConfirmDialog(this, panel, "Xuất Báo Cáo Chi Tiết Hóa Đơn (.xlsx)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        int idx = cboDinhDang.getSelectedIndex();
        String input = txtGiaTri.getText().trim();
        String loaiParam;
        String giaTriParam;
        String nhanThoiGian;
        String tenFileMacDinh;

        try {
            if (idx == 0) {
                // dd/MM/yyyy -> yyyy-MM-dd
                LocalDate d = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                loaiParam = "NGAY";
                giaTriParam = d.toString();
                nhanThoiGian = "Ngày " + input;
                tenFileMacDinh = "BaoCao_HoaDon_Ngay_" + input.replace("/", "-") + ".xlsx";
            } else if (idx == 1) {
                // MM/yyyy -> yyyy-MM
                String[] parts = input.split("/");
                if (parts.length != 2) throw new IllegalArgumentException();
                int thang = Integer.parseInt(parts[0]);
                int nam = Integer.parseInt(parts[1]);
                if (thang < 1 || thang > 12 || nam < 2000) throw new IllegalArgumentException();
                loaiParam = "THANG";
                giaTriParam = String.format("%04d-%02d", nam, thang);
                nhanThoiGian = "Tháng " + String.format("%02d/%04d", thang, nam);
                tenFileMacDinh = "BaoCao_HoaDon_Thang_" + String.format("%02d-%04d", thang, nam) + ".xlsx";
            } else {
                // yyyy
                int nam = Integer.parseInt(input);
                if (nam < 2000 || nam > 2100) throw new IllegalArgumentException();
                loaiParam = "NAM";
                giaTriParam = String.valueOf(nam);
                nhanThoiGian = "Năm " + nam;
                tenFileMacDinh = "BaoCao_HoaDon_Nam_" + nam + ".xlsx";
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Thời gian nhập không đúng định dạng yêu cầu!",
                    "Sai định dạng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String json = ApiClient.sendGet("/thong-ke/xuat-excel?loai=" + loaiParam + "&giaTri=" + giaTriParam);
            if (!json.contains("\"success\":true")) {
                JOptionPane.showMessageDialog(this, "Không lấy được dữ liệu từ server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<DongChiTietHD> danhSach = parseChiTietHoaDon(json);
            if (danhSach.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có hóa đơn nào phát sinh trong " + nhanThoiGian + ".",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Lưu báo cáo chi tiết hóa đơn");
            fc.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
            fc.setSelectedFile(new File(tenFileMacDinh));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getParentFile(), file.getName() + ".xlsx");
            }

            ghiFileExcelBaoCao(file, nhanThoiGian, danhSach);

            int moFile = JOptionPane.showConfirmDialog(this,
                    "Xuất thành công " + danhSach.size() + " dòng chi tiết hóa đơn!\nĐường dẫn: "
                            + file.getAbsolutePath() + "\n\nBạn có muốn mở file ngay không?",
                    "Xuất Excel thành công", JOptionPane.YES_NO_OPTION);
            if (moFile == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<DongChiTietHD> parseChiTietHoaDon(String json) {
        List<DongChiTietHD> list = new ArrayList<>();
        int dataIdx = json.indexOf("\"data\":");
        if (dataIdx == -1) return list;

        String dataPart = json.substring(dataIdx);
        Matcher m = Pattern.compile("\\{([^}]+)\\}").matcher(dataPart);
        while (m.find()) {
            String obj = m.group(1);
            DongChiTietHD d = new DongChiTietHD();
            d.maHD = extractJsonNumber(obj, "MaHD");
            d.ngayLap = extractJsonValue(obj, "NgayLap");
            d.tenNV = extractJsonValue(obj, "TenNV");
            d.tenKH = extractJsonValue(obj, "TenKH");
            d.tongTienHD = new BigDecimal(extractJsonNumber(obj, "TongTienHD")).longValue();
            d.tenThuoc = extractJsonValue(obj, "TenThuoc");
            d.dvt = extractJsonValue(obj, "DonViTinh");
            d.soLo = extractJsonValue(obj, "SoLo");
            d.hsd = extractJsonValue(obj, "HanSuDung");
            d.soLuong = Integer.parseInt(extractJsonNumber(obj, "SoLuongBan"));
            d.giaBan = new BigDecimal(extractJsonNumber(obj, "GiaBan")).longValue();
            d.thanhTien = new BigDecimal(extractJsonNumber(obj, "ThanhTien")).longValue();
            list.add(d);
        }
        return list;
    }

    // ----- Đóng gói file .xlsx chuẩn SpreadsheetML không cần thư viện ngoài -----
    private static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String cellStr(String ref, String text, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + xmlEscape(text) + "</t></is></c>";
    }

    private static String cellNum(String ref, long value, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\"><v>" + value + "</v></c>";
    }

    private static String cellEmpty(String ref, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\"/>";
    }

    private static String rowXml(int r, String cells) {
        return "<row r=\"" + r + "\">" + cells + "</row>";
    }

    private static void writeZipEntry(ZipOutputStream z, String name, String content) throws IOException {
        z.putNextEntry(new ZipEntry(name));
        z.write(content.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private void ghiFileExcelBaoCao(File file, String nhanThoiGian, List<DongChiTietHD> ds) throws IOException {
        final String HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        final String NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        final String NS_PKG = "http://schemas.openxmlformats.org/package/2006/relationships";

        // Đếm số hóa đơn duy nhất & tổng doanh thu kỳ
        long tongDoanhThuKy = 0;
        List<String> dsMaHD = new ArrayList<>();
        for (DongChiTietHD d : ds) {
            tongDoanhThuKy += d.thanhTien;
            if (!dsMaHD.contains(d.maHD)) {
                dsMaHD.add(d.maHD);
            }
        }

        StringBuilder sd = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sd.append(rowXml(1, cellStr("A1", "BÁO CÁO CHI TIẾT HÓA ĐƠN BÁN HÀNG", 1)));
        merges.add("A1:L1");

        sd.append(rowXml(2, cellStr("A2", "Kỳ báo cáo: " + nhanThoiGian + "  |  Tổng số hóa đơn: " + dsMaHD.size()
                + "  |  Tổng doanh thu: " + String.format("%,d", tongDoanhThuKy) + " VNĐ", 2)));
        merges.add("A2:L2");

        int rHead = 4;
        sd.append(rowXml(rHead,
                cellStr("A4", "STT", 3)
                        + cellStr("B4", "Mã HD", 3)
                        + cellStr("C4", "Ngày lập", 3)
                        + cellStr("D4", "Nhân viên", 3)
                        + cellStr("E4", "Khách hàng", 3)
                        + cellStr("F4", "Tên thuốc", 3)
                        + cellStr("G4", "ĐVT", 3)
                        + cellStr("H4", "Số lô", 3)
                        + cellStr("I4", "Hạn dùng", 3)
                        + cellStr("J4", "SL bán", 3)
                        + cellStr("K4", "Đơn giá (đ)", 3)
                        + cellStr("L4", "Thành tiền (đ)", 3)));

        int r = rHead + 1;
        int stt = 1;
        for (DongChiTietHD d : ds) {
            sd.append(rowXml(r,
                    cellNum("A" + r, stt++, 6)
                            + cellStr("B" + r, "HD" + d.maHD, 6)
                            + cellStr("C" + r, d.ngayLap, 6)
                            + cellStr("D" + r, d.tenNV, 4)
                            + cellStr("E" + r, d.tenKH, 4)
                            + cellStr("F" + r, d.tenThuoc, 4)
                            + cellStr("G" + r, d.dvt, 6)
                            + cellStr("H" + r, d.soLo, 6)
                            + cellStr("I" + r, d.hsd, 6)
                            + cellNum("J" + r, d.soLuong, 6)
                            + cellNum("K" + r, d.giaBan, 5)
                            + cellNum("L" + r, d.thanhTien, 5)));
            r++;
        }

        int rTong = r;
        String congThuc = "SUM(L" + (rHead + 1) + ":L" + (rTong - 1) + ")";
        sd.append(rowXml(rTong,
                cellStr("A" + rTong, "TỔNG DOANH THU KỲ BÁO CÁO", 7)
                        + cellEmpty("B" + rTong, 7) + cellEmpty("C" + rTong, 7)
                        + cellEmpty("D" + rTong, 7) + cellEmpty("E" + rTong, 7)
                        + cellEmpty("F" + rTong, 7) + cellEmpty("G" + rTong, 7)
                        + cellEmpty("H" + rTong, 7) + cellEmpty("I" + rTong, 7)
                        + cellEmpty("J" + rTong, 7) + cellEmpty("K" + rTong, 7)
                        + "<c r=\"L" + rTong + "\" s=\"8\"><f>" + congThuc + "</f><v>" + tongDoanhThuKy + "</v></c>"));
        merges.add("A" + rTong + ":K" + rTong);

        StringBuilder mc = new StringBuilder("<mergeCells count=\"" + merges.size() + "\">");
        for (String m : merges) {
            mc.append("<mergeCell ref=\"").append(m).append("\"/>");
        }
        mc.append("</mergeCells>");

        String sheet = HEAD + "<worksheet xmlns=\"" + NS + "\">"
                + "<sheetViews><sheetView showGridLines=\"1\" workbookViewId=\"0\"/></sheetViews>"
                + "<sheetFormatPr defaultRowHeight=\"20\"/>"
                + "<cols>"
                + "<col min=\"1\" max=\"1\" width=\"6\" customWidth=\"1\"/>"
                + "<col min=\"2\" max=\"2\" width=\"10\" customWidth=\"1\"/>"
                + "<col min=\"3\" max=\"3\" width=\"20\" customWidth=\"1\"/>"
                + "<col min=\"4\" max=\"4\" width=\"22\" customWidth=\"1\"/>"
                + "<col min=\"5\" max=\"5\" width=\"22\" customWidth=\"1\"/>"
                + "<col min=\"6\" max=\"6\" width=\"28\" customWidth=\"1\"/>"
                + "<col min=\"7\" max=\"7\" width=\"10\" customWidth=\"1\"/>"
                + "<col min=\"8\" max=\"8\" width=\"14\" customWidth=\"1\"/>"
                + "<col min=\"9\" max=\"9\" width=\"13\" customWidth=\"1\"/>"
                + "<col min=\"10\" max=\"10\" width=\"9\" customWidth=\"1\"/>"
                + "<col min=\"11\" max=\"11\" width=\"15\" customWidth=\"1\"/>"
                + "<col min=\"12\" max=\"12\" width=\"17\" customWidth=\"1\"/>"
                + "</cols>"
                + "<sheetData>" + sd + "</sheetData>" + mc + "</worksheet>";

        String border = "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right>"
                + "<top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border>";
        String styles = HEAD + "<styleSheet xmlns=\"" + NS + "\">"
                + "<fonts count=\"3\">"
                + "<font><sz val=\"11\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"15\"/><name val=\"Arial\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E1F2\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" + border + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"9\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"/>"
                + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\"/></xf>"
                + "<xf numFmtId=\"3\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";

        String contentTypes = HEAD + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";

        String rels = HEAD + "<Relationships xmlns=\"" + NS_PKG + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_R + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";

        String workbook = HEAD + "<workbook xmlns=\"" + NS + "\" xmlns:r=\"" + NS_R + "\">"
                + "<sheets><sheet name=\"ChiTietHoaDon\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";

        String workbookRels = HEAD + "<Relationships xmlns=\"" + NS_PKG + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_R + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"" + NS_R + "/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";

        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writeZipEntry(z, "[Content_Types].xml", contentTypes);
            writeZipEntry(z, "_rels/.rels", rels);
            writeZipEntry(z, "xl/workbook.xml", workbook);
            writeZipEntry(z, "xl/_rels/workbook.xml.rels", workbookRels);
            writeZipEntry(z, "xl/styles.xml", styles);
            writeZipEntry(z, "xl/worksheets/sheet1.xml", sheet);
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