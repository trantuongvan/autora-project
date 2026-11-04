package dao;

import connection.ConnectDB;
import entity.DichVu;
import entity.KhachHang;
import entity.MucUuTien;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.Xe;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuTiepNhanVaSuaChua_Dao {
    /** Các cột xe (bí danh x) và chủ xe (bí danh k) mà docXe đọc theo tên. */
    static final String COT_XE = "x.bienSo, x.soVIN, x.hangXe, x.dongXe, x.doiXe, x.mauSon, " + KhachHang_Dao.COT_KHACH_HANG;

    private static final String SELECT_JOIN =
            "SELECT p.maPhieuTNSC, p.yeuCauBanDau, p.soKm, p.mucNhienLieu, "
            + "p.trayXuoc, p.taiSanTrenXe, p.thoiGianTiepNhan, p.thoiGianDuKien, "
            + "p.trangThai, p.danhGiaNoiThat, p.ghiChu, "
            + "p.phuTungDuKien, p.ghiChuKyThuat, p.uuTien, "
            + COT_XE + ", "
            + NhanVien_Dao.cotNhanVien("n", "lap_") + ", "
            + NhanVien_Dao.cotNhanVien("pt", "pt_") + " "
            + "FROM PhieuTiepNhanVaSuaChua p "
            + "JOIN Xe x ON x.bienSo = p.bienSo "
            + "JOIN KhachHang k ON k.maKH = p.maKH "
            + "JOIN NhanVien n ON n.maNV = p.maNV_LapPhieu "
            + "LEFT JOIN NhanVien pt ON pt.maNV = p.maNV_PhuTrach ";

    static final String NOT_LOCKED =
            "NOT EXISTS (SELECT 1 FROM HoaDon h "
            + "WHERE h.maPhieuTNSC = p.maPhieuTNSC "
            + "AND h.trangThaiThanhToan <> N'Đã hủy') AND p.trangThai <> N'Đã hủy'";

    public List<PhieuTiepNhanVaSuaChua> search(String keyword, TrangThaiPhieu trangThai,
            LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String key = keyword == null ? "" : keyword.trim();
        String status = trangThai == null ? "" : trangThai.getText();
        String sql = SELECT_JOIN
                + "WHERE (? = N'' OR p.maPhieuTNSC LIKE ? OR p.bienSo LIKE ? "
                + "OR k.hoTen LIKE ? OR k.soDienThoai LIKE ?) "
                + "AND (? = N'' OR p.trangThai = ?) ";

        if (tuNgay != null) {
            sql += "AND p.thoiGianTiepNhan >= ? ";
        }
        if (denNgay != null) {
            sql += "AND p.thoiGianTiepNhan < ? ";
        }
        sql += "ORDER BY p.thoiGianTiepNhan DESC";

        List<PhieuTiepNhanVaSuaChua> result = new ArrayList<>();
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, "%" + key + "%");
            statement.setString(3, "%" + key + "%");
            statement.setString(4, "%" + key + "%");
            statement.setString(5, "%" + key + "%");
            statement.setString(6, status);
            statement.setString(7, status);

            int index = 8;
            if (tuNgay != null) {
                statement.setTimestamp(index++, Timestamp.valueOf(tuNgay.atStartOfDay()));
            }
            if (denNgay != null) {
                statement.setTimestamp(index,
                        Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapPhieu(rs));
                }
            }
        }
        return result;
    }

    public PhieuTiepNhanVaSuaChua findById(String ma) throws SQLException {
        String sql = SELECT_JOIN + "WHERE p.maPhieuTNSC = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ma);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapPhieu(rs);
                }
                return null;
            }
        }
    }

    public String nextId() throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return nextId(connection);
        }
    }

    private String nextId(Connection connection) throws SQLException {
        String sql = "SELECT 'PTNSC' + RIGHT('00000' + CAST("
                + "COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maPhieuTNSC, 6, 25))), 0) + 1 "
                + "AS VARCHAR(25)), 5) FROM PhieuTiepNhanVaSuaChua";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    public boolean insert(PhieuTiepNhanVaSuaChua phieu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return insert(connection, phieu);
        }
    }

    /**
     * Lưu một lần tiếp nhận trong cùng transaction: khách mới (nếu có), xe mới (nếu có), phiếu và các dòng dịch vụ.
     * Lỗi ở bất kỳ bước nào sẽ hủy toàn bộ để không còn khách hoặc xe mồ côi.
     * Kỹ thuật viên từng dòng, người phụ trách và giờ dự kiến (khi lễ tân không tự chọn) do PhanCong_Dao tính
     * tại thời điểm tiếp nhận; trả về kết quả đó để báo lại cho lễ tân.
     */
    public PhanCong_Dao.KetQua taoTiepNhan(KhachHang khachMoi, Xe xeMoi, PhieuTiepNhanVaSuaChua phieu,
            List<DichVu> dichVu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            LocalDateTime duKienGoc = phieu.getThoiGianDuKien();
            NhanVien phuTrachGoc = phieu.getNhanVienPhuTrach();
            try {
                phieu.setMaPhieuTNSC(nextId(connection));
                if (khachMoi != null) {
                    khachMoi.setMaKH(nextMaKhachHang(connection));
                    xeMoi.setKhachHang(khachMoi);
                    insertKhachHang(connection, khachMoi);
                }
                if (xeMoi != null) {
                    insertXe(connection, xeMoi);
                }
                LocalDateTime hienTai = phieu.getThoiGianTiepNhan() != null
                        ? phieu.getThoiGianTiepNhan() : LocalDateTime.now();
                PhanCong_Dao.KetQua phanCong = new PhanCong_Dao().chon(connection, hienTai, dichVu);
                if (duKienGoc == null) {
                    phieu.setThoiGianDuKien(phanCong.getDuKien());
                }
                if (phanCong.getPhuTrach() != null) {
                    phieu.setNhanVienPhuTrach(phanCong.getPhuTrach());
                }
                insert(connection, phieu);
                LichHen_Dao.danhDauDaDen(connection, phieu.getXe().getBienSo());
                insertDichVu(connection, phieu.getMaPhieuTNSC(), dichVu, phanCong);
                connection.commit();
                return phanCong;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                phieu.setThoiGianDuKien(duKienGoc);
                phieu.setNhanVienPhuTrach(phuTrachGoc);
                throw ex;
            }
        }
    }

    private void insertKhachHang(Connection connection, KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (maKH, hoTen, soDienThoai, diaChi, email, loaiKhachHang, maSoThue) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, kh.getMaKH());
            statement.setString(2, kh.getHoTen());
            statement.setString(3, kh.getSoDienThoai());
            statement.setString(4, kh.getDiaChi());
            statement.setString(5, kh.getEmail());
            statement.setString(6, kh.getLoaiKhachHang().getText());
            statement.setString(7, kh.getMaSoThue());
            statement.executeUpdate();
        }
    }

    private void insertXe(Connection connection, Xe xe) throws SQLException {
        String sql = "INSERT INTO Xe (bienSo, soVIN, hangXe, dongXe, doiXe, mauSon, maKH) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, xe.getBienSo());
            statement.setString(2, xe.getSoVIN());
            statement.setString(3, xe.getHangXe());
            statement.setString(4, xe.getDongXe());
            statement.setObject(5, xe.getDoiXe(), Types.INTEGER);
            statement.setString(6, xe.getMauSon());
            statement.setString(7, xe.getKhachHang().getMaKH());
            statement.executeUpdate();
        }
    }

    private void insertDichVu(Connection connection, String maPhieu, List<DichVu> dichVu,
            PhanCong_Dao.KetQua phanCong) throws SQLException {
        String sql = "INSERT INTO ChiTietSuaChua_DichVu (maPhieuTNSC, maDichVu, tienCongThucTe, maNV_KyThuat) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (DichVu dv : dichVu) {
                NhanVien tho = phanCong.getPhanCong().get(dv);
                statement.setString(1, maPhieu);
                statement.setString(2, dv.getMaDichVu());
                statement.setDouble(3, dv.getTienCong());
                statement.setString(4, tho == null ? null : tho.getMaNV());
                statement.executeUpdate();
            }
        }
    }

    private String nextMaKhachHang(Connection connection) throws SQLException {
        String sql = "SELECT 'KH' + RIGHT('0000' + CAST("
                + "COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maKH, 3, 18))), 0) + 1 "
                + "AS VARCHAR(18)), 4) FROM KhachHang";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getString(1);
        }
    }

    /** Trả về null khi không có khách này. */
    public KhachHang findKhachTheoSdt(String sdt) throws SQLException {
        String sql = "SELECT " + KhachHang_Dao.COT_KHACH_HANG + " FROM KhachHang k WHERE k.soDienThoai = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sdt);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return KhachHang_Dao.docKhachHang(rs);
            }
        }
    }

    /** Trả về xe cùng chủ xe, hoặc null khi không có xe này. */
    public Xe findXeDayDu(String bienSo) throws SQLException {
        String sql = "SELECT " + COT_XE + " "
                + "FROM Xe x JOIN KhachHang k ON k.maKH = x.maKH WHERE x.bienSo = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bienSo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return docXe(rs);
            }
        }
    }

    /** Bước hiện tại của thanh tiến trình: 2 Chỉ định, 3 Xuất kho, 4 Thanh toán. */
    public int buocHienTai(String ma) throws SQLException {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM HoaDon WHERE maPhieuTNSC = ? AND trangThaiThanhToan <> N'Đã hủy') THEN 4 "
                + "WHEN EXISTS (SELECT 1 FROM PhieuYeuCauXuatKho WHERE maPhieuTNSC = ?) THEN 3 ELSE 2 END";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ma);
            statement.setString(2, ma);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Lưu KTV phụ trách và ghi chú kỹ thuật; trả về false khi phiếu đã bị khóa bởi hóa đơn. */
    public boolean capNhatPhanCong(String ma, String maNVPhuTrach, String ghiChuKyThuat) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return capNhatPhanCong(connection, ma, maNVPhuTrach, ghiChuKyThuat);
        }
    }

    boolean capNhatPhanCong(Connection connection, String ma, String maNVPhuTrach, String ghiChuKyThuat)
            throws SQLException {
        String sql = "UPDATE p SET maNV_PhuTrach = ?, ghiChuKyThuat = ? FROM PhieuTiepNhanVaSuaChua p "
                + "WHERE p.maPhieuTNSC = ? AND " + NOT_LOCKED;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maNVPhuTrach);
            statement.setString(2, ghiChuKyThuat);
            statement.setString(3, ma);
            return statement.executeUpdate() == 1;
        }
    }

    private boolean insert(Connection connection, PhieuTiepNhanVaSuaChua phieu) throws SQLException {
        phieu.kiemTraHopLe();
        String sql = "INSERT INTO PhieuTiepNhanVaSuaChua "
                + "(maPhieuTNSC, yeuCauBanDau, soKm, mucNhienLieu, trayXuoc, "
                + "taiSanTrenXe, thoiGianTiepNhan, thoiGianDuKien, "
                + "trangThai, bienSo, maNV_LapPhieu, danhGiaNoiThat, ghiChu, phuTungDuKien, uuTien, maNV_PhuTrach, maKH) "
                // Lưu chủ xe lúc tiếp nhận: xe đổi chủ về sau thì phiếu và hóa đơn cũ vẫn đúng người.
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT maKH FROM Xe WHERE bienSo = ?))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getMaPhieuTNSC());
            statement.setString(2, phieu.getYeuCauBanDau());
            statement.setInt(3, phieu.getSoKm());
            statement.setDouble(4, phieu.getMucNhienLieu());
            statement.setString(5, phieu.getTrayXuoc());
            statement.setString(6, phieu.getTaiSanTrenXe());
            if (phieu.getThoiGianTiepNhan() == null) {
                statement.setNull(7, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(7, Timestamp.valueOf(phieu.getThoiGianTiepNhan()));
            }
            if (phieu.getThoiGianDuKien() == null) {
                statement.setNull(8, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(8, Timestamp.valueOf(phieu.getThoiGianDuKien()));
            }
            statement.setString(9, phieu.getTrangThai().getText());
            statement.setString(10, phieu.getXe().getBienSo());
            statement.setString(11, phieu.getNhanVienLap().getMaNV());
            statement.setString(12, phieu.getDanhGiaNoiThat());
            statement.setString(13, phieu.getGhiChu());
            statement.setString(14, phieu.getPhuTungDuKien());
            statement.setString(15, phieu.getUuTien().getText());
            statement.setString(16, phieu.getNhanVienPhuTrach() == null ? null : phieu.getNhanVienPhuTrach().getMaNV());
            statement.setString(17, phieu.getXe().getBienSo());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(PhieuTiepNhanVaSuaChua phieu) throws SQLException {
        phieu.kiemTraHopLe();
        String sql = "UPDATE p SET yeuCauBanDau = ?, soKm = ?, mucNhienLieu = ?, "
                + "trayXuoc = ?, taiSanTrenXe = ?, thoiGianTiepNhan = ?, "
                + "thoiGianDuKien = ?, danhGiaNoiThat = ?, ghiChu = ?, phuTungDuKien = ?, uuTien = ? "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "WHERE p.maPhieuTNSC = ? AND " + NOT_LOCKED;
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phieu.getYeuCauBanDau());
            statement.setInt(2, phieu.getSoKm());
            statement.setDouble(3, phieu.getMucNhienLieu());
            statement.setString(4, phieu.getTrayXuoc());
            statement.setString(5, phieu.getTaiSanTrenXe());
            if (phieu.getThoiGianTiepNhan() == null) {
                statement.setNull(6, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(6, Timestamp.valueOf(phieu.getThoiGianTiepNhan()));
            }
            if (phieu.getThoiGianDuKien() == null) {
                statement.setNull(7, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(7, Timestamp.valueOf(phieu.getThoiGianDuKien()));
            }
            statement.setString(8, phieu.getDanhGiaNoiThat());
            statement.setString(9, phieu.getGhiChu());
            statement.setString(10, phieu.getPhuTungDuKien());
            statement.setString(11, phieu.getUuTien().getText());
            statement.setString(12, phieu.getMaPhieuTNSC());
            return statement.executeUpdate() == 1;
        }
    }

    /** Chỉ đổi khi phiếu còn ở trạng thái cũ và bước chuyển hợp lệ, nên hai máy cùng sửa không vượt luồng được. */
    public boolean capNhatTrangThai(String ma, TrangThaiPhieu cu, TrangThaiPhieu moi, String tenDangNhap) throws SQLException {
        if (!PhieuTiepNhanVaSuaChua.coTheChuyen(cu, moi)) {
            return false;
        }
        String sql = "UPDATE p SET trangThai = ? "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "WHERE p.maPhieuTNSC = ? AND p.trangThai = ? AND " + NOT_LOCKED;
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, moi.getText());
                statement.setString(2, ma);
                statement.setString(3, cu.getText());
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                new NhatKy_Dao().ghi(connection, tenDangNhap, "Cập nhật trạng thái phiếu", ma, cu.getText(), moi.getText());
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    /**
     * Hủy phiếu: giữ lại dữ liệu để đối soát thay vì xóa, và ghi nhật ký trong cùng transaction.
     * Trả về false khi phiếu đã hủy hoặc đã có hóa đơn, phiếu bàn giao, yêu cầu xuất kho chưa bị từ chối.
     */
    public boolean huy(String ma, String tenDangNhap) throws SQLException {
        String sqlCu = "SELECT trangThai FROM PhieuTiepNhanVaSuaChua WHERE maPhieuTNSC = ?";
        String sql = "UPDATE p SET trangThai = N'Đã hủy' FROM PhieuTiepNhanVaSuaChua p "
                + "WHERE p.maPhieuTNSC = ? AND p.trangThai <> N'Đã hủy' "
                + "AND NOT EXISTS (SELECT 1 FROM HoaDon h "
                + "WHERE h.maPhieuTNSC = p.maPhieuTNSC AND h.trangThaiThanhToan <> N'Đã hủy') "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuBanGiao b "
                + "WHERE b.maPhieuTNSC = p.maPhieuTNSC) "
                + "AND NOT EXISTS (SELECT 1 FROM PhieuYeuCauXuatKho y "
                + "WHERE y.maPhieuTNSC = p.maPhieuTNSC AND y.trangThai <> N'Từ chối')";
        try (Connection connection = ConnectDB.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement doc = connection.prepareStatement(sqlCu);
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                doc.setString(1, ma);
                String cu;
                try (ResultSet rs = doc.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    cu = rs.getString(1);
                }
                statement.setString(1, ma);
                if (statement.executeUpdate() != 1) {
                    connection.rollback();
                    return false;
                }
                new NhatKy_Dao().ghi(connection, tenDangNhap, "Hủy phiếu sửa chữa", ma, cu, TrangThaiPhieu.DA_HUY.getText());
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public boolean isKhoa(String ma) throws SQLException {
        String sql = "SELECT 1 FROM PhieuTiepNhanVaSuaChua p WHERE p.maPhieuTNSC = ? AND NOT (" + NOT_LOCKED + ")";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ma);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Xe findXeVaChuXe(String bienSo) throws SQLException {
        String sql = "SELECT " + COT_XE + " "
                + "FROM Xe x JOIN KhachHang k ON k.maKH = x.maKH "
                + "WHERE x.bienSo = ?";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bienSo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return docXe(rs);
            }
        }
    }

    static Xe docXe(ResultSet rs) throws SQLException {
        int doiXe = rs.getInt("doiXe");
        Integer nam = rs.wasNull() ? null : doiXe;
        return new Xe(rs.getString("bienSo"), rs.getString("soVIN"), rs.getString("hangXe"),
                rs.getString("dongXe"), nam, rs.getString("mauSon"), KhachHang_Dao.docKhachHang(rs));
    }

    public boolean coPhieuDangMo(String bienSo) throws SQLException {
        String sql = "SELECT 1 FROM PhieuTiepNhanVaSuaChua "
                + "WHERE bienSo = ? AND trangThai NOT IN (N'Hoàn thành', N'Đã hủy')";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bienSo);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String timBienSoTheoSdt(String sdt) throws SQLException {
        String sql = "SELECT TOP (1) x.bienSo FROM Xe x "
                + "JOIN KhachHang k ON k.maKH = x.maKH "
                + "WHERE k.soDienThoai = ? ORDER BY x.bienSo";
        try (Connection connection = ConnectDB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sdt);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bienSo");
                }
                return null;
            }
        }
    }

    private PhieuTiepNhanVaSuaChua mapPhieu(ResultSet rs) throws SQLException {
        Timestamp tiepNhan = rs.getTimestamp("thoiGianTiepNhan");
        Timestamp duKien = rs.getTimestamp("thoiGianDuKien");
        PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(
                rs.getString("maPhieuTNSC"),
                rs.getString("yeuCauBanDau"),
                rs.getInt("soKm"),
                rs.getDouble("mucNhienLieu"),
                rs.getString("trayXuoc"),
                rs.getString("taiSanTrenXe"),
                tiepNhan == null ? null : tiepNhan.toLocalDateTime(),
                duKien == null ? null : duKien.toLocalDateTime(),
                TrangThaiPhieu.fromDb(rs.getString("trangThai")),
                docXe(rs),
                NhanVien_Dao.docNhanVien(rs, "lap_"));
        phieu.setDanhGiaNoiThat(rs.getString("danhGiaNoiThat"));
        phieu.setGhiChu(rs.getString("ghiChu"));
        phieu.setPhuTungDuKien(rs.getString("phuTungDuKien"));
        phieu.setGhiChuKyThuat(rs.getString("ghiChuKyThuat"));
        phieu.setUuTien(MucUuTien.fromDb(rs.getString("uuTien")));
        phieu.setNhanVienPhuTrach(NhanVien_Dao.docNhanVien(rs, "pt_"));
        return phieu;
    }
}
