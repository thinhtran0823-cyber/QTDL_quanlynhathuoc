package views;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * MODULE 3: POS BÁN HÀNG & HÓA ĐƠN
 *
 * - KHÔNG kết nối MySQL trực tiếp: mọi dữ liệu đều lấy qua backend Node.js
 * (GET /thuoc, /khach-hang, /nhan-vien/:tenDangNhap, /hoa-don).
 * - Việc tạo hóa đơn (trừ kho theo FEFO, transaction...) do backend Node.js xử
 * lý:
 * POST {API_BAN_HANG} body: { MaNV, MaKH?, chiTiet:[{MaThuoc, SoLuongBan}] }
 * GET {API_BAN_HANG} -> lịch sử hóa đơn; GET {API_BAN_HANG}/{id} -> chi tiết
 *
 * Cách gắn vào MainDashboard:
 * 
 */
public class BanHangPanel extends JPanel {

    // ====== CẤU HÌNH – chỉ cần địa chỉ backend, không có thông tin database ======
    private static final String API_GOC = "http://localhost:5000/api/ban-hang";
    private static final String API_BAN_HANG = API_GOC + "/hoa-don";
    // =======================================================

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final DecimalFormat MONEY;
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        MONEY = new DecimalFormat("#,##0", s);
    }

    // Khớp từng dòng chi tiết (mỗi lô) trong JSON trả về của backend
    // Nhóm: 1=TenThuoc 2=SoLo 3=HanSuDung 4=SoLuongBan 5=GiaBan 6=ThanhTien
    private static final Pattern LO_PATTERN = Pattern.compile(
            "\"TenThuoc\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,\\s*\"MaLo\"\\s*:\\s*\\d+\\s*,"
                    + "\\s*\"SoLo\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,\\s*\"HanSuDung\"\\s*:\\s*\"([^\"]*)\"\\s*,"
                    + "\\s*\"SoLuongBan\"\\s*:\\s*(\\d+)\\s*,\\s*\"GiaBan\"\\s*:\\s*(\\d+(?:\\.\\d+)?)"
                    + "\\s*,\\s*\"ThanhTien\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ---------- Dữ liệu ----------
    private static class Thuoc {
        int ma;
        String ten;
        String dvt;
        long giaBan;
        int tonConHan;
    }

    private static class GioHang {
        Thuoc thuoc;
        int soLuong;
    }

    private static class KhachHangItem {
        Integer ma; // null = khách vãng lai
        String ten;

        KhachHangItem(Integer ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    // Một dòng trên hóa đơn (mỗi lô xuất kho là một dòng)
    private static class DongHoaDon {
        String tenThuoc;
        String soLo;
        String hanDung;
        int soLuong;
        long giaBan;
        long thanhTien;
    }

    // Hóa đơn vừa lập, dùng để xuất Excel
    private static class HoaDonKQ {
        int maHD;
        LocalDateTime ngayLap;
        String tenNV;
        String tenKH;
        long tongTien;
        final List<DongHoaDon> dong = new ArrayList<>();
    }

    private final String tenDangNhap;
    private int maNV = -1;
    private String tenNV = "";
    private HoaDonKQ hoaDonCuoi;

    private final List<Thuoc> dsThuoc = new ArrayList<>();
    private final Map<Integer, GioHang> gioHang = new LinkedHashMap<>();

    // ---------- Component ----------
    private JTextField txtTimKiem;
    private JTable tblThuoc;
    private DefaultTableModel modelThuoc;
    private TableRowSorter<DefaultTableModel> sorterThuoc;
    private JSpinner spnSoLuong;

    private JComboBox<KhachHangItem> cboKhachHang;
    private JTable tblGio;
    private DefaultTableModel modelGio;
    private JLabel lblTongTien;
    private JButton btnThanhToan;
    private JButton btnXuatExcel;
    private JLabel lblNV;

    // Tab lịch sử hóa đơn
    private JTable tblLichSu;
    private DefaultTableModel modelLichSu;
    private JButton btnTaiLichSu;

    public BanHangPanel(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(taoHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoPanelThuoc(), taoPanelGioHang());
        split.setResizeWeight(0.5);
        split.setBorder(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bán hàng", split);
        tabs.addTab("Lịch sử hóa đơn", taoPanelLichSu());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                taiLichSu();
            }
        });
        add(tabs, BorderLayout.CENTER);

        taiDuLieu();
        taiNhanVien();
    }

    // GIAO DIỆN

    private JPanel taoHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("BÁN HÀNG (POS)");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));

        lblNV = new JLabel("Đang tải thông tin nhân viên...");
        lblNV.setFont(new Font("Arial", Font.ITALIC, 12));
        lblNV.setForeground(Color.DARK_GRAY);

        p.add(lbl, BorderLayout.WEST);
        p.add(lblNV, BorderLayout.EAST);
        return p;
    }

    private JPanel taoPanelThuoc() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createTitledBorder("Danh sách thuốc"));

        // Thanh tìm kiếm
        JPanel pTop = new JPanel(new BorderLayout(6, 0));
        pTop.add(new JLabel("Tìm thuốc:"), BorderLayout.WEST);
        txtTimKiem = new JTextField();
        pTop.add(txtTimKiem, BorderLayout.CENTER);
        JButton btnTaiLai = new JButton("Tải lại");
        btnTaiLai.addActionListener(e -> taiDuLieu());
        pTop.add(btnTaiLai, BorderLayout.EAST);
        p.add(pTop, BorderLayout.NORTH);

        // Bảng thuốc
        modelThuoc = new DefaultTableModel(
                new Object[] { "Mã", "Tên thuốc", "ĐVT", "Giá bán", "Tồn (còn hạn)" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblThuoc = new JTable(modelThuoc);
        tblThuoc.setRowHeight(24);
        tblThuoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorterThuoc = new TableRowSorter<>(modelThuoc);
        tblThuoc.setRowSorter(sorterThuoc);
        canPhai(tblThuoc, 3, 4);
        tblThuoc.getColumnModel().getColumn(0).setMaxWidth(50);
        tblThuoc.getColumnModel().getColumn(1).setPreferredWidth(180);
        p.add(new JScrollPane(tblThuoc), BorderLayout.CENTER);

        // Lọc khi gõ
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            private void loc() {
                String t = txtTimKiem.getText().trim();
                sorterThuoc.setRowFilter(t.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(t), 1));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                loc();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loc();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loc();
            }
        });

        // Nhấp đúp để thêm vào giỏ
        tblThuoc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    themVaoGio();
                }
            }
        });

        // Thanh thêm vào giỏ
        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pBottom.add(new JLabel("Số lượng:"));
        spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        spnSoLuong.setPreferredSize(new Dimension(70, 28));
        pBottom.add(spnSoLuong);
        JButton btnThem = new JButton("Thêm vào giỏ  ▶");
        btnThem.addActionListener(e -> themVaoGio());
        pBottom.add(btnThem);
        p.add(pBottom, BorderLayout.SOUTH);

        return p;
    }

    private JPanel taoPanelGioHang() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createTitledBorder("Giỏ hàng / Hóa đơn"));

        // Khách hàng
        JPanel pTop = new JPanel(new BorderLayout(6, 0));
        pTop.add(new JLabel("Khách hàng:"), BorderLayout.WEST);
        cboKhachHang = new JComboBox<>();
        pTop.add(cboKhachHang, BorderLayout.CENTER);
        p.add(pTop, BorderLayout.NORTH);

        // Bảng giỏ
        modelGio = new DefaultTableModel(
                new Object[] { "Mã", "Tên thuốc", "SL", "Đơn giá", "Thành tiền" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblGio = new JTable(modelGio);
        tblGio.setRowHeight(24);
        tblGio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        canPhai(tblGio, 2, 3, 4);
        tblGio.getColumnModel().getColumn(0).setMaxWidth(50);
        tblGio.getColumnModel().getColumn(2).setMaxWidth(50);
        tblGio.getColumnModel().getColumn(1).setPreferredWidth(160);
        p.add(new JScrollPane(tblGio), BorderLayout.CENTER);

        // Tổng tiền + nút
        JPanel pBottom = new JPanel(new BorderLayout(0, 6));

        JPanel pTong = new JPanel(new BorderLayout());
        JLabel lblTxt = new JLabel("TỔNG TIỀN:");
        lblTxt.setFont(new Font("Arial", Font.BOLD, 15));
        lblTongTien = new JLabel("0 đ");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
        lblTongTien.setForeground(new Color(200, 30, 30));
        pTong.add(lblTxt, BorderLayout.WEST);
        pTong.add(lblTongTien, BorderLayout.EAST);
        pBottom.add(pTong, BorderLayout.NORTH);

        JPanel pBtn = new JPanel(new GridLayout(1, 3, 6, 0));
        JButton btnXoaDong = new JButton("Xóa dòng");
        JButton btnXoaHet = new JButton("Xóa hết");
        btnThanhToan = new JButton("THANH TOÁN");
        btnThanhToan.setFont(new Font("Arial", Font.BOLD, 13));
        btnThanhToan.setBackground(new Color(40, 140, 70));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setOpaque(true);
        btnXoaDong.addActionListener(e -> xoaDong());
        btnXoaHet.addActionListener(e -> {
            gioHang.clear();
            capNhatGio();
        });
        btnThanhToan.addActionListener(e -> thanhToan());
        pBtn.add(btnXoaDong);
        pBtn.add(btnXoaHet);
        pBtn.add(btnThanhToan);

        btnXuatExcel = new JButton("Xuất Excel hóa đơn vừa lập");
        btnXuatExcel.setEnabled(false);
        btnXuatExcel.addActionListener(e -> xuatExcel(hoaDonCuoi));

        JPanel pBtnAll = new JPanel(new GridLayout(2, 1, 0, 6));
        pBtnAll.add(pBtn);
        pBtnAll.add(btnXuatExcel);
        pBottom.add(pBtnAll, BorderLayout.SOUTH);

        p.add(pBottom, BorderLayout.SOUTH);
        return p;
    }

    private void canPhai(JTable table, int... cols) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cols) {
            table.getColumnModel().getColumn(c).setCellRenderer(r);
        }
    }

    // =========================================================
    // DỮ LIỆU – lấy qua API backend (không truy cập DB trực tiếp)
    // =========================================================
    // Mỗi phần tử: {
    // "MaThuoc":1,"TenThuoc":"..","DonViTinh":"..","GiaBan":65000,"TonConHan":10 }
    // Nhóm: 1=MaThuoc 2=TenThuoc 3=DonViTinh 4=GiaBan 5=TonConHan
    private static final Pattern THUOC_PATTERN = Pattern.compile(
            "\\{\\s*\"MaThuoc\"\\s*:\\s*(\\d+)\\s*,\\s*\"TenThuoc\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,"
                    + "\\s*\"DonViTinh\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,"
                    + "\\s*\"GiaBan\"\\s*:\\s*(\\d+(?:\\.\\d+)?)\\s*,\\s*\"TonConHan\"\\s*:\\s*(\\d+)\\s*\\}");

    // Nhóm: 1=MaKH 2=TenKH 3=SoDienThoai
    private static final Pattern KH_PATTERN = Pattern.compile(
            "\\{\\s*\"MaKH\"\\s*:\\s*(\\d+)\\s*,\\s*\"TenKH\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,"
                    + "\\s*\"SoDienThoai\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\}");

    private void taiDuLieu() {
        taiDanhSachThuoc();
        taiDanhSachKhachHang();
    }

    // Đổi tên đăng nhập -> mã nhân viên (cần cho việc lập hóa đơn)
    private void taiNhanVien() {
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                String dn = URLEncoder.encode(tenDangNhap, StandardCharsets.UTF_8);
                return goiGet(API_GOC + "/nhan-vien/" + dn);
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> res = get();
                    String json = res.body() == null ? "" : res.body();
                    String ma = timSo(json, "MaNV");
                    if (res.statusCode() == 200 && ma != null) {
                        maNV = Integer.parseInt(ma);
                        String ten = timChuoi(json, "HoTen");
                        tenNV = ten == null ? "" : ten;
                        lblNV.setText("Nhân viên: " + tenNV + " (" + tenDangNhap + ")");
                        lblNV.setForeground(Color.DARK_GRAY);
                    } else {
                        lblNV.setText("Không xác định được nhân viên từ tài khoản '" + tenDangNhap + "'");
                        lblNV.setForeground(Color.RED);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lblNV.setText("Không kết nối được server để xác định nhân viên");
                    lblNV.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void taiDanhSachThuoc() {
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                return goiGet(API_GOC + "/thuoc");
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> res = get();
                    String json = res.body() == null ? "" : res.body();
                    if (res.statusCode() != 200) {
                        String msg = timChuoi(json, "message");
                        JOptionPane.showMessageDialog(BanHangPanel.this,
                                "Không tải được danh sách thuốc:\n"
                                        + (msg != null ? msg : "HTTP " + res.statusCode()),
                                "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    dsThuoc.clear();
                    modelThuoc.setRowCount(0);
                    Matcher m = THUOC_PATTERN.matcher(json);
                    while (m.find()) {
                        Thuoc t = new Thuoc();
                        t.ma = Integer.parseInt(m.group(1));
                        t.ten = giaiMa(m.group(2));
                        t.dvt = giaiMa(m.group(3));
                        t.giaBan = new BigDecimal(m.group(4)).longValue();
                        t.tonConHan = Integer.parseInt(m.group(5));
                        dsThuoc.add(t);
                        modelThuoc.addRow(new Object[] {
                                t.ma, t.ten, t.dvt, MONEY.format(t.giaBan), t.tonConHan });
                    }
                } catch (Exception ex) {
                    baoLoiKetNoi(ex);
                }
            }
        }.execute();
    }

    private void taiDanhSachKhachHang() {
        final KhachHangItem dangChon = (KhachHangItem) cboKhachHang.getSelectedItem();

        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                return goiGet(API_GOC + "/khach-hang");
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> res = get();
                    String json = res.body() == null ? "" : res.body();

                    cboKhachHang.removeAllItems();
                    cboKhachHang.addItem(new KhachHangItem(null, "— Khách vãng lai —"));

                    if (res.statusCode() != 200) {
                        String msg = timChuoi(json, "message");
                        JOptionPane.showMessageDialog(BanHangPanel.this,
                                "Không tải được danh sách khách hàng:\n"
                                        + (msg != null ? msg : "HTTP " + res.statusCode()),
                                "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Matcher m = KH_PATTERN.matcher(json);
                    while (m.find()) {
                        String sdt = giaiMa(m.group(3));
                        String ten = giaiMa(m.group(2)) + (!sdt.isBlank() ? " - " + sdt : "");
                        cboKhachHang.addItem(new KhachHangItem(Integer.parseInt(m.group(1)), ten));
                    }

                    // Giữ lại khách đang chọn (nếu có)
                    if (dangChon != null && dangChon.ma != null) {
                        for (int i = 0; i < cboKhachHang.getItemCount(); i++) {
                            if (dangChon.ma.equals(cboKhachHang.getItemAt(i).ma)) {
                                cboKhachHang.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    baoLoiKetNoi(ex);
                }
            }
        }.execute();
    }

    // =========================================================
    // GIỎ HÀNG
    // =========================================================
    private void themVaoGio() {
        int viewRow = tblThuoc.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một loại thuốc trong danh sách.");
            return;
        }
        Thuoc t = dsThuoc.get(tblThuoc.convertRowIndexToModel(viewRow));
        int them = (Integer) spnSoLuong.getValue();

        GioHang g = gioHang.get(t.ma);
        int tong = (g == null ? 0 : g.soLuong) + them;

        if (tong > t.tonConHan) {
            JOptionPane.showMessageDialog(this,
                    "Thuốc \"" + t.ten + "\" chỉ còn " + t.tonConHan + " " + t.dvt
                            + " (còn hạn)\nTrong giỏ đã có " + (g == null ? 0 : g.soLuong) + ".",
                    "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (g == null) {
            g = new GioHang();
            g.thuoc = t;
            gioHang.put(t.ma, g);
        }
        g.soLuong = tong;
        spnSoLuong.setValue(1);
        capNhatGio();
    }

    private void xoaDong() {
        int row = tblGio.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trong giỏ hàng.");
            return;
        }
        int maThuoc = (Integer) modelGio.getValueAt(row, 0);
        gioHang.remove(maThuoc);
        capNhatGio();
    }

    private long tinhTong() {
        long tong = 0;
        for (GioHang g : gioHang.values()) {
            tong += g.soLuong * g.thuoc.giaBan;
        }
        return tong;
    }

    private void capNhatGio() {
        modelGio.setRowCount(0);
        for (GioHang g : gioHang.values()) {
            modelGio.addRow(new Object[] {
                    g.thuoc.ma,
                    g.thuoc.ten,
                    g.soLuong,
                    MONEY.format(g.thuoc.giaBan),
                    MONEY.format(g.soLuong * g.thuoc.giaBan) });
        }
        lblTongTien.setText(MONEY.format(tinhTong()) + " đ");
    }

    // =========================================================
    // THANH TOÁN – gọi backend Node.js
    // =========================================================
    private String taoJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"MaNV\":").append(maNV);

        KhachHangItem kh = (KhachHangItem) cboKhachHang.getSelectedItem();
        if (kh != null && kh.ma != null) {
            sb.append(",\"MaKH\":").append(kh.ma);
        }

        sb.append(",\"chiTiet\":[");
        boolean dau = true;
        for (GioHang g : gioHang.values()) {
            if (!dau) {
                sb.append(",");
            }
            dau = false;
            sb.append("{\"MaThuoc\":").append(g.thuoc.ma)
                    .append(",\"SoLuongBan\":").append(g.soLuong).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private void thanhToan() {
        if (maNV <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Không xác định được nhân viên đang đăng nhập nên không thể lập hóa đơn.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán " + MONEY.format(tinhTong()) + " đ ?",
                "Thanh toán", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        final String body = taoJson();
        btnThanhToan.setEnabled(false);
        btnThanhToan.setText("Đang xử lý...");

        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                HttpRequest req = HttpRequest.newBuilder(URI.create(API_BAN_HANG))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            }

            @Override
            protected void done() {
                btnThanhToan.setEnabled(true);
                btnThanhToan.setText("THANH TOÁN");
                try {
                    xuLyKetQua(get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(BanHangPanel.this,
                            "Không kết nối được tới server bán hàng:\n" + API_BAN_HANG
                                    + "\n\nHãy kiểm tra backend Node.js đã chạy chưa.",
                            "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void xuLyKetQua(HttpResponse<String> res) {
        String json = res.body() == null ? "" : res.body();
        boolean thanhCong = res.statusCode() == 201
                && Pattern.compile("\"success\"\\s*:\\s*true").matcher(json).find();

        if (!thanhCong) {
            String msg = timChuoi(json, "message");
            JOptionPane.showMessageDialog(this,
                    msg != null ? msg : "Server trả về lỗi (HTTP " + res.statusCode() + ")",
                    "Tạo hóa đơn thất bại", JOptionPane.ERROR_MESSAGE);
            taiDanhSachThuoc(); // tồn kho có thể đã thay đổi
            return;
        }

        HoaDonKQ hd = new HoaDonKQ();
        String maHD = timSo(json, "MaHD");
        String tong = timSo(json, "TongTien");
        hd.maHD = maHD != null ? Integer.parseInt(maHD) : 0;
        hd.tongTien = tong != null ? new BigDecimal(tong).longValue() : tinhTong();
        hd.tenNV = tenNV;
        KhachHangItem kh = (KhachHangItem) cboKhachHang.getSelectedItem();
        hd.tenKH = (kh != null && kh.ma != null) ? kh.ten : "Khách vãng lai";
        try {
            hd.ngayLap = LocalDateTime.parse(timChuoi(json, "NgayLap").replace(' ', 'T'));
        } catch (Exception ex) {
            hd.ngayLap = LocalDateTime.now();
        }

        Matcher m = LO_PATTERN.matcher(json);
        while (m.find()) {
            DongHoaDon d = new DongHoaDon();
            d.tenThuoc = giaiMa(m.group(1));
            d.soLo = giaiMa(m.group(2));
            d.hanDung = dinhDangHan(m.group(3));
            d.soLuong = Integer.parseInt(m.group(4));
            d.giaBan = new BigDecimal(m.group(5)).longValue();
            d.thanhTien = new BigDecimal(m.group(6)).longValue();
            hd.dong.add(d);
        }
        hoaDonCuoi = hd;
        btnXuatExcel.setEnabled(true);

        StringBuilder sb = new StringBuilder();
        sb.append("Tạo hóa đơn thành công!\n\n");
        sb.append("Mã hóa đơn: ").append(hd.maHD).append("\n");
        sb.append("Khách hàng: ").append(hd.tenKH).append("\n");
        sb.append("Tổng tiền: ").append(MONEY.format(hd.tongTien)).append(" đ\n\n");
        sb.append("Chi tiết xuất kho (theo hạn dùng gần nhất):\n");
        for (DongHoaDon d : hd.dong) {
            sb.append("  • ").append(d.tenThuoc).append(" – lô ").append(d.soLo)
                    .append(" × ").append(d.soLuong).append("\n");
        }

        // Làm mới giỏ và tồn kho trước khi hiện hộp thoại
        gioHang.clear();
        capNhatGio();
        cboKhachHang.setSelectedIndex(0);
        taiDanhSachThuoc();

        Object[] lc = { "Xuất Excel", "Đóng" };
        int chon = JOptionPane.showOptionDialog(this, sb.toString(), "Hóa đơn",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, lc, lc[0]);
        if (chon == 0) {
            xuatExcel(hd);
        }
    }

    // Backend trả HanSuDung dạng ISO (vd 2026-12-30T17:00:00.000Z) → đổi về ngày
    // theo múi giờ máy
    private static String dinhDangHan(String raw) {
        try {
            return Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDate().format(NGAY);
        } catch (Exception ignore) {
            // thử dạng yyyy-MM-dd
        }
        try {
            return LocalDate.parse(raw.substring(0, 10)).format(NGAY);
        } catch (Exception ex) {
            return raw;
        }
    }

    // =========================================================
    // LỊCH SỬ HÓA ĐƠN – GET /api/ban-hang/hoa-don
    // =========================================================
    // Khớp từng hóa đơn trong danh sách (thứ tự khóa do backend cố định)
    // Nhóm: 1=MaHD 2=NgayLap 3=TenNV 4=TenKH 5=TongTien
    private static final Pattern HD_PATTERN = Pattern.compile(
            "\\{\\s*\"MaHD\"\\s*:\\s*(\\d+)\\s*,\\s*\"NgayLap\"\\s*:\\s*\"([^\"]*)\"\\s*,"
                    + "\\s*\"TenNV\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,"
                    + "\\s*\"TenKH\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*,"
                    + "\\s*\"TongTien\"\\s*:\\s*(\\d+(?:\\.\\d+)?)\\s*\\}");

    private JPanel taoPanelLichSu() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createTitledBorder("Lịch sử hóa đơn bán hàng"));

        modelLichSu = new DefaultTableModel(
                new Object[] { "Mã HD", "Ngày lập", "Nhân viên", "Khách hàng", "Tổng tiền" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLichSu = new JTable(modelLichSu);
        tblLichSu.setRowHeight(24);
        tblLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        canPhai(tblLichSu, 0, 4);
        tblLichSu.getColumnModel().getColumn(0).setMaxWidth(70);
        tblLichSu.getColumnModel().getColumn(1).setPreferredWidth(140);
        p.add(new JScrollPane(tblLichSu), BorderLayout.CENTER);

        // Nhấp đúp để xem chi tiết
        tblLichSu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemChiTietDangChon();
                }
            }
        });

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        btnTaiLichSu = new JButton("Tải lại");
        btnTaiLichSu.addActionListener(e -> taiLichSu());
        JButton btnXem = new JButton("Xem chi tiết / Xuất Excel");
        btnXem.addActionListener(e -> xemChiTietDangChon());
        pBtn.add(btnTaiLichSu);
        pBtn.add(btnXem);
        p.add(pBtn, BorderLayout.SOUTH);
        return p;
    }

    private static HttpResponse<String> goiGet(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String dinhDangNgayGio(String raw) {
        try {
            return LocalDateTime.parse(raw.replace(' ', 'T')).format(NGAY_GIO);
        } catch (Exception ex) {
            return raw;
        }
    }

    private void baoLoiKetNoi(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Không kết nối được tới server bán hàng:\n" + API_BAN_HANG
                        + "\n\nHãy kiểm tra backend Node.js đã chạy chưa.",
                "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
    }

    private void taiLichSu() {
        btnTaiLichSu.setEnabled(false);
        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                return goiGet(API_BAN_HANG);
            }

            @Override
            protected void done() {
                btnTaiLichSu.setEnabled(true);
                try {
                    HttpResponse<String> res = get();
                    String json = res.body() == null ? "" : res.body();
                    if (res.statusCode() != 200) {
                        String msg = timChuoi(json, "message");
                        JOptionPane.showMessageDialog(BanHangPanel.this,
                                msg != null ? msg : "Server trả về lỗi (HTTP " + res.statusCode() + ")",
                                "Không tải được lịch sử", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    modelLichSu.setRowCount(0);
                    Matcher m = HD_PATTERN.matcher(json);
                    while (m.find()) {
                        modelLichSu.addRow(new Object[] {
                                Integer.parseInt(m.group(1)),
                                dinhDangNgayGio(m.group(2)),
                                giaiMa(m.group(3)),
                                giaiMa(m.group(4)),
                                MONEY.format(new BigDecimal(m.group(5)).longValue()) + " đ" });
                    }
                } catch (Exception ex) {
                    baoLoiKetNoi(ex);
                }
            }
        }.execute();
    }

    private void xemChiTietDangChon() {
        int row = tblLichSu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong danh sách.");
            return;
        }
        final int maHD = (Integer) modelLichSu.getValueAt(row, 0);

        new SwingWorker<HttpResponse<String>, Void>() {
            @Override
            protected HttpResponse<String> doInBackground() throws Exception {
                return goiGet(API_BAN_HANG + "/" + maHD);
            }

            @Override
            protected void done() {
                try {
                    HttpResponse<String> res = get();
                    String json = res.body() == null ? "" : res.body();
                    if (res.statusCode() != 200) {
                        String msg = timChuoi(json, "message");
                        JOptionPane.showMessageDialog(BanHangPanel.this,
                                msg != null ? msg : "Server trả về lỗi (HTTP " + res.statusCode() + ")",
                                "Không xem được hóa đơn", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    hienChiTietHoaDon(docHoaDonTuJson(json));
                } catch (Exception ex) {
                    baoLoiKetNoi(ex);
                }
            }
        }.execute();
    }

    // Dựng HoaDonKQ từ JSON chi tiết (cùng định dạng dòng với lúc tạo hóa đơn)
    private HoaDonKQ docHoaDonTuJson(String json) {
        HoaDonKQ hd = new HoaDonKQ();
        String ma = timSo(json, "MaHD");
        String tong = timSo(json, "TongTien");
        String ngay = timChuoi(json, "NgayLap");
        hd.maHD = ma != null ? Integer.parseInt(ma) : 0;
        hd.tongTien = tong != null ? new BigDecimal(tong).longValue() : 0;
        hd.tenNV = timChuoi(json, "TenNV");
        hd.tenKH = timChuoi(json, "TenKH");
        try {
            hd.ngayLap = LocalDateTime.parse(ngay.replace(' ', 'T'));
        } catch (Exception ex) {
            hd.ngayLap = LocalDateTime.now();
        }
        Matcher m = LO_PATTERN.matcher(json);
        while (m.find()) {
            DongHoaDon d = new DongHoaDon();
            d.tenThuoc = giaiMa(m.group(1));
            d.soLo = giaiMa(m.group(2));
            d.hanDung = dinhDangHan(m.group(3));
            d.soLuong = Integer.parseInt(m.group(4));
            d.giaBan = new BigDecimal(m.group(5)).longValue();
            d.thanhTien = new BigDecimal(m.group(6)).longValue();
            hd.dong.add(d);
        }
        return hd;
    }

    private void hienChiTietHoaDon(HoaDonKQ hd) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã hóa đơn: ").append(hd.maHD).append("\n");
        sb.append("Ngày lập: ").append(hd.ngayLap.format(NGAY_GIO)).append("\n");
        sb.append("Nhân viên: ").append(hd.tenNV).append("\n");
        sb.append("Khách hàng: ").append(hd.tenKH).append("\n\n");
        for (DongHoaDon d : hd.dong) {
            sb.append("  • ").append(d.tenThuoc).append(" – lô ").append(d.soLo)
                    .append(" (HSD ").append(d.hanDung).append(") × ").append(d.soLuong)
                    .append(" = ").append(MONEY.format(d.thanhTien)).append(" đ\n");
        }
        sb.append("\nTỔNG TIỀN: ").append(MONEY.format(hd.tongTien)).append(" đ");

        Object[] lc = { "Xuất Excel", "Đóng" };
        int chon = JOptionPane.showOptionDialog(this, sb.toString(), "Chi tiết hóa đơn",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, lc, lc[0]);
        if (chon == 0) {
            xuatExcel(hd);
        }
    }

    // =========================================================
    // XUẤT HÓA ĐƠN RA EXCEL (.xlsx) – không cần thư viện ngoài
    // =========================================================
    private void xuatExcel(HoaDonKQ hd) {
        if (hd == null) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào để xuất.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu hóa đơn ra Excel");
        fc.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        fc.setSelectedFile(new File("HoaDon_" + hd.maHD + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "File đã tồn tại, bạn có muốn ghi đè?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ghiExcel(file, hd);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không ghi được file (file có đang mở trong Excel không?):\n" + ex.getMessage(),
                    "Lỗi xuất Excel", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Đã lưu: " + file.getAbsolutePath() + "\n\nMở file để xem/in ngay?",
                "Xuất Excel thành công", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không mở được file: " + ex.getMessage());
            }
        }
    }

    // ----- Các hàm dựng XML cho SpreadsheetML -----
    private static String xml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String cStr(String ref, String text, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + xml(text) + "</t></is></c>";
    }

    private static String cNum(String ref, long value, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\"><v>" + value + "</v></c>";
    }

    private static String cTrong(String ref, int style) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\"/>";
    }

    private static String hang(int r, String cells) {
        return "<row r=\"" + r + "\">" + cells + "</row>";
    }

    private static void ghiEntry(ZipOutputStream z, String ten, String noiDung) throws IOException {
        z.putNextEntry(new ZipEntry(ten));
        z.write(noiDung.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private static void ghiExcel(File file, HoaDonKQ hd) throws IOException {
        final String HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
        final String NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        final String NS_PKG = "http://schemas.openxmlformats.org/package/2006/relationships";

        // ---- sheet1.xml ----
        StringBuilder sd = new StringBuilder();
        List<String> merge = new ArrayList<>();

        sd.append(hang(1, cStr("A1", "HÓA ĐƠN BÁN THUỐC", 1)));
        merge.add("A1:G1");

        sd.append(hang(3, cStr("A3", "Mã hóa đơn:", 2) + cStr("B3", String.valueOf(hd.maHD), 0)));
        sd.append(hang(4, cStr("A4", "Ngày lập:", 2) + cStr("B4", hd.ngayLap.format(NGAY_GIO), 0)));
        sd.append(hang(5, cStr("A5", "Nhân viên:", 2) + cStr("B5", hd.tenNV == null ? "" : hd.tenNV, 0)));
        sd.append(hang(6, cStr("A6", "Khách hàng:", 2) + cStr("B6", hd.tenKH, 0)));

        int rHead = 8;
        sd.append(hang(rHead,
                cStr("A8", "STT", 3) + cStr("B8", "Tên thuốc", 3) + cStr("C8", "Số lô", 3)
                        + cStr("D8", "Hạn dùng", 3) + cStr("E8", "SL", 3)
                        + cStr("F8", "Đơn giá (đ)", 3) + cStr("G8", "Thành tiền (đ)", 3)));

        int r = rHead + 1;
        int stt = 1;
        for (DongHoaDon d : hd.dong) {
            sd.append(hang(r,
                    cNum("A" + r, stt++, 6) + cStr("B" + r, d.tenThuoc, 4) + cStr("C" + r, d.soLo, 4)
                            + cStr("D" + r, d.hanDung, 6) + cNum("E" + r, d.soLuong, 6)
                            + cNum("F" + r, d.giaBan, 5) + cNum("G" + r, d.thanhTien, 5)));
            r++;
        }

        int rTong = r;
        String congThuc = hd.dong.isEmpty() ? "0" : "SUM(G" + (rHead + 1) + ":G" + (rTong - 1) + ")";
        sd.append(hang(rTong,
                cStr("A" + rTong, "TỔNG CỘNG", 7) + cTrong("B" + rTong, 7) + cTrong("C" + rTong, 7)
                        + cTrong("D" + rTong, 7) + cTrong("E" + rTong, 7) + cTrong("F" + rTong, 7)
                        + "<c r=\"G" + rTong + "\" s=\"8\"><f>" + congThuc + "</f><v>" + hd.tongTien + "</v></c>"));
        merge.add("A" + rTong + ":F" + rTong);

        int rCam = rTong + 2;
        sd.append(hang(rCam, cStr("A" + rCam, "Cảm ơn quý khách!", 9)));
        merge.add("A" + rCam + ":G" + rCam);

        StringBuilder mc = new StringBuilder("<mergeCells count=\"" + merge.size() + "\">");
        for (String m : merge) {
            mc.append("<mergeCell ref=\"").append(m).append("\"/>");
        }
        mc.append("</mergeCells>");

        String sheet = HEAD + "<worksheet xmlns=\"" + NS + "\">"
                + "<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\"/></sheetViews>"
                + "<sheetFormatPr defaultRowHeight=\"18\"/>"
                + "<cols>"
                + "<col min=\"1\" max=\"1\" width=\"14\" customWidth=\"1\"/>"
                + "<col min=\"2\" max=\"2\" width=\"32\" customWidth=\"1\"/>"
                + "<col min=\"3\" max=\"3\" width=\"16\" customWidth=\"1\"/>"
                + "<col min=\"4\" max=\"4\" width=\"13\" customWidth=\"1\"/>"
                + "<col min=\"5\" max=\"5\" width=\"8\" customWidth=\"1\"/>"
                + "<col min=\"6\" max=\"6\" width=\"16\" customWidth=\"1\"/>"
                + "<col min=\"7\" max=\"7\" width=\"18\" customWidth=\"1\"/>"
                + "</cols>"
                + "<sheetData>" + sd + "</sheetData>" + mc + "</worksheet>";

        // ---- styles.xml ----
        String border = "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right>"
                + "<top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border>";
        String styles = HEAD + "<styleSheet xmlns=\"" + NS + "\">"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"11\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Arial\"/></font>"
                + "<font><b/><sz val=\"16\"/><name val=\"Arial\"/></font>"
                + "<font><i/><sz val=\"11\"/><name val=\"Arial\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E1F2\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" + border
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"10\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" // 0 thường
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 1
                                                                                                                                                                                            // tiêu
                                                                                                                                                                                            // đề
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" // 2 nhãn
                                                                                                             // đậm
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" // 3
                                                                                                                                                                                                                                             // tiêu
                                                                                                                                                                                                                                             // đề
                                                                                                                                                                                                                                             // cột
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\"/>" // 4 chữ
                                                                                                               // có
                                                                                                               // viền
                + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\"/>" // 5
                                                                                                                                       // số
                                                                                                                                       // tiền
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>" // 6
                                                                                                                                                                          // căn
                                                                                                                                                                          // giữa
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\"/></xf>" // 7
                                                                                                                                                                                         // nhãn
                                                                                                                                                                                         // tổng
                + "<xf numFmtId=\"3\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\"/>" // 8
                                                                                                                                                       // tổng
                                                                                                                                                       // tiền
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>" // 9
                                                                                                                                                                        // chữ
                                                                                                                                                                        // nghiêng
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
                + "<sheets><sheet name=\"HoaDon\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";

        String workbookRels = HEAD + "<Relationships xmlns=\"" + NS_PKG + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_R + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"" + NS_R + "/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";

        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            ghiEntry(z, "[Content_Types].xml", contentTypes);
            ghiEntry(z, "_rels/.rels", rels);
            ghiEntry(z, "xl/workbook.xml", workbook);
            ghiEntry(z, "xl/_rels/workbook.xml.rels", workbookRels);
            ghiEntry(z, "xl/styles.xml", styles);
            ghiEntry(z, "xl/worksheets/sheet1.xml", sheet);
        }
    }

    // ---------- Parse JSON đơn giản (tránh phụ thuộc thư viện ngoài) ----------
    private static String timChuoi(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? giaiMa(m.group(1)) : null;
    }

    private static String timSo(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?(\\d+(?:\\.\\d+)?)\"?").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String giaiMa(String s) {
        return s.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }
}