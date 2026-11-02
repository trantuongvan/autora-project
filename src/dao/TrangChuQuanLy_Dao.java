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
 * Mọi mốc thời gian đều truyền dưới dạng tham số (nửa khoảng [từ, đến)) để không bọc hàm lên cột ngayLap.
 */
public class TrangChuQuanLy_Dao {

    private static final String DA_THANH_TOAN = "trangThaiThanhToan = N'Đã thanh toán' ";
    private static final String CONG_NO = "trangThaiThanhToan = N'Công nợ' ";

    /** Doanh thu đã thanh toán trong đúng một ngày. */
    public double doanhThuNgay(LocalDate ngay) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tongThanhToan), 0) AS tong FROM HoaDon "
                + "WHERE " + DA_THANH_TOAN + "AND ngayLap >= ? AND ngayLap < ?";
        Timestamp tu = Timestamp.valueOf(ngay.atStartOfDay());
        Timestamp den = Timestamp.valueOf(ngay.plusDays(1).atStartOfDay());
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
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
        String sql = "SELECT COALESCE(SUM(tongThanhToan), 0) AS tong FROM HoaDon "
                + "WHERE " + DA_THANH_TOAN + "AND ngayLap >= ? AND ngayLap < ?";
        LocalDate dauThang = LocalDate.of(nam, thang, 1);
        Timestamp tu = Timestamp.valueOf(dauThang.atStartOfDay());
        Timestamp den = Timestamp.valueOf(dauThang.plusMonths(1).atStartOfDay());
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
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
        String sql = "SELECT COUNT(*) AS tiepNhan, "
                + "COALESCE(SUM(CASE WHEN bg.maPhieuBG IS NULL THEN 1 ELSE 0 END), 0) AS trongXuong, "
                + "COALESCE(SUM(CASE WHEN bg.maPhieuBG IS NOT NULL THEN 1 ELSE 0 END), 0) AS daBanGiao "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC = p.maPhieuTNSC "
                + "WHERE p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ?";
        LocalDate homNay = LocalDate.now();
        Timestamp tu = Timestamp.valueOf(homNay.atStartOfDay());
        Timestamp den = Timestamp.valueOf(homNay.plusDays(1).atStartOfDay());
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tu);
            p.setTimestamp(2, den);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return new int[]{r.getInt("tiepNhan"), r.getInt("trongXuong"), r.getInt("daBanGiao")};
            }
        }
    }

    /** {số hóa đơn còn nợ, tổng tiền còn nợ}. */
    public Object[] hoaDonChoThanhToan() throws SQLException {
        String sql = "SELECT COUNT(*) AS soLuong, COALESCE(SUM(tongThanhToan), 0) AS tongTien "
                + "FROM HoaDon WHERE " + CONG_NO;
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            r.next();
            return new Object[]{r.getInt("soLuong"), r.getDouble("tongTien")};
        }
    }

    /** {tổng tiền công, tổng tiền phụ tùng} của hóa đơn đã thanh toán trong tháng. */
    public double[] tyTrongThang(int nam, int thang) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(tongTienPhuTung), 0) AS phuTung "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayLap >= ? AND ngayLap < ?";
        LocalDate dauThang = LocalDate.of(nam, thang, 1);
        Timestamp tu = Timestamp.valueOf(dauThang.atStartOfDay());
        Timestamp den = Timestamp.valueOf(dauThang.plusMonths(1).atStartOfDay());
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
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
        String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, COALESCE(SUM(tongThanhToan), 0) AS tong "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayLap >= ? AND ngayLap < ? "
                + "GROUP BY CAST(ngayLap AS DATE) ORDER BY CAST(ngayLap AS DATE)";
        Timestamp tuTS = Timestamp.valueOf(tu.atStartOfDay());
        Timestamp denTS = Timestamp.valueOf(den.plusDays(1).atStartOfDay());
        Map<LocalDate, Double> ketQua = new LinkedHashMap<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
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
    public List<Object[]> bangDoanhThu(LocalDate tu, LocalDate den) throws SQLException {
        String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, COUNT(*) AS soPhieu, "
                + "COALESCE(SUM(tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(tongTienPhuTung), 0) AS phuTung, "
                + "COALESCE(SUM(tongTienVAT), 0) AS vat, "
                + "COALESCE(SUM(tongThanhToan), 0) AS tong "
                + "FROM HoaDon WHERE " + DA_THANH_TOAN + "AND ngayLap >= ? AND ngayLap < ? "
                + "GROUP BY CAST(ngayLap AS DATE) ORDER BY CAST(ngayLap AS DATE) DESC";
        Timestamp tuTS = Timestamp.valueOf(tu.atStartOfDay());
        Timestamp denTS = Timestamp.valueOf(den.plusDays(1).atStartOfDay());
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, tuTS);
            p.setTimestamp(2, denTS);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ketQua.add(new Object[]{
                            r.getDate("ngay").toLocalDate(),
                            r.getInt("soPhieu"),
                            r.getDouble("tienCong"),
                            r.getDouble("phuTung"),
                            r.getDouble("vat"),
                            r.getDouble("tong")
                    });
                }
            }
        }
        return ketQua;
    }

    /** {maSKU, tenPhuTung, soLuongTon, nguongCanhBao} của phụ tùng đã chạm ngưỡng cảnh báo. */
    public List<Object[]> canhBaoTonKho() throws SQLException {
        String sql = "SELECT maSKU, tenPhuTung, soLuongTon, nguongCanhBao FROM PhuTung "
                + "WHERE soLuongTon <= nguongCanhBao ORDER BY soLuongTon ASC, maSKU ASC";
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                ketQua.add(new Object[]{
                        r.getString("maSKU"),
                        r.getString("tenPhuTung"),
                        r.getInt("soLuongTon"),
                        r.getInt("nguongCanhBao")
                });
            }
        }
        return ketQua;
    }
}
