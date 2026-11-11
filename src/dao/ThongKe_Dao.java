package dao;

import connection.ConnectDB;
import entity.DoanhThuNgay;
import entity.DuLieuThongKe;
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
 * DAO đọc số liệu cho UC014 - Xem báo cáo thống kê.
 * DAO chỉ đọc và gộp số liệu bằng SQL; mọi tổng, tỷ lệ, lợi nhuận và giá trị tồn do QLThongKeController tính.
 * Doanh thu tính theo ngày thu tiền (ngayThanhToan), giống trang chủ quản lý.
 */
public class ThongKe_Dao {

    public DuLieuThongKe taiDuLieuThongKe(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Timestamp tu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp den = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());
        try (Connection c = ConnectDB.getConnection()) {
            return new DuLieuThongKe(docDoanhThuNgay(c, tu, den), docGiaVonPhuTung(c, tu, den),
                    docPhuTung(c, tu, den), dem(c, SQL_DEM_PHIEU_NHAP, tu, den), docNhapXuatNgay(c, tu, den),
                    dem(c, SQL_DEM_LUOT_XE, tu, den), dem(c, SQL_DEM_XE_XONG, tu, den),
                    docNangSuatKTV(c, tu, den), docLuotXeNgay(c, tu, den));
        }
    }

    private static final String DA_THU_TIEN = "h.trangThaiThanhToan = N'Đã thanh toán' AND h.ngayThanhToan >= ? AND h.ngayThanhToan < ? ";
    private static final String PHIEU_TRONG_KY = "p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ? ";
    private static final String SQL_DEM_PHIEU_NHAP = "SELECT COUNT(*) FROM PhieuNhapKho WHERE ngayNhap >= ? AND ngayNhap < ?";
    private static final String SQL_DEM_LUOT_XE = "SELECT COUNT(*) FROM PhieuTiepNhanVaSuaChua p WHERE " + PHIEU_TRONG_KY;
    private static final String SQL_DEM_XE_XONG = SQL_DEM_LUOT_XE + "AND p.trangThai = N'Hoàn thành'";

    private static PreparedStatement chuanBi(Connection c, String sql, Timestamp tu, Timestamp den, int soCap) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < soCap; i++) {
            ps.setTimestamp(i * 2 + 1, tu);
            ps.setTimestamp(i * 2 + 2, den);
        }
        return ps;
    }

    private static int dem(Connection c, String sql, Timestamp tu, Timestamp den) throws SQLException {
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 1); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Doanh thu theo ngày thu tiền (ngayThanhToan), giống trang chủ quản lý. */
    private static List<DoanhThuNgay> docDoanhThuNgay(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        String sql = "SELECT CAST(h.ngayThanhToan AS DATE) AS ngay, COUNT(h.maHD) AS soHD, "
                + "COALESCE(SUM(h.tongTienCong), 0) AS tienCong, COALESCE(SUM(h.tongTienPhuTung), 0) AS tienPhuTung, "
                + "COALESCE(SUM(h.tongTienGiam), 0) AS giamGia, COALESCE(SUM(h.tongTienVAT), 0) AS vat, "
                + "COALESCE(SUM(h.tongThanhToan), 0) AS tongThanhToan "
                + "FROM HoaDon h WHERE " + DA_THU_TIEN
                + "GROUP BY CAST(h.ngayThanhToan AS DATE) ORDER BY ngay DESC";
        List<DoanhThuNgay> ds = new ArrayList<>();
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 1); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new DoanhThuNgay(rs.getDate("ngay").toLocalDate(), rs.getInt("soHD"), rs.getDouble("tienCong"),
                        rs.getDouble("tienPhuTung"), rs.getDouble("giamGia"), rs.getDouble("vat"), rs.getDouble("tongThanhToan")));
            }
        }
        return ds;
    }

    private static double docGiaVonPhuTung(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        String sql = "SELECT COALESCE(SUM(ct.soLuongSuDung * pt.giaNhap), 0) FROM HoaDon h "
                + "JOIN ChiTietSuaChua_PhuTung ct ON ct.maPhieuTNSC = h.maPhieuTNSC "
                + "JOIN PhuTung pt ON pt.maSKU = ct.maSKU WHERE " + DA_THU_TIEN;
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 1); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private static List<ThongKePhuTung> docPhuTung(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        String sql = "SELECT pt.maSKU, pt.tenPhuTung, pt.hangSanXuat, pt.viTriLuuKho, "
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
                + "    WHERE " + PHIEU_TRONG_KY + "GROUP BY ct.maSKU "
                + ") xuat ON xuat.maSKU = pt.maSKU "
                + "ORDER BY COALESCE(xuat.slXuat, 0) DESC, pt.maSKU ASC";
        List<ThongKePhuTung> ds = new ArrayList<>();
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 2); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhuTung pt = new PhuTung(rs.getString("maSKU"));
                pt.setTenPhuTung(rs.getString("tenPhuTung"));
                pt.setHangSanXuat(rs.getString("hangSanXuat"));
                pt.setViTriLuuKho(rs.getString("viTriLuuKho"));
                pt.setGiaBanLe(rs.getDouble("giaBanLe"));
                pt.setGiaNhap(rs.getDouble("giaNhap"));
                pt.setSoLuongTon(rs.getInt("soLuongTon"));
                pt.setNguongCanhBao(rs.getInt("nguongCanhBao"));
                ds.add(new ThongKePhuTung(pt, rs.getInt("slNhap"), rs.getInt("slXuat"), rs.getDouble("gtNhap"), rs.getDouble("gtXuat")));
            }
        }
        return ds;
    }

    private static List<NhapXuatNgay> docNhapXuatNgay(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        String sql = "SELECT ngay, SUM(slNhap) AS nhap, SUM(slXuat) AS xuat FROM ( "
                + "    SELECT CAST(pn.ngayNhap AS DATE) AS ngay, SUM(ct.soLuongNhap) AS slNhap, 0 AS slXuat "
                + "    FROM ChiTietNhapKho ct JOIN PhieuNhapKho pn ON pn.maPhieuNhap = ct.maPhieuNhap "
                + "    WHERE pn.ngayNhap >= ? AND pn.ngayNhap < ? GROUP BY CAST(pn.ngayNhap AS DATE) "
                + "    UNION ALL "
                + "    SELECT CAST(p.thoiGianTiepNhan AS DATE) AS ngay, 0 AS slNhap, SUM(ct.soLuongSuDung) AS slXuat "
                + "    FROM ChiTietSuaChua_PhuTung ct JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC "
                + "    WHERE " + PHIEU_TRONG_KY + "GROUP BY CAST(p.thoiGianTiepNhan AS DATE) "
                + ") t GROUP BY ngay ORDER BY ngay ASC";
        List<NhapXuatNgay> ds = new ArrayList<>();
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 2); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new NhapXuatNgay(rs.getDate("ngay").toLocalDate(), rs.getInt("nhap"), rs.getInt("xuat")));
            }
        }
        return ds;
    }

    private static List<NangSuatKTV> docNangSuatKTV(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        // p chỉ khác NULL khi phiếu nằm trong kỳ: mọi phép đếm phải kiểm tra, nếu không sẽ cộng cả lịch sử.
        String sql = "SELECT nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi, "
                + "COUNT(DISTINCT p.maPhieuTNSC) AS soXe, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN 1 ELSE 0 END), 0) AS hmXong, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo <> N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN 1 ELSE 0 END), 0) AS hmDangLam, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN ct.tienCongThucTe ELSE 0 END), 0) AS tienCong "
                + "FROM NhanVien nv "
                + "LEFT JOIN ChiTietSuaChua_DichVu ct ON ct.maNV_KyThuat = nv.maNV "
                + "LEFT JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC AND " + PHIEU_TRONG_KY
                + "WHERE nv.viTriLamViec = N'Kỹ thuật viên' "
                + "GROUP BY nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi "
                + "ORDER BY hmXong DESC, tienCong DESC";
        List<NangSuatKTV> ds = new ArrayList<>();
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 1); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(rs.getString("maNV"), rs.getString("hoTen"), rs.getString("soDienThoai"),
                        rs.getString("diaChi"), VaiTro.KY_THUAT_VIEN);
                ds.add(new NangSuatKTV(nv, rs.getInt("soXe"), rs.getInt("hmXong"), rs.getInt("hmDangLam"), rs.getDouble("tienCong")));
            }
        }
        return ds;
    }

    private static List<LuotXeNgay> docLuotXeNgay(Connection c, Timestamp tu, Timestamp den) throws SQLException {
        String sql = "SELECT CAST(p.thoiGianTiepNhan AS DATE) AS ngay, COUNT(*) AS soLuot "
                + "FROM PhieuTiepNhanVaSuaChua p WHERE " + PHIEU_TRONG_KY
                + "GROUP BY CAST(p.thoiGianTiepNhan AS DATE) ORDER BY ngay ASC";
        List<LuotXeNgay> ds = new ArrayList<>();
        try (PreparedStatement ps = chuanBi(c, sql, tu, den, 1); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new LuotXeNgay(rs.getDate("ngay").toLocalDate(), rs.getInt("soLuot")));
            }
        }
        return ds;
    }
}
