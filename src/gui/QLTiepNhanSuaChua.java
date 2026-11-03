package gui;

import controller.DieuHuongPhieu;
import controller.QLTiepNhanSuaChuaController;
import entity.DichVu;
import entity.KhachHang;
import entity.PhieuTiepNhanVaSuaChua;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/** Màn hình Tiếp nhận xe & Lập phiếu (JPanel) theo Figma. Phụ trách: Nguyễn Xuân Việt Anh. */
public class QLTiepNhanSuaChua extends JPanel {
    // --- Tra cứu ---
    private final JTextField txtTraCuu = new PlaceholderField("Nhập biển số xe hoặc số điện thoại");
    private final JButton btnTraCuu = UIComponents.nutIcon(new LineIcon(Loai.SEARCH, UIConstants.PRIMARY, 22));
    private final ThanhTienTrinh thanhTienTrinh = new ThanhTienTrinh(1, "Tiếp nhận", "Chỉ định", "Xuất kho", "Thanh toán");

    // --- Khách hàng ---
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtDiaChi = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtMaSoThue = new JTextField();
    private final JRadioButton radVangLai = new JRadioButton(KhachHang.LOAI_KHACH_HANG[0], true);
    private final JRadioButton radThanhVien = new JRadioButton(KhachHang.LOAI_KHACH_HANG[1]);
    private final JRadioButton radVip = new JRadioButton(KhachHang.LOAI_KHACH_HANG[2]);

    // --- Phương tiện ---
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtSoKhung = new JTextField();
    private final JTextField txtHangXe = new JTextField();
    private final JTextField txtDongXe = new JTextField();
    private final JTextField txtDoiXe = new JTextField();
    private final JTextField txtMauSon = new JTextField();

    // --- Tình trạng xe ---
    private final JTextField txtSoKm = new JTextField();
    private final JSlider sldNhienLieu = new JSlider(0, 100, 50);
    private final JLabel lblNhienLieu = UIComponents.formLabel("Mức nhiên liệu: 50%", true);
    private final JTextField txtNgoaiThat = new JTextField();
    private final JTextField txtNoiThat = new JTextField();
    private final JTextField txtTaiSan = new JTextField();
    private final JTextArea txtGhiChu = new JTextArea(3, 20);

    // --- Yêu cầu & dịch vụ ---
    private final JTextArea txtYeuCau = new JTextArea(3, 20);
    private final JList<DichVu> lstDichVu = new JList<>(new DefaultListModel<>());
    private final JTextField txtPhuTungDuKien = new JTextField();

    // --- Phân công & trạng thái ---
    private final JComboBox<String> cboTrangThai = new JComboBox<>(PhieuTiepNhanVaSuaChua.TRANG_THAI);
    private final JRadioButton radThuong = new JRadioButton("Thường", true);
    private final JRadioButton radKhan = new JRadioButton("Khẩn");
    private final JTextField txtDuKien = new PlaceholderField("dd/MM/yyyy HH:mm");

    // --- Thanh hành động ---
    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuuVaChiDinh = UIComponents.nutPhu("Lưu & Tiếp tục chỉ định thợ");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu phiếu tiếp nhận", UIConstants.PRIMARY);

    private QLTiepNhanSuaChuaController controller;

    public QLTiepNhanSuaChua() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(taoThanhTraCuu(), 0);
        noiDung.them(taoTheTienTrinh(), 16);
        noiDung.them(taoTheKhachHang(), 16);
        noiDung.them(taoThePhuongTien(), 16);
        noiDung.them(taoTheTinhTrang(), 16);
        noiDung.them(taoTheYeuCau(), 16);
        noiDung.them(taoThePhanCong(), 16);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        add(taoThanhHanhDong(), BorderLayout.SOUTH);
        for (JTextField o : new JTextField[] {txtTraCuu, txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail, txtMaSoThue, txtBienSo,
                txtSoKhung, txtHangXe, txtDongXe, txtDoiXe, txtMauSon, txtSoKm, txtNgoaiThat, txtNoiThat, txtTaiSan,
                txtPhuTungDuKien, txtDuKien}) {
            UIComponents.styleInput(o);
        }
        UIComponents.styleCombo(cboTrangThai);
        // Phiếu mới luôn bắt đầu ở "Chờ kiểm tra"; đổi trạng thái tại Chi tiết phiếu.
        cboTrangThai.setEnabled(false);
        // Ô tra cứu nằm sẵn trong thẻ nên không cần viền riêng.
        txtTraCuu.setBorder(new EmptyBorder(6, 2, 6, 2));
        sldNhienLieu.addChangeListener(e -> lblNhienLieu.setText("Mức nhiên liệu: " + sldNhienLieu.getValue() + "%"));
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel(DieuHuongPhieu dieuHuong) {
        QLTiepNhanSuaChua panel = new QLTiepNhanSuaChua();
        panel.controller = new QLTiepNhanSuaChuaController(panel, dieuHuong);
        return panel;
    }

    private JPanel taoThanhTraCuu() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(8, 0));
        the.setBorder(new EmptyBorder(6, 14, 6, 10));
        the.add(txtTraCuu, BorderLayout.CENTER);
        btnTraCuu.setToolTipText("Tra cứu xe hoặc khách hàng");
        the.add(btnTraCuu, BorderLayout.EAST);
        return the;
    }

    private JPanel taoTheTienTrinh() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.setBorder(new EmptyBorder(8, 20, 8, 20));
        the.add(thanhTienTrinh);
        return the;
    }

    private JPanel taoTheKhachHang() {
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(radVangLai);
        nhom.add(radThanhVien);
        nhom.add(radVip);
        JPanel than = than();
        than.add(luoi(2, o("Họ và tên", true, txtHoTen), o("Số điện thoại", true, txtSoDienThoai),
                o("Địa chỉ", true, txtDiaChi), o("Email", false, txtEmail)));
        than.add(Box.createVerticalStrut(12));
        than.add(luoi(2, o("Phân loại khách hàng", true, luoi(3, khungChon(radVangLai), khungChon(radThanhVien), khungChon(radVip))),
                o("Mã số thuế", false, txtMaSoThue)));
        return the("Thông tin khách hàng", Loai.USER, than);
    }

    private JPanel taoThePhuongTien() {
        JPanel than = than();
        than.add(luoi(3, o("Biển số", true, txtBienSo), o("Số khung", true, txtSoKhung), o("Hãng xe", true, txtHangXe),
                o("Dòng xe", true, txtDongXe), o("Đời xe", true, txtDoiXe), o("Màu sơn", true, txtMauSon)));
        return the("Thông tin phương tiện", Loai.CAR, than);
    }

    private JPanel taoTheTinhTrang() {
        sldNhienLieu.setOpaque(false);
        JPanel oNhienLieu = UIComponents.trong(new BorderLayout(0, 6));
        oNhienLieu.add(lblNhienLieu, BorderLayout.NORTH);
        oNhienLieu.add(sldNhienLieu, BorderLayout.CENTER);
        JPanel than = than();
        than.add(luoi(2, o("Số km hiện tại", true, txtSoKm), oNhienLieu,
                o("Đánh giá ngoại thất", true, txtNgoaiThat), o("Đánh giá nội thất", true, txtNoiThat)));
        than.add(Box.createVerticalStrut(12));
        than.add(o("Tài sản trên xe", true, txtTaiSan));
        than.add(Box.createVerticalStrut(12));
        than.add(o("Ghi chú", false, vungChu(txtGhiChu)));
        return the("Kiểm tra tình trạng xe", Loai.SEARCH, than);
    }

    private JPanel taoTheYeuCau() {
        lstDichVu.setVisibleRowCount(4);
        lstDichVu.setFont(UIConstants.FONT_BODY);
        lstDichVu.setToolTipText("Giữ Ctrl để chọn nhiều dịch vụ");
        JPanel than = than();
        than.add(o("Yêu cầu ban đầu của khách", true, vungChu(txtYeuCau)));
        than.add(Box.createVerticalStrut(12));
        than.add(o("Hạng mục dịch vụ (giữ Ctrl để chọn nhiều)", true, new JScrollPane(lstDichVu)));
        than.add(Box.createVerticalStrut(12));
        than.add(o("Phụ tùng dự kiến thay thế", true, txtPhuTungDuKien));
        return the("Yêu cầu & Hạng mục dịch vụ", Loai.CLIPBOARD, than);
    }

    private JPanel taoThePhanCong() {
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(radThuong);
        nhom.add(radKhan);
        JPanel than = than();
        than.add(luoi(2, o("Trạng thái phiếu", true, cboTrangThai),
                o("Ưu tiên", true, luoi(2, khungChon(radThuong), khungChon(radKhan))),
                o("Dự kiến hoàn thành", false, txtDuKien)));
        return the("Phân công & Trạng thái", Loai.CLIPBOARD, than);
    }

    private JPanel taoThanhHanhDong() {
        lblLoi.setBorder(new EmptyBorder(0, 16, 0, 0));
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnHuy);
        nut.add(btnLuuVaChiDinh);
        nut.add(btnLuu);
        JPanel thanh = new JPanel(new BorderLayout());
        thanh.setBackground(UIConstants.CARD_BG);
        thanh.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(10, 8, 10, 16)));
        thanh.add(lblLoi, BorderLayout.CENTER);
        thanh.add(nut, BorderLayout.EAST);
        return thanh;
    }

    private static JPanel than() {
        JPanel than = UIComponents.trong(null);
        than.setLayout(new BoxLayout(than, BoxLayout.Y_AXIS));
        than.setBorder(new EmptyBorder(4, 20, 22, 20));
        return than;
    }

    private static JPanel the(String tieuDe, Loai loai, JPanel than) {
        RoundedPanel the = UIComponents.theMuc(tieuDe, loai, UIConstants.ROLE_RECEPTION);
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    static JPanel luoi(int soCot, JComponent... o) {
        JPanel luoi = UIComponents.trong(new GridLayout(0, soCot, 20, 12));
        luoi.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent c : o) {
            luoi.add(c);
        }
        return luoi;
    }

    static JPanel o(String nhan, boolean batBuoc, JComponent truong) {
        JPanel o = UIComponents.trong(new BorderLayout(0, 6));
        o.setAlignmentX(LEFT_ALIGNMENT);
        o.add(UIComponents.formLabel(nhan, batBuoc), BorderLayout.NORTH);
        o.add(truong, BorderLayout.CENTER);
        return o;
    }

    static JComponent vungChu(JTextArea vung) {
        vung.setLineWrap(true);
        vung.setWrapStyleWord(true);
        vung.setFont(UIConstants.FONT_BODY);
        return new JScrollPane(vung);
    }

    private static JPanel khungChon(JRadioButton nut) {
        nut.setOpaque(false);
        nut.setFont(UIConstants.FONT_BODY);
        RoundedPanel khung = new RoundedPanel(8, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
        khung.setLayout(new BorderLayout());
        khung.setBorder(new EmptyBorder(8, 12, 8, 12));
        khung.add(nut);
        return khung;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void hienLoi(String thongBao) {
        UIComponents.showError(lblLoi, thongBao);
    }

    /** Khóa nhóm khách hàng khi khách đã có trong hệ thống. */
    public void setChoSuaKhachHang(boolean choSua) {
        for (JTextField o : new JTextField[] {txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail, txtMaSoThue}) {
            o.setEditable(choSua);
        }
        radVangLai.setEnabled(choSua);
        radThanhVien.setEnabled(choSua);
        radVip.setEnabled(choSua);
    }

    /** Khóa nhóm phương tiện khi xe đã có trong hệ thống. */
    public void setChoSuaXe(boolean choSua) {
        for (JTextField o : new JTextField[] {txtBienSo, txtSoKhung, txtHangXe, txtDongXe, txtDoiXe, txtMauSon}) {
            o.setEditable(choSua);
        }
    }

    /** Khi sửa phiếu, trạng thái và dịch vụ được quản lý tại màn hình Chi tiết phiếu. */
    public void setCheDoSua(boolean sua) {
        btnLuu.setText(sua ? "Cập nhật phiếu" : "Lưu phiếu tiếp nhận");
        btnLuuVaChiDinh.setText(sua ? "Cập nhật & Chỉ định thợ" : "Lưu & Tiếp tục chỉ định thợ");
        lstDichVu.setEnabled(!sua);
        txtTraCuu.setEnabled(!sua);
        btnTraCuu.setEnabled(!sua);
    }

    public void xoaTrangForm() {
        for (JTextField o : new JTextField[] {txtTraCuu, txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail, txtMaSoThue, txtBienSo,
                txtSoKhung, txtHangXe, txtDongXe, txtDoiXe, txtMauSon, txtSoKm, txtNgoaiThat, txtNoiThat, txtTaiSan,
                txtPhuTungDuKien, txtDuKien}) {
            o.setText("");
        }
        txtGhiChu.setText("");
        txtYeuCau.setText("");
        lstDichVu.clearSelection();
        sldNhienLieu.setValue(50);
        radVangLai.setSelected(true);
        radThuong.setSelected(true);
        cboTrangThai.setSelectedIndex(0);
        setChoSuaKhachHang(true);
        setChoSuaXe(true);
        hienLoi(null);
    }

    public JTextField getTxtTraCuu() { return txtTraCuu; }
    public JButton getBtnTraCuu() { return btnTraCuu; }
    public JTextField getTxtHoTen() { return txtHoTen; }
    public JTextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public JTextField getTxtDiaChi() { return txtDiaChi; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtMaSoThue() { return txtMaSoThue; }
    public JTextField getTxtDuKien() { return txtDuKien; }
    public JRadioButton getRadVip() { return radVip; }

    public String getLoaiKhachHang() {
        return radVip.isSelected() ? radVip.getText() : radThanhVien.isSelected() ? radThanhVien.getText() : radVangLai.getText();
    }

    public void setLoaiKhachHang(String loai) {
        radVip.setSelected(radVip.getText().equals(loai));
        radThanhVien.setSelected(radThanhVien.getText().equals(loai));
        radVangLai.setSelected(!radVip.isSelected() && !radThanhVien.isSelected());
    }
    public JTextField getTxtBienSo() { return txtBienSo; }
    public JTextField getTxtSoKhung() { return txtSoKhung; }
    public JTextField getTxtHangXe() { return txtHangXe; }
    public JTextField getTxtDongXe() { return txtDongXe; }
    public JTextField getTxtDoiXe() { return txtDoiXe; }
    public JTextField getTxtMauSon() { return txtMauSon; }
    public JTextField getTxtSoKm() { return txtSoKm; }
    public JSlider getSldNhienLieu() { return sldNhienLieu; }
    public JTextField getTxtNgoaiThat() { return txtNgoaiThat; }
    public JTextField getTxtNoiThat() { return txtNoiThat; }
    public JTextField getTxtTaiSan() { return txtTaiSan; }
    public JTextArea getTxtGhiChu() { return txtGhiChu; }
    public JTextArea getTxtYeuCau() { return txtYeuCau; }
    public JList<DichVu> getLstDichVu() { return lstDichVu; }
    public JTextField getTxtPhuTungDuKien() { return txtPhuTungDuKien; }
    public JComboBox<String> getCboTrangThai() { return cboTrangThai; }
    public JRadioButton getRadThuong() { return radThuong; }
    public JRadioButton getRadKhan() { return radKhan; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnLuuVaChiDinh() { return btnLuuVaChiDinh; }
    public JButton getBtnLuu() { return btnLuu; }
    public QLTiepNhanSuaChuaController getController() { return controller; }
}
