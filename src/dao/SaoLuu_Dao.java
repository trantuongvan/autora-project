package dao;

import connection.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Sao lưu CSDL ra tệp .bak trong thư mục sao lưu mặc định của SQL Server (NFR-03). */
public class SaoLuu_Dao {

    /** Trả về đường dẫn tệp trên máy chủ; tài khoản ứng dụng cần quyền db_backupoperator. */
    public String saoLuu(String tenDangNhap) throws SQLException {
        String tenTep = "GaraAUTORA_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".bak";
        try (Connection c = ConnectDB.getConnection()) {
            String thuMuc = thuMucSaoLuu(c);
            String duongDan = thuMuc == null ? tenTep : thuMuc + (thuMuc.endsWith("\\") ? "" : "\\") + tenTep;
            // BACKUP không nhận tham số cho tên tệp; tên do ứng dụng sinh nên chỉ có chữ, số và dấu gạch.
            try (Statement s = c.createStatement()) {
                s.execute("BACKUP DATABASE GaraAUTORA TO DISK = N'" + duongDan.replace("'", "''") + "' WITH INIT, CHECKSUM");
            }
            new NhatKy_Dao().ghi(c, tenDangNhap, "Sao lưu dữ liệu", "GaraAUTORA", null, duongDan);
            return duongDan;
        }
    }

    /** Thư mục sao lưu mặc định của máy chủ; LocalDB không có thì dùng thư mục chứa tệp dữ liệu của CSDL. */
    private static String thuMucSaoLuu(Connection c) throws SQLException {
        String sql = "SELECT COALESCE(CAST(SERVERPROPERTY('InstanceDefaultBackupPath') AS NVARCHAR(400)), "
                + "(SELECT TOP (1) LEFT(physical_name, LEN(physical_name) - CHARINDEX('\\', REVERSE(physical_name))) "
                + "FROM sys.database_files WHERE type = 0))";
        try (PreparedStatement p = c.prepareStatement(sql);
                ResultSet r = p.executeQuery()) {
            return r.next() ? r.getString(1) : null;
        }
    }
}
