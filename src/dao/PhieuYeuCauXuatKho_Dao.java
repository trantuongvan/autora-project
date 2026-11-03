package dao;

import connection.ConnectDB;
import entity.ChiTietYeuCauXuatKho;
import entity.PhieuYeuCauXuatKho;
import java.sql.*;
import java.util.List;

public class PhieuYeuCauXuatKho_Dao {

    /** Phát sinh mã tự động PYC + 5 chữ số (VD: PYC00001) */
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


    public boolean insertWithDetails(PhieuYeuCauXuatKho phieu, List<ChiTietYeuCauXuatKho> chiTietList) throws SQLException {
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
}