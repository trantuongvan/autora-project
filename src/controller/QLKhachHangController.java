package controller;

import dao.KhachHang_Dao;
import entity.KhachHang;
import entity.LoaiKhachHang;
import gui.QLKhachHang;
import util.UIComponents;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Điều phối màn hình khách hàng: tìm kiếm, thêm, sửa, xóa và tải xe, lịch sử của khách đang chọn. */
public class QLKhachHangController {
    private static final String LOI_BAT_BUOC = "Vui lòng nhập đầy đủ thông tin bắt buộc và Số điện thoại hợp lệ";
    private static final String KHONG_CO = "—";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QLKhachHang view;
    private final KhachHang_Dao dao = new KhachHang_Dao();
    private final NumberFormat dinhDangTien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    /** Dữ liệu đầy đủ của từng dòng trong bảng, cùng thứ tự với bảng. */
    private List<Object[]> dongHienTai = new ArrayList<>();
    private String maDangSua;
    private int phienBanDanhSach;
    private int phienBanLienKet;

    public QLKhachHangController(QLKhachHang view) {
        this.view = view;
        initController();
        moFormThem();
        taiDanhSach();
    }

    private void initController() {
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void removeUpdate(DocumentEvent e) { taiDanhSach(); }
            @Override public void changedUpdate(DocumentEvent e) { taiDanhSach(); }
        });
        view.getCboLocLoai().addActionListener(e -> taiDanhSach());
        view.getCboLocGhiNo().addActionListener(e -> taiDanhSach());
        view.getBtnTim().addActionListener(e -> taiDanhSach());
        view.getBtnThem().addActionListener(e -> {
            moFormThem();
            view.getTxtHoTen().requestFocusInWindow();
        });
        view.getBtnHuy().addActionListener(e -> moFormThem());
        view.getBtnLuu().addActionListener(e -> luu());
        view.getTblDanhSach().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                taiLienKet();
                // Danh sách tải lại sẽ chọn lại dòng đang sửa: giữ nguyên nội dung người dùng đang gõ.
                String ma = maDongChon();
                if (ma != null && !ma.equals(maDangSua)) {
                    dienForm(dongHienTai.get(view.getTblDanhSach().getSelectedRow()));
                }
            }
        });
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                xuLyClickBang(e);
            }
        });
        ganPhim(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai", this::taiDanhSach);
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
        if (dong >= 0 && UIComponents.viTriBieuTuong(bang, e, QLKhachHang.COT_THAO_TAC, 2) == QLKhachHang.ICON_XOA) {
            xoa((String) dongHienTai.get(dong)[0]);
        }
    }

    // ==========================================================
    // Tải dữ liệu
    // ==========================================================

    public void taiDanhSach() {
        String tuKhoa = view.getTxtTimKiem().getText();
        Object loai = view.getCboLocLoai().getSelectedItem();
        String ghiNo = String.valueOf(view.getCboLocGhiNo().getSelectedItem());
        LoaiKhachHang locLoai = loai instanceof LoaiKhachHang ? (LoaiKhachHang) loai : null;
        Boolean locGhiNo = QLKhachHang.TAT_CA.equals(ghiNo) ? null : Boolean.valueOf(QLKhachHang.DUOC_PHEP.equals(ghiNo));
        int phienBan = ++phienBanDanhSach;
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return dao.search(tuKhoa, locLoai, locGhiNo);
            }

            @Override
            protected void done() {
                // Bỏ kết quả cũ khi người dùng đã gõ tiếp từ khóa khác.
                if (phienBan != phienBanDanhSach) {
                    return;
                }
                try {
                    hienDanhSach(get());
                } catch (Exception ex) {
                    hienDanhSach(new ArrayList<>());
                    baoLoi("Không thể tải danh sách khách hàng.", ex);
                }
            }
        }.execute();
    }

    private void hienDanhSach(List<Object[]> danhSach) {
        String maDangChon = maDongChon();
        dongHienTai = danhSach;
        List<Object[]> dong = new ArrayList<>();
        for (Object[] r : danhSach) {
            dong.add(new Object[] {r[0], r[1], r[2], r[3], emailVaMaSoThue((String) r[4], (String) r[5]), r[6],
                (Boolean) r[7] ? QLKhachHang.DUOC_PHEP : QLKhachHang.KHONG, ((String) r[8]).isEmpty() ? KHONG_CO : r[8], ""});
        }
        view.setDanhSach(dong);
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i)[0].equals(maDangChon)) {
                view.getTblDanhSach().setRowSelectionInterval(i, i);
            }
        }
    }

    private static String emailVaMaSoThue(String email, String maSoThue) {
        if (maSoThue.isEmpty()) {
            return email.isEmpty() ? KHONG_CO : email;
        }
        return email.isEmpty() ? "MST: " + maSoThue : email + " / MST: " + maSoThue;
    }

    private String maDongChon() {
        int dong = view.getTblDanhSach().getSelectedRow();
        return dong < 0 || dong >= dongHienTai.size() ? null : (String) dongHienTai.get(dong)[0];
    }

    private void taiLienKet() {
        String ma = maDongChon();
        int phienBan = ++phienBanLienKet;
        if (ma == null) {
            view.setXeLienKet(new ArrayList<>());
            view.setLichSu(new ArrayList<>());
            return;
        }
        new SwingWorker<List<List<Object[]>>, Void>() {
            @Override
            protected List<List<Object[]>> doInBackground() throws Exception {
                List<List<Object[]>> ketQua = new ArrayList<>();
                ketQua.add(dao.xeLienKet(ma));
                ketQua.add(dao.lichSuSuaChua(ma));
                return ketQua;
            }

            @Override
            protected void done() {
                if (phienBan != phienBanLienKet) {
                    return;
                }
                try {
                    view.setXeLienKet(get().get(0));
                    view.setLichSu(thanhDongLichSu(get().get(1)));
                } catch (Exception ex) {
                    view.setXeLienKet(new ArrayList<>());
                    view.setLichSu(new ArrayList<>());
                    baoLoi("Không thể tải xe và lịch sử sửa chữa.", ex);
                }
            }
        }.execute();
    }

    private List<Object[]> thanhDongLichSu(List<Object[]> lichSu) {
        List<Object[]> dong = new ArrayList<>();
        for (Object[] r : lichSu) {
            dong.add(new Object[] {r[0], r[1] == null ? KHONG_CO : ((LocalDateTime) r[1]).format(DINH_DANG_NGAY),
                r[2] == null ? KHONG_CO : xuongDong((String) r[2]), r[3] == null ? KHONG_CO : dinhDangTien.format(r[3]) + " đ"});
        }
        return dong;
    }

    /** Bọc HTML để ô bảng tự xuống dòng; thoát ký tự vì tên dịch vụ có thể chứa &. */
    private static String xuongDong(String s) {
        return "<html>" + s.replace("&", "&amp;").replace("<", "&lt;") + "</html>";
    }

    // ==========================================================
    // Form
    // ==========================================================

    private void moFormThem() {
        maDangSua = null;
        view.getTblDanhSach().clearSelection();
        view.xoaLoi();
        view.getLblTieuDeForm().setText("Thêm khách hàng");
        view.getTxtMaKH().setText("Tự động cấp khi lưu");
        view.getTxtHoTen().setText("");
        view.getTxtSoDienThoai().setText("");
        view.getTxtDiaChi().setText("");
        view.getTxtEmail().setText("");
        view.getTxtMaSoThue().setText("");
        view.getCboLoai().setSelectedIndex(0);
        view.getCboGhiNo().setSelectedItem(QLKhachHang.KHONG);
    }

    private void dienForm(Object[] r) {
        maDangSua = (String) r[0];
        view.xoaLoi();
        view.getLblTieuDeForm().setText("Sửa thông tin khách hàng");
        view.getTxtMaKH().setText((String) r[0]);
        view.getTxtHoTen().setText((String) r[1]);
        view.getTxtSoDienThoai().setText((String) r[2]);
        view.getTxtDiaChi().setText((String) r[3]);
        view.getTxtEmail().setText((String) r[4]);
        view.getTxtMaSoThue().setText((String) r[5]);
        view.getCboLoai().setSelectedItem(r[6]);
        view.getCboGhiNo().setSelectedItem((Boolean) r[7] ? QLKhachHang.DUOC_PHEP : QLKhachHang.KHONG);
    }

    private void luu() {
        view.xoaLoi();
        KhachHang kh = new KhachHang();
        // Giới hạn độ dài theo cột trong CSDL; dùng & để mọi ô sai đều được bôi đỏ cùng lúc.
        boolean batBuocHopLe = kiemTra(view.getTxtHoTen(), view.getLblLoiHoTen(), 100, kh::setHoTen)
                & kiemTra(view.getTxtSoDienThoai(), view.getLblLoiSoDienThoai(), 15, kh::setSoDienThoai)
                & kiemTra(view.getTxtDiaChi(), view.getLblLoiDiaChi(), 255, kh::setDiaChi);
        boolean tuyChonHopLe = kiemTra(view.getTxtEmail(), view.getLblLoiEmail(), 100, kh::setEmail)
                & kiemTra(view.getTxtMaSoThue(), view.getLblLoiMaSoThue(), 50, kh::setMaSoThue);
        if (!batBuocHopLe) {
            UIComponents.showError(view.getLblLoiChung(), LOI_BAT_BUOC);
        }
        if (!batBuocHopLe || !tuyChonHopLe) {
            return;
        }
        kh.setLoaiKhachHang((LoaiKhachHang) view.getCboLoai().getSelectedItem());
        kh.setDuocGhiNo(QLKhachHang.DUOC_PHEP.equals(view.getCboGhiNo().getSelectedItem()));
        try {
            if (maDangSua == null) {
                dao.insert(kh);
            } else {
                kh.setMaKH(maDangSua);
                if (!dao.update(kh)) {
                    JOptionPane.showMessageDialog(view, "Khách hàng này không còn tồn tại.", "Không thể lưu",
                            JOptionPane.WARNING_MESSAGE);
                    taiDanhSach();
                    return;
                }
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            danhDauLoi(view.getTxtSoDienThoai(), view.getLblLoiSoDienThoai(), ex.getMessage());
            return;
        } catch (SQLException ex) {
            baoLoi("Không thể lưu khách hàng.", ex);
            return;
        }
        JOptionPane.showMessageDialog(view, "Thao tác thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        moFormThem();
        taiDanhSach();
    }

    private boolean kiemTra(JTextField o, JLabel lblLoi, int doDaiToiDa, Consumer<String> setter) {
        String giaTri = o.getText();
        if (giaTri.trim().length() > doDaiToiDa) {
            danhDauLoi(o, lblLoi, "Tối đa " + doDaiToiDa + " ký tự");
            return false;
        }
        try {
            setter.accept(giaTri);
            return true;
        } catch (IllegalArgumentException ex) {
            danhDauLoi(o, lblLoi, ex.getMessage());
            return false;
        }
    }

    private void danhDauLoi(JTextField o, JLabel lblLoi, String thongBao) {
        UIComponents.setError(o, true);
        UIComponents.showError(lblLoi, thongBao);
    }

    private void xoa(String ma) {
        int chon = JOptionPane.showConfirmDialog(view, "Xóa khách hàng " + ma + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!dao.delete(ma)) {
                JOptionPane.showMessageDialog(view,
                        "Không thể xóa khách hàng đang có xe liên kết. Hãy chuyển hoặc xóa xe tại màn hình Hồ sơ xe trước.",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                taiDanhSach();
                return;
            }
            if (ma.equals(view.getTxtMaKH().getText())) {
                moFormThem();
            }
            JOptionPane.showMessageDialog(view, "Thao tác thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            taiDanhSach();
        } catch (SQLException ex) {
            baoLoi("Không thể xóa khách hàng.", ex);
        }
    }

    private void baoLoi(String thongBao, Exception ex) {
        JOptionPane.showMessageDialog(view, thongBao + "\n" + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
