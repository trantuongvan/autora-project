package controller;

import dao.NhanVien_Dao;
import dao.PhieuBanGiao_Dao;
import entity.NhanVien;
import gui.InPhieuXuatXuongDialog;
import gui.QLBanGiaoXe;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Bàn giao chỉ được xác nhận khi dịch vụ hoàn tất và hóa đơn hợp lệ. */
public class QLBanGiaoXeController {

    private final QLBanGiaoXe view;
    private final PhieuBanGiao_Dao dao = new PhieuBanGiao_Dao();
    private final NhanVien_Dao nhanVienDao = new NhanVien_Dao();
    private String selected;
    private int loadVersion;

    public QLBanGiaoXeController(QLBanGiaoXe view) {
        this.view = view;
        initController();
        load();
    }

    private void initController() {
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                load();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                load();
            }
        });
        view.getCboLoc().addActionListener(e -> load());
        view.getTblDanhSach().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && view.getTblDanhSach().getSelectedRow() >= 0) {
                int row = view.getTblDanhSach().convertRowIndexToModel(
                        view.getTblDanhSach().getSelectedRow());
                selected = String.valueOf(view.getTableModel().getValueAt(row, 0));
                detail(selected);
            }
        });
        view.getBtnXacNhan().addActionListener(e -> confirm());
        view.getBtnXemPhieu().addActionListener(e -> print());
    }

    private void load() {
        final int version = ++loadVersion;
        final String query = view.getTxtTimKiem().getText();
        final String filter = String.valueOf(view.getCboLoc().getSelectedItem());
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return dao.findDanhSach(query, filter);
            }

            @Override
            protected void done() {
                if (version != loadVersion) {
                    return;
                }
                try {
                    List<Object[]> rows = get();
                    view.setDanhSach(rows);
                    view.anThongBaoThanhCong();
                    view.setCanhBaoDieuKien(null);
                    if (rows.isEmpty()) {
                        view.anChiTiet();
                        selected = null;
                    } else {
                        view.getTblDanhSach().setRowSelectionInterval(0, 0);
                    }
                } catch (Exception ignored) {
                    // Lỗi truy vấn được xử lý ở lớp DAO hoặc giao diện.
                }
            }
        }.execute();
    }

    private void detail(String id) {
        try {
            Object[] data = dao.findChiTiet(id);
            if (data == null) {
                return;
            }
            QLBanGiaoXe.ChiTietBanGiao detail = new QLBanGiaoXe.ChiTietBanGiao();
            detail.xe = data[0] + " • " + data[1];
            detail.chuXeVaPhieu = data[2] + " • " + id;
            detail.nhanTienDo = "Hoàn tất " + data[3] + "%";
            detail.hoanTat100 = ((Number) data[3]).intValue() == 100;
            detail.tienCong = money(data[4]);
            detail.phuTung = money(data[5]);
            detail.giamGia = "− " + money(data[6]);
            double taxRate = ((Number) data[12]).doubleValue();
            detail.nhanVAT = String.format(
                    java.util.Locale.forLanguageTag("vi-VN"), "VAT (%.0f%%)", taxRate);
            detail.vat = money(data[7]);
            detail.tongThanhToan = money(data[8]);
            detail.duDieuKien = (Boolean) data[10];
            detail.daBanGiao = !String.valueOf(data[14]).isEmpty();
            if (detail.daBanGiao) {
                detail.thongBao = "Phiếu đã bàn giao, có thể xem và in lại phiếu xuất xưởng.";
            } else if (detail.duDieuKien) {
                detail.thongBao = data[9] + " đã xử lý. Đủ điều kiện bàn giao.";
            } else {
                detail.thongBao = lyDoChuaDuDieuKien(data);
            }
            view.hienChiTiet(detail);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể tải chi tiết phiếu: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String lyDoChuaDuDieuKien(Object[] data) {
        int progress = ((Number) data[3]).intValue();
        if (progress < 100) {
            return progress == 0
                    ? "Chưa có hạng mục sửa chữa hoàn tất."
                    : "Còn " + (100 - progress) + "% hạng mục sửa chữa chưa hoàn tất.";
        }
        String payment = String.valueOf(data[11]);
        if (String.valueOf(data[9]).isEmpty()) {
            return "Phiếu chưa có hóa đơn.";
        }
        if ("Đã hủy".equals(payment)) {
            return "Hóa đơn đã bị hủy.";
        }
        if ("Công nợ".equals(payment) && !Boolean.TRUE.equals(data[17])) {
            return "Khách hàng không được phép ghi nợ.";
        }
        return "Hóa đơn chưa được thanh toán hoặc ghi nhận công nợ hợp lệ.";
    }

    private void confirm() {
        if (selected == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(view,
                "Xác nhận đã bàn giao xe cho khách?", "Xác nhận bàn giao",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            List<NhanVien> receptionists = nhanVienDao.findByViTri("Lễ tân");
            if (receptionists.isEmpty()) {
                throw new SQLException("Không có nhân viên lễ tân để ghi nhận bàn giao.");
            }
            dao.xacNhan(selected, receptionists.get(0).getMaNV(), "");
            JOptionPane.showMessageDialog(view, "Đã ghi nhận bàn giao.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(),
                    "Không thể bàn giao", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void print() {
        if (selected == null) {
            return;
        }
        try {
            Object[] data = dao.findChiTiet(selected);
            if (data == null) {
                return;
            }
            InPhieuXuatXuongDialog.ThongTinPhieu receipt =
                    new InPhieuXuatXuongDialog.ThongTinPhieu();
            receipt.maPhieuTNSC = selected;
            receipt.maPhieuBG = String.valueOf(data[14]).isEmpty()
                    ? "Chưa xác nhận bàn giao" : String.valueOf(data[14]);
            receipt.bienSo = String.valueOf(data[0]);
            receipt.xe = String.valueOf(data[1]);
            receipt.tenKhach = String.valueOf(data[2]);
            receipt.soDienThoai = String.valueOf(data[13]);
            receipt.tongThanhToan = money(data[8]);
            receipt.trangThaiHoaDon = String.valueOf(data[11]);
            java.sql.Timestamp deliveredAt = (java.sql.Timestamp) data[15];
            receipt.ngayBanGiao = deliveredAt == null
                    ? "Chưa xác nhận bàn giao"
                    : deliveredAt.toLocalDateTime().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            receipt.nhanVienBanGiao = data[16] == null ? "" : String.valueOf(data[16]);
            new InPhieuXuatXuongDialog(SwingUtilities.getWindowAncestor(view), receipt)
                    .setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể tải thông tin phiếu: " + ex.getMessage());
        }
    }

    private static String money(Object amount) {
        return String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0f đ",
                ((Number) amount).doubleValue()).replace(',', '.');
    }
}
