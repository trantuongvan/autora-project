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

    /** Bước chuyển hợp lệ theo tiến trình thực tế; phiếu đã xong hoặc đã hủy không chuyển được nữa. */
    public boolean coTheChuyenSang(TrangThaiPhieu moi) {
        switch (this) {
            case CHO_KIEM_TRA:
                return moi == DANG_SUA || moi == CHO_PHU_TUNG;
            case DANG_SUA:
                return moi == CHO_PHU_TUNG || moi == HOAN_THANH;
            case CHO_PHU_TUNG:
                return moi == DANG_SUA;
            default:
                return false;
        }
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
