package entity;

import java.util.Objects;

/** Một dòng NhanVien_DichVu: kỹ thuật viên làm được dịch vụ này trong thoiGianPhut phút. */
public class KyNang {
    private NhanVien nhanVien;
    private DichVu dichVu;
    private int thoiGianPhut;

    public KyNang() {
    }

    public KyNang(NhanVien nhanVien, DichVu dichVu, int thoiGianPhut) {
        setNhanVien(nhanVien);
        setDichVu(dichVu);
        setThoiGianPhut(thoiGianPhut);
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        if (nhanVien == null) {
            throw new IllegalArgumentException("Nhân viên không được để trống.");
        }
        this.nhanVien = nhanVien;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        if (dichVu == null) {
            throw new IllegalArgumentException("Dịch vụ không được để trống.");
        }
        this.dichVu = dichVu;
    }

    public int getThoiGianPhut() {
        return thoiGianPhut;
    }

    public void setThoiGianPhut(int thoiGianPhut) {
        if (thoiGianPhut <= 0) {
            throw new IllegalArgumentException("Thời gian làm dịch vụ phải lớn hơn 0 phút.");
        }
        this.thoiGianPhut = thoiGianPhut;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KyNang other = (KyNang) obj;
        return Objects.equals(nhanVien, other.nhanVien) && Objects.equals(dichVu, other.dichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nhanVien, dichVu);
    }

    @Override
    public String toString() {
        return nhanVien + " - " + dichVu + " (" + thoiGianPhut + " phút)";
    }
}
