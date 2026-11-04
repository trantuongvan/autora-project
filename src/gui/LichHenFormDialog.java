package gui;

import entity.Xe;
import util.ChonNgay;
import util.UIComponents;
import util.UIComponents.CotDoc;
import util.UIComponents.PlaceholderField;
import util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/** Hộp thoại thêm lịch hẹn cho một khách hàng. */
public class LichHenFormDialog extends JDialog {

    private final JTextField txtThoiGian = new PlaceholderField("Chọn ngày giờ hẹn");
    private final JComboBox<Xe> cboXe = new JComboBox<>();
    private final JTextField txtGhiChu = new JTextField();
    private final JLabel lblLoi = UIComponents.errorLabel();
    private final JButton btnHuy = UIComponents.nutPhu("Hủy");
    private final JButton btnLuu = UIComponents.nutChinh("Lưu lịch hẹn", UIConstants.PRIMARY);

    /** xeCuaKhach là xe của khách đang chọn; ô chọn xe luôn có thêm lựa chọn trống ở đầu. */
    public LichHenFormDialog(Window owner, String tenKhach, List<Xe> xeCuaKhach) {
        super(owner, "Thêm lịch hẹn", ModalityType.APPLICATION_MODAL);
        setSize(480, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        UIComponents.styleInput(txtThoiGian);
        ChonNgay.ganNgayGio(txtThoiGian);
        UIComponents.styleInput(txtGhiChu);
        cboXe.addItem(null);
        for (Xe xe : xeCuaKhach) {
            cboXe.addItem(xe);
        }
        UIComponents.styleCombo(cboXe);

        CotDoc noiDung = new CotDoc();
        noiDung.setBorder(new EmptyBorder(20, 24, 20, 24));
        noiDung.them(UIComponents.label("Thêm lịch hẹn", UIConstants.FONT_PAGE_TITLE, UIConstants.TEXT_DARK), 0);
        noiDung.them(UIComponents.label("Khách hàng: " + tenKhach, UIConstants.FONT_BODY, UIConstants.TEXT_MUTED), 4);
        noiDung.them(taoTruong("Ngày giờ hẹn", true, txtThoiGian), 16);
        noiDung.them(taoTruong("Xe", false, cboXe), 16);
        noiDung.them(taoTruong("Ghi chú", false, txtGhiChu), 16);
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

    private static JPanel taoTruong(String nhan, boolean batBuoc, JComponent o) {
        JPanel p = UIComponents.trong(new BorderLayout(0, 6));
        p.add(UIComponents.formLabel(nhan, batBuoc), BorderLayout.NORTH);
        p.add(o, BorderLayout.CENTER);
        return p;
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

    public void hienLoi(String msg) {
        UIComponents.showError(lblLoi, msg);
    }

    public JTextField getTxtThoiGian() { return txtThoiGian; }
    public JComboBox<Xe> getCboXe() { return cboXe; }
    public JTextField getTxtGhiChu() { return txtGhiChu; }
    public JButton getBtnHuy() { return btnHuy; }
    public JButton getBtnLuu() { return btnLuu; }
}
