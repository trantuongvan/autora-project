package gui;

import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Danh mục dịch vụ (tiền công, thời gian chuẩn) và nhà cung cấp, dành cho Chủ cửa hàng. */
public class QLDanhMuc extends JPanel {

    /** Một thẻ danh mục: ô tìm kiếm, bảng và biểu mẫu thêm/sửa. Ô đầu tiên là mã, do hệ thống sinh. */
    public static class TheDanhMuc extends JPanel {
        private final JTextField txtTimKiem;
        private final DefaultTableModel tableModel;
        private final JTable tblDanhSach;
        private final JTextField[] o;
        private final JLabel lblTieuDeForm = UIComponents.label("", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK);
        private final JLabel lblLoi = UIComponents.label(" ", UIConstants.FONT_BODY, UIConstants.STATUS_DANGER);
        private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();
        private final JButton btnThemMoi = UIComponents.nutPhu("Thêm mới");
        private final JButton btnXoa = UIComponents.nutPhu("Xóa");
        private final JButton btnLuu = UIComponents.nutChinh("Lưu", UIConstants.PRIMARY);
        private final String tenLoai;

        TheDanhMuc(String tenLoai, String goiYTim, String[] cot, String[] nhanO, boolean[] batBuoc, int... doRong) {
            this.tenLoai = tenLoai;
            setLayout(new BorderLayout());
            setOpaque(false);
            CotDoc noiDung = new CotDoc();
            noiDung.setBorder(new EmptyBorder(16, 0, 0, 0));

            IconField timKiem = new IconField(goiYTim, new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
            timKiem.setPreferredSize(new Dimension(420, 40));
            txtTimKiem = timKiem.getTextField();
            JPanel hang = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 8, 0));
            hang.add(timKiem);
            hang.add(btnTaiDuLieu);
            noiDung.them(hang, 0);

            tableModel = UIComponents.modelKhongSua(cot);
            tblDanhSach = new JTable(tableModel);
            UIComponents.styleTable(tblDanhSach);
            UIComponents.datDoRong(tblDanhSach, doRong);
            tblDanhSach.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            RoundedPanel bang = UIComponents.card();
            bang.setLayout(new BorderLayout());
            bang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
            noiDung.them(bang, 12);

            RoundedPanel form = UIComponents.card();
            form.setLayout(new GridBagLayout());
            form.setBorder(new EmptyBorder(16, 20, 16, 20));
            form.add(lblTieuDeForm, UIComponents.gbc(0, 0, 2, 1, 0, 0));
            o = new JTextField[nhanO.length];
            for (int i = 0; i < nhanO.length; i++) {
                o[i] = new JTextField();
                UIComponents.styleInput(o[i]);
                JPanel cot2 = UIComponents.trong(new BorderLayout(0, 6));
                cot2.add(UIComponents.formLabel(nhanO[i], batBuoc[i]), BorderLayout.NORTH);
                cot2.add(o[i], BorderLayout.CENTER);
                form.add(cot2, UIComponents.gbc(i % 2, 1 + i / 2, 1, 0.5, 12, i % 2 == 0 ? 16 : 0));
            }
            o[0].setEditable(false);
            JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            nut.add(btnXoa);
            nut.add(btnThemMoi);
            nut.add(btnLuu);
            int hangCuoi = 1 + (nhanO.length + 1) / 2;
            form.add(lblLoi, UIComponents.gbc(0, hangCuoi, 2, 1, 12, 0));
            form.add(nut, UIComponents.gbc(0, hangCuoi + 1, 2, 1, 8, 0));
            noiDung.them(form, 12);
            noiDung.ketThuc();
            add(noiDung, BorderLayout.CENTER);
            datCheDoThem();
        }

        public void setDanhSach(List<Object[]> dong) {
            tableModel.setRowCount(0);
            for (Object[] r : dong) {
                tableModel.addRow(r);
            }
            UIComponents.canChieuCaoBang(tblDanhSach, 3, 10);
            revalidate();
        }

        /** Biểu mẫu trống để thêm mới; mã sẽ được sinh khi lưu. */
        public void datCheDoThem() {
            for (JTextField f : o) {
                f.setText("");
            }
            o[0].setText("Tự động cấp khi lưu");
            lblTieuDeForm.setText("Thêm " + tenLoai);
            btnXoa.setVisible(false);
            tblDanhSach.clearSelection();
            hienLoi(null);
        }

        public void datCheDoSua(String[] giaTri) {
            for (int i = 0; i < o.length; i++) {
                o[i].setText(giaTri[i]);
            }
            lblTieuDeForm.setText("Sửa " + tenLoai + " " + giaTri[0]);
            btnXoa.setVisible(true);
            hienLoi(null);
        }

        /** Mã đang sửa; null khi đang thêm mới. */
        public String getMaDangSua() {
            return btnXoa.isVisible() ? o[0].getText().trim() : null;
        }

        public void hienLoi(String loi) {
            lblLoi.setText(loi == null ? " " : loi);
        }

        public String giaTri(int i) { return o[i].getText().trim(); }
        public JTextField getO(int i) { return o[i]; }
        public JTextField getTxtTimKiem() { return txtTimKiem; }
        public JTable getTblDanhSach() { return tblDanhSach; }
        public DefaultTableModel getTableModel() { return tableModel; }
        public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
        public JButton getBtnThemMoi() { return btnThemMoi; }
        public JButton getBtnXoa() { return btnXoa; }
        public JButton getBtnLuu() { return btnLuu; }
    }

    private final TheDanhMuc dichVu = new TheDanhMuc("dịch vụ", "Tìm mã hoặc tên dịch vụ",
            new String[] {"Mã", "Tên dịch vụ", "Tiền công", "Thời gian chuẩn"},
            new String[] {"Mã dịch vụ", "Tên dịch vụ", "Tiền công (đ)", "Thời gian chuẩn (phút)"},
            new boolean[] {false, true, true, true}, 110, 420, 160, 160);
    private final TheDanhMuc nhaCungCap = new TheDanhMuc("nhà cung cấp", "Tìm mã, tên hoặc số điện thoại",
            new String[] {"Mã", "Tên nhà cung cấp", "Số điện thoại", "Địa chỉ"},
            new String[] {"Mã nhà cung cấp", "Tên nhà cung cấp", "Số điện thoại", "Địa chỉ"},
            new boolean[] {false, true, false, false}, 110, 340, 150, 300);
    private final JTabbedPane tabs = new JTabbedPane();

    public QLDanhMuc() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);
        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Dịch vụ & Nhà cung cấp",
                "Tiền công và thời gian chuẩn dùng khi lập phiếu và tự phân công; nhà cung cấp dùng khi nhập kho.",
                UIConstants.ROLE_MANAGER, null), 0);
        tabs.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD, 14f));
        tabs.addTab("Dịch vụ", dichVu);
        tabs.addTab("Nhà cung cấp", nhaCungCap);
        noiDung.them(tabs, 16);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLDanhMuc panel = new QLDanhMuc();
        new controller.QLDanhMucController(panel);
        return panel;
    }

    public TheDanhMuc getDichVu() { return dichVu; }
    public TheDanhMuc getNhaCungCap() { return nhaCungCap; }
    public JTabbedPane getTabs() { return tabs; }
}
