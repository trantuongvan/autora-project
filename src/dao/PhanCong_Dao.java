package dao;

import connection.ConnectDB;
import entity.DichVu;
import entity.NhanVien;
import util.PhanCongTuDong;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Đọc ứng viên và hàng đợi; việc chọn ai do NghiepVuPhieu.chonKyThuatVien quyết định. */
public class PhanCong_Dao {

    /** Người làm được từng dịch vụ, kèm số phút của riêng họ, theo mã dịch vụ. */
    public Map<String, List<PhanCongTuDong.UngVien>> docUngVien(List<DichVu> dichVu) throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return docUngVien(connection, dichVu);
        }
    }

    /** Việc chưa xong của từng kỹ thuật viên, theo mã nhân viên. */
    public Map<String, PhanCongTuDong.HangDoi> docHangDoi() throws SQLException {
        try (Connection connection = ConnectDB.getConnection()) {
            return docHangDoi(connection);
        }
    }

    private Map<String, List<PhanCongTuDong.UngVien>> docUngVien(Connection connection, List<DichVu> dichVu) throws SQLException {
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < dichVu.size(); i++) {
            in.append(i == 0 ? "?" : ", ?");
        }
        String sql = "SELECT k.maDichVu, k.thoiGianPhut, ca.maCa, ca.tenCa, ca.gioBatDau, ca.gioKetThuc, "
                + NhanVien_Dao.cotNhanVien("n", "") + " "
                + "FROM NhanVien_DichVu k JOIN NhanVien n ON n.maNV = k.maNV "
                + "JOIN CaLamViec ca ON ca.maCa = n.maCa "
                + "WHERE k.maDichVu IN (" + in + ") ORDER BY n.maNV";
        Map<String, List<PhanCongTuDong.UngVien>> ketQua = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < dichVu.size(); i++) {
                statement.setString(i + 1, dichVu.get(i).getMaDichVu());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    NhanVien nhanVien = NhanVien_Dao.docNhanVien(rs, "");
                    nhanVien.setCaLamViec(NhanVien_Dao.docCa(rs));
                    ketQua.computeIfAbsent(rs.getString("maDichVu"), k -> new ArrayList<>())
                            .add(new PhanCongTuDong.UngVien(nhanVien, rs.getInt("thoiGianPhut")));
                }
            }
        }
        return ketQua;
    }

    /** Một câu SQL: tổng phút và số việc chưa xong của mỗi KTV; không có dòng kỹ năng thì dùng thời gian chuẩn. */
    private Map<String, PhanCongTuDong.HangDoi> docHangDoi(Connection connection) throws SQLException {
        String sql = "SELECT c.maNV_KyThuat, COUNT(*) AS soViec, SUM(COALESCE(k.thoiGianPhut, d.thoiGianChuan)) AS phut "
                + "FROM ChiTietSuaChua_DichVu c "
                + "JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = c.maPhieuTNSC "
                + "JOIN DichVu d ON d.maDichVu = c.maDichVu "
                + "LEFT JOIN NhanVien_DichVu k ON k.maNV = c.maNV_KyThuat AND k.maDichVu = c.maDichVu "
                + "WHERE c.maNV_KyThuat IS NOT NULL AND c.trangThaiTienDo <> N'Hoàn thành' "
                + "AND p.trangThai NOT IN (N'Hoàn thành', N'Đã hủy') "
                + "GROUP BY c.maNV_KyThuat";
        Map<String, PhanCongTuDong.HangDoi> ketQua = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ketQua.put(rs.getString("maNV_KyThuat"), new PhanCongTuDong.HangDoi(rs.getInt("phut"), rs.getInt("soViec")));
            }
        }
        return ketQua;
    }
}
