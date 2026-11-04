package gui;

import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Nhật ký hệ thống (JPanel, chỉ đọc) dành cho Chủ cửa hàng. Phụ trách: Nguyễn Xuân Việt Anh. */
public class QLNhatKy extends JPanel {
    private final JTextField txtTimKiem;
    private final IconField fTuNgay = new IconField("Từ ngày", new LineIcon(Loai.CALENDAR, UIConstants.TEXT_MUTED, 15), false);
    private final IconField fDenNgay = new IconField("Đến ngày", new LineIcon(Loai.CALENDAR, UIConstants.TEXT_MUTED, 15), false);
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "Thời gian", "Tài khoản", "Hành động", "Đối tượng", "Giá trị cũ", "Giá trị mới");
    private final JTable tblDanhSach = new JTable(tableModel);
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();

    public QLNhatKy() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Nhật ký hệ thống",
                "Các thao tác nhạy cảm: hủy phiếu, sửa tiền công, đặt lại mật khẩu.", UIConstants.ROLE_MANAGER, null), 0);
        IconField timKiem = new IconField("Tìm tài khoản, hành động hoặc đối tượng",
                new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(420, 40));
        txtTimKiem = timKiem.getTextField();
        JPanel hang = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hang.add(timKiem);
        fTuNgay.setPreferredSize(new Dimension(150, 40));
        fDenNgay.setPreferredSize(new Dimension(150, 40));
        util.ChonNgay.ganNgay(fTuNgay.getTextField(), fDenNgay.getTextField());
        hang.add(fTuNgay);
        hang.add(fDenNgay);
        hang.add(btnTaiDuLieu);
        noiDung.them(hang, 18);

        UIComponents.styleTable(tblDanhSach);
        UIComponents.datDoRong(tblDanhSach, 160, 110, 170, 200, 170, 170);
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        noiDung.them(the, 12);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLNhatKy panel = new QLNhatKy();
        new controller.QLNhatKyController(panel);
        return panel;
    }

    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) {
            tableModel.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 14);
        revalidate();
    }

    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public JTextField getTxtTuNgay() { return fTuNgay.getTextField(); }
    public JTextField getTxtDenNgay() { return fDenNgay.getTextField(); }
    public DefaultTableModel getTableModel() { return tableModel; }
}
