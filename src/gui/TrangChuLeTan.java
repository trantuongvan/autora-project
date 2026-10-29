package gui;

import util.UIConstants;
import util.UIComponents;
import util.UIComponents.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang chủ Lễ tân (JPanel) - nằm trong vùng Main Workspace của GiaoDienChinh.
 * Phụ trách: Nguyễn Tuấn Vũ.
 */
public class TrangChuLeTan extends JPanel {

    /** GiaoDienChinh đăng nhập để chuyển màn hình (CardLayout) khi bấm cột Thao tác. */
    public interface DieuHuongListener {
        void chuyenMan(String card, String maPhieu);
    }

    public static final String CARD_TIEP_NHAN = "QL_TIEP_NHAN";
    public static final String CARD_HO_SO_XE = "QL_HO_SO_XE";
    public static final String CARD_BAN_GIAO = "QL_BAN_GIAO";
    public static final int COT_THAO_TAC = 5;

    private final JLabel lblXinChao = new JLabel("Xin chào, đây là tình hình gara hôm nay.");
    private final JLabel lblSoChoTiepNhan = new JLabel("00");
    private final JLabel lblSoDangSua = new JLabel("00");
    private final JLabel lblSoChoBanGiao = new JLabel("00");
    private final JLabel lblSoDaBanGiao = new JLabel("00");
    private final JLabel lblThongTinBang = new JLabel();
    private final JLabel lblCapNhat = new JLabel();
    private final JLabel lblXemTatCa = new JLabel("<html><u>Xem tất cả</u></html>");
    private final JTextField txtTimKiem;
    private final JTable tblDanhSach;
    private final DefaultTableModel tableModel;
    private final JButton btnLamMoi;
    private DieuHuongListener dieuHuong = (card, ma) -> { };

    public TrangChuLeTan() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 28, 24, 28));

        // --- Tiêu đề + ca làm việc ---
        Badge badgeCa = new Badge("Ca làm việc \u2022 08:00 \u2013 17:00", UIConstants.ROLE_RECEPTION);
        noiDung.them(UIComponents.tieuDeTrang("Tổng quan công việc", lblXinChao, UIConstants.ROLE_RECEPTION, badgeCa), 0);

        // --- 4 thẻ thống kê ---
        JPanel hangThe = UIComponents.trong(new GridLayout(1, 4, 16, 0));
        hangThe.add(taoTheThongKe("Xe chờ tiếp nhận", "Cần lập hồ sơ tiếp nhận", UIConstants.ROLE_RECEPTION_LIGHT, Loai.CLIPBOARD, lblSoChoTiepNhan));
        hangThe.add(taoTheThongKe("Xe đang sửa", "Đang thực hiện sửa chữa", UIConstants.PRIMARY_LIGHT, Loai.WRENCH, lblSoDangSua));
        hangThe.add(taoTheThongKe("Xe chờ bàn giao", "Kiểm tra điều kiện bàn giao", UIConstants.ROLE_TECH_STOCK_LIGHT, Loai.KEY, lblSoChoBanGiao));
        hangThe.add(taoTheThongKe("Đã bàn giao hôm nay", "Tính đến " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " hôm nay", UIConstants.STATUS_SUCCESS, Loai.CHECK, lblSoDaBanGiao));
        noiDung.them(hangThe, 20);

        // --- Ô tìm kiếm ---
        IconField timKiem = new IconField("Tìm mã phiếu, biển số hoặc khách hàng", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(380, 40));
        txtTimKiem = timKiem.getTextField();
        JPanel hangTim = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hangTim.add(timKiem);
        noiDung.them(hangTim, 18);

        // --- Bảng danh sách công việc ---
        RoundedPanel theBang = UIComponents.card();
        theBang.setLayout(new BorderLayout());
        lblThongTinBang.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblThongTinBang.setForeground(UIConstants.TEXT_MUTED);
        theBang.add(UIComponents.tieuDeCard(new JLabel("Danh sách công việc"), lblThongTinBang), BorderLayout.NORTH);

        tableModel = UIComponents.modelKhongSua("Mã phiếu", "Biển số", "Khách hàng", "Xe", "Trạng thái", "Thao tác");
        tblDanhSach = new JTable(tableModel);
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(4).setCellRenderer(new BadgeRenderer(TrangChuLeTan::mauTrangThai));
        tblDanhSach.getColumnModel().getColumn(5).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), s -> UIConstants.PRIMARY));
        UIComponents.datDoRong(tblDanhSach, 150, 110, 180, 190, 190, 120);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);

        // --- Chân bảng ---
        lblXemTatCa.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblXemTatCa.setForeground(UIConstants.TEXT_MUTED);
        lblXemTatCa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblCapNhat.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblCapNhat.setForeground(UIConstants.TEXT_MUTED);
        btnLamMoi = UIComponents.nutIcon(new LineIcon(Loai.REFRESH, UIConstants.PRIMARY));
        JPanel phai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.add(lblCapNhat);
        phai.add(btnLamMoi);
        JPanel chan = UIComponents.trong(new BorderLayout());
        chan.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(8, 20, 8, 14)));
        chan.add(lblXemTatCa, BorderLayout.WEST);
        chan.add(phai, BorderLayout.EAST);
        theBang.add(chan, BorderLayout.SOUTH);
        noiDung.them(theBang, 16);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        ganSuKienGiaoDien();
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        TrangChuLeTan panel = new TrangChuLeTan();
        panel.napDuLieuMau();
        new controller.TrangChuLeTanController(panel);
        return panel;
    }

    // ==========================================================
    // Thành phần giao diện
    // ==========================================================

    private JPanel taoTheThongKe(String tieuDe, String moTa, Color nen, Loai loai, JLabel lblSo) {
        RoundedPanel the = new RoundedPanel(10, nen, null);
        the.setLayout(new BorderLayout(0, 6));
        the.setBorder(new EmptyBorder(16, 18, 16, 18));

        RoundedPanel khungIcon = new RoundedPanel(8, UIComponents.tint(nen, 0.8f), null);
        khungIcon.setLayout(new GridBagLayout());
        khungIcon.setPreferredSize(new Dimension(34, 34));
        khungIcon.add(new JLabel(new LineIcon(loai, nen)));

        JPanel tren = UIComponents.trong(new BorderLayout());
        tren.add(UIComponents.label(tieuDe, UIConstants.FONT_SECTION_TITLE, Color.WHITE), BorderLayout.WEST);
        tren.add(khungIcon, BorderLayout.EAST);

        lblSo.setFont(UIConstants.FONT_PAGE_TITLE.deriveFont(36f));
        lblSo.setForeground(Color.WHITE);
        JPanel duoi = UIComponents.trong(new BorderLayout(14, 0));
        duoi.add(lblSo, BorderLayout.WEST);
        duoi.add(UIComponents.label(moTa, UIConstants.FONT_BODY.deriveFont(12f), Color.WHITE), BorderLayout.CENTER);

        the.add(tren, BorderLayout.NORTH);
        the.add(duoi, BorderLayout.CENTER);
        return the;
    }

    private void ganSuKienGiaoDien() {
        tblDanhSach.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean tren = tblDanhSach.rowAtPoint(e.getPoint()) >= 0 && tblDanhSach.columnAtPoint(e.getPoint()) == COT_THAO_TAC;
                tblDanhSach.setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        tblDanhSach.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblDanhSach.rowAtPoint(e.getPoint());
                if (row < 0 || tblDanhSach.columnAtPoint(e.getPoint()) != COT_THAO_TAC) return;
                String ma = String.valueOf(tableModel.getValueAt(row, 0));
                String thaoTac = String.valueOf(tableModel.getValueAt(row, COT_THAO_TAC));
                dieuHuong.chuyenMan(cardTheoThaoTac(thaoTac), ma);
            }
        });
        lblXemTatCa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dieuHuong.chuyenMan(CARD_TIEP_NHAN, null);
            }
        });
    }

    private static String cardTheoThaoTac(String thaoTac) {
        if (thaoTac.startsWith("Bàn giao") || thaoTac.startsWith("In phiếu")) return CARD_BAN_GIAO;
        if (thaoTac.startsWith("Hồ sơ xe")) return CARD_HO_SO_XE;
        return CARD_TIEP_NHAN;
    }

    private static Color mauTrangThai(String s) {
        if (s.startsWith("Chờ tiếp nhận")) return UIConstants.ROLE_RECEPTION_LIGHT;
        if (s.startsWith("Đang sửa")) return UIConstants.PRIMARY_LIGHT;
        if (s.startsWith("Chờ bàn giao")) return UIConstants.STATUS_WARNING;
        if (s.startsWith("Đã bàn giao")) return UIConstants.STATUS_SUCCESS;
        return null;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setDieuHuongListener(DieuHuongListener l) {
        this.dieuHuong = l == null ? (card, ma) -> { } : l;
    }

    public void setTenNguoiDung(String ten) {
        lblXinChao.setText("Xin chào " + ten + ", đây là tình hình gara hôm nay.");
    }

    public void setSoLieu(int choTiepNhan, int dangSua, int choBanGiao, int daBanGiaoHomNay) {
        lblSoChoTiepNhan.setText(String.format("%02d", choTiepNhan));
        lblSoDangSua.setText(String.format("%02d", dangSua));
        lblSoChoBanGiao.setText(String.format("%02d", choBanGiao));
        lblSoDaBanGiao.setText(String.format("%02d", daBanGiaoHomNay));
    }

    /** Mỗi dòng: {maPhieu, bienSo, khachHang, xe, trangThai, thaoTac}. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) tableModel.addRow(r);
        lblThongTinBang.setText(dong.size() + " phiếu gần nhất \u2022 Ngày " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblCapNhat.setText("Cập nhật lúc " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 7);
    }

    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getBtnLamMoi() { return btnLamMoi; }
    public JLabel getLblXemTatCa() { return lblXemTatCa; }

    // ==========================================================
    // Dữ liệu mẫu để xem trước giao diện (xóa khi nối Controller)
    // ==========================================================

    public void napDuLieuMau() {
        setTenNguoiDung("Lan Anh");
        setSoLieu(4, 12, 3, 8);
        List<Object[]> ds = new ArrayList<>();
        ds.add(new Object[]{"SC-261012-021", "51H-123.45", "Nguyễn Văn Minh", "Toyota Vios \u2022 2021", "Chờ bàn giao", "Bàn giao \u2192"});
        ds.add(new Object[]{"SC-261012-020", "51G-678.90", "Trần Thị Hương", "Honda City \u2022 2020", "Đang sửa \u2022 80%", "Xem phiếu"});
        ds.add(new Object[]{"SC-261012-019", "59A-246.80", "Lê Hoàng Nam", "Mazda 3 \u2022 2022", "Chờ bàn giao", "Bàn giao \u2192"});
        ds.add(new Object[]{"SC-261012-018", "51K-889.99", "Phạm Ngọc Mai", "Kia Seltos \u2022 2023", "Chờ tiếp nhận", "Hồ sơ xe"});
        ds.add(new Object[]{"SC-261012-017", "51F-345.67", "Võ Quốc Bảo", "Hyundai i10 \u2022 2019", "Đã bàn giao", "In phiếu"});
        ds.add(new Object[]{"SC-261012-017", "51F-345.67", "Võ Quốc Bảo", "Hyundai i10 \u2022 2019", "Đã bàn giao", "In phiếu"});
        ds.add(new Object[]{"SC-261012-017", "51F-345.67", "Võ Quốc Bảo", "Hyundai i10 \u2022 2019", "Đã bàn giao", "In phiếu"});
        setDanhSach(ds);
    }
}
