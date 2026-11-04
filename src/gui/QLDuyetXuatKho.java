package gui;

import controller.QLDuyetXuatKhoController;
import entity.PhieuYeuCauXuatKho;
import entity.TrangThaiYeuCauXuat;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QLDuyetXuatKho extends JPanel {

    private static QLDuyetXuatKhoController activeController;

    // TopBar & Sidebar
    private final JLabel lblAvatar = new JLabel("BV", SwingConstants.CENTER);
    private final JLabel lblTenThuKhoHeader = UIComponents.label("Bùi Thái Vy", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = TrangChuKyThuatVien.taoNutDangXuat();

    private final JButton btnMenuTrangChu = TrangChuThuKho.taoNutMenuThuKho("Trang chủ", TrangChuThuKho.MenuThuKho.TRANG_CHU, false);
    private final JButton btnMenuPhuTung = TrangChuThuKho.taoNutMenuThuKho("Quản lý phụ tùng & tồn kho", TrangChuThuKho.MenuThuKho.PHU_TUNG, false);
    private final JButton btnMenuNhapKho = TrangChuThuKho.taoNutMenuThuKho("Quản lý nhập kho", TrangChuThuKho.MenuThuKho.NHAP_KHO, false);
    private final JLabel lblBadgeSidebarDuyetXuat = TrangChuThuKho.taoBadgeSoDo("0");
    private final JButton btnMenuDuyetXuat = TrangChuThuKho.taoNutMenuDuyetXuat(true, lblBadgeSidebarDuyetXuat);

    // Header badge
    private final UIComponents.Badge badgeSoYeuCauCho = new UIComponents.Badge("— yêu cầu chờ duyệt", UIConstants.ROLE_RECEPTION_LIGHT);

    // Cột trái: Yêu cầu xuất kho
    private final JLabel lblDemYeuCauTrai = UIComponents.label("—", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
    private final UIComponents.IconField fTimKiem = new UIComponents.IconField(
            "Tìm mã yêu cầu hoặc mã phiếu",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();
    private final TrangThaiYeuCauXuat[] CAC_TAB = TrangThaiYeuCauXuat.values();
    private final JButton[] btnTabs = new JButton[CAC_TAB.length];
    private final JPanel pnlDanhSachYeuCau = new JPanel();

    // Cột phải: Chi tiết yêu cầu
    private final UIComponents.Badge badgeTrangThai = new UIComponents.Badge("Chờ xuất", UIConstants.STATUS_WARNING);
    private final JLabel lblMaYeuCau = UIComponents.label("---", UIConstants.FONT_PAGE_TITLE.deriveFont(Font.BOLD, 24f), UIConstants.TEXT_DARK);
    private final JLabel lblThongTinPhu = UIComponents.label("---", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED);

    private final JLabel lblValPhieuSC = giaTri("---");
    private final JLabel lblValXe = giaTri("---");
    private final JLabel lblValKTV = giaTri("---");
    private final JLabel lblValNgayYC = giaTri("---");

    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã SKU", "Tên phụ tùng", "Vị trí kho", "SL yêu cầu", "Tồn hiện tại"
    );
    private final JTable tblKiemTraTon = new JTable(tableModel);
    private JScrollPane spTon;

    private final UIComponents.RoundedPanel pnlHopLoiTonKho = new UIComponents.RoundedPanel(
            8, UIComponents.tint(UIConstants.STATUS_DANGER, 0.90f), UIConstants.STATUS_DANGER
    );
    private final JLabel lblLoiTonKho = UIComponents.label(
            "Tồn kho trên hệ thống không đủ, vui lòng kiểm tra lại số liệu.",
            UIConstants.FONT_FORM_LABEL.deriveFont(13f),
            UIComponents.shade(UIConstants.STATUS_DANGER, 0.75f)
    );

    private final JLabel lblGhiChuTruTon = UIComponents.label("Tồn kho sẽ được trừ ngay khi xác nhận xuất kho.", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
    private final JButton btnTuChoi = new NutVienDo("Từ chối");
    private final JButton btnXacNhanXuat = UIComponents.nutChinh("Xác nhận xuất kho", UIConstants.ROLE_MANAGER);

    public QLDuyetXuatKho() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
    }

    public static JPanel createPanel() {
        QLDuyetXuatKho view = new QLDuyetXuatKho();
        activeController = new QLDuyetXuatKhoController(view);
        return view;
    }

    public static QLDuyetXuatKhoController getActiveController() {
        return activeController;
    }

    private void buildUI() {



        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Duyệt xuất kho",
                "Chọn yêu cầu để xem chi tiết và xác nhận xuất kho.",
                UIConstants.ROLE_RECEPTION,
                badgeSoYeuCauCho
        );
        root.them(pnlHeader, 0);

        // --- CỘT TRÁI: YÊU CẦU XUẤT KHO ---
        UIComponents.RoundedPanel cardTrai = UIComponents.card();
        cardTrai.setLayout(new BorderLayout(0, 12));
        cardTrai.setBorder(new EmptyBorder(18, 18, 18, 18));

        UIComponents.CotDoc pnlTopTrai = new UIComponents.CotDoc();
        JPanel pnlTitleTrai = UIComponents.trong(new BorderLayout());
        pnlTitleTrai.add(UIComponents.label("Yêu cầu xuất kho", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        pnlTitleTrai.add(lblDemYeuCauTrai, BorderLayout.EAST);
        pnlTopTrai.them(pnlTitleTrai, 0);

        JPanel pnlTabWrap = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlTabWrap.setBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        for (int i = 0; i < CAC_TAB.length; i++) {
            btnTabs[i] = taoTab(CAC_TAB[i].getText(), i == 0);
            pnlTabWrap.add(btnTabs[i]);
        }
        pnlTopTrai.them(pnlTabWrap, 8);

        fTimKiem.setPreferredSize(new Dimension(100, 40));
        JPanel hangTim = UIComponents.trong(new BorderLayout(8, 0));
        hangTim.add(fTimKiem, BorderLayout.CENTER);
        hangTim.add(btnTaiDuLieu, BorderLayout.EAST);
        pnlTopTrai.them(hangTim, 12);
        cardTrai.add(pnlTopTrai, BorderLayout.NORTH);

        pnlDanhSachYeuCau.setLayout(new BoxLayout(pnlDanhSachYeuCau, BoxLayout.Y_AXIS));
        pnlDanhSachYeuCau.setOpaque(false);
        JPanel pnlWrapList = UIComponents.trong(new BorderLayout());
        pnlWrapList.add(pnlDanhSachYeuCau, BorderLayout.NORTH);
        cardTrai.add(pnlWrapList, BorderLayout.CENTER);

        // --- CỘT PHẢI: CHI TIẾT YÊU CẦU ---
        UIComponents.RoundedPanel cardPhai = UIComponents.card();
        cardPhai.setLayout(new BorderLayout());
        cardPhai.setBorder(new EmptyBorder(20, 24, 22, 24));

        UIComponents.CotDoc pnlChiTiet = new UIComponents.CotDoc();

        JPanel pnlHeadPhai = UIComponents.trong(new BorderLayout());
        pnlHeadPhai.add(UIComponents.label("Chi tiết yêu cầu", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        pnlHeadPhai.add(badgeTrangThai, BorderLayout.EAST);
        pnlChiTiet.them(pnlHeadPhai, 0);

        pnlChiTiet.them(lblMaYeuCau, 14);
        pnlChiTiet.them(lblThongTinPhu, 4);

        pnlChiTiet.them(dongThongTinNgang("Phiếu sửa chữa", lblValPhieuSC), 14);
        pnlChiTiet.them(dongThongTinNgang("Xe", lblValXe), 0);
        pnlChiTiet.them(dongThongTinNgang("Kỹ thuật viên", lblValKTV), 0);
        pnlChiTiet.them(dongThongTinNgang("Ngày yêu cầu", lblValNgayYC), 0);

        pnlChiTiet.them(UIComponents.label("Kiểm tra tồn kho", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.TEXT_DARK), 18);

        UIComponents.styleTable(tblKiemTraTon);
        tblKiemTraTon.setRowHeight(46);
        UIComponents.datDoRong(tblKiemTraTon, 95, 235, 95, 90, 95);
        spTon = UIComponents.cuonBang(tblKiemTraTon);
        spTon.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        spTon.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(this::capNhatChieuCaoBangTon));
        capNhatChieuCaoBangTon();
        pnlChiTiet.them(spTon, 10);

        pnlHopLoiTonKho.setLayout(new BorderLayout(10, 0));
        pnlHopLoiTonKho.setBorder(new EmptyBorder(10, 14, 10, 14));
        pnlHopLoiTonKho.add(new JLabel(new UIComponents.LineIcon(UIComponents.Loai.ALERT, UIConstants.STATUS_DANGER, 18)), BorderLayout.WEST);
        pnlHopLoiTonKho.add(lblLoiTonKho, BorderLayout.CENTER);
        pnlHopLoiTonKho.setVisible(false);
        pnlChiTiet.them(pnlHopLoiTonKho, 12);

        pnlChiTiet.them(lblGhiChuTruTon, 12);

        JPanel pnlActions = UIComponents.trong(new BorderLayout());
        pnlActions.add(btnTuChoi, BorderLayout.WEST);
        pnlActions.add(btnXacNhanXuat, BorderLayout.EAST);
        pnlChiTiet.them(pnlActions, 14);

        pnlChiTiet.ketThuc();
        cardPhai.add(pnlChiTiet, BorderLayout.CENTER);

        // Khóa tỷ lệ 2 cột Trái (37%) - Phải (63%) và giữ chiều cao đồng nhất kể cả khi không có dữ liệu
        JPanel pnlNoiDung = UIComponents.trong(new TwoColumnLayout(0.37, 20));
        pnlNoiDung.add(cardTrai);
        pnlNoiDung.add(cardPhai);

        root.them(pnlNoiDung, 18);
        root.ketThuc();
        add(UIComponents.cuonTrang(root), BorderLayout.CENTER);
    }


    public void capNhatChieuCaoBangTon() {
        if (spTon == null) return;
        int headerH = tblKiemTraTon.getTableHeader().getPreferredSize().height;
        int totalRowsH = 0;
        for (int r = 0; r < tblKiemTraTon.getRowCount(); r++) {
            totalRowsH += tblKiemTraTon.getRowHeight(r);
        }
        int minRowsH = 3 * 46;
        int totalH = headerH + Math.max(minRowsH, totalRowsH) + 2;
        Dimension d = new Dimension(100, totalH);
        spTon.setPreferredSize(d);
        spTon.setMinimumSize(d);
        spTon.revalidate();
        revalidate();
        repaint();
    }

    private JPanel dongThongTinNgang(String nhan, JLabel lblGiaTri) {
        JPanel row = UIComponents.trong(new BorderLayout(12, 0));
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIComponents.tint(UIConstants.BORDER_COLOR, 0.55f)),
                new EmptyBorder(10, 0, 10, 0)
        ));
        JLabel l = UIComponents.label(nhan, UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_MUTED);
        l.setPreferredSize(new Dimension(150, 22));
        l.setVerticalAlignment(SwingConstants.TOP);
        lblGiaTri.setVerticalAlignment(SwingConstants.TOP);
        row.add(l, BorderLayout.WEST);
        row.add(lblGiaTri, BorderLayout.CENTER);
        return row;
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

    public void capNhatStyleTab(JButton b, boolean active) {
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
        b.setForeground(active ? UIConstants.ROLE_RECEPTION : UIConstants.TEXT_MUTED);
        b.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, active ? 3 : 0, 0, UIConstants.ROLE_RECEPTION),
                new EmptyBorder(8, 2, 8, 2)
        ));
    }

    private JLabel giaTri(String text) {
        return UIComponents.label(text, UIConstants.FONT_BODY.deriveFont(13.5f), UIConstants.TEXT_DARK);
    }

    public JPanel taoTheYeuCau(PhieuYeuCauXuatKho yc, String tenKTV, boolean active) {
        Color vien = active ? UIConstants.ROLE_RECEPTION_LIGHT : UIConstants.BORDER_COLOR;
        Color nen = active ? UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.94f) : UIConstants.CARD_BG;

        UIComponents.RoundedPanel item = new UIComponents.RoundedPanel(8, nen, vien);
        item.setLayout(new BorderLayout(8, 4));
        item.setBorder(new EmptyBorder(12, 16, 12, 16));
        item.setPreferredSize(new Dimension(100, 68));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = UIComponents.trong(new GridLayout(2, 1, 0, 4));
        left.add(UIComponents.label(yc.getMaPhieuYC(), UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK));
        left.add(UIComponents.label(yc.getPhieu().getMaPhieuTNSC(), UIConstants.FONT_BODY.deriveFont(12f), UIConstants.TEXT_MUTED));

        JPanel center = UIComponents.trong(new GridBagLayout());
        center.add(UIComponents.label(tenKTV, UIConstants.FONT_FORM_LABEL.deriveFont(13f), UIConstants.TEXT_DARK));

        JPanel right = UIComponents.trong(new GridBagLayout());
        Color cTrangThai = yc.getTrangThai() == TrangThaiYeuCauXuat.CHO_XUAT ? UIConstants.STATUS_WARNING
                : (yc.getTrangThai() == TrangThaiYeuCauXuat.DA_XUAT ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER);
        right.add(new UIComponents.Badge(yc.getTrangThai().getText(), cTrangThai));

        item.add(left, BorderLayout.WEST);
        item.add(center, BorderLayout.CENTER);
        item.add(right, BorderLayout.EAST);
        return item;
    }

    private static class TwoColumnLayout implements LayoutManager {
        private final double leftRatio;
        private final int gap;

        public TwoColumnLayout(double leftRatio, int gap) {
            this.leftRatio = leftRatio;
            this.gap = gap;
        }

        @Override public void addLayoutComponent(String name, Component comp) {}
        @Override public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets in = parent.getInsets();
                int h = 580; // Khóa chiều cao tối thiểu 580px để có dữ liệu hay không có dữ liệu đều giữ nguyên khung
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    Component c = parent.getComponent(i);
                    if (c.isVisible()) h = Math.max(h, c.getPreferredSize().height);
                }
                return new Dimension(800, h + in.top + in.bottom);
            }
        }

        @Override public Dimension minimumLayoutSize(Container parent) { return new Dimension(400, 580); }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                if (parent.getComponentCount() < 2) return;
                Insets in = parent.getInsets();
                int availW = Math.max(0, parent.getWidth() - in.left - in.right);
                int availH = Math.max(0, parent.getHeight() - in.top - in.bottom);
                int leftW = (int) Math.round((availW - gap) * leftRatio);
                int rightW = Math.max(0, availW - gap - leftW);
                parent.getComponent(0).setBounds(in.left, in.top, leftW, availH);
                parent.getComponent(1).setBounds(in.left + leftW + gap, in.top, rightW, availH);
            }
        }
    }

    private static class NutVienDo extends JButton {
        public NutVienDo(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(14f));
            setForeground(UIConstants.ROLE_RECEPTION);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(UIConstants.TEXT_MUTED);
            } else {
                g2.setColor(getModel().isRollover() ? UIComponents.tint(UIConstants.ROLE_RECEPTION, 0.92f) : UIConstants.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(UIConstants.ROLE_RECEPTION_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(getForeground());
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
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

    public UIComponents.Badge getBadgeSoYeuCauCho() { return badgeSoYeuCauCho; }
    public JLabel getLblDemYeuCauTrai() { return lblDemYeuCauTrai; }
    public JTextField getTxtTimKiem() { return fTimKiem.getTextField(); }
    public TrangThaiYeuCauXuat[] getCacTab() { return CAC_TAB; }
    public JButton[] getBtnTabs() { return btnTabs; }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
    public JPanel getPnlDanhSachYeuCau() { return pnlDanhSachYeuCau; }
    public UIComponents.Badge getBadgeTrangThai() { return badgeTrangThai; }
    public JLabel getLblMaYeuCau() { return lblMaYeuCau; }
    public JLabel getLblThongTinPhu() { return lblThongTinPhu; }
    public JLabel getLblValPhieuSC() { return lblValPhieuSC; }
    public JLabel getLblValXe() { return lblValXe; }
    public JLabel getLblValKTV() { return lblValKTV; }
    public JLabel getLblValNgayYC() { return lblValNgayYC; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTblKiemTraTon() { return tblKiemTraTon; }
    public JPanel getPnlHopLoiTonKho() { return pnlHopLoiTonKho; }
    public JButton getBtnTuChoi() { return btnTuChoi; }
    public JButton getBtnXacNhanXuat() { return btnXacNhanXuat; }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AUTORA GARAGE - Duyệt Xuất Kho");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(QLDuyetXuatKho.createPanel());
            frame.setSize(1366, 840);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}