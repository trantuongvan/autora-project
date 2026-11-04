package gui;

import controller.QLPhuTungController;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;

public class QLPhuTung extends JPanel {

    // TopBar & Sidebar
    private final JLabel lblAvatar = new JLabel("BV", SwingConstants.CENTER);
    private final JLabel lblTenThuKhoHeader = UIComponents.label("Bùi Thái Vy", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = TrangChuKyThuatVien.taoNutDangXuat();

    private final JButton btnMenuTrangChu = TrangChuThuKho.taoNutMenuThuKho("Trang chủ", TrangChuThuKho.MenuThuKho.TRANG_CHU, false);
    private final JButton btnMenuPhuTung = TrangChuThuKho.taoNutMenuThuKho("Quản lý phụ tùng & tồn kho", TrangChuThuKho.MenuThuKho.PHU_TUNG, true);
    private final JButton btnMenuNhapKho = TrangChuThuKho.taoNutMenuThuKho("Quản lý nhập kho", TrangChuThuKho.MenuThuKho.NHAP_KHO, false);
    private final JLabel lblBadgeSidebarDuyetXuat = TrangChuThuKho.taoBadgeSoDo("0");
    private final JButton btnMenuDuyetXuat = TrangChuThuKho.taoNutMenuDuyetXuat(false, lblBadgeSidebarDuyetXuat);

    // LayeredPane để phủ Drawer Thêm/Sửa phụ tùng bên phải
    private final JLayeredPane layeredPane = new JLayeredPane();
    private final JPanel pnlMainBody = new JPanel(new BorderLayout());
    private final JPanel pnlDrawerOverlay = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(18, 24, 32, 115));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    };

    // Thành phần màn hình chính Danh mục phụ tùng
    private final JLabel lblMoTaSoMatHang = new JLabel("<html>— mặt hàng • <font color='#E53935'><b>— mặt hàng dưới ngưỡng cảnh báo</b></font></html>");
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();
    private final JButton btnMoThemPhuTung = UIComponents.nutChinh("Thêm phụ tùng", UIConstants.PRIMARY);

    private final UIComponents.IconField fTimKiem = new UIComponents.IconField(
            "Tìm mã SKU hoặc tên phụ tùng",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final JComboBox<String> cboHangSanXuat = new JComboBox<>(new String[]{"Hãng sản xuất: Tất cả"});
    private final JComboBox<String> cboKhoangGia = new JComboBox<>(new String[]{
            "Khoảng giá: Tất cả",
            "Khoảng giá: Dưới 200.000đ",
            "Khoảng giá: 200.000đ - 500.000đ",
            "Khoảng giá: Trên 500.000đ"
    });
    private final JComboBox<String> cboTinhTrangTon = new JComboBox<>(new String[]{
            "Tình trạng tồn: Tất cả",
            "Tình trạng tồn: Dưới ngưỡng cảnh báo",
            "Tình trạng tồn: Còn hàng",
            "Tình trạng tồn: Hết hàng"
    });

    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã SKU", "Tên phụ tùng", "Hãng", "Vị trí kho", "Giá nhập", "Giá bán lẻ", "Tồn", "Ngưỡng cảnh báo", "Thao tác"
    );
    private final JTable tblPhuTung = new JTable(tableModel);
    private JScrollPane spBang;
    private final JLabel lblFooterHienThi = UIComponents.label("Hiển thị 0 / 0 mặt hàng", UIConstants.FONT_CAPTION.deriveFont(12.5f), UIConstants.TEXT_MUTED);

    // Thành phần Drawer Thêm / Sửa phụ tùng bên phải
    private final JLabel lblTieuDeDrawer = UIComponents.label("Thêm phụ tùng mới", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 20f), UIConstants.TEXT_DARK);
    private final JButton btnDongDrawer = UIComponents.nutIcon(new UIComponents.LineIcon(UIComponents.Loai.CLOSE, UIConstants.TEXT_MUTED, 16));

    private final JTextField txtMaSKU = new JTextField();
    private final JLabel lblLoiMaSKU = UIComponents.errorLabel();

    private final JTextField txtTenPhuTung = new JTextField();
    private final JLabel lblLoiTenPhuTung = UIComponents.errorLabel();

    private final JComboBox<String> cboDrawerHang = new JComboBox<>(new String[]{"Toyota", "Honda OEM", "Castrol", "Mobil", "Bosch", "Brembo", "Denso", "NGK", "Mahle"});
    private final JComboBox<String> cboDrawerViTri = new JComboBox<>(new String[]{"Kệ A-01", "Kệ A-02", "Kệ B-01", "Kệ B-02", "Kệ C-01", "Kệ D-01", "Kệ E-01"});

    private final JTextField txtGiaNhap = new JTextField();
    private final JLabel lblLoiGiaNhap = UIComponents.errorLabel();

    private final JTextField txtGiaBanLe = new JTextField();
    private final JLabel lblGhiChuGia = UIComponents.label(
            "<html><div style='width:175px;'>Không được thấp hơn giá nhập</div></html>",
            UIConstants.FONT_CAPTION.deriveFont(11.5f), UIConstants.TEXT_MUTED
    );

    // Tách riêng 2 nhãn lỗi dưới từng ô Số lượng tồn và Ngưỡng cảnh báo
    private final JTextField txtSoLuongTon = new JTextField();
    private final JLabel lblLoiSoLuongTon = UIComponents.errorLabel();

    private final JTextField txtNguongCanhBao = new JTextField();
    private final JLabel lblLoiNguongCanhBao = UIComponents.errorLabel();

    private final JButton btnHuyDrawer = new NutVienDo("Hủy");
    private final JButton btnLuuDrawer = UIComponents.nutChinh("Lưu", UIConstants.ROLE_MANAGER);

    public QLPhuTung() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        buildUI();
    }

    private static QLPhuTungController activeController;

    public static QLPhuTungController getActiveController() {
        return activeController;
    }

    public static JPanel createPanel() {
        QLPhuTung view = new QLPhuTung();
        activeController = new QLPhuTungController(view);
        return view;
    }

    private void buildUI() {
        pnlMainBody.setBackground(UIConstants.APP_BG);


        pnlMainBody.add(buildWorkspace(), BorderLayout.CENTER);

        buildDrawerOverlay();

        layeredPane.add(pnlMainBody, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(pnlDrawerOverlay, JLayeredPane.PALETTE_LAYER);
        pnlDrawerOverlay.setVisible(false);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                pnlMainBody.setBounds(0, 0, w, h);
                pnlDrawerOverlay.setBounds(0, 0, w, h);
                layeredPane.revalidate();
            }
        });

        add(layeredPane, BorderLayout.CENTER);
    }

    private JComponent buildWorkspace() {
        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Danh mục phụ tùng",
                lblMoTaSoMatHang,
                UIConstants.ROLE_RECEPTION,
                btnMoThemPhuTung
        );
        root.them(pnlHeader, 0);

        JPanel pnlFilterRow = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fTimKiem.setPreferredSize(new Dimension(230, 40));
        UIComponents.styleCombo(cboHangSanXuat);
        UIComponents.styleCombo(cboKhoangGia);
        UIComponents.styleCombo(cboTinhTrangTon);
        cboHangSanXuat.setPreferredSize(new Dimension(195, 40));
        cboKhoangGia.setPreferredSize(new Dimension(200, 40));
        cboTinhTrangTon.setPreferredSize(new Dimension(220, 40));

        pnlFilterRow.add(fTimKiem);
        pnlFilterRow.add(cboHangSanXuat);
        pnlFilterRow.add(cboKhoangGia);
        pnlFilterRow.add(cboTinhTrangTon);
        pnlFilterRow.add(btnTaiDuLieu);
        root.them(pnlFilterRow, 18);

        UIComponents.RoundedPanel cardBang = UIComponents.card();
        cardBang.setLayout(new BorderLayout());

        cauhinhBang();
        spBang = UIComponents.cuonBang(tblPhuTung);
        spBang.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        cardBang.add(spBang, BorderLayout.CENTER);
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(this::capNhatChieuCaoBang));
        SwingUtilities.invokeLater(this::capNhatChieuCaoBang);

        JPanel pnlFooter = UIComponents.trong(new BorderLayout());
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(12, 18, 12, 18)
        ));
        pnlFooter.add(lblFooterHienThi, BorderLayout.WEST);

        JPanel pnlPagination = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPagination.add(new PaginationButton("‹", false));
        pnlPagination.add(new PaginationButton("1", true));
        pnlPagination.add(new PaginationButton("›", false));
        pnlFooter.add(pnlPagination, BorderLayout.EAST);

        cardBang.add(pnlFooter, BorderLayout.SOUTH);
        root.them(cardBang, 16);

        root.ketThuc();
        return UIComponents.cuonTrang(root);
    }

    private void buildDrawerOverlay() {
        pnlDrawerOverlay.setOpaque(false);
        pnlDrawerOverlay.addMouseListener(new MouseAdapter() {});

        JPanel drawer = new JPanel(new BorderLayout());
        drawer.setBackground(UIConstants.CARD_BG);
        drawer.setPreferredSize(new Dimension(440, 0));
        drawer.setBorder(new CompoundBorder(
                new MatteBorder(0, 1, 0, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(24, 26, 24, 26)
        ));

        JPanel pnlTop = UIComponents.trong(new BorderLayout());
        pnlTop.add(lblTieuDeDrawer, BorderLayout.WEST);
        pnlTop.add(btnDongDrawer, BorderLayout.EAST);
        drawer.add(pnlTop, BorderLayout.NORTH);

        UIComponents.CotDoc form = new UIComponents.CotDoc();
        khoaKichThuocInput(txtMaSKU);
        khoaKichThuocInput(txtTenPhuTung);
        UIComponents.styleCombo(cboDrawerHang);
        cboDrawerHang.setEditable(true);
        cboDrawerHang.setPreferredSize(new Dimension(100, 40));
        cboDrawerHang.setMinimumSize(new Dimension(100, 40));
        cboDrawerHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        UIComponents.styleCombo(cboDrawerViTri);
        cboDrawerViTri.setEditable(true);
        cboDrawerViTri.setPreferredSize(new Dimension(100, 40));
        cboDrawerViTri.setMinimumSize(new Dimension(100, 40));
        cboDrawerViTri.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        khoaKichThuocInput(txtGiaNhap);
        khoaKichThuocInput(txtGiaBanLe);
        khoaKichThuocInput(txtSoLuongTon);
        khoaKichThuocInput(txtNguongCanhBao);

        form.them(UIComponents.formLabel("Mã SKU", true), 18);
        form.them(txtMaSKU, 6);
        form.them(lblLoiMaSKU, 4);

        form.them(UIComponents.formLabel("Tên phụ tùng", true), 14);
        form.them(txtTenPhuTung, 6);
        form.them(lblLoiTenPhuTung, 4);

        form.them(UIComponents.formLabel("Hãng sản xuất", false), 14);
        form.them(cboDrawerHang, 6);

        form.them(UIComponents.formLabel("Vị trí lưu kho", false), 14);
        form.them(cboDrawerViTri, 6);

        // Hàng đôi 1: Giá nhập & Giá bán lẻ (Mỗi ô có chiều cao cố định 40px và nhãn cảnh báo riêng ngay bên dưới)
        JPanel pnlRowGia = UIComponents.trong(new GridLayout(1, 2, 14, 0));
        pnlRowGia.add(taoCumInputCoLoi("Giá nhập", true, txtGiaNhap, lblLoiGiaNhap));
        pnlRowGia.add(taoCumInputCoLoi("Giá bán lẻ", true, txtGiaBanLe, lblGhiChuGia));
        form.them(pnlRowGia, 14);

        // Hàng đôi 2: Số lượng tồn & Ngưỡng cảnh báo (Mỗi ô có nhãn cảnh báo riêng ngay bên dưới, tự xuống dòng)
        JPanel pnlRowTon = UIComponents.trong(new GridLayout(1, 2, 14, 0));
        pnlRowTon.add(taoCumInputCoLoi("Số lượng tồn", true, txtSoLuongTon, lblLoiSoLuongTon));
        pnlRowTon.add(taoCumInputCoLoi("Ngưỡng cảnh báo", true, txtNguongCanhBao, lblLoiNguongCanhBao));
        form.them(pnlRowTon, 14);

        form.ketThuc();
        JScrollPane spForm = new JScrollPane(form);
        spForm.setBorder(null);
        spForm.getViewport().setBackground(UIConstants.CARD_BG);
        spForm.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        drawer.add(spForm, BorderLayout.CENTER);

        JPanel pnlBot = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlBot.setBorder(new EmptyBorder(12, 0, 0, 0));
        pnlBot.add(btnHuyDrawer);
        pnlBot.add(btnLuuDrawer);
        drawer.add(pnlBot, BorderLayout.SOUTH);

        pnlDrawerOverlay.add(drawer, BorderLayout.EAST);
    }

    private void khoaKichThuocInput(JTextField txt) {
        UIComponents.styleInput(txt);
        Dimension d = new Dimension(100, 40);
        txt.setPreferredSize(d);
        txt.setMinimumSize(d);
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    private JPanel taoCumInputCoLoi(String nhan, boolean batBuoc, JTextField txt, JLabel lblLoi) {
        UIComponents.CotDoc col = new UIComponents.CotDoc();
        col.them(UIComponents.formLabel(nhan, batBuoc), 0);
        col.them(txt, 6);
        col.them(lblLoi, 4);
        col.ketThuc();
        return col;
    }

    public void hienThiDrawer(boolean mo) {
        pnlDrawerOverlay.setVisible(mo);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public static void showWrappedError(JLabel label, String msg, int widthPx) {
        if (msg == null || msg.trim().isEmpty()) {
            label.setText("");
            label.setVisible(false);
        } else {
            label.setText("<html><div style='width:" + widthPx + "px;'>" + msg + "</div></html>");
            label.setVisible(true);
        }
    }

    private void capNhatChieuCaoBang() {
        if (spBang == null) return;
        int rowCount = tblPhuTung.getRowCount();
        int headerH = tblPhuTung.getTableHeader().getPreferredSize().height;
        int totalH = headerH + Math.max(rowCount, 3) * tblPhuTung.getRowHeight() + 2;
        Dimension d = new Dimension(100, totalH);
        spBang.setPreferredSize(d);
        spBang.setMinimumSize(d);
        spBang.revalidate();
        revalidate();
        repaint();
    }

    private void cauhinhBang() {
        UIComponents.styleTable(tblPhuTung);
        UIComponents.datDoRong(tblPhuTung, 105, 230, 100, 90, 110, 110, 65, 130, 100);

        Color bgWarningRed = Color.decode("#FFF4F4");
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));

                int ton = 0;
                int nguong = 0;
                try {
                    ton = Integer.parseInt(String.valueOf(t.getValueAt(r, 6)));
                    nguong = Integer.parseInt(String.valueOf(t.getValueAt(r, 7)));
                } catch (Exception ignored) {}
                boolean duoiNguong = ton <= nguong;

                if (c == 8) {
                    setText("<html><font color='#1565C0'><b>Sửa</b></font> &nbsp;&nbsp;<font color='#C0392B'><b>Xóa</b></font></html>");
                } else if (c == 0) {
                    setFont(UIConstants.FONT_FORM_LABEL.deriveFont(13.5f));
                    setForeground(UIConstants.TEXT_DARK);
                } else if (c == 6 || c == 7) {
                    setFont(duoiNguong ? UIConstants.FONT_FORM_LABEL.deriveFont(13.5f) : UIConstants.FONT_BODY.deriveFont(13.5f));
                    setForeground(duoiNguong ? UIConstants.STATUS_DANGER : UIConstants.TEXT_DARK);
                } else {
                    setFont(UIConstants.FONT_BODY.deriveFont(13.5f));
                    setForeground(UIConstants.TEXT_DARK);
                }

                if (sel) {
                    setBackground(t.getSelectionBackground());
                } else {
                    setBackground(duoiNguong ? bgWarningRed : UIConstants.CARD_BG);
                }
                return this;
            }
        };

        for (int i = 0; i < tblPhuTung.getColumnCount(); i++) {
            tblPhuTung.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
        tblPhuTung.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static class PaginationButton extends JButton {
        private final boolean active;

        public PaginationButton(String text, boolean active) {
            super(text);
            this.active = active;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(Font.BOLD, 12.5f));
            setPreferredSize(new Dimension(32, 32));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (active) {
                g2.setColor(UIConstants.ROLE_RECEPTION);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(getModel().isRollover() ? UIConstants.APP_BG : UIConstants.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.setColor(UIConstants.TEXT_DARK);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
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
            setBorder(new EmptyBorder(10, 22, 10, 22));
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

    public JLabel getLblMoTaSoMatHang() { return lblMoTaSoMatHang; }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
    public JButton getBtnMoThemPhuTung() { return btnMoThemPhuTung; }
    public JTextField getTxtTimKiem() { return fTimKiem.getTextField(); }
    public JComboBox<String> getCboHangSanXuat() { return cboHangSanXuat; }
    public JComboBox<String> getCboKhoangGia() { return cboKhoangGia; }
    public JComboBox<String> getCboTinhTrangTon() { return cboTinhTrangTon; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTblPhuTung() { return tblPhuTung; }
    public JLabel getLblFooterHienThi() { return lblFooterHienThi; }

    public JLabel getLblTieuDeDrawer() { return lblTieuDeDrawer; }
    public JButton getBtnDongDrawer() { return btnDongDrawer; }
    public JTextField getTxtMaSKU() { return txtMaSKU; }
    public JLabel getLblLoiMaSKU() { return lblLoiMaSKU; }
    public JTextField getTxtTenPhuTung() { return txtTenPhuTung; }
    public JLabel getLblLoiTenPhuTung() { return lblLoiTenPhuTung; }
    public JComboBox<String> getCboDrawerHang() { return cboDrawerHang; }
    public JComboBox<String> getCboDrawerViTri() { return cboDrawerViTri; }
    public JTextField getTxtGiaNhap() { return txtGiaNhap; }
    public JLabel getLblLoiGiaNhap() { return lblLoiGiaNhap; }
    public JTextField getTxtGiaBanLe() { return txtGiaBanLe; }
    public JLabel getLblGhiChuGia() { return lblGhiChuGia; }
    public JTextField getTxtSoLuongTon() { return txtSoLuongTon; }
    public JLabel getLblLoiSoLuongTon() { return lblLoiSoLuongTon; }
    public JTextField getTxtNguongCanhBao() { return txtNguongCanhBao; }
    public JLabel getLblLoiNguongCanhBao() { return lblLoiNguongCanhBao; }
    public JButton getBtnHuyDrawer() { return btnHuyDrawer; }
    public JButton getBtnLuuDrawer() { return btnLuuDrawer; }

//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ignored) {}
//
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("AUTORA GARAGE - Quản Lý Phụ Tùng & Tồn Kho");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setContentPane(QLPhuTung.createPanel());
//            frame.setSize(1366, 840);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//    }
}