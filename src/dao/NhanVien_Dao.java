package dao;

import connection.ConnectDB;
import entity.NhanVien;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn nhân viên và tài khoản liên kết theo schema AUTORA. */
public class NhanVien_Dao {
    private static final String SELECT_COLUMNS = "SELECT n.maNV,n.hoTen,n.soDienThoai,n.diaChi,n.viTriLamViec,t.tenDangNhap ";
    private static final String FROM = "FROM NhanVien n LEFT JOIN TaiKhoan t ON t.maNV=n.maNV ";

    public List<NhanVien> findAll() throws SQLException { return search("", ""); }

    public List<NhanVien> search(String keyword, String viTri) throws SQLException {
        String k = keyword == null ? "" : keyword.trim();
        String position = viTri == null ? "" : viTri;
        String sql = SELECT_COLUMNS + FROM
                + "WHERE (?='' OR n.maNV LIKE ? OR n.hoTen LIKE ? OR n.soDienThoai LIKE ?) AND (?='' OR n.viTriLamViec=?) ORDER BY n.maNV";
        List<NhanVien> result = new ArrayList<>();
        try (Connection c=ConnectDB.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,k); p.setString(2,"%"+k+"%"); p.setString(3,"%"+k+"%"); p.setString(4,"%"+k+"%");
            p.setString(5,position); p.setString(6,position);
            try (ResultSet r=p.executeQuery()) { while(r.next()) result.add(mapNhanVien(r)); }
        }
        return result;
    }

    /** Nhân viên theo vị trí để chọn người phụ trách ở các nghiệp vụ khác. */
    public List<NhanVien> findByViTri(String viTri) throws SQLException {
        String sql=SELECT_COLUMNS+FROM+"WHERE n.viTriLamViec=? ORDER BY n.hoTen";
        List<NhanVien> result=new ArrayList<>();
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,viTri);try(ResultSet r=p.executeQuery()){while(r.next())result.add(mapNhanVien(r));}
        }
        return result;
    }

    public boolean existsSoDienThoai(String sdt, String exceptMaNV) throws SQLException {
        String sql="SELECT 1 FROM NhanVien WHERE soDienThoai=? AND maNV<>?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,sdt);p.setString(2,exceptMaNV==null?"":exceptMaNV);
            try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }

    public NhanVien findById(String id) throws SQLException {
        String sql=SELECT_COLUMNS+FROM+"WHERE n.maNV=?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,id);try(ResultSet r=p.executeQuery()){return r.next()?mapNhanVien(r):null;}
        }
    }

    public String nextId() throws SQLException {
        String sql="SELECT 'NV'+RIGHT('0000'+CAST(COALESCE(MAX(TRY_CONVERT(INT,SUBSTRING(maNV,3,18))),0)+1 AS VARCHAR(18)),4) FROM NhanVien";
        try(Connection c=ConnectDB.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getString(1);}
    }

    public boolean insert(NhanVien nv) throws SQLException {
        String sql="INSERT INTO NhanVien(maNV,hoTen,soDienThoai,diaChi,viTriLamViec) VALUES(?,?,?,?,?)";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,nv.getMaNV());p.setString(2,nv.getHoTen());p.setString(3,nv.getSoDienThoai());p.setString(4,nv.getDiaChi());p.setString(5,nv.getViTriLamViec());return p.executeUpdate()==1;
        }
    }

    public boolean update(NhanVien nv) throws SQLException {
        String sql="UPDATE NhanVien SET hoTen=?,soDienThoai=?,diaChi=?,viTriLamViec=? WHERE maNV=?";
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,nv.getHoTen());p.setString(2,nv.getSoDienThoai());p.setString(3,nv.getDiaChi());p.setString(4,nv.getViTriLamViec());p.setString(5,nv.getMaNV());return p.executeUpdate()==1;
        }
    }

    public boolean delete(String id) throws SQLException {
        try(Connection c=ConnectDB.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM NhanVien WHERE maNV=?")){
            p.setString(1,id);return p.executeUpdate()==1;
        }
    }

    private NhanVien mapNhanVien(ResultSet r)throws SQLException{
        NhanVien nv=new NhanVien(r.getString("maNV"),r.getString("hoTen"),r.getString("soDienThoai"),r.getString("diaChi"),r.getString("viTriLamViec"));
        nv.setTenDangNhap(r.getString("tenDangNhap"));return nv;
    }
}
