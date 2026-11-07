package controller;

import dao.LichHen_Dao;
import dao.PhieuBanGiao_Dao;
import entity.LichHen;
import entity.PhieuTiepNhanVaSuaChua;
import entity.TrangThaiPhieu;
import gui.TrangChuLeTan;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Nạp số liệu tổng quan và danh sách công việc của lễ tân. */
public class TrangChuLeTanController {

    private final TrangChuLeTan view;
    private final PhieuBanGiao_Dao dao = new PhieuBanGiao_Dao();
    private final LichHen_Dao lichHenDao = new LichHen_Dao();
    private List<LichHen> lichHen = new ArrayList<>();
    /** Nhận biển số (hoặc SĐT khi lịch chưa gắn xe) để mở Tiếp nhận xe và tra cứu. */
    private Consumer<String> moTiepNhan = tuKhoa -> { };
    private List<Object[]> all = new ArrayList<>();
    private int loadVersion;
    private boolean loadErrorShown;

    public TrangChuLeTanController(TrangChuLeTan view) {
        this.view = view;

        for (int i = 0; i < view.getTableModel().getRowCount(); i++) {
            Object[] row = new Object[view.getTableModel().getColumnCount()];
            for (int j = 0; j < row.length; j++) {
                row[j] = view.getTableModel().getValueAt(i, j);
            }
            all.add(row);
        }

        initController();
        load();
    }

    private void initController() {
        view.getTxtTimKiem().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        view.getBtnLamMoi().addActionListener(e -> load());
        view.getTblLichHen().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.getTblLichHen().rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row >= 0 && row < lichHen.size()) {
                    LichHen lh = lichHen.get(row);
                    moTiepNhan.accept(lh.getXe() != null ? lh.getXe().getBienSo() : lh.getKhachHang().getSoDienThoai());
                }
            }
        });
    }

    public void setMoTiepNhan(Consumer<String> moTiepNhan) {
        this.moTiepNhan = moTiepNhan;
    }

    /** Tải lại số liệu, danh sách công việc và lịch hẹn hôm nay. */
    public void load() {
        final int version = ++loadVersion;
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.findDashboardJobs(), dao.findDashboardStats(), lichHenDao.findHomNay()};
            }

            @Override
            protected void done() {
                if (version != loadVersion) {
                    return;
                }
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked")
                    List<PhieuTiepNhanVaSuaChua> jobs = (List<PhieuTiepNhanVaSuaChua>) result[0];
                    all = thanhDong(jobs);
                    int[] stats = (int[]) result[1];
                    view.setSoLieu(stats[0], stats[1], stats[2], stats[3]);
                    @SuppressWarnings("unchecked")
                    List<LichHen> homNay = (List<LichHen>) result[2];
                    hienLichHen(homNay);
                    filter();
                    loadErrorShown = false;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showLoadError(ex);
                } catch (ExecutionException ex) {
                    showLoadError(ex.getCause() == null ? ex : ex.getCause());
                }
            }
        }.execute();
    }

    private static List<Object[]> thanhDong(List<PhieuTiepNhanVaSuaChua> dsPhieu) {
        List<Object[]> rows = new ArrayList<>();
        for (PhieuTiepNhanVaSuaChua p : dsPhieu) {
            boolean ready = QLBanGiaoXeController.duDieuKienBanGiao(p);
            int progress = QLBanGiaoXeController.tinhPhanTramHoanTat(p);
            String status;
            if (ready) {
                status = "Chờ bàn giao";
            } else if (p.getTrangThai() == TrangThaiPhieu.CHO_KIEM_TRA) {
                status = "Chờ tiếp nhận";
            } else if (progress == 100) {
                status = "Chờ xử lý hóa đơn";
            } else {
                status = "Đang sửa • " + progress + "%";
            }

            String action;
            if (ready) {
                action = "Bàn giao →";
            } else if (progress == 0) {
                action = "Hồ sơ xe";
            } else {
                action = "Xem phiếu";
            }
            rows.add(new Object[]{p.getMaPhieuTNSC(), p.getXe().getBienSo(),
                p.getXe().getKhachHang().getHoTen(), p.getTenXe(), status, action});
        }
        return rows;
    }

    private void hienLichHen(List<LichHen> homNay) {
        lichHen = homNay;
        DateTimeFormatter gio = DateTimeFormatter.ofPattern("HH:mm");
        List<Object[]> rows = new ArrayList<>();
        for (LichHen lh : homNay) {
            rows.add(new Object[]{lh.getThoiGianHen().format(gio), lh.getKhachHang().getHoTen(),
                lh.getKhachHang().getSoDienThoai(), lh.getXe() == null ? "—" : lh.getXe().toString(),
                lh.getGhiChu().isEmpty() ? "—" : lh.getGhiChu()});
        }
        view.setLichHen(rows);
    }

    private void showLoadError(Throwable error) {
        if (loadErrorShown) {
            return;
        }
        loadErrorShown = true;
        String message = error.getMessage() == null
                ? error.getClass().getSimpleName() : error.getMessage();
        JOptionPane.showMessageDialog(view,
                "Không thể tải dữ liệu trang chủ lễ tân: " + message,
                "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private void filter() {
        String query = view.getTxtTimKiem().getText().trim().toLowerCase(java.util.Locale.ROOT);
        List<Object[]> rows = new ArrayList<>();
        for (Object[] row : all) {
            boolean match = false;
            for (Object cell : row) {
                if (String.valueOf(cell).toLowerCase(java.util.Locale.ROOT).contains(query)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                rows.add(row);
            }
        }
        view.setDanhSach(rows);
    }
}
