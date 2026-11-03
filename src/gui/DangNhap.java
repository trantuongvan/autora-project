package gui;

import util.UIComponents;
import util.UIComponents.LineIcon;
import util.UIComponents.Loai;
import util.UIComponents.PlaceholderField;
import util.UIComponents.RoundedPanel;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Màn hình đăng nhập (JFrame). Phụ trách: Nguyễn Xuân Việt Anh. */
public class DangNhap extends JFrame {
    private static final char KY_TU_AN = '•';

    private final JTextField txtTenDangNhap = new PlaceholderField("Nhập tên đăng nhập");
    private final JPasswordField pwdMatKhau = new JPasswordField();
    private final JToggleButton btnHienMatKhau = new JToggleButton(new LineIcon(Loai.EYE, UIConstants.TEXT_MUTED));
    private final JCheckBox chkNhoDangNhap = new JCheckBox("Nhớ đăng nhập");
    private final JButton btnQuenMatKhau = new JButton("Quên mật khẩu?");
    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnDangNhap = UIComponents.nutChinh("Đăng nhập", UIConstants.PRIMARY);

    public DangNhap() {
        super("AUTORA - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(taoBangThuongHieu(), BorderLayout.WEST);
        add(taoVungDangNhap(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnDangNhap);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1280, 820);
        setLocationRelativeTo(null);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static void hienThi() {
        DangNhap view = new DangNhap();
        new controller.DangNhapController(view);
        view.setVisible(true);
    }

    private JPanel taoBangThuongHieu() {
        JPanel bang = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY_LIGHT.darker());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                g2.fill(new Ellipse2D.Float(300, -160, 420, 420));
                g2.fill(new Ellipse2D.Float(-110, getHeight() - 244, 300, 300));
                g2.dispose();
            }
        };
        bang.setBackground(UIConstants.PRIMARY);
        bang.setPreferredSize(new Dimension(560, 100));
        bang.setBorder(new EmptyBorder(56, 56, 48, 56));

        RoundedPanel theLogo = new RoundedPanel(12, UIConstants.CARD_BG, null);
        theLogo.setLayout(new GridBagLayout());
        theLogo.setPreferredSize(new Dimension(256, 64));
        theLogo.add(UIComponents.logoAutora());
        JPanel dongLogo = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dongLogo.add(theLogo);
        bang.add(dongLogo, BorderLayout.NORTH);

        JPanel gioiThieu = UIComponents.trong(null);
        gioiThieu.setLayout(new BoxLayout(gioiThieu, BoxLayout.Y_AXIS));
        gioiThieu.add(UIComponents.label("Hệ thống quản lý gara ô tô", UIConstants.FONT_PAGE_TITLE.deriveFont(32f), Color.WHITE));
        gioiThieu.add(Box.createVerticalStrut(18));
        JLabel lblMoTa = new JLabel("<html><body style='width:300px'>Giải pháp toàn diện cho lễ tân, kỹ thuật và chủ gara — từ tiếp nhận xe đến bàn giao.</body></html>");
        lblMoTa.setFont(UIConstants.FONT_BODY.deriveFont(15f));
        lblMoTa.setForeground(Color.WHITE);
        gioiThieu.add(lblMoTa);
        gioiThieu.add(Box.createVerticalStrut(18));
        for (String tinhNang : new String[] {"Tiếp nhận & theo dõi phiếu sửa chữa theo từng xe",
                "Quản lý hồ sơ xe, lịch sử và định giá sửa chữa",
                "In phiếu xuất xưởng, hóa đơn và bàn giao xe",
                "Phân quyền theo vai trò: lễ tân, thợ, chủ gara"}) {
            JLabel lblTinhNang = UIComponents.label(tinhNang, UIConstants.FONT_BODY.deriveFont(14f), Color.WHITE);
            lblTinhNang.setIcon(new LineIcon(Loai.CHECK, Color.WHITE, 20));
            lblTinhNang.setIconTextGap(10);
            lblTinhNang.setBorder(new EmptyBorder(6, 0, 6, 0));
            gioiThieu.add(lblTinhNang);
        }
        JPanel giua = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        giua.add(gioiThieu, gc);
        bang.add(giua, BorderLayout.CENTER);

        JPanel chan = UIComponents.trong(new GridLayout(2, 1, 0, 4));
        chan.add(UIComponents.label("AUTORA • Bình Thạnh, TP.HCM", UIConstants.FONT_FORM_LABEL, Color.WHITE));
        chan.add(UIComponents.label("Hotline: 028 3840 8888 • support@autoragarage.vn", UIConstants.FONT_BODY.deriveFont(12f), Color.WHITE));
        bang.add(chan, BorderLayout.SOUTH);
        return bang;
    }

    private JPanel taoVungDangNhap() {
        JPanel vung = new JPanel(new BorderLayout());
        vung.setBackground(UIConstants.CARD_BG);
        JPanel giua = UIComponents.trong(new GridBagLayout());
        giua.add(taoTheDangNhap());
        vung.add(giua, BorderLayout.CENTER);
        JLabel lblBanQuyen = UIComponents.label("© 2026 AUTORA Garage • 123 Điện Biên Phủ, Bình Thạnh, TP.HCM",
                UIConstants.FONT_BODY.deriveFont(12f), UIConstants.TEXT_MUTED);
        lblBanQuyen.setHorizontalAlignment(SwingConstants.CENTER);
        lblBanQuyen.setBorder(new EmptyBorder(0, 0, 32, 0));
        vung.add(lblBanQuyen, BorderLayout.SOUTH);
        return vung;
    }

    private JPanel taoTheDangNhap() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new GridBagLayout());
        the.setBorder(new EmptyBorder(32, 32, 28, 32));
        the.setPreferredSize(new Dimension(420, 488));
        UIComponents.styleInput(txtTenDangNhap);
        UIComponents.styleInput(pwdMatKhau);
        pwdMatKhau.setEchoChar(KY_TU_AN);

        btnHienMatKhau.setContentAreaFilled(false);
        btnHienMatKhau.setBorderPainted(false);
        btnHienMatKhau.setFocusPainted(false);
        btnHienMatKhau.setToolTipText("Hiện hoặc ẩn mật khẩu");
        btnHienMatKhau.getAccessibleContext().setAccessibleName("Hiện mật khẩu");
        btnHienMatKhau.addActionListener(e -> pwdMatKhau.setEchoChar(btnHienMatKhau.isSelected() ? (char) 0 : KY_TU_AN));
        JPanel oMatKhau = UIComponents.trong(new BorderLayout(4, 0));
        oMatKhau.add(pwdMatKhau, BorderLayout.CENTER);
        oMatKhau.add(btnHienMatKhau, BorderLayout.EAST);

        chkNhoDangNhap.setOpaque(false);
        chkNhoDangNhap.setFont(UIConstants.FONT_BODY);
        chkNhoDangNhap.setForeground(UIConstants.TEXT_MUTED);
        btnQuenMatKhau.setContentAreaFilled(false);
        btnQuenMatKhau.setBorderPainted(false);
        btnQuenMatKhau.setFocusPainted(false);
        btnQuenMatKhau.setMargin(new Insets(0, 0, 0, 0));
        btnQuenMatKhau.setFont(UIConstants.FONT_FORM_LABEL);
        btnQuenMatKhau.setForeground(UIConstants.PRIMARY);
        btnQuenMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel dongTuyChon = UIComponents.trong(new BorderLayout());
        dongTuyChon.add(chkNhoDangNhap, BorderLayout.WEST);
        dongTuyChon.add(btnQuenMatKhau, BorderLayout.EAST);

        JPanel dongChia = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gcChia = new GridBagConstraints();
        gcChia.fill = GridBagConstraints.HORIZONTAL;
        gcChia.weightx = 1;
        dongChia.add(new JSeparator(), gcChia);
        JLabel lblHoac = UIComponents.label("hoặc", UIConstants.FONT_BODY.deriveFont(12f), UIConstants.TEXT_MUTED);
        lblHoac.setBorder(new EmptyBorder(0, 10, 0, 10));
        gcChia.weightx = 0;
        dongChia.add(lblHoac, gcChia);
        gcChia.weightx = 1;
        dongChia.add(new JSeparator(), gcChia);

        JLabel lblGhiChu = new JLabel("<html><center>Chưa có tài khoản? Liên hệ chủ gara để được cấp quyền truy cập.</center></html>");
        lblGhiChu.setFont(UIConstants.FONT_BODY);
        lblGhiChu.setForeground(UIConstants.TEXT_MUTED);
        lblGhiChu.setHorizontalAlignment(SwingConstants.CENTER);

        int hang = 0;
        them(the, UIComponents.label("Đăng nhập", UIConstants.FONT_PAGE_TITLE.deriveFont(28f), UIConstants.TEXT_DARK), hang++, 0);
        them(the, UIComponents.label("Truy cập hệ thống quản lý gara AUTORA.", UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_MUTED), hang++, 4);
        them(the, UIComponents.formLabel("Tên đăng nhập", false), hang++, 20);
        them(the, txtTenDangNhap, hang++, 6);
        them(the, UIComponents.formLabel("Mật khẩu", false), hang++, 14);
        them(the, oMatKhau, hang++, 6);
        them(the, dongTuyChon, hang++, 10);
        them(the, lblLoi, hang++, 4);
        them(the, btnDangNhap, hang++, 4);
        them(the, dongChia, hang++, 16);
        them(the, lblGhiChu, hang, 14);
        return the;
    }

    private void them(JPanel cha, Component con, int hang, int khoangTren) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = hang;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(khoangTren, 0, 0, 0);
        cha.add(con, gc);
    }

    public void hienLoi(String thongBao) {
        UIComponents.showError(lblLoi, thongBao);
    }

    public JTextField getTxtTenDangNhap() {
        return txtTenDangNhap;
    }

    public JPasswordField getPwdMatKhau() {
        return pwdMatKhau;
    }

    public JCheckBox getChkNhoDangNhap() {
        return chkNhoDangNhap;
    }

    public JButton getBtnQuenMatKhau() {
        return btnQuenMatKhau;
    }

    public JButton getBtnDangNhap() {
        return btnDangNhap;
    }
}
