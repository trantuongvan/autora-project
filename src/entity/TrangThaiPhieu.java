package entity;

/** Giá trị lưu trong CSDL chính là chuỗi tiếng Việt của từng hằng. */
public enum TrangThaiPhieu {
    CHO_KIEM_TRA("Chờ kiểm tra"),
    DANG_SUA("Đang sửa"),
    CHO_PHU_TUNG("Chờ phụ tùng"),
    HOAN_THANH("Hoàn thành"),
    DA_HUY("Đã hủy");

    private final String text;

    TrangThaiPhieu(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /** Nhãn "Chờ sửa" chỉ để hiển thị; trong CSDL phiếu đó là "Chờ kiểm tra". */
    public static TrangThaiPhieu tuNhan(String nhan) {
        if (nhan == null) {
            return CHO_KIEM_TRA;
        }
        String s = nhan.trim();
        if ("Chờ sửa".equalsIgnoreCase(s)) {
            return CHO_KIEM_TRA;
        }
        for (TrangThaiPhieu t : values()) {
            if (t.text.equalsIgnoreCase(s)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Trạng thái phiếu không hợp lệ: " + s);
    }

    public static TrangThaiPhieu fromDb(String text) {
        for (TrangThaiPhieu giaTri : values()) {
            if (giaTri.text.equals(text)) {
                return giaTri;
            }
        }
        throw new IllegalArgumentException("TrangThaiPhieu không hợp lệ: " + text);
    }

    @Override
    public String toString() {
        return text;
    }
}
