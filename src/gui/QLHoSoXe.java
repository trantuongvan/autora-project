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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình Quản lý hồ sơ xe (JPanel) - phân hệ Lễ tân (màu ROLE_RECEPTION).
 * Phụ trách: Nguyễn Tuấn Vũ.
 */
public class QLHoSoXe extends JPanel {

    public static final int COT_THAO_TAC = 6;
    public static final int ICON_XEM = 0;
    public static final int ICON_SUA = 1;

    /** Danh sách Hãng -> Dòng xe gợi ý (ô combo cho phép gõ thêm giá trị khác). */
    public static final Map<String, String[]> HANG_DONG = new LinkedHashMap<>();
    static {
        HANG_DONG.put("Toyota", new String[]{"Vios", "Camry", "Cross", "Innova", "Fortuner"});
        HANG_DONG.put("Honda", new String[]{"City", "Civic", "CR-V", "HR-V"});
        HANG_DONG.put("Hyundai", new String[]{"i10", "Accent", "SantaFe", "Tucson"});
        HANG_DONG.put("Kia", new String[]{"Morning", "Seltos", "Carnival"});
        HANG_DONG.put("Mazda", new String[]{"3", "CX-5", "CX-8"});
        HANG_DONG.put("BMW", new String[]{"320i", "X5"});
        HANG_DONG.put("Mercedes", new String[]{"C200", "GLC"});
    }

    // --- Danh sách ---
    private final JButton btnThemHoSo = UIComponents.nutChinh("+  Thêm hồ sơ xe", UIConstants.PRIMARY);
    private final JTextField txtTimKiem;
    private final JTable tblDanhSach;
    private final DefaultTableModel tableModel;
    private final JLabel lblThongKeBang = new JLabel();

    // --- Form ---
    private final JLabel lblTieuDeForm = new JLabel("Thêm hồ sơ xe");
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtVIN = new JTextField();
    private final JComboBox<String> cboHang = new JComboBox<>();
    private final JComboBox<String> cboDong = new JComboBox<>();
    private final JTextField txtNam = new JTextField();
    private final JTextField txtMauSon = new JTextField();
    private final JComboBox<String> cboChuSoHuu = new JComboBox<>();
    private String maChuSoHuuCanChon;
    private final JLabel lblLoiBienSo = UIComponents.errorLabel();
    private final JLabel lblLoiChuSoHuu = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu hồ sơ", UIConstants.PRIMARY);

    // --- Lịch sử sửa chữa ---
    private final JLabel lblBienSoLichSu = new JLabel();
    private final JLabel lblTenChuXe = new JLabel();
    private final JLabel lblThongTinXe = new JLabel();
    private final CotDoc pnlDanhSachLichSu = new CotDoc();
    private final RoundedPanel pnlThongBaoKhoa = UIComponents.box(UIComponents.tint(UIConstants.PRIMARY_LIGHT, 0.9f));
    private final JLabel lblThongBaoKhoa = new JLabel();

    public QLHoSoXe() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.APP_BG);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(24, 28, 24, 28));

        noiDung.them(UIComponents.tieuDeTrang("Quản lý hồ sơ xe", "Thông tin xe, chủ sở hữu và lịch sử sửa chữa tại gara.",
                UIConstants.ROLE_RECEPTION, btnThemHoSo), 0);

        // --- Tìm kiếm ---
        IconField timKiem = new IconField("Tìm biển số, SĐT hoặc tên chủ xe", new LineIcon(Loai.SEARCH, UIConstants.TEXT_MUTED), true);
        timKiem.setPreferredSize(new Dimension(460, 40));
        txtTimKiem = timKiem.getTextField();
        JPanel hangTim = UIComponents.trong(new BorderLayout());
        JPanel trai = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.add(timKiem);
        hangTim.add(trai, BorderLayout.WEST);
        hangTim.add(UIComponents.label("Xem / Sửa hồ sơ bằng biểu tượng trong bảng", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED), BorderLayout.EAST);
        noiDung.them(hangTim, 18);

        // --- Bảng ---
        RoundedPanel theBang = UIComponents.card();
        theBang.setLayout(new BorderLayout());
        tableModel = UIComponents.modelKhongSua("Biển số", "Hãng / Dòng", "Năm", "VIN", "Chủ xe", "SĐT", "Xem / Sửa");
        tblDanhSach = new JTable(tableModel);
        UIComponents.styleTable(tblDanhSach);
        tblDanhSach.getColumnModel().getColumn(0).setCellRenderer(new CellRenderer(UIConstants.FONT_FORM_LABEL.deriveFont(14f), null));
        Icon xem = new LineIcon(Loai.EYE, UIConstants.PRIMARY);
        Icon sua = new LineIcon(Loai.PENCIL, UIConstants.PRIMARY);
        tblDanhSach.getColumnModel().getColumn(COT_THAO_TAC).setCellRenderer(new ActionRenderer(xem, sua));
        UIComponents.datDoRong(tblDanhSach, 120, 160, 70, 190, 190, 130, 110);
        theBang.add(UIComponents.cuonBang(tblDanhSach), BorderLayout.CENTER);

        lblThongKeBang.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblThongKeBang.setForeground(UIConstants.TEXT_MUTED);
        JPanel chan = UIComponents.trong(new BorderLayout());
        chan.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR), new EmptyBorder(9, 22, 9, 22)));
        chan.add(lblThongKeBang, BorderLayout.WEST);
        theBang.add(chan, BorderLayout.SOUTH);
        noiDung.them(theBang, 16);

        // --- Hàng dưới: Form (trái) + Lịch sử (phải) ---
        JPanel hangDuoi = UIComponents.trong(new GridBagLayout());
        GridBagConstraints gt = new GridBagConstraints();
        gt.gridx = 0;
        gt.weightx = 0.58;
        gt.fill = GridBagConstraints.BOTH;
        gt.insets = new Insets(0, 0, 0, 8);
        hangDuoi.add(taoTheForm(), gt);
        GridBagConstraints gp = new GridBagConstraints();
        gp.gridx = 1;
        gp.weightx = 0.42;
        gp.fill = GridBagConstraints.BOTH;
        gp.insets = new Insets(0, 8, 0, 0);
        hangDuoi.add(taoTheLichSu(), gp);
        noiDung.them(hangDuoi, 16);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);

        khoiTaoThanhPhanForm();
        ganSuKienGiaoDien();
    }

    /** Điểm tạo View theo chuẩn nhóm. */
    public static JPanel createPanel() {
        QLHoSoXe panel = new QLHoSoXe();
        panel.napDuLieuMau();
        new controller.QLHoSoXeController(panel);
        return panel;
    }

    // ==========================================================
    // Dựng giao diện
    // ==========================================================

    private JPanel taoTheForm() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        JLabel ghiChu = UIComponents.label("* Thông tin bắt buộc", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED);
        the.add(UIComponents.tieuDeCard(lblTieuDeForm, ghiChu), BorderLayout.NORTH);

        JPanel than = UIComponents.trong(new GridBagLayout());
        than.setBorder(new EmptyBorder(18, 24, 24, 24));
        than.add(UIComponents.formLabel("Biển số", true), UIComponents.gbc(0, 0, 2, 0.56, 0, 12));
        than.add(UIComponents.formLabel("VIN", false), UIComponents.gbc(2, 0, 2, 0.44, 0, 0));
        than.add(txtBienSo, UIComponents.gbc(0, 1, 2, 0.56, 6, 12));
        than.add(txtVIN, UIComponents.gbc(2, 1, 2, 0.44, 6, 0));
        than.add(lblLoiBienSo, UIComponents.gbc(0, 2, 2, 0, 4, 0));

        than.add(UIComponents.formLabel("Hãng", false), UIComponents.gbc(0, 3, 1, 0.25, 14, 12));
        than.add(UIComponents.formLabel("Dòng", false), UIComponents.gbc(1, 3, 1, 0.25, 14, 12));
        than.add(UIComponents.formLabel("Năm", false), UIComponents.gbc(2, 3, 1, 0.25, 14, 12));
        than.add(UIComponents.formLabel("Màu", false), UIComponents.gbc(3, 3, 1, 0.25, 14, 0));
        than.add(cboHang, UIComponents.gbc(0, 4, 1, 0.25, 6, 12));
        than.add(cboDong, UIComponents.gbc(1, 4, 1, 0.25, 6, 12));
        than.add(txtNam, UIComponents.gbc(2, 4, 1, 0.25, 6, 12));
        than.add(txtMauSon, UIComponents.gbc(3, 4, 1, 0.25, 6, 0));

        than.add(UIComponents.formLabel("Chủ sở hữu", true), UIComponents.gbc(0, 5, 4, 1, 14, 0));
        than.add(cboChuSoHuu, UIComponents.gbc(0, 6, 4, 1, 6, 0));
        than.add(lblLoiChuSoHuu, UIComponents.gbc(0, 7, 4, 1, 4, 0));

        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.add(btnHuy);
        nut.add(btnLuu);
        than.add(nut, UIComponents.gbc(0, 8, 4, 1, 16, 0));
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTheLichSu() {
        RoundedPanel the = UIComponents.card();
        the.setLayout(new BorderLayout());
        lblBienSoLichSu.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblBienSoLichSu.setForeground(UIConstants.TEXT_MUTED);
        the.add(UIComponents.tieuDeCard(new JLabel("Lịch sử sửa chữa"), lblBienSoLichSu), BorderLayout.NORTH);

        lblTenChuXe.setFont(UIConstants.FONT_FORM_LABEL.deriveFont(16f));
        lblTenChuXe.setForeground(UIConstants.TEXT_DARK);
        lblThongTinXe.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblThongTinXe.setForeground(UIConstants.TEXT_MUTED);
        JPanel chu = UIComponents.trong(new BorderLayout(10, 0));
        chu.add(new JLabel(new LineIcon(Loai.KEY, UIConstants.PRIMARY)), BorderLayout.WEST);
        JPanel chuChu = UIComponents.trong(null);
        chuChu.setLayout(new BoxLayout(chuChu, BoxLayout.Y_AXIS));
        chuChu.add(lblTenChuXe);
        chuChu.add(lblThongTinXe);
        chu.add(chuChu, BorderLayout.CENTER);

        pnlThongBaoKhoa.setLayout(new BorderLayout(10, 0));
        pnlThongBaoKhoa.setBorder(new EmptyBorder(12, 14, 12, 14));
        pnlThongBaoKhoa.add(new JLabel(new LineIcon(Loai.LOCK, UIConstants.PRIMARY)), BorderLayout.WEST);
        lblThongBaoKhoa.setFont(UIConstants.FONT_BODY.deriveFont(13f));
        lblThongBaoKhoa.setForeground(UIConstants.PRIMARY);
        pnlThongBaoKhoa.add(lblThongBaoKhoa, BorderLayout.CENTER);
        pnlThongBaoKhoa.setVisible(false);

        CotDoc than = new CotDoc();
        than.setBorder(new EmptyBorder(16, 18, 18, 18));
        than.them(chu, 0);
        than.them(pnlDanhSachLichSu, 12);
        than.them(pnlThongBaoKhoa, 12);
        than.ketThuc();
        the.add(than, BorderLayout.CENTER);
        return the;
    }

    private void khoiTaoThanhPhanForm() {
        UIComponents.styleInput(txtBienSo);
        UIComponents.styleInput(txtVIN);
        UIComponents.styleInput(txtNam);
        UIComponents.styleInput(txtMauSon);
        UIComponents.styleCombo(cboHang);
        UIComponents.styleCombo(cboDong);
        UIComponents.styleCombo(cboChuSoHuu);
        cboHang.setEditable(true);
        cboDong.setEditable(true);
        cboHang.addItem("");
        for (String h : HANG_DONG.keySet()) cboHang.addItem(h);
        cboHang.setSelectedIndex(0);
        cboChuSoHuu.addItem("Chọn chủ sở hữu");
    }

    private void ganSuKienGiaoDien() {
        txtBienSo.getDocument().addDocumentListener(new DocumentListener() {
            private void capNhat() {
                if (entity.Xe.bienSoHopLe(txtBienSo.getText())) {
                    hienLoiBienSo(null);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhat();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhat();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhat();
            }
        });
        cboChuSoHuu.addActionListener(e -> {
            Object value = cboChuSoHuu.getSelectedItem();
            if (value != null && !value.toString().startsWith("Chọn")) {
                hienLoiChuSoHuu(null);
            }
        });
        tblDanhSach.setToolTipText("Chọn hồ sơ và nhấn Delete để xóa");
        // Chọn Hãng -> nạp lại danh sách Dòng tương ứng
        cboHang.addActionListener(e -> {
            Object h = cboHang.getSelectedItem();
            String[] dong = h == null ? null : HANG_DONG.get(h.toString());
            cboDong.removeAllItems();
            cboDong.addItem("");
            if (dong != null) for (String d : dong) cboDong.addItem(d);
            cboDong.setSelectedIndex(0);
        });
        tblDanhSach.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean tren = UIComponents.viTriBieuTuong(tblDanhSach, e, COT_THAO_TAC, 2) >= 0;
                tblDanhSach.setCursor(Cursor.getPredefinedCursor(tren ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        // Nút "Hủy" đưa form về trạng thái thêm mới (Controller có thể gán thêm listener)
        btnHuy.addActionListener(e -> {
            xoaTrangForm();
            setCheDoSua(false);
            anLichSu();
        });
        btnThemHoSo.addActionListener(e -> {
            tblDanhSach.clearSelection();
            xoaTrangForm();
            setCheDoSua(false);
            anLichSu();
            txtBienSo.requestFocusInWindow();
        });
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    /** Mỗi dòng: {bienSo, "Hãng / Dòng", nam, vin, chuXe, sdt, ""}. */
    public void setDanhSach(List<Object[]> dong) {
        tableModel.setRowCount(0);
        for (Object[] r : dong) tableModel.addRow(r);
        UIComponents.canChieuCaoBang(tblDanhSach, 3, 8);
    }

    public void setThongKeBang(int soHoSo, String dangChon) {
        lblThongKeBang.setText(soHoSo + " hồ sơ" + (dangChon == null ? "" : " \u2022 Đang chọn " + dangChon));
    }

    /** Chuyển form giữa chế độ Thêm và Sửa (khóa ô khóa chính bienSo khi sửa). */
    public void setCheDoSua(boolean sua) {
        lblTieuDeForm.setText(sua ? "Sửa hồ sơ xe" : "Thêm hồ sơ xe");
        btnLuu.setText(sua ? "Cập nhật" : "Lưu hồ sơ");
        txtBienSo.setEditable(!sua);
        txtBienSo.setEnabled(true);
        txtVIN.setEnabled(true);
        txtNam.setEnabled(true);
        txtMauSon.setEnabled(true);
        cboHang.setEnabled(true);
        cboDong.setEnabled(true);
        cboChuSoHuu.setEnabled(true);
        btnLuu.setEnabled(true);
        btnHuy.setText("Hủy");
    }
    /** Chế độ xem thông tin: giữ nguyên form nhưng khóa tất cả trường và nút lưu. */
    public void setCheDoXem(boolean xem) {
        if (!xem) {
            setCheDoSua(false);
            return;
        }
        lblTieuDeForm.setText("Thông tin hồ sơ xe");
        txtBienSo.setEditable(false);
        txtBienSo.setEnabled(false);
        txtVIN.setEnabled(false);
        txtNam.setEnabled(false);
        txtMauSon.setEnabled(false);
        cboHang.setEnabled(false);
        cboDong.setEnabled(false);
        cboChuSoHuu.setEnabled(false);
        btnLuu.setText("Chế độ xem");
        btnLuu.setEnabled(false);
        btnHuy.setText("Đóng");
    }
    public void dienForm(entity.Xe xe) {
        maChuSoHuuCanChon = xe.getMaKH();
        txtBienSo.setText(xe.getBienSo());
        txtVIN.setText(xe.getSoVIN());
        cboHang.setSelectedItem(xe.getHangXe());
        cboDong.setSelectedItem(xe.getDongXe());
        txtNam.setText(xe.getDoiXe() == null ? "" : xe.getDoiXe().toString());
        txtMauSon.setText(xe.getMauSon());
        chonChuSoHuu();
        hienLoiBienSo(null);
        hienLoiChuSoHuu(null);
    }

    public void setDanhSachChuSoHuu(List<String> hienThi) {
        String dangChon = String.valueOf(cboChuSoHuu.getSelectedItem());
        String maCanChon = maChuSoHuuCanChon;
        if (maCanChon == null && dangChon.contains(" - ")) maCanChon = dangChon.split(" - ", 2)[0];
        cboChuSoHuu.removeAllItems();
        cboChuSoHuu.addItem("Chọn chủ sở hữu");
        for (String s : hienThi) {
            cboChuSoHuu.addItem(s);
        }
        maChuSoHuuCanChon = maCanChon;
        if (maCanChon != null) chonChuSoHuu();
        else if (!"null".equals(dangChon) && !"Chọn chủ sở hữu".equals(dangChon)) {
            cboChuSoHuu.setSelectedItem(dangChon);
        }
    }

    private void chonChuSoHuu() {
        if (maChuSoHuuCanChon == null) {
            return;
        }
        String prefix = maChuSoHuuCanChon + " - ";
        for (int i = 0; i < cboChuSoHuu.getItemCount(); i++) {
            if (cboChuSoHuu.getItemAt(i).startsWith(prefix)) {
                cboChuSoHuu.setSelectedIndex(i);
                return;
            }
        }
    }

    public void xoaTrangForm() {
        txtBienSo.setText("");
        maChuSoHuuCanChon = null;
        txtVIN.setText("");
        txtMauSon.setText("");
        cboHang.setSelectedIndex(0);
        cboDong.setSelectedIndex(0);
        txtNam.setText("");
        if (cboChuSoHuu.getItemCount() > 0) cboChuSoHuu.setSelectedIndex(0);
        hienLoiBienSo(null);
        hienLoiChuSoHuu(null);
    }

    /** msg = null để xóa lỗi. Đồng thời bôi đỏ / bỏ bôi đỏ viền ô nhập. */
    public void hienLoiBienSo(String msg) {
        UIComponents.showError(lblLoiBienSo, msg);
        UIComponents.setError(txtBienSo, msg != null);
    }

    public void hienLoiChuSoHuu(String msg) {
        UIComponents.showError(lblLoiChuSoHuu, msg);
        UIComponents.setError(cboChuSoHuu, msg != null);
    }

    /**
     * Hiển thị lịch sử sửa chữa của một xe.
     * Mỗi phiếu: {maPhieu, ngay, moTa, trangThai (vd "Hoàn tất 100%"), tongTien (vd "2.970.000 đ")}.
     * thongBaoKhoa = null thì ẩn dòng thông báo khóa dữ liệu.
     */
    public void hienThiLichSu(String bienSo, String tenChu, String thongTinXe, List<Object[]> dsPhieu, String thongBaoKhoa) {
        lblBienSoLichSu.setText(bienSo);
        lblTenChuXe.setText(tenChu);
        lblThongTinXe.setText(thongTinXe);
        pnlDanhSachLichSu.removeAll();
        boolean dau = true;
        for (Object[] p : dsPhieu) {
            pnlDanhSachLichSu.them(taoTheLichSu(p, dau), 10);
            dau = false;
        }
        pnlThongBaoKhoa.setVisible(thongBaoKhoa != null);
        if (thongBaoKhoa != null) lblThongBaoKhoa.setText("<html>" + thongBaoKhoa + "</html>");
        pnlDanhSachLichSu.revalidate();
        pnlDanhSachLichSu.repaint();
    }

    public void anLichSu() {
        lblBienSoLichSu.setText("");
        lblTenChuXe.setText("Chọn một xe để xem lịch sử sửa chữa");
        lblThongTinXe.setText("");
        pnlDanhSachLichSu.removeAll();
        pnlThongBaoKhoa.setVisible(false);
        pnlDanhSachLichSu.revalidate();
        pnlDanhSachLichSu.repaint();
    }

    private JPanel taoTheLichSu(Object[] p, boolean noiBat) {
        RoundedPanel the = new RoundedPanel(8, noiBat ? UIComponents.tint(UIConstants.PRIMARY_LIGHT, 0.9f) : UIConstants.CARD_BG,
                noiBat ? null : UIConstants.BORDER_COLOR);
        the.setLayout(new GridBagLayout());
        the.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        the.add(UIComponents.label(String.valueOf(p[0]), UIConstants.FONT_FORM_LABEL.deriveFont(15f), noiBat ? UIConstants.PRIMARY : UIConstants.TEXT_DARK), g);
        g = new GridBagConstraints();
        g.gridy = 0;
        g.gridx = 1;
        g.anchor = GridBagConstraints.EAST;
        the.add(UIComponents.label(String.valueOf(p[1]), UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED), g);
        g = new GridBagConstraints();
        g.gridy = 1;
        g.gridx = 0;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(6, 0, 8, 0);
        the.add(UIComponents.label(String.valueOf(p[2]), UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK), g);
        g = new GridBagConstraints();
        g.gridy = 2;
        g.gridx = 0;
        g.anchor = GridBagConstraints.WEST;
        String tt = String.valueOf(p[3]);
        if (tt.startsWith("Hoàn tất")) {
            the.add(new Badge(tt, UIConstants.STATUS_SUCCESS), g);
        } else {
            the.add(UIComponents.label(
                    tt, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK), g);
        }
        g = new GridBagConstraints();
        g.gridy = 2;
        g.gridx = 1;
        g.anchor = GridBagConstraints.EAST;
        the.add(UIComponents.label(String.valueOf(p[4]), UIConstants.FONT_FORM_LABEL.deriveFont(15f), UIConstants.TEXT_DARK), g);
        return the;
    }

    // --- Getter cho Controller ---
    public JButton getBtnThemHoSo() { return btnThemHoSo; }
    public JTextField getTxtTimKiem() { return txtTimKiem; }
    public JTable getTblDanhSach() { return tblDanhSach; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtBienSo() { return txtBienSo; }
    public JTextField getTxtVIN() { return txtVIN; }
    public JComboBox<String> getCboHang() { return cboHang; }
    public JComboBox<String> getCboDong() { return cboDong; }
    public JTextField getTxtNam() { return txtNam; }
    public JTextField getTxtMauSon() { return txtMauSon; }
    public JComboBox<String> getCboChuSoHuu() { return cboChuSoHuu; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnLuu() { return btnLuu; }

    // ==========================================================
    // Dữ liệu mẫu để xem trước giao diện (xóa khi nối Controller)
    // ==========================================================

    public void napDuLieuMau() {
        List<Object[]> ds = new ArrayList<>();
        ds.add(new Object[]{"51H-123.45", "Toyota / Vios", "2021", "RL4B29F3300123456", "Nguyễn Văn Minh", "0901234567", ""});
        ds.add(new Object[]{"51G-678.90", "Honda / City", "2020", "RL4GM6640LY123456", "Trần Thị Hương", "0912345678", ""});
        ds.add(new Object[]{"51G-678.90", "Honda / City", "2020", "RL4GM6640LY123456", "Trần Thị Hương", "0912345678", ""});
        ds.add(new Object[]{"51G-678.90", "Honda / City", "2020", "RL4GM6640LY123456", "Trần Thị Hương", "0912345678", ""});
        setDanhSach(ds);
        setThongKeBang(2, null);

        setDanhSachChuSoHuu(List.of("KH0001 - Nguyễn Văn Minh (0901234567)", "KH0002 - Trần Thị Hương (0912345678)"));
        // Form thêm mới bắt đầu ở trạng thái sạch; chỉ hiển thị lỗi sau khi người dùng lưu dữ liệu sai.
        xoaTrangForm();

        anLichSu();
    }
}
