package gui;

import controller.ChiTietPhieuSuaChuaController;
import controller.DieuHuongPhieu;
import entity.DichVu;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Chi tiết phiếu & chỉ định thợ (JPanel trong khung chính) theo Figma. Phụ trách: Nguyễn Xuân Việt Anh. */
public class ChiTietPhieuSuaChua extends JPanel {
    public static final String CHO_PHAN_CONG = "Chờ phân công";
    public static final int COT_THO = 2;
    public static final int COT_TIEN_CONG = 4;

    private final JLabel lblMaPhieu = UIComponents.label("", UIConstants.FONT_PAGE_TITLE.deriveFont(30f), UIConstants.TEXT_DARK);
    private final ThanhTienTrinh thanhTienTrinh = new ThanhTienTrinh(2, "Tiếp nhận", "Chỉ định", "Xuất kho", "Thanh toán");
    private final JLabel lblKhoa = UIComponents.errorLabel();

    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtXe = new JTextField();
    private final JTextField txtKhachHang = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtSoKm = new JTextField();
    private final JTextField txtPhuTungDuKien = new JTextField();
    // Dùng Object vì mục đầu là chuỗi CHO_PHAN_CONG, các mục sau là NhanVien.
    private final JComboBox<Object> cboKtvPhuTrach = new JComboBox<>();
    private final JComboBox<TrangThaiPhieu> cboTrangThai = new JComboBox<>(new TrangThaiPhieu[] {
        TrangThaiPhieu.CHO_KIEM_TRA, TrangThaiPhieu.DANG_SUA, TrangThaiPhieu.CHO_PHU_TUNG, TrangThaiPhieu.HOAN_THANH});
    private final JTextArea txtGhiChuKyThuat = new JTextArea(3, 20);

    private boolean khoa;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] {"STT", "Hạng mục dịch vụ", "Thợ phụ trách", "Trạng thái", "Tiền công (đ)"}, 0) {
        @Override
        public boolean isCellEditable(int hang, int cot) {
            return !khoa && (cot == COT_THO || cot == COT_TIEN_CONG);
        }
    };
    private final JTable tblChiTiet = new JTable(tableModel);
    private final JComboBox<Object> cboThoTrongBang = new JComboBox<>();
    private final JComboBox<DichVu> cboDichVu = new JComboBox<>();
    private final JButton btnThemDichVu = UIComponents.nutChinh("+  Thêm dịch vụ", UIConstants.ROLE_RECEPTION);
    private final JButton btnXoaDichVu = UIComponents.nutPhu("Xóa dòng");
    private final JLabel lblTongTienCong = UIComponents.label("", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK);

    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnQuayVe = UIComponents.nutPhu("Về danh sách phiếu");
    private final JButton btnHuyPhieu = UIComponents.nutPhu("Hủy phiếu");
    private final JButton btnInPhieu = UIComponents.nutPhu("In phiếu");
    private final JButton btnTuDongPhanCong = UIComponents.nutPhu("Tự động phân công");
    private final JButton btnLuuPhanCong = UIComponents.nutPhu("Lưu phân công");
    private final JButton btnCapNhatTienDo = UIComponents.nutChinh("Cập nhật tiến độ", UIConstants.PRIMARY);

    public ChiTietPhieuSuaChua() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(taoTheDau(), 0);
        noiDung.them(lblKhoa, 8);
        noiDung.them(taoTheThongTin(), 12);
        noiDung.them(taoTheDichVu(), 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        add(taoThanhHanhDong(), BorderLayout.SOUTH);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel(String maPhieu, DieuHuongPhieu dieuHuong) {
        ChiTietPhieuSuaChua panel = new ChiTietPhieuSuaChua();
        panel.lblMaPhieu.setText(maPhieu);
        new ChiTietPhieuSuaChuaController(panel, maPhieu, dieuHuong);
        return panel;
    }

    private JPanel taoTheDau() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 12));
        the.setBorder(new EmptyBorder(16, 24, 16, 24));
        JPanel dau = UIComponents.trong(new GridLayout(2, 1));
        dau.add(UIComponents.label("Phiếu sửa chữa", UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK));
        dau.add(lblMaPhieu);
        the.add(dau, BorderLayout.NORTH);

        RoundedPanel khung = new RoundedPanel(8, UIConstants.CARD_BG, UIConstants.BORDER_COLOR);
        khung.setLayout(new BorderLayout(0, 4));
        khung.setBorder(new EmptyBorder(8, 12, 10, 24));
        khung.add(UIComponents.label("Tiến trình phiếu", UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK), BorderLayout.NORTH);
        khung.add(thanhTienTrinh, BorderLayout.CENTER);
        the.add(khung, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheThongTin() {
        for (JTextField o : new JTextField[] {txtBienSo, txtXe, txtKhachHang, txtSoDienThoai, txtSoKm, txtPhuTungDuKien}) {
            UIComponents.styleInput(o);
            o.setEditable(false);
        }
        UIComponents.styleCombo(cboKtvPhuTrach);
        UIComponents.styleCombo(cboTrangThai);
        JPanel than = UIComponents.trong(null);
        than.setLayout(new BoxLayout(than, BoxLayout.Y_AXIS));
        than.setBorder(new EmptyBorder(4, 20, 22, 20));
        than.add(QLTiepNhanSuaChua.luoi(2,
                QLTiepNhanSuaChua.o("Biển số", false, txtBienSo), QLTiepNhanSuaChua.o("Xe", false, txtXe),
                QLTiepNhanSuaChua.o("Khách hàng", false, txtKhachHang), QLTiepNhanSuaChua.o("Số điện thoại", false, txtSoDienThoai),
                QLTiepNhanSuaChua.o("Số km", false, txtSoKm), QLTiepNhanSuaChua.o("Phụ tùng dự kiến", false, txtPhuTungDuKien),
                QLTiepNhanSuaChua.o("KTV phụ trách", false, cboKtvPhuTrach), QLTiepNhanSuaChua.o("Trạng thái phiếu", false, cboTrangThai)));
        than.add(Box.createVerticalStrut(12));
        than.add(QLTiepNhanSuaChua.o("Ghi chú kỹ thuật", false, QLTiepNhanSuaChua.vungChu(txtGhiChuKyThuat)));
        RoundedPanel the = UIComponents.theMuc("Thông tin phiếu", Loai.CLIPBOARD, UIConstants.ROLE_RECEPTION);
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheDichVu() {
        UIComponents.styleTable(tblChiTiet);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblChiTiet.getColumnModel().getColumn(COT_THO).setCellEditor(new DefaultCellEditor(cboThoTrongBang));
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(new BadgeRenderer(ChiTietPhieuSuaChua::mauTienDo));
        UIComponents.datDoRong(tblChiTiet, 50, 340, 230, 110, 130);
        DefaultTableCellRenderer veTien = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon, boolean focus, int h, int c) {
                super.getTableCellRendererComponent(t, v, chon, focus, h, c);
                setBorder(new EmptyBorder(0, 0, 0, 16));
                return this;
            }
        };
        veTien.setHorizontalAlignment(SwingConstants.RIGHT);
        tblChiTiet.getColumnModel().getColumn(COT_TIEN_CONG).setCellRenderer(veTien);
        UIComponents.canPhaiTieuDe(tblChiTiet, COT_TIEN_CONG);

        JPanel bangVaTong = UIComponents.trong(new BorderLayout(0, 8));
        bangVaTong.add(UIComponents.cuonBang(tblChiTiet), BorderLayout.CENTER);
        bangVaTong.add(lblTongTienCong, BorderLayout.SOUTH);

        UIComponents.styleCombo(cboDichVu);
        cboDichVu.setPreferredSize(new Dimension(320, 40));
        JPanel themDong = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 8, 0));
        themDong.add(cboDichVu);
        themDong.add(btnThemDichVu);
        themDong.add(btnXoaDichVu);

        JPanel than = UIComponents.trong(new BorderLayout(0, 12));
        than.setBorder(new EmptyBorder(4, 20, 22, 20));
        than.add(themDong, BorderLayout.NORTH);
        than.add(bangVaTong, BorderLayout.CENTER);
        RoundedPanel the = UIComponents.theMuc("Hạng mục & Chỉ định thợ", Loai.WRENCH, UIConstants.ROLE_RECEPTION);
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoThanhHanhDong() {
        lblLoi.setBorder(new EmptyBorder(0, 16, 0, 0));
        btnHuyPhieu.setForeground(UIConstants.ROLE_RECEPTION);
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnQuayVe);
        nut.add(btnHuyPhieu);
        nut.add(btnInPhieu);
        nut.add(btnTuDongPhanCong);
        nut.add(btnLuuPhanCong);
        nut.add(btnCapNhatTienDo);
        JPanel thanh = new JPanel(new BorderLayout());
        thanh.setBackground(UIConstants.CARD_BG);
        thanh.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(10, 8, 10, 16)));
        thanh.add(lblLoi, BorderLayout.CENTER);
        thanh.add(nut, BorderLayout.EAST);
        return thanh;
    }

    private static Color mauTienDo(String tienDo) {
        if (TrangThaiTienDo.HOAN_THANH.getText().equals(tienDo)) {
            return UIConstants.STATUS_SUCCESS;
        }
        return TrangThaiTienDo.DANG_SUA.getText().equals(tienDo) ? UIConstants.PRIMARY_LIGHT : UIConstants.STATUS_WARNING;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    /** Khóa toàn bộ thao tác ghi khi phiếu đã có hóa đơn. */
    public void setKhoa(boolean khoa) {
        this.khoa = khoa;
        UIComponents.showError(lblKhoa, khoa ? "Phiếu đã có hóa đơn hoặc đã hủy nên chỉ được xem, không được sửa." : null);
        for (JComponent c : new JComponent[] {cboKtvPhuTrach, cboTrangThai, cboDichVu, btnThemDichVu, btnXoaDichVu,
                btnHuyPhieu, btnTuDongPhanCong, btnLuuPhanCong, btnCapNhatTienDo}) {
            c.setEnabled(!khoa);
        }
        txtGhiChuKyThuat.setEditable(!khoa);
    }

    public void hienLoi(String thongBao) {
        UIComponents.showError(lblLoi, thongBao);
    }

    public void setTongTienCong(String tongTien) {
        lblTongTienCong.setText("Tổng tiền công: " + tongTien);
    }

    public void canChieuCaoBang() {
        UIComponents.canChieuCaoBang(tblChiTiet, 3, 8);
        revalidate();
    }

    public ThanhTienTrinh getThanhTienTrinh() { return thanhTienTrinh; }
    public JTextField getTxtBienSo() { return txtBienSo; }
    public JTextField getTxtXe() { return txtXe; }
    public JTextField getTxtKhachHang() { return txtKhachHang; }
    public JTextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public JTextField getTxtSoKm() { return txtSoKm; }
    public JTextField getTxtPhuTungDuKien() { return txtPhuTungDuKien; }
    public JComboBox<Object> getCboKtvPhuTrach() { return cboKtvPhuTrach; }
    public JComboBox<TrangThaiPhieu> getCboTrangThai() { return cboTrangThai; }
    public JTextArea getTxtGhiChuKyThuat() { return txtGhiChuKyThuat; }
    public JTable getTblChiTiet() { return tblChiTiet; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JComboBox<Object> getCboThoTrongBang() { return cboThoTrongBang; }
    public JComboBox<DichVu> getCboDichVu() { return cboDichVu; }
    public JButton getBtnThemDichVu() { return btnThemDichVu; }
    public JButton getBtnXoaDichVu() { return btnXoaDichVu; }
    public JButton getBtnQuayVe() { return btnQuayVe; }
    public JButton getBtnHuyPhieu() { return btnHuyPhieu; }
    public JButton getBtnInPhieu() { return btnInPhieu; }
    public JButton getBtnTuDongPhanCong() { return btnTuDongPhanCong; }
    public JButton getBtnLuuPhanCong() { return btnLuuPhanCong; }
    public JButton getBtnCapNhatTienDo() { return btnCapNhatTienDo; }
}
