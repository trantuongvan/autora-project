package gui;

import controller.DieuHuongHoaDon;
import controller.QLHoaDonController;
import entity.HoaDon;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Danh sách hóa đơn: tìm kiếm, lọc, xem chi tiết, in và hủy. Phụ trách: Trần Thị Tường Vân. */
public class QLHoaDon extends JPanel {
    public static final String TAT_CA = "Tất cả";
    public static final int COT_TRANG_THAI = 11;
    public static final int COT_THAO_TAC = 12;
    public static final int ICON_CHI_TIET = 0;
    public static final int ICON_IN = 1;
    public static final int ICON_HUY = 2;

    private final JTextField txtTimKiem = new PlaceholderField("Mã HĐ / Mã phiếu / SĐT / Biển số");
    private final JComboBox<String> cboTrangThai = new JComboBox<>();
    private final JComboBox<String> cboHinhThuc = new JComboBox<>();
    private final JTextField txtTuNgay = new PlaceholderField("dd/MM/yyyy");
    private final JTextField txtDenNgay = new PlaceholderField("dd/MM/yyyy");
    private final JButton btnTim = UIComponents.nutChinh("Tìm", UIConstants.PRIMARY);
    private final JButton btnLapHoaDon = UIComponents.nutChinh("+ Lập hóa đơn", UIConstants.PRIMARY);
    private final JLabel lblLoiLoc = UIComponents.errorLabel();
    private final JLabel lblSoDong = UIComponents.label(" ", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Mã HĐ", "Mã phiếu", "Khách hàng", "Biển số", "Ngày lập", "Tiền công", "Tiền phụ tùng",
            "Giảm giá", "VAT", "Tổng thanh toán", "Hình thức", "Trạng thái", "Thao tác");
    // 13 cột không vừa khi có sidebar: giữ độ rộng và cuộn ngang, chỉ giãn khi cửa sổ đủ rộng.
    private final JTable tblDanhSach = new JTable(tableModel) {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getPreferredSize().width < getParent().getWidth();
        }
    };
    private QLHoaDonController controller;

    public QLHoaDon() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Quản lý hóa đơn", "F5 làm mới • Ctrl+F tìm kiếm",
                UIConstants.ROLE_RECEPTION, btnLapHoaDon), 0);

        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout(0, 12));
        the.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel dau = UIComponents.trong(new BorderLayout(0, 4));
        dau.add(taoHangLoc(), BorderLayout.CENTER);
        dau.add(lblLoiLoc, BorderLayout.SOUTH);
        the.add(dau, BorderLayout.NORTH);

        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL, null));
        tblDanhSach.getColumnModel().getColumn(9)
                .setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL, null));
        tblDanhSach.getColumnModel().getColumn(COT_TRANG_THAI).setCellRenderer(new BadgeRenderer(QLHoaDon::mauTrangThai));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(
                new LineIcon(Loai.EYE, UIConstants.PRIMARY),
                new LineIcon(Loai.PRINT, UIConstants.PRIMARY),
                new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT)));
        tblDanhSach.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UIComponents.datDoRong(tblDanhSach, 100, 110, 170, 105, 95, 90, 95, 85, 85, 115, 115, 150, 125);
        the.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        the.add(lblSoDong, BorderLayout.SOUTH);
        noiDung.them(the, 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    private JPanel taoHangLoc() {
        UIComponents.styleInput(txtTimKiem);
        UIComponents.styleInput(txtTuNgay);
        UIComponents.styleInput(txtDenNgay);
        cboTrangThai.addItem(TAT_CA);
        for (String s : HoaDon.TRANG_THAI) {
            cboTrangThai.addItem(s);
        }
        cboHinhThuc.addItem(TAT_CA);
        for (String s : HoaDon.HINH_THUC) {
            cboHinhThuc.addItem(s);
        }
        UIComponents.styleCombo(cboTrangThai);
        UIComponents.styleCombo(cboHinhThuc);

        JPanel hang = UIComponents.trong(new GridBagLayout());
        JComponent[] o = {
            QLTiepNhanSuaChua.o("Tìm kiếm", false, txtTimKiem),
            QLTiepNhanSuaChua.o("Trạng thái", false, cboTrangThai),
            QLTiepNhanSuaChua.o("Hình thức", false, cboHinhThuc),
            QLTiepNhanSuaChua.o("Từ ngày", false, txtTuNgay),
            QLTiepNhanSuaChua.o("Đến ngày", false, txtDenNgay),
            QLTiepNhanSuaChua.o(" ", false, btnTim)
        };
        double[] rong = {0.34, 0.17, 0.17, 0.16, 0.16, 0};
        for (int i = 0; i < o.length; i++) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = i;
            gc.weightx = rong[i];
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(0, 0, 0, i < o.length - 1 ? 12 : 0);
            hang.add(o[i], gc);
        }
        return hang;
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel(DieuHuongHoaDon dieuHuong) {
        QLHoaDon panel = new QLHoaDon();
        panel.controller = new QLHoaDonController(panel, dieuHuong);
        return panel;
    }

    static Color mauTrangThai(String trangThai) {
        switch (trangThai) {
            case HoaDon.DA_THANH_TOAN:
                return UIConstants.STATUS_SUCCESS;
            case HoaDon.CONG_NO:
                return UIConstants.STATUS_WARNING;
            default:
                return UIConstants.TEXT_MUTED;
        }
    }

    /** Hiện toàn bộ dòng khớp, bảng tự cuộn khi quá 14 dòng. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) {
            tableModel.addRow(r);
        }
        lblSoDong.setText("Hiển thị " + dong.size() + " hóa đơn");
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 14);
        // Chừa chỗ cho thanh cuộn ngang để không che dòng cuối.
        JScrollPane cuon = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tblDanhSach);
        Dimension co = cuon.getPreferredSize();
        cuon.setPreferredSize(new Dimension(co.width, co.height + 20));
        revalidate();
    }

    public void hienLoiLoc(String thongBao) {
        UIComponents.showError(lblLoiLoc, thongBao);
    }

    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<String> getCboTrangThai() { return cboTrangThai; }
    public JComboBox<String> getCboHinhThuc() { return cboHinhThuc; }
    public JTextField getTxtTuNgay() { return txtTuNgay; }
    public JTextField getTxtDenNgay() { return txtDenNgay; }
    public JButton getBtnTim() { return btnTim; }
    public JButton getBtnLapHoaDon() { return btnLapHoaDon; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public QLHoaDonController getController() { return controller; }
}
