package gui;

import controller.QLTienDoSuaChuaController;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import util.UIComponents;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Màn hình 2 & 3: Quản lý tiến độ sửa chữa & Lập phiếu yêu cầu phụ tùng.
 * Đã khóa cứng tỷ lệ 2 cột và tự động rớt dòng cho các nhãn lọc trạng thái.
 */
public class QLTienDoSuaChua extends JPanel {

    public static final String CARD_TIEN_DO = "CARD_TIEN_DO";
    public static final String CARD_YEU_CAU_PT = "CARD_YEU_CAU_PT";

    private static QLTienDoSuaChuaController activeController;

    // TopBar & Sidebar components
    private final JLabel lblTieuDeTopBar = UIComponents.label("Quản lý tiến độ sửa chữa", UIConstants.FONT_SECTION_TITLE.deriveFont(Font.BOLD, 18f), UIConstants.TEXT_DARK);
    private final JLabel lblAvatar = new JLabel("TV", SwingConstants.CENTER);
    private final JLabel lblTenKTVHeader = UIComponents.label("Lê Văn Thợ Máy", UIConstants.FONT_FORM_LABEL.deriveFont(13.5f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = TrangChuKyThuatVien.taoNutDangXuat();
    private final JButton btnMenuTrangChu = TrangChuKyThuatVien.taoNutMenuSidebar("Trang chủ", true, false);
    private final JButton btnMenuTienDo = TrangChuKyThuatVien.taoNutMenuSidebar("<html>Quản lý tiến độ sửa<br>chữa</html>", false, true);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlMainCards = new JPanel(cardLayout);

    // =========================================================================
    // THÀNH PHẦN MÀN HÌNH 2: QUẢN LÝ TIẾN ĐỘ SỬA CHỮA
    // =========================================================================
    private final UIComponents.IconField fTimPhieu = new UIComponents.IconField(
            "Tìm mã phiếu hoặc biển số",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final String[] CAC_TAB_LOC = {"Tất cả", "Chờ sửa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"};
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();
    private final JButton[] btnLocTrangThai = new JButton[CAC_TAB_LOC.length];
    private final JPanel pnlDanhSachPhieu = new JPanel();

    private final JLabel lblMaPhieuChiTiet = UIComponents.label("---", UIConstants.FONT_PAGE_TITLE.deriveFont(22f), UIConstants.TEXT_DARK);
    private final UIComponents.Badge badgeTrangThaiPhieu = new UIComponents.Badge("Chờ sửa", UIConstants.STATUS_DANGER);
    private final ProgressBarBoGoc thanhTienDo = new ProgressBarBoGoc();

    private final JLabel lblValBienSo = giaTriThongTin("---");
    private final JLabel lblValXe = giaTriThongTin("---");
    private final JLabel lblValKhachHang = giaTriThongTin("---");
    private final JLabel lblValTiepNhan = giaTriThongTin("---");
    private final JLabel lblValDuKien = giaTriThongTin("---");
    private final JLabel lblValSoKm = giaTriThongTin("---");
    private final JLabel lblValNhienLieu = giaTriThongTin("---");
    private final JLabel lblValYeuCau = giaTriThongTin("---");

    private final DefaultTableModel modelHangMuc = UIComponents.modelKhongSua(
            "Hạng mục", "Tiến độ", "Trạng thái"
    );
    private final JTable tblHangMuc = new JTable(modelHangMuc);
    private JScrollPane spHangMuc;

    private final JComboBox<String> cboTrangThaiMoi = new JComboBox<>(new String[]{
            "Trạng thái mới: Chờ sửa",
            "Trạng thái mới: Đang sửa",
            "Trạng thái mới: Chờ phụ tùng",
            "Trạng thái mới: Hoàn thành"
    });

    private final UIComponents.RoundedPanel pnlHopLoiTrangThai = new UIComponents.RoundedPanel(
            8, UIComponents.tint(UIConstants.STATUS_DANGER, 0.90f), UIConstants.STATUS_DANGER
    );
    private final JLabel lblThongBaoLoiTrangThai = UIComponents.label(
            "Trạng thái không hợp lệ: không thể chuyển từ 'Chờ sửa' sang 'Hoàn thành'. Vui lòng chọn lại.",
            UIConstants.FONT_BODY.deriveFont(13f),
            UIComponents.shade(UIConstants.STATUS_DANGER, 0.78f)
    );

    private final JButton btnMoYeuCauPhuTung = new NutVienMau("Yêu cầu phụ tùng", UIConstants.ROLE_RECEPTION);
    private final JButton btnCapNhatTrangThai = UIComponents.nutChinh("Cập nhật trạng thái", UIConstants.ROLE_MANAGER);

    // =========================================================================
    // THÀNH PHẦN MÀN HÌNH 3: LẬP PHIẾU YÊU CẦU PHỤ TÙNG
    // =========================================================================
    private final JButton btnQuayLai = UIComponents.nutPhu("←  Quay lại");
    private final JTextField txtMaYeuCau = new JTextField();
    private final JTextField txtPhieuSuaChuaYC = new JTextField();
    private final JTextField txtXeYC = new JTextField();
    private final JTextField txtKyThuatVienYC = new JTextField();
    private final JTextField txtNgayYeuCau = new JTextField();
    private final UIComponents.Badge badgeTrangThaiYC = new UIComponents.Badge("Chờ xuất", UIConstants.STATUS_WARNING);

    private final UIComponents.IconField fTimPhuTung = new UIComponents.IconField(
            "Tìm theo mã SKU hoặc tên phụ tùng",
            new UIComponents.LineIcon(UIComponents.Loai.SEARCH, UIConstants.TEXT_MUTED, 16),
            true
    );
    private final JPanel pnlDanhSachPhuTungChon = new JPanel();
    private final JLabel lblLoiSoLuongPT = UIComponents.errorLabel();
    private final JButton btnThemPhuTung = UIComponents.nutChinh("+  Thêm phụ tùng", UIConstants.PRIMARY);
    private final JButton btnGuiYeuCau = UIComponents.nutChinh("Gửi yêu cầu", UIConstants.ROLE_RECEPTION);

    public QLTienDoSuaChua() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);




        pnlMainCards.setOpaque(false);
        pnlMainCards.add(buildManHinhTienDo(), CARD_TIEN_DO);
        pnlMainCards.add(buildManHinhYeuCauPhuTung(), CARD_YEU_CAU_PT);
        add(pnlMainCards, BorderLayout.CENTER);
    }

    public static JPanel createPanel() {
        QLTienDoSuaChua view = new QLTienDoSuaChua();
        activeController = new QLTienDoSuaChuaController(view);
        return view;
    }

    public static QLTienDoSuaChuaController getActiveController() {
        return activeController;
    }

    public void hienThiManHinh(String tenCard) {
        cardLayout.show(pnlMainCards, tenCard);
        if (CARD_YEU_CAU_PT.equals(tenCard)) {
            lblTieuDeTopBar.setText("Quản lý tiến độ sửa chữa › Phiếu yêu cầu phụ tùng");
        } else {
            lblTieuDeTopBar.setText("Quản lý tiến độ sửa chữa");
        }
    }

    private final UIComponents.Badge badgeCa = new UIComponents.Badge("Chưa xếp ca", UIConstants.ROLE_RECEPTION_LIGHT);

    /** Ca thật của kỹ thuật viên đang đăng nhập; null khi chưa xếp ca. */
    public void setCaLamViec(entity.CaLamViec ca) {
        badgeCa.setText(ca == null ? "Chưa xếp ca"
                : "Ca làm việc • " + ca.getGioBatDau() + " – " + ca.getGioKetThuc());
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

    // =========================================================================
    // DỰNG TOP BAR & LEFT SIDEBAR CHUẨN FIGMA
    // =========================================================================
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
        pnlCenter.add(lblTieuDeTopBar);
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
        pnlFooter.add(UIComponents.label(TrangChuKyThuatVien.layChuoiNgayTiengViet(), UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED));
        pnlFooter.add(UIComponents.label("\u25CF  Hệ thống sẵn sàng", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.STATUS_SUCCESS));

        sidebar.add(pnlFooter, BorderLayout.SOUTH);
        return sidebar;
    }

    // =========================================================================
    // DỰNG GIAO DIỆN MÀN HÌNH 2: QUẢN LÝ TIẾN ĐỘ SỬA CHỮA
    // =========================================================================
    private JComponent buildManHinhTienDo() {
        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Quản lý tiến độ sửa chữa",
                "Theo dõi và cập nhật trạng thái từng phiếu sửa chữa được giao cho bạn.",
                UIConstants.ROLE_RECEPTION,
                badgeCa
        );
        root.them(pnlHeader, 0);

        // --- CỘT TRÁI: PHIẾU ĐƯỢC GIAO ---
        UIComponents.RoundedPanel cardTrai = UIComponents.card();
        cardTrai.setLayout(new BorderLayout(0, 14));
        cardTrai.setBorder(new EmptyBorder(20, 18, 20, 18));

        UIComponents.CotDoc pnlTopTrai = new UIComponents.CotDoc();
        pnlTopTrai.them(UIComponents.label("Phiếu được giao", UIConstants.FONT_SECTION_TITLE.deriveFont(18f), UIConstants.TEXT_DARK), 0);

        fTimPhieu.setPreferredSize(new Dimension(100, 40));
        JPanel hangTim = UIComponents.trong(new BorderLayout(8, 0));
        hangTim.add(fTimPhieu, BorderLayout.CENTER);
        hangTim.add(btnTaiDuLieu, BorderLayout.EAST);
        pnlTopTrai.them(hangTim, 12);

        // Dùng WrapFlowLayout để các nhãn lọc ("Hoàn thành") tự động rớt xuống dòng dưới
        JPanel pnlFilterPills = UIComponents.trong(new WrapFlowLayout(FlowLayout.LEFT, 6, 6));
        for (int i = 0; i < CAC_TAB_LOC.length; i++) {
            JButton b = taoNutPillLoc(CAC_TAB_LOC[i], i == 0);
            btnLocTrangThai[i] = b;
            pnlFilterPills.add(b);
        }
        pnlTopTrai.them(pnlFilterPills, 10);
        cardTrai.add(pnlTopTrai, BorderLayout.NORTH);

        pnlDanhSachPhieu.setLayout(new BoxLayout(pnlDanhSachPhieu, BoxLayout.Y_AXIS));
        pnlDanhSachPhieu.setOpaque(false);
        JPanel pnlWrapListPhieu = UIComponents.trong(new BorderLayout());
        pnlWrapListPhieu.add(pnlDanhSachPhieu, BorderLayout.NORTH);
        cardTrai.add(pnlWrapListPhieu, BorderLayout.CENTER);

        // --- CỘT PHẢI: CHI TIẾT PHIẾU SỬA CHỮA ---
        UIComponents.RoundedPanel cardPhai = UIComponents.card();
        cardPhai.setLayout(new BorderLayout());
        cardPhai.setBorder(new EmptyBorder(20, 24, 22, 24));

        UIComponents.CotDoc pnlChiTiet = new UIComponents.CotDoc();

        // 1. Mã phiếu + Badge trạng thái
        JPanel pnlHeadPhieu = UIComponents.trong(new BorderLayout());
        pnlHeadPhieu.add(lblMaPhieuChiTiet, BorderLayout.WEST);
        JPanel pnlBadgeWrap = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlBadgeWrap.add(badgeTrangThaiPhieu);
        pnlHeadPhieu.add(pnlBadgeWrap, BorderLayout.EAST);
        pnlChiTiet.them(pnlHeadPhieu, 0);

        // 2. Thanh Tiến độ
        pnlChiTiet.them(UIComponents.label("Tiến độ", UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED), 12);
        pnlChiTiet.them(thanhTienDo, 6);

        // 3. Lưới thông tin xe & khách hàng
        JPanel pnlGridInfo = UIComponents.trong(new GridLayout(4, 2, 20, 12));
        pnlGridInfo.add(oThongTin("Biển số", lblValBienSo));
        pnlGridInfo.add(oThongTin("Xe", lblValXe));
        pnlGridInfo.add(oThongTin("Khách hàng", lblValKhachHang));
        pnlGridInfo.add(oThongTin("Tiếp nhận", lblValTiepNhan));
        pnlGridInfo.add(oThongTin("Dự kiến hoàn thành", lblValDuKien));
        pnlGridInfo.add(oThongTin("Số km", lblValSoKm));
        pnlGridInfo.add(oThongTin("Mức nhiên liệu", lblValNhienLieu));
        pnlGridInfo.add(oThongTin("Yêu cầu ban đầu", lblValYeuCau));
        pnlChiTiet.them(pnlGridInfo, 16);

        // 4. Bảng Hạng mục dịch vụ được giao
        pnlChiTiet.them(UIComponents.label("Hạng mục dịch vụ được giao", UIConstants.FONT_SECTION_TITLE.deriveFont(15f), UIConstants.TEXT_DARK), 18);

        cauhinhBangHangMuc();
        spHangMuc = UIComponents.cuonBang(tblHangMuc);
        spHangMuc.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        spHangMuc.setBorder(new LineBorder(UIConstants.BORDER_COLOR, 1, true));
        modelHangMuc.addTableModelListener(e -> SwingUtilities.invokeLater(this::capNhatChieuCaoBangHangMuc));
        capNhatChieuCaoBangHangMuc();
        pnlChiTiet.them(spHangMuc, 8);

        // 5. Cập nhật trạng thái
        pnlChiTiet.them(UIComponents.label("Cập nhật trạng thái", UIConstants.FONT_SECTION_TITLE.deriveFont(15f), UIConstants.TEXT_DARK), 18);

        UIComponents.styleCombo(cboTrangThaiMoi);
        cboTrangThaiMoi.setPreferredSize(new Dimension(200, 42));
        cboTrangThaiMoi.setMinimumSize(new Dimension(200, 42));
        pnlChiTiet.them(cboTrangThaiMoi, 8);

        // 6. Hộp cảnh báo lỗi chuyển trạng thái
        pnlHopLoiTrangThai.setLayout(new BorderLayout(10, 0));
        pnlHopLoiTrangThai.setBorder(new EmptyBorder(10, 14, 10, 14));
        pnlHopLoiTrangThai.add(new JLabel(new UIComponents.LineIcon(UIComponents.Loai.ALERT, UIConstants.STATUS_DANGER, 18)), BorderLayout.WEST);
        pnlHopLoiTrangThai.add(lblThongBaoLoiTrangThai, BorderLayout.CENTER);
        pnlHopLoiTrangThai.setVisible(false);
        pnlChiTiet.them(pnlHopLoiTrangThai, 10);

        // 7. Hai nút hành động dưới cùng
        JPanel pnlActions = UIComponents.trong(new BorderLayout());
        pnlActions.add(btnMoYeuCauPhuTung, BorderLayout.WEST);
        pnlActions.add(btnCapNhatTrangThai, BorderLayout.EAST);
        pnlChiTiet.them(pnlActions, 18);

        pnlChiTiet.ketThuc();
        cardPhai.add(pnlChiTiet, BorderLayout.CENTER);

        // Khóa tỷ lệ 2 cột Trái (38%) - Phải (62%) bằng TwoColumnLayout chuẩn Swing
        JPanel pnlNoiDung = UIComponents.trong(new TwoColumnLayout(0.38, 20));
        pnlNoiDung.add(cardTrai);
        pnlNoiDung.add(cardPhai);

        root.them(pnlNoiDung, 18);
        root.ketThuc();
        return UIComponents.cuonTrang(root);
    }

    private void capNhatChieuCaoBangHangMuc() {
        if (spHangMuc == null) return;
        int rows = Math.max(1, tblHangMuc.getRowCount());
        int headerH = tblHangMuc.getTableHeader().getPreferredSize().height;
        int totalH = headerH + rows * tblHangMuc.getRowHeight() + 2;
        Dimension d = new Dimension(100, totalH);
        spHangMuc.setPreferredSize(d);
        spHangMuc.setMinimumSize(d);
        spHangMuc.revalidate();
        revalidate();
        repaint();
    }

    // =========================================================================
    // DỰNG GIAO DIỆN MÀN HÌNH 3: LẬP PHIẾU YÊU CẦU PHỤ TÙNG
    // =========================================================================
    private JComponent buildManHinhYeuCauPhuTung() {
        UIComponents.CotDoc root = new UIComponents.CotDoc();
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel pnlHeader = UIComponents.tieuDeTrang(
                "Lập phiếu yêu cầu phụ tùng",
                "Yêu cầu sẽ được gửi đến Thủ kho để duyệt xuất kho.",
                UIConstants.ROLE_RECEPTION,
                btnQuayLai
        );
        root.them(pnlHeader, 0);

        // CỘT TRÁI: THÔNG TIN PHIẾU
        UIComponents.RoundedPanel cardThongTin = UIComponents.card();
        cardThongTin.setLayout(new BorderLayout());
        cardThongTin.setBorder(new EmptyBorder(20, 22, 20, 22));

        UIComponents.CotDoc colTrai = new UIComponents.CotDoc();
        colTrai.them(UIComponents.label("Thông tin phiếu", UIConstants.FONT_SECTION_TITLE.deriveFont(18f), UIConstants.TEXT_DARK), 0);

        cauhinhOThongTinKhoa(txtMaYeuCau, true);
        cauhinhOThongTinKhoa(txtPhieuSuaChuaYC, false);
        cauhinhOThongTinKhoa(txtXeYC, false);
        cauhinhOThongTinKhoa(txtKyThuatVienYC, false);
        cauhinhOThongTinKhoa(txtNgayYeuCau, false);

        colTrai.them(nhanNho("Mã yêu cầu"), 14);
        colTrai.them(txtMaYeuCau, 6);
        colTrai.them(UIComponents.label("Tự động phát sinh", UIConstants.FONT_CAPTION.deriveFont(11f), UIConstants.TEXT_MUTED), 4);

        colTrai.them(nhanNho("Phiếu sửa chữa"), 12);
        colTrai.them(txtPhieuSuaChuaYC, 6);

        colTrai.them(nhanNho("Xe"), 12);
        colTrai.them(txtXeYC, 6);

        colTrai.them(nhanNho("Kỹ thuật viên"), 12);
        colTrai.them(txtKyThuatVienYC, 6);

        colTrai.them(nhanNho("Ngày yêu cầu"), 12);
        colTrai.them(txtNgayYeuCau, 6);

        colTrai.them(nhanNho("Trạng thái"), 12);
        UIComponents.RoundedPanel boxTrangThai = new UIComponents.RoundedPanel(8, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
        boxTrangThai.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        boxTrangThai.add(badgeTrangThaiYC);
        colTrai.them(boxTrangThai, 6);
        colTrai.ketThuc();
        cardThongTin.add(colTrai, BorderLayout.CENTER);

        // CỘT PHẢI: PHỤ TÙNG CẦN XUẤT
        UIComponents.RoundedPanel cardPhuTung = UIComponents.card();
        cardPhuTung.setLayout(new BorderLayout(0, 14));
        cardPhuTung.setBorder(new EmptyBorder(20, 24, 20, 24));

        UIComponents.CotDoc pnlTopPT = new UIComponents.CotDoc();
        pnlTopPT.them(UIComponents.label("Phụ tùng cần xuất", UIConstants.FONT_SECTION_TITLE.deriveFont(18f), UIConstants.TEXT_DARK), 0);
        pnlTopPT.them(UIComponents.label("Danh sách phụ tùng đã chọn để xuất kho", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED), 4);
        fTimPhuTung.setPreferredSize(new Dimension(400, 40));
        pnlTopPT.them(fTimPhuTung, 12);
        cardPhuTung.add(pnlTopPT, BorderLayout.NORTH);

        JPanel pnlBangContainer = UIComponents.trong(new BorderLayout());
        pnlBangContainer.add(taoHeaderBangPhuTungYC(), BorderLayout.NORTH);

        pnlDanhSachPhuTungChon.setLayout(new BoxLayout(pnlDanhSachPhuTungChon, BoxLayout.Y_AXIS));
        pnlDanhSachPhuTungChon.setBackground(UIConstants.CARD_BG);

        JPanel pnlWrapRows = new JPanel(new BorderLayout());
        pnlWrapRows.setBackground(UIConstants.CARD_BG);
        pnlWrapRows.add(pnlDanhSachPhuTungChon, BorderLayout.NORTH);
        pnlBangContainer.add(pnlWrapRows, BorderLayout.CENTER);

        cardPhuTung.add(pnlBangContainer, BorderLayout.CENTER);

        JPanel pnlBottomPT = UIComponents.trong(new BorderLayout(0, 12));
        lblLoiSoLuongPT.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        pnlBottomPT.add(lblLoiSoLuongPT, BorderLayout.NORTH);

        JPanel pnlBtnPT = UIComponents.trong(new BorderLayout());
        pnlBtnPT.add(btnThemPhuTung, BorderLayout.WEST);
        pnlBtnPT.add(btnGuiYeuCau, BorderLayout.EAST);
        pnlBottomPT.add(pnlBtnPT, BorderLayout.SOUTH);

        cardPhuTung.add(pnlBottomPT, BorderLayout.SOUTH);

        JPanel pnlNoiDung = UIComponents.trong(new TwoColumnLayout(0.34, 20));
        pnlNoiDung.add(cardThongTin);
        pnlNoiDung.add(cardPhuTung);

        root.them(pnlNoiDung, 18);
        root.ketThuc();
        return UIComponents.cuonTrang(root);
    }

    private JPanel taoHeaderBangPhuTungYC() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UIComponents.tint(UIConstants.APP_BG, 0.45f));
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)
        ));
        Font f = UIConstants.FONT_FORM_LABEL.deriveFont(13f);
        Color c = UIConstants.TEXT_MUTED;
        header.add(UIComponents.label("Mã SKU", f, c), cotGrid(0, 0.16));
        header.add(UIComponents.label("Tên phụ tùng", f, c), cotGrid(1, 0.34));
        header.add(UIComponents.label("Hãng", f, c), cotGrid(2, 0.14));
        header.add(UIComponents.label("Tồn kho", f, c), cotGrid(3, 0.12));
        header.add(UIComponents.label("Số lượng yêu cầu", f, c), cotGrid(4, 0.18));
        header.add(UIComponents.label("Xóa", f, c), cotGrid(5, 0.06));
        return header;
    }

    public static GridBagConstraints cotGrid(int x, double weightx) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = 0;
        gc.weightx = weightx;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 4, 0, 4);
        return gc;
    }

    private void cauhinhOThongTinKhoa(JTextField txt, boolean nenXam) {
        UIComponents.styleInput(txt);
        txt.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(14f));
        txt.setEditable(false);
        txt.setBackground(nenXam ? UIComponents.tint(UIConstants.APP_BG, 0.4f) : UIConstants.CARD_BG);
        txt.setMinimumSize(new Dimension(120, 40));
    }

    private JLabel nhanNho(String text) {
        return UIComponents.label(text, UIConstants.FONT_BODY.deriveFont(12f), UIConstants.TEXT_MUTED);
    }

    private JLabel giaTriThongTin(String text) {
        return UIComponents.label(text, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
    }

    private JPanel oThongTin(String nhan, JLabel lblGiaTri) {
        JPanel p = UIComponents.trong(new BorderLayout(0, 4));
        p.add(UIComponents.label(nhan, UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED), BorderLayout.NORTH);
        p.add(lblGiaTri, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(100, 42));
        p.setMinimumSize(new Dimension(100, 42));
        return p;
    }

    private void cauhinhBangHangMuc() {
        UIComponents.styleTable(tblHangMuc);
        tblHangMuc.setRowHeight(46);
        UIComponents.datDoRong(tblHangMuc, 260, 120, 140);

        tblHangMuc.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            private String trangThai = "Chờ sửa";

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, "", sel, false, r, c);
                trangThai = v == null ? "Chờ sửa" : v.toString();
                setBackground(sel ? t.getSelectionBackground() : t.getBackground());
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int activeDots = 1;
                Color activeColor = UIConstants.ROLE_RECEPTION;
                if (TrangThaiTienDo.HOAN_THANH.getText().equalsIgnoreCase(trangThai)) {
                    activeDots = 3;
                    activeColor = UIConstants.STATUS_SUCCESS;
                } else if (TrangThaiTienDo.DANG_SUA.getText().equalsIgnoreCase(trangThai)) {
                    activeDots = 2;
                    activeColor = UIConstants.PRIMARY;
                }

                int y = (getHeight() - 8) / 2;
                int startX = 14;
                for (int i = 0; i < 3; i++) {
                    g2.setColor(i < activeDots ? activeColor : UIConstants.BORDER_COLOR);
                    g2.fill(new Ellipse2D.Float(startX + i * 14, y, 8, 8));
                }
                g2.dispose();
            }
        });

        tblHangMuc.getColumnModel().getColumn(2).setCellRenderer(
                new UIComponents.BadgeRenderer(this::mauTrangThai)
        );
    }

    public Color mauTrangThai(String s) {
        if (s == null) return UIConstants.TEXT_MUTED;
        if (s.startsWith(TrangThaiPhieu.HOAN_THANH.getText())) return UIConstants.STATUS_SUCCESS;
        if (s.startsWith(TrangThaiPhieu.DANG_SUA.getText())) return UIConstants.PRIMARY;
        if (s.startsWith(TrangThaiPhieu.CHO_PHU_TUNG.getText())) return UIConstants.STATUS_WARNING;
        if (s.startsWith("Chờ sửa") || s.startsWith(TrangThaiPhieu.CHO_KIEM_TRA.getText())) return UIConstants.STATUS_DANGER;
        return UIConstants.TEXT_MUTED;
    }

    private JButton taoNutPillLoc(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(12f));
        b.setBorder(new EmptyBorder(6, 11, 6, 11));
        capNhatStyleNutPill(b, active);
        return b;
    }

    public void capNhatStyleNutPill(JButton b, boolean active) {
        String text = b.getText();
        Color base = UIConstants.ROLE_RECEPTION;
        if (TrangThaiPhieu.DANG_SUA.getText().equals(text)) base = UIConstants.PRIMARY;
        else if (TrangThaiPhieu.CHO_PHU_TUNG.getText().equals(text)) base = UIConstants.STATUS_WARNING;
        else if (TrangThaiPhieu.HOAN_THANH.getText().equals(text)) base = UIConstants.STATUS_SUCCESS;

        if (active) {
            b.setBackground(UIComponents.tint(base, 0.80f));
            b.setForeground(UIComponents.shade(base, 0.68f));
        } else {
            b.setBackground(UIComponents.tint(UIConstants.APP_BG, 0.35f));
            b.setForeground(UIConstants.TEXT_MUTED);
        }
    }

    public JPanel taoThePhieuItem(PhieuTiepNhanVaSuaChua phieu, String nhanTrangThai, boolean dangChon) {
        Color vien = dangChon ? UIConstants.ROLE_RECEPTION_LIGHT : UIConstants.BORDER_COLOR;
        Color nen = dangChon ? UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.94f) : UIConstants.CARD_BG;

        UIComponents.RoundedPanel item = new UIComponents.RoundedPanel(10, nen, vien);
        item.setLayout(new BorderLayout(8, 6));
        item.setBorder(new EmptyBorder(14, 16, 14, 16));
        item.setPreferredSize(new Dimension(100, 74));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = UIComponents.trong(new GridLayout(2, 1, 0, 4));
        JLabel lblMa = UIComponents.label(phieu.getMaPhieuTNSC(), UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
        String moTaXe = phieu.getXe().getBienSo() + " • " + phieu.getThongTinXeNgan();
        JLabel lblXe = UIComponents.label(moTaXe, UIConstants.FONT_BODY.deriveFont(12.5f), UIConstants.TEXT_MUTED);
        left.add(lblMa);
        left.add(lblXe);

        String tt = nhanTrangThai == null ? "" : nhanTrangThai;
        UIComponents.Badge badge = new UIComponents.Badge(tt, mauTrangThai(tt));
        JPanel right = UIComponents.trong(new GridBagLayout());
        right.add(badge);

        item.add(left, BorderLayout.CENTER);
        item.add(right, BorderLayout.EAST);
        return item;
    }

    // =========================================================================
    // LAYOUT CHIA 2 CỘT CỐ ĐỊNH TỶ LỆ & FLOWLAYOUT TỰ ĐỘNG RỚT DÒNG
    // =========================================================================

    /**
     * LayoutManager chia chính xác 2 cột theo tỷ lệ cố định (ví dụ 38% - 62%),
     * giữ nguyên kích thước 2 khung bất kể dữ liệu bên trong ngắn hay dài.
     */
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
                int h = 500;
                for (int i = 0; i < parent.getComponentCount(); i++) {
                    Component c = parent.getComponent(i);
                    if (c.isVisible()) {
                        h = Math.max(h, c.getPreferredSize().height);
                    }
                }
                return new Dimension(800, h + in.top + in.bottom);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(400, 360);
        }

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

    /**
     * FlowLayout thông minh tự động tính lại chiều cao khi các nút lọc ("Hoàn thành")
     * tràn chiều ngang và rớt xuống dòng tiếp theo.
     */
    private static class WrapFlowLayout extends FlowLayout {
        public WrapFlowLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return computeSize(target);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension d = computeSize(target);
            d.width = 100;
            return d;
        }

        private Dimension computeSize(Container target) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                Container p = target.getParent();
                while (targetWidth <= 0 && p != null) {
                    targetWidth = p.getWidth();
                    p = p.getParent();
                }
                if (targetWidth <= 0) targetWidth = 340;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = Math.max(100, targetWidth - (insets.left + insets.right + hgap * 2));

                int dimWidth = 0;
                int dimHeight = insets.top + vgap;
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();
                        if (rowWidth == 0 || rowWidth + hgap + d.width <= maxWidth) {
                            if (rowWidth > 0) rowWidth += hgap;
                            rowWidth += d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        } else {
                            dimWidth = Math.max(dimWidth, rowWidth);
                            dimHeight += rowHeight + vgap;
                            rowWidth = d.width;
                            rowHeight = d.height;
                        }
                    }
                }
                dimWidth = Math.max(dimWidth, rowWidth);
                dimHeight += rowHeight + vgap + insets.bottom;
                return new Dimension(dimWidth, dimHeight);
            }
        }
    }

    public static class ProgressBarBoGoc extends JComponent {
        private int percent = 0;

        public ProgressBarBoGoc() {
            Dimension d = new Dimension(100, 8);
            setPreferredSize(d);
            setMinimumSize(d);
        }

        public void setPercent(int percent) {
            this.percent = Math.max(0, Math.min(100, percent));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(UIConstants.APP_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            if (percent > 0) {
                int fillW = Math.max(h, Math.round(w * (percent / 100f)));
                g2.setColor(UIConstants.PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));
            }
            g2.dispose();
        }
    }

    private static class NutVienMau extends JButton {
        private final Color color;

        public NutVienMau(String text, Color color) {
            super(text);
            this.color = color;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            g2.setColor(hover ? UIComponents.tint(color, 0.90f) : UIConstants.CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(color);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // Getters cho Controller
    public JButton getBtnMenuTrangChu() { return btnMenuTrangChu; }
    public JButton getBtnMenuTienDo() { return btnMenuTienDo; }
    public JButton getBtnDangXuat() { return btnDangXuat; }
    public JTextField getTxtTimPhieu() { return fTimPhieu.getTextField(); }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
    public String[] getCacTabLoc() { return CAC_TAB_LOC; }
    public JButton[] getBtnLocTrangThai() { return btnLocTrangThai; }
    public JPanel getPnlDanhSachPhieu() { return pnlDanhSachPhieu; }
    public JLabel getLblMaPhieuChiTiet() { return lblMaPhieuChiTiet; }
    public UIComponents.Badge getBadgeTrangThaiPhieu() { return badgeTrangThaiPhieu; }
    public ProgressBarBoGoc getThanhTienDo() { return thanhTienDo; }
    public JLabel getLblValBienSo() { return lblValBienSo; }
    public JLabel getLblValXe() { return lblValXe; }
    public JLabel getLblValKhachHang() { return lblValKhachHang; }
    public JLabel getLblValTiepNhan() { return lblValTiepNhan; }
    public JLabel getLblValDuKien() { return lblValDuKien; }
    public JLabel getLblValSoKm() { return lblValSoKm; }
    public JLabel getLblValNhienLieu() { return lblValNhienLieu; }
    public JLabel getLblValYeuCau() { return lblValYeuCau; }
    public DefaultTableModel getModelHangMuc() { return modelHangMuc; }
    public JTable getTblHangMuc() { return tblHangMuc; }
    public JComboBox<String> getCboTrangThaiMoi() { return cboTrangThaiMoi; }
    public JPanel getPnlHopLoiTrangThai() { return pnlHopLoiTrangThai; }
    public JLabel getLblThongBaoLoiTrangThai() { return lblThongBaoLoiTrangThai; }
    public JButton getBtnMoYeuCauPhuTung() { return btnMoYeuCauPhuTung; }
    public JButton getBtnCapNhatTrangThai() { return btnCapNhatTrangThai; }

    public JButton getBtnQuayLai() { return btnQuayLai; }
    public JTextField getTxtMaYeuCau() { return txtMaYeuCau; }
    public JTextField getTxtPhieuSuaChuaYC() { return txtPhieuSuaChuaYC; }
    public JTextField getTxtXeYC() { return txtXeYC; }
    public JTextField getTxtKyThuatVienYC() { return txtKyThuatVienYC; }
    public JTextField getTxtNgayYeuCau() { return txtNgayYeuCau; }
    public UIComponents.Badge getBadgeTrangThaiYC() { return badgeTrangThaiYC; }
    public JTextField getTxtTimPhuTung() { return fTimPhuTung.getTextField(); }
    public JPanel getPnlDanhSachPhuTungChon() { return pnlDanhSachPhuTungChon; }
    public JLabel getLblLoiSoLuongPT() { return lblLoiSoLuongPT; }
    public JButton getBtnThemPhuTung() { return btnThemPhuTung; }
    public JButton getBtnGuiYeuCau() { return btnGuiYeuCau; }
}