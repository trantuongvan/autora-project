package entity;

import java.util.Objects;

/** POJO hồ sơ xe theo bảng Xe. */
public class Xe {
    private String bienSo, soVIN, hangXe, dongXe, mauSon, maKH;
    private Integer doiXe;
    public Xe() {}
    public Xe(String bienSo) { setBienSo(bienSo); }
    public Xe(String bienSo, String soVIN, String hangXe, String dongXe, Integer doiXe, String maKH) {
        this(bienSo, soVIN, hangXe, dongXe, doiXe, "", maKH);
    }
    public Xe(String bienSo, String soVIN, String hangXe, String dongXe, Integer doiXe, String mauSon, String maKH) {
        setBienSo(bienSo); setSoVIN(soVIN); setHangXe(hangXe); setDongXe(dongXe); setDoiXe(doiXe); setMaKH(maKH);
        setMauSon(mauSon);
    }
    public String getBienSo() { return bienSo; }
    public void setBienSo(String v) {
        bienSo = required(v, "Biển số").toUpperCase(java.util.Locale.ROOT);
    }
    public static boolean bienSoHopLe(String v) {
        return v != null && v.trim().toUpperCase(java.util.Locale.ROOT)
                .matches("^[0-9]{2}[A-Z]{1,2}-(?:[0-9]{3}\\.[0-9]{2}|[0-9]{4,5})$");
    }
    public String getSoVIN() { return soVIN; }
    public void setSoVIN(String v) { soVIN = v == null ? "" : v.trim(); }
    public String getHangXe() { return hangXe; }
    public void setHangXe(String v) { hangXe = required(v, "Hãng xe"); }
    public String getDongXe() { return dongXe; }
    public void setDongXe(String v) { dongXe = v == null ? "" : v.trim(); }
    public Integer getDoiXe() { return doiXe; }
    public void setDoiXe(Integer v) { if (v != null && (v < 1886 || v > 2100)) throw new IllegalArgumentException("Năm sản xuất không hợp lệ."); doiXe = v; }
    public String getMauSon() { return mauSon; }
    public void setMauSon(String v) { mauSon = v == null ? "" : v.trim(); }
    public String getMaKH() { return maKH; }
    public void setMaKH(String v) { maKH = required(v, "Chủ sở hữu"); }
    private static String required(String v, String label) { if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException(label + " không được để trống."); return v.trim(); }
    @Override public boolean equals(Object obj) { return this==obj || obj instanceof Xe && Objects.equals(bienSo,((Xe)obj).bienSo); }
    @Override public int hashCode() { return Objects.hash(bienSo); }
    @Override public String toString() { return bienSo+" - "+hangXe+" "+dongXe; }
}
