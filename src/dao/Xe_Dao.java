package dao;

import connection.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiHoaDon;
import entity.TrangThaiPhieu;
import entity.Xe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD hồ sơ xe; truy vấn danh sách ghép thông tin chủ xe. */
public class Xe_Dao {

    public List<Xe> findAll() throws SQLException {
        return search("");
    }

    public List<Xe> search(String keyword) throws SQLException {
        return search(keyword, "");
    }

    /** hang rỗng nghĩa là không lọc theo hãng xe. */
    public List<Xe> search(String keyword, String hang) throws SQLException {
        String q = keyword == null ? "" : keyword.trim();
        List<Xe> ds = new ArrayList<>();
        String sql = "SELECT x.bienSo, x.hangXe, x.dongXe, x.doiXe, x.soVIN, x.mauSon, "
                + "k.maKH, k.hoTen, k.soDienThoai "
                + "FROM Xe x JOIN KhachHang k ON k.maKH = x.maKH "
                + "WHERE (? = '' OR x.bienSo LIKE ? OR x.soVIN LIKE ? "
                + "OR k.hoTen LIKE ? OR k.soDienThoai LIKE ?) "
                + "AND (? = '' OR x.hangXe = ?) "
                + "ORDER BY x.bienSo";
        try (Connection c = ConnectDB.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, q);
                for (int i = 2; i <= 5; i++) {
                    p.setString(i, "%" + q + "%");
                }
                p.setString(6, hang);
                p.setString(7, hang);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        KhachHang chuXe = new KhachHang(r.getString("maKH"));
                        chuXe.setHoTen(r.getString("hoTen"));
                        chuXe.setSoDienThoai(r.getString("soDienThoai"));
                        ds.add(mapXe(r, chuXe));
                    }
                }
            }
        }
        return ds;
    }

    public Xe findById(String bienSo) throws SQLException {
        String sql = "SELECT bienSo, soVIN, hangXe, dongXe, doiXe, mauSon, maKH FROM Xe WHERE bienSo = ?";
        try (Connection c = ConnectDB.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, bienSo);
                try (ResultSet r = p.executeQuery()) {
                    return r.next() ? mapXe(r, new KhachHang(r.getString("maKH"))) : null;
                }
            }
        }
    }

    public boolean insert(Xe xe) throws SQLException {
        return write(xe, false);
    }

    public boolean update(Xe xe) throws SQLException {
        return write(xe, true);
    }

    private boolean write(Xe xe, boolean update) throws SQLException {
        String sql = update
                ? "UPDATE Xe SET soVIN = ?, hangXe = ?, dongXe = ?, doiXe = ?, mauSon = ?, maKH = ? WHERE bienSo = ?"
                : "INSERT INTO Xe(soVIN, hangXe, dongXe, doiXe, mauSon, maKH, bienSo) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectDB.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, xe.getSoVIN());
                p.setString(2, xe.getHangXe());
                p.setString(3, xe.getDongXe());
                if (xe.getDoiXe() == null) {
                    p.setNull(4, Types.INTEGER);
                } else {
                    p.setInt(4, xe.getDoiXe());
                }
                p.setString(5, xe.getMauSon());
                p.setString(6, xe.getKhachHang().getMaKH());
                p.setString(7, xe.getBienSo());
                return p.executeUpdate() == 1;
            }
        }
    }

    public boolean delete(String bienSo) throws SQLException {
        String sql = "DELETE FROM Xe WHERE bienSo = ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, bienSo);
            return p.executeUpdate() == 1;
        }
    }

    private Xe mapXe(ResultSet r, KhachHang chuXe) throws SQLException {
        Object nam = r.getObject("doiXe");
        Integer doiXe = null;
        if (nam != null && !nam.toString().trim().isEmpty()) {
            try {
                doiXe = Integer.valueOf(nam.toString().trim());
            } catch (NumberFormatException ex) {
                throw new SQLException("Năm sản xuất trong hồ sơ xe không hợp lệ: " + nam, ex);
            }
        }
        return new Xe(
                r.getString("bienSo"),
                r.getString("soVIN"),
                r.getString("hangXe"),
                r.getString("dongXe"),
                doiXe,
                r.getString("mauSon"),
                chuXe);
    }

    public List<String> findChuSoHuu() throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai FROM KhachHang ORDER BY hoTen";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString("maKH") + " - " + rs.getString("hoTen")
                        + " (" + rs.getString("soDienThoai") + ")");
            }
        }
        return ds;
    }

    public List<PhieuTiepNhanVaSuaChua> findLichSu(String bienSo) throws SQLException {
        String sql = "SELECT p.maPhieuTNSC, p.thoiGianTiepNhan, p.yeuCauBanDau, p.trangThai, "
                + "h.tongThanhToan, h.trangThaiThanhToan, ti.soHangMuc, ti.soHangMucXong "
                + "FROM PhieuTiepNhanVaSuaChua p "
                + "LEFT JOIN HoaDon h ON h.maPhieuTNSC = p.maPhieuTNSC "
                + "AND h.trangThaiThanhToan <> N'Đã hủy' "
                + "OUTER APPLY (SELECT COUNT(*) AS soHangMuc, "
                + "COALESCE(SUM(CASE WHEN d.trangThaiTienDo = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS soHangMucXong "
                + "FROM ChiTietSuaChua_DichVu d WHERE d.maPhieuTNSC = p.maPhieuTNSC) ti "
                + "WHERE p.bienSo = ? ORDER BY p.thoiGianTiepNhan DESC";
        List<PhieuTiepNhanVaSuaChua> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, bienSo);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    PhieuTiepNhanVaSuaChua phieu = new PhieuTiepNhanVaSuaChua(r.getString("maPhieuTNSC"));
                    Timestamp tiepNhan = r.getTimestamp("thoiGianTiepNhan");
                    phieu.setThoiGianTiepNhan(tiepNhan == null ? null : tiepNhan.toLocalDateTime());
                    phieu.setYeuCauBanDau(r.getString("yeuCauBanDau"));
                    phieu.setTrangThai(TrangThaiPhieu.fromDb(r.getString("trangThai")));
                    phieu.setTongSoHangMuc(r.getInt("soHangMuc"));
                    phieu.setSoHangMucHoanThanh(r.getInt("soHangMucXong"));
                    String trangThaiHoaDon = r.getString("trangThaiThanhToan");
                    if (trangThaiHoaDon != null) {
                        phieu.setHoaDon(new HoaDon(TrangThaiHoaDon.fromDb(trangThaiHoaDon), r.getDouble("tongThanhToan")));
                    }
                    ds.add(phieu);
                }
            }
        }
        return ds;
    }
}
