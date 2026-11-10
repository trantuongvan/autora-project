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
 * DAO xử lý toàn bộ truy vấn và tính toán số liệu cho UC014 - Xem báo cáo thống kê.
 * DAO chỉ gộp số liệu bằng SQL; lợi nhuận, tỷ lệ và giá trị tồn do DuLieuThongKe tự tính.
 * Doanh thu tính theo ngày thu tiền (ngayThanhToan), giống trang chủ quản lý.
 */
public class ThongKe_Dao {

    public DuLieuThongKe taiDuLieuThongKe(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        DuLieuThongKe kq = new DuLieuThongKe();
        try (Connection c = ConnectDB.getConnection()) {
            tinhThongKeDoanhThu(c, tuNgay, denNgay, kq);
            tinhThongKePhuTung(c, tuNgay, denNgay, kq);
            tinhThongKeHieuSuatKTV(c, tuNgay, denNgay, kq);
        }
        return kq;
    }

    private void tinhThongKeDoanhThu(Connection c, LocalDate tuNgay, LocalDate denNgay, DuLieuThongKe kq) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlNgay = "SELECT CAST(h.ngayThanhToan AS DATE) AS ngay, "
                + "COUNT(h.maHD) AS soHD, "
                + "COALESCE(SUM(h.tongTienCong), 0) AS tienCong, "
                + "COALESCE(SUM(h.tongTienPhuTung), 0) AS tienPhuTung, "
                + "COALESCE(SUM(h.tongTienGiam), 0) AS giamGia, "
                + "COALESCE(SUM(h.tongTienVAT), 0) AS vat, "
                + "COALESCE(SUM(h.tongThanhToan), 0) AS tongThanhToan "
                + "FROM HoaDon h "
                + "WHERE h.trangThaiThanhToan = N'Đã thanh toán' AND h.ngayThanhToan >= ? AND h.ngayThanhToan < ? "
                + "GROUP BY CAST(h.ngayThanhToan AS DATE) "
                + "ORDER BY ngay DESC";

        List<DoanhThuNgay> dsNgay = new ArrayList<>();
        int tongHD = 0;
        double tongCong = 0, tongPT = 0, tongGiam = 0, tongVAT = 0, tongDT = 0;

        try (PreparedStatement ps = c.prepareStatement(sqlNgay)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date d = rs.getDate("ngay");
                    int soHD = rs.getInt("soHD");
                    double tc = rs.getDouble("tienCong");
                    double tpt = rs.getDouble("tienPhuTung");
                    double gg = rs.getDouble("giamGia");
                    double vat = rs.getDouble("vat");
                    double tt = rs.getDouble("tongThanhToan");

                    dsNgay.add(new DoanhThuNgay(d.toLocalDate(), soHD, tc, tpt, gg, vat, tt));

                    tongHD += soHD;
                    tongCong += tc;
                    tongPT += tpt;
                    tongGiam += gg;
                    tongVAT += vat;
                    tongDT += tt;
                }
            }
        }

        String sqlGiaVon = "SELECT COALESCE(SUM(ct.soLuongSuDung * pt.giaNhap), 0) AS tongGiaVon "
                + "FROM HoaDon h "
                + "JOIN ChiTietSuaChua_PhuTung ct ON ct.maPhieuTNSC = h.maPhieuTNSC "
                + "JOIN PhuTung pt ON pt.maSKU = ct.maSKU "
                + "WHERE h.trangThaiThanhToan = N'Đã thanh toán' AND h.ngayThanhToan >= ? AND h.ngayThanhToan < ?";
        double tongGiaVonPhuTung = 0;
        try (PreparedStatement ps = c.prepareStatement(sqlGiaVon)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongGiaVonPhuTung = rs.getDouble("tongGiaVon");
                }
            }
        }

        kq.setTongSoHoaDon(tongHD);
        kq.setTongTienCong(tongCong);
        kq.setTongTienPhuTung(tongPT);
        kq.setTongGiamGia(tongGiam);
        kq.setTongVAT(tongVAT);
        kq.setTongDoanhThu(tongDT);
        kq.setTongGiaVonPhuTung(tongGiaVonPhuTung);
        kq.setDanhSachDoanhThuNgay(dsNgay);
    }

    private void tinhThongKePhuTung(Connection c, LocalDate tuNgay, LocalDate denNgay, DuLieuThongKe kq) throws SQLException {
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
        int soMatHang = 0, tongSLNhap = 0, tongSLXuat = 0;

        try (PreparedStatement ps = c.prepareStatement(sqlPT)) {
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
                    pt.setSoLuongTon(rs.getInt("soLuongTon"));
                    pt.setNguongCanhBao(rs.getInt("nguongCanhBao"));

                    int slNhap = rs.getInt("slNhap");
                    int slXuat = rs.getInt("slXuat");
                    dsPT.add(new ThongKePhuTung(pt, slNhap, slXuat));

                    soMatHang++;
                    tongSLNhap += slNhap;
                    tongGTNhap += rs.getDouble("gtNhap");
                    tongSLXuat += slXuat;
                    tongGTXuat += rs.getDouble("gtXuat");
                }
            }
        }

        int soPhieuNhap = 0;
        String sqlDemPN = "SELECT COUNT(*) FROM PhieuNhapKho WHERE ngayNhap >= ? AND ngayNhap < ?";
        try (PreparedStatement ps = c.prepareStatement(sqlDemPN)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) soPhieuNhap = rs.getInt(1);
            }
        }

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
        try (PreparedStatement ps = c.prepareStatement(sqlNXNgay)) {
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

        kq.setSoMatHangQuanLy(soMatHang);
        kq.setTongSoLuongNhap(tongSLNhap);
        kq.setTongGiaTriNhap(tongGTNhap);
        kq.setTongSoLuongXuat(tongSLXuat);
        kq.setTongGiaTriXuat(tongGTXuat);
        kq.setSoPhieuNhapTrongKy(soPhieuNhap);
        kq.setDanhSachPhuTung(dsPT);
        kq.setDanhSachNhapXuatNgay(dsNXNgay);
    }

    private void tinhThongKeHieuSuatKTV(Connection c, LocalDate tuNgay, LocalDate denNgay, DuLieuThongKe kq) throws SQLException {
        Timestamp tsTu = Timestamp.valueOf(tuNgay.atStartOfDay());
        Timestamp tsDen = Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay());

        String sqlXe = "SELECT COUNT(*) AS tongXe, "
                + "COALESCE(SUM(CASE WHEN trangThai = N'Hoàn thành' THEN 1 ELSE 0 END), 0) AS xeXong "
                + "FROM PhieuTiepNhanVaSuaChua "
                + "WHERE trangThai <> N'Đã hủy' AND thoiGianTiepNhan >= ? AND thoiGianTiepNhan < ?";
        int tongXe = 0, xeXong = 0;
        try (PreparedStatement ps = c.prepareStatement(sqlXe)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongXe = rs.getInt("tongXe");
                    xeXong = rs.getInt("xeXong");
                }
            }
        }

        String sqlKTV = "SELECT nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi, "
                + "COUNT(DISTINCT p.maPhieuTNSC) AS soXe, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN 1 ELSE 0 END), 0) AS hmXong, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo <> N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN 1 ELSE 0 END), 0) AS hmDangLam, "
                + "COALESCE(SUM(CASE WHEN ct.trangThaiTienDo = N'Hoàn thành' AND p.maPhieuTNSC IS NOT NULL THEN ct.tienCongThucTe ELSE 0 END), 0) AS tienCong "
                // p chỉ khác NULL khi phiếu nằm trong kỳ: mọi phép đếm phải kiểm tra, nếu không sẽ cộng cả lịch sử.
                + "FROM NhanVien nv "
                + "LEFT JOIN ChiTietSuaChua_DichVu ct ON ct.maNV_KyThuat = nv.maNV "
                + "LEFT JOIN PhieuTiepNhanVaSuaChua p ON p.maPhieuTNSC = ct.maPhieuTNSC "
                + "     AND p.trangThai <> N'Đã hủy' AND p.thoiGianTiepNhan >= ? AND p.thoiGianTiepNhan < ? "
                + "WHERE nv.viTriLamViec = N'Kỹ thuật viên' "
                + "GROUP BY nv.maNV, nv.hoTen, nv.soDienThoai, nv.diaChi "
                + "ORDER BY hmXong DESC, tienCong DESC";

        List<NangSuatKTV> dsKTV = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(sqlKTV)) {
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
                    int hmXong = rs.getInt("hmXong");
                    int hmDang = rs.getInt("hmDangLam");
                    double tienCong = rs.getDouble("tienCong");

                    dsKTV.add(new NangSuatKTV(nv, soXe, hmXong, hmDang, tienCong));
                }
            }
        }

        String sqlLuotNgay = "SELECT CAST(thoiGianTiepNhan AS DATE) AS ngay, COUNT(*) AS soLuot "
                + "FROM PhieuTiepNhanVaSuaChua "
                + "WHERE trangThai <> N'Đã hủy' AND thoiGianTiepNhan >= ? AND thoiGianTiepNhan < ? "
                + "GROUP BY CAST(thoiGianTiepNhan AS DATE) ORDER BY ngay ASC";
        List<LuotXeNgay> dsLuotNgay = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sqlLuotNgay)) {
            ps.setTimestamp(1, tsTu);
            ps.setTimestamp(2, tsDen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsLuotNgay.add(new LuotXeNgay(rs.getDate("ngay").toLocalDate(), rs.getInt("soLuot")));
                }
            }
        }

        kq.setTongLuotXeVaoXuong(tongXe);
        kq.setTongXeSuaXong(xeXong);
        kq.setDanhSachNangSuatKTV(dsKTV);
        kq.setDanhSachLuotXeNgay(dsLuotNgay);
    }
}