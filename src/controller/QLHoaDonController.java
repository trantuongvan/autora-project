package controller;

import dao.HoaDon_Dao;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiHoaDon;
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
    private boolean daTai;

    public QLHoaDonController(QLHoaDon view, DieuHuongHoaDon dieuHuong) {
        this.view = view;
        this.dieuHuong = dieuHuong;
        initController();
        UIComponents.goiYChuaTai(view.getTblDanhSach(), true);
    }

    private void initController() {
        view.getBtnTim().addActionListener(e -> taiDuLieu());
        view.getTxtTimKiem().addActionListener(e -> taiDuLieu());
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
            huyHoaDon(ma, (TrangThaiHoaDon) view.getTableModel().getValueAt(dongModel, QLHoaDon.COT_TRANG_THAI));
        }
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    public void taiLai() {
        if (daTai) {
            taiDuLieu();
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
        daTai = true;
        UIComponents.goiYChuaTai(view.getTblDanhSach(), false);

        String tuKhoa = view.getTxtTimKiem().getText();
        TrangThaiHoaDon trangThai = giaTriLoc(view.getCboTrangThai(), TrangThaiHoaDon.class);
        HinhThucThanhToan hinhThuc = giaTriLoc(view.getCboHinhThuc(), HinhThucThanhToan.class);
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

    /** Mục "Tất cả" không thuộc kiểu enum nên trả về null, nghĩa là không lọc. */
    private static <T> T giaTriLoc(JComboBox<Object> cbo, Class<T> loai) {
        Object chon = cbo.getSelectedItem();
        return loai.isInstance(chon) ? loai.cast(chon) : null;
    }

    private static LocalDate docNgay(String chuoi) {
        String s = chuoi.trim();
        return s.isEmpty() ? null : LocalDate.parse(s, DINH_DANG_NGAY);
    }

    private List<Object[]> thanhDong(List<HoaDon> danhSach) {
        DateTimeFormatter ngay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Object[]> dong = new ArrayList<>();
        for (HoaDon h : danhSach) {
            dong.add(new Object[] {h.getMaHD(), h.getPhieu().getMaPhieuTNSC(), h.getPhieu().getXe().getKhachHang().getHoTen(),
                h.getPhieu().getXe().getBienSo(),
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
        List<PhieuTiepNhanVaSuaChua> phieu;
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
            luaChon[i] = phieu.get(i).getMaPhieuTNSC() + " — " + phieu.get(i).getXe().getBienSo() + " — "
                    + phieu.get(i).getXe().getKhachHang().getHoTen();
        }
        Object chon = JOptionPane.showInputDialog(view, "Chọn phiếu đã hoàn thành:", "Lập hóa đơn",
                JOptionPane.PLAIN_MESSAGE, null, luaChon, luaChon[0]);
        if (chon != null) {
            dieuHuong.moLapHoaDon(phieu.get(java.util.Arrays.asList(luaChon).indexOf(chon)).getMaPhieuTNSC());
        }
    }

    private void huyHoaDon(String ma, TrangThaiHoaDon trangThai) {
        if (trangThai == TrangThaiHoaDon.DA_HUY) {
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
            taiLai();
        } catch (IllegalArgumentException | SQLException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể hủy hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }
}
