package gui;

import controller.DieuHuongPhieu;
import controller.QLPhieuSuaChuaController;
import entity.TrangThaiPhieu;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Danh sách phiếu sửa chữa: tìm kiếm, lọc, mở chi tiết, sửa và xóa. Phụ trách: Nguyễn Xuân Việt Anh. */
public class QLPhieuSuaChua extends JPanel {
    public static final String TAT_CA_TRANG_THAI = "Tất cả trạng thái";
    public static final int COT_THAO_TAC = 7;
    public static final int ICON_SUA = 0;
    public static final int ICON_XOA = 1;

    private final JTextField txtTimKiem;
    private final JComboBox<Object> cboLocTrangThai = new JComboBox<>();
    private final JTextField txtTuNgay = new PlaceholderField("Từ ngày");
    private final JTextField txtDenNgay = new PlaceholderField("Đến ngày");
    private final JButton btnLoc = UIComponents.nutTaiDuLieu();
    private final JButton btnLamMoi = UIComponents.nutPhu("Xóa bộ lọc");
    private final JButton btnTaoPhieu = UIComponents.nutChinh("+  Tiếp nhận xe mới", UIConstants.PRIMARY);
    private final JLabel lblLoiLoc = UIComponents.errorLabel();
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã phiếu", "Biển số", "Khách hàng", "Xe", "Tiếp nhận", "Ưu tiên", "Trạng thái", "Thao tác");
    private final JTable tblDanhSach = new JTable(tableModel);
    private QLPhieuSuaChuaController controller;

    public QLPhieuSuaChua() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Phiếu sửa chữa",
                "F5 làm mới • Ctrl+F tìm kiếm • Bấm đúp vào dòng để xem chi tiết • Delete hủy phiếu đang chọn", UIConstants.ROLE_RECEPTION, btnTaoPhieu), 0);

        IconField timKiem = new IconField("Tìm mã phiếu, biển số, khách hàng hoặc SĐT",
                new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(380, 40));
        txtTimKiem = timKiem.getTextField();
        cboLocTrangThai.addItem(TAT_CA_TRANG_THAI);
        for (TrangThaiPhieu trangThai : TrangThaiPhieu.values()) {
            cboLocTrangThai.addItem(trangThai);
        }
        UIComponents.styleCombo(cboLocTrangThai);
        cboLocTrangThai.setPreferredSize(new Dimension(170, 40));
        UIComponents.styleInput(txtTuNgay);
        UIComponents.styleInput(txtDenNgay);
        util.ChonNgay.ganNgay(txtTuNgay, txtDenNgay);
        txtTuNgay.setPreferredSize(new Dimension(130, 40));
        txtDenNgay.setPreferredSize(new Dimension(130, 40));
        JPanel hangLoc = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangLoc.add(timKiem);
        hangLoc.add(cboLocTrangThai);
        hangLoc.add(txtTuNgay);
        hangLoc.add(txtDenNgay);
        hangLoc.add(btnLoc);
        hangLoc.add(btnLamMoi);
        noiDung.them(hangLoc, 18);
        noiDung.them(lblLoiLoc, 4);

        RoundedPanel theBang = UIComponents.card();
        theBang.setLayout(new BorderLayout());
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer(QLPhieuSuaChua::mauTrangThai));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(
                new LineIcon(Loai.PENCIL, UIConstants.ROLE_RECEPTION),
                new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT)));
        UIComponents.datDoRong(tblDanhSach, 105, 95, 170, 125, 150, 80, 125, 110);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        noiDung.them(theBang, 12);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel(DieuHuongPhieu dieuHuong) {
        QLPhieuSuaChua panel = new QLPhieuSuaChua();
        panel.controller = new QLPhieuSuaChuaController(panel, dieuHuong);
        return panel;
    }

    static Color mauTrangThai(String trangThai) {
        if (TrangThaiPhieu.CHO_KIEM_TRA.getText().equals(trangThai)) {
            return UIConstants.STATUS_WARNING;
        }
        if (TrangThaiPhieu.DANG_SUA.getText().equals(trangThai)) {
            return UIConstants.PRIMARY_LIGHT;
        }
        if (TrangThaiPhieu.CHO_PHU_TUNG.getText().equals(trangThai)) {
            return UIConstants.ROLE_TECH_STOCK_LIGHT;
        }
        if (TrangThaiPhieu.HOAN_THANH.getText().equals(trangThai)) {
            return UIConstants.STATUS_SUCCESS;
        }
        if (TrangThaiPhieu.DA_HUY.getText().equals(trangThai)) {
            return UIConstants.TEXT_MUTED;
        }
        return null;
    }

    /** Mỗi dòng: {maPhieu, bienSo, khachHang, xe, tiepNhan, uuTien, trangThai, ""}. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) {
            tableModel.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 12);
        revalidate();
    }

    public void hienLoiLoc(String thongBao) {
        UIComponents.showError(lblLoiLoc, thongBao);
    }

    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<Object> getCboLocTrangThai() { return cboLocTrangThai; }
    public JTextField getTxtTuNgay() { return txtTuNgay; }
    public JTextField getTxtDenNgay() { return txtDenNgay; }
    public JButton getBtnLoc() { return btnLoc; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JButton getBtnTaoPhieu() { return btnTaoPhieu; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public QLPhieuSuaChuaController getController() { return controller; }
}
