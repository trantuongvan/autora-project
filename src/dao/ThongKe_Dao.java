package dao;

import connection.ConnectDB;
import entity.DoanhThuNgay;
import entity.LuotXeNgay;
import entity.NangSuatKTV;
import entity.NhanVien;
import entity.NhapXuatNgay;
import entity.PhuTung;
import entity.ThongKePhuTung;
import entity.VaiTro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO chỉ thực hiện truy vấn SQL lấy dữ liệu thô từ CSDL phục vụ báo cáo thống kê (UC014).
 */
public class ThongKe_Dao {

    public List<DoanhThuNgay> taiDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlNgay = "SELECT CAST(h.ngayLap AS DATE) AS ngay, "
                + "COUNT(h.maHD) AS soHD, "
                + "COALESCE(SUM(h.tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(h.tongTienPhuTung), 0) AS tienPhuTung, "
                + "COALESCE(SUM(h.tongTienGiam), 0) AS giamGia, "
                + "COALESCE(SUM(h.tongTienVAT), 0) AS vat, "
                + "COALESCE(SUM(h.tongThanhToan), 0) AS tongThanhToan "
                + "FROM HoaDon h "
                + "WHERE h.trangThaiThanhToan = N'Đã thanh toán' AND h.ngayLap >= ? AND h.ngayLap < ? "
                + "GROUP BY CAST(h.ngayLap AS DATE) "
                + "ORDER BY ngay DESC";

        List<DoanhThuNgay> dsNgay = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlNgay)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsNgay.add(new DoanhThuNgay(
                            rs.getDate("ngay").toLocalDate(),
                            rs.getInt("soHD"),
                            rs.getDouble("tienCong"),
                            rs.getDouble("tienPhuTung"),
                            rs.getDouble("giamGia"),
                            rs.getDouble("vat"),
                            rs.getDouble("tongThanhToan")
                    ));
                }
            }
        }
        return dsNgay;
    }

    public double taiTongGiaVonPhuTung(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlGiaVon = "SELECT COALESCE(SUM(ct.soLuongSuDung * pt.giaNhap), 0) AS tongGiaVon "
                + "FROM HoaDon h "
                + "JOIN ChiTietSuaChua_PhuTung ct ON ct.maPhieuTNSC = h.maPhieuTNSC "
                + "JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "WHERE h.trangThaiThanhToan = N'Đã thanh toán' AND h.ngayLap >= ? AND h.ngayLap < ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlGiaVon)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("tongGiaVon");
                }
            }
        }
        return 0;
    }

    public List<ThongKePhuTung> taiDanhSachPhuTungTho(LocalDate tuNgay, LocalDate denNgay, double[] tongGiaTriNhapXuat) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlPT = "SELECT pt.maSKU, pt.tenPhuTung, pt.hangSanXuat, pt.viTriLuuKho, "
                + "pt.giaNhap, pt.giaBanLe, pt.soLuongTon, pt.nguongCanhBao, "
                + "COALESCE(nhap.slNhap, 0) AS slNhap, COALESCE(nhap.gtNhap, 0) AS gtNhap, "
                + "COALESCE(xuat.slXuat, 0) AS slXuat, COALESCE(xuat.gtXuat, 0) AS gtXuat "
                + "FROM PhuTung pt "
                + "LEFT JOIN ( "
                + "    SELECT ct.maSKU, SUM(ct.soLuongNhap) AS slNhap, SUM(ct.soLuongNhap * ct.giaNhapThucTe) AS gtNhap "
                + "    FROM ChiTietNhapKho ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap "
                + "    WHERE pn.ngayNhap >= ? AND pn.ngayNhap < ? GROUP BY ct.maSKU "
                + ") nhap ON nhap.maSKU = pt.maSKU "
                + "LEFT JOIN ( "
                + "    SELECT ct.maSKU, SUM(ct.soLuongSuDung) AS slXuat, SUM(ct.soLuongSuDung * ct.giaBanThucTe) AS gtXuat "
                + "    FROM ChiTietSuaChua_PhuTung ct JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC "
                + "    WHERE p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ? GROUP BY ct.maSKU "
                + ") xuat ON xuat.maSKU = pt.maSKU "
                + "ORDER BY COALESCE(xuat.slXuat, 0) DESC, pt.maSKU ASC";

        List<ThongKePhuTung> dsPT = new ArrayList<>();
        double tongGTNhap = 0, tongGTXuat = 0;

        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlPT)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            ps.setTimestamp(3, tsTu);
            ps.setTimestamp(4, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhuTung pt = new PhuTung(rs.getString("maSKU"));
                    pt.setTenPhuTung(rs.getString("tenPhuTung"));
                    pt.setHangSanXuat(rs.getString("hangSanXuat"));
                    pt.setViTriLuuKho(rs.getString("viTriLuuKho"));
                    pt.setGiaBanLe(rs.getDouble("giaBanLe"));
                    pt.setGiaNhap(rs.getDouble("giaNhap"));
                    int ton = rs.getInt("soLuongTon");
                    int nguong = rs.getInt("nguongCanhBao");
                    pt.setSoLuongTon(ton);
                    pt.setNguongCanhBao(nguong);

                    int slNhap = rs.getInt("slNhap");
                    int slXuat = rs.getInt("slXuat");

                    dsPT.add(new ThongKePhuTung(pt, slNhap, slXuat, ton));
                    tongGTNhap += rs.getDouble("gtNhap");
                    tongGTXuat += rs.getDouble("gtXuat");
                }
            }
        }
        if (tongGiaTriNhapXuat != null && tongGiaTriNhapXuat.length >= 2) {
            tongGiaTriNhapXuat[0] = tongGTNhap;
            tongGiaTriNhapXuat[1] = tongGTXuat;
        }
        return dsPT;
    }

    public int demSoPhieuNhap(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlDemPN = "SELECT COUNT(*) FROM PhieuNhapKho WHERE ngayNhap >= ? AND ngayNhap < ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlDemPN)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<NhapXuatNgay> taiNhapXuatTheoNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlNXNgay = "SELECT ngay, SUM(slNhap) AS nhap, SUM(slXuat) AS xuat FROM ( "
                + "    SELECT CAST(pn.ngayNhap AS DATE) AS ngay, SUM(ct.soLuongNhap) AS slNhap, 0 AS slXuat "
                + "    FROM ChiTietNhapKho ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap "
                + "    WHERE pn.ngayNhap >= ? AND pn.ngayNhap < ? GROUP BY CAST(pn.ngayNhap AS DATE) "
                + "    UNION ALL "
                + "    SELECT CAST(p.thoiGianTiepNhan AS DATE) AS ngay, 0 AS slNhap, SUM(ct.soLuongSuDung) AS slXuat "
                + "    FROM ChiTietSuaChua_PhuTung ct JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC "
                + "    WHERE p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ? GROUP BY CAST(p.thoiGianTiepNhan AS DATE) "
                + ") t GROUP BY ngay ORDER BY ngay ASC";

        List<NhapXuatNgay> dsNXNgay = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlNXNgay)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            ps.setTimestamp(3, tsTu);
            ps.setTimestamp(4, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsNXNgay.add(new NhapXuatNgay(
                            rs.getDate("ngay").toLocalDate(),
                            rs.getInt("nhap"),
                            rs.getInt("xuat")
                    ));
                }
            }
        }
        return dsNXNgay;
    }

    public int[] taiSoLuotXeVaoXuongVaSuaXong(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlXe = "SELECT COUNT(*) AS tongXe, "
                + "COALESCE(SUM(CASE WHEN trangThai = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS xeXong "
                + "FROM PhieuTiepNhanVaSuaChua "
                + "WHERE trangThai <> N'Đã hủy' AND thoiGianTiepNhan >= ? AND thoiGianTiepNhan < ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlXe)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("tongXe"), rs.getInt("xeXong")};
                }
            }
        }
        return new int[]{0, 0};
    }

    public List<NangSuatKTV> taiDanhSachNangSuatKTVTho(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlKTV = "SELECT nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi, "
                + "COUNT(DISTINCT p.maPhieuTNSC) AS soXe, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS hmXong, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo <> N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN 1 ELSE 0 END), 0) AS hmDangLam, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' THEN ct.tienCongThucTe ELSE 0 END), 0) AS tienCong "
                + "FROM NhanVien nv "
                + "LEFT JOIN ChiTietSuaChua_DichVu ct ON ct.maNV_KyThuat = nv.maNV "
                + "LEFT JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC "
                + "     AND p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ? "
                + "WHERE nv.viTriLamViec = N'Kỹ thuật viên' "
                + "GROUP BY nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi "
                + "ORDER BY hmXong DESC, tienCong DESC";

        List<NangSuatKTV> dsKTV = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlKTV)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien(
                            rs.getString("maNV"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getString("diaChi"),
                            VaiTro.KY_THUAT_VIEN
                    );
                    int soXe = rs.getInt("soXe");
                    int hmXong = soXe == 0 ? 0 : rs.getInt("hmXong");
                    int hmDang = soXe == 0 ? 0 : rs.getInt("hmDangLam");
                    double tienCong = soXe == 0 ? 0 : rs.getDouble("tienCong");

                    dsKTV.add(new NangSuatKTV(nv, soXe, hmXong, hmDang, tienCong));
                }
            }
        }
        return dsKTV;
    }

    public List<LuotXeNgay> taiLuotXeTheoNgay(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlLuotNgay = "SELECT CAST(thoiGianTiepNhan AS DATE) AS ngay, COUNT(*) AS soLuot "
                + "FROM PhieuTiepNhanVaSuaChua "
                + "WHERE trangThai <> N'Đã hủy' AND thoiGianTiepNhan >= ? AND thoiGianTiepNhan < ? "
                + "GROUP BY CAST(thoiGianTiepNhan AS DATE) ORDER BY ngay ASC";
        List<LuotXeNgay> dsLuotNgay = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlLuotNgay)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsLuotNgay.add(new LuotXeNgay(rs.getDate("ngay").toLocalDate(), rs.getInt("soLuot")));
                }
            }
        }
        return dsLuotNgay;
    }
}
