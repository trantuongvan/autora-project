package controller;

import dao.PhieuBanGiao_Dao;
import dao.NhanVien_Dao;
import entity.NhanVien;
import gui.InPhieuXuatXuongDialog;
import gui.QLBanGiaoXe;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Bàn giao chỉ được xác nhận khi dịch vụ hoàn tất và hóa đơn đã thanh toán/ghi nợ hợp lệ. */
public class QLBanGiaoXeController {
    private final QLBanGiaoXe view; private final PhieuBanGiao_Dao dao=new PhieuBanGiao_Dao(); private final NhanVien_Dao nhanVienDao=new NhanVien_Dao(); private String selected; private int loadVersion;
    public QLBanGiaoXeController(QLBanGiaoXe view){this.view=view;
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){load();}public void removeUpdate(DocumentEvent e){load();}public void changedUpdate(DocumentEvent e){load();}});
        view.getCboLoc().addActionListener(e->load());
        view.getTblDanhSach().getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&view.getTblDanhSach().getSelectedRow()>=0){int row=view.getTblDanhSach().convertRowIndexToModel(view.getTblDanhSach().getSelectedRow());selected=String.valueOf(view.getTableModel().getValueAt(row,0));detail(selected);}});
        view.getBtnXacNhan().addActionListener(e->confirm());view.getBtnXemPhieu().addActionListener(e->print());load();
    }
    private void load(){final int version=++loadVersion;final String query=view.getTxtTimKiem().getText();final String filter=String.valueOf(view.getCboLoc().getSelectedItem());new SwingWorker<List<Object[]>,Void>(){protected List<Object[]> doInBackground()throws Exception{return dao.findDanhSach(query,filter);}protected void done(){if(version!=loadVersion)return;try{List<Object[]> rows=get();view.setDanhSach(rows);view.anThongBaoThanhCong();view.setCanhBaoDieuKien(null);if(rows.isEmpty()){view.anChiTiet();selected=null;}else view.getTblDanhSach().setRowSelectionInterval(0,0);}catch(Exception ignored){}}}.execute();}
    private void detail(String id){try{Object[] d=dao.findChiTiet(id);if(d==null)return;QLBanGiaoXe.ChiTietBanGiao c=new QLBanGiaoXe.ChiTietBanGiao();c.xe=d[0]+" • "+d[1];c.chuXeVaPhieu=d[2]+" • "+id;c.nhanTienDo="Hoàn tất "+d[3]+"%";c.hoanTat100=((Number)d[3]).intValue()==100;c.tienCong=money(d[4]);c.phuTung=money(d[5]);c.giamGia="− "+money(d[6]);double taxRate=((Number)d[12]).doubleValue();c.nhanVAT=String.format(java.util.Locale.forLanguageTag("vi-VN"),"VAT (%.0f%%)",taxRate);c.vat=money(d[7]);c.tongThanhToan=money(d[8]);c.duDieuKien=(Boolean)d[10];c.daBanGiao=!String.valueOf(d[14]).isEmpty();c.thongBao=c.daBanGiao?"Phiếu đã bàn giao, có thể xem và in lại phiếu xuất xưởng.":c.duDieuKien?(d[9]+" đã xử lý. Đủ điều kiện bàn giao."):lyDoChuaDuDieuKien(d);view.hienChiTiet(c);}catch(SQLException ex){JOptionPane.showMessageDialog(view,"Không thể tải chi tiết phiếu: "+ex.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}}
    private String lyDoChuaDuDieuKien(Object[] d){int progress=((Number)d[3]).intValue();if(progress<100)return progress==0?"Chưa có hạng mục sửa chữa hoàn tất.":"Còn "+(100-progress)+"% hạng mục sửa chữa chưa hoàn tất.";String payment=String.valueOf(d[11]);if(String.valueOf(d[9]).isEmpty())return "Phiếu chưa có hóa đơn.";if("Đã hủy".equals(payment))return "Hóa đơn đã bị hủy.";if("Công nợ".equals(payment)&&!Boolean.TRUE.equals(d[17]))return "Khách hàng không được phép ghi nợ.";return "Hóa đơn chưa được thanh toán hoặc ghi nhận công nợ hợp lệ.";}
    private void confirm(){if(selected==null)return;int yes=JOptionPane.showConfirmDialog(view,"Xác nhận đã bàn giao xe cho khách?","Xác nhận bàn giao",JOptionPane.YES_NO_OPTION);if(yes!=JOptionPane.YES_OPTION)return;try{List<NhanVien> receptionists=nhanVienDao.findByViTri("Lễ tân");if(receptionists.isEmpty())throw new SQLException("Không có nhân viên lễ tân đang làm việc để ghi nhận bàn giao.");dao.xacNhan(selected,receptionists.get(0).getMaNV(),"");JOptionPane.showMessageDialog(view,"Đã ghi nhận bàn giao.","Thành công",JOptionPane.INFORMATION_MESSAGE);load();}catch(SQLException ex){JOptionPane.showMessageDialog(view,ex.getMessage(),"Không thể bàn giao",JOptionPane.WARNING_MESSAGE);}}
    private void print(){if(selected==null)return;try{Object[] d=dao.findChiTiet(selected);if(d==null)return;InPhieuXuatXuongDialog.ThongTinPhieu p=new InPhieuXuatXuongDialog.ThongTinPhieu();p.maPhieuTNSC=selected;p.maPhieuBG=String.valueOf(d[14]).isEmpty()?"Chưa xác nhận bàn giao":String.valueOf(d[14]);p.bienSo=String.valueOf(d[0]);p.xe=String.valueOf(d[1]);p.tenKhach=String.valueOf(d[2]);p.soDienThoai=String.valueOf(d[13]);p.tongThanhToan=money(d[8]);p.trangThaiHoaDon=String.valueOf(d[11]);java.sql.Timestamp deliveredAt=(java.sql.Timestamp)d[15];p.ngayBanGiao=deliveredAt==null?"Chưa xác nhận bàn giao":deliveredAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));p.nhanVienBanGiao=d[16]==null?"":String.valueOf(d[16]);new InPhieuXuatXuongDialog(SwingUtilities.getWindowAncestor(view),p).setVisible(true);}catch(SQLException ex){JOptionPane.showMessageDialog(view,"Không thể tải thông tin phiếu: "+ex.getMessage());}}
    private static String money(Object n){return String.format(java.util.Locale.forLanguageTag("vi-VN"),"%,.0f đ",((Number)n).doubleValue()).replace(',', '.');}
}
