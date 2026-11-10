package gui;

import controller.QLNhapKhoController;
import entity.NhaCungCap;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;


public class QLNhapKho extends JPanel {

    private static QLNhapKhoController activeController;

    // TopBar & Sidebar
    private final JLabel lblAvatar = new JLabel("BV", SwingConstants.CENTER);
    private final JLabel lblTenThuKhoHeader = UIComponents.label("Bùi Thái Vy", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = TrangChuKyThuatVien.taoNutDangXuat();

    private final JButton btnMenuTrangChu = TrangChuThuKho.taoNutMenuThuKho("Trang chủ", TrangChuThuKho.MenuThuKho.TRANG_CHU, false);
    private final JButton btnMenuPhuTung = TrangChuThuKho.taoNutMenuThuKho("Quản lý phụ tùng & tồn kho", TrangChuThuKho.MenuThuKho.PHU_TUNG, false);
    private final JButton btnMenuNhapKho = TrangChuThuKho.taoNutMenuThuKho("Quản lý nhập kho", TrangChuThuKho.MenuThuKho.NHAP_KHO, true);
    private final JLabel lblBadgeSidebarDuyetXuat = TrangChuThuKho.taoBadgeSoDo("0");
    private final JButton btnMenuDuyetXuat = TrangChuThuKho.taoNutMenuDuyetXuat(false, lblBadgeSidebarDuyetXuat);

    // LayeredPane phủ Modal Lập phiếu nhập kho ở giữa màn hình
    private final JLayeredPane layeredPane = new JLayeredPane();
    private final JPanel pnlMainBody = new JPanel(new BorderLayout());
    private final JPanel pnlModalOverlay = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(18, 24, 32, 125));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    };

    // Thành phần màn hình chính Lịch sử phiếu nhập
    private final JButton btnTaoPhieuMoi = UIComponents.nutChinh("Tạo phiếu mới", UIConstants.PRIMARY);
    private final UIComponents.IconField fTimPhieu = new UIComponents.IconField(
            "Tìm mã phiếu hoặc nhà cung cấp",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final JComboBox<String> cboLocNCC = new JComboBox<>(new String[]{"Nhà cung cấp: Tất cả"});
    private final UIComponents.IconField fTuNgay = new UIComponents.IconField(
            "Từ ngày",
            new UIComponents.LineIcon(UIComponents.Loai.CALENDAR, UIConstants.TEXT_MUTED, 15),
            false
    );
    private final UIComponents.IconField fDenNgay = new UIComponents.IconField(
            "Đến ngày",
            new UIComponents.LineIcon(UIComponents.Loai.CALENDAR, UIConstants.TEXT_MUTED, 15),
            false
    );

    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Số mặt hàng", "Tổng tiền", "Thủ kho", "Thao tác"
    );
    private final JTable tblPhieuNhap = new JTable(tableModel);
    private JScrollPane spBang;

    private final JLabel lblFooterSoPhieu = UIComponents.label("Hiển thị 0 / 0 phiếu", UIConstants.FONT_CAPTION.deriveFont(12.5f), UIConstants.TEXT_MUTED);
    private final JLabel lblCapNhatLuc = UIComponents.label("Cập nhật lúc --:--", UIConstants.FONT_CAPTION.deriveFont(12.5f), UIConstants.TEXT_MUTED);
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();

    // Thành phần Modal Popup Lập phiếu nhập kho
    private final JLabel lblMaPhieuNhapModal = UIComponents.label("PN031120260001", UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
    private final JLabel lblNgayNhapModal = UIComponents.label("03/11/2026 14:35", UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
    private final JComboBox<NhaCungCap> cboModalNCC = new JComboBox<>();
    private final JLabel lblThuKhoModal = UIComponents.label("Bùi Thái Vy", UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK);

    private final UIComponents.IconField fTimPhuTungModal = new UIComponents.IconField(
            "Tìm phụ tùng cần nhập (mã SKU hoặc tên) - Nhấn Enter để thêm",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final JPanel pnlDanhSachChiTietModal = new JPanel();
    private final JLabel lblLoiModal = UIComponents.errorLabel();
    private final JLabel lblTongTienModal = UIComponents.label("Tổng tiền nhập   0đ", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 16f), UIConstants.TEXT_DARK);

    private final JButton btnNhapExcel = UIComponents.nutPhu("Nhập từ Excel");
    private final JButton btnDongModal = UIComponents.nutPhu("×");
    private final JButton btnHuyModal = new NutVienDo("Hủy");
    private final JButton btnLuuPhieuNhap = UIComponents.nutChinh("Lưu phiếu nhập", UIConstants.ROLE_MANAGER);

    public QLNhapKho() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
    }

    public static JPanel createPanel() {
        QLNhapKho view = new QLNhapKho();
        activeController = new QLNhapKhoController(view);
        return view;
    }

    public static QLNhapKhoController getActiveController() {
        return activeController;
    }

    private void buildUI() {
        pnlMainBody.setBackground(UIConstants.APP_BG);


        pnlMainBody.add(buildWorkspace(), BorderLayout.CENTER);

        buildModalLapPhieu();

        layeredPane.add(pnlMainBody, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(pnlModalOverlay, JLayeredPane.PALETTE_LAYER);
        pnlModalOverlay.setVisible(false);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                pnlMainBody.setBounds(0, 0, w, h);
                pnlModalOverlay.setBounds(0, 0, w, h);
                layeredPane.revalidate();
            }
        });

        add(layeredPane, BorderLayout.CENTER);
    }

    private JComponent buildWorkspace() {
        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        UIComponents.Badge badgeCa = new UIComponents.Badge("Ca làm việc • 08:00 – 17:00", UIConstants.ROLE_RECEPTION_LIGHT);
        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Quản lý nhập kho",
                "Theo dõi lịch sử phiếu nhập kho và tạo phiếu nhập mới.",
                UIConstants.ROLE_RECEPTION,
                badgeCa
        );
        root.them(pnlHeader, 0);

        UIComponents.RoundedPanel cardBang = UIComponents.card();
        cardBang.setLayout(new BorderLayout());

        // Phần đầu Card: Tiêu đề + Nút "+ Tạo phiếu mới" + Hàng bộ lọc
        JPanel pnlCardTop = UIComponents.trong(new BorderLayout(0, 14));
        pnlCardTop.setBorder(new EmptyBorder(18, 22, 14, 22));

        JPanel pnlTitleRow = UIComponents.trong(new BorderLayout());
        pnlTitleRow.add(UIComponents.label("Lịch sử phiếu nhập", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        pnlTitleRow.add(btnTaoPhieuMoi, BorderLayout.EAST);
        pnlCardTop.add(pnlTitleRow, BorderLayout.NORTH);

        // Kéo rộng cboLocNCC lên 270px để không bị che tên Nhà cung cấp dài khi xổ xuống
        JPanel pnlFilterRow = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fTimPhieu.setPreferredSize(new Dimension(260, 40));
        UIComponents.styleCombo(cboLocNCC);
        cboLocNCC.setPreferredSize(new Dimension(270, 40));
        fTuNgay.setPreferredSize(new Dimension(140, 40));
        fDenNgay.setPreferredSize(new Dimension(140, 40));
        util.ChonNgay.ganNgay(fTuNgay.getTextField(), fDenNgay.getTextField());

        pnlFilterRow.add(fTimPhieu);
        pnlFilterRow.add(cboLocNCC);
        pnlFilterRow.add(fTuNgay);
        pnlFilterRow.add(fDenNgay);
        pnlFilterRow.add(btnTaiDuLieu);
        pnlCardTop.add(pnlFilterRow, BorderLayout.CENTER);

        cardBang.add(pnlCardTop, BorderLayout.NORTH);

        cauhinhBang();
        spBang = UIComponents.cuonBang(tblPhieuNhap);
        spBang.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cardBang.add(spBang, BorderLayout.CENTER);
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(this::capNhatChieuCaoBang));

        JPanel pnlFooter = UIComponents.trong(new BorderLayout());
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(12, 22, 12, 22)
        ));
        pnlFooter.add(lblFooterSoPhieu, BorderLayout.WEST);

        JPanel pnlRightFooter = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRightFooter.add(new QLPhuTung.PaginationButton("‹", false));
        pnlRightFooter.add(new QLPhuTung.PaginationButton("1", true));
        pnlRightFooter.add(new QLPhuTung.PaginationButton("›", false));
        pnlRightFooter.add(Box.createHorizontalStrut(8));
        pnlRightFooter.add(lblCapNhatLuc);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);

        cardBang.add(pnlFooter, BorderLayout.SOUTH);
        root.them(cardBang, 18);

        root.ketThuc();
        return UIComponents.cuonTrang(root);
    }

    private void buildModalLapPhieu() {
        pnlModalOverlay.setOpaque(false);
        pnlModalOverlay.addMouseListener(new MouseAdapter() {});

        UIComponents.RoundedPanel modal = new UIComponents.RoundedPanel(14, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
        modal.setLayout(new BorderLayout(0, 14));
        modal.setBorder(new EmptyBorder(22, 26, 22, 26));
        // Danh sách cần đủ chỗ để kiểm tra các dòng nhập từ Excel, nhưng không vượt quá màn hình laptop.
        int caoModal = Math.min(720, Toolkit.getDefaultToolkit().getScreenSize().height - 200);
        modal.setPreferredSize(new Dimension(800, caoModal));

        JPanel pnlModalHead = UIComponents.trong(new BorderLayout());
        pnlModalHead.add(UIComponents.label("Lập phiếu nhập kho", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 20f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        btnDongModal.setPreferredSize(new Dimension(32, 32));
        btnDongModal.setBorder(new EmptyBorder(2, 8, 2, 8));
        pnlModalHead.add(btnDongModal, BorderLayout.EAST);
        modal.add(pnlModalHead, BorderLayout.NORTH);

        JPanel pnlModalBody = UIComponents.trong(new BorderLayout(0, 12));

        JPanel pnlGridMeta = UIComponents.trong(new GridLayout(2, 2, 14, 12));

        UIComponents.RoundedPanel boxMaPhieu = UIComponents.box(UIComponents.tint(UIConstants.APP_BG, 0.55f));
        boxMaPhieu.setLayout(new GridLayout(3, 1, 0, 2));
        boxMaPhieu.setBorder(new EmptyBorder(8, 14, 8, 14));
        boxMaPhieu.add(UIComponents.label("Mã phiếu nhập", UIConstants.FONT_CAPTION.deriveFont(11.5f), UIConstants.TEXT_MUTED));
        boxMaPhieu.add(lblMaPhieuNhapModal);
        boxMaPhieu.add(UIComponents.label("Tự động phát sinh", UIConstants.FONT_CAPTION.deriveFont(10.5f), UIConstants.TEXT_MUTED));

        UIComponents.RoundedPanel boxNgayNhap = UIComponents.box(UIComponents.tint(UIConstants.APP_BG, 0.55f));
        boxNgayNhap.setLayout(new GridLayout(2, 1, 0, 4));
        boxNgayNhap.setBorder(new EmptyBorder(10, 14, 10, 14));
        boxNgayNhap.add(UIComponents.label("Ngày nhập", UIConstants.FONT_CAPTION.deriveFont(11.5f), UIConstants.TEXT_MUTED));
        boxNgayNhap.add(lblNgayNhapModal);

        UIComponents.RoundedPanel boxNCC = UIComponents.box(UIComponents.tint(UIConstants.APP_BG, 0.55f));
        boxNCC.setLayout(new BorderLayout(0, 4));
        boxNCC.setBorder(new EmptyBorder(8, 14, 8, 14));
        boxNCC.add(UIComponents.label("Nhà cung cấp", UIConstants.FONT_CAPTION.deriveFont(11.5f), UIConstants.TEXT_MUTED), BorderLayout.NORTH);
        UIComponents.styleCombo(cboModalNCC);
        cboModalNCC.setPreferredSize(new Dimension(240, 32));
        boxNCC.add(cboModalNCC, BorderLayout.CENTER);

        UIComponents.RoundedPanel boxThuKho = UIComponents.box(UIComponents.tint(UIConstants.APP_BG, 0.55f));
        boxThuKho.setLayout(new GridLayout(2, 1, 0, 4));
        boxThuKho.setBorder(new EmptyBorder(10, 14, 10, 14));
        boxThuKho.add(UIComponents.label("Thủ kho", UIConstants.FONT_CAPTION.deriveFont(11.5f), UIConstants.TEXT_MUTED));
        boxThuKho.add(lblThuKhoModal);

        pnlGridMeta.add(boxMaPhieu);
        pnlGridMeta.add(boxNgayNhap);
        pnlGridMeta.add(boxNCC);
        pnlGridMeta.add(boxThuKho);

        JPanel pnlMetaAndSearch = UIComponents.trong(new BorderLayout(0, 12));
        pnlMetaAndSearch.add(pnlGridMeta, BorderLayout.NORTH);
        fTimPhuTungModal.setPreferredSize(new Dimension(100, 40));
        btnNhapExcel.setToolTipText("Dòng 1 là tiêu đề. Cột A: mã SKU, cột B: số lượng, cột C: đơn giá.");
        JPanel pnlTimVaExcel = UIComponents.trong(new BorderLayout(10, 0));
        pnlTimVaExcel.add(fTimPhuTungModal, BorderLayout.CENTER);
        pnlTimVaExcel.add(btnNhapExcel, BorderLayout.EAST);
        pnlMetaAndSearch.add(pnlTimVaExcel, BorderLayout.SOUTH);
        pnlModalBody.add(pnlMetaAndSearch, BorderLayout.NORTH);

        JPanel pnlTableWrap = new JPanel(new BorderLayout());
        pnlTableWrap.setBackground(UIConstants.CARD_BG);
        pnlTableWrap.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        pnlTableWrap.add(taoHeaderBangChiTietModal(), BorderLayout.NORTH);

        pnlDanhSachChiTietModal.setLayout(new BoxLayout(pnlDanhSachChiTietModal, BoxLayout.Y_AXIS));
        pnlDanhSachChiTietModal.setBackground(UIConstants.CARD_BG);
        JPanel pnlNorthRows = new JPanel(new BorderLayout());
        pnlNorthRows.setBackground(UIConstants.CARD_BG);
        pnlNorthRows.add(pnlDanhSachChiTietModal, BorderLayout.NORTH);

        JScrollPane spChiTiet = new JScrollPane(pnlNorthRows);
        spChiTiet.setBorder(null);
        spChiTiet.getViewport().setBackground(UIConstants.CARD_BG);
        spChiTiet.getVerticalScrollBar().setUnitIncrement(16);
        pnlTableWrap.add(spChiTiet, BorderLayout.CENTER);

        pnlModalBody.add(pnlTableWrap, BorderLayout.CENTER);
        modal.add(pnlModalBody, BorderLayout.CENTER);

        JPanel pnlModalFoot = UIComponents.trong(new BorderLayout(0, 10));
        lblLoiModal.setFont(UIConstants.FONT_BODY.deriveFont(12.5f));
        pnlModalFoot.add(lblLoiModal, BorderLayout.NORTH);

        JPanel pnlTotalAndBtns = UIComponents.trong(new BorderLayout(0, 10));
        JPanel pnlRightTotal = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRightTotal.add(lblTongTienModal);
        pnlTotalAndBtns.add(pnlRightTotal, BorderLayout.NORTH);

        JPanel pnlRightBtns = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightBtns.add(btnHuyModal);
        pnlRightBtns.add(btnLuuPhieuNhap);
        pnlTotalAndBtns.add(pnlRightBtns, BorderLayout.SOUTH);

        pnlModalFoot.add(pnlTotalAndBtns, BorderLayout.SOUTH);
        modal.add(pnlModalFoot, BorderLayout.SOUTH);

        pnlModalOverlay.add(modal);
    }

    private JPanel taoHeaderBangChiTietModal() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UIComponents.tint(UIConstants.APP_BG, 0.45f));
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)
        ));
        Font f = UIConstants.FONT_FORM_LABEL.deriveFont(12.5f);
        Color c = UIConstants.TEXT_MUTED;
        header.add(UIComponents.label("Mã SKU", f, c), cotModal(0, 0.16));
        header.add(UIComponents.label("Tên phụ tùng", f, c), cotModal(1, 0.34));
        header.add(UIComponents.label("Số lượng nhập", f, c), cotModal(2, 0.14));
        header.add(UIComponents.label("Đơn giá nhập", f, c), cotModal(3, 0.14));
        header.add(UIComponents.label("Thành tiền", f, c), cotModal(4, 0.16));
        header.add(UIComponents.label("Xóa", f, c), cotModal(5, 0.06));
        return header;
    }

    public static GridBagConstraints cotModal(int x, double weightx) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = 0;
        gc.weightx = weightx;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 4, 0, 4);
        return gc;
    }

    public void hienThiModalLapPhieu(boolean mo) {
        pnlModalOverlay.setVisible(mo);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private void capNhatChieuCaoBang() {
        if (spBang == null) return;
        int rowCount = Math.max(3, tblPhieuNhap.getRowCount());
        int headerH = tblPhieuNhap.getTableHeader().getPreferredSize().height;
        int totalH = headerH + rowCount * tblPhieuNhap.getRowHeight() + 2;
        Dimension d = new Dimension(100, totalH);
        spBang.setPreferredSize(d);
        spBang.setMinimumSize(d);
        spBang.revalidate();
        revalidate();
        repaint();
    }

    private void cauhinhBang() {
        UIComponents.styleTable(tblPhieuNhap);
        UIComponents.datDoRong(tblPhieuNhap, 140, 130, 260, 100, 120, 140, 120);

        tblPhieuNhap.getColumnModel().getColumn(0).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), null)
        );
        tblPhieuNhap.getColumnModel().getColumn(4).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), null)
        );
        tblPhieuNhap.getColumnModel().getColumn(6).setCellRenderer(
                new UIComponents.CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(13f), s -> UIConstants.PRIMARY)
        );
        tblPhieuNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            setBorder(new EmptyBorder(9, 18, 9, 18));
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

    public JButton getBtnTaoPhieuMoi() { return btnTaoPhieuMoi; }
    public JTextField getTxtTimPhieu() { return fTimPhieu.getTextField(); }
    public JComboBox<String> getCboLocNCC() { return cboLocNCC; }
    public JTextField getTxtTuNgay() { return fTuNgay.getTextField(); }
    public JTextField getTxtDenNgay() { return fDenNgay.getTextField(); }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTblPhieuNhap() { return tblPhieuNhap; }
    public JLabel getLblFooterSoPhieu() { return lblFooterSoPhieu; }
    public JLabel getLblCapNhatLuc() { return lblCapNhatLuc; }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }

    public JLabel getLblMaPhieuNhapModal() { return lblMaPhieuNhapModal; }
    public JLabel getLblNgayNhapModal() { return lblNgayNhapModal; }
    public JComboBox<NhaCungCap> getCboModalNCC() { return cboModalNCC; }
    public JLabel getLblThuKhoModal() { return lblThuKhoModal; }
    public JTextField getTxtTimPhuTungModal() { return fTimPhuTungModal.getTextField(); }
    public JPanel getPnlDanhSachChiTietModal() { return pnlDanhSachChiTietModal; }
    public JLabel getLblLoiModal() { return lblLoiModal; }
    public JLabel getLblTongTienModal() { return lblTongTienModal; }
    public JButton getBtnDongModal() { return btnDongModal; }
    public JButton getBtnNhapExcel() { return btnNhapExcel; }
    public JButton getBtnHuyModal() { return btnHuyModal; }
    public JButton getBtnLuuPhieuNhap() { return btnLuuPhieuNhap; }

}