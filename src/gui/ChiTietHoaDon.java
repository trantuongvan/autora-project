package gui;

import controller.ChiTietHoaDonController;
import controller.DieuHuongHoaDon;
import entity.HinhThucThanhToan;
import entity.KhuyenMai;
import entity.Thue;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Một trang cho cả lập hóa đơn và xem hóa đơn, thay các hộp thoại rời theo Figma. Phụ trách: Trần Thị Tường Vân. */
public class ChiTietHoaDon extends JPanel {
    private final JLabel lblTieuDe = new JLabel();
    private final JButton btnQuayLai = UIComponents.nutPhu("Quay lại");
    private final JButton btnIn = UIComponents.nutChinh("In hóa đơn", UIConstants.PRIMARY);

    private final JLabel lblKhachHang = dongThongTin();
    private final JLabel lblSdt = dongThongTin();
    private final JLabel lblDiaChi = dongThongTin();
    private final JLabel lblEmail = dongThongTin();
    private final JLabel lblBienSo = dongThongTin();
    private final JLabel lblXe = dongThongTin();
    private final JLabel lblVin = dongThongTin();
    private final JLabel lblMaPhieu = dongThongTin();
    private final JLabel lblNgayLap = dongThongTin();
    private final JLabel lblHinhThuc = dongThongTin();
    private final JLabel lblMaKM = dongThongTin();
    private final Badge lblTrangThai = new Badge();

    private final DefaultTableModel modelDichVu = UIComponents.modelKhongSua(
            "Tên dịch vụ", "Mã DV", "Kỹ thuật viên", "Trạng thái", "Tiền công");
    private final JTable tblDichVu = new JTable(modelDichVu);
    private final DefaultTableModel modelPhuTung = UIComponents.modelKhongSua(
            "Tên phụ tùng", "Mã SKU", "Số lượng", "Giá bán", "Thành tiền");
    private final JTable tblPhuTung = new JTable(modelPhuTung);

    private final JLabel lblQr = new JLabel("", SwingConstants.CENTER);
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();
    private final JComboBox<Thue> cboThue = new JComboBox<>();
    private final JComboBox<HinhThucThanhToan> cboHinhThuc = new JComboBox<>();
    private final JTextField txtTienKhachDua = new JTextField();
    private final JLabel lblTienThua = UIComponents.label(" ", UIConstants.FONT_FORM_LABEL, UIConstants.TEXT_DARK);
    private final JTextField txtLyDoHuy = new JTextField();
    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnGhiCongNo = UIComponents.nutPhu("Ghi nhận công nợ");
    private final JButton btnHuy = UIComponents.nutPhu("Hủy hóa đơn");
    private final JButton btnXacNhan = UIComponents.nutChinh("Xác nhận thanh toán", UIConstants.PRIMARY);
    private JPanel oKhuyenMai;
    private JPanel oThue;
    private JPanel oTienKhachDua;
    private JPanel oLyDoHuy;

    private final JLabel lblTongCong = giaTriTong();
    private final JLabel lblTongPhuTung = giaTriTong();
    private final JLabel lblTamTinh = giaTriTong();
    private final JLabel lblGiam = giaTriTong();
    private final JLabel lblNhanVat = UIComponents.label("VAT", UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
    private final JLabel lblVat = giaTriTong();
    private final JLabel lblTong = UIComponents.label("", UIConstants.FONT_SECTION_TITLE.deriveFont(18f), UIConstants.TEXT_DARK);

    public ChiTietHoaDon() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        lblTieuDe.setFont(UIConstants.FONT_PAGE_TITLE.deriveFont(24f));
        lblTieuDe.setForeground(UIConstants.ROLE_RECEPTION);
        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(taoDauTrang(), 0);
        noiDung.them(taoTheThongTin(), 16);
        noiDung.them(taoTheChiTiet(), 16);
        noiDung.them(taoHangThanhToan(), 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Mở hóa đơn đã có. */
    public static JPanel createPanelHoaDon(String maHD, DieuHuongHoaDon dieuHuong) {
        ChiTietHoaDon panel = new ChiTietHoaDon();
        new ChiTietHoaDonController(panel, maHD, null, dieuHuong);
        return panel;
    }

    /** Mở trang lập hóa đơn cho phiếu đã hoàn thành. */
    public static JPanel createPanelLap(String maPhieu, DieuHuongHoaDon dieuHuong) {
        ChiTietHoaDon panel = new ChiTietHoaDon();
        new ChiTietHoaDonController(panel, null, maPhieu, dieuHuong);
        return panel;
    }

    private static JLabel dongThongTin() {
        return UIComponents.label(" ", UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
    }

    private static JLabel giaTriTong() {
        return UIComponents.label("", UIConstants.FONT_FORM_LABEL, UIConstants.TEXT_DARK);
    }

    private JPanel taoDauTrang() {
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnQuayLai);
        nut.add(btnIn);
        JPanel dau = UIComponents.trong(new BorderLayout());
        dau.add(lblTieuDe, BorderLayout.CENTER);
        dau.add(nut, BorderLayout.EAST);
        return dau;
    }

    private static JPanel cotThongTin(String tieuDe, JComponent... dong) {
        JPanel cot = UIComponents.trong(new GridLayout(0, 1, 0, 4));
        cot.add(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK));
        for (JComponent d : dong) {
            cot.add(d);
        }
        return cot;
    }

    private JPanel taoTheThongTin() {
        JPanel oTrangThai = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        oTrangThai.add(UIComponents.label("Trạng thái:  ", UIConstants.FONT_BODY, UIConstants.TEXT_DARK));
        oTrangThai.add(lblTrangThai);
        RoundedPanel the = UIComponents.card();
        the.setLayout(new GridLayout(1, 3, 20, 0));
        the.setBorder(new EmptyBorder(16, 20, 16, 20));
        the.add(cotThongTin("Khách hàng", lblKhachHang, lblSdt, lblDiaChi, lblEmail));
        the.add(cotThongTin("Phương tiện", lblBienSo, lblXe, lblVin, lblMaPhieu));
        the.add(cotThongTin("Thanh toán", lblNgayLap, lblHinhThuc, lblMaKM, oTrangThai));
        return the;
    }

    private JPanel taoTheChiTiet() {
        ganCanPhai(tblDichVu, 4);
        ganCanPhai(tblPhuTung, 4);
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBorder(new EmptyBorder(16, 20, 16, 20));
        the.add(nhanTrai("Dịch vụ"));
        the.add(Box.createVerticalStrut(8));
        the.add(trai(UIComponents.cuonBang(tblDichVu)));
        the.add(Box.createVerticalStrut(16));
        the.add(nhanTrai("Phụ tùng"));
        the.add(Box.createVerticalStrut(8));
        the.add(trai(UIComponents.cuonBang(tblPhuTung)));
        return the;
    }

    private static JComponent nhanTrai(String tieuDe) {
        return trai(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK));
    }

    private static JComponent trai(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private static void ganCanPhai(JTable bang, int cot) {
        UIComponents.styleTable(bang);
        DefaultTableCellRenderer veTien = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon, boolean focus, int h, int c) {
                super.getTableCellRendererComponent(t, v, chon, focus, h, c);
                setBorder(new EmptyBorder(0, 0, 0, 16));
                return this;
            }
        };
        veTien.setHorizontalAlignment(SwingConstants.RIGHT);
        bang.getColumnModel().getColumn(cot).setCellRenderer(veTien);
        UIComponents.canPhaiTieuDe(bang, cot);
    }

    private JPanel taoHangThanhToan() {
        JPanel hang = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.NORTH;
        gc.weightx = 0.68;
        gc.weighty = 1;
        gc.insets = new Insets(0, 0, 0, 16);
        hang.add(taoTheThanhToan(), gc);
        gc.gridx = 1;
        gc.weightx = 0.32;
        gc.insets = new Insets(0, 0, 0, 0);
        hang.add(taoTheTongHop(), gc);
        return hang;
    }

    private JPanel taoTheThanhToan() {
        UIComponents.styleCombo(cboKhuyenMai);
        UIComponents.styleCombo(cboThue);
        UIComponents.styleCombo(cboHinhThuc);
        UIComponents.styleInput(txtTienKhachDua);
        UIComponents.styleInput(txtLyDoHuy);
        cboKhuyenMai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> ds, Object v, int i, boolean chon, boolean focus) {
                KhuyenMai km = (KhuyenMai) v;
                String chu = km == null ? "Không áp dụng" : km.getMaKM() + " — " + km.getTenChuongTrinh();
                return super.getListCellRendererComponent(ds, chu, i, chon, focus);
            }
        });

        RoundedPanel khungQr = new RoundedPanel(8, UIConstants.APP_BG, UIConstants.BORDER_COLOR);
        khungQr.setLayout(new BorderLayout());
        khungQr.setPreferredSize(new Dimension(190, 230));
        lblQr.setFont(UIConstants.FONT_CAPTION);
        lblQr.setForeground(UIConstants.TEXT_MUTED);
        lblQr.setBorder(new EmptyBorder(4, 6, 4, 6));
        khungQr.add(lblQr, BorderLayout.CENTER);
        JPanel oQr = UIComponents.trong(new BorderLayout());
        oQr.add(khungQr, BorderLayout.NORTH);

        oKhuyenMai = QLTiepNhanSuaChua.o("Khuyến mãi", false, cboKhuyenMai);
        oThue = QLTiepNhanSuaChua.o("Thuế", true, cboThue);
        oTienKhachDua = QLTiepNhanSuaChua.o("Tiền khách đưa", true, txtTienKhachDua);
        oLyDoHuy = QLTiepNhanSuaChua.o("Lý do hủy", false, txtLyDoHuy);

        // BoxLayout bỏ qua ô đang ẩn, GridLayout thì vẫn chừa chỗ trống.
        JPanel form = UIComponents.trong(null);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JPanel oHinhThuc = QLTiepNhanSuaChua.o("Hình thức thanh toán", true, cboHinhThuc);
        for (JComponent c : new JComponent[] {oKhuyenMai, oThue, oHinhThuc, oTienKhachDua}) {
            c.setBorder(new EmptyBorder(0, 0, 10, 0));
            form.add(c);
        }
        lblTienThua.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblTienThua);
        JPanel giuForm = UIComponents.trong(new BorderLayout());
        giuForm.add(form, BorderLayout.NORTH);

        JPanel tren = UIComponents.trong(new BorderLayout(16, 0));
        tren.add(oQr, BorderLayout.WEST);
        tren.add(giuForm, BorderLayout.CENTER);

        btnHuy.setForeground(UIConstants.ROLE_RECEPTION);
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnGhiCongNo);
        nut.add(btnHuy);
        nut.add(btnXacNhan);
        JPanel duoi = UIComponents.trong(new BorderLayout(0, 8));
        duoi.add(lblLoi, BorderLayout.NORTH);
        duoi.add(nut, BorderLayout.SOUTH);

        JPanel giua = UIComponents.trong(new BorderLayout(0, 12));
        giua.add(tren, BorderLayout.NORTH);
        giua.add(oLyDoHuy, BorderLayout.CENTER);
        giua.add(duoi, BorderLayout.SOUTH);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 12));
        the.setBorder(new EmptyBorder(16, 20, 16, 20));
        the.add(UIComponents.label("Thanh toán", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), BorderLayout.NORTH);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheTongHop() {
        JPanel luoi = UIComponents.trong(new GridLayout(0, 1, 0, 12));
        luoi.add(dongTong(new JLabel("Tổng tiền công"), lblTongCong));
        luoi.add(dongTong(new JLabel("Tổng tiền phụ tùng"), lblTongPhuTung));
        luoi.add(dongTong(new JLabel("Tạm tính"), lblTamTinh));
        luoi.add(dongTong(new JLabel("Giảm giá"), lblGiam));
        luoi.add(dongTong(lblNhanVat, lblVat));
        JPanel dongCuoi = UIComponents.trong(new BorderLayout());
        dongCuoi.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(1, 0, 0, 0, UIConstants.TEXT_DARK), new EmptyBorder(12, 0, 0, 0)));
        dongCuoi.add(UIComponents.label("Tổng thanh toán", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), BorderLayout.WEST);
        dongCuoi.add(lblTong, BorderLayout.EAST);
        JPanel than = UIComponents.trong(new BorderLayout(0, 12));
        than.add(luoi, BorderLayout.NORTH);
        than.add(dongCuoi, BorderLayout.CENTER);
        JPanel giu = UIComponents.trong(new BorderLayout());
        giu.add(than, BorderLayout.NORTH);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 12));
        the.setBorder(new EmptyBorder(16, 20, 16, 20));
        the.add(UIComponents.label("Tổng hợp tiền", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), BorderLayout.NORTH);
        the.add(giu, BorderLayout.CENTER);
        return the;
    }

    private static JPanel dongTong(JLabel nhan, JLabel giaTri) {
        nhan.setFont(UIConstants.FONT_BODY);
        nhan.setForeground(UIConstants.TEXT_DARK);
        JPanel dong = UIComponents.trong(new BorderLayout());
        dong.add(nhan, BorderLayout.WEST);
        dong.add(giaTri, BorderLayout.EAST);
        return dong;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setTieuDe(String tieuDe) {
        lblTieuDe.setText(tieuDe);
    }

    public void setThongTin(String[] khachHang, String[] phuongTien, String[] thanhToan, String trangThai) {
        JLabel[] dong = {lblKhachHang, lblSdt, lblDiaChi, lblEmail, lblBienSo, lblXe, lblVin, lblMaPhieu, lblNgayLap, lblHinhThuc, lblMaKM};
        for (int i = 0; i < 4; i++) {
            dong[i].setText(khachHang[i]);
            dong[i + 4].setText(phuongTien[i]);
        }
        for (int i = 0; i < 3; i++) {
            dong[i + 8].setText(thanhToan[i]);
        }
        Color mau = trangThai == null ? UIConstants.TEXT_MUTED : QLHoaDon.mauTrangThai(trangThai);
        lblTrangThai.set(trangThai == null ? "Chưa lập" : trangThai, mau);
    }

    public void setChiTiet(java.util.List<Object[]> dichVu, java.util.List<Object[]> phuTung) {
        for (Object[] d : dichVu) {
            modelDichVu.addRow(d);
        }
        for (Object[] d : phuTung) {
            modelPhuTung.addRow(d);
        }
        canChieuCao(tblDichVu);
        canChieuCao(tblPhuTung);
        revalidate();
    }

    /** Thêm vài điểm ảnh vì viền khung cuộn làm thiếu 1 điểm ảnh, thanh cuộn dọc sẽ hiện dù đủ chỗ. */
    private static void canChieuCao(JTable bang) {
        UIComponents.canChieuCaoBang(bang, 1, 8);
        JScrollPane cuon = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, bang);
        Dimension co = cuon.getPreferredSize();
        cuon.setPreferredSize(new Dimension(co.width, co.height + 3));
    }

    public void setTongHop(String cong, String phuTung, String tamTinh, String giam, String nhanVat, String vat, String tong) {
        lblTongCong.setText(cong);
        lblTongPhuTung.setText(phuTung);
        lblTamTinh.setText(tamTinh);
        lblGiam.setText(giam);
        lblNhanVat.setText(nhanVat);
        lblVat.setText(vat);
        lblTong.setText(tong);
    }

    public void hienQr(Image anh) {
        lblQr.setText("");
        lblQr.setIcon(anh == null ? null : new ImageIcon(anh));
    }

    /** Chữ trong khung QR: chỗ giữ chỗ, đang tải hoặc thông tin chuyển khoản tay khi tải lỗi. */
    public void hienChuQr(String chu) {
        lblQr.setIcon(null);
        lblQr.setText("<html><div style='text-align:center'>" + chu.replace("\n", "<br>") + "</div></html>");
    }

    public void hienLoi(String thongBao) {
        UIComponents.showError(lblLoi, thongBao);
    }

    public void setTienThua(String chu) {
        lblTienThua.setText(chu == null ? " " : chu);
    }

    /** Hiện đúng ô và nút theo chế độ. */
    public void setHienThi(boolean khuyenMaiThue, boolean tienKhachDua, boolean lyDoHuy, boolean nutGhiNo, boolean nutHuy, boolean nutXacNhan) {
        oKhuyenMai.setVisible(khuyenMaiThue);
        oThue.setVisible(khuyenMaiThue);
        oTienKhachDua.setVisible(tienKhachDua);
        lblTienThua.setVisible(tienKhachDua);
        oLyDoHuy.setVisible(lyDoHuy);
        btnGhiCongNo.setVisible(nutGhiNo);
        btnHuy.setVisible(nutHuy);
        btnXacNhan.setVisible(nutXacNhan);
        revalidate();
    }

    public JButton getBtnQuayLai() { return btnQuayLai; }
    public JButton getBtnIn() { return btnIn; }
    public JComboBox<KhuyenMai> getCboKhuyenMai() { return cboKhuyenMai; }
    public JComboBox<Thue> getCboThue() { return cboThue; }
    public JComboBox<HinhThucThanhToan> getCboHinhThuc() { return cboHinhThuc; }
    public JTextField getTxtTienKhachDua() { return txtTienKhachDua; }
    public JTextField getTxtLyDoHuy() { return txtLyDoHuy; }
    public JButton getBtnGhiCongNo() { return btnGhiCongNo; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnXacNhan() { return btnXacNhan; }
}
