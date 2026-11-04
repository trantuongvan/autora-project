package connection;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDB {

    /** Tệp cấu hình đặt ở thư mục chạy ứng dụng và không đưa lên git; xem database.properties.example. */
    private static final File TEP_CAU_HINH = new File("database.properties");

    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    private ConnectDB() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Không tìm thấy driver Microsoft SQL Server", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Properties cauHinh = docCauHinh(TEP_CAU_HINH);
        return DriverManager.getConnection(cauHinh.getProperty("url"), cauHinh.getProperty("user"),
                cauHinh.getProperty("password"));
    }

    public static Properties docCauHinh(File tep) throws SQLException {
        Properties cauHinh = new Properties();
        try (Reader doc = Files.newBufferedReader(tep.toPath(), StandardCharsets.UTF_8)) {
            cauHinh.load(doc);
        } catch (IOException ex) {
            throw new SQLException("Không đọc được tệp cấu hình " + tep.getAbsolutePath()
                    + ". Hãy sao chép database.properties.example thành database.properties rồi điền tài khoản CSDL.", ex);
        }
        for (String khoa : new String[] {"url", "user", "password"}) {
            if (cauHinh.getProperty(khoa, "").trim().isEmpty()) {
                throw new SQLException("Tệp cấu hình " + tep.getAbsolutePath() + " thiếu giá trị " + khoa + ".");
            }
        }
        return cauHinh;
    }
}
