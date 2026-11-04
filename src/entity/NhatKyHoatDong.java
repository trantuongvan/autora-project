package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Một dòng nhật ký thao tác theo bảng NhatKyHoatDong.
 * Bảng không có khóa ngoại để nhật ký còn nguyên khi tài khoản bị xóa, nên tenDangNhap là chuỗi.
 */
public class NhatKyHoatDong {

    private final int maNhatKy;
    private final String tenDangNhap;
    private final LocalDateTime thoiGian;
    private final String hanhDong;
    private final String doiTuong;
    private final String giaTriCu;
    private final String giaTriMoi;

    public NhatKyHoatDong(int maNhatKy, String tenDangNhap, LocalDateTime thoiGian, String hanhDong,
            String doiTuong, String giaTriCu, String giaTriMoi) {
        this.maNhatKy = maNhatKy;
        this.tenDangNhap = tenDangNhap;
        this.thoiGian = thoiGian;
        this.hanhDong = hanhDong;
        this.doiTuong = doiTuong;
        this.giaTriCu = giaTriCu;
        this.giaTriMoi = giaTriMoi;
    }

    public int getMaNhatKy() {
        return maNhatKy;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public String getDoiTuong() {
        return doiTuong;
    }

    public String getGiaTriCu() {
        return giaTriCu;
    }

    public String getGiaTriMoi() {
        return giaTriMoi;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof NhatKyHoatDong && maNhatKy == ((NhatKyHoatDong) o).maNhatKy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhatKy);
    }

    @Override
    public String toString() {
        return maNhatKy + " - " + tenDangNhap + " - " + hanhDong + " - " + doiTuong;
    }
}
