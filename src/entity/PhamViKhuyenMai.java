package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum PhamViKhuyenMai {
    TONG_HOA_DON("Tổng hóa đơn") {
        @Override
        double coSoGiam(double tienCong, double tienPhuTung) {
            return tienCong + tienPhuTung;
        }
    },
    NHOM_DICH_VU("Nhóm dịch vụ") {
        @Override
        double coSoGiam(double tienCong, double tienPhuTung) {
            return tienCong;
        }
    },
    PHU_TUNG("Phụ tùng") {
        @Override
        double coSoGiam(double tienCong, double tienPhuTung) {
            return tienPhuTung;
        }
    };

    private final String text;

    PhamViKhuyenMai(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /** Phần tiền của hóa đơn mà khuyến mãi được áp dụng. */
    abstract double coSoGiam(double tienCong, double tienPhuTung);

    public static PhamViKhuyenMai fromDb(String text) {
        for (PhamViKhuyenMai giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("PhamViKhuyenMai không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
