package dao;

import connection.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Truy vấn số liệu tổng hợp cho trang chủ phân hệ Quản lý.
 * Mọi mốc thời gian đều truyền dưới dạng tham số (nửa khoảng [từ, đến)) để không bọc hàm lên cột ngayThanhToan.
 */
public class TrangChuQuanLy_Dao {
    /** Doanh thu đã thu trong một ngày, tổng hợp từ hóa đơn nên chỉ đọc. */
    public static class DoanhThuNgay {
        private final LocalDate ngay;
        private final int soPhieu;
        private final double tienCong;
        private final double tienPhuTung;
        private final double tienVAT;
        private final double tongTien;

        public DoanhThuNgay(LocalDate ngay, int soPhieu, double tienCong, double tienPhuTung, double tienVAT, double tongTien) {
            this.ngay = ngay;
            this.soPhieu = soPhieu;
            this.tienCong = tienCong;
            this.tienPhuTung = tienPhuTung;
            this.tienVAT = tienVAT;
            this.tongTien = tongTien;
        }

        public LocalDate getNgay() {
            return ngay;
        }

        public int getSoPhieu() {
            return soPhieu;
        }

        public double getTienCong() {
            return tienCong;
        }

        public double getTienPhuTung() {
            return tienPhuTung;
        }

        public double getTienVAT() {
            return tienVAT;
        }

        public double getTongTien() {
            return tongTien;
        }
    }

    /**
     * Số việc chờ thanh toán gồm hóa đơn công nợ và phiếu đã hoàn thành chưa lập hóa đơn.
     * Tổng tiền chỉ tính công nợ vì phiếu chưa lập hóa đơn chưa có khuyến mãi và thuế nên chưa có số cuối cùng.
     */
    public static class ThongKeChoThanhToan {
        private final int soLuong;
        private final double tongTien;

        public ThongKeChoThanhToan(int soLuong, double tongTien) {
            this.soLuong = soLuong;
            this.tongTien = tongTien;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public double getTongTien() {
            return tongTien;
        }
    }


    /** Mọi số liệu của trang chủ quản lý, đọc trên một kết nối. */
    public static class TongQuan {
        public double homNay;
        public double homQua;
        public double thangNay;
        public double thangTruoc;
        public int[] luotXe;
        public ThongKeChoThanhToan hoaDonCho;
        public double[] tyTrong;
        public Map<LocalDate, Double> bayNgay;
        public List<DoanhThuNgay> bang;
    }

    public TongQuan docTongQuan(LocalDate homNay, LocalDate tu, LocalDate den) throws SQLException {
        LocalDate thangTruoc = homNay.minusMonths(1);
        TongQuan t = new TongQuan();
        try (Connection c = ConnectDB.getConnection()) {
            t.homNay = doanhThuNgay(c, homNay);
            t.homQua = doanhThuNgay(c, homNay.minusDays(1));
            t.thangNay = doanhThuThang(c, homNay.getYear(), homNay.getMonthValue());
            t.thangTruoc = doanhThuThang(c, thangTruoc.getYear(), thangTruoc.getMonthValue());
            t.luotXe = luotXeHomNay(c);
            t.hoaDonCho = hoaDonChoThanhToan(c);
            t.tyTrong = tyTrongThang(c, homNay.getYear(), homNay.getMonthValue());
            t.bayNgay = doanhThuTheoNgay(c, homNay.minusDays(6), homNay);
            t.bang = bangDoanhThu(c, tu, den);
        }
        return t;
    }

    private static final String DA_THANH_TOAN = "trangThaiThanhToan = N'Đã thanh toán' ";
    private static final String CONG_NO = "trangThaiThanhToan = N'Công nợ' ";

    /** Doanh thu đã thanh toán trong đúng một ngày. */
    public double doanhThuNgay(LocalDate ngay) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return doanhThuNgay(c, ngay);
        }
    }

    private double doanhThuNgay(Connection c, LocalDate ngay) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tongThanhToan), 0) AS tong FROM HoaDon "
                + "WHERE " + DA_THANH_TOAN + "AND ngayThanhToan >= ? AND ngayThanhToan < ?";
        Timestamp tu = Timestamp.valueOf(ngay.atStartOfDay());
        Timestamp den = Timestamp.valueOf(ngay.plusDays(1).atStartOfDay());
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tu);
            p.setTimestamp(2, den);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getDouble("tong");
            }
        }
    }

    /** Doanh thu đã thanh toán trong một tháng. */
    public double doanhThuThang(int nam, int thang) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return doanhThuThang(c, nam, thang);
        }
    }

    private double doanhThuThang(Connection c, int nam, int thang) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tongThanhToan), 0) AS tong FROM HoaDon "
                + "WHERE " + DA_THANH_TOAN + "AND ngayThanhToan >= ? AND ngayThanhToan < ?";
        LocalDate dauThang = LocalDate.of(nam, thang, 1);
        Timestamp tu = Timestamp.valueOf(dauThang.atStartOfDay());
        Timestamp den = Timestamp.valueOf(dauThang.plusMonths(1).atStartOfDay());
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tu);
            p.setTimestamp(2, den);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getDouble("tong");
            }
        }
    }

    /** {số phiếu tiếp nhận hôm nay, số phiếu còn trong xưởng, số phiếu đã bàn giao}. */
    public int[] luotXeHomNay() throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return luotXeHomNay(c);
        }
    }

    private int[] luotXeHomNay(Connection c) throws SQLException {
        String sql = "SELECT COUNT(*) AS tiepNhan, "
                + "COALESCE(SUM(CASE WHEN bg.maPhieuBG IS NULL THEN 1 ELSE 0 END), 0) AS trongXuong, "
                + "COALESCE(SUM(CASE WHEN bg.maPhieuBG IS NOT NULL THEN 1 ELSE 0 END), 0) AS daBanGiao "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC = p.maPhieuTNSC "
                + "WHERE p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ?";
        LocalDate homNay = LocalDate.now();
        Timestamp tu = Timestamp.valueOf(homNay.atStartOfDay());
        Timestamp den = Timestamp.valueOf(homNay.plusDays(1).atStartOfDay());
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tu);
            p.setTimestamp(2, den);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return new int[]{r.getInt("tiepNhan"), r.getInt("trongXuong"), r.getInt("daBanGiao")};
            }
        }
    }

    public ThongKeChoThanhToan hoaDonChoThanhToan() throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return hoaDonChoThanhToan(c);
        }
    }

    private ThongKeChoThanhToan hoaDonChoThanhToan(Connection c) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM HoaDon WHERE " + CONG_NO + ") "
                + "+ (SELECT COUNT(*) FROM PhieuTiepNhanVaSuaChua p WHERE p.trangThai = N'Hoàn thành' "
                + "AND NOT EXISTS (SELECT 1 FROM HoaDon h WHERE h.maPhieuTNSC = p.maPhieuTNSC "
                + "AND h.trangThaiThanhToan <> N'Đã hủy')) AS soLuong, "
                + "(SELECT COALESCE(SUM(tongThanhToan), 0) FROM HoaDon WHERE " + CONG_NO + ") AS tongTien";
        try (PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            r.next();
            return new ThongKeChoThanhToan(r.getInt("soLuong"), r.getDouble("tongTien"));
        }
    }

    /** {tổng tiền công, tổng tiền phụ tùng} của hóa đơn đã thanh toán trong tháng. */
    public double[] tyTrongThang(int nam, int thang) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return tyTrongThang(c, nam, thang);
        }
    }

    private double[] tyTrongThang(Connection c, int nam, int thang) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(tongTienPhuTung), 0) AS phuTung "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayThanhToan >= ? AND ngayThanhToan < ?";
        LocalDate dauThang = LocalDate.of(nam, thang, 1);
        Timestamp tu = Timestamp.valueOf(dauThang.atStartOfDay());
        Timestamp den = Timestamp.valueOf(dauThang.plusMonths(1).atStartOfDay());
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tu);
            p.setTimestamp(2, den);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return new double[]{r.getDouble("tienCong"), r.getDouble("phuTung")};
            }
        }
    }

    /** Doanh thu đã thanh toán theo từng ngày trong khoảng (chỉ các ngày có hóa đơn). */
    public Map<LocalDate, Double> doanhThuTheoNgay(LocalDate tu, LocalDate den) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return doanhThuTheoNgay(c, tu, den);
        }
    }

    private Map<LocalDate, Double> doanhThuTheoNgay(Connection c, LocalDate tu, LocalDate den) throws SQLException {
        String sql = "SELECT CAST(ngayThanhToan AS DATE) AS ngay, COALESCE(SUM(tongThanhToan), 0) AS tong "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayThanhToan >= ? AND ngayThanhToan < ? "
                + "GROUP BY CAST(ngayThanhToan AS DATE) ORDER BY CAST(ngayThanhToan AS DATE)";
        Timestamp tuTS = Timestamp.valueOf(tu.atStartOfDay());
        Timestamp denTS = Timestamp.valueOf(den.plusDays(1).atStartOfDay());
        Map<LocalDate, Double> ketQua = new LinkedHashMap<>();
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tuTS);
            p.setTimestamp(2, denTS);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ketQua.put(r.getDate("ngay").toLocalDate(), r.getDouble("tong"));
                }
            }
        }
        return ketQua;
    }

    /** {LocalDate ngày, int số phiếu, double tiền công, double phụ tùng, double VAT, double tổng} theo ngày. */
    public List<DoanhThuNgay> bangDoanhThu(LocalDate tu, LocalDate den) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return bangDoanhThu(c, tu, den);
        }
    }

    private List<DoanhThuNgay> bangDoanhThu(Connection c, LocalDate tu, LocalDate den) throws SQLException {
        String sql = "SELECT CAST(ngayThanhToan AS DATE) AS ngay, COUNT(*) AS soPhieu, "
                + "COALESCE(SUM(tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(tongTienPhuTung), 0) AS phuTung, "
                + "COALESCE(SUM(tongTienVAT), 0) AS vat, "
                + "COALESCE(SUM(tongThanhToan), 0) AS tong "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayThanhToan >= ? AND ngayThanhToan < ? "
                + "GROUP BY CAST(ngayThanhToan AS DATE) ORDER BY CAST(ngayThanhToan AS DATE) DESC";
        Timestamp tuTS = Timestamp.valueOf(tu.atStartOfDay());
        Timestamp denTS = Timestamp.valueOf(den.plusDays(1).atStartOfDay());
        List<DoanhThuNgay> ketQua = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tuTS);
            p.setTimestamp(2, denTS);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ketQua.add(new DoanhThuNgay(
                            r.getDate("ngay").toLocalDate(),
                            r.getInt("soPhieu"),
                            r.getDouble("tienCong"),
                            r.getDouble("phuTung"),
                            r.getDouble("vat"),
                            r.getDouble("tong")));
                }
            }
        }
        return ketQua;
    }
}
