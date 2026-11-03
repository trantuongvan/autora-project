package util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tạo liên kết ảnh VietQR. Chỉ gửi tài khoản, số tiền và mã hóa đơn cho dịch vụ này. */
public final class VietQRUtil {
    // shortcut: đây là tài khoản TEST, phải thay bằng tài khoản thật của gara trước khi dùng thực tế.
    public static final String MA_BIN = "970422";
    public static final String SO_TAI_KHOAN = "0123456789";
    public static final String TEN_TAI_KHOAN = "AUTORA GARAGE TEST";
    private static final int THOI_GIAN_CHO_MS = 5000;
    private static final Pattern O_QR = Pattern.compile("M(\\d+) (\\d+)h(\\d+)v1h-\\d+z");

    private VietQRUtil() {
    }

    public static String taoUrl(double soTien, String noiDung) {
        return "https://img.vietqr.io/image/" + MA_BIN + "-" + SO_TAI_KHOAN + "-compact2.png"
                + "?amount=" + Math.round(soTien)
                + "&addInfo=" + maHoa(noiDung)
                + "&accountName=" + maHoa(TEN_TAI_KHOAN);
    }

    private static String maHoa(String s) {
        try {
            // URLEncoder đổi khoảng trắng thành +, còn trong query của dịch vụ cần %20.
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Tải ảnh, có giới hạn thời gian. Gọi ngoài EDT. */
    public static BufferedImage taiAnh(String url) throws IOException {
        HttpURLConnection ketNoi = (HttpURLConnection) new URL(url).openConnection();
        ketNoi.setConnectTimeout(THOI_GIAN_CHO_MS);
        ketNoi.setReadTimeout(THOI_GIAN_CHO_MS);
        try (InputStream vao = ketNoi.getInputStream()) {
            ByteArrayOutputStream ra = new ByteArrayOutputStream();
            byte[] dem = new byte[8192];
            for (int n = vao.read(dem); n >= 0; n = vao.read(dem)) {
                ra.write(dem, 0, n);
            }
            BufferedImage anh = ImageIO.read(new ByteArrayInputStream(ra.toByteArray()));
            return anh != null ? anh : veQrTuSvg(new String(ra.toByteArray(), "UTF-8"));
        } finally {
            ketNoi.disconnect();
        }
    }

    // shortcut: dịch vụ đang chuyển PNG sang SVG mà Swing không đọc được, nên tự vẽ các ô QR trong <path>; đổi định dạng thì rơi về thông báo chữ.
    public static BufferedImage veQrTuSvg(String svg) throws IOException {
        Matcher o = O_QR.matcher(svg);
        List<int[]> ds = new ArrayList<>();
        int soO = 0;
        while (o.find()) {
            int[] d = {Integer.parseInt(o.group(1)), Integer.parseInt(o.group(2)), Integer.parseInt(o.group(3))};
            ds.add(d);
            soO = Math.max(soO, Math.max(d[0] + d[2], d[1] + 1));
        }
        if (ds.isEmpty()) {
            throw new IOException("Dịch vụ VietQR không trả về ảnh.");
        }
        int vien = 2;
        int co = 6;
        BufferedImage anh = new BufferedImage((soO + 2 * vien) * co, (soO + 2 * vien) * co, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = anh.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, anh.getWidth(), anh.getHeight());
        g.setColor(Color.BLACK);
        for (int[] d : ds) {
            g.fillRect((d[0] + vien) * co, (d[1] + vien) * co, d[2] * co, co);
        }
        g.dispose();
        return anh;
    }
}
