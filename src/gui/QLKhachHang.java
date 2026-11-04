package gui;

import controller.QLKhachHangController;
import entity.LoaiKhachHang;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Quản lý khách hàng (UC007): danh sách, form, xe liên kết và lịch sử sửa chữa. Phụ trách: Trần Thị Tường Vân. */
public class QLKhachHang extends JPanel {
    public static final String TAT_CA = "Tất cả";
    public static final String DUOC_PHEP = "Được phép";
    public static final String KHONG = "Không";
    public static final int COT_LOAI = 5;
    public static final int COT_GHI_NO = 6;
    public static final int COT_THAO_TAC = 8;
    public static final int ICON_XOA = 1;

    private final JTextField txtTimKiem;
    private final JComboBox<Object> cboLocLoai = new JComboBox<>();
    private final JComboBox<String> cboLocGhiNo = new JComboBox<>(new String[] {TAT_CA, DUOC_PHEP, KHONG});
    private final JButton btnTim = UIComponents.nutChinh("Tìm", UIConstants.PRIMARY);
    private final JButton btnThem = UIComponents.nutChinh("+  Thêm khách hàng", UIConstants.PRIMARY);
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã KH", "Họ tên", "SĐT", "Địa chỉ", "Email / MST", "Loại KH", "Ghi nợ", "Xe", "Thao tác");
    private final JTable tblDanhSach = new JTable(tableModel);

    private final JLabel lblTieuDeForm = UIComponents.label("", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK);
    private final JTextField txtMaKH = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtDiaChi = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtMaSoThue = new JTextField();
    private final JComboBox<LoaiKhachHang> cboLoai = new JComboBox<>(LoaiKhachHang.values());
    private final JComboBox<String> cboGhiNo = new JComboBox<>(new String[] {KHONG, DUOC_PHEP});
    private final JLabel lblLoiHoTen = UIComponents.errorLabel();
    private final JLabel lblLoiSoDienThoai = UIComponents.errorLabel();
    private final JLabel lblLoiDiaChi = UIComponents.errorLabel();
    private final JLabel lblLoiEmail = UIComponents.errorLabel();
    private final JLabel lblLoiMaSoThue = UIComponents.errorLabel();
    private final JLabel lblLoiChung = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu", UIConstants.PRIMARY);

    private final DefaultTableModel xeModel = UIComponents.modelKhongSua(
            "Biển số", "Số VIN", "Hãng xe", "Dòng xe", "Đời xe", "Màu sơn");
    private final JTable tblXe = new JTable(xeModel);
    private final DefaultTableModel lichSuModel = UIComponents.modelKhongSua("Mã phiếu", "Ngày", "Hạng mục", "Tổng tiền");
    private final JTable tblLichSu = new JTable(lichSuModel);

    public QLKhachHang() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        IconField timKiem = new IconField("Tên / SĐT / Biển số", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        txtTimKiem = timKiem.getTextField();

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Quản lý khách hàng", "F5 làm mới • Ctrl+F tìm kiếm • Bấm vào dòng để hiện lên biểu mẫu",
                UIConstants.ROLE_RECEPTION, btnThem), 0);
        noiDung.them(taoTheDanhSach(timKiem), 18);
        noiDung.them(taoTheForm(), 16);
        noiDung.them(taoTheLienKet(), 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLKhachHang panel = new QLKhachHang();
        new QLKhachHangController(panel);
        return panel;
    }

    private JPanel taoTheDanhSach(IconField timKiem) {
        cboLocLoai.addItem(TAT_CA);
        for (LoaiKhachHang loai : LoaiKhachHang.values()) {
            cboLocLoai.addItem(loai);
        }
        UIComponents.styleCombo(cboLocLoai);
        UIComponents.styleCombo(cboLocGhiNo);

        JPanel hangLoc = UIComponents.trong(new GridBagLayout());
        hangLoc.add(QLTiepNhanSuaChua.o("Tìm kiếm", false, timKiem), UIComponents.gbc(0, 0, 1, 0.5, 0, 12));
        hangLoc.add(QLTiepNhanSuaChua.o("Loại khách hàng", false, cboLocLoai), UIComponents.gbc(1, 0, 1, 0.25, 0, 12));
        hangLoc.add(QLTiepNhanSuaChua.o("Được ghi nợ", false, cboLocGhiNo), UIComponents.gbc(2, 0, 1, 0.25, 0, 12));
        GridBagConstraints gcNut = UIComponents.gbc(3, 0, 1, 0, 0, 0);
        gcNut.anchor = GridBagConstraints.SOUTH;
        gcNut.fill = GridBagConstraints.NONE;
        hangLoc.add(btnTim, gcNut);

        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(COT_LOAI).setCellRenderer(new BadgeRenderer(QLKhachHang::mauLoai));
        tblDanhSach.getColumnModel().getColumn(COT_GHI_NO).setCellRenderer(new BadgeRenderer(QLKhachHang::mauGhiNo));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(
                new LineIcon(Loai.PENCIL, UIConstants.ROLE_RECEPTION),
                new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT)));
        UIComponents.datDoRong(tblDanhSach, 75, 140, 115, 120, 150, 135, 110, 110, 112);
        // Dưới mức này ActionRenderer xuống dòng làm mất icon.
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setMinWidth(112);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 12));
        the.setBorder(new EmptyBorder(18, 20, 18, 20));
        the.add(hangLoc, BorderLayout.NORTH);
        the.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheForm() {
        txtMaKH.setEditable(false);
        for (JTextField o : new JTextField[] {txtMaKH, txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail, txtMaSoThue}) {
            UIComponents.styleInput(o);
        }
        txtMaKH.setBackground(UIComponents.tint(UIConstants.APP_BG, 0.5f));
        UIComponents.styleCombo(cboLoai);
        UIComponents.styleCombo(cboGhiNo);

        JPanel luoi = QLTiepNhanSuaChua.luoi(3,
                oCoLoi("Mã KH", false, txtMaKH, UIComponents.errorLabel()),
                oCoLoi("Họ tên", true, txtHoTen, lblLoiHoTen),
                oCoLoi("SĐT", true, txtSoDienThoai, lblLoiSoDienThoai),
                oCoLoi("Địa chỉ", true, txtDiaChi, lblLoiDiaChi),
                oCoLoi("Email", false, txtEmail, lblLoiEmail),
                oCoLoi("Mã số thuế", false, txtMaSoThue, lblLoiMaSoThue),
                oCoLoi("Loại khách hàng", false, cboLoai, UIComponents.errorLabel()),
                oCoLoi("Được ghi nợ", false, cboGhiNo, UIComponents.errorLabel()));

        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnHuy);
        nut.add(btnLuu);
        JPanel chan = UIComponents.trong(new BorderLayout());
        chan.add(lblLoiChung, BorderLayout.CENTER);
        chan.add(nut, BorderLayout.EAST);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 14));
        the.setBorder(new EmptyBorder(18, 20, 18, 20));
        the.add(lblTieuDeForm, BorderLayout.NORTH);
        the.add(luoi, BorderLayout.CENTER);
        the.add(chan, BorderLayout.SOUTH);
        return the;
    }

    /** Ô đặt ở NORTH để khi dòng lỗi hiện ra, các ô cùng hàng trong lưới không bị kéo cao. */
    private static JPanel oCoLoi(String nhan, boolean batBuoc, JComponent truong, JLabel lblLoi) {
        JPanel khoi = UIComponents.trong(new BorderLayout(0, 4));
        khoi.add(truong, BorderLayout.NORTH);
        khoi.add(lblLoi, BorderLayout.CENTER);
        return QLTiepNhanSuaChua.o(nhan, batBuoc, khoi);
    }

    private JPanel taoTheLienKet() {
        UIComponents.styleTable(tblXe);
        UIComponents.styleTable(tblLichSu);
        UIComponents.datDoRong(tblXe, 120, 200, 120, 120, 80, 130);
        tblLichSu.setRowHeight(58);
        UIComponents.datDoRong(tblLichSu, 120, 100, 500, 120);

        CotDoc than = new CotDoc();
        than.them(UIComponents.label("Xe liên kết", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), 0);
        than.them(UIComponents.label("Chỉ xem. Thêm và sửa xe tại màn hình Hồ sơ xe.",
                UIConstants.FONT_CAPTION, UIConstants.TEXT_MUTED), 2);
        than.them(UIComponents.cuonBang(tblXe), 8);
        than.them(UIComponents.label("Lịch sử sửa chữa", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), 16);
        than.them(UIComponents.cuonBang(tblLichSu), 8);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.setBorder(new EmptyBorder(18, 20, 18, 20));
        the.add(than, BorderLayout.CENTER);
        UIComponents.canChieuCaoBang(tblXe, 2, 5);
        UIComponents.canChieuCaoBang(tblLichSu, 2, 8);
        return the;
    }

    private static Color mauLoai(String loai) {
        if (LoaiKhachHang.VIP.getText().equals(loai)) {
            return UIConstants.ROLE_TECH_STOCK_LIGHT;
        }
        if (LoaiKhachHang.THANH_VIEN.getText().equals(loai)) {
            return UIConstants.PRIMARY_LIGHT;
        }
        return UIConstants.TEXT_MUTED;
    }

    private static Color mauGhiNo(String ghiNo) {
        return DUOC_PHEP.equals(ghiNo) ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setDanhSach(List<Object[]> dong) {
        datDong(tableModel, dong);
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 8);
        revalidate();
    }

    public void setXeLienKet(List<Object[]> dong) {
        datDong(xeModel, dong);
        UIComponents.canChieuCaoBang(tblXe, 2, 5);
        revalidate();
    }

    public void setLichSu(List<Object[]> dong) {
        datDong(lichSuModel, dong);
        UIComponents.canChieuCaoBang(tblLichSu, 2, 8);
        revalidate();
    }

    private static void datDong(DefaultTableModel model, List<Object[]> dong) {
        model.setRowCount(0);
        for (Object[] r : dong) {
            model.addRow(r);
        }
    }

    public void xoaLoi() {
        for (JComponent o : new JComponent[] {txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail, txtMaSoThue}) {
            UIComponents.setError(o, false);
        }
        for (JLabel l : new JLabel[] {lblLoiHoTen, lblLoiSoDienThoai, lblLoiDiaChi, lblLoiEmail, lblLoiMaSoThue, lblLoiChung}) {
            UIComponents.showError(l, null);
        }
    }

    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<Object> getCboLocLoai() { return cboLocLoai; }
    public JComboBox<String> getCboLocGhiNo() { return cboLocGhiNo; }
    public JButton getBtnTim() { return btnTim; }
    public JButton getBtnThem() { return btnThem; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public JLabel getLblTieuDeForm() { return lblTieuDeForm; }
    public JTextField getTxtMaKH() { return txtMaKH; }
    public JTextField getTxtHoTen() { return txtHoTen; }
    public JTextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public JTextField getTxtDiaChi() { return txtDiaChi; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtMaSoThue() { return txtMaSoThue; }
    public JComboBox<LoaiKhachHang> getCboLoai() { return cboLoai; }
    public JComboBox<String> getCboGhiNo() { return cboGhiNo; }
    public JLabel getLblLoiHoTen() { return lblLoiHoTen; }
    public JLabel getLblLoiSoDienThoai() { return lblLoiSoDienThoai; }
    public JLabel getLblLoiDiaChi() { return lblLoiDiaChi; }
    public JLabel getLblLoiEmail() { return lblLoiEmail; }
    public JLabel getLblLoiMaSoThue() { return lblLoiMaSoThue; }
    public JLabel getLblLoiChung() { return lblLoiChung; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnLuu() { return btnLuu; }
}
