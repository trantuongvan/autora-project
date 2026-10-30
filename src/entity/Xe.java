package entity;

import java.util.Objects;

/** POJO hồ sơ xe theo bảng Xe. */
public class Xe {

    private String bienSo;
    private String soVIN;
    private String hangXe;
    private String dongXe;
    private String mauSon;
    private String maKH;
    private Integer doiXe;

    public Xe() {
    }

    public Xe(String bienSo) {
        setBienSo(bienSo);
    }

    public Xe(String bienSo, String soVIN, String hangXe, String dongXe, Integer doiXe, String maKH) {
        this(bienSo, soVIN, hangXe, dongXe, doiXe, "", maKH);
    }

    public Xe(String bienSo, String soVIN, String hangXe, String dongXe, Integer doiXe, String mauSon, String maKH) {
        setBienSo(bienSo);
        setSoVIN(soVIN);
        setHangXe(hangXe);
        setDongXe(dongXe);
        setDoiXe(doiXe);
        setMaKH(maKH);
        setMauSon(mauSon);
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số không được để trống.");
        }
        this.bienSo = bienSo.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public static boolean bienSoHopLe(String bienSo) {
        return bienSo != null && bienSo.trim().toUpperCase(java.util.Locale.ROOT)
                .matches("^[0-9]{2}[A-Z]{1,2}-(?:[0-9]{3}\\.[0-9]{2}|[0-9]{4,5})$");
    }

    public String getSoVIN() {
        return soVIN;
    }

    public void setSoVIN(String soVIN) {
        this.soVIN = soVIN == null ? "" : soVIN.trim();
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String hangXe) {
        if (hangXe == null || hangXe.trim().isEmpty()) {
            throw new IllegalArgumentException("Hãng xe không được để trống.");
        }
        this.hangXe = hangXe.trim();
    }

    public String getDongXe() {
        return dongXe;
    }

    public void setDongXe(String dongXe) {
        this.dongXe = dongXe == null ? "" : dongXe.trim();
    }

    public Integer getDoiXe() {
        return doiXe;
    }

    public void setDoiXe(Integer doiXe) {
        if (doiXe != null && (doiXe < 1886 || doiXe > 2100)) {
            throw new IllegalArgumentException("Năm sản xuất không hợp lệ.");
        }
        this.doiXe = doiXe;
    }

    public String getMauSon() {
        return mauSon;
    }

    public void setMauSon(String mauSon) {
        this.mauSon = mauSon == null ? "" : mauSon.trim();
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Chủ sở hữu không được để trống.");
        }
        this.maKH = maKH.trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Xe)) {
            return false;
        }
        Xe that = (Xe) obj;
        return Objects.equals(bienSo, that.bienSo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bienSo);
    }

    @Override
    public String toString() {
        return bienSo + " - " + hangXe + " " + dongXe;
    }
}
