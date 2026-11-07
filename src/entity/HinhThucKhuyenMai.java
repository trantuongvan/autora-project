package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum HinhThucKhuyenMai {
    PHAN_TRAM("Giảm %") {
        @Override
        public double tinhTienGiam(double coSo, double giaTri) {
            return Thue.phanTram(coSo, giaTri);
        }
    },
    TIEN_MAT("Giảm tiền mặt") {
        @Override
        public double tinhTienGiam(double coSo, double giaTri) {
            return Math.round(giaTri);
        }
    };

    private final String text;

    HinhThucKhuyenMai(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /** Số tiền giảm trên số tiền cơ sở, chưa chặn theo cơ sở. */
    public abstract double tinhTienGiam(double coSo, double giaTri);

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
