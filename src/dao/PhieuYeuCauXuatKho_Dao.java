package dao;

import connection.ConnectDB;
import entity.ChiTietYeuCauXuatKho;
import entity.PhieuYeuCauXuatKho;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuYeuCauXuatKho_Dao {

    public void ensureSampleDataIfEmpty() {
        String checkSql = "SELECT COUNT(*) FROM PhieuYeuCauXuatKho";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(checkSql)) {
            if (r.next() && r.getInt(1) == 0) {
                String insPYC = "INSERT INTO PhieuYeuCauXuatKho(maPhieuYC, ngayYeuCau, ngayXuat, trangThai, maPhieuTNSC, maNV_KyThuat, maNV_ThuKho) VALUES "
                        + "('PYC00001', '2026-10-20 09:30:00', '2026-10-20 09:45:00', N'Đã xuất', 'PTNSC00001', 'NV0005', 'NV0004'), "
                        + "('PYC00002', '2026-10-23 10:15:00', NULL, N'Chờ xuất', 'PTNSC00002', 'NV0005', NULL), "
                        + "('PYC00003', '2026-10-23 11:00:00', NULL, N'Chờ xuất', 'PTNSC00002', 'NV0006', NULL)";
                String insCT = "INSERT INTO ChiTietYeuCauXuatKho(maPhieuYC, maSKU, soLuong) VALUES "
                        + "('PYC00001', 'SKU_OIL01', 1), "
                        + "('PYC00001', 'SKU_FLT01', 1), "
                        + "('PYC00002', 'SKU_BRK01', 1), "
                        + "('PYC00002', 'SKU_FLT02', 1), "
                        + "('PYC00003', 'SKU_SPK01', 6)";
                s.executeUpdate(insPYC);
                s.executeUpdate(insCT);
            }
        } catch (SQLException ignored) {}
    }

    public String nextId() throws SQLException {
        String sql = "SELECT 'PYC' + RIGHT('00000' + CAST(COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maPhieuYC, 4, 20))), 0) + 1 AS VARCHAR(20)), 5) "
                + "FROM PhieuYeuCauXuatKho";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getString(1);
        }
    }

    public int countByTrangThai(String trangThai) throws SQLException {
        ensureSampleDataIfEmpty();
        String sql = "SELECT COUNT(*) FROM PhieuYeuCauXuatKho WHERE trangThai = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThai);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public int countDaXuatHomNay() throws SQLException {
        ensureSampleDataIfEmpty();
        String sql = "SELECT COUNT(*) FROM PhieuYeuCauXuatKho WHERE trangThai = N'Đã xuất'";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public List<PhieuYeuCauXuatKho> searchYeuCauThongMinh(String keyword, String trangThai) throws SQLException {
        ensureSampleDataIfEmpty();
        String k = keyword == null ? "" : keyword.trim();
        String tt = (trangThai == null || "Tất cả".equals(trangThai)) ? "" : trangThai.trim();

        String sql = "SELECT yc.maPhieuYC, yc.ngayYeuCau, yc.ngayXuat, yc.trangThai, yc.maPhieuTNSC, yc.maNV_KyThuat, yc.maNV_ThuKho "
                + "FROM PhieuYeuCauXuatKho yc "
                + "LEFT JOIN NhanVien nv ON nv.maNV = yc.maNV_KyThuat "
                + "WHERE (? = '' OR yc.maPhieuYC LIKE ? OR yc.maPhieuTNSC LIKE ? OR nv.hoTen LIKE ? "
                + "       OR EXISTS (SELECT 1 FROM ChiTietYeuCauXuatKho ct JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "                  WHERE ct.maPhieuYC = yc.maPhieuYC AND (ct.maSKU LIKE ? OR pt.tenPhuTung LIKE ?))) "
                + "AND (? = '' OR yc.trangThai = ?) "
                + "ORDER BY CASE WHEN yc.trangThai = N'Chờ xuất' THEN 0 ELSE 1 END, yc.ngayYeuCau DESC, yc.maPhieuYC DESC";

        List<PhieuYeuCauXuatKho> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, "%" + k + "%");
            ps.setString(3, "%" + k + "%");
            ps.setString(4, "%" + k + "%");
            ps.setString(5, "%" + k + "%");
            ps.setString(6, "%" + k + "%");
            ps.setString(7, tt);
            ps.setString(8, tt);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp tsYC = rs.getTimestamp("ngayYeuCau");
                    Timestamp tsXuat = rs.getTimestamp("ngayXuat");
                    PhieuYeuCauXuatKho yc = new PhieuYeuCauXuatKho(
                            rs.getString("maPhieuYC"),
                            tsYC != null ? tsYC.toLocalDateTime() : LocalDateTime.now(),
                            tsXuat != null ? tsXuat.toLocalDateTime() : null,
                            rs.getString("trangThai"),
                            rs.getString("maPhieuTNSC"),
                            rs.getString("maNV_KyThuat"),
                            rs.getString("maNV_ThuKho")
                    );
                    list.add(yc);
                }
            }
        }
        return list;
    }

    public String getTenKTV(String maNV) throws SQLException {
        String sql = "SELECT hoTen FROM NhanVien WHERE maNV = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : maNV;
            }
        }
    }

    public String getThongTinXe(String maPhieuTNSC) throws SQLException {
        String sql = "SELECT x.hangXe, x.dongXe, x.doiXe, x.bienSo "
                + "FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo = p.bienSo "
                + "WHERE p.maPhieuTNSC = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String h = rs.getString("hangXe");
                    String d = rs.getString("dongXe");
                    int doi = rs.getInt("doiXe");
                    String bs = rs.getString("bienSo");
                    return h + " " + d + " • " + doi + " • " + bs;
                }
            }
        }
        return "---";
    }

    public String getTomTatPhuTungYeuCau(String maPhieuYC) throws SQLException {
        String sql = "SELECT pt.tenPhuTung, ct.soLuong "
                + "FROM ChiTietYeuCauXuatKho ct JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "WHERE ct.maPhieuYC = ?";
        StringBuilder sb = new StringBuilder();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuYC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString("tenPhuTung")).append(" x").append(rs.getInt("soLuong"));
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "---";
    }

    public List<ChiTietYeuCauXuatKho> getChiTietByMaPhieuYC(String maPhieuYC) throws SQLException {
        String sql = "SELECT ct.maPhieuYC, ct.maSKU, ct.soLuong, pt.tenPhuTung, pt.hangSanXuat, pt.viTriLuuKho, pt.soLuongTon, pt.nguongCanhBao "
                + "FROM ChiTietYeuCauXuatKho ct "
                + "JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "WHERE ct.maPhieuYC = ? ORDER BY ct.maSKU ASC";
        List<ChiTietYeuCauXuatKho> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuYC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietYeuCauXuatKho ct = new ChiTietYeuCauXuatKho(
                            rs.getString("maPhieuYC"),
                            rs.getString("maSKU"),
                            rs.getInt("soLuong")
                    );
                    ct.setTenPhuTung(rs.getString("tenPhuTung"));
                    ct.setHangSanXuat(rs.getString("hangSanXuat"));
                    ct.setViTriKho(rs.getString("viTriLuuKho"));
                    ct.setSoLuongTon(rs.getInt("soLuongTon"));
                    ct.setNguongCanhBao(rs.getInt("nguongCanhBao"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    /**
     * Kiểm tra phiếu sửa chữa đã có Hóa đơn còn hiệu lực hay chưa (loại trừ trạng thái 'Đã hủy').
     */
    public boolean kiemTraTonTaiHoaDonHopLe(String maPhieuTNSC) throws SQLException {
        String sql = "SELECT 1 FROM HoaDon WHERE maPhieuTNSC = ? AND trangThaiThanhToan <> N'Đã hủy'";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuTNSC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean insertWithDetails(PhieuYeuCauXuatKho phieu, List<ChiTietYeuCauXuatKho> chiTietList) throws SQLException {
        if (kiemTraTonTaiHoaDonHopLe(phieu.getMaPhieuTNSC())) {
            throw new SQLException("Phiếu sửa chữa " + phieu.getMaPhieuTNSC() + " đã có hóa đơn còn hiệu lực, không thể tạo thêm yêu cầu phụ tùng!");
        }

        String sqlPhieu = "INSERT INTO PhieuYeuCauXuatKho(maPhieuYC, ngayYeuCau, ngayXuat, trangThai, maPhieuTNSC, maNV_KyThuat, maNV_ThuKho) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietYeuCauXuatKho(maPhieuYC, maSKU, soLuong) VALUES(?, ?, ?)";
        String sqlUpdatePTNSC = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = N'Chờ phụ tùng' "
                + "WHERE maPhieuTNSC = ? AND trangThai <> N'Hoàn thành'";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psPhieu = c.prepareStatement(sqlPhieu);
                 PreparedStatement psCT = c.prepareStatement(sqlChiTiet);
                 PreparedStatement psUpdate = c.prepareStatement(sqlUpdatePTNSC)) {

                psPhieu.setString(1, phieu.getMaPhieuYC());
                psPhieu.setTimestamp(2, Timestamp.valueOf(phieu.getNgayYeuCau()));
                if (phieu.getNgayXuat() != null) {
                    psPhieu.setTimestamp(3, Timestamp.valueOf(phieu.getNgayXuat()));
                } else {
                    psPhieu.setNull(3, Types.TIMESTAMP);
                }
                psPhieu.setString(4, phieu.getTrangThai());
                psPhieu.setString(5, phieu.getMaPhieuTNSC());
                psPhieu.setString(6, phieu.getMaNVKyThuat());
                if (phieu.getMaNVThuKho() != null && !phieu.getMaNVThuKho().isEmpty()) {
                    psPhieu.setString(7, phieu.getMaNVThuKho());
                } else {
                    psPhieu.setNull(7, Types.VARCHAR);
                }
                psPhieu.executeUpdate();

                for (ChiTietYeuCauXuatKho ct : chiTietList) {
                    psCT.setString(1, phieu.getMaPhieuYC());
                    psCT.setString(2, ct.getMaSKU());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.addBatch();
                }
                psCT.executeBatch();

                psUpdate.setString(1, phieu.getMaPhieuTNSC());
                psUpdate.executeUpdate();

                TienDoSuaChua_Dao.ghiNhatKyAnToan(c, phieu.getMaNVKyThuat(),
                        "Lập phiếu yêu cầu xuất kho",
                        "Tạo phiếu yêu cầu " + phieu.getMaPhieuYC() + " cho phiếu sửa chữa " + phieu.getMaPhieuTNSC());

                c.commit();
                c.setAutoCommit(oldAutoCommit);
                return true;
            } catch (SQLException ex) {
                c.rollback();
                c.setAutoCommit(oldAutoCommit);
                throw ex;
            }
        }
    }

    public boolean duyetXuatKho(String maPhieuYC, String maPhieuTNSC, String maNVThuKho, List<ChiTietYeuCauXuatKho> chiTietList) throws SQLException {
        // Chặn ngay ở tầng DAO nếu phiếu sửa chữa đã có hóa đơn hợp lệ (khác 'Đã hủy')
        if (kiemTraTonTaiHoaDonHopLe(maPhieuTNSC)) {
            throw new SQLException("Phiếu sửa chữa " + maPhieuTNSC + " đã có hóa đơn còn hiệu lực. Vui lòng hủy hóa đơn trước khi duyệt xuất kho!");
        }

        String sqlTruTon = "UPDATE PhuTung SET soLuongTon = soLuongTon - ? WHERE maSKU = ? AND soLuongTon >= ?";
        String sqlUpsertCTPT = "IF EXISTS (SELECT 1 FROM ChiTietSuaChua_PhuTung WHERE maPhieuTNSC = ? AND maSKU = ?) "
                + "UPDATE ChiTietSuaChua_PhuTung SET soLuongSuDung = soLuongSuDung + ? WHERE maPhieuTNSC = ? AND maSKU = ? "
                + "ELSE INSERT INTO ChiTietSuaChua_PhuTung(maPhieuTNSC, maSKU, soLuongSuDung, giaBanThucTe) "
                + "SELECT ?, maSKU, ?, giaBanLe FROM PhuTung WHERE maSKU = ?";
        String sqlUpdateYC = "UPDATE PhieuYeuCauXuatKho SET trangThai = N'Đã xuất', ngayXuat = GETDATE(), maNV_ThuKho = ? WHERE maPhieuYC = ?";
        String sqlUpdatePTNSC = "UPDATE PhieuTiepNhanVaSuaChua SET trangThai = N'Đang sửa' WHERE maPhieuTNSC = ? AND trangThai = N'Chờ phụ tùng'";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psTru = c.prepareStatement(sqlTruTon);
                 PreparedStatement psCTPT = c.prepareStatement(sqlUpsertCTPT);
                 PreparedStatement psYC = c.prepareStatement(sqlUpdateYC);
                 PreparedStatement psPTNSC = c.prepareStatement(sqlUpdatePTNSC)) {

                for (ChiTietYeuCauXuatKho ct : chiTietList) {
                    psTru.setInt(1, ct.getSoLuong());
                    psTru.setString(2, ct.getMaSKU());
                    psTru.setInt(3, ct.getSoLuong());
                    int affected = psTru.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Tồn kho trên hệ thống không đủ cho mã " + ct.getMaSKU());
                    }

                    psCTPT.setString(1, maPhieuTNSC);
                    psCTPT.setString(2, ct.getMaSKU());
                    psCTPT.setInt(3, ct.getSoLuong());
                    psCTPT.setString(4, maPhieuTNSC);
                    psCTPT.setString(5, ct.getMaSKU());
                    psCTPT.setString(6, maPhieuTNSC);
                    psCTPT.setInt(7, ct.getSoLuong());
                    psCTPT.setString(8, ct.getMaSKU());
                    psCTPT.executeUpdate();
                }

                psYC.setString(1, maNVThuKho);
                psYC.setString(2, maPhieuYC);
                psYC.executeUpdate();

                psPTNSC.setString(1, maPhieuTNSC);
                psPTNSC.executeUpdate();

                TienDoSuaChua_Dao.ghiNhatKyAnToan(c, maNVThuKho,
                        "Duyệt xuất kho phụ tùng",
                        "Duyệt xuất kho phiếu " + maPhieuYC + " cho phiếu sửa chữa " + maPhieuTNSC);

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

    public boolean tuChoiYeuCau(String maPhieuYC, String maNVThuKho) throws SQLException {
        String sql = "UPDATE PhieuYeuCauXuatKho SET trangThai = N'Từ chối', maNV_ThuKho = ? WHERE maPhieuYC = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maNVThuKho);
            ps.setString(2, maPhieuYC);
            boolean ok = ps.executeUpdate() == 1;
            if (ok) {
                TienDoSuaChua_Dao.ghiNhatKyAnToan(c, maNVThuKho,
                        "Từ chối yêu cầu xuất kho",
                        "Từ chối phiếu yêu cầu xuất kho " + maPhieuYC);
            }
            return ok;
        }
    }
}