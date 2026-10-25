package test;

import connection.ConnectDB;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   ĐANG KIỂM TRA KẾT NỐI CƠ SỞ DỮ LIỆU AUTORA...  ");
        System.out.println("=================================================");

        try (Connection con = ConnectDB.getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println(">> [SUCCESS] KẾT NỐI ĐẾN SQL SERVER THÀNH CÔNG RỰC RỠ!");
                DatabaseMetaData meta = con.getMetaData();
                System.out.println(">> Database Product Name: " + meta.getDatabaseProductName());
                System.out.println(">> Database Version     : " + meta.getDatabaseProductVersion());
                System.out.println(">> Driver Name          : " + meta.getDriverName());

                // Đếm thử số lượng nhân viên trong bảng NhanVien
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM NhanVien")) {
                    if (rs.next()) {
                        System.out.println(">> Số lượng bản ghi mẫu trong bảng NhanVien: " + rs.getInt("total"));
                    }
                }
                System.out.println("=================================================");
                System.out.println(">> Cơ sở dữ liệu đã sẵn sàng cho toàn nhóm code!");
                System.out.println("=================================================");
            }
        } catch (Exception e) {
            System.err.println(">> [FAILED] KẾT NỐI THẤT BẠI!");
            System.err.println(">> Nguyên nhân lỗi: " + e.getMessage());
            System.err.println(">> HƯỚNG DẪN KHẮC PHỤC:");
            System.err.println("   1. Hãy chắc chắn dịch vụ SQL Server đang chạy (Running).");
            System.err.println("   2. Đã mở SQL Server Configuration Manager -> Bật TCP/IP (Enabled) ở cổng 1433.");
            System.err.println("   3. Đã chạy file script 'database/GaraAUTORA_Database.sql' để tạo database GaraAUTORA.");
            System.err.println("   4. Kiểm tra tài khoản 'sa' và mật khẩu trong class 'connection.ConnectDB.java'.");
        }
    }
}
