package controller;

import dao.PhieuBanGiao_Dao;
import gui.TrangChuLeTan;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Nạp số liệu tổng quan và danh sách công việc của lễ tân. */
public class TrangChuLeTanController {
    private final TrangChuLeTan view; private final PhieuBanGiao_Dao dao=new PhieuBanGiao_Dao(); private List<Object[]> all=new ArrayList<>(); private int loadVersion;
    public TrangChuLeTanController(TrangChuLeTan view){this.view=view;for(int i=0;i<view.getTableModel().getRowCount();i++){Object[] row=new Object[view.getTableModel().getColumnCount()];for(int j=0;j<row.length;j++)row[j]=view.getTableModel().getValueAt(i,j);all.add(row);}view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});view.getBtnLamMoi().addActionListener(e->load());load();}
    private void load(){final int version=++loadVersion;new SwingWorker<Object[],Void>(){protected Object[] doInBackground()throws Exception{return new Object[]{dao.findDashboardJobs(),dao.findDashboardStats()};}protected void done(){if(version!=loadVersion)return;try{Object[] result=get();@SuppressWarnings("unchecked") List<Object[]> jobs=(List<Object[]>)result[0];all=jobs;int[] stats=(int[])result[1];view.setSoLieu(stats[0],stats[1],stats[2],stats[3]);filter();}catch(Exception ignored){/* Mockup đã được nạp sẵn nếu CSDL chưa sẵn sàng. */}}}.execute();}
    private void filter(){String q=view.getTxtTimKiem().getText().trim().toLowerCase(java.util.Locale.ROOT);List<Object[]> rows=new ArrayList<>();for(Object[] r:all){boolean match=false;for(Object c:r)if(String.valueOf(c).toLowerCase(java.util.Locale.ROOT).contains(q)){match=true;break;}if(match)rows.add(r);}view.setDanhSach(rows);}
}
