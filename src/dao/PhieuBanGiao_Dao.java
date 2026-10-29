package dao;

import connection.ConnectDB;
import entity.PhieuBanGiao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn danh sách bàn giao, kiểm tra điều kiện và ghi nhận bàn giao trong transaction. */
public class PhieuBanGiao_Dao {
    private static final String LIST_SQL = "SELECT p.maPhieuTNSC,x.bienSo,k.hoTen,k.soDienThoai,CONCAT(x.hangXe,' ',x.dongXe) AS xe,COALESCE(ti.leHoanTat,0) AS tienDo,COALESCE(h.trangThaiThanhToan,N'Chưa lập hóa đơn') AS thanhToan,COALESCE(h.tongThanhToan,0) AS tong,k.duocGhiNo,COALESCE(h.maHD,'') AS maHD,COALESCE(h.tongTienCong,0) AS tienCong,COALESCE(h.tongTienPhuTung,0) AS tienPhuTung,COALESCE(h.tongTienGiam,0) AS giam,COALESCE(h.tongThueVAT,0) AS vat,COALESCE(th.tyLe,0) AS thueSuat,p.trangThai,COALESCE(bg.maPhieuBG,'') AS maPhieuBG,bg.ngayBanGiao,bg.maNV_BanGiao,nv.hoTen AS tenNVBanGiao FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo=p.bienSo JOIN KhachHang k ON k.maKH=x.maKH LEFT JOIN HoaDon h ON h.maPhieuTNSC=p.maPhieuTNSC LEFT JOIN Thue th ON th.maThue=h.maThue LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC=p.maPhieuTNSC LEFT JOIN NhanVien nv ON nv.maNV=bg.maNV_BanGiao OUTER APPLY (SELECT CASE WHEN COUNT(*)=0 THEN 0 ELSE CONVERT(INT,FLOOR(100.0*SUM(CASE WHEN d.trangThaiTienDo=N'Hoàn thành' THEN 1 ELSE 0 END)/COUNT(*))) END AS leHoanTat FROM ChiTietSuaChua_DichVu d WHERE d.maPhieuTNSC=p.maPhieuTNSC) ti";

    public PhieuBanGiao findById(String maPhieuBG) throws SQLException {
        String sql="SELECT maPhieuBG,maPhieuTNSC,maNV_BanGiao,ngayBanGiao,xacNhanHoanTat,xacNhanThanhToan,chuKyKhachHang FROM PhieuBanGiao WHERE maPhieuBG=?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,maPhieuBG);try(ResultSet r=p.executeQuery()){return r.next()?mapPhieuBanGiao(r):null;}
        }
    }
    public PhieuBanGiao findByMaPhieuSuaChua(String maPhieuTNSC) throws SQLException {
        String sql="SELECT maPhieuBG,maPhieuTNSC,maNV_BanGiao,ngayBanGiao,xacNhanHoanTat,xacNhanThanhToan,chuKyKhachHang FROM PhieuBanGiao WHERE maPhieuTNSC=?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,maPhieuTNSC);try(ResultSet r=p.executeQuery()){return r.next()?mapPhieuBanGiao(r):null;}
        }
    }

    public List<Object[]> findDanhSach(String tuKhoa, String loc) throws SQLException {
        String sql=LIST_SQL+" WHERE (?='' OR p.maPhieuTNSC LIKE ? OR x.bienSo LIKE ? OR k.hoTen LIKE ?) AND (?='Tất cả' OR (?='Chờ bàn giao' AND NOT EXISTS(SELECT 1 FROM PhieuBanGiao bg WHERE bg.maPhieuTNSC=p.maPhieuTNSC)) OR (?='Đã bàn giao' AND EXISTS(SELECT 1 FROM PhieuBanGiao bg WHERE bg.maPhieuTNSC=p.maPhieuTNSC))) ORDER BY p.thoiGianTiepNhan DESC";
        String q=tuKhoa==null?"":tuKhoa.trim();String filter=loc==null?"Tất cả":loc;List<Object[]> ds=new ArrayList<>();
        try(Connection c=ConnectDB.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,q);s.setString(2,"%"+q+"%");s.setString(3,"%"+q+"%");s.setString(4,"%"+q+"%");s.setString(5,filter);s.setString(6,filter);s.setString(7,filter);try(ResultSet r=s.executeQuery()){while(r.next()){int progress=r.getInt("tienDo");String payment=r.getString("thanhToan");boolean paid="Đã thanh toán".equals(payment);boolean debt="Công nợ".equals(payment)&&r.getBoolean("duocGhiNo");boolean ready=progress==100&&(paid||debt);String invoice=paid?"Đã thanh toán":debt?"Công nợ hợp lệ":payment;String condition=ready?"Đủ điều kiện • Bàn giao":"Chưa đủ điều kiện";ds.add(new Object[]{r.getString("maPhieuTNSC"),r.getString("bienSo"),r.getString("hoTen"),r.getString("xe"),progress+"%",invoice,format(r.getDouble("tong"))+" đ",condition});}}}return ds;
    }

    public boolean xacNhan(String maPhieu, String maNhanVien, String chuKy) throws SQLException {
        try(Connection c=ConnectDB.getConnection()){
            c.setAutoCommit(false);
            try {
                String check="SELECT p.maPhieuTNSC,COUNT(d.maDichVu) AS tongDV,SUM(CASE WHEN d.trangThaiTienDo=N'Hoàn thành' THEN 1 ELSE 0 END) AS xong,MAX(CASE WHEN h.trangThaiThanhToan=N'Đã thanh toán' OR (h.trangThaiThanhToan=N'Công nợ' AND k.duocGhiNo=1) THEN 1 ELSE 0 END) AS daXuLy,MAX(CASE WHEN bg.maPhieuBG IS NULL THEN 0 ELSE 1 END) AS daBanGiao FROM PhieuTiepNhanVaSuaChua p JOIN Xe x ON x.bienSo=p.bienSo JOIN KhachHang k ON k.maKH=x.maKH LEFT JOIN ChiTietSuaChua_DichVu d ON d.maPhieuTNSC=p.maPhieuTNSC LEFT JOIN HoaDon h ON h.maPhieuTNSC=p.maPhieuTNSC LEFT JOIN PhieuBanGiao bg ON bg.maPhieuTNSC=p.maPhieuTNSC WHERE p.maPhieuTNSC=? GROUP BY p.maPhieuTNSC";
                try(PreparedStatement p=c.prepareStatement(check)){p.setString(1,maPhieu);try(ResultSet r=p.executeQuery()){if(!r.next()||r.getInt("tongDV")==0||r.getInt("xong")!=r.getInt("tongDV")||r.getInt("daXuLy")!=1||r.getInt("daBanGiao")!=0)throw new SQLException("Phiếu chưa hoàn tất, chưa thanh toán hợp lệ hoặc đã được bàn giao.");}}
                String idSql="SELECT 'PBG'+RIGHT('00000'+CAST(COALESCE(MAX(TRY_CONVERT(INT,SUBSTRING(maPhieuBG,4,18))),0)+1 AS VARCHAR(18)),5) FROM PhieuBanGiao WITH (UPDLOCK,HOLDLOCK)";
                String id;try(Statement s=c.createStatement();ResultSet r=s.executeQuery(idSql)){r.next();id=r.getString(1);}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO PhieuBanGiao(maPhieuBG,maPhieuTNSC,maNV_BanGiao,ngayBanGiao,xacNhanHoanTat,xacNhanThanhToan,chuKyKhachHang) VALUES(?,?,?,GETDATE(),1,1,?)")){p.setString(1,id);p.setString(2,maPhieu);p.setString(3,maNhanVien);p.setString(4,chuKy);p.executeUpdate();}
                c.commit();return true;
            } catch(SQLException ex){c.rollback();throw ex;} finally {c.setAutoCommit(true);}
        }
    }

    /** Trả chi tiết: biển số, xe, khách, % hoàn tất, tiền công, phụ tùng, giảm, VAT, tổng, hóa đơn, đủ điều kiện, trạng thái, thuế, SĐT, mã/ngày/NV bàn giao. */
    public Object[] findChiTiet(String maPhieu) throws SQLException {
        String sql=LIST_SQL+" WHERE p.maPhieuTNSC=?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,maPhieu);try(ResultSet r=p.executeQuery()){if(!r.next())return null;int progress=r.getInt("tienDo");String payment=r.getString("thanhToan");boolean paid="Đã thanh toán".equals(payment);boolean debt="Công nợ".equals(payment)&&r.getBoolean("duocGhiNo");return new Object[]{r.getString("bienSo"),r.getString("xe"),r.getString("hoTen"),progress,r.getDouble("tienCong"),r.getDouble("tienPhuTung"),r.getDouble("giam"),r.getDouble("vat"),r.getDouble("tong"),r.getString("maHD"),progress==100&&(paid||debt),payment,r.getDouble("thueSuat"),r.getString("soDienThoai"),r.getString("maPhieuBG"),r.getTimestamp("ngayBanGiao"),r.getString("tenNVBanGiao"),r.getBoolean("duocGhiNo")};}}
    }

    public List<Object[]> findDashboardJobs() throws SQLException {
        String sql=LIST_SQL.replaceFirst("SELECT ","SELECT TOP 7 ")+" WHERE bg.maPhieuBG IS NULL ORDER BY p.thoiGianTiepNhan DESC";
        List<Object[]> ds=new ArrayList<>();try(Connection c=ConnectDB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){while(r.next()){int n=r.getInt("tienDo");boolean paid="Đã thanh toán".equals(r.getString("thanhToan"));boolean debt="Công nợ".equals(r.getString("thanhToan"))&&r.getBoolean("duocGhiNo");boolean ready=n==100&&(paid||debt);String status=ready?"Chờ bàn giao":("Chờ kiểm tra".equals(r.getString("trangThai"))?"Chờ tiếp nhận":n==100?"Chờ xử lý hóa đơn":"Đang sửa • "+n+"%");String action=ready?"Bàn giao →":n==0?"Hồ sơ xe":"Xem phiếu";ds.add(new Object[]{r.getString("maPhieuTNSC"),r.getString("bienSo"),r.getString("hoTen"),r.getString("xe"),status,action});}}return ds;
    }
    /** Thống kê toàn gara; tách khỏi 7 dòng gần nhất đang hiển thị ở dashboard. */
    public int[] findDashboardStats() throws SQLException {
        String sql="SELECT SUM(CASE WHEN trangThai=N'Chờ kiểm tra' THEN 1 ELSE 0 END) AS choTiepNhan,SUM(CASE WHEN trangThai<>N'Chờ kiểm tra' AND tienDo<100 THEN 1 ELSE 0 END) AS dangSua,SUM(CASE WHEN tienDo=100 AND (thanhToan=N'Đã thanh toán' OR (thanhToan=N'Công nợ' AND duocGhiNo=1)) THEN 1 ELSE 0 END) AS choBanGiao FROM ("+LIST_SQL+") q WHERE maPhieuBG=''";
        try(Connection c=ConnectDB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return new int[]{r.getInt("choTiepNhan"),r.getInt("dangSua"),r.getInt("choBanGiao"),countDeliveredToday()};}
    }
    public int countDeliveredToday() throws SQLException {try(Connection c=ConnectDB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM PhieuBanGiao WHERE CAST(ngayBanGiao AS DATE)=CAST(GETDATE() AS DATE)")){r.next();return r.getInt(1);}}
    private static String format(double n){return String.format(java.util.Locale.forLanguageTag("vi-VN"),"%,.0f",n).replace(',', '.');}
    private PhieuBanGiao mapPhieuBanGiao(ResultSet r)throws SQLException{Timestamp time=r.getTimestamp("ngayBanGiao");return new PhieuBanGiao(r.getString("maPhieuBG"),r.getString("maPhieuTNSC"),r.getString("maNV_BanGiao"),time==null?null:time.toLocalDateTime(),r.getBoolean("xacNhanHoanTat"),r.getBoolean("xacNhanThanhToan"),r.getString("chuKyKhachHang"));}
}
