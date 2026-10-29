package gui;

import util.UIConstants;
import util.UIComponents;
import util.UIComponents.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản lý bàn giao xe (JPanel) - phân hệ Lễ tân (màu ROLE_RECEPTION).
 * Phụ trách: Nguyễn Tuấn Vũ.
 */
public class QLBanGiaoXe extends JPanel {

    public static final int COT_DIEU_KIEN = 7;

    /** Dữ liệu hiển thị ở thẻ "Xác nhận bàn giao xe". */
    public static class ChiTietBanGiao {
        public String xe;              // "51H-123.45 • Toyota Vios"
        public String chuXeVaPhieu;    // "Nguyễn Văn Minh • SC-261012-021"
        public String nhanTienDo;      // "Hoàn tất 100%"
        public boolean hoanTat100;
        public String tienCong, phuTung, giamGia, nhanVAT, vat, tongThanhToan;
        public boolean duDieuKien;
        public boolean daBanGiao;
        public String thongBao;        // dòng xanh/đỏ phía dưới
    }

    // --- Danh sách ---
    private final JTextField txtTimKiem;
    private final JComboBox<String> cboLoc = new JComboBox<>(new String[]{"Chờ bàn giao", "Đã bàn giao", "Tất cả"});
    private final JTable tblDanhSach;
    private final DefaultTableModel tableModel;
    private final RoundedPanel theBang = UIComponents.card();

    // --- Thẻ xác nhận bàn giao ---
    private final RoundedPanel theChiTiet = UIComponents.card();
    private final JLabel lblXeTieuDe = new JLabel();
    private final JLabel lblChuXePhieu = new JLabel();
    private final Badge badgeTienDo = new Badge();
    private final JLabel lblTienCong = giaTri();
    private final JLabel lblPhuTung = giaTri();
    private final JLabel lblGiamGia = giaTri();
    private final JLabel lblVAT = giaTri();
    private final JLabel lblNhanVAT = new JLabel("VAT");
    private final JLabel lblTongThanhToan = new JLabel();
    private final RoundedPanel boxThongBao = UIComponents.box(UIComponents.tint(UIConstants.STATUS_SUCCESS, 0.88f));
    private final JLabel lblIconThongBao = new JLabel();
    private final JLabel lblThongBao = new JLabel();
    private final JButton btnXemPhieu = UIComponents.nutChinh("Xem phiếu xuất xưởng", UIConstants.PRIMARY_LIGHT);
    private final JButton btnDong = UIComponents.nutPhu("Đóng");
    private final JButton btnXacNhan = UIComponents.nutChinh("Xác nhận bàn giao xe", UIConstants.PRIMARY);
    private final JButton btnDongChiTiet = UIComponents.nutIcon(new LineIcon(Loai.CLOSE, UIConstants.TEXT_MUTED));

    // --- Cột phải ---
    private final RoundedPanel bannerThanhCong = UIComponents.box(UIComponents.tint(UIConstants.STATUS_SUCCESS, 0.88f));
    private final JLabel lblBannerThanhCong = new JLabel();
    private final RoundedPanel theDieuKien = UIComponents.card();
    private final RoundedPanel boxCanhBao = UIComponents.box(UIComponents.tint(UIConstants.STATUS_DANGER, 0.88f));
    private final JLabel lblCanhBao = new JLabel();
    private final JButton btnDongDieuKien = UIComponents.nutIcon(new LineIcon(Loai.CLOSE, UIConstants.TEXT_MUTED));

    public QLBanGiaoXe() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 28, 24, 28));
        noiDung.them(UIComponents.tieuDeTrang("Quản lý bàn giao xe", "Kiểm tra tiến độ và hóa đơn trước khi xác nhận bàn giao.",
                UIConstants.ROLE_RECEPTION, null), 0);

        // --- Tìm kiếm + bộ lọc ---
        IconField timKiem = new IconField("Tìm mã phiếu, biển số hoặc tên khách", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(460, 40));
        txtTimKiem = timKiem.getTextField();
        UIComponents.styleCombo(cboLoc);
        cboLoc.setPreferredSize(new Dimension(180, 40));
        JPanel hangTim = UIComponents.trong(new BorderLayout());
        JPanel trai = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.add(timKiem);
        JPanel phai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        phai.add(cboLoc);
        hangTim.add(trai, BorderLayout.WEST);
        hangTim.add(phai, BorderLayout.EAST);
        noiDung.them(hangTim, 18);

        // --- Bảng ---
        theBang.setLayout(new BorderLayout());
        tableModel = UIComponents.modelKhongSua("Mã phiếu", "Biển số", "Khách hàng", "Xe", "Tiến độ", "Hóa đơn", "Tổng tiền", "Điều kiện");
        tblDanhSach = new JTable(tableModel);
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(4).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f),
                s -> s.startsWith("100") ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER));
        tblDanhSach.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer(QLBanGiaoXe::mauHoaDon));
        tblDanhSach.getColumnModel().getColumn(COT_DIEU_KIEN).setCellRenderer(new DieuKienRenderer());
        UIComponents.datDoRong(tblDanhSach, 130, 100, 150, 110, 70, 140, 110, 190);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        noiDung.them(theBang, 16);

        // --- Hàng dưới: thẻ xác nhận (trái) + cột phải ---
        JPanel hangDuoi = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gt = new GridBagConstraints();
        gt.gridx = 0; gt.weightx = 0.6; gt.weighty = 1; gt.fill = GridBagConstraints.BOTH; gt.anchor = GridBagConstraints.NORTH;
        gt.insets = new Insets(0, 0, 0, 8);
        hangDuoi.add(taoTheChiTiet(), gt);
        GridBagConstraints gp = new GridBagConstraints();
        gp.gridx = 1; gp.weightx = 0.4; gp.weighty = 1; gp.fill = GridBagConstraints.BOTH; gp.anchor = GridBagConstraints.NORTH;
        gp.insets = new Insets(0, 8, 0, 0);
        hangDuoi.add(taoCotPhai(), gp);
        noiDung.them(hangDuoi, 16);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        theChiTiet.setVisible(false);
        bannerThanhCong.setVisible(false);
        ganSuKienGiaoDien();
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLBanGiaoXe panel = new QLBanGiaoXe();
        panel.napDuLieuMau();
        new controller.QLBanGiaoXeController(panel);
        return panel;
    }

    // ==========================================================
    // Dựng giao diện
    // ==========================================================

    private static JLabel giaTri() {
        JLabel l = new JLabel();
        l.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(15f));
        l.setForeground(UIConstants.TEXT_DARK);
        return l;
    }

    private JPanel dongTien(JLabel nhan, JLabel gia) {
        nhan.setFont(UIConstants.FONT_BODY.deriveFont(15f));
        nhan.setForeground(UIConstants.TEXT_DARK);
        JPanel p = UIComponents.trong(new BorderLayout());
        p.add(nhan, BorderLayout.WEST);
        p.add(gia, BorderLayout.EAST);
        return p;
    }

    private JPanel taoTheChiTiet() {
        theChiTiet.setLayout(new BorderLayout());
        theChiTiet.add(UIComponents.tieuDeCard(new JLabel("Xác nhận bàn giao xe"), btnDongChiTiet), BorderLayout.NORTH);

        lblXeTieuDe.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(16f));
        lblXeTieuDe.setForeground(UIConstants.TEXT_DARK);
        lblChuXePhieu.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblChuXePhieu.setForeground(UIConstants.TEXT_MUTED);
        JPanel tt = UIComponents.trong(null);
        tt.setLayout(new BoxLayout(tt, BoxLayout.Y_AXIS));
        tt.add(lblXeTieuDe);
        tt.add(Box.createVerticalStrut(2));
        tt.add(lblChuXePhieu);
        JPanel dauThe = UIComponents.trong(new BorderLayout());
        dauThe.add(tt, BorderLayout.CENTER);
        dauThe.add(badgeTienDo, BorderLayout.EAST);

        lblTongThanhToan.setFont(UIConstants.FONT_PAGE_TITLE.deriveFont(22f));
        lblTongThanhToan.setForeground(UIConstants.PRIMARY);
        RoundedPanel tong = UIComponents.box(UIComponents.tint(UIConstants.PRIMARY_LIGHT, 0.9f));
        tong.setLayout(new BorderLayout());
        tong.setBorder(new EmptyBorder(8, 14, 8, 14));
        tong.add(UIComponents.label("Tổng thanh toán (VNĐ)", UIConstants.FONT_FORM_LABEL.deriveFont(16f), UIConstants.TEXT_DARK), BorderLayout.WEST);
        tong.add(lblTongThanhToan, BorderLayout.EAST);

        boxThongBao.setLayout(new BorderLayout(10, 0));
        boxThongBao.setBorder(new EmptyBorder(11, 14, 11, 14));
        boxThongBao.add(lblIconThongBao, BorderLayout.WEST);
        lblThongBao.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        boxThongBao.add(lblThongBao, BorderLayout.CENTER);

        JPanel nut = UIComponents.trong(new BorderLayout());
        JPanel nutPhai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nutPhai.add(btnDong);
        nutPhai.add(btnXacNhan);
        JPanel nutTrai = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nutTrai.add(btnXemPhieu);
        nut.add(nutTrai, BorderLayout.WEST);
        nut.add(nutPhai, BorderLayout.EAST);

        CotDoc than = new CotDoc();
        than.setBorder(new EmptyBorder(16, 22, 20, 22));
        than.them(dauThe, 0);
        than.them(dongTien(new JLabel("Tiền công"), lblTienCong), 14);
        than.them(dongTien(new JLabel("Phụ tùng"), lblPhuTung), 8);
        than.them(dongTien(new JLabel("Giảm giá"), lblGiamGia), 8);
        than.them(dongTien(lblNhanVAT, lblVAT), 8);
        than.them(tong, 10);
        than.them(boxThongBao, 12);
        than.them(nut, 14);
        than.ketThuc();
        theChiTiet.add(than, BorderLayout.CENTER);
        return theChiTiet;
    }

    private JPanel taoCotPhai() {
        bannerThanhCong.setLayout(new BorderLayout(10, 0));
        bannerThanhCong.setBorder(new EmptyBorder(12, 16, 12, 16));
        bannerThanhCong.add(new JLabel(new LineIcon(Loai.CHECK, UIConstants.STATUS_SUCCESS)), BorderLayout.WEST);
        lblBannerThanhCong.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblBannerThanhCong.setForeground(UIComponents.shade(UIConstants.STATUS_SUCCESS, 0.7f));
        bannerThanhCong.add(lblBannerThanhCong, BorderLayout.CENTER);

        theDieuKien.setLayout(new BorderLayout());
        theDieuKien.add(UIComponents.tieuDeCard(new JLabel("Điều kiện bàn giao"), btnDongDieuKien), BorderLayout.NORTH);
        boxCanhBao.setLayout(new BorderLayout(10, 0));
        boxCanhBao.setBorder(new EmptyBorder(12, 14, 12, 14));
        boxCanhBao.add(new JLabel(new LineIcon(Loai.ALERT, UIConstants.ROLE_RECEPTION)), BorderLayout.WEST);
        lblCanhBao.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblCanhBao.setForeground(UIConstants.ROLE_RECEPTION);
        boxCanhBao.add(lblCanhBao, BorderLayout.CENTER);
        boxCanhBao.setVisible(false);

        CotDoc than = new CotDoc();
        than.setBorder(new EmptyBorder(16, 22, 20, 22));
        than.them(UIComponents.label("1. Sửa chữa đã hoàn tất 100%.", UIConstants.FONT_BODY.deriveFont(15f), UIConstants.TEXT_DARK), 0);
        than.them(UIComponents.label("2. Hóa đơn đã thanh toán hoặc công nợ hợp lệ.", UIConstants.FONT_BODY.deriveFont(15f), UIConstants.TEXT_DARK), 10);
        than.them(boxCanhBao, 14);
        than.ketThuc();
        theDieuKien.add(than, BorderLayout.CENTER);

        CotDoc cot = new CotDoc();
        cot.them(bannerThanhCong, 0);
        cot.them(theDieuKien, 12);
        cot.ketThuc();
        return cot;
    }

    private void ganSuKienGiaoDien() {
        btnDongChiTiet.addActionListener(e -> theChiTiet.setVisible(false));
        btnDong.addActionListener(e -> theChiTiet.setVisible(false));
        btnDongDieuKien.addActionListener(e -> theDieuKien.setVisible(false));
        tblDanhSach.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tblDanhSach.rowAtPoint(e.getPoint());
                boolean tren = row >= 0 && tblDanhSach.columnAtPoint(e.getPoint()) == COT_DIEU_KIEN
                        && String.valueOf(tableModel.getValueAt(row, COT_DIEU_KIEN)).startsWith("Đủ điều kiện");
                tblDanhSach.setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private static Color mauHoaDon(String s) {
        if (s.startsWith("Đã thanh toán")) return UIConstants.STATUS_SUCCESS;
        if (s.startsWith("Chưa thanh toán")) return UIConstants.STATUS_WARNING;
        if (s.startsWith("Công nợ")) return UIConstants.PRIMARY_LIGHT;
        return UIConstants.TEXT_MUTED;
    }

    /** Cột "Điều kiện": xanh khi đủ điều kiện, đỏ kèm ổ khóa khi chưa đủ. */
    private static class DieuKienRenderer implements TableCellRenderer {
        private final JLabel lbl = new JLabel();

        DieuKienRenderer() {
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setIconTextGap(8);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();
            boolean du = s.startsWith("Đủ điều kiện");
            lbl.setText(s);
            lbl.setFont(du ? UIConstants.FONT_FORM_LABEL.deriveFont(14f) : UIConstants.FONT_BODY.deriveFont(14f));
            lbl.setForeground(du ? UIConstants.PRIMARY : UIConstants.ROLE_RECEPTION);
            lbl.setIcon(du ? null : new LineIcon(Loai.LOCK, UIConstants.ROLE_RECEPTION));
            lbl.setBackground(sel ? t.getSelectionBackground() : t.getBackground());
            return lbl;
        }
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    /**
     * Mỗi dòng: {maPhieu, bienSo, khachHang, xe, tienDo ("100%"), hoaDon ("Đã thanh toán" | "Chưa thanh toán" | "Công nợ hợp lệ"),
     * tongTien ("2.970.000 đ"), dieuKien ("Đủ điều kiện • Bàn giao" | "Chưa đủ điều kiện")}.
     */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) tableModel.addRow(r);
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 8);
        int visibleRows = Math.max(3, Math.min(8, dong.size()));
        theBang.setPreferredSize(new Dimension(100, 46 + visibleRows * tblDanhSach.getRowHeight()));
        theBang.revalidate();
        revalidate();
    }

    public void hienChiTiet(ChiTietBanGiao ct) {
        lblXeTieuDe.setText(ct.xe);
        lblChuXePhieu.setText(ct.chuXeVaPhieu);
        badgeTienDo.set(ct.nhanTienDo, ct.hoanTat100 ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER);
        lblTienCong.setText(ct.tienCong);
        lblPhuTung.setText(ct.phuTung);
        lblGiamGia.setText(ct.giamGia);
        lblNhanVAT.setText(ct.nhanVAT);
        lblVAT.setText(ct.vat);
        lblTongThanhToan.setText(ct.tongThanhToan);

        Color nen = ct.duDieuKien ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER;
        boxThongBao.setBackground(UIComponents.tint(nen, 0.88f));
        lblIconThongBao.setIcon(new LineIcon(ct.duDieuKien ? Loai.CHECK : Loai.ALERT, ct.duDieuKien ? UIConstants.STATUS_SUCCESS : UIConstants.ROLE_RECEPTION));
        lblThongBao.setForeground(ct.duDieuKien ? UIComponents.shade(UIConstants.STATUS_SUCCESS, 0.7f) : UIConstants.ROLE_RECEPTION);
        lblThongBao.setText("<html>" + ct.thongBao + "</html>");

        btnXacNhan.setEnabled(ct.duDieuKien && !ct.daBanGiao);
        btnXemPhieu.setEnabled(ct.duDieuKien);
        theChiTiet.setVisible(true);
        theChiTiet.getParent().revalidate();
        theChiTiet.getParent().repaint();
    }

    public void anChiTiet() {
        theChiTiet.setVisible(false);
    }

    public void hienThongBaoThanhCong(String noiDung) {
        lblBannerThanhCong.setText("<html>" + noiDung + "</html>");
        bannerThanhCong.setVisible(true);
        bannerThanhCong.getParent().revalidate();
    }
    public void anThongBaoThanhCong() { bannerThanhCong.setVisible(false); }

    /** msg = null để ẩn hộp cảnh báo đỏ trong thẻ "Điều kiện bàn giao". */
    public void setCanhBaoDieuKien(String msg) {
        boxCanhBao.setVisible(msg != null);
        if (msg != null) lblCanhBao.setText("<html>" + msg + "</html>");
        theDieuKien.setVisible(true);
    }

    // --- Getter cho Controller ---
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<String> getCboLoc() { return cboLoc; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnXemPhieu() { return btnXemPhieu; }
    public JButton getBtnDong() { return btnDong; }
    public JButton getBtnXacNhan() { return btnXacNhan; }

    // ==========================================================
    // Dữ liệu mẫu để xem trước giao diện (xóa khi nối Controller)
    // ==========================================================

    public void napDuLieuMau() {
        List<Object[]> ds = new ArrayList<>();
        ds.add(new Object[]{"SC-261012-021", "51H-123.45", "Nguyễn Văn Minh", "Toyota Vios", "100%", "Đã thanh toán", "2.970.000 đ", "Đủ điều kiện \u2022 Bàn giao"});
        ds.add(new Object[]{"SC-261012-020", "51G-678.90", "Trần Thị Hương", "Honda City", "80%", "Chưa thanh toán", "4.400.000 đ", "Chưa đủ điều kiện"});
        ds.add(new Object[]{"SC-261012-019", "59A-246.80", "Lê Hoàng Nam", "Mazda 3", "100%", "Công nợ hợp lệ", "1.650.000 đ", "Đủ điều kiện \u2022 Bàn giao"});
        ds.add(new Object[]{"SC-261012-019", "59A-246.80", "Lê Hoàng Nam", "Mazda 3", "100%", "Công nợ hợp lệ", "1.650.000 đ", "Đủ điều kiện \u2022 Bàn giao"});
        ds.add(new Object[]{"SC-261012-019", "59A-246.80", "Lê Hoàng Nam", "Mazda 3", "100%", "Công nợ hợp lệ", "1.650.000 đ", "Đủ điều kiện \u2022 Bàn giao"});
        setDanhSach(ds);
        tblDanhSach.setRowSelectionInterval(0, 0);

        ChiTietBanGiao ct = new ChiTietBanGiao();
        ct.xe = "51H-123.45 \u2022 Toyota Vios";
        ct.chuXeVaPhieu = "Nguyễn Văn Minh \u2022 SC-261012-021";
        ct.nhanTienDo = "Hoàn tất 100%";
        ct.hoanTat100 = true;
        ct.tienCong = "800.000 đ";
        ct.phuTung = "2.000.000 đ";
        ct.giamGia = "\u2013 100.000 đ";
        ct.nhanVAT = "VAT (10% \u00D7 2.700.000)";
        ct.vat = "270.000 đ";
        ct.tongThanhToan = "2.970.000 đ";
        ct.duDieuKien = true;
        ct.thongBao = "HD-261012-021 đã thanh toán đủ. Đủ điều kiện bàn giao.";
        hienChiTiet(ct);

        anThongBaoThanhCong();
        setCanhBaoDieuKien(null);
    }
}
