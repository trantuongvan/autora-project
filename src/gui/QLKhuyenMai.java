package gui;

import entity.HinhThucKhuyenMai;
import util.UIConstants;
import util.UIComponents;
import util.UIComponents.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Màn hình Quản lý khuyến mãi - phân hệ Chủ cửa hàng (màu ROLE_MANAGER).
 */
public class QLKhuyenMai extends JPanel {

    public static final int COT_THAO_TAC = 7;
    public static final int ICON_SUA = 0;
    public static final int ICON_KICH_HOAT = 1;
    public static final int ICON_KET_THUC = 2;
    public static final int ICON_XOA = 3;

    private final JButton btnTaoMoi = UIComponents.nutChinh("+  Tạo chương trình mới", UIConstants.PRIMARY);
    private final JComboBox<String> cboLocTinhTrang =
            new JComboBox<>(new String[]{"Tất cả", "Đang áp dụng", "Sắp diễn ra", "Hết hạn", "Ngừng áp dụng"});
    private final JComboBox<Object> cboLocHinhThuc =
            new JComboBox<>(new Object[]{"Tất cả", HinhThucKhuyenMai.PHAN_TRAM, HinhThucKhuyenMai.TIEN_MAT});
    private final JButton btnXuatExcel = UIComponents.nutPhu("Xuất file Excel");
    private final RoundedPanel theBang = UIComponents.card();

    private final JLabel lblTongSo = taoSoLon();
    private final JLabel lblDangDienRa = taoSoLon();
    private final JLabel lblSapHetHan = taoSoLon();

    private final JTextField txtTimKiem;
    private final JTable tblDanhSach;
    private final DefaultTableModel tableModel;

    public QLKhuyenMai() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        IconField oTimKiem = new IconField("Tìm theo mã hoặc tên khuyến mãi",
                new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        txtTimKiem = oTimKiem.getTextField();
        UIComponents.styleCombo(cboLocTinhTrang);
        UIComponents.styleCombo(cboLocHinhThuc);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(taoTheDau(oTimKiem), 0);

        theBang.setLayout(new BorderLayout());
        theBang.add(UIComponents.tieuDeCard(new JLabel("Danh sách chương trình khuyến mãi"), null), BorderLayout.NORTH);
        tableModel = UIComponents.modelKhongSua("Mã khuyến mãi", "Tên chương trình", "Hình thức",
                "Phạm vi áp dụng", "Thời gian hiệu lực", "Đã dùng", "Trạng thái", "Thao tác");
        tblDanhSach = new JTable(tableModel);
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0)
                .setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(2)
                .setCellRenderer(new BadgeRenderer(s -> UIConstants.ROLE_MANAGER));
        tblDanhSach.getColumnModel().getColumn(6)
                .setCellRenderer(new BadgeRenderer(this::mauTrangThai));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(
                new LineIcon(Loai.PENCIL, UIConstants.ROLE_MANAGER),
                new LineIcon(Loai.REFRESH, UIConstants.PRIMARY),
                new LineIcon(Loai.CLOSE, UIConstants.ROLE_TECH_STOCK),
                new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT)));
        UIComponents.datDoRong(tblDanhSach, 110, 220, 140, 140, 190, 90, 130, 150);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        noiDung.them(theBang, 18);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        add(taoThanhDuoi(), BorderLayout.SOUTH);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLKhuyenMai panel = new QLKhuyenMai();
        new controller.QLKhuyenMaiController(panel);
        return panel;
    }

    // ==========================================================
    // Dựng giao diện
    // ==========================================================

    private JPanel taoTheDau(IconField oTimKiem) {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.setBorder(new EmptyBorder(20, 22, 20, 22));
        the.add(UIComponents.label("Quản lý khuyến mãi", UIConstants.FONT_PAGE_TITLE, UIConstants.TEXT_DARK),
                BorderLayout.NORTH);

        JPanel than = UIComponents.trong(new GridBagLayout());
        than.add(taoHangThongKe(), UIComponents.gbc(0, 0, 1, 1, 18, 0));
        than.add(taoHangLoc(oTimKiem), UIComponents.gbc(0, 1, 1, 1, 18, 0));
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoHangThongKe() {
        JPanel hang = UIComponents.trong(new GridLayout(1, 3, 16, 0));
        hang.add(taoOTThongKe("Tổng chương trình", UIConstants.ROLE_MANAGER_LIGHT, lblTongSo));
        hang.add(taoOTThongKe("Đang diễn ra", UIConstants.STATUS_SUCCESS, lblDangDienRa));
        hang.add(taoOTThongKe("Sắp hết hạn", UIConstants.STATUS_WARNING, lblSapHetHan));
        return hang;
    }

    private JPanel taoOTThongKe(String ten, Color mau, JLabel lblSo) {
        RoundedPanel o = UIComponents.box(UIComponents.tint(mau, 0.9f));
        o.setLayout(new BorderLayout(14, 0));
        o.setBorder(new EmptyBorder(12, 12, 12, 18));

        RoundedPanel vach = new RoundedPanel(3, mau, null);
        vach.setPreferredSize(new Dimension(6, 40));
        o.add(vach, BorderLayout.WEST);

        JPanel giua = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        giua.add(new Badge(ten, mau));
        o.add(giua, BorderLayout.CENTER);
        o.add(lblSo, BorderLayout.EAST);
        return o;
    }

    private JPanel taoHangLoc(IconField oTimKiem) {
        JPanel phai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        phai.add(UIComponents.label("Trạng thái", UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK));
        phai.add(cboLocTinhTrang);
        phai.add(UIComponents.label("Hình thức", UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK));
        phai.add(cboLocHinhThuc);
        phai.add(btnTaoMoi);

        JPanel hang = UIComponents.trong(new BorderLayout(16, 0));
        hang.add(oTimKiem, BorderLayout.CENTER);
        hang.add(phai, BorderLayout.EAST);
        return hang;
    }

    private JPanel taoThanhDuoi() {
        JPanel thanh = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 10));
        thanh.setBackground(UIConstants.CARD_BG);
        thanh.setBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        thanh.add(btnXuatExcel);
        return thanh;
    }

    private static JLabel taoSoLon() {
        JLabel l = UIComponents.label("0", UIConstants.FONT_PAGE_TITLE.deriveFont(26f), UIConstants.TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private Color mauTrangThai(String tinhTrang) {
        switch (tinhTrang) {
            case "Đang áp dụng":
                return UIConstants.STATUS_SUCCESS;
            case "Sắp diễn ra":
                return UIConstants.PRIMARY_LIGHT;
            case "Hết hạn":
                return UIConstants.TEXT_MUTED;
            case "Ngừng áp dụng":
                return UIConstants.STATUS_WARNING;
            default:
                return UIConstants.TEXT_MUTED;
        }
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setThongKe(int tongSo, int dangDienRa, int sapHetHan) {
        lblTongSo.setText(String.valueOf(tongSo));
        lblDangDienRa.setText(String.valueOf(dangDienRa));
        lblSapHetHan.setText(String.valueOf(sapHetHan));
    }

    /** Mỗi dòng: {maKM, ten, hinhThuc, phamVi, thoiGian, daDung, tinhTrang, ""}. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) {
            tableModel.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 10);
        theBang.revalidate();
        revalidate();
    }

    // --- Getter cho Controller ---
    public JButton getBtnTaoMoi() {
        return btnTaoMoi;
    }

    public JTextField getTxtTimKiem() {
        return txtTimKiem;
    }

    public JComboBox<String> getCboLocTinhTrang() {
        return cboLocTinhTrang;
    }

    public JComboBox<Object> getCboLocHinhThuc() {
        return cboLocHinhThuc;
    }

    public JTable getTblDanhSach() {
        return tblDanhSach;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JButton getBtnXuatExcel() {
        return btnXuatExcel;
    }
}
