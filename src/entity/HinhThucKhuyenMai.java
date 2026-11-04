package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum HinhThucKhuyenMai {
    PHAN_TRAM("Giảm %"),
    TIEN_MAT("Giảm tiền mặt");

    private final String text;

    HinhThucKhuyenMai(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static HinhThucKhuyenMai fromDb(String text) {
        for (HinhThucKhuyenMai giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("HinhThucKhuyenMai không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
