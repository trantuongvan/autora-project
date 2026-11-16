package controller;

import dao.NhatKy_Dao;
import dao.TaiKhoan_Dao;
import entity.NhanVien;
import entity.NhatKyHoatDong;
import util.PhienDangNhap;
import entity.TaiKhoan;
import entity.VaiTro;
import gui.QLTaiKhoan;
import util.UIComponents;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.Normalizer;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Điều phối tạo, sửa, khóa và xóa tài khoản; các quy tắc giữ Chủ cửa hàng cuối cùng nằm trong DAO. */
public class QLTaiKhoanController {
    private static final String LOI_BAT_BUOC = "Vui lòng nhập đầy đủ các trường thông tin bắt buộc";
    private static final String KHONG_TU_KHOA = "Không thể khóa hoặc xóa tài khoản đang đăng nhập.";
    private static final int SO_DONG_NHAT_KY = 8;
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final QLTaiKhoan view;
    private final TaiKhoan_Dao dao = new TaiKhoan_Dao();
    private final NhatKy_Dao nhatKyDao = new NhatKy_Dao();
    /** Cùng thứ tự với cboNhanVien ở chế độ tạo. */
    private List<NhanVien> dsNhanVien = new ArrayList<>();
    /** Cùng thứ tự với bảng tài khoản. */
    private List<TaiKhoan> dsTaiKhoan = new ArrayList<>();
    private TaiKhoan dangSua;
    private String tenGoiY = "";
    private int phienBanDanhSach;
    private int phienBanNhanVien;
    private boolean daTai;

    public QLTaiKhoanController(QLTaiKhoan view) {
        this.view = view;
        initController();
        moFormTao();
        view.getCboLocTrangThai().setSelectedItem(entity.TaiKhoan.DANG_HOAT_DONG);
        UIComponents.boLocMacDinhKhiTim(view.getTxtTimKiem(),
                () -> view.getCboLocTrangThai().setSelectedItem(QLTaiKhoan.TAT_CA_TRANG_THAI));
        UIComponents.tuTaiLai(this::taiTatCa, view.getTxtTimKiem(), view.getCboLocVaiTro(), view.getCboLocTrangThai());
        taiTatCa();
    }

    private void initController() {
        view.getTxtTimKiem().addActionListener(e -> taiTatCa());
        view.getBtnTaiDuLieu().addActionListener(e -> taiTatCa());
        view.getCboNhanVien().addActionListener(e -> theoViTriNhanVien());
        view.getBtnDong().addActionListener(e -> moFormTao());
        view.getBtnLuu().addActionListener(e -> luu());
        view.getTblDanhSach().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                xuLyClickBang(e);
            }
        });
        util.UIComponents.ganPhim(view, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "taiLai", () -> {
            taiTatCa();
            if (dangSua == null) {
                taiNhanVien();
            }
        });
        util.UIComponents.ganPhim(view, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "tim",
                () -> view.getTxtTimKiem().requestFocusInWindow());
    }

    private void xuLyClickBang(MouseEvent e) {
        JTable bang = view.getTblDanhSach();
        int dong = bang.rowAtPoint(e.getPoint());
        if (dong < 0 || dong >= dsTaiKhoan.size()) {
            return;
        }
        TaiKhoan taiKhoan = dsTaiKhoan.get(dong);
        int icon = UIComponents.viTriBieuTuong(bang, e, QLTaiKhoan.COT_THAO_TAC, 3);
        if (icon == QLTaiKhoan.ICON_SUA) {
            moFormSua(taiKhoan);
        } else if (icon == QLTaiKhoan.ICON_KHOA) {
            doiTrangThai(taiKhoan);
        } else if (icon == QLTaiKhoan.ICON_XOA) {
            xoa(taiKhoan);
        }
    }

    // ==========================================================
    // Tải dữ liệu
    // ==========================================================

    private void taiTatCa() {
        daTai = true;
        UIComponents.goiYChuaTai(view.getTblDanhSach(), false);
        UIComponents.goiYChuaTai(view.getTblNhatKy(), false);
        taiDanhSach();
        taiNhatKy();
    }

    /** Sau khi dữ liệu đổi: chỉ làm mới khi đã tải. */
    private void taiLai() {
        if (daTai) {
            taiDanhSach();
            taiNhatKy();
        }
    }

    private void taiDanhSach() {
        String tuKhoa = view.getTxtTimKiem().getText();
        Object chonVaiTro = view.getCboLocVaiTro().getSelectedItem();
        VaiTro vaiTro = chonVaiTro instanceof VaiTro ? (VaiTro) chonVaiTro : null;
        Object chonTrangThai = view.getCboLocTrangThai().getSelectedItem();
        Boolean trangThai = QLTaiKhoan.TAT_CA_TRANG_THAI.equals(chonTrangThai)
                ? null : Boolean.valueOf(TaiKhoan.DANG_HOAT_DONG.equals(chonTrangThai));
        int phienBan = ++phienBanDanhSach;
        new SwingWorker<List<TaiKhoan>, Void>() {
            @Override
            protected List<TaiKhoan> doInBackground() throws Exception {
                return dao.search(tuKhoa, vaiTro, trangThai);
            }

            @Override
            protected void done() {
                // Bỏ kết quả cũ khi người dùng đã gõ tiếp từ khóa khác.
                if (phienBan != phienBanDanhSach) {
                    return;
                }
                try {
                    dsTaiKhoan = get();
                } catch (Exception ex) {
                    dsTaiKhoan = new ArrayList<>();
                    baoLoi("Không thể tải danh sách tài khoản.", ex);
                }
                List<Object[]> dong = new ArrayList<>();
                for (TaiKhoan t : dsTaiKhoan) {
                    dong.add(new Object[] {t.getTenDangNhap(), xuongDong(t.getNhanVien().getMaNV() + " — " + t.getNhanVien().getHoTen()), xuongDong(t.getVaiTro().getText()),
                        t.getTrangThaiHienThi(), ""});
                }
                view.setDanhSach(dong);
            }
        }.execute();
    }

    private void taiNhatKy() {
        new SwingWorker<List<NhatKyHoatDong>, Void>() {
            @Override
            protected List<NhatKyHoatDong> doInBackground() throws Exception {
                return nhatKyDao.search("");
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> dong = new ArrayList<>();
                    for (NhatKyHoatDong r : get()) {
                        if (dong.size() == SO_DONG_NHAT_KY) {
                            break;
                        }
                        dong.add(new Object[] {r.getTenDangNhap(), xuongDong(r.getThoiGian().format(DINH_DANG_GIO)),
                            xuongDong(r.getHanhDong() + " " + r.getDoiTuong()),
                            xuongDong(truocSau(r.getGiaTriCu(), r.getGiaTriMoi()))});
                    }
                    view.setNhatKy(dong);
                } catch (Exception ex) {
                    view.setNhatKy(new ArrayList<>());
                    baoLoi("Không thể tải nhật ký hoạt động.", ex);
                }
            }
        }.execute();
    }

    /** Bọc HTML để ô bảng tự xuống dòng; thoát ký tự vì nội dung là dữ liệu người dùng nhập. */
    private static String xuongDong(String s) {
        return "<html>" + s.replace("&", "&amp;").replace("<", "&lt;") + "</html>";
    }

    private static String truocSau(String cu, String moi) {
        if (cu == null && moi == null) {
            return "—";
        }
        return (cu == null ? "—" : cu) + " → " + (moi == null ? "—" : moi);
    }

    private void taiNhanVien() {
        int phienBan = ++phienBanNhanVien;
        new SwingWorker<List<NhanVien>, Void>() {
            @Override
            protected List<NhanVien> doInBackground() throws Exception {
                return dao.nhanVienChuaCoTaiKhoan();
            }

            @Override
            protected void done() {
                // Người dùng đã chuyển sang sửa thì combo đang hiện nhân viên của tài khoản đó.
                if (phienBan != phienBanNhanVien || dangSua != null) {
                    return;
                }
                try {
                    dsNhanVien = get();
                } catch (Exception ex) {
                    dsNhanVien = new ArrayList<>();
                    baoLoi("Không thể tải danh sách nhân viên.", ex);
                }
                JComboBox<String> cbo = view.getCboNhanVien();
                cbo.removeAllItems();
                for (NhanVien nv : dsNhanVien) {
                    cbo.addItem(nv.getMaNV() + " — " + nv.getHoTen() + " — " + nv.getViTriLamViec());
                }
                theoViTriNhanVien();
            }
        }.execute();
    }

    private void theoViTriNhanVien() {
        int chon = view.getCboNhanVien().getSelectedIndex();
        if (dangSua == null && chon >= 0 && chon < dsNhanVien.size()) {
            view.getCboVaiTro().setSelectedItem(dsNhanVien.get(chon).getViTriLamViec());
            goiYTenDangNhap(dsNhanVien.get(chon));
        }
    }

    /** Chỉ thay ô tên đăng nhập khi trống hoặc còn đúng giá trị tự gợi ý lần trước, để không đè tên người dùng đã gõ. */
    private void goiYTenDangNhap(NhanVien nv) {
        String hienTai = view.getTxtTenDangNhap().getText();
        if (!hienTai.isEmpty() && !hienTai.equals(tenGoiY)) {
            return;
        }
        try {
            String goc = taoTenDangNhap(nv.getHoTen());
            tenGoiY = goc.isEmpty() ? "" : tenDangNhapConTrong(goc);
            view.getTxtTenDangNhap().setText(tenGoiY);
        } catch (SQLException ex) {
            baoLoi("Không thể gợi ý tên đăng nhập.", ex);
        }
    }

    /** "Nguyễn Tuấn Vũ" -> "vunt": tên gọi cộng chữ cái đầu các từ còn lại, chữ thường, không dấu. */
    /** Tên gốc nếu còn trống, nếu không thì thêm số nhỏ nhất từ 2; cắt gốc để không vượt VARCHAR(50). */
    public static String tenDangNhapConTrong(String goc) throws java.sql.SQLException {
        java.util.Set<String> daCo = new TaiKhoan_Dao().tenDangNhapBatDauBang(goc);
        String ung = goc;
        for (int n = 2; daCo.contains(ung.toLowerCase(Locale.ROOT)); n++) {
            String duoi = String.valueOf(n);
            ung = goc.substring(0, Math.min(goc.length(), 50 - duoi.length())) + duoi;
        }
        return ung;
    }

    public static String taoTenDangNhap(String hoTen) {
        if (hoTen == null) {
            return "";
        }
        String[] tu = Normalizer.normalize(hoTen.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", "").trim().split("\\s+");
        if (tu[0].isEmpty()) {
            return "";
        }
        StringBuilder ten = new StringBuilder(tu[tu.length - 1]);
        for (int i = 0; i < tu.length - 1; i++) {
            ten.append(tu[i].charAt(0));
        }
        // Tên đăng nhập cần ít nhất 3 ký tự, ví dụ "An" thành "an01".
        if (ten.length() < 3) {
            ten.append("01");
        }
        return ten.toString();
    }

    // ==========================================================
    // Form
    // ==========================================================

    private void moFormTao() {
        dangSua = null;
        view.xoaLoi();
        view.setCheDoSua(false);
        // Xóa ngay để không lưu nhầm theo danh sách cũ trong lúc danh sách mới đang tải.
        dsNhanVien = new ArrayList<>();
        view.getCboNhanVien().removeAllItems();
        view.getTxtTenDangNhap().setText("");
        tenGoiY = "";
        view.getPwdMatKhau().setText("");
        view.getCboTrangThai().setSelectedItem(TaiKhoan.DANG_HOAT_DONG);
        taiNhanVien();
    }

    private void moFormSua(TaiKhoan taiKhoan) {
        dangSua = taiKhoan;
        view.xoaLoi();
        view.setCheDoSua(true);
        view.getCboNhanVien().removeAllItems();
        view.getCboNhanVien().addItem(taiKhoan.getNhanVien().getMaNV() + " — " + taiKhoan.getNhanVien().getHoTen());
        view.getTxtTenDangNhap().setText(taiKhoan.getTenDangNhap());
        view.getPwdMatKhau().setText("");
        view.getCboVaiTro().setSelectedItem(taiKhoan.getVaiTro());
        view.getCboTrangThai().setSelectedItem(taiKhoan.getTrangThaiHienThi());
    }

    private void luu() {
        view.xoaLoi();
        VaiTro vaiTro = (VaiTro) view.getCboVaiTro().getSelectedItem();
        boolean hoatDong = TaiKhoan.DANG_HOAT_DONG.equals(view.getCboTrangThai().getSelectedItem());
        String matKhau = new String(view.getPwdMatKhau().getPassword());
        try {
            if (!(dangSua == null ? luuMoi(vaiTro, hoatDong, matKhau) : luuSua(vaiTro, hoatDong, matKhau))) {
                return;
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            if (TaiKhoan_Dao.TRUNG_TEN_DANG_NHAP.equals(ex.getMessage())) {
                danhDauLoi(view.getTxtTenDangNhap(), view.getLblLoiTenDangNhap(), ex.getMessage());
            } else {
                danhDauLoi(view.getCboNhanVien(), view.getLblLoiNhanVien(), ex.getMessage());
                taiNhanVien();
            }
            return;
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể lưu", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException ex) {
            baoLoi("Không thể lưu tài khoản.", ex);
            return;
        }
        JOptionPane.showMessageDialog(view, "Thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        moFormTao();
        taiLai();
    }

    /** Trả về false khi dữ liệu nhập sai và đã được đánh dấu trên form. */
    private boolean luuMoi(VaiTro vaiTro, boolean hoatDong, String matKhau) throws SQLException {
        String ten = view.getTxtTenDangNhap().getText();
        int chonNhanVien = view.getCboNhanVien().getSelectedIndex();
        boolean thieu = false;
        if (chonNhanVien < 0 || chonNhanVien >= dsNhanVien.size()) {
            danhDauLoi(view.getCboNhanVien(), view.getLblLoiNhanVien(), "Không còn nhân viên nào chưa có tài khoản");
            thieu = true;
        }
        if (ten.trim().isEmpty()) {
            danhDauLoi(view.getTxtTenDangNhap(), view.getLblLoiTenDangNhap(), LOI_BAT_BUOC);
            thieu = true;
        }
        if (matKhau.isEmpty()) {
            danhDauLoi(view.getPwdMatKhau(), view.getLblLoiMatKhau(), LOI_BAT_BUOC);
            thieu = true;
        }
        TaiKhoan taiKhoan = new TaiKhoan();
        boolean tenHopLe = !ten.trim().isEmpty() && kiemTraTen(taiKhoan, ten);
        boolean matKhauHopLe = !matKhau.isEmpty() && kiemTraMatKhau(matKhau);
        if (thieu || !tenHopLe || !matKhauHopLe) {
            return false;
        }
        NhanVien nv = dsNhanVien.get(chonNhanVien);
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan.setTrangThai(hoatDong);
        taiKhoan.setNhanVien(nv);
        dao.insert(taiKhoan, matKhau, nguoiThucHien());
        return true;
    }

    /** Trả về false khi đã báo lý do cho người dùng và không cần báo thành công. */
    private boolean luuSua(VaiTro vaiTro, boolean hoatDong, String matKhau) throws SQLException {
        if (!matKhau.isEmpty() && !kiemTraMatKhau(matKhau)) {
            return false;
        }
        if (!hoatDong && laTaiKhoanDangDangNhap(dangSua)) {
            JOptionPane.showMessageDialog(view, KHONG_TU_KHOA, "Không thể lưu", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!dao.capNhat(dangSua.getTenDangNhap(), vaiTro, matKhau.isEmpty() ? null : matKhau, hoatDong, nguoiThucHien())) {
            JOptionPane.showMessageDialog(view, "Tài khoản này không còn tồn tại.", "Không thể lưu", JOptionPane.WARNING_MESSAGE);
            moFormTao();
            taiLai();
            return false;
        }
        return true;
    }

    private boolean kiemTraTen(TaiKhoan taiKhoan, String ten) {
        try {
            taiKhoan.setTenDangNhap(ten);
            return true;
        } catch (IllegalArgumentException ex) {
            danhDauLoi(view.getTxtTenDangNhap(), view.getLblLoiTenDangNhap(), ex.getMessage());
            return false;
        }
    }

    private boolean kiemTraMatKhau(String matKhau) {
        try {
            TaiKhoan.kiemTraMatKhau(matKhau);
            return true;
        } catch (IllegalArgumentException ex) {
            danhDauLoi(view.getPwdMatKhau(), view.getLblLoiMatKhau(), ex.getMessage());
            return false;
        }
    }

    private void danhDauLoi(JComponent o, JLabel lblLoi, String thongBao) {
        UIComponents.setError(o, true);
        UIComponents.showError(lblLoi, thongBao);
    }

    // ==========================================================
    // Khóa / Xóa
    // ==========================================================

    private void doiTrangThai(TaiKhoan taiKhoan) {
        boolean seKhoa = taiKhoan.isTrangThai();
        if (seKhoa && laTaiKhoanDangDangNhap(taiKhoan)) {
            JOptionPane.showMessageDialog(view, KHONG_TU_KHOA, "Không thể khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String hanhDong = seKhoa ? "Khóa" : "Mở khóa";
        if (!xacNhan(hanhDong + " tài khoản " + taiKhoan.getTenDangNhap() + "?")) {
            return;
        }
        thucHien(hanhDong + " tài khoản", () -> dao.datTrangThai(taiKhoan.getTenDangNhap(), !seKhoa, nguoiThucHien()));
    }

    private void xoa(TaiKhoan taiKhoan) {
        if (laTaiKhoanDangDangNhap(taiKhoan)) {
            JOptionPane.showMessageDialog(view, KHONG_TU_KHOA, "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!xacNhan("Xóa tài khoản " + taiKhoan.getTenDangNhap() + "? Nhân viên vẫn được giữ lại.")) {
            return;
        }
        thucHien("Xóa tài khoản", () -> dao.delete(taiKhoan.getTenDangNhap(), nguoiThucHien()));
    }

    private interface ThaoTac {
        boolean chay() throws SQLException;
    }

    private void thucHien(String ten, ThaoTac thaoTac) {
        try {
            if (!thaoTac.chay()) {
                JOptionPane.showMessageDialog(view, "Tài khoản này không còn tồn tại.", ten, JOptionPane.WARNING_MESSAGE);
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), ten, JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            baoLoi("Không thể thực hiện: " + ten + ".", ex);
        }
        moFormTao();
        taiLai();
    }

    private boolean xacNhan(String cauHoi) {
        return JOptionPane.showConfirmDialog(view, cauHoi, "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private static boolean laTaiKhoanDangDangNhap(TaiKhoan taiKhoan) {
        return taiKhoan.getTenDangNhap().equalsIgnoreCase(nguoiThucHien());
    }

    private static String nguoiThucHien() {
        return PhienDangNhap.getHienTai().getTenDangNhap();
    }

    private void baoLoi(String thongBao, Exception ex) {
        JOptionPane.showMessageDialog(view, thongBao + "\n" + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
