package dao;

import connection.ConnectDB;
import entity.ChiTietYeuCauXuatKho;
import entity.NhanVien;
import entity.PhieuTiepNhanVaSuaChua;
import entity.PhieuYeuCauXuatKho;
import entity.PhuTung;
import entity.TrangThaiYeuCauXuat;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuYeuCauXuatKho_Dao {
    /** Hết yêu cầu đang chờ thì phiếu trở lại "Đang sửa". Tham số là mã yêu cầu vừa được duyệt hoặc từ chối. */
    private static final String TRA_PHIEU_VE_DANG_SUA = "UPDATE p SET trangThai = N'Đang sửa' "
            + "FROM PhieuTiepNhanVaSuaChua p WHERE p.trangThai = N'Chờ phụ tùng' "
            + "AND p.maPhieuTNSC = (SELECT maPhieuTNSC FROM PhieuYeuCauXuatKho WHERE maPhieuYC = ?) "
            + "AND NOT EXISTS (SELECT 1 FROM PhieuYeuCauXuatKho y "
            + "WHERE y.maPhieuTNSC = p.maPhieuTNSC AND y.trangThai = N'Chờ xuất')";

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

    public int countByTrangThai(TrangThaiYeuCauXuat trangThai) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuYeuCauXuatKho WHERE trangThai = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThai.getText());
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public int countDaXuatHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuYeuCauXuatKho WHERE trangThai = N'Đã xuất' "
                + "AND CAST(ngayXuat AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public List<PhieuYeuCauXuatKho> searchYeuCauThongMinh(String keyword, TrangThaiYeuCauXuat trangThai) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String tt = trangThai == null ? "" : trangThai.getText();

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
                            TrangThaiYeuCauXuat.fromDb(rs.getString("trangThai")),
                            new PhieuTiepNhanVaSuaChua(rs.getString("maPhieuTNSC")),
                            new NhanVien(rs.getString("maNV_KyThuat")),
                            rs.getString("maNV_ThuKho") == null ? null : new NhanVien(rs.getString("maNV_ThuKho"))
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
                    PhuTung pt = new PhuTung(rs.getString("maSKU"));
                    pt.setTenPhuTung(rs.getString("tenPhuTung"));
                    pt.setHangSanXuat(rs.getString("hangSanXuat"));
                    pt.setViTriLuuKho(rs.getString("viTriLuuKho"));
                    pt.setSoLuongTon(rs.getInt("soLuongTon"));
                    pt.setNguongCanhBao(rs.getInt("nguongCanhBao"));
                    list.add(new ChiTietYeuCauXuatKho(new PhieuYeuCauXuatKho(rs.getString("maPhieuYC")), pt,
                            rs.getInt("soLuong")));
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

    public boolean insertWithDetails(PhieuYeuCauXuatKho phieu, List<ChiTietYeuCauXuatKho> chiTietList,
            String tenDangNhap) throws SQLException {
        if (kiemTraTonTaiHoaDonHopLe(phieu.getPhieu().getMaPhieuTNSC())) {
            throw new SQLException("Phiếu sửa chữa " + phieu.getPhieu().getMaPhieuTNSC() + " đã có hóa đơn còn hiệu lực, không thể tạo thêm yêu cầu phụ tùng!");
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
                psPhieu.setString(4, phieu.getTrangThai().getText());
                psPhieu.setString(5, phieu.getPhieu().getMaPhieuTNSC());
                psPhieu.setString(6, phieu.getKyThuatVien().getMaNV());
                if (phieu.getThuKho() != null) {
                    psPhieu.setString(7, phieu.getThuKho().getMaNV());
                } else {
                    psPhieu.setNull(7, Types.VARCHAR);
                }
                psPhieu.executeUpdate();

                for (ChiTietYeuCauXuatKho ct : chiTietList) {
                    psCT.setString(1, phieu.getMaPhieuYC());
                    psCT.setString(2, ct.getPhuTung().getMaSKU());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.addBatch();
                }
                psCT.executeBatch();

                psUpdate.setString(1, phieu.getPhieu().getMaPhieuTNSC());
                psUpdate.executeUpdate();

                new NhatKy_Dao().ghi(c, tenDangNhap, "Lập phiếu yêu cầu xuất kho", phieu.getMaPhieuYC(), null,
                        "Phiếu sửa chữa " + phieu.getPhieu().getMaPhieuTNSC());

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

    public boolean duyetXuatKho(String maPhieuYC, String maPhieuTNSC, String maNVThuKho,
            List<ChiTietYeuCauXuatKho> chiTietList, String tenDangNhap) throws SQLException {
        // Chặn ngay ở tầng DAO nếu phiếu sửa chữa đã có hóa đơn hợp lệ (khác 'Đã hủy')
        if (kiemTraTonTaiHoaDonHopLe(maPhieuTNSC)) {
            throw new SQLException("Phiếu sửa chữa " + maPhieuTNSC + " đã có hóa đơn còn hiệu lực. Vui lòng hủy hóa đơn trước khi duyệt xuất kho!");
        }

        String sqlTruTon = "UPDATE PhuTung SET soLuongTon = soLuongTon - ? WHERE maSKU = ? AND soLuongTon >= ?";
        String sqlUpsertCTPT = "IF EXISTS (SELECT 1 FROM ChiTietSuaChua_PhuTung WHERE maPhieuTNSC = ? AND maSKU = ?) "
                + "UPDATE ChiTietSuaChua_PhuTung SET soLuongSuDung = soLuongSuDung + ? WHERE maPhieuTNSC = ? AND maSKU = ? "
                + "ELSE INSERT INTO ChiTietSuaChua_PhuTung(maPhieuTNSC, maSKU, soLuongSuDung, giaBanThucTe) "
                + "SELECT ?, maSKU, ?, giaBanLe FROM PhuTung WHERE maSKU = ?";
        String sqlUpdateYC = "UPDATE PhieuYeuCauXuatKho SET trangThai = N'Đã xuất', ngayXuat = GETDATE(), maNV_ThuKho = ? "
                + "WHERE maPhieuYC = ? AND trangThai = N'Chờ xuất'";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psTru = c.prepareStatement(sqlTruTon);
                 PreparedStatement psCTPT = c.prepareStatement(sqlUpsertCTPT);
                 PreparedStatement psYC = c.prepareStatement(sqlUpdateYC);
                 PreparedStatement psPTNSC = c.prepareStatement(TRA_PHIEU_VE_DANG_SUA)) {

                // Đổi trạng thái trước: hai máy cùng duyệt thì máy sau dừng ở đây, không trừ kho lần nữa.
                psYC.setString(1, maNVThuKho);
                psYC.setString(2, maPhieuYC);
                if (psYC.executeUpdate() != 1) {
                    throw new SQLException("Yêu cầu " + maPhieuYC + " không còn chờ xuất. Bấm Tải dữ liệu để xem lại.");
                }

                for (ChiTietYeuCauXuatKho ct : chiTietList) {
                    psTru.setInt(1, ct.getSoLuong());
                    psTru.setString(2, ct.getPhuTung().getMaSKU());
                    psTru.setInt(3, ct.getSoLuong());
                    int affected = psTru.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Tồn kho trên hệ thống không đủ cho mã " + ct.getPhuTung().getMaSKU());
                    }

                    psCTPT.setString(1, maPhieuTNSC);
                    psCTPT.setString(2, ct.getPhuTung().getMaSKU());
                    psCTPT.setInt(3, ct.getSoLuong());
                    psCTPT.setString(4, maPhieuTNSC);
                    psCTPT.setString(5, ct.getPhuTung().getMaSKU());
                    psCTPT.setString(6, maPhieuTNSC);
                    psCTPT.setInt(7, ct.getSoLuong());
                    psCTPT.setString(8, ct.getPhuTung().getMaSKU());
                    psCTPT.executeUpdate();
                }

                psPTNSC.setString(1, maPhieuYC);
                psPTNSC.executeUpdate();

                new NhatKy_Dao().ghi(c, tenDangNhap, "Duyệt xuất kho phụ tùng", maPhieuYC, null,
                        "Phiếu sửa chữa " + maPhieuTNSC);

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

    /** Trả về false khi yêu cầu không còn chờ xuất, ví dụ máy khác đã duyệt hoặc từ chối trước. */
    public boolean tuChoiYeuCau(String maPhieuYC, String maNVThuKho, String tenDangNhap) throws SQLException {
        String sql = "UPDATE PhieuYeuCauXuatKho SET trangThai = N'Từ chối', maNV_ThuKho = ? "
                + "WHERE maPhieuYC = ? AND trangThai = N'Chờ xuất'";
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql);
                 PreparedStatement psPhieu = c.prepareStatement(TRA_PHIEU_VE_DANG_SUA)) {
                ps.setString(1, maNVThuKho);
                ps.setString(2, maPhieuYC);
                if (ps.executeUpdate() != 1) {
                    c.rollback();
                    return false;
                }
                psPhieu.setString(1, maPhieuYC);
                psPhieu.executeUpdate();
                new NhatKy_Dao().ghi(c, tenDangNhap, "Từ chối yêu cầu xuất kho", maPhieuYC, null, null);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
