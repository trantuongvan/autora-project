package gui;

import controller.QLThongKeController;
import entity.DoanhThuNgay;
import entity.DuLieuThongKe;
import entity.LuotXeNgay;
import entity.NangSuatKTV;
import entity.NhapXuatNgay;
import entity.ThongKePhuTung;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Thống kê báo cáo (UC014) - Đồng bộ chuẩn UIComponents & UIConstants.
 */
public class QLThongKe extends JPanel {

    private static final Color RED_KPI = new Color(225, 65, 55);
    private static final Color BLUE_KPI = new Color(37, 137, 229);
    private static final Color AMBER_KPI = new Color(238, 162, 27);
    private static final Color GREEN_KPI = new Color(9, 166, 70);

    private final DecimalFormat dfTien = new DecimalFormat("#,##0");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dtfNgan = DateTimeFormatter.ofPattern("dd/MM");

    private final UIComponents.Badge badgeKyBaoCao = new UIComponents.Badge("Kỳ báo cáo", UIConstants.ROLE_RECEPTION_LIGHT);

    private JButton btnTabDoanhThu, btnTabPhuTung, btnTabHieuSuat;
    private int tabHienTai = 0;

    private JButton btnLocNgay, btnLocTuan, btnLocThang, btnLocNam;
    private JTextField txtTuNgay, txtDenNgay;
    private JButton btnXemBaoCao, btnXuatExcel, btnXuatPDF, btnIn;

    private ThongKeKpiCard kpi1, kpi2, kpi3, kpi4;

    private CardLayout cardLayoutNoiDung;
    private JPanel pnlNoiDungTab;

    // Thành phần Tab 0: Doanh thu
    private DonutChartPanel chartDonutDoanhThu;
    private BarChartPanel chartCotDoanhThu;
    private JLabel lblKyBangDoanhThu, lblFooterDoanhThu;
    private UIComponents.IconField fTimDoanhThu;
    private final DefaultTableModel modelDoanhThu = UIComponents.modelKhongSua(
            "Ngày", "Số HĐ", "Tiền công", "Tiền phụ tùng", "Giảm giá", "VAT", "Tổng thanh toán"
    );
    private final JTable tblDoanhThu = new JTable(modelDoanhThu);

    // Thành phần Tab 1: Phụ tùng
    private HorizontalBarPanel chartNgangPhuTung;
    private GroupedBarChartPanel chartCotNhapXuat;
    private JLabel lblKyBangPhuTung, lblFooterPhuTung;
    private UIComponents.IconField fTimPhuTung;
    private final DefaultTableModel modelPhuTung = UIComponents.modelKhongSua(
            "Mã SKU", "Tên phụ tùng", "Hãng", "Nhập", "Xuất", "Tồn hiện tại", "Giá trị tồn"
    );
    private final JTable tblPhuTung = new JTable(modelPhuTung);
    private final List<Boolean> canhBaoPhuTungHienThi = new ArrayList<>();

    // Thành phần Tab 2: Hiệu suất KTV
    private HorizontalBarPanel chartNgangKTV;
    private BarChartPanel chartCotLuotXe;
    private JLabel lblKyBangKTV, lblFooterKTV;
    private UIComponents.IconField fTimKTV;
    private final DefaultTableModel modelKTV = UIComponents.modelKhongSua(
            "Kỹ thuật viên", "Số xe tham gia", "Hạng mục hoàn thành", "Đang thực hiện", "Tiền công mang lại", "Tỷ lệ hoàn thành"
    );
    private final JTable tblKTV = new JTable(modelKTV);

    private DuLieuThongKe duLieuHienTai;
    private LocalDate tuNgayHienTai = LocalDate.now().withDayOfMonth(1);
    private LocalDate denNgayHienTai = LocalDate.now();
    private QLThongKeController controller;

    public QLThongKe() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
        controller = new QLThongKeController(this);
    }

    public static JPanel createPanel() {
        return new QLThongKe();
    }

    private void buildUI() {
        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Thống kê báo cáo",
                "Theo dõi doanh thu, phụ tùng và hiệu suất kỹ thuật viên.",
                UIConstants.ROLE_RECEPTION,
                badgeKyBaoCao
        );
        root.them(pnlHeader, 0);

        UIComponents.RoundedPanel cardFilter = UIComponents.card();
        cardFilter.setLayout(new BorderLayout(0, 12));
        cardFilter.setBorder(new EmptyBorder(12, 18, 14, 18));

        JPanel pnlTabWrap = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlTabWrap.setBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        btnTabDoanhThu = taoTab("Doanh thu", true);
        btnTabPhuTung = taoTab("Phụ tùng", false);
        btnTabHieuSuat = taoTab("Hiệu suất kỹ thuật viên", false);

        btnTabDoanhThu.addActionListener(e -> chuyenTab(0));
        btnTabPhuTung.addActionListener(e -> chuyenTab(1));
        btnTabHieuSuat.addActionListener(e -> chuyenTab(2));

        pnlTabWrap.add(btnTabDoanhThu);
        pnlTabWrap.add(btnTabPhuTung);
        pnlTabWrap.add(btnTabHieuSuat);
        cardFilter.add(pnlTabWrap, BorderLayout.NORTH);

        JPanel pnlControls = UIComponents.trong(new BorderLayout(12, 0));

        JPanel pnlLeftControls = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnLocNgay = taoNutKyNhanh("Ngày", false);
        btnLocTuan = taoNutKyNhanh("Tuần", false);
        btnLocThang = taoNutKyNhanh("Tháng", true);
        btnLocNam = taoNutKyNhanh("Năm", false);
        pnlLeftControls.add(btnLocNgay);
        pnlLeftControls.add(btnLocTuan);
        pnlLeftControls.add(btnLocThang);
        pnlLeftControls.add(btnLocNam);

        pnlLeftControls.add(Box.createHorizontalStrut(8));
        pnlLeftControls.add(UIComponents.label("Từ ngày", UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_MUTED));
        txtTuNgay = taoOThoiGian("01/10/2026");
        pnlLeftControls.add(txtTuNgay);

        pnlLeftControls.add(UIComponents.label("Đến ngày", UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_MUTED));
        txtDenNgay = taoOThoiGian("31/10/2026");
        pnlLeftControls.add(txtDenNgay);

        btnXemBaoCao = UIComponents.nutChinh("Xem báo cáo", UIConstants.ROLE_RECEPTION);
        pnlLeftControls.add(btnXemBaoCao);

        JPanel pnlRightControls = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnXuatExcel = taoNutVien("Xuất Excel");
        btnXuatPDF = taoNutVien("Xuất PDF");
        btnIn = UIComponents.nutChinh("In", BLUE_KPI);
        pnlRightControls.add(btnXuatExcel);
        pnlRightControls.add(btnXuatPDF);
        pnlRightControls.add(btnIn);

        pnlControls.add(pnlLeftControls, BorderLayout.WEST);
        pnlControls.add(pnlRightControls, BorderLayout.EAST);
        cardFilter.add(pnlControls, BorderLayout.CENTER);

        root.them(cardFilter, 18);

        JPanel pnlKpis = UIComponents.trong(new GridLayout(1, 4, 16, 0));
        pnlKpis.setPreferredSize(new Dimension(100, 118));
        pnlKpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        kpi1 = new ThongKeKpiCard("Tổng doanh thu", "0đ", "Sau giảm giá và VAT", RED_KPI);
        kpi2 = new ThongKeKpiCard("Tiền công dịch vụ", "0đ", "0% doanh thu", BLUE_KPI);
        kpi3 = new ThongKeKpiCard("Tiền phụ tùng", "0đ", "0% doanh thu", AMBER_KPI);
        kpi4 = new ThongKeKpiCard("Lợi nhuận", "0đ", "Kỳ báo cáo", GREEN_KPI);
        pnlKpis.add(kpi1);
        pnlKpis.add(kpi2);
        pnlKpis.add(kpi3);
        pnlKpis.add(kpi4);
        root.them(pnlKpis, 16);

        cardLayoutNoiDung = new CardLayout();
        pnlNoiDungTab = UIComponents.trong(cardLayoutNoiDung);
        pnlNoiDungTab.add(taoNoiDungTabDoanhThu(), "TAB_0");
        pnlNoiDungTab.add(taoNoiDungTabPhuTung(), "TAB_1");
        pnlNoiDungTab.add(taoNoiDungTabHieuSuat(), "TAB_2");

        root.them(pnlNoiDungTab, 16);
        root.ketThuc();
        add(UIComponents.cuonTrang(root), BorderLayout.CENTER);
    }

    private JPanel taoNoiDungTabDoanhThu() {
        UIComponents.CotDoc panel = new UIComponents.CotDoc();

        JPanel pnlCharts = UIComponents.trong(new GridLayout(1, 2, 18, 0));
        pnlCharts.setPreferredSize(new Dimension(100, 265));
        pnlCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));

        UIComponents.RoundedPanel cardDonut = taoCardBieuDo("Tỷ trọng doanh thu", "");
        chartDonutDoanhThu = new DonutChartPanel();
        cardDonut.add(chartDonutDoanhThu, BorderLayout.CENTER);

        UIComponents.RoundedPanel cardBar = taoCardBieuDo("Doanh thu theo ngày", "Đơn vị: triệu đồng");
        chartCotDoanhThu = new BarChartPanel(RED_KPI, true);
        cardBar.add(chartCotDoanhThu, BorderLayout.CENTER);

        pnlCharts.add(cardDonut);
        pnlCharts.add(cardBar);
        panel.them(pnlCharts, 0);

        cauHinhBang(tblDoanhThu, -1);
        UIComponents.datDoRong(tblDoanhThu, 120, 80, 140, 140, 120, 120, 160);
        lblKyBangDoanhThu = UIComponents.label("Kỳ báo cáo", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        lblFooterDoanhThu = UIComponents.label("Hiển thị 0 ngày", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        fTimDoanhThu = taoOTimKiem("Tìm theo ngày hoặc số tiền...", this::locBangDoanhThu);

        panel.them(taoCardBang("Chi tiết doanh thu", fTimDoanhThu, lblKyBangDoanhThu, tblDoanhThu, lblFooterDoanhThu), 16);
        panel.ketThuc();
        return panel;
    }

    private JPanel taoNoiDungTabPhuTung() {
        UIComponents.CotDoc panel = new UIComponents.CotDoc();

        JPanel pnlCharts = UIComponents.trong(new GridLayout(1, 2, 18, 0));
        pnlCharts.setPreferredSize(new Dimension(100, 265));
        pnlCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));

        UIComponents.RoundedPanel cardTopPT = taoCardBieuDo("Phụ tùng xuất nhiều nhất", "Đơn vị: cái");
        chartNgangPhuTung = new HorizontalBarPanel();
        cardTopPT.add(taoScrollBieuDoNgang(chartNgangPhuTung), BorderLayout.CENTER);

        UIComponents.RoundedPanel cardNX = taoCardBieuDo("Nhập – xuất theo ngày", "Đơn vị: cái   ■ Nhập   ■ Xuất");
        chartCotNhapXuat = new GroupedBarChartPanel();
        cardNX.add(chartCotNhapXuat, BorderLayout.CENTER);

        pnlCharts.add(cardTopPT);
        pnlCharts.add(cardNX);
        panel.them(pnlCharts, 0);

        cauHinhBang(tblPhuTung, 5);
        UIComponents.datDoRong(tblPhuTung, 110, 250, 120, 90, 90, 100, 140);
        lblKyBangPhuTung = UIComponents.label("Kỳ báo cáo", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        lblFooterPhuTung = UIComponents.label("Hiển thị 0 mặt hàng", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        fTimPhuTung = taoOTimKiem("Tìm mã SKU, tên phụ tùng, hãng...", this::locBangPhuTung);

        panel.them(taoCardBang("Chi tiết nhập – xuất – tồn kho", fTimPhuTung, lblKyBangPhuTung, tblPhuTung, lblFooterPhuTung), 16);
        panel.ketThuc();
        return panel;
    }

    private JPanel taoNoiDungTabHieuSuat() {
        UIComponents.CotDoc panel = new UIComponents.CotDoc();

        JPanel pnlCharts = UIComponents.trong(new GridLayout(1, 2, 18, 0));
        pnlCharts.setPreferredSize(new Dimension(100, 265));
        pnlCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 265));

        UIComponents.RoundedPanel cardTopKTV = taoCardBieuDo("Hạng mục hoàn thành theo kỹ thuật viên", "Đơn vị: hạng mục");
        chartNgangKTV = new HorizontalBarPanel();
        cardTopKTV.add(taoScrollBieuDoNgang(chartNgangKTV), BorderLayout.CENTER);

        UIComponents.RoundedPanel cardLuotXe = taoCardBieuDo("Lượt xe vào xưởng theo ngày", "Đơn vị: lượt xe");
        chartCotLuotXe = new BarChartPanel(BLUE_KPI, false);
        cardLuotXe.add(chartCotLuotXe, BorderLayout.CENTER);

        pnlCharts.add(cardTopKTV);
        pnlCharts.add(cardLuotXe);
        panel.them(pnlCharts, 0);

        cauHinhBang(tblKTV, -1);
        UIComponents.datDoRong(tblKTV, 220, 120, 150, 130, 160, 130);
        lblKyBangKTV = UIComponents.label("Kỳ báo cáo", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        lblFooterKTV = UIComponents.label("Hiển thị 0 kỹ thuật viên", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        fTimKTV = taoOTimKiem("Tìm tên hoặc mã kỹ thuật viên...", this::locBangKTV);

        panel.them(taoCardBang("Năng suất kỹ thuật viên", fTimKTV, lblKyBangKTV, tblKTV, lblFooterKTV), 16);
        panel.ketThuc();
        return panel;
    }

    private JScrollPane taoScrollBieuDoNgang(HorizontalBarPanel chartPanel) {
        JScrollPane sp = new JScrollPane(chartPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        return sp;
    }

    public void capNhatGiaoDien(DuLieuThongKe dl, LocalDate tuNgay, LocalDate denNgay) {
        this.duLieuHienTai = dl;
        this.tuNgayHienTai = tuNgay;
        this.denNgayHienTai = denNgay;

        String chuoiKyNgan = "Kỳ " + tuNgay.format(dtfNgan) + " – " + denNgay.format(dtfNgan);
        String chuoiKyDayDu = "Kỳ " + tuNgay.format(dtf) + " – " + denNgay.format(dtf);

        badgeKyBaoCao.setText(chuoiKyDayDu);
        lblKyBangDoanhThu.setText(chuoiKyDayDu);
        lblKyBangPhuTung.setText(chuoiKyDayDu);
        lblKyBangKTV.setText(chuoiKyDayDu);

        chartDonutDoanhThu.setData(dl.getTongTienCong(), dl.getTongTienPhuTung(),
                dl.getTyTrongTienCong(), dl.getTyTrongPhuTung());

        List<String> nhanNgayDT = new ArrayList<>();
        List<Double> giaTriNgayDT = new ArrayList<>();
        List<DoanhThuNgay> dsDT = dl.getDanhSachDoanhThuNgay();
        for (int i = dsDT.size() - 1; i >= 0; i--) {
            DoanhThuNgay d = dsDT.get(i);
            nhanNgayDT.add(String.format("%02d", d.getNgay().getDayOfMonth()));
            giaTriNgayDT.add(d.getTongThanhToan() / 1_000_000.0);
        }
        chartCotDoanhThu.setData(nhanNgayDT, giaTriNgayDT);
        locBangDoanhThu();

        List<String> tenAllPT = new ArrayList<>();
        List<Integer> slAllPT = new ArrayList<>();
        for (ThongKePhuTung item : dl.getDanhSachPhuTung()) {
            tenAllPT.add(item.getPhuTung().getTenPhuTung());
            slAllPT.add(item.getSoLuongXuat());
        }
        chartNgangPhuTung.setData(tenAllPT, slAllPT);
        chartCotNhapXuat.setData(dl.getDanhSachNhapXuatNgay());
        locBangPhuTung();

        List<String> tenAllKTV = new ArrayList<>();
        List<Integer> hmAllKTV = new ArrayList<>();
        for (NangSuatKTV k : dl.getDanhSachNangSuatKTV()) {
            tenAllKTV.add(k.getKyThuatVien().getHoTen());
            hmAllKTV.add(k.getHangMucHoanThanh());
        }
        chartNgangKTV.setData(tenAllKTV, hmAllKTV);

        List<String> nhanNgayXe = new ArrayList<>();
        List<Double> slNgayXe = new ArrayList<>();
        for (LuotXeNgay lx : dl.getDanhSachLuotXeNgay()) {
            nhanNgayXe.add(String.format("%02d", lx.getNgay().getDayOfMonth()));
            slNgayXe.add((double) lx.getSoLuotXe());
        }
        chartCotLuotXe.setData(nhanNgayXe, slNgayXe);
        locBangKTV();

        capNhat4TheKPI(chuoiKyNgan);
        revalidate();
        repaint();
    }

    private void locBangDoanhThu() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(fTimDoanhThu);
        modelDoanhThu.setRowCount(0);

        List<DoanhThuNgay> dsDT = duLieuHienTai.getDanhSachDoanhThuNgay();
        int demNgay = 0;
        int tongHD = 0;
        double tongCong = 0, tongPT = 0, tongGiam = 0, tongVAT = 0, tongThanhToan = 0;

        for (DoanhThuNgay d : dsDT) {
            String chuoiNgay = d.getNgay().format(dtf);
            String chuoiTong = dfTien.format(d.getTongThanhToan());
            if (!kw.isEmpty() && !chuoiNgay.toLowerCase().contains(kw)
                    && !String.valueOf(d.getSoHoaDon()).contains(kw)
                    && !chuoiTong.contains(kw)) {
                continue;
            }
            modelDoanhThu.addRow(new Object[]{
                    chuoiNgay,
                    d.getSoHoaDon(),
                    dfTien.format(d.getTienCong()) + "đ",
                    dfTien.format(d.getTienPhuTung()) + "đ",
                    dfTien.format(d.getGiamGia()) + "đ",
                    dfTien.format(d.getTienVAT()) + "đ",
                    chuoiTong + "đ"
            });
            demNgay++;
            tongHD += d.getSoHoaDon();
            tongCong += d.getTienCong();
            tongPT += d.getTienPhuTung();
            tongGiam += d.getGiamGia();
            tongVAT += d.getTienVAT();
            tongThanhToan += d.getTongThanhToan();
        }

        modelDoanhThu.addRow(new Object[]{
                "Tổng kỳ (" + demNgay + " ngày)",
                tongHD,
                dfTien.format(tongCong) + "đ",
                dfTien.format(tongPT) + "đ",
                dfTien.format(tongGiam) + "đ",
                dfTien.format(tongVAT) + "đ",
                dfTien.format(tongThanhToan) + "đ"
        });
        lblFooterDoanhThu.setText("Hiển thị " + demNgay + " / " + dsDT.size() + " ngày có giao dịch");
    }

    private void locBangPhuTung() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(fTimPhuTung);
        modelPhuTung.setRowCount(0);
        canhBaoPhuTungHienThi.clear();

        List<ThongKePhuTung> dsPT = duLieuHienTai.getDanhSachPhuTung();
        int dem = 0;
        int tongNhap = 0, tongXuat = 0;
        double tongGiaTriTon = 0;

        for (ThongKePhuTung item : dsPT) {
            String ma = item.getPhuTung().getMaSKU() == null ? "" : item.getPhuTung().getMaSKU();
            String ten = item.getPhuTung().getTenPhuTung() == null ? "" : item.getPhuTung().getTenPhuTung();
            String hang = item.getPhuTung().getHangSanXuat() == null ? "" : item.getPhuTung().getHangSanXuat();

            if (!kw.isEmpty() && !ma.toLowerCase().contains(kw)
                    && !ten.toLowerCase().contains(kw)
                    && !hang.toLowerCase().contains(kw)) {
                continue;
            }

            modelPhuTung.addRow(new Object[]{
                    ma,
                    ten,
                    hang,
                    item.getSoLuongNhap(),
                    item.getSoLuongXuat(),
                    item.getTonHienTai(),
                    dfTien.format(item.getGiaTriTon()) + "đ"
            });
            canhBaoPhuTungHienThi.add(item.isDuoiNguongCanhBao());
            dem++;
            tongNhap += item.getSoLuongNhap();
            tongXuat += item.getSoLuongXuat();
            tongGiaTriTon += item.getGiaTriTon();
        }

        modelPhuTung.addRow(new Object[]{
                "Tổng kỳ báo cáo", "", "",
                tongNhap,
                tongXuat,
                "",
                dfTien.format(tongGiaTriTon) + "đ"
        });
        lblFooterPhuTung.setText("Hiển thị " + dem + " / " + dsPT.size() + " mặt hàng");
    }

    private void locBangKTV() {
        if (duLieuHienTai == null) return;
        String kw = layTuKhoaTimKiem(fTimKTV);
        modelKTV.setRowCount(0);

        List<NangSuatKTV> dsKTV = duLieuHienTai.getDanhSachNangSuatKTV();
        int dem = 0;
        int tongHMXong = 0, tongHMDangLam = 0;
        double tongTienCong = 0;

        for (NangSuatKTV k : dsKTV) {
            String ten = k.getKyThuatVien().getHoTen() == null ? "" : k.getKyThuatVien().getHoTen();
            String ma = k.getKyThuatVien().getMaNV() == null ? "" : k.getKyThuatVien().getMaNV();

            if (!kw.isEmpty() && !ten.toLowerCase().contains(kw) && !ma.toLowerCase().contains(kw)) {
                continue;
            }

            modelKTV.addRow(new Object[]{
                    ten,
                    k.getSoXeThamGia(),
                    k.getHangMucHoanThanh(),
                    k.getHangMucDangThucHien(),
                    dfTien.format(k.getTienCongMangLai()) + "đ",
                    k.getTyLeHoanThanh() + "%"
            });
            dem++;
            tongHMXong += k.getHangMucHoanThanh();
            tongHMDangLam += k.getHangMucDangThucHien();
            tongTienCong += k.getTienCongMangLai();
        }

        int tongHM = tongHMXong + tongHMDangLam;
        double tyLeChung = tongHM > 0 ? Math.round((tongHMXong * 1000.0) / tongHM) / 10.0 : 0.0;

        modelKTV.addRow(new Object[]{
                "Tổng kỳ báo cáo",
                kw.isEmpty() ? duLieuHienTai.getTongXeSuaXong() : "-",
                tongHMXong,
                tongHMDangLam,
                dfTien.format(tongTienCong) + "đ",
                tyLeChung + "%"
        });
        lblFooterKTV.setText("Hiển thị " + dem + " / " + dsKTV.size() + " kỹ thuật viên");
    }

    private String layTuKhoaTimKiem(UIComponents.IconField field) {
        if (field == null || field.getTextField() == null) return "";
        String val = field.getTextField().getText().trim();
        Object ph = field.getTextField().getClientProperty("placeholder");
        if (ph != null && val.equalsIgnoreCase(ph.toString())) return "";
        if (val.startsWith("Tìm ")) return "";
        return val.toLowerCase();
    }

    private void capNhat4TheKPI(String chuoiKyNgan) {
        if (duLieuHienTai == null) return;
        if (tabHienTai == 0) {
            kpi1.setValues("Tổng doanh thu", dfTien.format(duLieuHienTai.getTongDoanhThu()) + "đ", "Sau giảm giá và VAT");
            kpi2.setValues("Tiền công dịch vụ", dfTien.format(duLieuHienTai.getTongTienCong()) + "đ", duLieuHienTai.getTyTrongTienCong() + "% doanh thu");
            kpi3.setValues("Tiền phụ tùng", dfTien.format(duLieuHienTai.getTongTienPhuTung()) + "đ", duLieuHienTai.getTyTrongPhuTung() + "% doanh thu");
            kpi4.setValues("Lợi nhuận", dfTien.format(duLieuHienTai.getTongLoiNhuan()) + "đ", chuoiKyNgan);
        } else if (tabHienTai == 1) {
            kpi1.setValues("Giá trị tồn kho hiện tại", dfTien.format(duLieuHienTai.getGiaTriTonKhoHienTai()) + "đ", duLieuHienTai.getSoMatHangQuanLy() + " mặt hàng đang quản lý");
            kpi2.setValues("Số lượng nhập", String.valueOf(duLieuHienTai.getTongSoLuongNhap()), "Giá trị " + dfTien.format(duLieuHienTai.getTongGiaTriNhap()) + "đ");
            kpi3.setValues("Số lượng xuất", String.valueOf(duLieuHienTai.getTongSoLuongXuat()), "Giá trị " + dfTien.format(duLieuHienTai.getTongGiaTriXuat()) + "đ");
            kpi4.setValues("Phiếu nhập trong kỳ", String.valueOf(duLieuHienTai.getSoPhieuNhapTrongKy()), chuoiKyNgan);
        } else {
            kpi1.setValues("Lượt xe vào xưởng", String.valueOf(duLieuHienTai.getTongLuotXeVaoXuong()), "Tiếp nhận trong kỳ");
            kpi2.setValues("Xe sửa xong", String.valueOf(duLieuHienTai.getTongXeSuaXong()), duLieuHienTai.getTyLeXeSuaXong() + "% lượt tiếp nhận");
            kpi3.setValues("Hạng mục hoàn thành", String.valueOf(duLieuHienTai.getTongHangMucHoanThanh()), "Trung bình " + duLieuHienTai.getTrungBinhHangMucTrenXe() + " hạng mục/xe");
            kpi4.setValues("Kỹ thuật viên hoạt động", String.valueOf(duLieuHienTai.getSoKyThuatVienHoatDong()), "Có phiếu trong kỳ");
        }
    }

    private void chuyenTab(int index) {
        this.tabHienTai = index;
        capNhatStyleTab(btnTabDoanhThu, index == 0);
        capNhatStyleTab(btnTabPhuTung, index == 1);
        capNhatStyleTab(btnTabHieuSuat, index == 2);
        cardLayoutNoiDung.show(pnlNoiDungTab, "TAB_" + index);
        capNhat4TheKPI("Kỳ " + tuNgayHienTai.format(dtfNgan) + " – " + denNgayHienTai.format(dtfNgan));
    }

    public void setKhoangThoiGian(LocalDate tuNgay, LocalDate denNgay, String loaiActive) {
        txtTuNgay.setText(tuNgay.format(dtf));
        txtDenNgay.setText(denNgay.format(dtf));
        capNhatStyleKyNhanh(btnLocNgay, "NGAY".equals(loaiActive));
        capNhatStyleKyNhanh(btnLocTuan, "TUAN".equals(loaiActive));
        capNhatStyleKyNhanh(btnLocThang, "THANG".equals(loaiActive));
        capNhatStyleKyNhanh(btnLocNam, "NAM".equals(loaiActive));
    }

    private JButton taoTab(String text, boolean active) {
        JButton b = new JButton(text);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        capNhatStyleTab(b, active);
        return b;
    }

    private void capNhatStyleTab(JButton b, boolean active) {
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(active ? Font.BOLD : Font.PLAIN, 13.5f));
        b.setForeground(active ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_MUTED);
        b.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, active ? 3 : 0, 0, UIConstants.ROLE_RECEPTION),
                new EmptyBorder(8, 2, 8, 2)
        ));
    }

    private JButton taoNutKyNhanh(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        capNhatStyleKyNhanh(b, active);
        return b;
    }

    private void capNhatStyleKyNhanh(JButton b, boolean active) {
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(active ? Font.BOLD : Font.PLAIN, 13f));
        b.setBackground(active ? UIComponents.tint(UIConstants.ROLE_RECEPTION, 0.90f) : UIConstants.CARD_BG);
        b.setForeground(active ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_DARK);
        b.setBorder(new CompoundBorder(
                new LineBorder(active ? UIConstants.ROLE_RECEPTION_LIGHT : UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(7, 13, 7, 13)
        ));
    }

    private JTextField taoOThoiGian(String text) {
        JTextField t = new JTextField(text, 9);
        t.setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
        t.setForeground(UIConstants.TEXT_DARK);
        t.setBackground(UIConstants.CARD_BG);
        t.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return t;
    }

    private JButton taoNutVien(String text) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
        b.setBackground(UIConstants.CARD_BG);
        b.setForeground(UIConstants.TEXT_DARK);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private UIComponents.IconField taoOTimKiem(String placeholder, Runnable onSearch) {
        UIComponents.IconField field = new UIComponents.IconField(
                placeholder,
                new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
                true
        );
        field.setPreferredSize(new Dimension(260, 38));
        field.getTextField().putClientProperty("placeholder", placeholder);
        field.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearch.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearch.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearch.run(); }
        });
        return field;
    }

    private UIComponents.RoundedPanel taoCardBieuDo(String tieuDe, String phuDePhai) {
        UIComponents.RoundedPanel card = UIComponents.card();
        card.setLayout(new BorderLayout());

        JPanel head = UIComponents.trong(new BorderLayout());
        head.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(14, 18, 12, 18)
        ));
        head.add(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        if (!phuDePhai.isEmpty()) {
            head.add(UIComponents.label(phuDePhai, UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED), BorderLayout.EAST);
        }
        card.add(head, BorderLayout.NORTH);
        return card;
    }

    private UIComponents.RoundedPanel taoCardBang(String tieuDe, UIComponents.IconField fSearch, JLabel lblKy, JTable table, JLabel lblFooter) {
        UIComponents.RoundedPanel card = UIComponents.card();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        JPanel head = UIComponents.trong(new BorderLayout(12, 0));
        head.setBorder(new EmptyBorder(0, 0, 12, 0));
        head.add(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK), BorderLayout.WEST);

        JPanel pnlRightHead = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        if (fSearch != null) {
            pnlRightHead.add(fSearch);
        }
        pnlRightHead.add(lblKy);
        head.add(pnlRightHead, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JScrollPane sp = UIComponents.cuonBang(table);
        sp.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        sp.setPreferredSize(new Dimension(100, 290));
        card.add(sp, BorderLayout.CENTER);

        JPanel foot = UIComponents.trong(new BorderLayout());
        foot.setBorder(new EmptyBorder(12, 2, 0, 2));
        foot.add(lblFooter, BorderLayout.WEST);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private void cauHinhBang(JTable t, int cotCanhBaoDo) {
        UIComponents.styleTable(t);
        t.setRowHeight(46);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBorder(new EmptyBorder(0, 14, 0, 14));
                boolean isLastRow = (row == table.getRowCount() - 1);
                if (isLastRow) {
                    c.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13.5f));
                    c.setBackground(UIConstants.APP_BG);
                    c.setForeground(column == table.getColumnCount() - 1 ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_DARK);
                } else {
                    c.setFont(column == 0
                            ? UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13.5f)
                            : UIConstants.FONT_BODY.deriveFont(13.5f));
                    c.setBackground(UIConstants.CARD_BG);
                    c.setForeground(UIConstants.TEXT_DARK);
                    if (column == cotCanhBaoDo && row < canhBaoPhuTungHienThi.size()
                            && Boolean.TRUE.equals(canhBaoPhuTungHienThi.get(row))) {
                        c.setForeground(UIConstants.STATUS_DANGER);
                        c.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13.5f));
                    }
                }
                return c;
            }
        });
    }

    public JButton getBtnLocNgay() { return btnLocNgay; }
    public JButton getBtnLocTuan() { return btnLocTuan; }
    public JButton getBtnLocThang() { return btnLocThang; }
    public JButton getBtnLocNam() { return btnLocNam; }
    public JTextField getTxtTuNgay() { return txtTuNgay; }
    public JTextField getTxtDenNgay() { return txtDenNgay; }
    public JButton getBtnXemBaoCao() { return btnXemBaoCao; }
    public JButton getBtnXuatExcel() { return btnXuatExcel; }
    public JButton getBtnXuatPDF() { return btnXuatPDF; }
    public JButton getBtnIn() { return btnIn; }
    public int getTabHienTai() { return tabHienTai; }
    public JTable getBangHienTai() {
        return tabHienTai == 0 ? tblDoanhThu : tabHienTai == 1 ? tblPhuTung : tblKTV;
    }

    private static class ThongKeKpiCard extends JPanel {
        private final JLabel lblTitle;
        private final JLabel lblValue;
        private final JLabel lblSub;
        private final Color bgColor;

        public ThongKeKpiCard(String title, String value, String sub, Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
            setLayout(new BorderLayout(0, 6));
            setBorder(new EmptyBorder(16, 20, 16, 20));

            lblTitle = UIComponents.label(title, UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13.5f), new Color(255, 255, 255, 235));
            lblValue = UIComponents.label(value, UIConstants.FONT_PAGE_TITLE.deriveFont(Font.BOLD, 24f), Color.WHITE);
            lblSub = UIComponents.label(sub, UIConstants.FONT_CAPTION.deriveFont(12.5f), new Color(255, 255, 255, 215));

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblSub, BorderLayout.SOUTH);
        }

        public void setValues(String title, String value, String sub) {
            lblTitle.setText(title);
            lblValue.setText(value);
            lblSub.setText(sub);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class DonutChartPanel extends JPanel {
        private double tienCong, tienPhuTung, pctCong, pctPT;

        public DonutChartPanel() { setOpaque(false); }

        public void setData(double tienCong, double tienPhuTung, double pctCong, double pctPT) {
            this.tienCong = tienCong;
            this.tienPhuTung = tienPhuTung;
            this.pctCong = pctCong;
            this.pctPT = pctPT;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight() - 70) - 24;
            if (size < 60) size = 60;
            int x = (getWidth() - size) / 2;
            int y = 12;

            if (tienCong + tienPhuTung <= 0) {
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.fillOval(x, y, size, size);
            } else {
                double angleCong = (pctCong * 360.0) / 100.0;
                g2.setColor(BLUE_KPI);
                g2.fill(new Arc2D.Double(x, y, size, size, 90, -angleCong, Arc2D.PIE));
                g2.setColor(AMBER_KPI);
                g2.fill(new Arc2D.Double(x, y, size, size, 90 - angleCong, -(360 - angleCong), Arc2D.PIE));
            }

            int legY = y + size + 14;
            int mid = getWidth() / 2;
            veChuThich(g2, mid - 145, legY, BLUE_KPI, "Tiền công dịch vụ", pctCong + "%", dfTien.format(tienCong) + "đ");
            veChuThich(g2, mid + 18, legY, AMBER_KPI, "Tiền phụ tùng", pctPT + "%", dfTien.format(tienPhuTung) + "đ");
            g2.dispose();
        }

        private void veChuThich(Graphics2D g2, int x, int y, Color c, String ten, String pct, String tien) {
            g2.setColor(c);
            g2.fillRoundRect(x, y + 3, 10, 10, 3, 3);
            g2.setColor(UIConstants.TEXT_MUTED);
            g2.setFont(UIConstants.FONT_CAPTION.deriveFont(12f));
            g2.drawString(ten, x + 16, y + 12);
            g2.setColor(c);
            g2.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 14f));
            g2.drawString(pct, x + 16, y + 30);
            g2.setColor(UIConstants.TEXT_MUTED);
            g2.setFont(UIConstants.FONT_CAPTION.deriveFont(12f));
            g2.drawString(tien, x + 16, y + 46);
        }
    }

    private static class BarChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Double> values = new ArrayList<>();
        private final Color barColor;
        private final boolean suffixTr;

        public BarChartPanel(Color barColor, boolean suffixTr) {
            this.barColor = barColor;
            this.suffixTr = suffixTr;
            setOpaque(false);
        }

        public void setData(List<String> labels, List<Double> values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padL = 46, padR = 18, padT = 18, padB = 30;
            int w = getWidth() - padL - padR;
            int h = getHeight() - padT - padB;
            if (w <= 0 || h <= 0) return;

            double max = 4.0;
            for (Double v : values) if (v > max) max = v;

            g2.setFont(UIConstants.FONT_CAPTION.deriveFont(11.5f));
            for (int i = 0; i <= 4; i++) {
                int y = padT + h - (i * h / 4);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawLine(padL, y, padL + w, y);
                g2.setColor(UIConstants.TEXT_MUTED);
                String lbl = Math.round(max * i / 4.0) + (suffixTr && i > 0 ? "tr" : "");
                g2.drawString(lbl, 10, y + 4);
            }

            if (values.isEmpty()) {
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(UIConstants.FONT_BODY.deriveFont(13f));
                g2.drawString("Chưa có dữ liệu trong kỳ", padL + w / 2 - 70, padT + h / 2);
                g2.dispose();
                return;
            }

            int n = values.size();
            int step = Math.max(1, w / n);
            int barW = Math.max(10, Math.min(36, step - 8));

            for (int i = 0; i < n; i++) {
                int bh = (int) Math.round((values.get(i) / max) * (h - 8));
                int bx = padL + i * step + (step - barW) / 2;
                int by = padT + h - bh;
                g2.setColor(barColor);
                g2.fillRoundRect(bx, by, barW, Math.max(2, bh), 6, 6);
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString(labels.get(i), bx + Math.max(0, (barW - 14) / 2), padT + h + 18);
            }
            g2.dispose();
        }
    }

    private static class HorizontalBarPanel extends JPanel {
        private static final int ROW_HEIGHT = 38;
        private static final int TOP_PADDING = 12;
        private static final int BOTTOM_PADDING = 12;

        private List<String> names = new ArrayList<>();
        private List<Integer> counts = new ArrayList<>();

        public HorizontalBarPanel() {
            setOpaque(false);
        }

        public void setData(List<String> names, List<Integer> counts) {
            this.names = names != null ? names : new ArrayList<>();
            this.counts = counts != null ? counts : new ArrayList<>();
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            int totalH = TOP_PADDING + BOTTOM_PADDING + Math.max(1, names.size()) * ROW_HEIGHT;
            return new Dimension(340, totalH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (names.isEmpty()) {
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(UIConstants.FONT_BODY.deriveFont(13f));
                g2.drawString("Chưa có dữ liệu", getWidth() / 2 - 50, 42);
                g2.dispose();
                return;
            }

            int max = 1;
            for (Integer c : counts) if (c != null && c > max) max = c;

            int barX = 220;
            int maxBarW = Math.max(40, getWidth() - barX - 20);

            for (int i = 0; i < names.size(); i++) {
                int y = TOP_PADDING + i * ROW_HEIGHT;
                String ten = names.get(i) == null ? "" : names.get(i);
                if (ten.length() > 26) ten = ten.substring(0, 24) + "...";

                int countVal = i < counts.size() && counts.get(i) != null ? counts.get(i) : 0;

                g2.setColor(UIConstants.TEXT_DARK);
                g2.setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
                g2.drawString(ten, 18, y + 20);

                g2.setColor(RED_KPI);
                g2.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13.5f));
                g2.drawString(String.valueOf(countVal), barX - 32, y + 20);

                g2.setColor(UIConstants.APP_BG);
                g2.fillRoundRect(barX, y + 9, maxBarW, 11, 6, 6);

                int bw = countVal > 0 ? (int) Math.round((countVal * 1.0 / max) * maxBarW) : 5;
                g2.setColor(RED_KPI);
                g2.fillRoundRect(barX, y + 9, Math.max(5, bw), 11, 6, 6);
            }
            g2.dispose();
        }
    }

    private static class GroupedBarChartPanel extends JPanel {
        private List<NhapXuatNgay> list = new ArrayList<>();

        public GroupedBarChartPanel() { setOpaque(false); }

        public void setData(List<NhapXuatNgay> list) {
            this.list = list;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padL = 40, padR = 18, padT = 18, padB = 30;
            int w = getWidth() - padL - padR;
            int h = getHeight() - padT - padB;
            if (w <= 0 || h <= 0) return;

            int max = 10;
            for (NhapXuatNgay nx : list) {
                if (nx.getSoLuongNhap() > max) max = nx.getSoLuongNhap();
                if (nx.getSoLuongXuat() > max) max = nx.getSoLuongXuat();
            }

            g2.setFont(UIConstants.FONT_CAPTION.deriveFont(11.5f));
            for (int i = 0; i <= 4; i++) {
                int y = padT + h - (i * h / 4);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawLine(padL, y, padL + w, y);
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString(String.valueOf(max * i / 4), 10, y + 4);
            }

            if (list.isEmpty()) {
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(UIConstants.FONT_BODY.deriveFont(13f));
                g2.drawString("Chưa có dữ liệu nhập/xuất trong kỳ", padL + w / 2 - 95, padT + h / 2);
                g2.dispose();
                return;
            }

            int n = list.size();
            int step = Math.max(1, w / n);
            int barW = Math.max(6, Math.min(16, (step - 10) / 2));

            for (int i = 0; i < n; i++) {
                NhapXuatNgay item = list.get(i);
                int hNhap = (int) Math.round((item.getSoLuongNhap() * 1.0 / max) * (h - 8));
                int hXuat = (int) Math.round((item.getSoLuongXuat() * 1.0 / max) * (h - 8));

                int xCenter = padL + i * step + step / 2;
                g2.setColor(BLUE_KPI);
                g2.fillRoundRect(xCenter - barW - 1, padT + h - hNhap, barW, Math.max(2, hNhap), 4, 4);
                g2.setColor(RED_KPI);
                g2.fillRoundRect(xCenter + 1, padT + h - hXuat, barW, Math.max(2, hXuat), 4, 4);

                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString(String.format("%02d", item.getNgay().getDayOfMonth()), xCenter - 6, padT + h + 18);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("AUTORA GARAGE - Thống Kê Báo Cáo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(QLThongKe.createPanel());
            f.setSize(1366, 840);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}