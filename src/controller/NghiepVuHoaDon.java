package controller;

import dao.HoaDon_Dao;
import dao.Thue_Dao;
import entity.ChiTietSuaChua_DichVu;
import entity.ChiTietSuaChua_PhuTung;
import entity.HinhThucKhuyenMai;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.PhamViKhuyenMai;
import entity.PhieuTiepNhanVaSuaChua;
import entity.Thue;
import entity.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Nghiệp vụ hóa đơn: tính tiền công, phụ tùng, khuyến mãi, thuế và kiểm tra thanh toán.
 * Các màn hình hóa đơn, bàn giao và khuyến mãi dùng chung lớp này, nên không màn hình nào phải gọi sang màn hình khác.
 */
public final class NghiepVuHoaDon {

    private NghiepVuHoaDon() {
    }

    /** Làm tròn tới đồng; dùng BigDecimal để số kiểu 80,5 không bị double làm tròn xuống. */
    public static double phanTram(double coSo, double tyLe) {
        return BigDecimal.valueOf(coSo).multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).doubleValue();
    }

    public static double tienThue(Thue thue, double coSo) {
        return thue == null ? 0 : phanTram(coSo, thue.getThueSuat());
    }

    /** Phần tiền của hóa đơn mà khuyến mãi được áp dụng. */
    public static double coSoGiam(PhamViKhuyenMai phamVi, double tienCong, double tienPhuTung) {
        switch (phamVi) {
            case NHOM_DICH_VU:
                return tienCong;
            case PHU_TUNG:
                return tienPhuTung;
            default:
                return tienCong + tienPhuTung;
        }
    }

    /** Số tiền được giảm, không vượt quá phần tiền mà khuyến mãi áp dụng. */
    public static double tienGiam(KhuyenMai km, double tienCong, double tienPhuTung) {
        double coSo = coSoGiam(km.getPhamViApDung(), Math.round(tienCong), Math.round(tienPhuTung));
        double giam = km.getHinhThuc() == HinhThucKhuyenMai.PHAN_TRAM ? phanTram(coSo, km.getGiaTri()) : Math.round(km.getGiaTri());
        return Math.min(coSo, giam);
    }

    /** Khuyến mãi giảm nhiều tiền nhất; hòa thì giữ cái đứng trước; không cái nào giảm > 0 thì null. */
    public static KhuyenMai khuyenMaiTotNhat(List<KhuyenMai> hopLe, double tienCong, double tienPhuTung) {
        KhuyenMai tot = null;
        double giamMax = 0;
        for (KhuyenMai km : hopLe) {
            double giam = tienGiam(km, tienCong, tienPhuTung);
            if (giam > giamMax) {
                giamMax = giam;
                tot = km;
            }
        }
        return tot;
    }

    public static double thanhTien(ChiTietSuaChua_PhuTung dong) {
        return dong.getSoLuongSuDung() * dong.getGiaBanThucTe();
    }

    public static double tongTienCong(List<ChiTietSuaChua_DichVu> dichVu) {
        double tong = 0;
        for (ChiTietSuaChua_DichVu d : dichVu) {
            tong += d.getTienCongThucTe();
        }
        return tong;
    }

    public static double tongTienPhuTung(List<ChiTietSuaChua_PhuTung> phuTung) {
        double tong = 0;
        for (ChiTietSuaChua_PhuTung d : phuTung) {
            tong += thanhTien(d);
        }
        return tong;
    }

    /**
     * Hóa đơn mới với mọi khoản tiền đã tính, làm tròn tới đồng. Màn hình xem trước và lúc lưu dùng cùng hàm này.
     * Ghi nợ thì hóa đơn ở trạng thái Công nợ, còn lại là Đã thanh toán.
     */
    public static HoaDon tinhHoaDon(PhieuTiepNhanVaSuaChua phieu, KhuyenMai km, Thue thue, HinhThucThanhToan hinhThuc,
            double tienCong, double tienPhuTung) {
        if (tienCong < 0 || tienPhuTung < 0) {
            throw new IllegalArgumentException("Số tiền không được âm.");
        }
        double cong = Math.round(tienCong);
        double phuTung = Math.round(tienPhuTung);
        double giam = km == null ? 0 : tienGiam(km, cong, phuTung);
        double sauGiam = cong + phuTung - giam;
        double vat = tienThue(thue, sauGiam);
        TrangThaiHoaDon trangThai = hinhThuc == HinhThucThanhToan.CONG_NO ? TrangThaiHoaDon.CONG_NO : TrangThaiHoaDon.DA_THANH_TOAN;
        return new HoaDon(phieu, km, thue, hinhThuc, trangThai, cong, phuTung, giam, vat, sauGiam + vat);
    }

    /** Chỉ khách được duyệt mới được ghi nợ; các hình thức khác ai cũng dùng được. */
    public static boolean duocThanhToanBang(KhachHang khach, HinhThucThanhToan hinhThuc) {
        return hinhThuc != HinhThucThanhToan.CONG_NO || khach.isDuocGhiNo();
    }

    /** Chỉ tiền mặt cần khách đưa đủ tiền tại quầy. */
    public static boolean duTien(HinhThucThanhToan hinhThuc, Double tienKhachDua, double tongThanhToan) {
        return hinhThuc != HinhThucThanhToan.TIEN_MAT || tienKhachDua != null && tienKhachDua >= tongThanhToan;
    }

    public static boolean daThanhToan(HoaDon hoaDon) {
        return hoaDon.getTrangThaiThanhToan() == TrangThaiHoaDon.DA_THANH_TOAN;
    }

    public static boolean laCongNoHopLe(HoaDon hoaDon, KhachHang khach) {
        return hoaDon.getTrangThaiThanhToan() == TrangThaiHoaDon.CONG_NO && duocThanhToanBang(khach, HinhThucThanhToan.CONG_NO);
    }

    /** Đã trả đủ, hoặc còn nợ nhưng khách được phép ghi nợ. */
    public static boolean daThanhToanHopLe(HoaDon hoaDon, KhachHang khach) {
        return daThanhToan(hoaDon) || laCongNoHopLe(hoaDon, khach);
    }

    /** "Ngừng áp dụng", "Sắp diễn ra", "Hết hạn" hoặc "Đang áp dụng" tại ngày homNay. */
    public static String tinhTrangKhuyenMai(KhuyenMai km, LocalDate homNay) {
        if (!km.isTrangThai()) {
            return "Ngừng áp dụng";
        }
        if (homNay == null) {
            throw new IllegalArgumentException("Ngày xét tình trạng không được để trống.");
        }
        km.kiemTraHopLe();
        if (homNay.isBefore(km.getThoiGianBatDau())) {
            return "Sắp diễn ra";
        }
        if (homNay.isAfter(km.getThoiGianKetThuc())) {
            return "Hết hạn";
        }
        return "Đang áp dụng";
    }

    /**
     * Lập hóa đơn: đọc dữ liệu mới nhất của phiếu, tính tiền, kiểm tra quy tắc rồi giao DAO lưu.
     * tienKhachDua chỉ dùng cho tiền mặt. Trả về mã hóa đơn.
     */
    public static String lapHoaDon(String maPhieu, String maKM, String maThue, HinhThucThanhToan hinhThuc, Double tienKhachDua,
            String maNV) throws SQLException {
        if (maThue == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại thuế.");
        }
        HoaDon_Dao dao = new HoaDon_Dao();
        PhieuTiepNhanVaSuaChua phieu = dao.findThongTinPhieu(maPhieu);
        if (phieu == null) {
            throw new SQLException("Phiếu " + maPhieu + " không tồn tại.");
        }
        if (!duocThanhToanBang(phieu.getXe().getKhachHang(), hinhThuc)) {
            throw new IllegalArgumentException("Khách hàng của phiếu " + maPhieu + " không được phép ghi nợ.");
        }
        KhuyenMai km = null;
        if (maKM != null) {
            for (KhuyenMai k : dao.findKhuyenMaiHopLe()) {
                if (k.getMaKM().equals(maKM)) {
                    km = k;
                }
            }
            if (km == null) {
                throw new IllegalArgumentException("Khuyến mãi " + maKM + " đã hết hạn, ngừng áp dụng hoặc hết lượt.");
            }
        }
        Thue thue = null;
        for (Thue t : new Thue_Dao().findAll()) {
            if (t.getMaThue().equals(maThue)) {
                thue = t;
            }
        }
        if (thue == null) {
            throw new SQLException("Loại thuế " + maThue + " không tồn tại.");
        }
        HoaDon hoaDon = tinhHoaDon(phieu, km, thue, hinhThuc,
                tongTienCong(dao.findDichVu(maPhieu)), tongTienPhuTung(dao.findPhuTung(maPhieu)));
        if (!duTien(hinhThuc, tienKhachDua, hoaDon.getTongThanhToan())) {
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ tổng thanh toán.");
        }
        return dao.lapHoaDon(maPhieu, hoaDon, maNV);
    }

    /** Thu tiền hóa đơn công nợ bằng tiền mặt hoặc chuyển khoản. */
    public static void thuCongNo(String maHD, HinhThucThanhToan hinhThuc, Double tienKhachDua, String tenDangNhap)
            throws SQLException {
        if (hinhThuc != HinhThucThanhToan.TIEN_MAT && hinhThuc != HinhThucThanhToan.CHUYEN_KHOAN) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }
        HoaDon_Dao dao = new HoaDon_Dao();
        HoaDon hoaDon = dao.findById(maHD);
        if (hoaDon == null || hoaDon.getTrangThaiThanhToan() != TrangThaiHoaDon.CONG_NO) {
            throw new SQLException("Hóa đơn " + maHD + " không còn ở trạng thái Công nợ.");
        }
        if (!duTien(hinhThuc, tienKhachDua, hoaDon.getTongThanhToan())) {
            throw new IllegalArgumentException("Tiền khách đưa chưa đủ tổng thanh toán.");
        }
        dao.thanhToanCongNo(maHD, hinhThuc, tenDangNhap);
    }
}
