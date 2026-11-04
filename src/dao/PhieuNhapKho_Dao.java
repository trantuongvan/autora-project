package dao;

import connection.ConnectDB;
import entity.ChiTietNhapKho;
import entity.NhanVien;
import entity.VaiTro;
import entity.NhaCungCap;
import entity.PhieuNhapKho;
import entity.PhuTung;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapKho_Dao {

    /** Lấy Thủ kho mặc định từ DB (ưu tiên NV0004 - Bùi Thái Vy trong dữ liệu mẫu SQL). */
    public NhanVien findDefaultThuKho() throws SQLException {
        entity.PhienDangNhap phien = entity.PhienDangNhap.getHienTai();
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
                + "FROM NhanVien WHERE viTriLamViec = N'Thủ kho' ORDER BY maNV ASC";
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

    /** Phát sinh mã phiếu nhập tự động: PN + ddMMyyyy + 4 chữ số (VD: PN031120260001) theo Bảng 28 báo cáo. */
    public String nextId() throws SQLException {
        String prefix = "PN" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, RIGHT(maPhieuNhap, 4))), 0) + 1 FROM PhieuNhapKho";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            int nextNum = r.next() ? r.getInt(1) : 1;
            return prefix + String.format("%04d", nextNum);
        }
    }

    public int countPhieuNhapHomNay() throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuNhapKho WHERE CAST(ngayNhap AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection c = ConnectDB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    /** Tìm kiếm lịch sử phiếu nhập kho theo từ khóa, nhà cung cấp và khoảng ngày. */
    public List<PhieuNhapKho> search(String keyword, String maNCC, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String ncc = (maNCC == null || "Tất cả".equals(maNCC)) ? "" : maNCC.trim();

        StringBuilder sql = new StringBuilder(
                "SELECT pn.maPhieuNhap, pn.ngayNhap, pn.tongTienNhap, pn.maNCC, pn.maNV_ThuKho, "
                        + "       ncc.tenNCC, nv.hoTen AS tenThuKho, "
                        + "       (SELECT COUNT(*) FROM ChiTietNhapKho ct WHERE ct.maPhieuNhap = pn.maPhieuNhap) AS soMatHang "
                        + "FROM PhieuNhapKho pn "
                        + "JOIN NhaCungCap ncc ON ncc.maNCC = pn.maNCC "
                        + "JOIN NhanVien nv ON nv.maNV = pn.maNV_ThuKho "
                        + "WHERE (? = '' OR pn.maPhieuNhap LIKE ? OR ncc.tenNCC LIKE ?) "
                        + "AND (? = '' OR pn.maNCC = ? OR ncc.tenNCC = ?) "
        );

        if (tuNgay != null) {
            sql.append("AND CAST(pn.ngayNhap AS DATE) >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND CAST(pn.ngayNhap AS DATE) <= ? ");
        }
        sql.append("ORDER BY pn.ngayNhap DESC, pn.maPhieuNhap DESC");

        List<PhieuNhapKho> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, k);
            ps.setString(idx++, "%" + k + "%");
            ps.setString(idx++, "%" + k + "%");
            ps.setString(idx++, ncc);
            ps.setString(idx++, ncc);
            ps.setString(idx++, ncc);
            if (tuNgay != null) ps.setDate(idx++, Date.valueOf(tuNgay));
            if (denNgay != null) ps.setDate(idx++, Date.valueOf(denNgay));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("ngayNhap");
                    NhaCungCap nhaCungCap = new NhaCungCap(rs.getString("maNCC"));
                    nhaCungCap.setTenNCC(rs.getString("tenNCC"));
                    NhanVien thuKho = new NhanVien(rs.getString("maNV_ThuKho"));
                    thuKho.setHoTen(rs.getString("tenThuKho"));
                    PhieuNhapKho pn = new PhieuNhapKho(
                            rs.getString("maPhieuNhap"),
                            ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                            rs.getDouble("tongTienNhap"),
                            nhaCungCap,
                            thuKho
                    );
                    pn.setSoMatHang(rs.getInt("soMatHang"));
                    list.add(pn);
                }
            }
        }
        return list;
    }

    /** Nhà cung cấp của lần nhập gần nhất có phụ tùng này; null nếu chưa từng nhập. */
    public NhaCungCap findNhaCungCapGanNhat(String maSKU) throws SQLException {
        String sql = "SELECT TOP 1 ncc.maNCC, ncc.tenNCC FROM ChiTietNhapKho ct "
                + "JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap "
                + "JOIN NhaCungCap ncc ON ncc.maNCC = pn.maNCC "
                + "WHERE ct.maSKU = ? ORDER BY pn.ngayNhap DESC, pn.maPhieuNhap DESC";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSKU);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                NhaCungCap ncc = new NhaCungCap(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));
                return ncc;
            }
        }
    }

    public List<ChiTietNhapKho> findChiTietByMaPhieu(String maPhieuNhap) throws SQLException {
        String sql = "SELECT ct.maPhieuNhap, ct.maSKU, ct.soLuongNhap, ct.giaNhapThucTe, pt.tenPhuTung "
                + "FROM ChiTietNhapKho ct "
                + "JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "WHERE ct.maPhieuNhap = ? ORDER BY ct.maSKU ASC";
        List<ChiTietNhapKho> list = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhuTung phuTung = new PhuTung(rs.getString("maSKU"));
                    phuTung.setTenPhuTung(rs.getString("tenPhuTung"));
                    list.add(new ChiTietNhapKho(
                            new PhieuNhapKho(rs.getString("maPhieuNhap")),
                            phuTung,
                            rs.getInt("soLuongNhap"),
                            rs.getDouble("giaNhapThucTe")
                    ));
                }
            }
        }
        return list;
    }


    public boolean insertWithDetails(PhieuNhapKho phieu, List<ChiTietNhapKho> chiTietList) throws SQLException {
        String sqlPhieu = "INSERT INTO PhieuNhapKho(maPhieuNhap, ngayNhap, tongTienNhap, maNCC, maNV_ThuKho) VALUES(?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietNhapKho(maPhieuNhap, maSKU, soLuongNhap, giaNhapThucTe) VALUES(?, ?, ?, ?)";
        String sqlCongTon = "UPDATE PhuTung SET soLuongTon = soLuongTon + ?, giaNhap = ? WHERE maSKU = ?";

        try (Connection c = ConnectDB.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try (PreparedStatement psPhieu = c.prepareStatement(sqlPhieu);
                 PreparedStatement psCT = c.prepareStatement(sqlCT);
                 PreparedStatement psTon = c.prepareStatement(sqlCongTon)) {

                psPhieu.setString(1, phieu.getMaPhieuNhap());
                psPhieu.setTimestamp(2, Timestamp.valueOf(phieu.getNgayNhap()));
                psPhieu.setDouble(3, phieu.getTongTienNhap());
                psPhieu.setString(4, phieu.getNhaCungCap().getMaNCC());
                psPhieu.setString(5, phieu.getThuKho().getMaNV());
                psPhieu.executeUpdate();

                for (ChiTietNhapKho ct : chiTietList) {
                    psCT.setString(1, phieu.getMaPhieuNhap());
                    psCT.setString(2, ct.getPhuTung().getMaSKU());
                    psCT.setInt(3, ct.getSoLuongNhap());
                    psCT.setDouble(4, ct.getGiaNhapThucTe());
                    psCT.addBatch();

                    psTon.setInt(1, ct.getSoLuongNhap());
                    psTon.setDouble(2, ct.getGiaNhapThucTe());
                    psTon.setString(3, ct.getPhuTung().getMaSKU());
                    psTon.addBatch();
                }
                psCT.executeBatch();
                psTon.executeBatch();

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
}