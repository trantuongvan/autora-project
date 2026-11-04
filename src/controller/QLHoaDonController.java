package controller;

import dao.HoaDon_Dao;
import entity.HoaDon;
import entity.PhienDangNhap;
import gui.QLHoaDon;
import util.UIComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Danh sách hóa đơn: lọc, chọn phiếu để lập hóa đơn, mở chi tiết, in và hủy. */
public class QLHoaDonController {
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final QLHoaDon view;
    private final DieuHuongHoaDon dieuHuong;
    private final HoaDon_Dao dao = new HoaDon_Dao();
    private final NumberFormat dinhDangTien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private int phienBanTai;

    public QLHoaDonController(QLHoaDon view, DieuHuongHoaDon dieuHuong) {
        this.view = view;
        this.dieuHuong = dieuHuong;
        initController();
        taiDuLieu();
    }

    private void initController() {
        view.getBtnTim().addActionListener(e -> taiDuLieu());
        view.getTxtTimKiem().addActionListener(e -> taiDuLieu());
        view.getTxtTuNgay().addActionListener(e -> taiDuLieu());
        view.getTxtDenNgay().addActionListener(e -> taiDuLieu());
        view.getCboTrangThai().addActionListener(e -> taiDuLieu());
        view.getCboHinhThuc().addActionListener(e -> taiDuLieu());
        view.getBtnLapHoaDon().addActionListener(e -> chonPhieuLapHoaDon());
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                xuLyClickBang(e);
            }
        });
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai", this::taiDuLieu);
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "tim",
                () -> view.getTxtTimKiem().requestFocusInWindow());
    }

    private void ganPhim(KeyStroke phim, String ten, Runnable viec) {
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(phim, ten);
        view.getActionMap().put(ten, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viec.run();
            }
        });
    }

    private void xuLyClickBang(MouseEvent e) {
        JTable bang = view.getTblDanhSach();
        int dong = bang.rowAtPoint(e.getPoint());
        if (dong < 0) {
            return;
        }
        int dongModel = bang.convertRowIndexToModel(dong);
        String ma = String.valueOf(view.getTableModel().getValueAt(dongModel, 0));
        int icon = UIComponents.viTriBieuTuong(bang, e, QLHoaDon.COT_THAO_TAC, 2);
        if (icon < 0 && e.getClickCount() == 2) {
            dieuHuong.moChiTietHoaDon(ma);
        } else if (icon == QLHoaDon.ICON_IN) {
            ChiTietHoaDonController.inHoaDon(view, ma);
        } else if (icon == QLHoaDon.ICON_HUY) {
            huyHoaDon(ma, String.valueOf(view.getTableModel().getValueAt(dongModel, QLHoaDon.COT_TRANG_THAI)));
        }
    }

    public void taiDuLieu() {
        LocalDate tuNgay;
        LocalDate denNgay;
        try {
            tuNgay = docNgay(view.getTxtTuNgay().getText());
            denNgay = docNgay(view.getTxtDenNgay().getText());
        } catch (DateTimeParseException ex) {
            view.hienLoiLoc("Ngày lọc phải đúng định dạng dd/MM/yyyy và là ngày có thật.");
            return;
        }
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            view.hienLoiLoc("Đến ngày không được trước từ ngày.");
            return;
        }
        view.hienLoiLoc(null);

        String tuKhoa = view.getTxtTimKiem().getText();
        String trangThai = giaTriLoc(view.getCboTrangThai());
        String hinhThuc = giaTriLoc(view.getCboHinhThuc());
        int phienBan = ++phienBanTai;
        new SwingWorker<List<HoaDon>, Void>() {
            @Override
            protected List<HoaDon> doInBackground() throws Exception {
                return dao.search(tuKhoa, trangThai, hinhThuc, tuNgay, denNgay);
            }

            @Override
            protected void done() {
                // Bỏ kết quả cũ khi người dùng đã lọc tiếp.
                if (phienBan != phienBanTai) {
                    return;
                }
                try {
                    view.setDanhSach(thanhDong(get()));
                } catch (Exception ex) {
                    view.setDanhSach(new ArrayList<>());
                    JOptionPane.showMessageDialog(view, "Không thể tải danh sách hóa đơn.\n" + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String giaTriLoc(JComboBox<String> cbo) {
        String chon = String.valueOf(cbo.getSelectedItem());
        return QLHoaDon.TAT_CA.equals(chon) ? "" : chon;
    }

    private static LocalDate docNgay(String chuoi) {
        String s = chuoi.trim();
        return s.isEmpty() ? null : LocalDate.parse(s, DINH_DANG_NGAY);
    }

    private List<Object[]> thanhDong(List<HoaDon> danhSach) {
        DateTimeFormatter ngay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Object[]> dong = new ArrayList<>();
        for (HoaDon h : danhSach) {
            dong.add(new Object[] {h.getMaHD(), h.getMaPhieuTNSC(), h.getTenKhachHang(), h.getBienSo(),
                h.getNgayLap() == null ? "—" : h.getNgayLap().format(ngay),
                tien(h.getTongTienCong()), tien(h.getTongTienPhuTung()), tien(h.getTongTienGiam()),
                tien(h.getTongTienVAT()), tien(h.getTongThanhToan()),
                h.getHinhThucThanhToan(), h.getTrangThaiThanhToan(), ""});
        }
        return dong;
    }

    private String tien(double soTien) {
        return dinhDangTien.format(soTien) + "đ";
    }

    private void chonPhieuLapHoaDon() {
        List<String[]> phieu;
        try {
            phieu = dao.findPhieuChoLap();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không thể tải danh sách phiếu.\n" + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (phieu.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Không có phiếu nào đã hoàn thành mà chưa có hóa đơn.",
                    "Lập hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] luaChon = new String[phieu.size()];
        for (int i = 0; i < luaChon.length; i++) {
            luaChon[i] = phieu.get(i)[0] + " — " + phieu.get(i)[1] + " — " + phieu.get(i)[2];
        }
        Object chon = JOptionPane.showInputDialog(view, "Chọn phiếu đã hoàn thành:", "Lập hóa đơn",
                JOptionPane.PLAIN_MESSAGE, null, luaChon, luaChon[0]);
        if (chon != null) {
            dieuHuong.moLapHoaDon(phieu.get(java.util.Arrays.asList(luaChon).indexOf(chon))[0]);
        }
    }

    private void huyHoaDon(String ma, String trangThai) {
        if (HoaDon.DA_HUY.equals(trangThai)) {
            JOptionPane.showMessageDialog(view, "Hóa đơn " + ma + " đã được hủy trước đó.");
            return;
        }
        String lyDo = JOptionPane.showInputDialog(view, "Lý do hủy hóa đơn " + ma + ":", "Hủy hóa đơn",
                JOptionPane.WARNING_MESSAGE);
        if (lyDo == null) {
            return;
        }
        try {
            dao.huy(ma, lyDo, PhienDangNhap.getHienTai().getTenDangNhap());
            taiDuLieu();
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể hủy hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }
}
