package gui;

import util.UIConstants;
import util.UIComponents;
import util.UIComponents.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản lý nhân viên (JPanel) - phân hệ Chủ cửa hàng (màu ROLE_MANAGER).
 * Phụ trách: Nguyễn Tuấn Vũ.
 */
public class QLNhanVien extends JPanel {

    public static final int COT_THAO_TAC = 6;
    public static final int ICON_XEM = 0;
    public static final int ICON_SUA = 1;
    public static final int ICON_XOA = 2;

    public static final String[] VI_TRI = {"Chủ cửa hàng", "Lễ tân", "Kỹ thuật viên", "Thủ kho"};

    // --- Danh sách ---
    private final JButton btnThemNhanVien = UIComponents.nutChinh("+  Thêm nhân viên", UIConstants.ROLE_MANAGER);
    private final JTextField txtTimKiem;
    private final JComboBox<String> cboLocViTri = new JComboBox<>();
    private final JTable tblDanhSach;
    private final DefaultTableModel tableModel;
    private final RoundedPanel theBang = UIComponents.card();

    // --- Form ---
    private final JLabel lblTieuDeForm = new JLabel("Thêm nhân viên");
    private final IconField fldMaNV = new IconField("", new LineIcon(Loai.LOCK, UIConstants.TEXT_MUTED), false);
    private final JTextField txtMaNV = fldMaNV.getTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtSoDienThoai = new JTextField();
    private final JTextField txtDiaChi = new JTextField();
    private final JComboBox<String> cboViTri = new JComboBox<>(VI_TRI);
    private final JLabel lblLoiHoTen = UIComponents.errorLabel();
    private final JLabel lblLoiSoDienThoai = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu nhân viên", UIConstants.ROLE_MANAGER);

    // --- Thẻ xác nhận xóa ---
    private final RoundedPanel theXoa = UIComponents.card();
    private final JLabel lblTenXoa = new JLabel();
    private final JLabel lblMoTaXoa = new JLabel();
    private final JButton btnHuyXoa = UIComponents.nutPhu("Hủy");
    private final JButton btnXacNhanXoa = UIComponents.nutChinh("Xóa nhân viên", UIConstants.ROLE_RECEPTION_LIGHT);
    private final JButton btnDongXoa = UIComponents.nutIcon(new LineIcon(Loai.CLOSE, UIConstants.TEXT_MUTED));

    public QLNhanVien() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 28, 24, 28));
        noiDung.them(UIComponents.tieuDeTrang("Quản lý nhân viên", "Quản lý hồ sơ nhân sự \u2022 Tài khoản được quản lý tại trang Tạo tài khoản & phân quyền.",
                UIConstants.ROLE_MANAGER, btnThemNhanVien), 0);

        // --- Tìm kiếm + bộ lọc ---
        IconField timKiem = new IconField("Tìm mã nhân viên, họ tên hoặc SĐT", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(500, 40));
        txtTimKiem = timKiem.getTextField();
        cboLocViTri.addItem("Tất cả vị trí");
        for (String v : VI_TRI) cboLocViTri.addItem(v);
        UIComponents.styleCombo(cboLocViTri);
        cboLocViTri.setPreferredSize(new Dimension(170, 40));
        JPanel hangTim = UIComponents.trong(new BorderLayout());
        JPanel trai = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.add(timKiem);
        JPanel phai = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.add(cboLocViTri);
        hangTim.add(trai, BorderLayout.WEST);
        hangTim.add(phai, BorderLayout.EAST);
        noiDung.them(hangTim, 18);

        // --- Bảng ---
        theBang.setLayout(new BorderLayout());
        tableModel = UIComponents.modelKhongSua("Mã NV", "Họ tên", "SĐT", "Địa chỉ", "Vị trí", "Tài khoản liên kết", "Thao tác");
        tblDanhSach = new JTable(tableModel);
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(
                new LineIcon(Loai.EYE, UIConstants.ROLE_MANAGER),
                new LineIcon(Loai.PENCIL, UIConstants.ROLE_MANAGER),
                new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT)));
        UIComponents.datDoRong(tblDanhSach, 90, 160, 120, 230, 130, 190, 120);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);
        noiDung.them(theBang, 16);

        // --- Hàng dưới: Form (trái) + Xác nhận xóa (phải) ---
        JPanel hangDuoi = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gt = new GridBagConstraints();
        gt.gridx = 0; gt.weightx = 0.6; gt.weighty = 1; gt.fill = GridBagConstraints.BOTH; gt.anchor = GridBagConstraints.NORTH;
        gt.insets = new Insets(0, 0, 0, 8);
        hangDuoi.add(taoTheForm(), gt);
        GridBagConstraints gp = new GridBagConstraints();
        gp.gridx = 1; gp.weightx = 0.4; gp.weighty = 1; gp.fill = GridBagConstraints.BOTH; gp.anchor = GridBagConstraints.NORTH;
        gp.insets = new Insets(0, 8, 0, 0);
        hangDuoi.add(taoTheXoa(), gp);
        noiDung.them(hangDuoi, 16);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);

        khoiTaoThanhPhanForm();
        theXoa.setVisible(false);
        ganSuKienGiaoDien();
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLNhanVien panel = new QLNhanVien();
        new controller.QLNhanVienController(panel);
        return panel;
    }

    // ==========================================================
    // Dựng giao diện
    // ==========================================================

    private JPanel taoTheForm() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        JLabel ghiChu = UIComponents.label("Mã NV tự sinh \u2022 * Bắt buộc", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED);
        the.add(UIComponents.tieuDeCard(lblTieuDeForm, ghiChu), BorderLayout.NORTH);

        JPanel than = UIComponents.trong(new GridBagLayout());
        than.setBorder(new EmptyBorder(18, 24, 24, 24));
        than.add(UIComponents.formLabel("Mã NV", false), UIComponents.gbc(0, 0, 1, 0.2, 0, 12));
        than.add(UIComponents.formLabel("Họ tên", true), UIComponents.gbc(1, 0, 1, 0.36, 0, 12));
        than.add(UIComponents.formLabel("SĐT", true), UIComponents.gbc(2, 0, 1, 0.44, 0, 0));
        than.add(fldMaNV, UIComponents.gbc(0, 1, 1, 0.2, 6, 12));
        than.add(txtHoTen, UIComponents.gbc(1, 1, 1, 0.36, 6, 12));
        than.add(txtSoDienThoai, UIComponents.gbc(2, 1, 1, 0.44, 6, 0));
        than.add(lblLoiHoTen, UIComponents.gbc(1, 2, 1, 0, 4, 12));
        than.add(lblLoiSoDienThoai, UIComponents.gbc(2, 2, 1, 0, 4, 0));

        than.add(UIComponents.formLabel("Địa chỉ", false), UIComponents.gbc(0, 3, 2, 0.56, 14, 12));
        than.add(UIComponents.formLabel("Vị trí", true), UIComponents.gbc(2, 3, 1, 0.44, 14, 0));
        than.add(txtDiaChi, UIComponents.gbc(0, 4, 2, 0.56, 6, 12));
        than.add(cboViTri, UIComponents.gbc(2, 4, 1, 0.44, 6, 0));

        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.add(btnHuy);
        nut.add(btnLuu);
        than.add(nut, UIComponents.gbc(0, 6, 3, 1, 16, 0));
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheXoa() {
        theXoa.setLayout(new BorderLayout());
        theXoa.add(UIComponents.tieuDeCard(new JLabel("Xác nhận xóa nhân viên"), btnDongXoa), BorderLayout.NORTH);

        RoundedPanel khungIcon = UIComponents.box(UIComponents.tint(UIConstants.ROLE_RECEPTION_LIGHT, 0.88f));
        khungIcon.setLayout(new GridBagLayout());
        khungIcon.setPreferredSize(new Dimension(46, 46));
        khungIcon.add(new JLabel(new LineIcon(Loai.TRASH, UIConstants.ROLE_RECEPTION_LIGHT, 22)));

        lblTenXoa.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(16f));
        lblTenXoa.setForeground(UIConstants.TEXT_DARK);
        lblMoTaXoa.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        lblMoTaXoa.setForeground(UIConstants.TEXT_MUTED);
        JPanel chu = UIComponents.trong(null);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.add(lblTenXoa);
        chu.add(lblMoTaXoa);
        JPanel dau = UIComponents.trong(new BorderLayout(12, 0));
        dau.add(khungIcon, BorderLayout.WEST);
        dau.add(chu, BorderLayout.CENTER);

        JLabel cauHoi = UIComponents.label("<html>Bạn có chắc muốn xóa nhân viên này? Thao tác không thể hoàn tác.</html>",
                UIConstants.FONT_BODY.deriveFont(16f), UIConstants.TEXT_DARK);

        RoundedPanel canhBao = UIComponents.box(UIComponents.tint(UIConstants.STATUS_WARNING, 0.88f));
        canhBao.setLayout(new BorderLayout(10, 0));
        canhBao.setBorder(new EmptyBorder(11, 14, 11, 14));
        canhBao.add(new JLabel(new LineIcon(Loai.ALERT, UIComponents.shade(UIConstants.STATUS_WARNING, 0.6f))), BorderLayout.WEST);
        canhBao.add(UIComponents.label("Kiểm tra đúng nhân viên trước khi xác nhận xóa.", UIConstants.FONT_BODY.deriveFont(14f),
                UIComponents.shade(UIConstants.STATUS_WARNING, 0.5f)), BorderLayout.CENTER);

        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.add(btnHuyXoa);
        nut.add(btnXacNhanXoa);

        CotDoc than = new CotDoc();
        than.setBorder(new EmptyBorder(18, 22, 22, 22));
        than.them(dau, 0);
        than.them(cauHoi, 14);
        than.them(canhBao, 14);
        than.them(nut, 16);
        than.ketThuc();
        theXoa.add(than, BorderLayout.CENTER);
        return theXoa;
    }

    private void khoiTaoThanhPhanForm() {
        UIComponents.styleInput(txtHoTen);
        UIComponents.styleInput(txtSoDienThoai);
        UIComponents.styleInput(txtDiaChi);
        UIComponents.styleCombo(cboViTri);
        fldMaNV.setEditable(false);
    }

    private void ganSuKienGiaoDien() {
        txtHoTen.getDocument().addDocumentListener(new DocumentListener(){private void capNhat(){if(!txtHoTen.getText().trim().isEmpty())hienLoiHoTen(null);}public void insertUpdate(DocumentEvent e){capNhat();}public void removeUpdate(DocumentEvent e){capNhat();}public void changedUpdate(DocumentEvent e){capNhat();}});
        txtSoDienThoai.getDocument().addDocumentListener(new DocumentListener(){private void capNhat(){if(txtSoDienThoai.getText().trim().matches("0[0-9]{9}"))hienLoiSoDienThoai(null);}public void insertUpdate(DocumentEvent e){capNhat();}public void removeUpdate(DocumentEvent e){capNhat();}public void changedUpdate(DocumentEvent e){capNhat();}});
        btnDongXoa.addActionListener(e -> theXoa.setVisible(false));
        btnHuyXoa.addActionListener(e -> theXoa.setVisible(false));
        tblDanhSach.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean tren = UIComponents.viTriBieuTuong(tblDanhSach, e, COT_THAO_TAC, 3) >= 0;
                tblDanhSach.setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        btnHuy.addActionListener(e -> {
            xoaTrangForm();
            setCheDoSua(false);
        });
        btnThemNhanVien.addActionListener(e -> {
            tblDanhSach.clearSelection();
            xoaTrangForm();
            setCheDoSua(false);
            txtHoTen.requestFocusInWindow();
        });
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    /** Mỗi dòng: {maNV, hoTen, sdt, diaChi, viTri, taiKhoanLienKet (hoặc "—"), ""}. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) tableModel.addRow(r);
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 8);
        int visibleRows = Math.max(3, Math.min(8, dong.size()));
        theBang.setPreferredSize(new Dimension(100, 46 + visibleRows * tblDanhSach.getRowHeight()));
        theBang.revalidate();
        revalidate();
    }

    public void setCheDoSua(boolean sua) {
        lblTieuDeForm.setText(sua ? "Sửa nhân viên" : "Thêm nhân viên");
        btnLuu.setText(sua ? "Cập nhật" : "Lưu nhân viên");
    }
    public String getLblTieuDeFormText() { return lblTieuDeForm.getText(); }

    public void xoaTrangForm() {
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtDiaChi.setText("");
        cboViTri.setSelectedIndex(1);
        hienLoiHoTen(null);
        hienLoiSoDienThoai(null);
    }

    public void dienForm(entity.NhanVien nv) {
        txtMaNV.setText(nv.getMaNV());
        txtHoTen.setText(nv.getHoTen());
        txtSoDienThoai.setText(nv.getSoDienThoai());
        txtDiaChi.setText(nv.getDiaChi());
        cboViTri.setSelectedItem(nv.getViTriLamViec());
    }

    public void hienLoiHoTen(String msg) {
        UIComponents.showError(lblLoiHoTen, msg);
        UIComponents.setError(txtHoTen, msg != null);
    }

    public void hienLoiSoDienThoai(String msg) {
        UIComponents.showError(lblLoiSoDienThoai, msg);
        UIComponents.setError(txtSoDienThoai, msg != null);
    }

    public void hienXacNhanXoa(String hoTenVaMa, String viTriVaTrangThai) {
        lblTenXoa.setText(hoTenVaMa);
        lblMoTaXoa.setText(viTriVaTrangThai);
        theXoa.setVisible(true);
        theXoa.getParent().revalidate();
    }

    public void anXacNhanXoa() {
        theXoa.setVisible(false);
    }

    // --- Getter cho Controller ---
    public JButton getBtnThemNhanVien() { return btnThemNhanVien; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JComboBox<String> getCboLocViTri() { return cboLocViTri; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtMaNV() { return txtMaNV; }
    public JTextField getTxtHoTen() { return txtHoTen; }
    public JTextField getTxtSoDienThoai() { return txtSoDienThoai; }
    public JTextField getTxtDiaChi() { return txtDiaChi; }
    public JComboBox<String> getCboViTri() { return cboViTri; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnLuu() { return btnLuu; }
    public JButton getBtnHuyXoa() { return btnHuyXoa; }
    public JButton getBtnXacNhanXoa() { return btnXacNhanXoa; }
    public JButton getBtnDongXoa() { return btnDongXoa; }

    // ==========================================================
    // Dữ liệu mẫu để xem trước giao diện (xóa khi nối Controller)
    // ==========================================================

    public void napDuLieuMau() {
        List<Object[]> ds = new ArrayList<>();
        ds.add(new Object[]{"NV0001", "Trần Minh Hoàng", "0908888666", "18 Lê Quang Định, TP.HCM", "Chủ cửa hàng", "minhhoang", ""});
        ds.add(new Object[]{"NV0002", "Nguyễn Lan Anh", "0902345678", "45 Xô Viết Nghệ Tĩnh", "Lễ tân", "lananh", ""});
        ds.add(new Object[]{"NV0004", "Lê Văn Phúc", "0934567890", "26 Đinh Bộ Lĩnh, TP.HCM", "Thủ kho", "levanphuc", ""});
        ds.add(new Object[]{"NV0005", "Lê Văn Sĩ", "0934564390", "26 Đinh Bộ Lĩnh, TP.HCM", "Kỹ thuật viên", "levansi", ""});
        setDanhSach(ds);

        txtMaNV.setText("NV0006");
        txtHoTen.setText("Phạm Đức Long");
        txtSoDienThoai.setText("0912345678");
        txtDiaChi.setText("12 Nguyễn Hữu Cảnh, TP.HCM");
        cboViTri.setSelectedItem("Kỹ thuật viên");
        hienLoiHoTen(null);
        hienLoiSoDienThoai(null);
        anXacNhanXoa();
    }
}
