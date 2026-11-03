package controller;

import entity.PhienDangNhap;
import gui.ChiTietPhieuSuaChua;
import gui.DangNhap;
import gui.GiaoDienChinh;
import gui.QLBanGiaoXe;
import gui.QLHoSoXe;
import gui.QLKhuyenMai;
import gui.QLNhanVien;
import gui.QLNhatKy;
import gui.QLPhieuSuaChua;
import gui.QLTiepNhanSuaChua;
import gui.TrangChuLeTan;
import gui.TrangChuQuanLy;
import util.UIComponents;
import util.UIComponents.Loai;
import util.UIConstants;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Dựng menu theo vai trò, tạo màn hình khi mở lần đầu và điều hướng giữa các màn hình phiếu. */
public class GiaoDienChinhController implements DieuHuongPhieu {
    private static final String CARD_CHI_TIET = "CT_PHIEU";
    private static final String CARD_TIEP_NHAN = "QL_TIEP_NHAN";
    private static final String CARD_PHIEU = "QL_PHIEU";

    private static final Map<String, String> TIEU_DE = new HashMap<>();
    private static final Map<String, Loai> ICON = new HashMap<>();
    private static final Map<String, List<String>> MENU_THEO_VAI_TRO = new HashMap<>();

    static {
        muc("TRANG_CHU_QL", "Trang chủ", Loai.HOME);
        muc("TRANG_CHU_LT", "Trang chủ", Loai.HOME);
        muc("TRANG_CHU_KTV", "Trang chủ", Loai.HOME);
        muc("TRANG_CHU_KHO", "Trang chủ", Loai.HOME);
        muc(CARD_TIEP_NHAN, "Tiếp nhận xe", Loai.CAR);
        muc(CARD_PHIEU, "Phiếu sửa chữa", Loai.CLIPBOARD);
        muc("QL_KHACH_HANG", "Khách hàng", Loai.USERS);
        muc("QL_HO_SO_XE", "Hồ sơ xe", Loai.CAR);
        muc("QL_PHU_TUNG", "Phụ tùng & Kho", Loai.BOX);
        muc("QL_NHAP_KHO", "Nhập kho", Loai.DOWNLOAD);
        muc("QL_DUYET_XUAT", "Duyệt xuất kho", Loai.UPLOAD);
        muc("QL_HOA_DON", "Hóa đơn & Thanh toán", Loai.RECEIPT);
        muc("QL_BAN_GIAO", "Bàn giao xe", Loai.KEY);
        muc("QL_KHUYEN_MAI", "Khuyến mãi", Loai.PERCENT);
        muc("QL_THONG_KE", "Báo cáo thống kê", Loai.CHART);
        muc("QL_NHAN_VIEN", "Nhân viên & Tài khoản", Loai.USER);
        muc("QL_NHAT_KY", "Nhật ký hệ thống", Loai.HISTORY);
        muc("QL_TIEN_DO", "Tiến độ sửa chữa", Loai.WRENCH);

        MENU_THEO_VAI_TRO.put(PhienDangNhap.VAI_TRO_CHU_CUA_HANG, Arrays.asList(
                "TRANG_CHU_QL", CARD_TIEP_NHAN, CARD_PHIEU, "QL_KHACH_HANG", "QL_HO_SO_XE", "QL_PHU_TUNG",
                "QL_NHAP_KHO", "QL_DUYET_XUAT", "QL_HOA_DON", "QL_BAN_GIAO", "QL_KHUYEN_MAI", "QL_THONG_KE",
                "QL_NHAN_VIEN", "QL_NHAT_KY"));
        MENU_THEO_VAI_TRO.put(PhienDangNhap.VAI_TRO_LE_TAN, Arrays.asList(
                "TRANG_CHU_LT", CARD_TIEP_NHAN, CARD_PHIEU, "QL_KHACH_HANG", "QL_HO_SO_XE", "QL_HOA_DON", "QL_BAN_GIAO"));
        MENU_THEO_VAI_TRO.put(PhienDangNhap.VAI_TRO_KY_THUAT_VIEN, Arrays.asList("TRANG_CHU_KTV", "QL_TIEN_DO"));
        MENU_THEO_VAI_TRO.put(PhienDangNhap.VAI_TRO_THU_KHO, Arrays.asList(
                "TRANG_CHU_KHO", "QL_PHU_TUNG", "QL_NHAP_KHO", "QL_DUYET_XUAT"));
    }

    private static void muc(String card, String tieuDe, Loai icon) {
        TIEU_DE.put(card, tieuDe);
        ICON.put(card, icon);
    }

    private final GiaoDienChinh view;
    private final Map<String, Supplier<JPanel>> nhaMay = new HashMap<>();
    private final Set<String> cardDuocPhep = new HashSet<>();
    private final Set<String> cardDaTao = new HashSet<>();
    private QLTiepNhanSuaChua manHinhTiepNhan;
    private QLPhieuSuaChua manHinhPhieu;
    private JPanel manHinhChiTiet;

    public GiaoDienChinhController(GiaoDienChinh view) {
        this.view = view;
        initController();
    }

    private void initController() {
        // Màn hình chưa có ở đây sẽ hiện thông báo tạm; thành viên khác chỉ cần thêm một dòng nhaMay.put.
        nhaMay.put("TRANG_CHU_QL", () -> TrangChuQuanLy.createPanel(this));
        nhaMay.put("TRANG_CHU_LT", this::taoTrangChuLeTan);
        nhaMay.put("QL_HO_SO_XE", QLHoSoXe::createPanel);
        nhaMay.put(CARD_TIEP_NHAN, () -> {
            manHinhTiepNhan = (QLTiepNhanSuaChua) QLTiepNhanSuaChua.createPanel(this);
            return manHinhTiepNhan;
        });
        nhaMay.put(CARD_PHIEU, () -> {
            manHinhPhieu = (QLPhieuSuaChua) QLPhieuSuaChua.createPanel(this);
            return manHinhPhieu;
        });
        nhaMay.put("QL_BAN_GIAO", QLBanGiaoXe::createPanel);
        nhaMay.put("QL_NHAN_VIEN", QLNhanVien::createPanel);
        nhaMay.put("QL_KHUYEN_MAI", QLKhuyenMai::createPanel);
        nhaMay.put("QL_NHAT_KY", QLNhatKy::createPanel);

        view.getBtnDangXuat().addActionListener(e -> dangXuat());

        PhienDangNhap phien = PhienDangNhap.getHienTai();
        List<String> menu = phien == null ? null : MENU_THEO_VAI_TRO.get(phien.getVaiTro());
        if (menu == null) {
            JOptionPane.showMessageDialog(view, "Phiên đăng nhập hoặc vai trò không hợp lệ. Vui lòng đăng nhập lại.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cardDuocPhep.addAll(menu);
        for (String card : menu) {
            view.themMenu(card, TIEU_DE.get(card), ICON.get(card)).addActionListener(e -> moManHinh(card));
        }
        moManHinh(menu.get(0));
    }

    private JPanel taoTrangChuLeTan() {
        TrangChuLeTan panel = (TrangChuLeTan) TrangChuLeTan.createPanel();
        panel.setTenNguoiDung(PhienDangNhap.getHienTai().getHoTen());
        panel.setDieuHuongListener((card, maPhieu) -> moManHinh(card));
        return panel;
    }

    private void moManHinh(String card) {
        if (!cardDuocPhep.contains(card)) {
            JOptionPane.showMessageDialog(view, "Bạn không có quyền mở màn hình này.", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (cardDaTao.add(card)) {
                Supplier<JPanel> taoManHinh = nhaMay.get(card);
                view.themManHinh(card, taoManHinh == null ? taoManHinhTam(TIEU_DE.get(card)) : taoManHinh.get());
            }
            String tieuDe = "TRANG_CHU_QL".equals(card) ? "Trang chủ Quản lý"
                    : CARD_TIEP_NHAN.equals(card) ? "Tiếp nhận xe & Lập phiếu" : TIEU_DE.get(card);
            view.hienManHinh(card, tieuDe, card);
        } catch (RuntimeException ex) {
            cardDaTao.remove(card);
            JOptionPane.showMessageDialog(view, "Không thể mở màn hình " + TIEU_DE.get(card) + ".\n" + ex.getMessage(),
                    "Lỗi mở màn hình", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dangXuat() {
        int chon = JOptionPane.showConfirmDialog(view, "Bạn có chắc muốn đăng xuất?", "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (chon == JOptionPane.YES_OPTION) {
            PhienDangNhap.ketThuc();
            view.dispose();
            DangNhap.hienThi();
        }
    }

    private JPanel taoManHinhTam(String tieuDe) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.APP_BG);
        panel.add(UIComponents.label("Màn hình " + tieuDe + " đang được phát triển.",
                UIConstants.FONT_SECTION_TITLE, UIConstants.TEXT_MUTED));
        return panel;
    }

    // ==========================================================
    // DieuHuongPhieu
    // ==========================================================

    @Override
    public void moChiTiet(String maPhieu) {
        if (manHinhChiTiet != null) {
            view.xoaManHinh(manHinhChiTiet);
        }
        manHinhChiTiet = ChiTietPhieuSuaChua.createPanel(maPhieu, this);
        view.themManHinh(CARD_CHI_TIET, manHinhChiTiet);
        view.hienManHinh(CARD_CHI_TIET, "Chi tiết phiếu & chỉ định thợ", CARD_PHIEU);
    }

    @Override
    public void moDanhSach() {
        moManHinh(CARD_PHIEU);
        manHinhPhieu.getController().taiDanhSach();
    }

    @Override
    public void moTiepNhanMoi() {
        moManHinh(CARD_TIEP_NHAN);
    }

    @Override
    public void suaPhieu(String maPhieu) {
        moManHinh(CARD_TIEP_NHAN);
        manHinhTiepNhan.getController().moFormSua(maPhieu);
    }

    @Override
    public void moBaoCao() {
        moManHinh("QL_THONG_KE");
    }
}
