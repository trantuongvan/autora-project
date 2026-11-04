package gui;

import entity.TrangThaiPhieu;
import controller.TrangChuKyThuatVienController;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình 1: Trang chủ Kỹ thuật viên (Đầy đủ TopBar + Left Sidebar + Bảng tự co giãn chiều cao).
 */
public class TrangChuKyThuatVien extends JPanel {

    // TopBar & Sidebar components
    private final JLabel lblAvatar = new JLabel("TV", SwingConstants.CENTER);
    private final JLabel lblTenKTVHeader = UIComponents.label("Lê Văn Thợ Máy", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = taoNutDangXuat();
    private final JButton btnMenuTrangChu = taoNutMenuSidebar("Trang chủ", true, true);
    private final JButton btnMenuTienDo = taoNutMenuSidebar("<html>Quản lý tiến độ sửa<br>chữa</html>", false, false);

    // Workspace components
    private final JLabel lblMoTaChao = new JLabel("Xin chào, đây là các hạng mục sửa chữa được giao cho bạn hôm nay.");
    private final JLabel lblSoChoSua = new JLabel("00");
    private final JLabel lblSoDangSua = new JLabel("00");
    private final JLabel lblSoChoPhuTung = new JLabel("00");
    private final JLabel lblSoHoanThanh = new JLabel("00");
    private final JLabel lblSubHoanThanh = new JLabel("Tính đến hôm nay");

    private final UIComponents.IconField fTimKiem = new UIComponents.IconField(
            "Tìm mã phiếu, biển số hoặc hạng mục dịch vụ",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );

    private final JLabel lblThongKeBang = UIComponents.label("0 phiếu gần nhất", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã phiếu", "Biển số", "Xe", "Hạng mục dịch vụ", "Trạng thái", "Thao tác"
    );

    // Bảng tự động kéo dài theo số lượng dòng dữ liệu thực tế
    private final JTable tblCongViec = new JTable(tableModel) {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            int rows = Math.max(1, getRowCount());
            return new Dimension(super.getPreferredScrollableViewportSize().width, rows * getRowHeight());
        }
    };
    private JScrollPane spBang;

    private final JButton btnXemTatCa = new JButton("<html><u>Xem tất cả</u></html>");
    private final JLabel lblCapNhatLuc = UIComponents.label("Cập nhật lúc --:--", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final JButton btnLamMoi = UIComponents.nutIcon(
            new UIComponents.LineIcon(UIComponents.Loai.REFRESH, UIConstants.PRIMARY, 16)
    );

    public TrangChuKyThuatVien() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
    }

    private static TrangChuKyThuatVienController activeController;

    public static JPanel createPanel() {
        TrangChuKyThuatVien view = new TrangChuKyThuatVien();
        activeController = new TrangChuKyThuatVienController(view);
        return view;
    }

    public static TrangChuKyThuatVienController getActiveController() {
        return activeController;
    }

    private void buildUI() {



        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        UIComponents.Badge badgeCa = new UIComponents.Badge("Ca làm việc • 08:00 – 17:00", UIConstants.ROLE_RECEPTION_LIGHT);
        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Tổng quan công việc",
                lblMoTaChao,
                UIConstants.ROLE_RECEPTION,
                badgeCa
        );
        root.them(pnlHeader, 0);

        JPanel pnlCards = UIComponents.trong(new GridLayout(1, 4, 18, 0));
        pnlCards.add(taoTheKPI("Chờ sửa", lblSoChoSua, new JLabel("Phiếu chưa bắt đầu"),
                Color.decode("#E54837"), UIComponents.Loai.CLIPBOARD));
        pnlCards.add(taoTheKPI("Đang sửa", lblSoDangSua, new JLabel("Đang thực hiện sửa chữa"),
                Color.decode("#2185D5"), UIComponents.Loai.WRENCH));
        pnlCards.add(taoTheKPI("Chờ phụ tùng", lblSoChoPhuTung, new JLabel("Chờ Thủ kho xuất kho"),
                Color.decode("#E69916"), UIComponents.Loai.KEY));
        pnlCards.add(taoTheKPI("Hoàn thành hôm nay", lblSoHoanThanh, lblSubHoanThanh,
                Color.decode("#1CA449"), UIComponents.Loai.CHECK));
        root.them(pnlCards, 20);

        JPanel pnlSearchWrap = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fTimKiem.setPreferredSize(new Dimension(420, 42));
        pnlSearchWrap.add(fTimKiem);
        root.them(pnlSearchWrap, 20);

        UIComponents.RoundedPanel cardBang = UIComponents.card();
        cardBang.setLayout(new BorderLayout());

        JLabel lblTieuDeBang = new JLabel("Công việc được giao");
        cardBang.add(UIComponents.tieuDeCard(lblTieuDeBang, lblThongKeBang), BorderLayout.NORTH);

        cauhinhBang();
        spBang = UIComponents.cuonBang(tblCongViec);
        cardBang.add(spBang, BorderLayout.CENTER);

        JPanel pnlFooter = UIComponents.trong(new BorderLayout());
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(12, 22, 12, 22)
        ));

        btnXemTatCa.setFont(UIConstants.FONT_BODY);
        btnXemTatCa.setForeground(UIConstants.TEXT_MUTED);
        btnXemTatCa.setBorderPainted(false);
        btnXemTatCa.setContentAreaFilled(false);
        btnXemTatCa.setFocusPainted(false);
        btnXemTatCa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlFooter.add(btnXemTatCa, BorderLayout.WEST);

        JPanel pnlRightFooter = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRightFooter.add(lblCapNhatLuc);
        pnlRightFooter.add(btnLamMoi);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);

        cardBang.add(pnlFooter, BorderLayout.SOUTH);
        root.them(cardBang, 18);

        root.ketThuc();
        add(UIComponents.cuonTrang(root), BorderLayout.CENTER);
    }

    /** Cập nhật lại chiều cao bảng mỗi khi thêm/lọc dữ liệu để bảng tự kéo dài và hiện thanh cuộn khi tràn. */
    public void capNhatChieuCaoBang() {
        int rows = Math.max(1, tblCongViec.getRowCount());
        int height = 44 + rows * tblCongViec.getRowHeight() + 2;
        if (spBang != null) {
            spBang.setPreferredSize(new Dimension(100, height));
            spBang.setMinimumSize(new Dimension(100, height));
            spBang.revalidate();
        }
        revalidate();
        repaint();
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.CARD_BG);
        topBar.setPreferredSize(new Dimension(0, 64));
        topBar.setBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        JPanel pnlBrand = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 16, 12));
        pnlBrand.setPreferredSize(new Dimension(240, 64));

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
        pnlCenter.add(UIComponents.label("Trang chủ", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK));
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
        pnlUserInfo.add(lblTenKTVHeader);
        pnlUserInfo.add(UIComponents.label("Kỹ thuật viên", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.ROLE_RECEPTION));

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

    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.CARD_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));
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
        pnlTop.add(UIComponents.label("Kỹ thuật viên", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.ROLE_RECEPTION), gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 6, 0);
        pnlTop.add(btnMenuTrangChu, gc);

        gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);
        pnlTop.add(btnMenuTienDo, gc);

        sidebar.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlFooter = UIComponents.trong(new GridLayout(3, 1, 0, 5));
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(16, 20, 16, 20)
        ));
        pnlFooter.add(UIComponents.label("AUTORA • Bình Thạnh", UIConstants.FONT_FORM_LABEL.deriveFont(12.5f), UIConstants.TEXT_DARK));
        pnlFooter.add(UIComponents.label(layChuoiNgayTiengViet(), UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED));
        pnlFooter.add(UIComponents.label("\u25CF  Hệ thống sẵn sàng", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.STATUS_SUCCESS));

        sidebar.add(pnlFooter, BorderLayout.SOUTH);
        return sidebar;
    }

    public static String layChuoiNgayTiengViet() {
        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();
        String thu;
        switch (dow) {
            case MONDAY: thu = "Thứ Hai"; break;
            case TUESDAY: thu = "Thứ Ba"; break;
            case WEDNESDAY: thu = "Thứ Tư"; break;
            case THURSDAY: thu = "Thứ Năm"; break;
            case FRIDAY: thu = "Thứ Sáu"; break;
            case SATURDAY: thu = "Thứ Bảy"; break;
            default: thu = "Chủ Nhật"; break;
        }
        return thu + ", " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static JButton taoNutMenuSidebar(String text, boolean isGridIcon, boolean active) {
        Color fg = active ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_DARK;
        Color bg = active ? UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.88f) : UIConstants.CARD_BG;

        JButton btn = new JButton(text, new SidebarMenuIcon(isGridIcon, fg)) {
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
        btn.setIconTextGap(12);
        btn.setFont(active ? UIConstants.FONT_FORM_LABEL.deriveFont(13.5f) : UIConstants.FONT_BODY.deriveFont(13.5f));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton taoNutDangXuat() {
        JButton b = new JButton("Đăng xuất", new LogoutIcon(UIConstants.TEXT_MUTED));
        b.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        b.setForeground(UIConstants.TEXT_DARK);
        b.setIconTextGap(6);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void capNhatThongTinKTVHeader(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return;
        lblTenKTVHeader.setText(hoTen.trim());
        String[] parts = hoTen.trim().split("\\s+");
        String initials = parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                : ("" + parts[0].charAt(0)).toUpperCase();
        lblAvatar.setText(initials);
    }

    private JPanel taoTheKPI(String tieuDe, JLabel lblSoLon, JLabel lblMoTaNho, Color mauNen, UIComponents.Loai loaiIcon) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(14, mauNen, null);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setPreferredSize(new Dimension(220, 122));

        JPanel top = UIComponents.trong(new BorderLayout());
        JLabel lblTitle = UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE.deriveFont(16f), Color.WHITE);
        top.add(lblTitle, BorderLayout.WEST);

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

    private void cauhinhBang() {
        UIComponents.styleTable(tblCongViec);
        UIComponents.datDoRong(tblCongViec, 130, 110, 170, 260, 150, 130);

        tblCongViec.getColumnModel().getColumn(0).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null)
        );
        tblCongViec.getColumnModel().getColumn(4).setCellRenderer(
                new UIComponents.BadgeRenderer(this::mauTrangThai)
        );
        tblCongViec.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13f));
                setForeground(UIConstants.PRIMARY);
                setBackground(sel ? t.getSelectionBackground() : t.getBackground());
                return this;
            }
        });
        tblCongViec.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Color mauTrangThai(String s) {
        if (s == null) return UIConstants.TEXT_MUTED;
        if (s.startsWith(TrangThaiPhieu.HOAN_THANH.getText())) return UIConstants.STATUS_SUCCESS;
        if (s.startsWith(TrangThaiPhieu.DANG_SUA.getText())) return UIConstants.PRIMARY;
        if (s.startsWith(TrangThaiPhieu.CHO_PHU_TUNG.getText())) return UIConstants.STATUS_WARNING;
        if (s.startsWith("Chờ sửa") || s.startsWith(TrangThaiPhieu.CHO_KIEM_TRA.getText())) return UIConstants.STATUS_DANGER;
        return UIConstants.TEXT_MUTED;
    }

    public static class SidebarMenuIcon implements Icon {
        private final boolean isGrid;
        private final Color color;

        public SidebarMenuIcon(boolean isGrid, Color color) {
            this.isGrid = isGrid;
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
            if (isGrid) {
                g2.draw(new RoundRectangle2D.Float(2, 2, 5.5f, 5.5f, 2, 2));
                g2.draw(new RoundRectangle2D.Float(10.5f, 2, 5.5f, 5.5f, 2, 2));
                g2.draw(new RoundRectangle2D.Float(2, 10.5f, 5.5f, 5.5f, 2, 2));
                g2.draw(new RoundRectangle2D.Float(10.5f, 10.5f, 5.5f, 5.5f, 2, 2));
            } else {
                g2.draw(new Ellipse2D.Float(2, 3, 3, 3));
                g2.draw(new Ellipse2D.Float(2, 7.5f, 3, 3));
                g2.draw(new Ellipse2D.Float(2, 12, 3, 3));
                g2.draw(new Line2D.Float(7.5f, 4.5f, 16, 4.5f));
                g2.draw(new Line2D.Float(7.5f, 9f, 16, 9f));
                g2.draw(new Line2D.Float(7.5f, 13.5f, 16, 13.5f));
            }
            g2.dispose();
        }
    }

    public static class LogoutIcon implements Icon {
        private final Color color;
        public LogoutIcon(Color color) { this.color = color; }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D p = new Path2D.Float();
            p.moveTo(7, 3);
            p.lineTo(3, 3);
            p.lineTo(3, 15);
            p.lineTo(7, 15);
            g2.draw(p);
            g2.draw(new Line2D.Float(7, 9, 15, 9));
            g2.draw(new Line2D.Float(12, 6, 15, 9));
            g2.draw(new Line2D.Float(12, 12, 15, 9));
            g2.dispose();
        }
    }

    // Getters cho Controller
    public JButton getBtnMenuTrangChu() { return btnMenuTrangChu; }
    public JButton getBtnMenuTienDo() { return btnMenuTienDo; }
    public JButton getBtnDangXuat() { return btnDangXuat; }
    public JLabel getLblMoTaChao() { return lblMoTaChao; }
    public JLabel getLblSoChoSua() { return lblSoChoSua; }
    public JLabel getLblSoDangSua() { return lblSoDangSua; }
    public JLabel getLblSoChoPhuTung() { return lblSoChoPhuTung; }
    public JLabel getLblSoHoanThanh() { return lblSoHoanThanh; }
    public JLabel getLblSubHoanThanh() { return lblSubHoanThanh; }
    public JTextField getTxtTimKiem() { return fTimKiem.getTextField(); }
    public JLabel getLblThongKeBang() { return lblThongKeBang; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTblCongViec() { return tblCongViec; }
    public JButton getBtnXemTatCa() { return btnXemTatCa; }
    public JLabel getLblCapNhatLuc() { return lblCapNhatLuc; }
    public JButton getBtnLamMoi() { return btnLamMoi; }

}