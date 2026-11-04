package gui;

import entity.HinhThucKhuyenMai;
import entity.KhuyenMai;
import entity.PhamViKhuyenMai;
import util.UIConstants;
import util.UIComponents;
import util.UIComponents.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Hộp thoại tạo / sửa chương trình khuyến mãi.
 */
public class KhuyenMaiFormDialog extends JDialog {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ButtonGroup nhomHinhThuc = new ButtonGroup();
    private final ButtonGroup nhomPhamVi = new ButtonGroup();

    private final JLabel lblTieuDe =
            UIComponents.label("Tạo chương trình khuyến mãi", UIConstants.FONT_PAGE_TITLE, UIConstants.TEXT_DARK);

    private final JTextField txtTen = new JTextField();
    private final JRadioButton radPhanTram = new JRadioButton("Giảm theo %", true);
    private final JRadioButton radTienMat = new JRadioButton("Giảm theo số tiền cố định");
    private final JTextField txtGiaTri = new JTextField();
    private final JTextField txtBatDau = new PlaceholderField("Chọn ngày");
    private final JTextField txtKetThuc = new PlaceholderField("Chọn ngày");
    private final JRadioButton radTongHoaDon = new JRadioButton(PhamViKhuyenMai.TONG_HOA_DON.getText(), true);
    private final JRadioButton radNhomDichVu = new JRadioButton(PhamViKhuyenMai.NHOM_DICH_VU.getText());
    private final JRadioButton radPhuTung = new JRadioButton(PhamViKhuyenMai.PHU_TUNG.getText());
    private final JTextField txtSoLuotToiDa = new PlaceholderField("Để trống nếu không giới hạn lượt sử dụng");
    private final JTextArea txtGhiChu = new JTextArea(3, 20);
    private final JCheckBox chkKichHoat = new JCheckBox("Đang kích hoạt", true);
    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu thay đổi", UIConstants.PRIMARY);

    public KhuyenMaiFormDialog(Window owner) {
        super(owner, "Tạo / Sửa chương trình khuyến mãi", ModalityType.APPLICATION_MODAL);
        setSize(720, 860);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        nhomHinhThuc.add(radPhanTram);
        nhomHinhThuc.add(radTienMat);
        nhomPhamVi.add(radTongHoaDon);
        nhomPhamVi.add(radNhomDichVu);
        nhomPhamVi.add(radPhuTung);

        UIComponents.styleInput(txtTen);
        UIComponents.styleInput(txtGiaTri);
        UIComponents.styleInput(txtBatDau);
        UIComponents.styleInput(txtKetThuc);
        util.ChonNgay.ganNgay(txtBatDau, txtKetThuc);
        UIComponents.styleInput(txtSoLuotToiDa);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(20, 24, 20, 24));
        noiDung.them(lblTieuDe, 0);
        noiDung.them(taoTruong("Tên chương trình", true, txtTen), 18);
        noiDung.them(taoTruong("Hình thức giảm giá", true,
                taoHangRadio(radPhanTram, radTienMat)), 18);
        noiDung.them(taoTruong("Giá trị giảm", true, txtGiaTri), 18);

        JPanel hangNgay = UIComponents.trong(new GridLayout(1, 2, 16, 0));
        hangNgay.add(taoTruong("Thời gian bắt đầu", true, txtBatDau));
        hangNgay.add(taoTruong("Thời gian kết thúc", true, txtKetThuc));
        noiDung.them(hangNgay, 18);

        noiDung.them(taoTruong("Phạm vi áp dụng", true,
                taoHangRadio(radTongHoaDon, radNhomDichVu, radPhuTung)), 18);
        noiDung.them(taoTruong("Số lượt tối đa", false, txtSoLuotToiDa), 18);
        noiDung.them(taoTruong("Ghi chú", false, taoOVanBan()), 18);

        chkKichHoat.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        chkKichHoat.setForeground(UIConstants.TEXT_DARK);
        chkKichHoat.setOpaque(false);
        noiDung.them(chkKichHoat, 18);
        noiDung.ketThuc();

        add(UIComponents.cuonTrang(noiDung), BorderLayout.CENTER);
        add(taoThanhNut(), BorderLayout.SOUTH);

        btnHuy.addActionListener(e -> dispose());
        // Enter không lưu, chỉ Escape đóng hộp thoại
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dongHopThoai");
        getRootPane().getActionMap().put("dongHopThoai", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    // ==========================================================
    // Dựng giao diện
    // ==========================================================

    private JPanel taoTruong(String nhan, boolean batBuoc, JComponent o) {
        JPanel p = UIComponents.trong(new BorderLayout(0, 6));
        p.add(UIComponents.formLabel(nhan, batBuoc), BorderLayout.NORTH);
        p.add(o, BorderLayout.CENTER);
        return p;
    }

    private JPanel taoHangRadio(JRadioButton... ds) {
        JPanel p = UIComponents.trong(new FlowLayout(FlowLayout.LEFT, 18, 0));
        for (JRadioButton r : ds) {
            r.setFont(UIConstants.FONT_BODY.deriveFont(14f));
            r.setForeground(UIConstants.TEXT_DARK);
            r.setOpaque(false);
            p.add(r);
        }
        return p;
    }

    private JScrollPane taoOVanBan() {
        txtGhiChu.setFont(UIConstants.FONT_BODY.deriveFont(14f));
        txtGhiChu.setForeground(UIConstants.TEXT_DARK);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane cuon = new JScrollPane(txtGhiChu);
        cuon.setBorder(UIComponents.vienInput(false));
        cuon.setBackground(UIConstants.CARD_BG);
        cuon.setPreferredSize(new Dimension(100, 90));
        return cuon;
    }

    private JPanel taoThanhNut() {
        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        nut.add(btnHuy);
        nut.add(btnLuu);
        JPanel thanh = new JPanel(new BorderLayout());
        thanh.setBackground(UIConstants.CARD_BG);
        thanh.setBorder(new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));
        // Lỗi nằm cạnh nút Lưu để luôn thấy, không bị cuộn mất.
        lblLoi.setBorder(new EmptyBorder(0, 24, 0, 0));
        thanh.add(lblLoi, BorderLayout.CENTER);
        thanh.add(nut, BorderLayout.EAST);
        return thanh;
    }

    // ==========================================================
    // API cho Controller
    // ==========================================================

    public void setTieuDe(String tieuDe) {
        lblTieuDe.setText(tieuDe);
    }

    public void dien(KhuyenMai km) {
        txtTen.setText(km.getTenChuongTrinh());
        if (km.getHinhThuc() == HinhThucKhuyenMai.TIEN_MAT) {
            radTienMat.setSelected(true);
        } else {
            radPhanTram.setSelected(true);
        }
        txtGiaTri.setText(dinhDangGiaTri(km.getGiaTri(), km.getHinhThuc()));
        txtBatDau.setText(km.getThoiGianBatDau().format(DINH_DANG_NGAY));
        txtKetThuc.setText(km.getThoiGianKetThuc().format(DINH_DANG_NGAY));
        chonPhamVi(km.getPhamViApDung());
        txtSoLuotToiDa.setText(km.getSoLuotToiDa() == null ? "" : String.valueOf(km.getSoLuotToiDa()));
        txtGhiChu.setText(km.getGhiChu());
        chkKichHoat.setSelected(km.isTrangThai());
        hienLoi(null);
    }

    public void hienLoi(String msg) {
        UIComponents.showError(lblLoi, msg);
    }

    public PhamViKhuyenMai getPhamViDangChon() {
        if (radNhomDichVu.isSelected()) {
            return PhamViKhuyenMai.NHOM_DICH_VU;
        }
        if (radPhuTung.isSelected()) {
            return PhamViKhuyenMai.PHU_TUNG;
        }
        return PhamViKhuyenMai.TONG_HOA_DON;
    }

    private void chonPhamVi(PhamViKhuyenMai phamVi) {
        if (phamVi == PhamViKhuyenMai.NHOM_DICH_VU) {
            radNhomDichVu.setSelected(true);
        } else if (phamVi == PhamViKhuyenMai.PHU_TUNG) {
            radPhuTung.setSelected(true);
        } else {
            radTongHoaDon.setSelected(true);
        }
    }

    private String dinhDangGiaTri(double giaTri, HinhThucKhuyenMai hinhThuc) {
        if (hinhThuc == HinhThucKhuyenMai.PHAN_TRAM) {
            if (giaTri == Math.rint(giaTri)) {
                return String.valueOf((long) giaTri);
            }
            return String.valueOf(giaTri).replace('.', ',');
        }
        DecimalFormat df = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
        return df.format(giaTri).replace(',', '.');
    }

    // --- Getter cho Controller ---
    public JTextField getTxtTen() {
        return txtTen;
    }

    public JRadioButton getRadPhanTram() {
        return radPhanTram;
    }

    public JRadioButton getRadTienMat() {
        return radTienMat;
    }

    public JTextField getTxtGiaTri() {
        return txtGiaTri;
    }

    public JTextField getTxtBatDau() {
        return txtBatDau;
    }

    public JTextField getTxtKetThuc() {
        return txtKetThuc;
    }

    public JRadioButton getRadTongHoaDon() {
        return radTongHoaDon;
    }

    public JRadioButton getRadNhomDichVu() {
        return radNhomDichVu;
    }

    public JRadioButton getRadPhuTung() {
        return radPhuTung;
    }

    public JTextField getTxtSoLuotToiDa() {
        return txtSoLuotToiDa;
    }

    public JTextArea getTxtGhiChu() {
        return txtGhiChu;
    }

    public JCheckBox getChkKichHoat() {
        return chkKichHoat;
    }

    public JLabel getLblLoi() {
        return lblLoi;
    }

    public JButton getBtnHuy() {
        return btnHuy;
    }

    public JButton getBtnLuu() {
        return btnLuu;
    }
}
