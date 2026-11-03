package dao;

import connection.ConnectDB;
import entity.ChiTietSuaChua_DichVu;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ màn hình Trang chủ Kỹ thuật viên và Quản lý tiến độ sửa chữa (UC005).
 */
public class TienDoSuaChua_Dao {

    public NhanVien findDefaultKyThuatVien() throws SQLException {
        String sql = "SELECT TOP 1 maNV, hoTen, soDienThoai, diaChi, viTriLamViec "
                + "FROM NhanVien WHERE viTriLamViec = N'Kỹ thuật viên' ORDER BY maNV ASC";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            if (r.next()) {
                return new NhanVien(
                        r.getString("maNV"),
                        r.getString("hoTen"),
                        r.getString("soDienThoai"),
                        r.getString("diaChi"),
                        r.getString("viTriLamViec")
                );
            }
        }
        return null;
    }

    public List<PhieuTiepNhanVaSuaChua> searchPhieuĐượcGiao(String maNVKyThuat, String keyword, String trangThaiFilter) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String tt = trangThaiFilter == null || trangThaiFilter.equals("Tất cả") ? "" : trangThaiFilter.trim();
        String maKTV = maNVKyThuat == null ? "" : maNVKyThuat.trim();

        String sql = "SELECT p.maPhieuTNSC, p.yeuCauBanDau, p.soKm, p.mucNhienLieu, p.trayXuoc, p.taiSanTrenXe, "
                + "       p.thoiGianTiepNhan, p.thoiGianDuKien, p.trangThai, p.bienSo, p.maNV_LapPhieu, "
                + "       x.hangXe, x.dongXe, x.doiXe, x.mauSon, kh.hoTen AS tenKhachHang, kh.soDienThoai AS sdtKH "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "JOIN Xe x ON x.bienSo = p.bienSo "
                + "JOIN KhachHang kh ON kh.maKH = x.maKH "
                + "WHERE p.trangThai <> N'Đã hủy' "
                + "  AND (? = '' OR EXISTS (SELECT 1 FROM ChiTietSuaChua_DichVu ct WHERE ct.maPhieuTNSC = p.maPhieuTNSC AND (ct.maNV_KyThuat = ? OR ct.maNV_KyThuat IS NULL))) "
                + "  AND (? = '' OR p.maPhieuTNSC LIKE ? OR p.bienSo LIKE ? OR x.hangXe LIKE ? OR x.dongXe LIKE ? "
                + "       OR EXISTS (SELECT 1 FROM ChiTietSuaChua_DichVu ct2 JOIN DichVu dv2 ON dv2.maDichVu = ct2.maDichVu "
                + "                  WHERE ct2.maPhieuTNSC = p.maPhieuTNSC AND dv2.tenDichVu LIKE ?)) "
                + "  AND (? = '' OR p.trangThai = ? OR (? = N'Chờ sửa' AND p.trangThai = N'Chờ kiểm tra')) "
                + "ORDER BY p.thoiGianTiepNhan DESC, p.maPhieuTNSC DESC";

        List<PhieuTiepNhanVaSuaChua> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maKTV);
            ps.setString(2, maKTV);
            ps.setString(3, k);
            ps.setString(4, "%" + k + "%");
            ps.setString(5, "%" + k + "%");
            ps.setString(6, "%" + k + "%");
            ps.setString(7, "%" + k + "%");
            ps.setString(8, "%" + k + "%");
            ps.setString(9, tt);
            ps.setString(10, tt);
            ps.setString(11, tt);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuTiepNhanVaSuaChua p = mapPhieu(rs);
                    List<ChiTietSuaChua_DichVu> dvList = findDichVuByPhieu(c, p.getMaPhieuTNSC(), maKTV);
                    p.setDanhSachDichVu(dvList);
                    p.setTongSoHangMuc(dvList.size());

                    int done = 0;
                    StringBuilder sb = new StringBuilder();
                    for (ChiTietSuaChua_DichVu ct : dvList) {
                        if ("Hoàn thành".equalsIgnoreCase(ct.getTrangThaiTienDo())) {
                            done++;
                        }
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(ct.getTenDichVu());
                    }
                    p.setSoHangMucHoanThanh(done);
                    p.setTomTatHangMuc(sb.length() > 0 ? sb.toString() : p.getYeuCauBanDau());
                    list.add(p);
                }
            }
        }
        return list;
    }

    public List<ChiTietSuaChua_DichVu> findDichVuByPhieu(String maPhieuTNSC, String maNVKyThuat) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return findDichVuByPhieu(c, maPhieuTNSC, maNVKyThuat);
        }
    }

    private List<ChiTietSuaChua_DichVu> findDichVuByPhieu(Connection c, String maPhieuTNSC, String maNVKyThuat) throws SQLException {
        String sql = "SELECT ct.maPhieuTNSC, ct.maDichVu, ct.maNV_KyThuat, ct.trangThaiTienDo, ct.tienCongThucTe, "
                + "       dv.tenDichVu, nv.hoTen AS tenKTV "
                + "FROM ChiTietSuaChua_DichVu ct "
                + "JOIN DichVu dv ON dv.maDichVu = ct.maDichVu "
                + "LEFT JOIN NhanVien nv ON nv.maNV = ct.maNV_KyThuat "
                + "WHERE ct.maPhieuTNSC = ? "
                + "ORDER BY ct.maDichVu ASC";
        List<ChiTietSuaChua_DichVu> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietSuaChua_DichVu ct = new ChiTietSuaChua_DichVu(
                            rs.getString("maPhieuTNSC"),
                            rs.getString("maDichVu"),
                            rs.getString("maNV_KyThuat"),
                            rs.getString("trangThaiTienDo"),
                            rs.getDouble("tienCongThucTe")
                    );
                    ct.setTenDichVu(rs.getString("tenDichVu"));
                    ct.setTenKyThuatVien(rs.getString("tenKTV"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    /**
     * Kiểm tra phiếu sửa chữa đã có Hóa đơn còn hiệu lực hay chưa (trangThaiThanhToan <> N'Đã hủy').
     * Nếu hóa đơn bị hủy ('Đã hủy'), hệ thống cho phép mở khóa để thao tác lại.
     */
    public boolean isPhieuDaThanhToan(String maPhieuTNSC) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon WHERE maPhieuTNSC = ? AND trangThaiThanhToan <> N'Đã hủy'";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cập nhật trạng thái phiếu sửa chữa (đảm bảo lưu đúng chuỗi "Hoàn thành")
     * và ghi nhận vào Audit Log (bảng NhatKy nếu có).
     */
    public boolean capNhatTrangThaiPhieu(String maPhieuTNSC, String trangThaiMoi, String maNVKyThuat) throws SQLException {
        // Chuẩn hóa chuỗi trạng thái lưu vào DB để khớp 100% với bên Lập Hóa Đơn
        String dbTrangThai = chuanHoaTrangThaiDB(trangThaiMoi);

        String sqlPhieu = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = ? WHERE maPhieuTNSC = ?";
        String sqlDichVu = "UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = ? "
                + "WHERE maPhieuTNSC = ? AND (? = '' OR maNV_KyThuat = ? OR maNV_KyThuat IS NULL)";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psP = c.prepareStatement(sqlPhieu);
                 PreparedStatement psDV = c.prepareStatement(sqlDichVu)) {

                psP.setString(1, dbTrangThai);
                psP.setString(2, maPhieuTNSC);
                psP.executeUpdate();

                if ("Đang sửa".equals(dbTrangThai) || "Hoàn thành".equals(dbTrangThai)) {
                    String maKTV = maNVKyThuat == null ? "" : maNVKyThuat.trim();
                    psDV.setString(1, dbTrangThai);
                    psDV.setString(2, maPhieuTNSC);
                    psDV.setString(3, maKTV);
                    psDV.setString(4, maKTV);
                    psDV.executeUpdate();
                }

                ghiNhatKyAnToan(c, maNVKyThuat, "Cập nhật tiến độ sửa chữa",
                        "Cập nhật trạng thái phiếu " + maPhieuTNSC + " sang '" + dbTrangThai + "'");

                c.commit();
                c.setAutoCommit(oldAuto);
                return true;
            } catch (SQLException ex) {
                c.rollback();
                c.setAutoCommit(oldAuto);
                throw ex;
            }
        }
    }

    /**
     * Cập nhật tiến độ cho từng hạng mục dịch vụ; nếu tất cả hạng mục đều Hoàn thành
     * thì tự động cập nhật Phiếu sang đúng chữ "Hoàn thành".
     */
    public boolean capNhatTienDoHangMuc(String maPhieuTNSC, String maDichVu, String trangThaiHangMucMoi) throws SQLException {
        String sqlUpdateDV = "UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = ? WHERE maPhieuTNSC = ? AND maDichVu = ?";
        String sqlCheckNotDone = "SELECT COUNT(*) FROM ChiTietSuaChua_DichVu WHERE maPhieuTNSC = ? AND trangThaiTienDo <> N'Hoàn thành'";
        String sqlUpdatePhieu = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = ? WHERE maPhieuTNSC = ?";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psDV = c.prepareStatement(sqlUpdateDV);
                 PreparedStatement psCheck = c.prepareStatement(sqlCheckNotDone);
                 PreparedStatement psPhieu = c.prepareStatement(sqlUpdatePhieu)) {

                psDV.setString(1, trangThaiHangMucMoi);
                psDV.setString(2, maPhieuTNSC);
                psDV.setString(3, maDichVu);
                psDV.executeUpdate();

                psCheck.setString(1, maPhieuTNSC);
                int notDoneCount = 0;
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) notDoneCount = rs.getInt(1);
                }

                String trangThaiPhieuMoi = (notDoneCount == 0) ? "Hoàn thành" : "Đang sửa";
                psPhieu.setString(1, trangThaiPhieuMoi);
                psPhieu.setString(2, maPhieuTNSC);
                psPhieu.executeUpdate();

                ghiNhatKyAnToan(c, null, "Cập nhật hạng mục sửa chữa",
                        "Cập nhật hạng mục " + maDichVu + " của phiếu " + maPhieuTNSC
                                + " sang '" + trangThaiHangMucMoi + "' (Trạng thái phiếu: " + trangThaiPhieuMoi + ")");

                c.commit();
                c.setAutoCommit(oldAuto);
                return true;
            } catch (SQLException ex) {
                c.rollback();
                c.setAutoCommit(oldAuto);
                throw ex;
            }
        }
    }

    private String chuanHoaTrangThaiDB(String tt) {
        if (tt == null) return "Chờ kiểm tra";
        String s = tt.trim();
        if ("Chờ sửa".equalsIgnoreCase(s)) return "Chờ kiểm tra";
        if ("Hoàn thành".equalsIgnoreCase(s)) return "Hoàn thành";
        if ("Đang sửa".equalsIgnoreCase(s)) return "Đang sửa";
        if ("Chờ phụ tùng".equalsIgnoreCase(s)) return "Chờ phụ tùng";
        return s;
    }

    /**
     * Ghi Audit Log vào bảng NhatKy một cách an toàn (không làm hỏng transaction nếu schema bảng NhatKy khác biệt).
     */
    /**
     * Ghi Audit Log vào bảng NhatKy một cách an toàn tuyệt đối.
     * Tự động kiểm tra kết nối độc lập và không làm gián đoạn giao dịch chính nếu bảng NhatKy bận hoặc khác tên cột.
     */
    public static void ghiNhatKyAnToan(Connection c, String maNV, String hanhDong, String chiTiet) {
        String nv = (maNV == null || maNV.trim().isEmpty()) ? "NV0005" : maNV.trim();
        // Dùng kết nối riêng biệt hoặc kết nối hiện tại để ghi log, không làm ảnh hưởng transaction nghiệp vụ chính
        String sql = "INSERT INTO NhatKy (thoiGian, maNV, hanhDong, chiTiet) VALUES (GETDATE(), ?, ?, ?)";

        try {
            boolean useExistingConn = (c != null && !c.isClosed());
            Connection conn = useExistingConn ? c : connection.ConnectDB.getConnection();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nv);
                ps.setString(2, hanhDong);
                ps.setString(3, chiTiet);
                ps.executeUpdate();
            } finally {
                if (!useExistingConn && conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException ignored) {
            // Dự phòng trường hợp tên cột trong CSDL khác (ví dụ: thoi_gian thay vì thoiGian)
            try {
                String sqlAlt = "INSERT INTO NhatKy VALUES (GETDATE(), ?, ?, ?)";
                Connection connAlt = (c != null && !c.isClosed()) ? c : connection.ConnectDB.getConnection();
                try (PreparedStatement psAlt = connAlt.prepareStatement(sqlAlt)) {
                    psAlt.setString(1, nv);
                    psAlt.setString(2, hanhDong);
                    psAlt.setString(3, chiTiet);
                    psAlt.executeUpdate();
                } finally {
                    if (c == null && connAlt != null) connAlt.close();
                }
            } catch (SQLException ex2) {
                // Nuốt lỗi log hoàn toàn để không bao giờ làm crash luồng nghiệp vụ chính của người dùng
            }
        }
    }

    private PhieuTiepNhanVaSuaChua mapPhieu(ResultSet rs) throws SQLException {
        Timestamp tsTiepNhan = rs.getTimestamp("thoiGianTiepNhan");
        Timestamp tsDuKien = rs.getTimestamp("thoiGianDuKien");

        PhieuTiepNhanVaSuaChua p = new PhieuTiepNhanVaSuaChua(
                rs.getString("maPhieuTNSC"),
                rs.getString("yeuCauBanDau"),
                rs.getInt("soKm"),
                rs.getDouble("mucNhienLieu"),
                rs.getString("trayXuoc"),
                rs.getString("taiSanTrenXe"),
                tsTiepNhan != null ? tsTiepNhan.toLocalDateTime() : null,
                tsDuKien != null ? tsDuKien.toLocalDateTime() : null,
                rs.getString("trangThai"),
                rs.getString("bienSo"),
                rs.getString("maNV_LapPhieu")
        );
        p.setHangXe(rs.getString("hangXe"));
        p.setDongXe(rs.getString("dongXe"));
        p.setDoiXe(rs.getInt("doiXe"));
        p.setMauSon(rs.getString("mauSon"));
        p.setTenKhachHang(rs.getString("tenKhachHang"));
        p.setSdtKhachHang(rs.getString("sdtKH"));
        return p;
    }
}