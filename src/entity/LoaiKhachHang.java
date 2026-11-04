package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum LoaiKhachHang {
    KHACH_VANG_LAI("Khách vãng lai"),
    THANH_VIEN("Thành viên"),
    VIP("VIP");

    private final String text;

    LoaiKhachHang(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static LoaiKhachHang fromDb(String text) {
        for (LoaiKhachHang giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("LoaiKhachHang không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
