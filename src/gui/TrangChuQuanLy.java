package gui;

import controller.DieuHuongPhieu;
import controller.TrangChuQuanLyController;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CenterTextMode;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.RangeType;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Trang chủ Quản lý (JPanel) theo Figma. Phụ trách: Nguyễn Xuân Việt Anh. */
public class TrangChuQuanLy extends JPanel {
    public static final String[] KHOANG_THOI_GIAN = {"Tuần này", "Tháng này", "Tùy chọn"};
    public static final String[] COT_DOANH_THU = {"Ngày", "Số phiếu", "Tiền công", "Phụ tùng", "VAT", "Tổng cộng"};
    private static final String THE_BIEU_DO = "BIEU_DO";
    private static final String THE_TRONG = "TRONG";

    private final JLabel lblNgay = UIComponents.label("", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final JComboBox<String> cboKhoangThoiGian = new JComboBox<>(KHOANG_THOI_GIAN);
    private final JTextField txtTuNgay = new JTextField(9);
    private final JTextField txtDenNgay = new JTextField(9);
    private final JButton btnLoc = UIComponents.nutChinh("Lọc", UIConstants.PRIMARY);
    private final JButton btnXuatExcel = UIComponents.nutPhu("Xuất file Excel");
    private final JButton btnTiepNhan = UIComponents.nutChinh("+  Tiếp nhận xe mới", UIConstants.PRIMARY);
    private final JButton btnBaoCao = UIComponents.nutPhu("Báo cáo thống kê");
    private final JLabel lblLoiLoc = UIComponents.errorLabel();

    private final JLabel lblTieuDeThang = new JLabel();
    private final JLabel lblDoanhThuNgay = soLon();
    private final JLabel lblXuHuongNgay = dongPhu();
    private final JLabel lblDoanhThuThang = soLon();
    private final JLabel lblXuHuongThang = dongPhu();
    private final JLabel lblLuotXe = soLon();
    private final JLabel lblChiTietLuotXe = dongPhu();
    private final JLabel lblHoaDonCho = soLon();
    private final JLabel lblTienCho = dongPhu();
    private final Badge bdgCanXuLy = new Badge("Cần xử lý", UIConstants.STATUS_WARNING);

    private final DefaultPieDataset<String> duLieuTyTrong = new DefaultPieDataset<>();
    private final RingPlot bieuDoTron = new RingPlot(duLieuTyTrong);
    private final CardLayout cardTyTrong = new CardLayout();
    private final JPanel khungTyTrong = UIComponents.trong(cardTyTrong);
    private final JLabel lblTieuDeTyTrong = new JLabel();
    private final JLabel lblTienCong = dongPhu();
    private final JLabel lblPhuTung = dongPhu();
    private final DefaultCategoryDataset duLieuCot = new DefaultCategoryDataset();

    private final JLabel lblTieuDeBang = new JLabel("Doanh thu theo ngày");
    private final DefaultTableModel modelDoanhThu = UIComponents.modelKhongSua(COT_DOANH_THU);
    private final JTable tblDoanhThu = new JTable(modelDoanhThu);
    private final DefaultTableModel modelTonKho = UIComponents.modelKhongSua(
            "Mã SKU", "Tên phụ tùng", "Số lượng hiện tại", "Ngưỡng cảnh báo", "Trạng thái");
    private final JTable tblTonKho = new JTable(modelTonKho);
    private final JLabel lblKhongCanhBao = UIComponents.label("Không có phụ tùng dưới ngưỡng.", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);

    public TrangChuQuanLy() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(taoDau(), 0);
        noiDung.them(lblLoiLoc, 4);
        noiDung.them(taoHangThe(), 12);
        noiDung.them(taoHangBieuDo(), 20);
        noiDung.them(taoTheDoanhThu(), 20);
        noiDung.them(taoTheTonKho(), 20);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);

        JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chan.setBackground(UIConstants.CARD_BG);
        chan.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(10, 8, 10, 16)));
        chan.add(btnBaoCao);
        add(chan, BorderLayout.SOUTH);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel(DieuHuongPhieu dieuHuong) {
        TrangChuQuanLy panel = new TrangChuQuanLy();
        new TrangChuQuanLyController(panel, dieuHuong);
        return panel;
    }

    private static JLabel soLon() {
        return UIComponents.label("—", UIConstants.FONT_PAGE_TITLE.deriveFont(24f), UIConstants.TEXT_DARK);
    }

    private static JLabel dongPhu() {
        return UIComponents.label(" ", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    }

    private JPanel taoDau() {
        JPanel trai = UIComponents.trong(new GridLayout(2, 1, 0, 4));
        trai.add(UIComponents.label("Trang chủ Quản lý", UIConstants.FONT_PAGE_TITLE.deriveFont(24f), UIConstants.TEXT_DARK));
        trai.add(lblNgay);

        UIComponents.styleCombo(cboKhoangThoiGian);
        UIComponents.styleInput(txtTuNgay);
        UIComponents.styleInput(txtDenNgay);
        JPanel phai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.add(oLoc("Khoảng thời gian", cboKhoangThoiGian));
        phai.add(oLoc("Từ ngày", txtTuNgay));
        phai.add(oLoc("Đến ngày", txtDenNgay));
        phai.add(oLoc(" ", btnLoc));
        phai.add(oLoc(" ", btnXuatExcel));
        phai.add(oLoc(" ", btnTiepNhan));

        JPanel dau = UIComponents.trong(new BorderLayout(12, 0));
        dau.add(trai, BorderLayout.WEST);
        dau.add(phai, BorderLayout.EAST);
        return dau;
    }

    private static JPanel oLoc(String nhan, JComponent truong) {
        JPanel o = UIComponents.trong(new BorderLayout(0, 4));
        o.add(UIComponents.label(nhan, UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED), BorderLayout.NORTH);
        o.add(truong, BorderLayout.CENTER);
        return o;
    }

    private JPanel taoHangThe() {
        JPanel dongNo = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dongNo.add(lblTienCho);
        dongNo.add(bdgCanXuLy);
        bdgCanXuLy.setVisible(false);

        JPanel hang = UIComponents.trong(new GridLayout(1, 4, 16, 0));
        hang.add(taoThe(new JLabel("Doanh thu hôm nay"), Loai.RECEIPT, lblDoanhThuNgay, lblXuHuongNgay));
        hang.add(taoThe(lblTieuDeThang, Loai.CHART, lblDoanhThuThang, lblXuHuongThang));
        hang.add(taoThe(new JLabel("Lượt xe hôm nay"), Loai.CAR, lblLuotXe, lblChiTietLuotXe));
        hang.add(taoThe(new JLabel("Hóa đơn chờ thanh toán"), Loai.CLIPBOARD, lblHoaDonCho, dongNo));
        return hang;
    }

    private static JPanel taoThe(JLabel lblTieuDe, Loai loai, JComponent giaTri, JComponent dongDuoi) {
        lblTieuDe.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblTieuDe.setForeground(UIConstants.TEXT_MUTED);
        JPanel dau = UIComponents.trong(new BorderLayout());
        dau.add(lblTieuDe, BorderLayout.CENTER);
        dau.add(new JLabel(new LineIcon(loai, UIConstants.ROLE_MANAGER, 24)), BorderLayout.EAST);
        JPanel than = UIComponents.trong(new GridLayout(3, 1, 0, 6));
        than.setBorder(new EmptyBorder(14, 18, 14, 18));
        than.add(dau);
        than.add(giaTri);
        than.add(dongDuoi);

        JPanel vach = new JPanel();
        vach.setBackground(UIConstants.ROLE_MANAGER);
        vach.setPreferredSize(new Dimension(10, 4));
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(vach, BorderLayout.NORTH);
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoHangBieuDo() {
        JPanel hang = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.weightx = 0.39;
        gc.insets = new Insets(0, 0, 0, 16);
        hang.add(taoTheTyTrong(), gc);
        gc.weightx = 0.61;
        gc.insets = new Insets(0, 16, 0, 0);
        hang.add(taoTheCot(), gc);
        hang.setPreferredSize(new Dimension(100, 320));
        return hang;
    }

    private static JPanel dauBieuDo(JLabel lblTieuDe, String phu) {
        lblTieuDe.setFont(UIConstants.FONT_SECTION_TITLE.deriveFont(18f));
        lblTieuDe.setForeground(UIConstants.TEXT_DARK);
        JPanel dau = UIComponents.trong(new GridLayout(2, 1, 0, 2));
        dau.setBorder(new EmptyBorder(16, 20, 4, 20));
        dau.add(lblTieuDe);
        dau.add(UIComponents.label(phu, UIConstants.FONT_BODY.deriveFont(12f), UIConstants.TEXT_MUTED));
        return dau;
    }

    private JPanel taoTheTyTrong() {
        bieuDoTron.setSectionPaint("Tiền công", UIConstants.ROLE_MANAGER);
        bieuDoTron.setSectionPaint("Phụ tùng", UIConstants.PRIMARY);
        bieuDoTron.setSectionDepth(0.35);
        bieuDoTron.setSeparatorsVisible(false);
        bieuDoTron.setShadowPaint(null);
        bieuDoTron.setLabelGenerator(null);
        bieuDoTron.setBackgroundPaint(UIConstants.CARD_BG);
        bieuDoTron.setOutlineVisible(false);
        bieuDoTron.setCenterTextMode(CenterTextMode.FIXED);
        bieuDoTron.setCenterTextFont(UIConstants.FONT_FORM_LABEL.deriveFont(15f));
        JFreeChart bieuDo = new JFreeChart(null, null, bieuDoTron, false);
        bieuDo.setBackgroundPaint(UIConstants.CARD_BG);

        JPanel chuGiai = UIComponents.trong(new GridLayout(2, 1, 0, 14));
        chuGiai.add(mucChuGiai("Tiền công", UIConstants.ROLE_MANAGER, lblTienCong));
        chuGiai.add(mucChuGiai("Phụ tùng", UIConstants.PRIMARY, lblPhuTung));
        JPanel giuaChuGiai = UIComponents.trong(new GridBagLayout());
        giuaChuGiai.add(chuGiai);

        JPanel coDuLieu = UIComponents.trong(new GridLayout(1, 2, 8, 0));
        coDuLieu.add(new ChartPanel(bieuDo));
        coDuLieu.add(giuaChuGiai);
        JPanel khongCo = UIComponents.trong(new GridBagLayout());
        khongCo.add(UIComponents.label("Chưa có doanh thu trong tháng", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED));
        khungTyTrong.add(coDuLieu, THE_BIEU_DO);
        khungTyTrong.add(khongCo, THE_TRONG);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(dauBieuDo(lblTieuDeTyTrong, "Theo hạng mục"), BorderLayout.NORTH);
        the.add(khungTyTrong, BorderLayout.CENTER);
        return the;
    }

    private static JPanel mucChuGiai(String ten, Color mau, JLabel lblGiaTri) {
        JLabel lblTen = UIComponents.label("●  " + ten, UIConstants.FONT_FORM_LABEL.deriveFont(14f), mau);
        JPanel muc = UIComponents.trong(new GridLayout(2, 1, 0, 2));
        muc.add(lblTen);
        muc.add(lblGiaTri);
        return muc;
    }

    private JPanel taoTheCot() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(duLieuCot);
        plot.setDomainAxis(new CategoryAxis());
        NumberAxis trucGiaTri = new NumberAxis();
        // Khi mọi ngày bằng 0 vẫn giữ trục 0-1 thay vì tự co về các vạch cực nhỏ.
        trucGiaTri.setRangeType(RangeType.POSITIVE);
        trucGiaTri.setAutoRangeMinimumSize(1);
        trucGiaTri.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        plot.setRangeAxis(trucGiaTri);
        BarRenderer veCot = new BarRenderer();
        veCot.setBarPainter(new StandardBarPainter());
        veCot.setShadowVisible(false);
        veCot.setSeriesPaint(0, UIConstants.PRIMARY_LIGHT);
        veCot.setMaximumBarWidth(0.1);
        plot.setRenderer(veCot);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(UIConstants.CARD_BG);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(UIConstants.BORDER_COLOR);
        plot.setDomainGridlinesVisible(false);
        JFreeChart bieuDo = new JFreeChart(null, null, plot, false);
        bieuDo.setBackgroundPaint(UIConstants.CARD_BG);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(dauBieuDo(new JLabel("Doanh thu 7 ngày gần nhất"), "Đơn vị: Triệu đồng"), BorderLayout.NORTH);
        the.add(new ChartPanel(bieuDo), BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheDoanhThu() {
        UIComponents.styleTable(tblDoanhThu);
        canPhai(tblDoanhThu, 1, 2, 3, 4, 5);
        lblTieuDeBang.setIcon(new LineIcon(Loai.RECEIPT, UIConstants.ROLE_MANAGER, 22));
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(UIComponents.tieuDeCard(lblTieuDeBang, null), BorderLayout.NORTH);
        lblTieuDeBang.setForeground(UIConstants.ROLE_MANAGER);
        the.add(UIComponents.cuonBang(tblDoanhThu), BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheTonKho() {
        UIComponents.styleTable(tblTonKho);
        for (int cot = 0; cot < 4; cot++) {
            final int cotPhai = cot;
            tblTonKho.getColumnModel().getColumn(cot).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean chon, boolean focus, int h, int c) {
                    super.getTableCellRendererComponent(t, v, chon, focus, h, c);
                    setBackground(UIComponents.tint(UIConstants.STATUS_DANGER, 0.85f));
                    setForeground(UIConstants.ROLE_RECEPTION);
                    setHorizontalAlignment(cotPhai >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                    setBorder(new EmptyBorder(0, 12, 0, 16));
                    return this;
                }
            });
        }
        UIComponents.canPhaiTieuDe(tblTonKho, 2, 3);
        tblTonKho.getColumnModel().getColumn(4).setCellRenderer(new BadgeRenderer(s -> UIConstants.STATUS_DANGER));
        JLabel lblTieuDe = new JLabel("Cảnh báo tồn kho", new LineIcon(Loai.ALERT, UIConstants.ROLE_TECH_STOCK, 22), SwingConstants.LEFT);
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(UIComponents.tieuDeCard(lblTieuDe, lblKhongCanhBao), BorderLayout.NORTH);
        lblTieuDe.setForeground(UIConstants.ROLE_TECH_STOCK);
        the.add(UIComponents.cuonBang(tblTonKho), BorderLayout.CENTER);
        return the;
    }

    private static void canPhai(JTable bang, int... cot) {
        DefaultTableCellRenderer veCanPhai = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon, boolean focus, int h, int c) {
                super.getTableCellRendererComponent(t, v, chon, focus, h, c);
                setBorder(new EmptyBorder(0, 0, 0, 16));
                return this;
            }
        };
        veCanPhai.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cot) {
            bang.getColumnModel().getColumn(c).setCellRenderer(veCanPhai);
        }
        UIComponents.canPhaiTieuDe(bang, cot);
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setNgay(String ngay) {
        lblNgay.setText(ngay);
    }

    public void setDoanhThuNgay(String giaTri, String xuHuong) {
        lblDoanhThuNgay.setText(giaTri);
        hienXuHuong(lblXuHuongNgay, xuHuong);
    }

    public void setDoanhThuThang(String tieuDe, String giaTri, String xuHuong) {
        lblTieuDeThang.setText(tieuDe);
        lblDoanhThuThang.setText(giaTri);
        hienXuHuong(lblXuHuongThang, xuHuong);
    }

    private static void hienXuHuong(JLabel lbl, String xuHuong) {
        lbl.setText(xuHuong);
        lbl.setForeground(xuHuong.startsWith("▲") ? UIConstants.STATUS_SUCCESS
                : xuHuong.startsWith("▼") ? UIConstants.STATUS_DANGER : UIConstants.TEXT_MUTED);
    }

    public void setLuotXe(int tong, int trongXuong, int daBanGiao) {
        lblLuotXe.setText(String.valueOf(tong));
        lblChiTietLuotXe.setText(trongXuong + " còn trong xưởng - " + daBanGiao + " đã bàn giao");
    }

    public void setHoaDonCho(int soLuong, String tongTien) {
        lblHoaDonCho.setText(String.valueOf(soLuong));
        lblTienCho.setText(tongTien);
        bdgCanXuLy.setVisible(soLuong > 0);
    }

    public void setTyTrong(String tieuDe, double tienCong, double phuTung, String tongRutGon, String moTaTienCong, String moTaPhuTung) {
        lblTieuDeTyTrong.setText(tieuDe);
        boolean coDuLieu = tienCong + phuTung > 0;
        cardTyTrong.show(khungTyTrong, coDuLieu ? THE_BIEU_DO : THE_TRONG);
        if (coDuLieu) {
            duLieuTyTrong.setValue("Tiền công", tienCong);
            duLieuTyTrong.setValue("Phụ tùng", phuTung);
            bieuDoTron.setCenterText(tongRutGon);
            lblTienCong.setText(moTaTienCong);
            lblPhuTung.setText(moTaPhuTung);
        }
    }

    /** nhan[i] là ngày dd/MM, giaTri[i] tính bằng triệu đồng. */
    public void setBieuDoCot(List<String> nhan, List<Double> giaTri) {
        duLieuCot.clear();
        for (int i = 0; i < nhan.size(); i++) {
            duLieuCot.addValue(giaTri.get(i), "Doanh thu", nhan.get(i));
        }
    }

    public void setBangDoanhThu(String tieuDe, List<Object[]> dong) {
        lblTieuDeBang.setText(tieuDe);
        modelDoanhThu.setRowCount(0);
        for (Object[] r : dong) {
            modelDoanhThu.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblDoanhThu, 3, 10);
        revalidate();
    }

    public void setBangTonKho(List<Object[]> dong) {
        modelTonKho.setRowCount(0);
        for (Object[] r : dong) {
            modelTonKho.addRow(r);
        }
        lblKhongCanhBao.setVisible(dong.isEmpty());
        UIComponents.canChieuCaoBang(tblTonKho, 2, 8);
        revalidate();
    }

    public void hienLoiLoc(String thongBao) {
        UIComponents.showError(lblLoiLoc, thongBao);
    }

    public JComboBox<String> getCboKhoangThoiGian() { return cboKhoangThoiGian; }
    public JTextField getTxtTuNgay() { return txtTuNgay; }
    public JTextField getTxtDenNgay() { return txtDenNgay; }
    public JButton getBtnLoc() { return btnLoc; }
    public JButton getBtnXuatExcel() { return btnXuatExcel; }
    public JButton getBtnTiepNhan() { return btnTiepNhan; }
    public JButton getBtnBaoCao() { return btnBaoCao; }
    public DefaultTableModel getModelDoanhThu() { return modelDoanhThu; }
    public DefaultTableModel getModelTonKho() { return modelTonKho; }
}
