package controller;

import dao.NhanVien_Dao;
import entity.NhanVien;
import gui.QLNhanVien;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

/** Điều phối CRUD nhân viên, tìm kiếm và lọc theo vị trí/trạng thái. */
public class QLNhanVienController {
    private final QLNhanVien view;
    private final NhanVien_Dao dao = new NhanVien_Dao();
    private String selectedId;
    private int loadVersion;
    private boolean loadErrorShown;
    public QLNhanVienController(QLNhanVien view) {
        this.view=view;
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){load();}public void removeUpdate(DocumentEvent e){load();}public void changedUpdate(DocumentEvent e){load();}});
        view.getCboLocViTri().addActionListener(e->load());
        view.getBtnLuu().addActionListener(e->save());
        view.getBtnThemNhanVien().addActionListener(e->{selectedId=null;view.getTxtMaNV().setText("");});
        view.getBtnXacNhanXoa().addActionListener(e->delete());
        view.getBtnHuyXoa().addActionListener(e->{selectedId=null;view.anXacNhanXoa();});
        view.getBtnDongXoa().addActionListener(e->selectedId=null);
        view.getBtnHuy().addActionListener(e->{selectedId=null;view.getTxtMaNV().setText("");view.setCheDoSua(false);});
        view.getTblDanhSach().addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){int row=view.getTblDanhSach().rowAtPoint(e.getPoint());if(row<0)return;int icon=util.UIComponents.viTriBieuTuong(view.getTblDanhSach(),e,QLNhanVien.COT_THAO_TAC,3);if(icon>=0)act(row,icon);else if(e.getClickCount()==2)edit(row);}});
        load();
    }
    private void load(){final int version=++loadVersion;final String keyword=view.getTxtTimKiem().getText();final String position="Tất cả vị trí".equals(view.getCboLocViTri().getSelectedItem())?"":String.valueOf(view.getCboLocViTri().getSelectedItem());new SwingWorker<List<NhanVien>,Void>(){protected List<NhanVien> doInBackground()throws Exception{return dao.search(keyword,position);}protected void done(){if(version!=loadVersion)return;try{List<NhanVien> list=get();java.util.ArrayList<Object[]> rows=new java.util.ArrayList<>();for(NhanVien n:list)rows.add(new Object[]{n.getMaNV(),n.getHoTen(),n.getSoDienThoai(),n.getDiaChi(),n.getViTriLamViec(),n.getTenDangNhap()==null?"—":n.getTenDangNhap(),""});view.setDanhSach(rows);loadErrorShown=false;}catch(Exception ex){view.setDanhSach(java.util.Collections.emptyList());if(!loadErrorShown){loadErrorShown=true;JOptionPane.showMessageDialog(view,"Không thể tải danh sách nhân viên từ CSDL.\n"+rootMessage(ex),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);}}}}.execute();}
    private void act(int row,int icon){int modelRow=view.getTblDanhSach().convertRowIndexToModel(row);String id=String.valueOf(view.getTableModel().getValueAt(modelRow,0));if(icon==QLNhanVien.ICON_XEM){NhanVien n=find(id);if(n!=null)showEmployeeDetail(n);}else if(icon==QLNhanVien.ICON_SUA)edit(row);else{selectedId=id;view.hienXacNhanXoa(view.getTableModel().getValueAt(modelRow,1)+" • "+id,String.valueOf(view.getTableModel().getValueAt(modelRow,4)));}}
    private void showEmployeeDetail(NhanVien n){
        String[][] fields={{"Mã nhân viên:",n.getMaNV()},{"Họ tên:",n.getHoTen()},{"Số điện thoại:",n.getSoDienThoai()},
                {"Địa chỉ:",n.getDiaChi()},{"Vị trí:",n.getViTriLamViec()},{"Tài khoản:",n.getTenDangNhap()==null?"Chưa liên kết":n.getTenDangNhap()}};
        JPanel content=new JPanel(new java.awt.GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        content.setPreferredSize(new java.awt.Dimension(430,210));
        java.awt.Font font=UIManager.getFont("Label.font").deriveFont(15f);
        for(int i=0;i<fields.length;i++){
            java.awt.GridBagConstraints labelCell=new java.awt.GridBagConstraints();
            labelCell.gridx=0;labelCell.gridy=i;labelCell.weightx=0.34;labelCell.fill=java.awt.GridBagConstraints.HORIZONTAL;
            labelCell.anchor=java.awt.GridBagConstraints.NORTHWEST;labelCell.insets=new java.awt.Insets(5,2,5,16);
            JLabel label=new JLabel(fields[i][0]);label.setFont(font.deriveFont(java.awt.Font.BOLD));
            content.add(label,labelCell);
            java.awt.GridBagConstraints valueCell=new java.awt.GridBagConstraints();
            valueCell.gridx=1;valueCell.gridy=i;valueCell.weightx=0.66;valueCell.fill=java.awt.GridBagConstraints.HORIZONTAL;
            valueCell.anchor=java.awt.GridBagConstraints.NORTHWEST;valueCell.insets=new java.awt.Insets(5,0,5,2);
            JLabel value=new JLabel(fields[i][1]==null||fields[i][1].trim().isEmpty()?"—":fields[i][1]);value.setFont(font);
            content.add(value,valueCell);
        }
        JOptionPane.showMessageDialog(view,content,"Thông tin nhân viên",JOptionPane.INFORMATION_MESSAGE);
    }
    private NhanVien find(String id){try{return dao.findById(id);}catch(SQLException ex){JOptionPane.showMessageDialog(view,"Không thể tải nhân viên: "+ex.getMessage(),"Lỗi dữ liệu",JOptionPane.ERROR_MESSAGE);return null;}}
    private void edit(int row){int modelRow=view.getTblDanhSach().convertRowIndexToModel(row);selectedId=String.valueOf(view.getTableModel().getValueAt(modelRow,0));NhanVien n=find(selectedId);if(n!=null){view.dienForm(n);view.setCheDoSua(true);}}
    private void save(){
        boolean edit=view.getLblTieuDeFormText().startsWith("Sửa");
        String id=edit?view.getTxtMaNV().getText().trim():null;
        try{
            String name=view.getTxtHoTen().getText().trim();
            if(name.isEmpty()){view.hienLoiHoTen("Họ tên không được để trống.");return;}
            String phone=view.getTxtSoDienThoai().getText().trim();
            if(!phone.matches("0[0-9]{9}")){view.hienLoiSoDienThoai("SĐT phải gồm 10 chữ số và bắt đầu bằng 0.");return;}
            if(dao.existsSoDienThoai(phone,id)){view.hienLoiSoDienThoai("Số điện thoại đã được nhân viên khác sử dụng.");return;}
            if(!edit)id=dao.nextId();
            NhanVien n=new NhanVien(id,view.getTxtHoTen().getText(),phone,view.getTxtDiaChi().getText(),String.valueOf(view.getCboViTri().getSelectedItem()));
            boolean ok=edit?dao.update(n):dao.insert(n);
            if(!ok)throw new SQLException("Không tìm thấy nhân viên để cập nhật.");
            view.anXacNhanXoa();view.xoaTrangForm();view.getTxtMaNV().setText("");view.setCheDoSua(false);selectedId=null;load();
        }catch(IllegalArgumentException ex){if(view.getTxtHoTen().getText().trim().isEmpty())view.hienLoiHoTen(ex.getMessage());else view.hienLoiSoDienThoai(ex.getMessage());}
        catch(SQLException ex){JOptionPane.showMessageDialog(view,"Không thể lưu nhân viên. Hãy kiểm tra dữ liệu hoặc kết nối CSDL.\n"+ex.getMessage(),"Lỗi lưu dữ liệu",JOptionPane.ERROR_MESSAGE);}
    }
    private void delete(){if(selectedId==null)return;try{if(!dao.delete(selectedId))throw new SQLException("Không tìm thấy nhân viên.");view.anXacNhanXoa();selectedId=null;load();}catch(SQLException ex){JOptionPane.showMessageDialog(view,"Không thể xóa nhân viên đang được tham chiếu bởi dữ liệu khác.\n"+ex.getMessage(),"Không thể xóa",JOptionPane.WARNING_MESSAGE);}}
    private static String rootMessage(Exception ex){Throwable cause=ex;while(cause.getCause()!=null)cause=cause.getCause();String message=cause.getMessage();return message==null?ex.getMessage():message;}
}
