package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Băm và kiểm tra mật khẩu bằng PBKDF2 có sẵn trong JDK. Dạng lưu: pbkdf2$soVong$muoi$bam. */
public final class MatKhauUtil {
    private static final String THUAT_TOAN = "PBKDF2WithHmacSHA256";
    private static final int SO_VONG = 120_000;
    private static final int SO_BYTE_MUOI = 16;
    private static final int SO_BIT_BAM = 256;

    private MatKhauUtil() {
    }

    public static String bam(String matKhau) {
        byte[] muoi = new byte[SO_BYTE_MUOI];
        new SecureRandom().nextBytes(muoi);
        byte[] bam = tinhBam(matKhau, muoi, SO_VONG);
        Base64.Encoder ma = Base64.getEncoder();
        return "pbkdf2$" + SO_VONG + "$" + ma.encodeToString(muoi) + "$" + ma.encodeToString(bam);
    }

    /** Giá trị lưu sai định dạng (ví dụ mật khẩu dạng rõ còn sót) luôn trả về false. */
    public static boolean kiemTra(String matKhau, String giaTriLuu) {
        if (giaTriLuu == null) {
            return false;
        }
        String[] phan = giaTriLuu.split("\\$");
        if (phan.length != 4 || !"pbkdf2".equals(phan[0])) {
            return false;
        }
        try {
            byte[] muoi = Base64.getDecoder().decode(phan[2]);
            byte[] bamLuu = Base64.getDecoder().decode(phan[3]);
            // So sánh thời gian cố định để không lộ thông tin qua độ trễ.
            return MessageDigest.isEqual(bamLuu, tinhBam(matKhau, muoi, Integer.parseInt(phan[1])));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] tinhBam(String matKhau, byte[] muoi, int soVong) {
        PBEKeySpec spec = new PBEKeySpec(matKhau.toCharArray(), muoi, soVong, SO_BIT_BAM);
        try {
            return SecretKeyFactory.getInstance(THUAT_TOAN).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("JDK không hỗ trợ " + THUAT_TOAN, ex);
        } finally {
            spec.clearPassword();
        }
    }
}
