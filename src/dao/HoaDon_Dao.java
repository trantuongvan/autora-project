package dao;

import connection.ConnectDB;
import controller.ChiTietHoaDonController;
import entity.ChiTietSuaChua_DichVu;
import entity.ChiTietSuaChua_PhuTung;
import entity.HinhThucKhuyenMai;
import entity.HinhThucThanhToan;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.PhamViKhuyenMai;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhuTung;
import entity.Thue;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;

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
            + "h.maKM, t.maThue, t.tenLoaiThue, t.thueSuat, "
            + PhieuTiepNhanVaSuaChua_Dao.COT_XE + " "
            + "FROM HoaDon h "
            + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = h.maPhieuTNSC "
            + "JOIN Xe x ON x.bienSo = p.bienSo "
            + "JOIN KhachHang k ON k.maKH = p.maKH "
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

    /** Phiếu đủ điều kiện lập hóa đơn, kèm xe và chủ xe. */
    public List<PhieuTiepNhanVaSuaChua> findPhieuChoLap() throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, " + PhieuTiepNhanVaSuaChua_Dao.COT_XE + " FROM PhieuTiepNhanVaSuaChua p "
                + "JOIN Xe x ON x.bienSo = p.bienSo JOIN KhachHang k ON k.maKH = p.maKH "
                + "WHERE p.trangThai = N'Hoàn thành' AND NOT EXISTS (SELECT 1 FROM HoaDon h "
                + "WHERE h.maPhieuTNSC = p.maPhieuTNSC AND h.trangThaiThanhToan <> N'Đã hủy') "
                + "ORDER BY p.thoiGianTiepNhan DESC";
        List<PhieuTiepNhanVaSuaChua> ketQua = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(rs.getString("maPhieuTNSC"));
                phieu.setTrangThai(TrangThaiPhieu.HOAN_THANH);
                phieu.setXe(PhieuTiepNhanVaSuaChua_Dao.docXe(rs));
                ketQua.add(phieu);
            }
        }
        return ketQua;
    }

    /** Phiếu kèm xe và chủ xe; null khi không có phiếu. */
    public PhieuTiepNhanVaSuaChua findThongTinPhieu(String maPhieu) throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, p.trangThai, " + PhieuTiepNhanVaSuaChua_Dao.COT_XE + " "
                + "FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo = p.bienSo "
                + "JOIN KhachHang k ON k.maKH = p.maKH WHERE p.maPhieuTNSC = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(rs.getString("maPhieuTNSC"));
                phieu.setTrangThai(TrangThaiPhieu.fromDb(rs.getString("trangThai")));
                phieu.setXe(PhieuTiepNhanVaSuaChua_Dao.docXe(rs));
                return phieu;
            }
        }
    }

    public boolean coHoaDonHieuLuc(String maPhieu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return coHoaDonHieuLuc(connection, maPhieu);
        }
    }

    private static boolean coHoaDonHieuLuc(Connection connection, String maPhieu) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon WHERE maPhieuTNSC = ? AND trangThaiThanhToan <> N'Đã hủy'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<ChiTietSuaChua_DichVu> findDichVu(String maPhieu) throws SQLException {
        return new ChiTietSuaChua_DichVu_Dao().findByPhieu(maPhieu);
    }

    public List<ChiTietSuaChua_PhuTung> findPhuTung(String maPhieu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return docPhuTung(connection, maPhieu);
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

    /** Mã dự kiến để in lên nội dung QR trước khi lưu. */
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
        HoaDon hoaDon = new HoaDon();
        hoaDon.setHinhThucThanhToan(hinhThuc);
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                kiemTraPhieu(connection, maPhieu, hinhThuc);
                hoaDon.setKhuyenMai(maKM == null ? null : docKhuyenMai(connection, maKM));
                hoaDon.setThue(docThue(connection, maThue));

                double tienPhuTung = 0;
                for (ChiTietSuaChua_PhuTung d : docPhuTung(connection, maPhieu)) {
                    tienPhuTung += d.thanhTien();
                }
                ChiTietHoaDonController.KetQuaTinhToan ketQua = ChiTietHoaDonController.tinhToan(
                        docTienCong(connection, maPhieu), tienPhuTung, hoaDon.getKhuyenMai(), hoaDon.getThue());
                kiemTraTienMat(hinhThuc, tienKhachDua, ketQua.tongThanhToan);

                String maHD = sinhMaHD(connection);
                themHoaDon(connection, maHD, maPhieu, maNV, hoaDon, ketQua);
                connection.commit();
                return maHD;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    private static void kiemTraPhieu(Connection connection, String maPhieu, HinhThucThanhToan hinhThuc) throws SQLException {
        String sql = "SELECT p.trangThai, k.duocGhiNo FROM PhieuTiepNhanVaSuaChua p "
                + "JOIN Xe x ON x.bienSo = p.bienSo JOIN KhachHang k ON k.maKH = p.maKH WHERE p.maPhieuTNSC = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Phiếu " + maPhieu + " không tồn tại.");
                }
                if (TrangThaiPhieu.fromDb(rs.getString(1)) != TrangThaiPhieu.HOAN_THANH) {
                    throw new SQLException("Phiếu " + maPhieu + " chưa hoàn thành nên chưa lập được hóa đơn.");
                }
                KhachHang khach = new KhachHang();
                khach.setDuocGhiNo(rs.getBoolean(2));
                if (!khach.duocThanhToanBang(hinhThuc)) {
                    throw new SQLException("Khách hàng của phiếu " + maPhieu + " không được phép ghi nợ.");
                }
            }
        }
        // Phụ tùng chỉ vào hóa đơn sau khi duyệt xuất kho: lập lúc này sẽ tính thiếu tiền phụ tùng.
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM PhieuYeuCauXuatKho WHERE maPhieuTNSC = ? AND trangThai = N'Chờ xuất'")) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Phiếu " + maPhieu + " còn yêu cầu phụ tùng chờ xuất kho. "
                            + "Hãy duyệt hoặc từ chối yêu cầu trước khi lập hóa đơn.");
                }
            }
        }
        if (coHoaDonHieuLuc(connection, maPhieu)) {
            throw new SQLException("Phiếu " + maPhieu + " đã có hóa đơn còn hiệu lực.");
        }
    }

    private static KhuyenMai docKhuyenMai(Connection connection, String maKM) throws SQLException {
        String sql = "SELECT hinhThuc, giaTri, phamViApDung FROM KhuyenMai "
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

    private static Thue docThue(Connection connection, String maThue) throws SQLException {
        if (maThue == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại thuế.");
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT tenLoaiThue, thueSuat FROM Thue WHERE maThue = ?")) {
            statement.setString(1, maThue);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Loại thuế " + maThue + " không tồn tại.");
                }
                return new Thue(maThue, rs.getString(1), rs.getDouble(2));
            }
        }
    }

    private static double docTienCong(Connection connection, String maPhieu) throws SQLException {
        String sql = "SELECT COALESCE(SUM(tienCongThucTe), 0) FROM ChiTietSuaChua_DichVu WHERE maPhieuTNSC = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private static List<ChiTietSuaChua_PhuTung> docPhuTung(Connection connection, String maPhieu) throws SQLException {
        String sql = "SELECT t.maSKU, t.tenPhuTung, c.soLuongSuDung, c.giaBanThucTe "
                + "FROM ChiTietSuaChua_PhuTung c JOIN PhuTung t ON t.maSKU = c.maSKU "
                + "WHERE c.maPhieuTNSC = ? ORDER BY c.maSKU";
        List<ChiTietSuaChua_PhuTung> ketQua = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieu);
            PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(maPhieu);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PhuTung phuTung = new PhuTung(rs.getString("maSKU"));
                    phuTung.setTenPhuTung(rs.getString("tenPhuTung"));
                    ketQua.add(new ChiTietSuaChua_PhuTung(phieu, phuTung, rs.getInt("soLuongSuDung"),
                            rs.getDouble("giaBanThucTe")));
                }
            }
        }
        return ketQua;
    }

    private static void kiemTraTienMat(HinhThucThanhToan hinhThuc, Double tienKhachDua, double tong) throws SQLException {
        if (!hinhThuc.duTien(tienKhachDua, tong)) {
            throw new SQLException("Tiền khách đưa chưa đủ tổng thanh toán.");
        }
    }

    private static String sinhMaHD(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SINH_MA_HD);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    private static void themHoaDon(Connection connection, String maHD, String maPhieu, String maNV, HoaDon hoaDon,
            ChiTietHoaDonController.KetQuaTinhToan ketQua) throws SQLException {
        HinhThucThanhToan hinhThuc = hoaDon.getHinhThucThanhToan();
        // Hóa đơn công nợ chưa thu tiền nên chưa có ngày thanh toán và chưa tính vào doanh thu.
        String sql = "INSERT INTO HoaDon (maHD, maPhieuTNSC, maNV_Lap, maKM, maThue, ngayLap, tongTienCong, tongTienPhuTung, "
                + "tongTienGiam, tongTienVAT, tongThanhToan, hinhThucThanhToan, trangThaiThanhToan, ngayThanhToan) "
                + "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?, "
                + (hinhThuc == HinhThucThanhToan.CONG_NO ? "NULL" : "GETDATE()") + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maHD);
            statement.setString(2, maPhieu);
            statement.setString(3, maNV);
            if (hoaDon.getKhuyenMai() == null) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, hoaDon.getKhuyenMai().getMaKM());
            }
            statement.setString(5, hoaDon.getThue().getMaThue());
            statement.setDouble(6, ketQua.tongTienCong);
            statement.setDouble(7, ketQua.tongTienPhuTung);
            statement.setDouble(8, ketQua.tongTienGiam);
            statement.setDouble(9, ketQua.tongTienVAT);
            statement.setDouble(10, ketQua.tongThanhToan);
            statement.setString(11, hinhThuc.getText());
            statement.setString(12, (hinhThuc == HinhThucThanhToan.CONG_NO ? TrangThaiHoaDon.CONG_NO : TrangThaiHoaDon.DA_THANH_TOAN).getText());
            statement.executeUpdate();
        }
    }

    /** Thu tiền hóa đơn công nợ. */
    public void thanhToanCongNo(String maHD, HinhThucThanhToan hinhThuc, Double tienKhachDua, String tenDangNhap)
            throws SQLException {
        if (hinhThuc != HinhThucThanhToan.TIEN_MAT && hinhThuc != HinhThucThanhToan.CHUYEN_KHOAN) {
            throw new IllegalArgumentException("Hình thức thanh toán không hợp lệ");
        }
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                double tong;
                String doc = "SELECT tongThanhToan FROM HoaDon "
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
                String ghi = "UPDATE HoaDon SET hinhThucThanhToan = ?, trangThaiThanhToan = N'Đã thanh toán', "
                        + "ngayThanhToan = GETDATE() WHERE maHD = ?";
                try (PreparedStatement statement = connection.prepareStatement(ghi)) {
                    statement.setString(1, hinhThuc.getText());
                    statement.setString(2, maHD);
                    statement.executeUpdate();
                }
                nhatKyDao.ghi(connection, tenDangNhap, "Thu tiền công nợ", maHD,
                        TrangThaiHoaDon.CONG_NO.getText(), hinhThuc.getText() + " - " + (long) tong);
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
        String lyDo = HoaDon.chuanHoaLyDoHuy(lyDoHuy);
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String trangThaiCu;
                String maPhieu;
                String doc = "SELECT trangThaiThanhToan, maPhieuTNSC FROM HoaDon WHERE maHD = ?";
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
                String coBanGiao = "SELECT 1 FROM PhieuBanGiao WHERE maPhieuTNSC = ?";
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

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        Timestamp ngayLap = rs.getTimestamp("ngayLap");
        PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(rs.getString("maPhieuTNSC"));
        phieu.setXe(PhieuTiepNhanVaSuaChua_Dao.docXe(rs));
        String maKM = rs.getString("maKM");
        String maThue = rs.getString("maThue");
        return new HoaDon(rs.getString("maHD"), phieu,
                ngayLap == null ? null : ngayLap.toLocalDateTime(), rs.getDouble("tongTienCong"),
                rs.getDouble("tongTienPhuTung"), rs.getDouble("tongTienGiam"), rs.getDouble("tongTienVAT"),
                rs.getDouble("tongThanhToan"), HinhThucThanhToan.fromDb(rs.getString("hinhThucThanhToan")),
                TrangThaiHoaDon.fromDb(rs.getString("trangThaiThanhToan")),
                rs.getString("lyDoHuy"),
                maKM == null ? null : new KhuyenMai(maKM),
                maThue == null ? null : new Thue(maThue, rs.getString("tenLoaiThue"), rs.getDouble("thueSuat")));
    }
}
