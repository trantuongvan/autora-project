package controller;

import dao.PhieuTiepNhanVaSuaChua_Dao;
import entity.PhienDangNhap;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import gui.QLPhieuSuaChua;
import util.UIComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/** Danh sách phiếu: tìm kiếm đơn giản và nâng cao, mở chi tiết, sửa, hủy. */
public class QLPhieuSuaChuaController {
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final QLPhieuSuaChua view;
    private final DieuHuongPhieu dieuHuong;
    private final PhieuTiepNhanVaSuaChua_Dao dao = new PhieuTiepNhanVaSuaChua_Dao();
    private int phienBanTai;
    private boolean daTai;

    public QLPhieuSuaChuaController(QLPhieuSuaChua view, DieuHuongPhieu dieuHuong) {
        this.view = view;
        this.dieuHuong = dieuHuong;
        initController();
        UIComponents.goiYChuaTai(view.getTblDanhSach(), true);
    }

    private void initController() {
        view.getTxtTimKiem().addActionListener(e -> taiDanhSach());
        view.getBtnLoc().addActionListener(e -> taiDanhSach());
        view.getBtnLamMoi().addActionListener(e -> {
            view.getTxtTimKiem().setText("");
            view.getCboLocTrangThai().setSelectedIndex(0);
            view.getTxtTuNgay().setText("");
            view.getTxtDenNgay().setText("");
        });
        view.getBtnTaoPhieu().addActionListener(e -> dieuHuong.moTiepNhanMoi());
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                xuLyClickBang(e);
            }
        });
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai", this::taiDanhSach);
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "tim",
                () -> view.getTxtTimKiem().requestFocusInWindow());
        view.getTblDanhSach().getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "xoa");
        view.getTblDanhSach().getActionMap().put("xoa", hanhDong(() -> {
            int dong = view.getTblDanhSach().getSelectedRow();
            if (dong >= 0) {
                huyPhieu(maTaiDong(dong));
            }
        }));
    }

    private void ganPhim(KeyStroke phim, String ten, Runnable viec) {
        view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(phim, ten);
        view.getActionMap().put(ten, hanhDong(viec));
    }

    private static Action hanhDong(Runnable viec) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viec.run();
            }
        };
    }

    private void xuLyClickBang(MouseEvent e) {
        JTable bang = view.getTblDanhSach();
        int dong = bang.rowAtPoint(e.getPoint());
        if (dong < 0) {
            return;
        }
        String ma = maTaiDong(dong);
        int icon = UIComponents.viTriBieuTuong(bang, e, QLPhieuSuaChua.COT_THAO_TAC, 2);
        if (icon < 0 && e.getClickCount() == 2) {
            dieuHuong.moChiTiet(ma);
        } else if (icon == QLPhieuSuaChua.ICON_SUA) {
            dieuHuong.suaPhieu(ma);
        } else if (icon == QLPhieuSuaChua.ICON_XOA) {
            huyPhieu(ma);
        }
    }

    private String maTaiDong(int dong) {
        int dongModel = view.getTblDanhSach().convertRowIndexToModel(dong);
        return String.valueOf(view.getTableModel().getValueAt(dongModel, 0));
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi danh sách đã được tải. */
    public void taiLai() {
        if (daTai) {
            taiDanhSach();
        }
    }

    public void taiDanhSach() {
        LocalDate tuNgay;
        LocalDate denNgay;
        try {
            tuNgay = docNgay(view.getTxtTuNgay().getText());
            denNgay = docNgay(view.getTxtDenNgay().getText());
        } catch (DateTimeParseException ex) {
            view.hienLoiLoc("Ngày lọc phải đúng định dạng dd/MM/yyyy.");
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
        Object chon = view.getCboLocTrangThai().getSelectedItem();
        TrangThaiPhieu trangThai = chon instanceof TrangThaiPhieu ? (TrangThaiPhieu) chon : null;
        int phienBan = ++phienBanTai;
        new SwingWorker<List<PhieuTiepNhanVaSuaChua>, Void>() {
            @Override
            protected List<PhieuTiepNhanVaSuaChua> doInBackground() throws Exception {
                return dao.search(tuKhoa, trangThai, tuNgay, denNgay);
            }

            @Override
            protected void done() {
                // Bỏ kết quả cũ khi người dùng đã gõ tiếp từ khóa khác.
                if (phienBan != phienBanTai) {
                    return;
                }
                try {
                    view.setDanhSach(thanhDong(get()));
                } catch (Exception ex) {
                    view.setDanhSach(new ArrayList<>());
                    JOptionPane.showMessageDialog(view, "Không thể tải danh sách phiếu.\n" + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private List<Object[]> thanhDong(List<PhieuTiepNhanVaSuaChua> danhSach) {
        List<Object[]> dong = new ArrayList<>();
        for (PhieuTiepNhanVaSuaChua p : danhSach) {
            dong.add(new Object[] {p.getMaPhieuTNSC(), p.getXe().getBienSo(), p.getXe().getKhachHang().getHoTen(), p.getTenXe(),
                p.getThoiGianTiepNhan() == null ? "—" : p.getThoiGianTiepNhan().format(DINH_DANG_GIO),
                p.getUuTien(), p.getTrangThai(), ""});
        }
        return dong;
    }

    private static LocalDate docNgay(String chuoi) {
        String s = chuoi.trim();
        return s.isEmpty() ? null : LocalDate.parse(s, DINH_DANG_NGAY);
    }

    private void huyPhieu(String ma) {
        int chon = JOptionPane.showConfirmDialog(view, "Hủy phiếu " + ma + "? Phiếu vẫn được lưu để đối soát.",
                "Xác nhận hủy phiếu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!dao.huy(ma, PhienDangNhap.getHienTai().getTenDangNhap())) {
                JOptionPane.showMessageDialog(view,
                        "Không thể hủy phiếu đã hủy hoặc đã có hóa đơn, phiếu bàn giao, yêu cầu xuất kho.",
                        "Không thể hủy", JOptionPane.WARNING_MESSAGE);
                return;
            }
            taiLai();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Không thể hủy phiếu.\n" + ex.getMessage(), "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
