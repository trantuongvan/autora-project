package gui;

import controller.TrangChuThuKhoController;
import entity.NhanVien;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;


public class TrangChuThuKho extends JPanel {

    public static final int SIDEBAR_WIDTH = 265;
    public static final Dimension MENU_BTN_SIZE = new Dimension(236, 44);

    public enum MenuThuKho { TRANG_CHU, PHU_TUNG, NHAP_KHO, DUYET_XUAT }

    // TopBar & Sidebar components
    private final JLabel lblAvatar = new JLabel("BV", SwingConstants.CENTER);
    private final JLabel lblTenThuKhoHeader = UIComponents.label("Thủ kho", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = TrangChuKyThuatVien.taoNutDangXuat();

    private NhanVien nhanVienDangNhap;

    private final JButton btnMenuTrangChu = taoNutMenuThuKho("Trang chủ", MenuThuKho.TRANG_CHU, true);
    private final JButton btnMenuPhuTung = taoNutMenuThuKho("Quản lý phụ tùng & tồn kho", MenuThuKho.PHU_TUNG, false);
    private final JButton btnMenuNhapKho = taoNutMenuThuKho("Quản lý nhập kho", MenuThuKho.NHAP_KHO, false);
    private final JLabel lblBadgeSidebarDuyetXuat = taoBadgeSoDo("0");
    private final JButton btnMenuDuyetXuat = taoNutMenuDuyetXuat(false, lblBadgeSidebarDuyetXuat);

    // Workspace components
    private final JLabel lblMoTaChao = new JLabel("Xin chào, đây là tình hình tồn kho và các yêu cầu xuất kho hôm nay.");
    private final JLabel lblSoCanhBaoTon = new JLabel("00");
    private final JLabel lblSoYeuCauCho = new JLabel("00");
    private final JLabel lblSoPhieuNhapHomNay = new JLabel("00");
    private final JLabel lblSoDaXuatHomNay = new JLabel("00");
    private final JLabel lblSubDaXuat = new JLabel("Tính đến hôm nay");

    private final UIComponents.IconField fTimKiem = new UIComponents.IconField(
            "Tìm mã SKU, tên phụ tùng hoặc mã yêu cầu",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );

    // Bảng 1: Yêu cầu xuất kho chờ duyệt
    private final JLabel lblThongKeYeuCau = UIComponents.label("0 yêu cầu", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final DefaultTableModel modelYeuCau = UIComponents.modelKhongSua(
            "Mã yêu cầu", "Phiếu sửa chữa", "Kỹ thuật viên", "Phụ tùng", "Trạng thái", "Thao tác"
    );
    private final JTable tblYeuCau = new JTable(modelYeuCau);
    private JScrollPane spBangYeuCau;

    private final JButton btnXemTatCaYeuCau = new JButton("<html><u>Xem tất cả</u></html>");
    private final JLabel lblCapNhatLuc = UIComponents.label("Cập nhật lúc --:--", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final JButton btnLamMoi = UIComponents.nutIcon(
            new UIComponents.LineIcon(UIComponents.Loai.REFRESH, UIConstants.PRIMARY, 16)
    );

    // Bảng 2: Phụ tùng dưới ngưỡng cảnh báo
    private final JButton btnXemKho = taoNutLinkXanh("Xem kho →");
    private final DefaultTableModel modelCanhBao = UIComponents.modelKhongSua(
            "Mã SKU", "Tên phụ tùng", "Hãng", "Vị trí kho", "Tồn / Ngưỡng", "Thao tác"
    );
    private final JTable tblCanhBao = new JTable(modelCanhBao);
    private JScrollPane spBangCanhBao;

    public TrangChuThuKho() {
        this(null);
    }

    public TrangChuThuKho(NhanVien nv) {
        this.nhanVienDangNhap = nv;
        if (nv != null) {
            capNhatHeaderThuKho(lblAvatar, lblTenThuKhoHeader, nv.getHoTen());
            String ten = nv.getHoTen();
            String tenGoi = ten.contains(" ") ? ten.substring(ten.lastIndexOf(' ') + 1) : ten;
            lblMoTaChao.setText("Xin chào " + tenGoi + ", đây là tình hình tồn kho và các yêu cầu xuất kho hôm nay.");
        }
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
    }

    private static TrangChuThuKhoController activeController;

    public static TrangChuThuKhoController getActiveController() {
        return activeController;
    }

    public static JPanel createPanel(NhanVien nv) {
        TrangChuThuKho view = new TrangChuThuKho(nv);
        activeController = new TrangChuThuKhoController(view);
        return view;
    }

    public static JPanel createPanel() {
        return createPanel(null);
    }

    private void buildUI() {



        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        UIComponents.Badge badgeCa = new UIComponents.Badge("Ca làm việc • 08:00 – 17:00", UIConstants.ROLE_RECEPTION_LIGHT);
        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Tổng quan kho",
                lblMoTaChao,
                UIConstants.ROLE_RECEPTION,
                badgeCa
        );
        root.them(pnlHeader, 0);

        // 4 thẻ KPI
        JPanel pnlCards = UIComponents.trong(new GridLayout(1, 4, 18, 0));
        pnlCards.add(taoTheKPI("Cảnh báo tồn kho", lblSoCanhBaoTon, new JLabel("Phụ tùng chạm ngưỡng tối thiểu"),
                Color.decode("#E54837"), UIComponents.Loai.CLIPBOARD));
        pnlCards.add(taoTheKPI("Yêu cầu chờ xuất", lblSoYeuCauCho, new JLabel("Cần duyệt xuất kho"),
                Color.decode("#2185D5"), UIComponents.Loai.WRENCH));
        pnlCards.add(taoTheKPI("Phiếu nhập hôm nay", lblSoPhieuNhapHomNay, new JLabel("Từ nhà cung cấp"),
                Color.decode("#E69916"), UIComponents.Loai.KEY));
        pnlCards.add(taoTheKPI("Đã xuất hôm nay", lblSoDaXuatHomNay, lblSubDaXuat,
                Color.decode("#1CA449"), UIComponents.Loai.CHECK));
        root.them(pnlCards, 20);

        // Thanh tìm kiếm
        JPanel pnlSearchWrap = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fTimKiem.setPreferredSize(new Dimension(420, 42));
        pnlSearchWrap.add(fTimKiem);
        root.them(pnlSearchWrap, 20);

        // Card 1: Yêu cầu xuất kho chờ duyệt
        UIComponents.RoundedPanel cardYeuCau = UIComponents.card();
        cardYeuCau.setLayout(new BorderLayout());
        cardYeuCau.add(UIComponents.tieuDeCard(new JLabel("Yêu cầu xuất kho chờ duyệt"), lblThongKeYeuCau), BorderLayout.NORTH);

        cauhinhBangYeuCau();
        spBangYeuCau = UIComponents.cuonBang(tblYeuCau);
        spBangYeuCau.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cardYeuCau.add(spBangYeuCau, BorderLayout.CENTER);
        modelYeuCau.addTableModelListener(e -> SwingUtilities.invokeLater(() -> capNhatChieuCaoBang(tblYeuCau, spBangYeuCau)));

        JPanel pnlFooterYC = UIComponents.trong(new BorderLayout());
        pnlFooterYC.setBorder(new EmptyBorder(12, 22, 12, 22));
        btnXemTatCaYeuCau.setFont(UIConstants.FONT_BODY);
        btnXemTatCaYeuCau.setForeground(UIConstants.TEXT_MUTED);
        btnXemTatCaYeuCau.setBorderPainted(false);
        btnXemTatCaYeuCau.setContentAreaFilled(false);
        btnXemTatCaYeuCau.setFocusPainted(false);
        btnXemTatCaYeuCau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlFooterYC.add(btnXemTatCaYeuCau, BorderLayout.WEST);

        JPanel pnlRightFooter = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRightFooter.add(lblCapNhatLuc);
        pnlRightFooter.add(btnLamMoi);
        pnlFooterYC.add(pnlRightFooter, BorderLayout.EAST);
        cardYeuCau.add(pnlFooterYC, BorderLayout.SOUTH);
        root.them(cardYeuCau, 18);

        // Card 2: Phụ tùng dưới ngưỡng cảnh báo
        UIComponents.RoundedPanel cardCanhBao = UIComponents.card();
        cardCanhBao.setLayout(new BorderLayout());
        cardCanhBao.add(UIComponents.tieuDeCard(new JLabel("Phụ tùng dưới ngưỡng cảnh báo"), btnXemKho), BorderLayout.NORTH);

        cauhinhBangCanhBao();
        spBangCanhBao = UIComponents.cuonBang(tblCanhBao);
        spBangCanhBao.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cardCanhBao.add(spBangCanhBao, BorderLayout.CENTER);
        modelCanhBao.addTableModelListener(e -> SwingUtilities.invokeLater(() -> capNhatChieuCaoBang(tblCanhBao, spBangCanhBao)));

        SwingUtilities.invokeLater(() -> {
            capNhatChieuCaoBang(tblYeuCau, spBangYeuCau);
            capNhatChieuCaoBang(tblCanhBao, spBangCanhBao);
        });

        root.them(cardCanhBao, 20);
        root.ketThuc();
        add(UIComponents.cuonTrang(root), BorderLayout.CENTER);
    }

    private void capNhatChieuCaoBang(JTable table, JScrollPane sp) {
        if (sp == null) return;
        int rowCount = table.getRowCount();
        int headerH = table.getTableHeader().getPreferredSize().height;
        int totalH = headerH + rowCount * table.getRowHeight() + 2;
        Dimension d = new Dimension(100, totalH);
        table.setPreferredScrollableViewportSize(new Dimension(100, rowCount * table.getRowHeight()));
        sp.setPreferredSize(d);
        sp.setMinimumSize(d);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalH));
        sp.revalidate();
        revalidate();
        repaint();
    }

    private void cauhinhBangYeuCau() {
        UIComponents.styleTable(tblYeuCau);
        UIComponents.datDoRong(tblYeuCau, 110, 120, 150, 260, 130, 120);

        tblYeuCau.getColumnModel().getColumn(0).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null)
        );
        tblYeuCau.getColumnModel().getColumn(4).setCellRenderer(
                new UIComponents.BadgeRenderer(this::mauTrangThaiYeuCau)
        );
        tblYeuCau.getColumnModel().getColumn(5).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(13f), s -> UIConstants.PRIMARY)
        );
        tblYeuCau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void cauhinhBangCanhBao() {
        UIComponents.styleTable(tblCanhBao);
        UIComponents.datDoRong(tblCanhBao, 120, 240, 130, 120, 130, 120);

        Color bgRowRed = Color.decode("#FFF4F4");
        DefaultTableCellRenderer redRowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (c == 0) {
                    setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
                    setForeground(UIConstants.TEXT_DARK);
                } else if (c == 4) {
                    setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
                    setForeground(UIConstants.STATUS_DANGER);
                } else if (c == 5) {
                    setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13f));
                    setForeground(UIConstants.PRIMARY);
                } else {
                    setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
                    setForeground(UIConstants.TEXT_DARK);
                }
                setBackground(sel ? t.getSelectionBackground() : bgRowRed);
                return this;
            }
        };

        for (int i = 0; i < tblCanhBao.getColumnCount(); i++) {
            tblCanhBao.getColumnModel().getColumn(i).setCellRenderer(redRowRenderer);
        }
        tblCanhBao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Color mauTrangThaiYeuCau(String s) {
        if (s == null) return UIConstants.TEXT_MUTED;
        if (s.startsWith("Đã xuất")) return UIConstants.STATUS_SUCCESS;
        if (s.startsWith("Chờ xuất")) return UIConstants.STATUS_WARNING;
        if (s.startsWith("Từ chối")) return UIConstants.STATUS_DANGER;
        return UIConstants.TEXT_MUTED;
    }

    private JPanel taoTheKPI(String tieuDe, JLabel lblSoLon, JLabel lblMoTaNho, Color mauNen, UIComponents.Loai loaiIcon) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(14, mauNen, null);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(220, 122));

        JPanel top = UIComponents.trong(new BorderLayout());
        top.add(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE.deriveFont(16f), Color.WHITE), BorderLayout.WEST);

        UIComponents.RoundedPanel iconBox = new UIComponents.RoundedPanel(10, new Color(255, 255, 255, 235), null);
        iconBox.setPreferredSize(new Dimension(34, 34));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(new UIComponents.LineIcon(loaiIcon, mauNen, 18)));
        top.add(iconBox, BorderLayout.EAST);

        JPanel bottom = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblSoLon.setFont(UIConstants.FONT_PAGE_TITLE.deriveFont(Font.BOLD, 34f));
        lblSoLon.setForeground(Color.WHITE);
        lblMoTaNho.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblMoTaNho.setForeground(new Color(255, 255, 255, 225));
        lblMoTaNho.setBorder(new EmptyBorder(12, 12, 0, 0));

        bottom.add(lblSoLon);
        bottom.add(lblMoTaNho);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JButton taoNutLinkXanh(String text) {
        JButton b = new JButton(text);
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13f));
        b.setForeground(UIConstants.PRIMARY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel taoTopBarThuKho(String tieuDeTrang, JLabel lblAvatar, JLabel lblTenHeader, JButton btnDangXuat) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.CARD_BG);
        topBar.setPreferredSize(new Dimension(0, 64));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        JPanel pnlBrand = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 16, 12));
        pnlBrand.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 64));

        UIComponents.RoundedPanel logoBox = new UIComponents.RoundedPanel(10, UIConstants.PRIMARY, null);
        logoBox.setPreferredSize(new Dimension(38, 38));
        logoBox.setLayout(new GridBagLayout());
        logoBox.add(new JLabel(new UIComponents.LineIcon(UIComponents.Loai.WRENCH, Color.WHITE, 20)));

        JPanel pnlBrandText = UIComponents.trong(new GridLayout(2, 1, 0, 0));
        pnlBrandText.add(UIComponents.label("AUTORA", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.PRIMARY));
        pnlBrandText.add(UIComponents.label("GARAGE", UIConstants.FONT_CAPTION.deriveFont(Font.BOLD, 10f), UIConstants.TEXT_MUTED));

        pnlBrand.add(logoBox);
        pnlBrand.add(pnlBrandText);
        topBar.add(pnlBrand, BorderLayout.WEST);

        JPanel pnlCenter = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 12, 18));
        pnlCenter.add(UIComponents.label(tieuDeTrang, UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK));
        topBar.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlRight = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        pnlRight.setBorder(new EmptyBorder(0, 0, 0, 16));

        JPanel avatarCircle = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.82f));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(38, 38));
        lblAvatar.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 13f));
        lblAvatar.setForeground(UIConstants.ROLE_RECEPTION);
        avatarCircle.add(lblAvatar, BorderLayout.CENTER);

        JPanel pnlUserInfo = UIComponents.trong(new GridLayout(2, 1, 0, 2));
        pnlUserInfo.add(lblTenHeader);
        pnlUserInfo.add(UIComponents.label("Thủ kho", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.ROLE_RECEPTION));

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(UIConstants.BORDER_COLOR);

        pnlRight.add(avatarCircle);
        pnlRight.add(pnlUserInfo);
        pnlRight.add(sep);
        pnlRight.add(btnDangXuat);
        topBar.add(pnlRight, BorderLayout.EAST);

        return topBar;
    }

    public static JPanel taoSidebarThuKho(JButton btnTrangChu, JButton btnPhuTung, JButton btnNhapKho, JButton btnDuyetXuat) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.CARD_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        JPanel pnlTop = UIComponents.trong(new GridBagLayout());
        pnlTop.setBorder(new EmptyBorder(20, 14, 20, 14));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 8, 4, 8);
        pnlTop.add(UIComponents.label("KHU VỰC LÀM VIỆC", UIConstants.FONT_CAPTION.deriveFont(Font.BOLD, 11f), UIConstants.TEXT_MUTED), gc);

        gc.gridy++;
        gc.insets = new Insets(0, 8, 18, 8);
        pnlTop.add(UIComponents.label("Thủ kho", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.ROLE_RECEPTION), gc);

        JButton[] buttons = {btnTrangChu, btnPhuTung, btnNhapKho, btnDuyetXuat};
        for (JButton b : buttons) {
            b.setPreferredSize(MENU_BTN_SIZE);
            b.setMinimumSize(MENU_BTN_SIZE);
            b.setMaximumSize(MENU_BTN_SIZE);
            gc.gridy++;
            gc.insets = new Insets(0, 0, 6, 0);
            pnlTop.add(b, gc);
        }

        sidebar.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlFooter = UIComponents.trong(new GridLayout(3, 1, 0, 5));
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(16, 20, 16, 20)
        ));
        pnlFooter.add(UIComponents.label("AUTORA • Bình Thạnh", UIConstants.FONT_FORM_LABEL.deriveFont(12.5f), UIConstants.TEXT_DARK));
        pnlFooter.add(UIComponents.label(TrangChuKyThuatVien.layChuoiNgayTiengViet(), UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED));
        pnlFooter.add(UIComponents.label("\u25CF  Hệ thống sẵn sàng", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.STATUS_SUCCESS));

        sidebar.add(pnlFooter, BorderLayout.SOUTH);
        return sidebar;
    }

    public static JButton taoNutMenuThuKho(String text, MenuThuKho loai, boolean active) {
        Color fg = active ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_DARK;
        Color bg = active ? UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.88f) : UIConstants.CARD_BG;

        JButton btn = new JButton(text, new ThuKhoMenuIcon(loai, fg)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                } else if (getModel().isRollover()) {
                    g2.setColor(UIComponents.tint(UIConstants.APP_BG, 0.5f));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setFont(active ? UIConstants.FONT_FORM_LABEL.deriveFont(13f) : UIConstants.FONT_BODY.deriveFont(13f));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(0, 12, 0, 12));
        btn.setPreferredSize(MENU_BTN_SIZE);
        btn.setMinimumSize(MENU_BTN_SIZE);
        btn.setMaximumSize(MENU_BTN_SIZE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton taoNutMenuDuyetXuat(boolean active, JLabel lblBadge) {
        JButton btn = taoNutMenuThuKho("Duyệt xuất kho", MenuThuKho.DUYET_XUAT, active);
        btn.setLayout(new BorderLayout());
        JPanel pnlRight = UIComponents.trong(new GridBagLayout());
        pnlRight.add(lblBadge);
        btn.add(pnlRight, BorderLayout.EAST);
        return btn;
    }

    public static JLabel taoBadgeSoDo(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.ROLE_RECEPTION);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(UIConstants.FONT_CAPTION.deriveFont(Font.BOLD, 11f));
        l.setForeground(Color.WHITE);
        l.setPreferredSize(new Dimension(22, 20));
        return l;
    }

    public static void capNhatHeaderThuKho(JLabel lblAvatar, JLabel lblTen, String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return;
        lblTen.setText(hoTen.trim());
        String[] parts = hoTen.trim().split("\\s+");
        String initials = parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                : ("" + parts[0].charAt(0)).toUpperCase();
        lblAvatar.setText(initials);
    }

    public static void chuyenManHinhThuKho(JComponent currentView, MenuThuKho dichDen, String thamSoMa) {
        Window w0 = SwingUtilities.getWindowAncestor(currentView);
        if (w0 instanceof GiaoDienChinh) {
            GiaoDienChinh gdc = (GiaoDienChinh) w0;
            switch (dichDen) {
                case TRANG_CHU:
                    gdc.moCard("TRANG_CHU_KHO");
                    break;
                case PHU_TUNG:
                    gdc.moCard("QL_PHU_TUNG");
                    break;
                case NHAP_KHO:
                    gdc.moCard("QL_NHAP_KHO");
                    if (thamSoMa != null && QLNhapKho.getActiveController() != null) {
                        SwingUtilities.invokeLater(() -> QLNhapKho.getActiveController().moPopupLapPhieuNhap(thamSoMa));
                    }
                    break;
                case DUYET_XUAT:
                    gdc.moCard("QL_DUYET_XUAT");
                    if (thamSoMa != null && QLDuyetXuatKho.getActiveController() != null) {
                        QLDuyetXuatKho.getActiveController().chonYeuCau(thamSoMa);
                    }
                    break;
            }
            return;
        }
        Container parent = currentView.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            CardLayout cl = (CardLayout) parent.getLayout();
            switch (dichDen) {
                case TRANG_CHU: cl.show(parent, "TRANG_CHU_THU_KHO"); break;
                case PHU_TUNG: cl.show(parent, "QL_PHU_TUNG"); break;
                case NHAP_KHO: cl.show(parent, "QL_NHAP_KHO"); break;
                case DUYET_XUAT: cl.show(parent, "QL_DUYET_XUAT"); break;
            }
            return;
        }

        Window w = SwingUtilities.getWindowAncestor(currentView);
        if (w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            JPanel nextPanel = null;
            switch (dichDen) {
                case TRANG_CHU:
                    nextPanel = TrangChuThuKho.createPanel();
                    break;
                case PHU_TUNG:
                    nextPanel = QLPhuTung.createPanel();
                    break;
                case NHAP_KHO:
                    nextPanel = QLNhapKho.createPanel();
                    if (thamSoMa != null && QLNhapKho.getActiveController() != null) {
                        SwingUtilities.invokeLater(() -> QLNhapKho.getActiveController().moPopupLapPhieuNhap(thamSoMa));
                    }
                    break;
                case DUYET_XUAT:
                    nextPanel = QLDuyetXuatKho.createPanel();
                    if (thamSoMa != null && QLDuyetXuatKho.getActiveController() != null) {
                        QLDuyetXuatKho.getActiveController().chonYeuCau(thamSoMa);
                    }
                    break;
            }
            if (nextPanel != null) {
                frame.setContentPane(nextPanel);
                frame.revalidate();
                frame.repaint();
            }
        }
    }

    public static class ThuKhoMenuIcon implements Icon {
        private final MenuThuKho loai;
        private final Color color;

        public ThuKhoMenuIcon(MenuThuKho loai, Color color) {
            this.loai = loai;
            this.color = color;
        }

        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            if (loai == MenuThuKho.TRANG_CHU) {
                g2.draw(new RoundRectangle2D.Float(2, 2, 5.5f, 5.5f, 1.5f, 1.5f));
                g2.draw(new RoundRectangle2D.Float(10.5f, 2, 5.5f, 5.5f, 1.5f, 1.5f));
                g2.draw(new RoundRectangle2D.Float(2, 10.5f, 5.5f, 5.5f, 1.5f, 1.5f));
                g2.draw(new RoundRectangle2D.Float(10.5f, 10.5f, 5.5f, 5.5f, 1.5f, 1.5f));
            } else {
                Path2D cube = new Path2D.Float();
                cube.moveTo(9, 2);
                cube.lineTo(15.5, 5.5);
                cube.lineTo(15.5, 12.5);
                cube.lineTo(9, 16);
                cube.lineTo(2.5, 12.5);
                cube.lineTo(2.5, 5.5);
                cube.closePath();
                g2.draw(cube);
                g2.draw(new Line2D.Float(2.5f, 5.5f, 9f, 9f));
                g2.draw(new Line2D.Float(15.5f, 5.5f, 9f, 9f));
                g2.draw(new Line2D.Float(9f, 9f, 9f, 16f));
            }
            g2.dispose();
        }
    }

    // Getters cho Controller
    public JLabel getLblAvatar() { return lblAvatar; }
    public JLabel getLblTenThuKhoHeader() { return lblTenThuKhoHeader; }
    public JButton getBtnDangXuat() { return btnDangXuat; }
    public JButton getBtnMenuTrangChu() { return btnMenuTrangChu; }
    public JButton getBtnMenuPhuTung() { return btnMenuPhuTung; }
    public JButton getBtnMenuNhapKho() { return btnMenuNhapKho; }
    public JButton getBtnMenuDuyetXuat() { return btnMenuDuyetXuat; }
    public JLabel getLblBadgeSidebarDuyetXuat() { return lblBadgeSidebarDuyetXuat; }

    public JLabel getLblMoTaChao() { return lblMoTaChao; }
    public JLabel getLblSoCanhBaoTon() { return lblSoCanhBaoTon; }
    public JLabel getLblSoYeuCauCho() { return lblSoYeuCauCho; }
    public JLabel getLblSoPhieuNhapHomNay() { return lblSoPhieuNhapHomNay; }
    public JLabel getLblSoDaXuatHomNay() { return lblSoDaXuatHomNay; }
    public JLabel getLblSubDaXuat() { return lblSubDaXuat; }
    public JTextField getTxtTimKiem() { return fTimKiem.getTextField(); }

    public JLabel getLblThongKeYeuCau() { return lblThongKeYeuCau; }
    public DefaultTableModel getModelYeuCau() { return modelYeuCau; }
    public JTable getTblYeuCau() { return tblYeuCau; }
    public JButton getBtnXemTatCaYeuCau() { return btnXemTatCaYeuCau; }
    public JLabel getLblCapNhatLuc() { return lblCapNhatLuc; }
    public JButton getBtnLamMoi() { return btnLamMoi; }

    public JButton getBtnXemKho() { return btnXemKho; }
    public DefaultTableModel getModelCanhBao() { return modelCanhBao; }
    public JTable getTblCanhBao() { return tblCanhBao; }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AUTORA GARAGE - Trang Chủ Thủ Kho");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(TrangChuThuKho.createPanel());
            frame.setSize(1366, 840);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}