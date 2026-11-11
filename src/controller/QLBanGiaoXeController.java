package controller;

import dao.PhieuBanGiao_Dao;
import entity.HoaDon;
import entity.PhieuTiepNhanVaSuaChua;
import gui.InPhieuXuatXuongDialog;
import gui.QLBanGiaoXe;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Bàn giao chỉ được xác nhận khi dịch vụ hoàn tất và hóa đơn hợp lệ. */
public class QLBanGiaoXeController {

    private final QLBanGiaoXe view;
    private final PhieuBanGiao_Dao dao = new PhieuBanGiao_Dao();
    private String selected;
    private int loadVersion;
    private boolean loadErrorShown;
    private boolean daTai;

    public QLBanGiaoXeController(QLBanGiaoXe view) {
        this.view = view;
        initController();
        util.UIComponents.tuTaiLai(this::load, view.getTxtTimKiem(), view.getCboLoc());
        load();
    }

    private void initController() {
        view.getTxtTimKiem().addActionListener(e -> load());
        view.getBtnTaiDuLieu().addActionListener(e -> load());
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
        daTai = true;
        util.UIComponents.goiYChuaTai(view.getTblDanhSach(), false);
        final int version = ++loadVersion;
        final String query = view.getTxtTimKiem().getText();
        final String filter = String.valueOf(view.getCboLoc().getSelectedItem());
        new SwingWorker<List<PhieuTiepNhanVaSuaChua>, Void>() {
            @Override
            protected List<PhieuTiepNhanVaSuaChua> doInBackground() throws Exception {
                return dao.findDanhSach(query, filter);
            }

            @Override
            protected void done() {
                if (version != loadVersion) {
                    return;
                }
                try {
                    List<Object[]> rows = thanhDong(get());
                    view.setDanhSach(rows);
                    loadErrorShown = false;
                    view.anThongBaoThanhCong();
                    view.setCanhBaoDieuKien(null);
                    if (rows.isEmpty()) {
                        view.anChiTiet();
                        selected = null;
                    } else {
                        view.getTblDanhSach().setRowSelectionInterval(0, 0);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showLoadError(ex);
                } catch (ExecutionException ex) {
                    showLoadError(ex.getCause() == null ? ex : ex.getCause());
                }
            }
        }.execute();
    }

    private void showLoadError(Throwable error) {
        if (loadErrorShown) {
            return;
        }
        loadErrorShown = true;
        String message = error.getMessage() == null
                ? error.getClass().getSimpleName() : error.getMessage();
        JOptionPane.showMessageDialog(view,
                "Không thể tải danh sách bàn giao: " + message,
                "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static List<Object[]> thanhDong(List<PhieuTiepNhanVaSuaChua> dsPhieu) {
        List<Object[]> rows = new ArrayList<>();
        for (PhieuTiepNhanVaSuaChua p : dsPhieu) {
            rows.add(new Object[]{
                    p.getMaPhieuTNSC(),
                    p.getXe().getBienSo(),
                    p.getXe().getKhachHang().getHoTen(),
                    p.getTenXe(),
                    NghiepVuPhieu.phanTramHoanTat(p) + "%",
                    NghiepVuPhieu.nhanHoaDonBanGiao(p),
                    money(p.getHoaDon() == null ? 0 : p.getHoaDon().getTongThanhToan()),
                    NghiepVuPhieu.duDieuKienBanGiao(p) ? "Đủ điều kiện • Bàn giao" : "Chưa đủ điều kiện"
            });
        }
        return rows;
    }

    private void detail(String id) {
        try {
            PhieuTiepNhanVaSuaChua p = dao.findChiTiet(id);
            if (p == null) {
                return;
            }
            HoaDon hoaDon = p.getHoaDon() == null ? new HoaDon() : p.getHoaDon();
            double taxRate = hoaDon.getThue() == null ? 0 : hoaDon.getThue().getThueSuat();
            QLBanGiaoXe.ChiTietBanGiao detail = new QLBanGiaoXe.ChiTietBanGiao();
            detail.xe = p.getXe().getBienSo() + " • " + p.getTenXe();
            detail.chuXeVaPhieu = p.getXe().getKhachHang().getHoTen() + " • " + id;
            detail.nhanTienDo = "Hoàn tất " + NghiepVuPhieu.phanTramHoanTat(p) + "%";
            detail.hoanTat100 = NghiepVuPhieu.phanTramHoanTat(p) == 100;
            detail.tienCong = money(hoaDon.getTongTienCong());
            detail.phuTung = money(hoaDon.getTongTienPhuTung());
            detail.giamGia = "− " + money(hoaDon.getTongTienGiam());
            detail.nhanVAT = String.format(
                    java.util.Locale.forLanguageTag("vi-VN"), "VAT (%.0f%%)", taxRate);
            detail.vat = money(hoaDon.getTongTienVAT());
            detail.tongThanhToan = money(hoaDon.getTongThanhToan());
            detail.duDieuKien = NghiepVuPhieu.duDieuKienBanGiao(p);
            detail.daBanGiao = p.getBanGiao() != null;
            if (detail.daBanGiao) {
                detail.thongBao = "Phiếu đã bàn giao, có thể xem và in lại phiếu xuất xưởng.";
            } else if (detail.duDieuKien) {
                detail.thongBao = hoaDon.getMaHD() + " đã xử lý. Đủ điều kiện bàn giao.";
            } else {
                detail.thongBao = NghiepVuPhieu.lyDoChuaDuDieuKienBanGiao(p);
            }
            view.hienChiTiet(detail);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view,
                    "Không thể tải chi tiết phiếu: " + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
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
            // Ghi người đang đăng nhập, không phải lễ tân đầu tiên trong CSDL.
            dao.xacNhan(selected, util.PhienDangNhap.getHienTai().getMaNV(), "");
            JOptionPane.showMessageDialog(view, "Đã ghi nhận bàn giao.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (daTai) load();
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
            PhieuTiepNhanVaSuaChua p = dao.findChiTiet(selected);
            if (p == null) {
                return;
            }
            InPhieuXuatXuongDialog.ThongTinPhieu receipt =
                    new InPhieuXuatXuongDialog.ThongTinPhieu();
            receipt.maPhieuTNSC = selected;
            receipt.maPhieuBG = p.getBanGiao() == null
                    ? "Chưa xác nhận bàn giao" : p.getBanGiao().getMaPhieuBG();
            receipt.bienSo = p.getXe().getBienSo();
            receipt.xe = p.getTenXe();
            receipt.tenKhach = p.getXe().getKhachHang().getHoTen();
            receipt.soDienThoai = p.getXe().getKhachHang().getSoDienThoai();
            receipt.tongThanhToan = money(p.getHoaDon() == null ? 0 : p.getHoaDon().getTongThanhToan());
            receipt.trangThaiHoaDon = NghiepVuPhieu.trangThaiHoaDonHienThi(p);
            receipt.ngayBanGiao = p.getBanGiao() == null || p.getBanGiao().getNgayBanGiao() == null
                    ? "Chưa xác nhận bàn giao"
                    : p.getBanGiao().getNgayBanGiao().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            receipt.nhanVienBanGiao = p.getBanGiao() == null ? "" : p.getBanGiao().getNhanVienBanGiao().getHoTen();
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
