package gui;

import util.UIConstants;
import util.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Hộp thoại xem trước và in Phiếu xuất xưởng (Gate Pass).
 * Phụ trách: Nguyễn Tuấn Vũ. (Chưa có mockup - bố cục tự thiết kế theo UC003 / FR-22.)
 */
public class InPhieuXuatXuongDialog extends JDialog {

    /** Dữ liệu in trên phiếu (Controller điền từ PhieuBanGiao + HoaDon + Xe + KhachHang). */
    public static class ThongTinPhieu {
        public String maPhieuBG, maPhieuTNSC, bienSo, xe, tenKhach, soDienThoai;
        public String ngayBanGiao, tongThanhToan, trangThaiHoaDon, nhanVienBanGiao;
    }

    private final JPanel pnlPhieu = new JPanel(new GridBagLayout());
    private final JButton btnIn = UIComponents.nutChinh("In phiếu", UIConstants.PRIMARY);
    private final JButton btnDong = UIComponents.nutPhu("Đóng");

    public InPhieuXuatXuongDialog(Window owner, ThongTinPhieu tt) {
        super(owner, "Phiếu xuất xưởng", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel nen = new JPanel(new BorderLayout(0, 16));
        nen.setBackground(UIConstants.APP_BG);
        nen.setBorder(new EmptyBorder(20, 24, 20, 24));

        dungPhieu(tt);
        nen.add(pnlPhieu, BorderLayout.CENTER);

        JPanel nut = UIComponents.trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.add(btnDong);
        nut.add(btnIn);
        nen.add(nut, BorderLayout.SOUTH);

        btnDong.addActionListener(e -> dispose());
        btnIn.addActionListener(e -> inPhieu());

        setContentPane(nen);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void dungPhieu(ThongTinPhieu tt) {
        pnlPhieu.setBackground(UIConstants.CARD_BG);
        pnlPhieu.setBorder(new javax.swing.border.CompoundBorder(
                new LineBorder(UIConstants.BORDER_COLOR, 1), new EmptyBorder(28, 36, 28, 36)));
        pnlPhieu.setPreferredSize(new Dimension(560, 560));

        int y = 0;
        y = them(UIComponents.label("AUTORA GARAGE", UIConstants.FONT_PAGE_TITLE.deriveFont(22f), UIConstants.PRIMARY), y, 0, true);
        y = them(UIComponents.label("AUTORA \u2022 Bình Thạnh, TP.HCM", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED), y, 2, true);
        y = them(UIComponents.label("PHIẾU XUẤT XƯỞNG", UIConstants.FONT_PAGE_TITLE.deriveFont(24f), UIConstants.TEXT_DARK), y, 22, true);
        y = them(UIComponents.label("(Gate Pass)", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED), y, 2, true);

        y = dong(y, "Mã phiếu bàn giao", tt.maPhieuBG, 22);
        y = dong(y, "Mã phiếu sửa chữa", tt.maPhieuTNSC, 8);
        y = dong(y, "Biển số xe", tt.bienSo, 8);
        y = dong(y, "Xe", tt.xe, 8);
        y = dong(y, "Khách hàng", tt.tenKhach, 8);
        y = dong(y, "Số điện thoại", tt.soDienThoai, 8);
        y = dong(y, "Tổng thanh toán", tt.tongThanhToan, 8);
        y = dong(y, "Trạng thái hóa đơn", tt.trangThaiHoaDon, 8);
        y = dong(y, "Ngày bàn giao", tt.ngayBanGiao, 8);

        JLabel cam = UIComponents.label("<html>Xe đã hoàn tất 100% hạng mục sửa chữa và hóa đơn đã được thanh toán hoặc ghi nhận công nợ hợp lệ. "
                + "Khách hàng xác nhận đã nhận lại xe.</html>", UIConstants.FONT_BODY.deriveFont(13f), UIConstants.TEXT_MUTED);
        y = them(cam, y, 20, true);

        JPanel chuKy = new JPanel(new GridLayout(1, 2, 24, 0));
        chuKy.setOpaque(false);
        chuKy.add(khungKy("Khách hàng", "(Ký, ghi rõ họ tên)", tt.tenKhach));
        chuKy.add(khungKy("Nhân viên bàn giao", "(Ký, ghi rõ họ tên)", tt.nhanVienBanGiao));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weightx = 1; g.weighty = 1;
        g.fill = GridBagConstraints.BOTH; g.insets = new Insets(22, 0, 0, 0);
        pnlPhieu.add(chuKy, g);
    }

    private int them(Component c, int y, int khoangTren, boolean canGiua) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = y; g.gridwidth = 2; g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(khoangTren, 0, 0, 0);
        if (canGiua && c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        pnlPhieu.add(c, g);
        return y + 1;
    }

    private int dong(int y, String nhan, String giaTri, int khoangTren) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = y; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(khoangTren, 0, 0, 16);
        pnlPhieu.add(UIComponents.label(nhan, UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_MUTED), g);
        g = new GridBagConstraints();
        g.gridx = 1; g.gridy = y; g.weightx = 1; g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(khoangTren, 0, 0, 0);
        pnlPhieu.add(UIComponents.label(giaTri == null ? "" : giaTri, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK), g);
        return y + 1;
    }

    private JPanel khungKy(String vaiTro, String ghiChu, String tenIn) {
        JPanel p = UIComponents.trong(new BorderLayout(0, 4));
        JLabel l1 = UIComponents.label(vaiTro, UIConstants.FONT_FORM_LABEL.deriveFont(14f), UIConstants.TEXT_DARK);
        l1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel l2 = UIComponents.label(ghiChu, UIConstants.FONT_CAPTION.deriveFont(12f), UIConstants.TEXT_MUTED);
        l2.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel tren = UIComponents.trong(new GridLayout(2, 1));
        tren.add(l1);
        tren.add(l2);
        JLabel ten = UIComponents.label(tenIn == null ? "" : tenIn, UIConstants.FONT_BODY.deriveFont(14f), UIConstants.TEXT_DARK);
        ten.setHorizontalAlignment(SwingConstants.CENTER);
        ten.setBorder(new EmptyBorder(0, 0, 0, 0));
        p.add(tren, BorderLayout.NORTH);
        p.add(ten, BorderLayout.SOUTH);
        return p;
    }

    /** In nội dung phiếu ra máy in (hộp thoại chọn máy in của hệ điều hành). */
    private void inPhieu() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Phieu xuat xuong");
        job.setPrintable((Graphics g, PageFormat pf, int trang) -> {
            if (trang > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double tyLe = Math.min(1.0, pf.getImageableWidth() / pnlPhieu.getWidth());
            g2.scale(tyLe, tyLe);
            pnlPhieu.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Không thể in phiếu: " + ex.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public JButton getBtnIn() { return btnIn; }
    public JButton getBtnDong() { return btnDong; }
}
