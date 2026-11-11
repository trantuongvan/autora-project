package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum HinhThucThanhToan {
    TIEN_MAT("Tiền mặt") {
        @Override
        public boolean duTien(Double tienKhachDua, double tongThanhToan) {
            return tienKhachDua != null && tienKhachDua >= tongThanhToan;
        }
    },
    CHUYEN_KHOAN("Chuyển khoản"),
    CONG_NO("Công nợ");

    private final String text;

    HinhThucThanhToan(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /** Chỉ tiền mặt cần khách đưa đủ tiền tại quầy. */
    public boolean duTien(Double tienKhachDua, double tongThanhToan) {
        return true;
    }

    public static HinhThucThanhToan fromDb(String text) {
        for (HinhThucThanhToan giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("HinhThucThanhToan không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
