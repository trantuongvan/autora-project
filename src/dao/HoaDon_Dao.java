package dao;

import connection.ConnectDB;
import entity.HinhThucKhuyenMai;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.KhuyenMai;
import entity.LoaiKhachHang;
import entity.PhamViKhuyenMai;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Hóa đơn: tra cứu, lập, thanh toán công nợ và hủy. Mọi số tiền được tính lại trong transaction. */
public class HoaDon_Dao {
    private static final String SELECT_HOA_DON =
            "SELECT h.maHD, h.maPhieuTNSC, h.ngayLap, h.tongTienCong, h.tongTienPhuTung, h.tongTienGiam, "
            + "h.tongTienVAT, h.tongThanhToan, h.hinhThucThanhToan, h.trangThaiThanhToan, h.lyDoHuy, "
            + "h.maNV_Lap, h.maKM, h.maThue, k.hoTen, p.bienSo, t.thueSuat "
            + "FROM HoaDon h "
            + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = h.maPhieuTNSC "
            + "JOIN Xe x ON x.bienSo = p.bienSo "
            + "JOIN KhachHang k ON k.maKH = x.maKH "
            + "LEFT JOIN Thue t ON t.maThue = h.maThue ";

    private static final String SINH_MA_HD =
            "SELECT 'HD' + RIGHT('000000' + CAST(COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maHD, 3, 18))), 0) + 1 "
            + "AS VARCHAR(18)), 6) FROM HoaDon ";

    private static final String KHUYEN_MAI_HOP_LE =
            "KhuyenMai.trangThai = 1 "
            + "AND CAST(GETDATE() AS DATE) BETWEEN CAST(KhuyenMai.thoiGianBatDau AS DATE) AND CAST(KhuyenMai.thoiGianKetThuc AS DATE) "
            + "AND (KhuyenMai.soLuotToiDa IS NULL OR (SELECT COUNT(*) FROM HoaDon h WHERE h.maKM = KhuyenMai.maKM "
            + "AND h.trangThaiThanhToan <> N'Đã hủy') < KhuyenMai.soLuotToiDa)";

    private final NhatKy_Dao nhatKyDao = new NhatKy_Dao();

    public List<HoaDon> search(String tuKhoa, TrangThaiHoaDon trangThai, HinhThucThanhToan hinhThuc,
            LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        String sql = SELECT_HOA_DON
                + "WHERE (? = N'' OR h.maHD LIKE ? OR h.maPhieuTNSC LIKE ? OR k.soDienThoai LIKE ? OR p.bienSo LIKE ?) "
                + "AND (? = N'' OR h.trangThaiThanhToan = ?) "
                + "AND (? = N'' OR h.hinhThucThanhToan = ?) ";
        if (tuNgay != null) {
            sql += "AND h.ngayLap >= ? ";
        }
        if (denNgay != null) {
            sql += "AND h.ngayLap < ? ";
        }
        sql += "ORDER BY h.ngayLap DESC, h.maHD DESC";

        List<HoaDon> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            for (int i = 2; i <= 5; i++) {
                statement.setString(i, "%" + key + "%");
            }
            String chuoiTrangThai = trangThai == null ? "" : trangThai.getText();
            String chuoiHinhThuc = hinhThuc == null ? "" : hinhThuc.getText();
            statement.setString(6, chuoiTrangThai);
            statement.setString(7, chuoiTrangThai);
            statement.setString(8, chuoiHinhThuc);
            statement.setString(9, chuoiHinhThuc);
            int index = 10;
            if (tuNgay != null) {
                statement.setTimestamp(index++, Timestamp.valueOf(tuNgay.atStartOfDay()));
            }
            if (denNgay != null) {
                statement.setTimestamp(index, Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapHoaDon(rs));
                }
            }
        }
        return ketQua;
    }

    public HoaDon findById(String maHD) throws SQLException {
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_HOA_DON + "WHERE h.maHD = ?")) {
            statement.setString(1, maHD);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? mapHoaDon(rs) : null;
            }
        }
    }

    /** Phiếu đủ điều kiện lập hóa đơn. Mỗi dòng: {maPhieu, bienSo, hoTen}. */
    public List<String[]> findPhieuChoLap() throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, p.bienSo, k.hoTen FROM PhieuTiepNhanVaSuaChua p "
                + "JOIN Xe x ON x.bienSo = p.bienSo JOIN KhachHang k ON k.maKH = x.maKH "
                + "WHERE p.trangThai = N'Hoàn thành' AND NOT EXISTS (SELECT 1 FROM HoaDon h "
                + "WHERE h.maPhieuTNSC = p.maPhieuTNSC AND h.trangThaiThanhToan <> N'Đã hủy') "
                + "ORDER BY p.thoiGianTiepNhan DESC";
        List<String[]> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.add(new String[] {rs.getString(1), rs.getString(2), rs.getString(3)});
            }
        }
        return ketQua;
    }

    public HoaDon.ThongTinPhieu findThongTinPhieu(String maPhieu) throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, p.trangThai, k.maKH, k.hoTen, k.loaiKhachHang, k.soDienThoai, k.diaChi, "
                + "k.email, k.duocGhiNo, x.bienSo, "
                + "CONCAT(x.hangXe, ' ', x.dongXe, ' • Đời ', x.doiXe, ' • ', x.mauSon) AS xe, x.soVIN "
                + "FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo = p.bienSo "
                + "JOIN KhachHang k ON k.maKH = x.maKH WHERE p.maPhieuTNSC = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new HoaDon.ThongTinPhieu(rs.getString("maPhieuTNSC"), TrangThaiPhieu.fromDb(rs.getString("trangThai")),
                        rs.getString("maKH"), rs.getString("hoTen"), LoaiKhachHang.fromDb(rs.getString("loaiKhachHang")),
                        rs.getString("soDienThoai"),
                        rs.getString("diaChi"), rs.getString("email"), rs.getBoolean("duocGhiNo"),
                        rs.getString("bienSo"), rs.getString("xe"), rs.getString("soVIN"));
            }
        }
    }

    public boolean coHoaDonHieuLuc(String maPhieu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return coHoaDonHieuLuc(connection, maPhieu, "");
        }
    }

    private static boolean coHoaDonHieuLuc(Connection connection, String maPhieu, String khoa) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon " + khoa + " WHERE maPhieuTNSC = ? AND trangThaiThanhToan <> N'Đã hủy'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Mỗi dòng: {tenDichVu, maDichVu, kyThuatVien, trangThai, tienCong}. */
    public List<Object[]> findDichVu(String maPhieu) throws SQLException {
        String sql = "SELECT d.tenDichVu, c.maDichVu, c.maNV_KyThuat, n.hoTen, c.trangThaiTienDo, c.tienCongThucTe "
                + "FROM ChiTietSuaChua_DichVu c JOIN DichVu d ON d.maDichVu = c.maDichVu "
                + "LEFT JOIN NhanVien n ON n.maNV = c.maNV_KyThuat WHERE c.maPhieuTNSC = ? ORDER BY c.maDichVu";
        List<Object[]> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString(1);
                    String ma = rs.getString(2);
                    String maTho = rs.getString(3);
                    String tenTho = rs.getString(4);
                    String tho = maTho == null ? "Chờ phân công" : maTho + " — " + tenTho;
                    ketQua.add(new Object[] {ten, ma, tho, rs.getString(5), rs.getDouble(6)});
                }
            }
        }
        return ketQua;
    }

    /** Mỗi dòng: {tenPhuTung, maSKU, soLuong, giaBan, thanhTien}. */
    public List<Object[]> findPhuTung(String maPhieu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            List<Object[]> ketQua = new ArrayList<>();
            for (Object[] d : docPhuTung(connection, maPhieu, "")) {
                ketQua.add(new Object[] {d[0], d[1], d[2], d[3], (int) d[2] * (double) d[3]});
            }
            return ketQua;
        }
    }

    public List<KhuyenMai> findKhuyenMaiHopLe() throws SQLException {
        String sql = "SELECT maKM, tenChuongTrinh, hinhThuc, giaTri, thoiGianBatDau, thoiGianKetThuc, trangThai, phamViApDung "
                + "FROM KhuyenMai WHERE " + KHUYEN_MAI_HOP_LE + " ORDER BY maKM";
        List<KhuyenMai> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai(rs.getString("maKM"), rs.getString("tenChuongTrinh"), HinhThucKhuyenMai.fromDb(rs.getString("hinhThuc")),
                        rs.getDouble("giaTri"), rs.getTimestamp("thoiGianBatDau").toLocalDateTime().toLocalDate(),
                        rs.getTimestamp("thoiGianKetThuc").toLocalDateTime().toLocalDate(), rs.getBoolean("trangThai"));
                km.setPhamViApDung(PhamViKhuyenMai.fromDb(rs.getString("phamViApDung")));
                ketQua.add(km);
            }
        }
        return ketQua;
    }

    /** Mã dự kiến để in lên nội dung QR trước khi lưu; máy khác có thể lấy mất nên không dùng để ghi. */
    public String duDoanMaHD() throws SQLException {
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(SINH_MA_HD);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    /** Lập hóa đơn trong một transaction, không đổi tồn kho vì phụ tùng đã trừ lúc duyệt xuất kho. tienKhachDua chỉ dùng cho tiền mặt. Trả về mã hóa đơn. */
    public String lapHoaDon(String maPhieu, String maKM, String maThue, HinhThucThanhToan hinhThuc, Double tienKhachDua,
            String maNV) throws SQLException {
        new HoaDon().setHinhThucThanhToan(hinhThuc);
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                kiemTraPhieu(connection, maPhieu, hinhThuc);
                KhuyenMai km = maKM == null ? null : docKhuyenMai(connection, maKM);
                double thueSuat = docThueSuat(connection, maThue);

                // HOLDLOCK: không ai sửa được dòng đã tính tiền trước khi hóa đơn được ghi.
                List<Object[]> phuTung = docPhuTung(connection, maPhieu, "WITH (HOLDLOCK)");
                double tienPhuTung = 0;
                for (Object[] d : phuTung) {
                    tienPhuTung += (int) d[2] * (double) d[3];
                }
                TienHoaDon tien = tinhTien(docTienCong(connection, maPhieu), tienPhuTung,
                        km == null ? null : km.getHinhThuc(), km == null ? 0 : km.getGiaTri(),
                        km == null ? null : km.getPhamViApDung(), thueSuat);
                kiemTraTienMat(hinhThuc, tienKhachDua, tien.tong);

                String maHD = sinhMaHD(connection);
                themHoaDon(connection, maHD, maPhieu, maNV, maKM, maThue, hinhThuc, tien);
                connection.commit();
                return maHD;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private static void kiemTraPhieu(Connection connection, String maPhieu, HinhThucThanhToan hinhThuc) throws SQLException {
        // Khóa dòng phiếu để hai máy không cùng lập hóa đơn cho một phiếu.
        String sql = "SELECT p.trangThai, k.duocGhiNo FROM PhieuTiepNhanVaSuaChua p WITH (UPDLOCK, ROWLOCK) "
                + "JOIN Xe x ON x.bienSo = p.bienSo JOIN KhachHang k ON k.maKH = x.maKH WHERE p.maPhieuTNSC = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Phiếu " + maPhieu + " không tồn tại.");
                }
                if (TrangThaiPhieu.fromDb(rs.getString(1)) != TrangThaiPhieu.HOAN_THANH) {
                    throw new SQLException("Phiếu " + maPhieu + " chưa hoàn thành nên chưa lập được hóa đơn.");
                }
                if (hinhThuc == HinhThucThanhToan.CONG_NO && !rs.getBoolean(2)) {
                    throw new SQLException("Khách hàng của phiếu " + maPhieu + " không được phép ghi nợ.");
                }
            }
        }
        if (coHoaDonHieuLuc(connection, maPhieu, "WITH (UPDLOCK, HOLDLOCK)")) {
            throw new SQLException("Phiếu " + maPhieu + " đã có hóa đơn còn hiệu lực.");
        }
    }

    /** Khóa dòng khuyến mãi để hai hóa đơn không cùng dùng lượt cuối. */
    private static KhuyenMai docKhuyenMai(Connection connection, String maKM) throws SQLException {
        String sql = "SELECT hinhThuc, giaTri, phamViApDung FROM KhuyenMai WITH (UPDLOCK, ROWLOCK) "
                + "WHERE maKM = ? AND " + KHUYEN_MAI_HOP_LE;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKM);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Khuyến mãi " + maKM + " đã hết hạn, ngừng áp dụng hoặc hết lượt.");
                }
                KhuyenMai km = new KhuyenMai(maKM);
                km.setHinhThuc(HinhThucKhuyenMai.fromDb(rs.getString(1)));
                km.setGiaTri(rs.getDouble(2));
                km.setPhamViApDung(PhamViKhuyenMai.fromDb(rs.getString(3)));
                return km;
            }
        }
    }

    private static double docThueSuat(Connection connection, String maThue) throws SQLException {
        if (maThue == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại thuế.");
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT thueSuat FROM Thue WHERE maThue = ?")) {
            statement.setString(1, maThue);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Loại thuế " + maThue + " không tồn tại.");
                }
                return rs.getDouble(1);
            }
        }
    }

    private static double docTienCong(Connection connection, String maPhieu) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tienCongThucTe), 0) FROM ChiTietSuaChua_DichVu WITH (HOLDLOCK) WHERE maPhieuTNSC = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    /** Mỗi dòng: {tenPhuTung, maSKU, soLuong, giaBan}. */
    private static List<Object[]> docPhuTung(Connection connection, String maPhieu, String khoa) throws SQLException {
        String sql = "SELECT t.tenPhuTung, c.maSKU, c.soLuongSuDung, c.giaBanThucTe "
                + "FROM ChiTietSuaChua_PhuTung c " + khoa + " JOIN PhuTung t ON t.maSKU = c.maSKU "
                + "WHERE c.maPhieuTNSC = ? ORDER BY c.maSKU";
        List<Object[]> ketQua = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new Object[] {rs.getString(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)});
                }
            }
        }
        return ketQua;
    }

    private static void kiemTraTienMat(HinhThucThanhToan hinhThuc, Double tienKhachDua, double tong) throws SQLException {
        if (hinhThuc == HinhThucThanhToan.TIEN_MAT && (tienKhachDua == null || tienKhachDua < tong)) {
            throw new SQLException("Tiền khách đưa chưa đủ tổng thanh toán.");
        }
    }

    private static String sinhMaHD(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SINH_MA_HD + "WITH (UPDLOCK, HOLDLOCK)");
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    private static void themHoaDon(Connection connection, String maHD, String maPhieu, String maNV, String maKM,
            String maThue, HinhThucThanhToan hinhThuc, TienHoaDon tien) throws SQLException {
        String sql = "INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap, maKM, maThue, ngayLap, tongTienCong, tongTienPhuTung, "
                + "tongTienGiam, tongTienVAT, tongThanhToan, hinhThucThanhToan, trangThaiThanhToan) "
                + "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maHD);
            statement.setString(2, maPhieu);
            statement.setString(3, maNV);
            if (maKM == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, maKM);
            }
            statement.setString(5, maThue);
            statement.setDouble(6, tien.cong);
            statement.setDouble(7, tien.phuTung);
            statement.setDouble(8, tien.giam);
            statement.setDouble(9, tien.vat);
            statement.setDouble(10, tien.tong);
            statement.setString(11, hinhThuc.getText());
            statement.setString(12, (hinhThuc == HinhThucThanhToan.CONG_NO ? TrangThaiHoaDon.CONG_NO : TrangThaiHoaDon.DA_THANH_TOAN).getText());
            statement.executeUpdate();
        }
    }

    /** Thu tiền hóa đơn công nợ. */
    public void thanhToanCongNo(String maHD, HinhThucThanhToan hinhThuc, Double tienKhachDua) throws SQLException {
        if (hinhThuc != HinhThucThanhToan.TIEN_MAT && hinhThuc != HinhThucThanhToan.CHUYEN_KHOAN) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                double tong;
                String doc = "SELECT tongThanhToan FROM HoaDon WITH (UPDLOCK, ROWLOCK) "
                        + "WHERE maHD = ? AND trangThaiThanhToan = N'Công nợ'";
                try (PreparedStatement statement = connection.prepareStatement(doc)) {
                    statement.setString(1, maHD);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Hóa đơn " + maHD + " không còn ở trạng thái Công nợ.");
                        }
                        tong = rs.getDouble(1);
                    }
                }
                kiemTraTienMat(hinhThuc, tienKhachDua, tong);
                String ghi = "UPDATE HoaDon SET hinhThucThanhToan = ?, trangThaiThanhToan = N'Đã thanh toán' WHERE maHD = ?";
                try (PreparedStatement statement = connection.prepareStatement(ghi)) {
                    statement.setString(1, hinhThuc.getText());
                    statement.setString(2, maHD);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Hủy hóa đơn và ghi nhật ký trong một transaction. Phụ tùng vẫn nằm trên phiếu nên không hoàn kho. */
    public void huy(String maHD, String lyDoHuy, String tenDangNhap) throws SQLException {
        if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy.");
        }
        HoaDon kiemTra = new HoaDon();
        kiemTra.setLyDoHuy(lyDoHuy);
        String lyDo = kiemTra.getLyDoHuy();
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String trangThaiCu;
                String maPhieu;
                String doc = "SELECT trangThaiThanhToan, maPhieuTNSC FROM HoaDon WITH (UPDLOCK, ROWLOCK) WHERE maHD = ?";
                try (PreparedStatement statement = connection.prepareStatement(doc)) {
                    statement.setString(1, maHD);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Hóa đơn " + maHD + " không tồn tại.");
                        }
                        trangThaiCu = rs.getString(1);
                        maPhieu = rs.getString(2);
                    }
                }
                String coBanGiao = "SELECT 1 FROM PhieuBanGiao WITH (UPDLOCK, HOLDLOCK) WHERE maPhieuTNSC = ?";
                try (PreparedStatement statement = connection.prepareStatement(coBanGiao)) {
                    statement.setString(1, maPhieu);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("Xe của hóa đơn " + maHD + " đã bàn giao nên không thể hủy.");
                        }
                    }
                }
                String ghi = "UPDATE HoaDon SET trangThaiThanhToan = N'Đã hủy', lyDoHuy = ? "
                        + "WHERE maHD = ? AND trangThaiThanhToan <> N'Đã hủy'";
                try (PreparedStatement statement = connection.prepareStatement(ghi)) {
                    statement.setString(1, lyDo);
                    statement.setString(2, maHD);
                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("Hóa đơn " + maHD + " đã được hủy trước đó.");
                    }
                }
                nhatKyDao.ghi(connection, tenDangNhap, "Hủy hóa đơn", maHD, trangThaiCu,
                        TrangThaiHoaDon.DA_HUY.getText() + " - Lý do: " + lyDo);
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /** Các khoản tiền đã làm tròn tới đồng. */
    public static final class TienHoaDon {
        public final double cong;
        public final double phuTung;
        public final double giam;
        public final double vat;
        public final double tong;

        private TienHoaDon(double cong, double phuTung, double giam, double vat, double tong) {
            this.cong = cong;
            this.phuTung = phuTung;
            this.giam = giam;
            this.vat = vat;
            this.tong = tong;
        }
    }

    /** Hàm thuần để màn hình xem trước và lúc lưu dùng cùng một công thức. hinhThucKM null là không có khuyến mãi. */
    public static TienHoaDon tinhTien(double tienCong, double tienPhuTung, HinhThucKhuyenMai hinhThucKM, double giaTriKM,
            PhamViKhuyenMai phamViKM, double thueSuat) {
        if (tienCong < 0 || tienPhuTung < 0 || giaTriKM < 0 || thueSuat < 0) {
            throw new IllegalArgumentException("Số tiền và thuế suất không được âm.");
        }
        double cong = Math.round(tienCong);
        double phuTung = Math.round(tienPhuTung);
        double giam = hinhThucKM == null ? 0 : tinhGiam(cong, phuTung, hinhThucKM, giaTriKM, phamViKM);
        double sauGiam = cong + phuTung - giam;
        double vat = phanTram(sauGiam, thueSuat);
        return new TienHoaDon(cong, phuTung, giam, vat, sauGiam + vat);
    }

    private static double tinhGiam(double cong, double phuTung, HinhThucKhuyenMai hinhThucKM, double giaTriKM,
            PhamViKhuyenMai phamViKM) {
        double coSo;
        if (phamViKM == PhamViKhuyenMai.TONG_HOA_DON) {
            coSo = cong + phuTung;
        } else if (phamViKM == PhamViKhuyenMai.NHOM_DICH_VU) {
            coSo = cong;
        } else if (phamViKM == PhamViKhuyenMai.PHU_TUNG) {
            coSo = phuTung;
        } else {
            throw new IllegalArgumentException("Phạm vi khuyến mãi không hợp lệ.");
        }
        if (hinhThucKM == HinhThucKhuyenMai.PHAN_TRAM) {
            return Math.min(coSo, phanTram(coSo, giaTriKM));
        }
        return Math.min(coSo, Math.round(giaTriKM));
    }

    /** Dùng BigDecimal để số kiểu 80,5 không bị double làm tròn xuống. */
    public static double phanTram(double coSo, double tyLe) {
        return BigDecimal.valueOf(coSo).multiply(BigDecimal.valueOf(tyLe))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).doubleValue();
    }

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        Timestamp ngayLap = rs.getTimestamp("ngayLap");
        HoaDon hd = new HoaDon(rs.getString("maHD"), rs.getString("maPhieuTNSC"),
                ngayLap == null ? null : ngayLap.toLocalDateTime(), rs.getDouble("tongTienCong"),
                rs.getDouble("tongTienPhuTung"), rs.getDouble("tongTienGiam"), rs.getDouble("tongTienVAT"),
                rs.getDouble("tongThanhToan"), HinhThucThanhToan.fromDb(rs.getString("hinhThucThanhToan")),
                TrangThaiHoaDon.fromDb(rs.getString("trangThaiThanhToan")),
                rs.getString("lyDoHuy"), rs.getString("maNV_Lap"), rs.getString("maKM"), rs.getString("maThue"));
        hd.setTenKhachHang(rs.getString("hoTen"));
        hd.setBienSo(rs.getString("bienSo"));
        hd.setThueSuat(rs.getDouble("thueSuat"));
        return hd;
    }
}
