package gui;

import controller.QLTaiKhoanController;
import entity.TaiKhoan;
import entity.VaiTro;
import util.UIComponents;
import util.UIComponents.*;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Tạo tài khoản và phân quyền (UC013). Phụ trách: Trần Thị Tường Vân. */
public class QLTaiKhoan extends JPanel {
    public static final int COT_TRANG_THAI = 3;
    public static final int COT_THAO_TAC = 4;
    public static final int ICON_SUA = 0;
    public static final int ICON_KHOA = 1;
    public static final int ICON_XOA = 2;

    private static final Set<VaiTro> CHU_VA_LE_TAN = EnumSet.of(VaiTro.CHU_CUA_HANG, VaiTro.LE_TAN);
    private static final Set<VaiTro> CHU_VA_KTV = EnumSet.of(VaiTro.CHU_CUA_HANG, VaiTro.KY_THUAT_VIEN);
    private static final Set<VaiTro> CHU_VA_THU_KHO = EnumSet.of(VaiTro.CHU_CUA_HANG, VaiTro.THU_KHO);
    private static final Set<VaiTro> CHI_CHU = EnumSet.of(VaiTro.CHU_CUA_HANG);

    /**
     * {chức năng, card trong menu, tập vai trò được mở}.
     * Phản ánh đúng GiaoDienChinhController.MENU_THEO_VAI_TRO; test.TestKhachHangTaiKhoan so khớp hai bên.
     */
    public static final Object[][] PHAN_QUYEN = {
        {"Tiếp nhận xe", "QL_TIEP_NHAN", CHU_VA_LE_TAN},
        {"Phiếu sửa chữa", "QL_PHIEU", CHU_VA_LE_TAN},
        {"Khách hàng", "QL_KHACH_HANG", CHU_VA_LE_TAN},
        {"Hồ sơ xe", "QL_HO_SO_XE", CHU_VA_LE_TAN},
        {"Hóa đơn & Thanh toán", "QL_HOA_DON", CHU_VA_LE_TAN},
        {"Bàn giao xe", "QL_BAN_GIAO", CHU_VA_LE_TAN},
        {"Tiến độ sửa chữa", "QL_TIEN_DO", CHU_VA_KTV},
        {"Phụ tùng & Kho (lễ tân, KTV chỉ xem)", "QL_PHU_TUNG", EnumSet.allOf(VaiTro.class)},
        {"Nhập kho", "QL_NHAP_KHO", CHU_VA_THU_KHO},
        {"Duyệt xuất kho", "QL_DUYET_XUAT", CHU_VA_THU_KHO},
        {"Khuyến mãi", "QL_KHUYEN_MAI", CHI_CHU},
        {"Báo cáo thống kê", "QL_THONG_KE", CHI_CHU},
        {"Dịch vụ & Nhà cung cấp", "QL_DANH_MUC", CHI_CHU},
        {"Nhân viên", "QL_NHAN_VIEN", CHI_CHU},
        {"Tạo tài khoản & phân quyền", "QL_TAI_KHOAN", CHI_CHU},
        {"Nhật ký hệ thống", "QL_NHAT_KY", CHI_CHU},
    };

    private final JLabel lblTieuDeForm = UIComponents.label("", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK);
    private final JComboBox<String> cboNhanVien = new JComboBox<>();
    private final JTextField txtTenDangNhap = new JTextField();
    private final JPasswordField pwdMatKhau = new JPasswordField();
    private final JLabel lblMatKhau = UIComponents.formLabel("Mật khẩu", true);
    private final JComboBox<VaiTro> cboVaiTro = new JComboBox<>(VaiTro.values());
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] {TaiKhoan.DANG_HOAT_DONG, TaiKhoan.BI_KHOA});
    private final JLabel lblLoiNhanVien = UIComponents.errorLabel();
    private final JLabel lblLoiTenDangNhap = UIComponents.errorLabel();
    private final JLabel lblLoiMatKhau = UIComponents.errorLabel();
    private final JLabel lblLoiChung = UIComponents.errorLabel();
    private final JButton btnDong = UIComponents.nutPhu("Đóng");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu", UIConstants.PRIMARY);

    public static final String TAT_CA_VAI_TRO = "Tất cả vai trò";
    public static final String TAT_CA_TRANG_THAI = "Tất cả trạng thái";
    private final JButton btnTaiDuLieu = UIComponents.nutTaiDuLieu();
    private final JTextField txtTimKiem;
    private final JComboBox<Object> cboLocVaiTro = new JComboBox<>();
    private final JComboBox<String> cboLocTrangThai = new JComboBox<>(
            new String[] {TAT_CA_TRANG_THAI, TaiKhoan.DANG_HOAT_DONG, TaiKhoan.BI_KHOA});
    private final DefaultTableModel tableModel = UIComponents.modelKhongSua(
            "<html>Tên đăng<br>nhập</html>", "Nhân viên", "Vai trò", "Trạng thái", "Thao tác");
    private final JTable tblDanhSach = new JTable(tableModel);
    private final JTable tblPhanQuyen = new JTable(UIComponents.modelKhongSua(
            "Chức năng", "<html>Chủ cửa<br>hàng</html>", "Lễ tân", "<html>Kỹ thuật<br>viên</html>", "Thủ kho"));
    private final DefaultTableModel nhatKyModel = UIComponents.modelKhongSua(
            "<html>Tài<br>khoản</html>", "Thời gian", "Thao tác", "Trước → Sau");
    private final JTable tblNhatKy = new JTable(nhatKyModel);

    public QLTaiKhoan() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        IconField timKiem = new IconField("Tìm tài khoản hoặc nhân viên", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(240, 36));
        txtTimKiem = timKiem.getTextField();

        CotDoc cotTrai = new CotDoc();
        cotTrai.them(taoTheForm(), 0);
        cotTrai.them(taoTheDanhSach(timKiem), 16);
        cotTrai.ketThuc();
        CotDoc cotPhai = new CotDoc();
        cotPhai.them(taoThePhanQuyen(), 0);
        cotPhai.them(taoTheNhatKy(), 16);
        cotPhai.ketThuc();
        JPanel haiCot = UIComponents.trong(new GridLayout(1, 2, 16, 0));
        haiCot.add(cotTrai);
        haiCot.add(cotPhai);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 24, 24, 24));
        noiDung.them(UIComponents.tieuDeTrang("Tạo tài khoản và phân quyền", "F5 làm mới • Ctrl+F tìm kiếm",
                UIConstants.ROLE_MANAGER, null), 0);
        noiDung.them(haiCot, 18);
        noiDung.ketThuc();
        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLTaiKhoan panel = new QLTaiKhoan();
        new QLTaiKhoanController(panel);
        return panel;
    }

    private JPanel taoTheForm() {
        UIComponents.styleCombo(cboNhanVien);
        UIComponents.styleInput(txtTenDangNhap);
        UIComponents.styleInput(pwdMatKhau);
        UIComponents.styleCombo(cboVaiTro);
        UIComponents.styleCombo(cboTrangThai);

        JPanel oMatKhau = UIComponents.trong(new BorderLayout(0, 6));
        oMatKhau.add(lblMatKhau, BorderLayout.NORTH);
        oMatKhau.add(pwdMatKhau, BorderLayout.CENTER);

        CotDoc than = new CotDoc();
        than.them(lblTieuDeForm, 0);
        than.them(QLTiepNhanSuaChua.o("Nhân viên", false, cboNhanVien), 12);
        than.them(lblLoiNhanVien, 4);
        than.them(QLTiepNhanSuaChua.luoi(2, QLTiepNhanSuaChua.o("Tên đăng nhập", true, txtTenDangNhap), oMatKhau), 12);
        than.them(lblLoiTenDangNhap, 4);
        than.them(lblLoiMatKhau, 4);
        than.them(QLTiepNhanSuaChua.luoi(2, QLTiepNhanSuaChua.o("Vai trò", true, cboVaiTro),
                QLTiepNhanSuaChua.o("Trạng thái", false, cboTrangThai)), 12);
        than.them(lblLoiChung, 8);
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnDong);
        nut.add(btnLuu);
        than.them(nut, 12);
        return bocThe(than);
    }

    private JPanel taoTheDanhSach(IconField timKiem) {
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(COT_TRANG_THAI).setCellRenderer(new BadgeRenderer(
                s -> TaiKhoan.DANG_HOAT_DONG.equals(s) ? UIConstants.STATUS_SUCCESS : UIConstants.STATUS_DANGER));
        // Biểu tượng giữa đổi theo trạng thái: khóa với tài khoản đang hoạt động, chìa khóa với tài khoản bị khóa.
        TableCellRenderer nutKhoa = new ActionRenderer(new LineIcon(Loai.PENCIL, UIConstants.ROLE_MANAGER),
                new LineIcon(Loai.LOCK, UIConstants.ROLE_TECH_STOCK), new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT));
        TableCellRenderer nutMoKhoa = new ActionRenderer(new LineIcon(Loai.PENCIL, UIConstants.ROLE_MANAGER),
                new LineIcon(Loai.KEY, UIConstants.ROLE_MANAGER), new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer((t, v, chon, focus, dong, cot) ->
                (TaiKhoan.BI_KHOA.equals(t.getValueAt(dong, COT_TRANG_THAI)) ? nutMoKhoa : nutKhoa)
                        .getTableCellRendererComponent(t, v, chon, focus, dong, cot));
        tblDanhSach.setRowHeight(64);
        UIComponents.datDoRong(tblDanhSach, 90, 140, 80, 145, 112);
        tblDanhSach.getColumnModel().getColumn(0).setMinWidth(90);
        // Dưới mức này ActionRenderer xuống dòng làm mất icon, còn badge bị cắt chữ.
        tblDanhSach.getColumnModel().getColumn(COT_TRANG_THAI).setMinWidth(145);
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setMinWidth(112);

        CotDoc than = new CotDoc();
        JPanel dau = UIComponents.trong(new BorderLayout(12, 0));
        dau.add(UIComponents.label("Danh sách tài khoản", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), BorderLayout.CENTER);
        dau.add(btnTaiDuLieu, BorderLayout.EAST);
        than.them(dau, 0);
        cboLocVaiTro.addItem(TAT_CA_VAI_TRO);
        for (VaiTro vaiTro : VaiTro.values()) {
            cboLocVaiTro.addItem(vaiTro);
        }
        UIComponents.styleCombo(cboLocVaiTro);
        UIComponents.styleCombo(cboLocTrangThai);
        JPanel hangLoc = UIComponents.trong(new GridLayout(1, 3, 8, 0));
        hangLoc.add(timKiem);
        hangLoc.add(cboLocVaiTro);
        hangLoc.add(cboLocTrangThai);
        than.them(hangLoc, 10);
        than.them(UIComponents.cuonBang(tblDanhSach), 10);
        return bocThe(than);
    }

    private JPanel taoThePhanQuyen() {
        UIComponents.styleTable(tblPhanQuyen);
        tblPhanQuyen.setRowHeight(36);
        DefaultTableModel model = (DefaultTableModel) tblPhanQuyen.getModel();
        for (Object[] r : PHAN_QUYEN) {
            @SuppressWarnings("unchecked")
            Set<VaiTro> duocPhep = (Set<VaiTro>) r[2];
            Object[] dong = new Object[VaiTro.values().length + 1];
            dong[0] = r[0];
            for (VaiTro vt : VaiTro.values()) {
                dong[vt.ordinal() + 1] = duocPhep.contains(vt);
            }
            model.addRow(dong);
        }
        // Vẽ icon thay vì ký tự ✓ vì phông Roboto có thể không có ký tự này.
        Icon dau = new LineIcon(Loai.CHECK, UIConstants.ROLE_MANAGER);
        DefaultTableCellRenderer veQuyen = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean chon, boolean focus, int dong, int cot) {
                boolean coQuyen = Boolean.TRUE.equals(v);
                super.getTableCellRendererComponent(t, coQuyen ? "" : "—", chon, false, dong, cot);
                setIcon(coQuyen ? dau : null);
                setForeground(UIConstants.TEXT_MUTED);
                setBorder(new EmptyBorder(0, 12, 0, 0));
                return this;
            }
        };
        for (int cot = 1; cot <= 4; cot++) {
            tblPhanQuyen.getColumnModel().getColumn(cot).setCellRenderer(veQuyen);
        }
        UIComponents.datDoRong(tblPhanQuyen, 200, 70, 60, 70, 70);

        CotDoc than = new CotDoc();
        than.them(UIComponents.label("Phân quyền", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), 0);
        than.them(UIComponents.label("Quyền cố định theo vai trò trong mã nguồn, chỉ xem tại đây. Vai trò quyết định menu được mở.",
                UIConstants.FONT_CAPTION, UIConstants.TEXT_MUTED), 2);
        than.them(UIComponents.cuonBang(tblPhanQuyen), 10);
        UIComponents.canChieuCaoBang(tblPhanQuyen, PHAN_QUYEN.length, PHAN_QUYEN.length);
        return bocThe(than);
    }

    private JPanel taoTheNhatKy() {
        UIComponents.styleTable(tblNhatKy);
        tblNhatKy.setRowHeight(58);
        UIComponents.datDoRong(tblNhatKy, 70, 90, 180, 180);
        CotDoc than = new CotDoc();
        than.them(UIComponents.label("Nhật ký hoạt động", UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_DARK), 0);
        than.them(UIComponents.cuonBang(tblNhatKy), 10);
        return bocThe(than);
    }

    private static JPanel bocThe(JComponent than) {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        the.setBorder(new EmptyBorder(18, 20, 18, 20));
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) {
            tableModel.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 8);
        revalidate();
    }

    public void setNhatKy(List<Object[]> dong) {
        nhatKyModel.setRowCount(0);
        for (Object[] r : dong) {
            nhatKyModel.addRow(r);
        }
        UIComponents.canChieuCaoBang(tblNhatKy, 2, 8);
        revalidate();
    }

    public void xoaLoi() {
        UIComponents.setError(cboNhanVien, false);
        UIComponents.setError(txtTenDangNhap, false);
        UIComponents.setError(pwdMatKhau, false);
        for (JLabel l : new JLabel[] {lblLoiNhanVien, lblLoiTenDangNhap, lblLoiMatKhau, lblLoiChung}) {
            UIComponents.showError(l, null);
        }
    }

    /** Sửa: nhân viên và tên đăng nhập cố định, bỏ trống mật khẩu nghĩa là giữ nguyên. */
    public void setCheDoSua(boolean sua) {
        lblTieuDeForm.setText(sua ? "Sửa tài khoản" : "Tạo tài khoản");
        cboNhanVien.setEnabled(!sua);
        txtTenDangNhap.setEditable(!sua);
        lblMatKhau.setText(UIComponents.formLabel(sua ? "Mật khẩu (trống = giữ nguyên)" : "Mật khẩu", !sua).getText());
    }

    public JComboBox<String> getCboNhanVien() { return cboNhanVien; }
    public JTextField getTxtTenDangNhap() { return txtTenDangNhap; }
    public JPasswordField getPwdMatKhau() { return pwdMatKhau; }
    public JComboBox<VaiTro> getCboVaiTro() { return cboVaiTro; }
    public JComboBox<String> getCboTrangThai() { return cboTrangThai; }
    public JLabel getLblLoiNhanVien() { return lblLoiNhanVien; }
    public JLabel getLblLoiTenDangNhap() { return lblLoiTenDangNhap; }
    public JLabel getLblLoiMatKhau() { return lblLoiMatKhau; }
    public JLabel getLblLoiChung() { return lblLoiChung; }
    public JButton getBtnDong() { return btnDong; }
    public JButton getBtnLuu() { return btnLuu; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<Object> getCboLocVaiTro() { return cboLocVaiTro; }
    public JComboBox<String> getCboLocTrangThai() { return cboLocTrangThai; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public JTable getTblNhatKy() { return tblNhatKy; }
    public JButton getBtnTaiDuLieu() { return btnTaiDuLieu; }
}
