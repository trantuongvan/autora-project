package entity;

import java.util.Objects;

public class ChiTietSuaChua_DichVu {
    private PhieuTiepNhanVaSuaChua phieu;
    private DichVu dichVu;
    private NhanVien kyThuatVien;
    private TrangThaiTienDo trangThaiTienDo;
    private double tienCongThucTe;

    public ChiTietSuaChua_DichVu() {
    }

    public ChiTietSuaChua_DichVu(PhieuTiepNhanVaSuaChua phieu, DichVu dichVu) {
        setPhieu(phieu);
        setDichVu(dichVu);
    }

    public ChiTietSuaChua_DichVu(PhieuTiepNhanVaSuaChua phieu, DichVu dichVu,
            NhanVien kyThuatVien, TrangThaiTienDo trangThaiTienDo, double tienCongThucTe) {
        setPhieu(phieu);
        setDichVu(dichVu);
        setKyThuatVien(kyThuatVien);
        setTrangThaiTienDo(trangThaiTienDo);
        setTienCongThucTe(tienCongThucTe);
    }

    public PhieuTiepNhanVaSuaChua getPhieu() {
        return phieu;
    }

    public void setPhieu(PhieuTiepNhanVaSuaChua phieu) {
        if (phieu == null) {
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không được để trống.");
        }
        this.phieu = phieu;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        if (dichVu == null) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        }
        this.dichVu = dichVu;
    }

    /** Null khi dòng chưa phân công kỹ thuật viên. */
    public NhanVien getKyThuatVien() {
        return kyThuatVien;
    }

    public void setKyThuatVien(NhanVien kyThuatVien) {
        this.kyThuatVien = kyThuatVien;
    }

    public TrangThaiTienDo getTrangThaiTienDo() {
        return trangThaiTienDo;
    }

    public void setTrangThaiTienDo(TrangThaiTienDo trangThaiTienDo) {
        if (trangThaiTienDo == null) {
            throw new IllegalArgumentException("Trạng thái tiến độ dịch vụ không hợp lệ.");
        }
        this.trangThaiTienDo = trangThaiTienDo;
    }

    public double getTienCongThucTe() {
        return tienCongThucTe;
    }

    public void setTienCongThucTe(double tienCongThucTe) {
        if (!Double.isFinite(tienCongThucTe) || tienCongThucTe < 0) {
            throw new IllegalArgumentException("Tiền công thực tế phải là số hữu hạn không âm.");
        }
        this.tienCongThucTe = tienCongThucTe;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiTietSuaChua_DichVu other = (ChiTietSuaChua_DichVu) obj;
        return Objects.equals(maPhieu(), other.maPhieu())
                && Objects.equals(maDichVu(), other.maDichVu());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieu(), maDichVu());
    }

    @Override
    public String toString() {
        return maPhieu() + " - " + maDichVu();
    }

    /** Thợ phụ trách để hiển thị: "NV0003 — Tên" hoặc "Chờ phân công". */
    public String getThoHienThi() {
        return kyThuatVien == null ? "Chờ phân công" : kyThuatVien.getMaNV() + " — " + kyThuatVien.getHoTen();
    }

    private String maPhieu() {
        return phieu == null ? null : phieu.getMaPhieuTNSC();
    }

    private String maDichVu() {
        return dichVu == null ? null : dichVu.getMaDichVu();
    }
}
