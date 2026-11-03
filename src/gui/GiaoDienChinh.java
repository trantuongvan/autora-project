package gui;

import entity.PhienDangNhap;
import util.UIComponents;
import util.UIComponents.LineIcon;
import util.UIComponents.Loai;
import util.UIComponents.RoundedPanel;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Khung chính (JFrame): thanh trên cùng, menu trái và vùng CardLayout. Phụ trách: Nguyễn Xuân Việt Anh. */
public class GiaoDienChinh extends JFrame {
    private final JLabel lblTieuDeManHinh =
            UIComponents.label("", UIConstants.FONT_SECTION_TITLE.deriveFont(20f), UIConstants.TEXT_DARK);
    private final JButton btnDangXuat = new JButton("Đăng xuất", new LineIcon(Loai.LOGOUT, UIConstants.TEXT_MUTED));
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel menuPanel = UIComponents.trong(null);
    private final Map<String, NutMenu> menuButtons = new LinkedHashMap<>();

    public GiaoDienChinh() {
        super("AUTORA - Quản lý gara");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1440, 900);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);

        JPanel goc = new JPanel(new BorderLayout());
        goc.setBackground(UIConstants.APP_BG);
        goc.add(taoThanhTren(), BorderLayout.NORTH);
        goc.add(taoMenuTrai(), BorderLayout.WEST);
        contentPanel.setBackground(UIConstants.APP_BG);
        goc.add(contentPanel, BorderLayout.CENTER);
        setContentPane(goc);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static void hienThi() {
        GiaoDienChinh view = new GiaoDienChinh();
        new controller.GiaoDienChinhController(view);
        view.setVisible(true);
    }

    private JPanel taoThanhTren() {
        JPanel thanh = new JPanel(new BorderLayout(24, 0));
        thanh.setBackground(UIConstants.CARD_BG);
        thanh.setPreferredSize(new Dimension(100, 72));
        thanh.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                new EmptyBorder(0, 22, 0, 22)));

        JPanel trai = UIComponents.trong(new GridBagLayout());
        trai.add(UIComponents.logoAutora());
        lblTieuDeManHinh.setHorizontalAlignment(SwingConstants.CENTER);
        thanh.add(trai, BorderLayout.WEST);
        thanh.add(lblTieuDeManHinh, BorderLayout.CENTER);
        thanh.add(taoTaiKhoan(), BorderLayout.EAST);
        return thanh;
    }

    private JPanel taoTaiKhoan() {
        PhienDangNhap phien = PhienDangNhap.getHienTai();
        String hoTen = phien == null ? "" : phien.getHoTen();
        String vaiTro = phien == null ? "" : phien.getVaiTro();
        Color mauVaiTro = mauTheoVaiTro(vaiTro);

        RoundedPanel anhDaiDien = new RoundedPanel(36, UIComponents.tint(mauVaiTro, 0.85f), null);
        anhDaiDien.setLayout(new GridBagLayout());
        anhDaiDien.setPreferredSize(new Dimension(36, 36));
        anhDaiDien.add(UIComponents.label(chuCaiDau(hoTen), UIConstants.FONT_FORM_LABEL.deriveFont(14f), mauVaiTro));

        JPanel nguoiDung = UIComponents.trong(new GridLayout(2, 1));
        nguoiDung.add(UIComponents.label(hoTen, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK));
        nguoiDung.add(UIComponents.label(vaiTro, UIConstants.FONT_BODY, mauVaiTro));

        btnDangXuat.setContentAreaFilled(false);
        btnDangXuat.setBorderPainted(false);
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setFont(UIConstants.FONT_BODY);
        btnDangXuat.setForeground(UIConstants.TEXT_DARK);
        btnDangXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel khung = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        khung.add(anhDaiDien);
        khung.add(nguoiDung);
        JSeparator phanCach = new JSeparator(SwingConstants.VERTICAL);
        phanCach.setPreferredSize(new Dimension(1, 32));
        khung.add(phanCach);
        khung.add(btnDangXuat);
        return khung;
    }

    private JPanel taoMenuTrai() {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(UIConstants.CARD_BG);
        menu.setPreferredSize(new Dimension(240, 100));
        menu.setBorder(new MatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        JLabel lblMenu = UIComponents.label("MENU CHÍNH", UIConstants.FONT_BODY.deriveFont(15f), UIConstants.TEXT_MUTED);
        lblMenu.setBorder(new EmptyBorder(24, 16, 16, 16));
        menu.add(lblMenu, BorderLayout.NORTH);

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(0, 16, 0, 16));
        JPanel giuTren = UIComponents.trong(new BorderLayout());
        giuTren.add(menuPanel, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(giuTren, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        menu.add(cuon, BorderLayout.CENTER);

        JLabel lblChan = new JLabel("<html>Hệ thống quản lý gara ô tô<br><b>AUTORA</b> - Nhóm 13</html>");
        lblChan.setFont(UIConstants.FONT_CAPTION.deriveFont(12f));
        lblChan.setForeground(UIConstants.TEXT_MUTED);
        JPanel chan = UIComponents.trong(new BorderLayout());
        chan.setBorder(new CompoundBorder(new EmptyBorder(0, 16, 0, 16),
                new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(14, 0, 22, 0))));
        chan.add(lblChan);
        menu.add(chan, BorderLayout.SOUTH);
        return menu;
    }

    private static Color mauTheoVaiTro(String vaiTro) {
        if (PhienDangNhap.VAI_TRO_CHU_CUA_HANG.equals(vaiTro)) {
            return UIConstants.ROLE_MANAGER;
        }
        if (PhienDangNhap.VAI_TRO_LE_TAN.equals(vaiTro)) {
            return UIConstants.ROLE_RECEPTION;
        }
        return UIConstants.ROLE_TECH_STOCK;
    }

    /** "Nguyễn Xuân Việt Anh" thành "VA": hai chữ cuối là phần gọi tên trong tiếng Việt. */
    static String chuCaiDau(String hoTen) {
        String[] tu = hoTen.trim().split("\\s+");
        if (tu[0].isEmpty()) {
            return "";
        }
        String cuoi = tu[tu.length - 1].substring(0, 1);
        return (tu.length > 1 ? tu[tu.length - 2].substring(0, 1) : "") + cuoi;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public JButton themMenu(String card, String tieuDe, Loai loai) {
        NutMenu nut = new NutMenu(tieuDe, loai);
        menuButtons.put(card, nut);
        menuPanel.add(nut);
        menuPanel.add(Box.createVerticalStrut(4));

        int soThuTu = menuButtons.size();
        if (soThuTu <= 9) {
            String ten = "MENU_" + soThuTu;
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + soThuTu, InputEvent.CTRL_DOWN_MASK), ten);
            getRootPane().getActionMap().put(ten, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nut.doClick();
                }
            });
        }
        return nut;
    }

    public void themManHinh(String card, JPanel panel) {
        contentPanel.add(panel, card);
    }

    public void xoaManHinh(JPanel panel) {
        contentPanel.remove(panel);
    }

    /** Hiện màn hình card và tô đậm mục menu cardMenu. */
    public void hienManHinh(String card, String tieuDe, String cardMenu) {
        lblTieuDeManHinh.setText(tieuDe);
        cardLayout.show(contentPanel, card);
        for (Map.Entry<String, NutMenu> mucMenu : menuButtons.entrySet()) {
            mucMenu.getValue().setDangChon(mucMenu.getKey().equals(cardMenu));
        }
    }

    /** Kích hoạt chuyển sang mục menu tương ứng (tự khởi tạo màn hình nếu mở lần đầu). */
    public void moCard(String card) {
        NutMenu nut = menuButtons.get(card);
        if (nut != null) {
            nut.doClick();
        }
    }

    public JButton getBtnDangXuat() {
        return btnDangXuat;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    /** Mục menu: khi chọn có nền xanh nhạt và vạch xanh bên trái. */
    private static class NutMenu extends JButton {
        private final Icon iconThuong;
        private final Icon iconChon;
        private boolean dangChon;

        NutMenu(String text, Loai loai) {
            // Mục dài xuống dòng trước dấu "&" như thiết kế.
            super(text.length() > 16 ? "<html>" + text.replace(" & ", "<br>&amp; ") + "</html>" : text);
            iconThuong = new LineIcon(loai, UIConstants.TEXT_DARK, 22);
            iconChon = new LineIcon(loai, UIConstants.STATUS_SUCCESS, 22);
            setIcon(iconThuong);
            setIconTextGap(14);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(UIConstants.FONT_FORM_LABEL.deriveFont(14f));
            setForeground(UIConstants.TEXT_DARK);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 14, 0, 8));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setPreferredSize(new Dimension(208, 48));
        }

        void setDangChon(boolean chon) {
            dangChon = chon;
            setIcon(chon ? iconChon : iconThuong);
            setForeground(chon ? UIConstants.PRIMARY : UIConstants.TEXT_DARK);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (dangChon) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIComponents.tint(UIConstants.PRIMARY, 0.9f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UIConstants.PRIMARY);
                g2.fillRoundRect(0, 0, 12, getHeight(), 12, 12);
                g2.setColor(UIComponents.tint(UIConstants.PRIMARY, 0.9f));
                g2.fillRect(8, 0, 6, getHeight());
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
