package dao;

import connection.ConnectDB;
import entity.ChiTietSuaChua_DichVu;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import entity.TrangThaiTienDo;
import entity.VaiTro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ màn hình Trang chủ Kỹ thuật viên và Quản lý tiến độ sửa chữa (UC005).
 */
public class TienDoSuaChua_Dao {

    public NhanVien findDefaultKyThuatVien() throws SQLException {
        util.PhienDangNhap phien = util.PhienDangNhap.getHienTai();
        if (phien != null && phien.getMaNV() != null && !phien.getMaNV().trim().isEmpty()) {
            String sqlPhien = "SELECT maNV, hoTen, soDienThoai, diaChi, viTriLamViec FROM NhanVien WHERE maNV = ?";
            try (Connection c = ConnectDB.getConnection();
                 PreparedStatement p = c.prepareStatement(sqlPhien)) {
                p.setString(1, phien.getMaNV().trim());
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) {
                        return new NhanVien(
                                r.getString("maNV"),
                                r.getString("hoTen"),
                                r.getString("soDienThoai"),
                                r.getString("diaChi"),
                                VaiTro.fromDb(r.getString("viTriLamViec"))
                        );
                    }
                }
            }
        }
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
                        VaiTro.fromDb(r.getString("viTriLamViec"))
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
                + "       p.thoiGianTiepNhan, p.thoiGianDuKien, p.trangThai, "
                + PhieuTiepNhanVaSuaChua_Dao.COT_XE + ", " + NhanVien_Dao.cotNhanVien("nl", "lap_") + " "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "JOIN Xe x ON x.bienSo = p.bienSo "
                + "JOIN KhachHang k ON k.maKH = p.maKH "
                + "JOIN NhanVien nl ON nl.maNV = p.maNV_LapPhieu "
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

            java.util.Map<String, PhieuTiepNhanVaSuaChua> theoMa = new java.util.LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuTiepNhanVaSuaChua p = mapPhieu(rs);
                    theoMa.put(p.getMaPhieuTNSC(), p);
                }
            }
            java.util.Map<String, List<ChiTietSuaChua_DichVu>> dichVuTheoPhieu = docDichVuPhieuChuaHuy(c, theoMa);
            for (PhieuTiepNhanVaSuaChua p : theoMa.values()) {
                List<ChiTietSuaChua_DichVu> dvList = dichVuTheoPhieu.getOrDefault(p.getMaPhieuTNSC(), new ArrayList<>());
                p.setDanhSachDichVu(dvList);
                p.setTongSoHangMuc(dvList.size());

                int done = 0;
                StringBuilder sb = new StringBuilder();
                for (ChiTietSuaChua_DichVu ct : dvList) {
                    if (ct.getTrangThaiTienDo() == TrangThaiTienDo.HOAN_THANH) {
                        done++;
                    }
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(ct.getDichVu().getTenDichVu());
                }
                p.setSoHangMucHoanThanh(done);
                p.setTomTatHangMuc(sb.length() > 0 ? sb.toString() : p.getYeuCauBanDau());
                list.add(p);
            }
        }
        return list;
    }

    /** Dịch vụ của mọi phiếu chưa hủy trong một lần đọc, chia theo mã phiếu; chỉ giữ phiếu có trong danh sách. */
    private static java.util.Map<String, List<ChiTietSuaChua_DichVu>> docDichVuPhieuChuaHuy(Connection c,
            java.util.Map<String, PhieuTiepNhanVaSuaChua> theoMa) throws SQLException {
        java.util.Map<String, List<ChiTietSuaChua_DichVu>> ketQua = new java.util.HashMap<>();
        if (theoMa.isEmpty()) {
            return ketQua;
        }
        String sql = "SELECT c.maPhieuTNSC AS maPhieuDv, " + ChiTietSuaChua_DichVu_Dao.COT_DICH_VU + ChiTietSuaChua_DichVu_Dao.TU_DICH_VU
                + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = c.maPhieuTNSC "
                + "WHERE p.trangThai <> N'Đã hủy' "
                + "ORDER BY c.maPhieuTNSC, c.maDichVu";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhieuTiepNhanVaSuaChua phieu = theoMa.get(rs.getString("maPhieuDv"));
                if (phieu != null) {
                    ketQua.computeIfAbsent(phieu.getMaPhieuTNSC(), k -> new ArrayList<>())
                            .add(ChiTietSuaChua_DichVu_Dao.docDichVu(rs, phieu));
                }
            }
        }
        return ketQua;
    }

    public List<ChiTietSuaChua_DichVu> findDichVuByPhieu(String maPhieuTNSC, String maNVKyThuat) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return findDichVuByPhieu(c, new PhieuTiepNhanVaSuaChua(maPhieuTNSC), maNVKyThuat);
        }
    }

    private List<ChiTietSuaChua_DichVu> findDichVuByPhieu(Connection c, PhieuTiepNhanVaSuaChua phieu, String maNVKyThuat) throws SQLException {
        String sql = "SELECT " + ChiTietSuaChua_DichVu_Dao.COT_DICH_VU + ChiTietSuaChua_DichVu_Dao.TU_DICH_VU
                + "WHERE c.maPhieuTNSC = ? "
                + "ORDER BY c.maDichVu ASC";
        List<ChiTietSuaChua_DichVu> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phieu.getMaPhieuTNSC());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(ChiTietSuaChua_DichVu_Dao.docDichVu(rs, phieu));
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
     * Ghi tiến độ một hạng mục và trạng thái phiếu đã tính ở tầng nghiệp vụ, trong cùng một transaction.
     * Trả về false khi phiếu đã có hóa đơn hoặc đã hủy (màn hình có thể đang hiện dữ liệu cũ).
     */
    public boolean capNhatTienDoHangMuc(String maPhieuTNSC, String maDichVu, TrangThaiTienDo trangThaiHangMucMoi,
            TrangThaiPhieu trangThaiPhieuMoi, String tenDangNhap) throws SQLException {
        String sqlUpdateDV = "UPDATE d SET trangThaiTienDo = ? FROM ChiTietSuaChua_DichVu d "
                + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = d.maPhieuTNSC "
                + "WHERE d.maPhieuTNSC = ? AND d.maDichVu = ? AND " + PhieuTiepNhanVaSuaChua_Dao.NOT_LOCKED;
        String sqlUpdatePhieu = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = ? WHERE maPhieuTNSC = ?";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psDV = c.prepareStatement(sqlUpdateDV);
                 PreparedStatement psPhieu = c.prepareStatement(sqlUpdatePhieu)) {

                psDV.setString(1, trangThaiHangMucMoi.getText());
                psDV.setString(2, maPhieuTNSC);
                psDV.setString(3, maDichVu);
                if (psDV.executeUpdate() != 1) {
                    c.rollback();
                    c.setAutoCommit(oldAuto);
                    return false;
                }

                psPhieu.setString(1, trangThaiPhieuMoi.getText());
                psPhieu.setString(2, maPhieuTNSC);
                psPhieu.executeUpdate();

                new NhatKy_Dao().ghi(c, tenDangNhap, "Cập nhật hạng mục sửa chữa", maPhieuTNSC, null,
                        maDichVu + ": " + trangThaiHangMucMoi + " (phiếu: " + trangThaiPhieuMoi + ")");

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

    /** Phiếu còn yêu cầu phụ tùng chờ thủ kho duyệt. */
    public boolean coYeuCauPhuTungCho(String maPhieuTNSC) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM PhieuYeuCauXuatKho WHERE maPhieuTNSC = ? AND trangThai = N'Chờ xuất'")) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
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
                TrangThaiPhieu.fromDb(rs.getString("trangThai")),
                PhieuTiepNhanVaSuaChua_Dao.docXe(rs),
                NhanVien_Dao.docNhanVien(rs, "lap_")
        );
        return p;
    }
}