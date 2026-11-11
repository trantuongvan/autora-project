package controller;

import dao.ChiTietSuaChua_DichVu_Dao;
import dao.PhieuTiepNhanVaSuaChua_Dao;
import dao.TienDoSuaChua_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.HoaDon;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;

import dao.PhanCong_Dao;
import entity.DichVu;
import entity.KhachHang;
import entity.NhanVien;
import entity.Xe;
import util.PhanCongTuDong;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nghiệp vụ phiếu sửa chữa: luồng trạng thái, tiến độ và điều kiện bàn giao.
 * Màn hình tiếp nhận, tiến độ, bàn giao, hồ sơ xe và trang chủ dùng chung lớp này.
 */
public final class NghiepVuPhieu {

    private NghiepVuPhieu() {
    }

    /** Bước chuyển hợp lệ theo tiến trình thực tế; phiếu đã xong hoặc đã hủy không chuyển được nữa. */
    public static boolean coTheChuyenSang(TrangThaiPhieu cu, TrangThaiPhieu moi) {
        switch (cu) {
            case CHO_KIEM_TRA:
                return moi == TrangThaiPhieu.DANG_SUA || moi == TrangThaiPhieu.CHO_PHU_TUNG;
            case DANG_SUA:
                return moi == TrangThaiPhieu.CHO_PHU_TUNG || moi == TrangThaiPhieu.HOAN_THANH;
            case CHO_PHU_TUNG:
                return moi == TrangThaiPhieu.DANG_SUA;
            default:
                return false;
        }
    }

    /**
     * Trạng thái phiếu sau khi cập nhật hạng mục: xong hết thì Hoàn thành; còn việc và còn yêu cầu
     * phụ tùng chờ xuất thì vẫn Chờ phụ tùng; còn lại là Đang sửa.
     */
    public static TrangThaiPhieu trangThaiTheoTienDo(int soHangMucChuaXong, boolean conChoPhuTung) {
        if (soHangMucChuaXong == 0) {
            return TrangThaiPhieu.HOAN_THANH;
        }
        return conChoPhuTung ? TrangThaiPhieu.CHO_PHU_TUNG : TrangThaiPhieu.DANG_SUA;
    }

    /** Phiếu chỉ được đánh dấu hoàn thành khi có dịch vụ và mọi dịch vụ đã xong. */
    public static boolean coTheHoanThanh(PhieuTiepNhanVaSuaChua p) {
        if (p.getDanhSachDichVu().isEmpty()) {
            return false;
        }
        for (ChiTietSuaChua_DichVu d : p.getDanhSachDichVu()) {
            if (d.getTrangThaiTienDo() != TrangThaiTienDo.HOAN_THANH) {
                return false;
            }
        }
        return true;
    }

    /** Đếm hạng mục, số hạng mục xong và ghi tóm tắt tên dịch vụ của phiếu từ danh sách dịch vụ đã đọc. */
    public static void tomTatTienDo(PhieuTiepNhanVaSuaChua p) {
        int xong = 0;
        StringBuilder ten = new StringBuilder();
        for (ChiTietSuaChua_DichVu ct : p.getDanhSachDichVu()) {
            if (ct.getTrangThaiTienDo() == TrangThaiTienDo.HOAN_THANH) {
                xong++;
            }
            if (ten.length() > 0) {
                ten.append(", ");
            }
            ten.append(ct.getDichVu().getTenDichVu());
        }
        p.setTongSoHangMuc(p.getDanhSachDichVu().size());
        p.setSoHangMucHoanThanh(xong);
        p.setTomTatHangMuc(ten.length() > 0 ? ten.toString() : p.getYeuCauBanDau());
    }

    /** Phiếu được giao cho kỹ thuật viên (chuỗi rỗng: mọi phiếu), kèm tiến độ đã tóm tắt. */
    public static List<PhieuTiepNhanVaSuaChua> timPhieuDuocGiao(String maKyThuatVien, String tuKhoa, String trangThai)
            throws SQLException {
        List<PhieuTiepNhanVaSuaChua> ds = new TienDoSuaChua_Dao().searchPhieuĐượcGiao(maKyThuatVien, tuKhoa, trangThai);
        for (PhieuTiepNhanVaSuaChua p : ds) {
            tomTatTienDo(p);
        }
        return ds;
    }

    /** Phần trăm tiến độ của phiếu theo trạng thái và số hạng mục đã xong, dùng ở màn hình kỹ thuật viên. */
    public static int phanTramTienDo(PhieuTiepNhanVaSuaChua p) {
        int tong = p.getTongSoHangMuc();
        int xong = p.getSoHangMucHoanThanh();
        if (p.getTrangThai() == TrangThaiPhieu.HOAN_THANH) {
            return 100;
        }
        if (p.getTrangThai() == TrangThaiPhieu.CHO_KIEM_TRA) {
            return tong > 0 && xong > 0 ? Math.round((xong * 100f) / tong) : 0;
        }
        if (tong > 0) {
            int phanTram = Math.round((xong * 100f) / tong);
            return p.getTrangThai() == TrangThaiPhieu.DANG_SUA && phanTram == 0 ? 50 : phanTram;
        }
        return p.getTrangThai() == TrangThaiPhieu.DANG_SUA ? 50 : 0;
    }

    /** Làm tròn xuống: còn hạng mục chưa xong thì chưa đủ 100. Dùng cho bàn giao. */
    public static int phanTramHoanTat(PhieuTiepNhanVaSuaChua p) {
        return p.getTongSoHangMuc() == 0 ? 0 : p.getSoHangMucHoanThanh() * 100 / p.getTongSoHangMuc();
    }

    /** Nhãn "Chờ sửa" cho kỹ thuật viên; trong CSDL phiếu đó là "Chờ kiểm tra". */
    public static String trangThaiHienThiKTV(PhieuTiepNhanVaSuaChua p) {
        TrangThaiPhieu tt = p.getTrangThai();
        return tt == null || tt == TrangThaiPhieu.CHO_KIEM_TRA ? "Chờ sửa" : tt.getText();
    }

    /** Xe chỉ được bàn giao khi xong mọi hạng mục và hóa đơn đã trả hoặc được ghi nợ. */
    public static boolean duDieuKienBanGiao(PhieuTiepNhanVaSuaChua p) {
        return phanTramHoanTat(p) == 100 && p.getHoaDon() != null
                && NghiepVuHoaDon.daThanhToanHopLe(p.getHoaDon(), p.getXe().getKhachHang());
    }

    public static String trangThaiHoaDonHienThi(PhieuTiepNhanVaSuaChua p) {
        return p.getHoaDon() == null ? "Chưa lập hóa đơn" : p.getHoaDon().getTrangThaiThanhToan().getText();
    }

    public static String nhanHoaDonBanGiao(PhieuTiepNhanVaSuaChua p) {
        return p.getHoaDon() != null && NghiepVuHoaDon.laCongNoHopLe(p.getHoaDon(), p.getXe().getKhachHang())
                ? "Công nợ hợp lệ" : trangThaiHoaDonHienThi(p);
    }

    public static String lyDoChuaDuDieuKienBanGiao(PhieuTiepNhanVaSuaChua p) {
        int tienDo = phanTramHoanTat(p);
        if (tienDo < 100) {
            return tienDo == 0
                    ? "Chưa có hạng mục sửa chữa hoàn tất."
                    : "Còn " + (100 - tienDo) + "% hạng mục sửa chữa chưa hoàn tất.";
        }
        HoaDon hoaDon = p.getHoaDon();
        if (hoaDon == null) {
            return "Phiếu chưa có hóa đơn.";
        }
        if (hoaDon.getTrangThaiThanhToan() == TrangThaiHoaDon.DA_HUY) {
            return "Hóa đơn đã bị hủy.";
        }
        if (hoaDon.getTrangThaiThanhToan() == TrangThaiHoaDon.CONG_NO && !NghiepVuHoaDon.laCongNoHopLe(hoaDon, p.getXe().getKhachHang())) {
            return "Khách hàng không được phép ghi nợ.";
        }
        return "Hóa đơn chưa được thanh toán hoặc ghi nhận công nợ hợp lệ.";
    }

    /** Đổi trạng thái phiếu khi bước chuyển hợp lệ; false khi sai luồng, phiếu đã khóa hoặc đã đổi ở nơi khác. */
    public static boolean doiTrangThai(String maPhieu, TrangThaiPhieu cu, TrangThaiPhieu moi, String tenDangNhap)
            throws SQLException {
        return coTheChuyenSang(cu, moi) && new PhieuTiepNhanVaSuaChua_Dao().capNhatTrangThai(maPhieu, cu, moi, tenDangNhap);
    }

    /** Chọn kỹ thuật viên cho từng dịch vụ theo kỹ năng, ca và việc đang chờ của mỗi người. */
    public static PhanCongTuDong.KetQua chonKyThuatVien(LocalDateTime hienTai, List<DichVu> dichVu) throws SQLException {
        if (dichVu.isEmpty()) {
            return new PhanCongTuDong.KetQua();
        }
        PhanCong_Dao dao = new PhanCong_Dao();
        return new PhanCongTuDong(dao.docUngVien(dichVu), dao.docHangDoi()).chon(hienTai, dichVu);
    }

    /**
     * Lưu một lần tiếp nhận: tự phân công kỹ thuật viên, người phụ trách và giờ dự kiến (khi lễ tân không tự chọn),
     * rồi giao DAO lưu trong một transaction. Trả về kết quả phân công để báo lại cho lễ tân.
     */
    public static PhanCongTuDong.KetQua tiepNhan(KhachHang khachMoi, Xe xeMoi, PhieuTiepNhanVaSuaChua phieu,
            List<DichVu> dichVu) throws SQLException {
        LocalDateTime hienTai = phieu.getThoiGianTiepNhan() != null ? phieu.getThoiGianTiepNhan() : LocalDateTime.now();
        PhanCongTuDong.KetQua phanCong = chonKyThuatVien(hienTai, dichVu);
        LocalDateTime duKienGoc = phieu.getThoiGianDuKien();
        NhanVien phuTrachGoc = phieu.getNhanVienPhuTrach();
        if (duKienGoc == null) {
            phieu.setThoiGianDuKien(phanCong.getDuKien());
        }
        if (phanCong.getPhuTrach() != null) {
            phieu.setNhanVienPhuTrach(phanCong.getPhuTrach());
        }
        try {
            new PhieuTiepNhanVaSuaChua_Dao().taoTiepNhan(khachMoi, xeMoi, phieu, dichVu, phanCong);
        } catch (SQLException | RuntimeException ex) {
            // Lưu lỗi thì trả phiếu về như trước để lễ tân sửa và lưu lại.
            phieu.setThoiGianDuKien(duKienGoc);
            phieu.setNhanVienPhuTrach(phuTrachGoc);
            throw ex;
        }
        return phanCong;
    }

    /**
     * Cập nhật tiến độ một hạng mục và trạng thái phiếu theo tiến độ mới.
     * false khi phiếu đã có hóa đơn hoặc đã hủy (màn hình có thể đang hiện dữ liệu cũ).
     */
    public static boolean capNhatHangMuc(String maPhieu, String maDichVu, TrangThaiTienDo moi, String tenDangNhap)
            throws SQLException {
        List<ChiTietSuaChua_DichVu> dichVu = new ChiTietSuaChua_DichVu_Dao().findByPhieu(maPhieu);
        int chuaXong = 0;
        for (ChiTietSuaChua_DichVu d : dichVu) {
            TrangThaiTienDo tt = d.getDichVu().getMaDichVu().equals(maDichVu) ? moi : d.getTrangThaiTienDo();
            if (tt != TrangThaiTienDo.HOAN_THANH) {
                chuaXong++;
            }
        }
        TienDoSuaChua_Dao dao = new TienDoSuaChua_Dao();
        TrangThaiPhieu trangThaiPhieu = trangThaiTheoTienDo(chuaXong, dao.coYeuCauPhuTungCho(maPhieu));
        return dao.capNhatTienDoHangMuc(maPhieu, maDichVu, moi, trangThaiPhieu, tenDangNhap);
    }
}
