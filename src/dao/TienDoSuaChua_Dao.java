package dao;

import connection.ConnectDB;
import entity.ChiTietSuaChua_DichVu;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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

    /**
     * Tìm danh sách phiếu tiếp nhận & sửa chữa được giao cho Kỹ thuật viên (hoặc toàn bộ nếu maNVKyThuat rỗng/null).
     */
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
                + "WHERE (? = '' OR EXISTS (SELECT 1 FROM ChiTietSuaChua_DichVu ct WHERE ct.maPhieuTNSC = p.maPhieuTNSC AND (ct.maNV_KyThuat = ? OR ct.maNV_KyThuat IS NULL))) "
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

    /** Lấy danh sách hạng mục dịch vụ của 1 phiếu sửa chữa. */
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

    /** Kiểm tra phiếu sửa chữa đã có Hóa đơn thanh toán hay chưa (khóa dữ liệu không cho sửa). */
    public boolean isPhieuDaThanhToan(String maPhieuTNSC) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon WHERE maPhieuTNSC = ? AND trangThaiThanhToan = N'Đã thanh toán'";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cập nhật trạng thái phiếu sửa chữa và đồng bộ trạng thái các hạng mục dịch vụ (theo Sequence Diagram UC005).
     */
    public boolean capNhatTrangThaiPhieu(String maPhieuTNSC, String trangThaiMoi, String maNVKyThuat) throws SQLException {
        String sqlPhieu = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = ? WHERE maPhieuTNSC = ?";
        String sqlDichVu = "UPDATE ChiTietSuaChua_DichVu SET trangThaiTienDo = ? "
                + "WHERE maPhieuTNSC = ? AND (? = '' OR maNV_KyThuat = ? OR maNV_KyThuat IS NULL)";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psP = c.prepareStatement(sqlPhieu);
                 PreparedStatement psDV = c.prepareStatement(sqlDichVu)) {

                psP.setString(1, trangThaiMoi);
                psP.setString(2, maPhieuTNSC);
                psP.executeUpdate();

                // Đồng bộ trạng thái hạng mục dịch vụ nếu chuyển sang Đang sửa hoặc Hoàn thành
                if ("Đang sửa".equals(trangThaiMoi) || "Hoàn thành".equals(trangThaiMoi)) {
                    String maKTV = maNVKyThuat == null ? "" : maNVKyThuat.trim();
                    psDV.setString(1, trangThaiMoi);
                    psDV.setString(2, maPhieuTNSC);
                    psDV.setString(3, maKTV);
                    psDV.setString(4, maKTV);
                    psDV.executeUpdate();
                }

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
     * Cập nhật tiến độ cho từng hạng mục dịch vụ cụ thể; nếu tất cả hạng mục đều Hoàn thành thì tự động cập nhật Phiếu sang Hoàn thành (Sequence Diagram Hình 16).
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