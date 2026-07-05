package com.sdms.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Model đại diện cho hợp đồng thuê phòng ký túc xá.
 * Mỗi sinh viên có thể có một hợp đồng đang hiệu lực tại một thời điểm.
 */
public class Contract {

    // ── Trạng thái hợp đồng ──────────────────────────────────────
    public enum Status {
        ACTIVE,      // Đang hiệu lực
        EXPIRED,     // Đã hết hạn
        TERMINATED,  // Đã chấm dứt sớm
        PENDING      // Chờ ký kết
    }

    // ── Định dạng ngày dd/MM/yyyy ─────────────────────────────────
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Các trường dữ liệu ────────────────────────────────────────
    private String      id;          // Mã hợp đồng, VD: "HĐ0001"
    private String      studentId;   // Mã sinh viên liên kết
    private String      studentName; // Tên sinh viên (lưu cache để hiển thị nhanh)
    private String      roomId;      // Mã phòng
    private LocalDate   startDate;   // Ngày bắt đầu
    private LocalDate   endDate;     // Ngày kết thúc
    private long        monthlyFee;  // Tiền phòng hàng tháng (đồng)
    private String      note;        // Ghi chú thêm
    private Status      status;      // Trạng thái hợp đồng

    /**
     * Constructor đầy đủ — dùng khi tạo hợp đồng mới hoặc load từ store.
     */
    public Contract(String id, String studentId, String studentName,
                    String roomId, LocalDate startDate, LocalDate endDate,
                    long monthlyFee, String note, Status status) {
        this.id          = id;
        this.studentId   = studentId;
        this.studentName = studentName;
        this.roomId      = roomId;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.monthlyFee  = monthlyFee;
        this.note        = note;
        this.status      = status;
        refreshStatus(); // Tự động cập nhật trạng thái dựa trên ngày
    }

    // ── Tự động cập nhật trạng thái theo ngày hiện tại ──────────
    /**
     * Tự động chuyển trạng thái sang EXPIRED nếu đã quá hạn.
     * Không ghi đè nếu đã là TERMINATED hoặc PENDING.
     */
    public void refreshStatus() {
        if (status == Status.TERMINATED || status == Status.PENDING) return;
        if (endDate != null && LocalDate.now().isAfter(endDate)) {
            status = Status.EXPIRED;
        } else {
            status = Status.ACTIVE;
        }
    }

    // ── Tính số ngày còn lại của hợp đồng ───────────────────────
    /**
     * @return số ngày còn lại cho đến ngày kết thúc, 0 nếu đã hết hạn
     */
    public long getDaysRemaining() {
        if (endDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return Math.max(0, days);
    }

    // ── Phần trăm thời gian đã sử dụng (0–100) ──────────────────
    /**
     * @return phần trăm thời gian đã sử dụng của hợp đồng.
     * VD: hợp đồng 12 tháng, đã đi được 3 tháng → 25%
     */
    public int getElapsedPercent() {
        if (startDate == null || endDate == null) return 0;
        long total   = ChronoUnit.DAYS.between(startDate, endDate);
        if (total <= 0) return 100;
        long elapsed = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        elapsed = Math.max(0, Math.min(elapsed, total));
        return (int) (elapsed * 100 / total);
    }

    // ── Tính tổng thời hạn hợp đồng tính theo tháng ─────────────
    /**
     * @return số tháng từ ngày bắt đầu đến ngày kết thúc
     */
    public long getDurationMonths() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    // ── Lấy trạng thái dạng chuỗi tiếng Việt ────────────────────
    public String getStatusText() {
        return switch (status) {
            case ACTIVE     -> "Đang hiệu lực";
            case EXPIRED    -> "Đã hết hạn";
            case TERMINATED -> "Đã chấm dứt";
            case PENDING    -> "Chờ ký kết";
        };
    }

    // ── Chuyển thành mảng Object[] để đưa vào JTable ─────────────
    /**
     * Thứ tự cột: Mã HĐ | Mã SV | Tên SV | Phòng | Ngày BĐ | Ngày KT | Tiền/tháng | Trạng thái
     */
    public Object[] toRow() {
        return new Object[]{
            id,
            studentId,
            studentName,
            roomId,
            startDate != null ? startDate.format(FMT) : "—",
            endDate   != null ? endDate.format(FMT)   : "—",
            String.format("%,d đ", monthlyFee),
            getStatusText()
        };
    }

    // ── Getters ───────────────────────────────────────────────────
    public String      getId()          { return id; }
    public String      getStudentId()   { return studentId; }
    public String      getStudentName() { return studentName; }
    public String      getRoomId()      { return roomId; }
    public LocalDate   getStartDate()   { return startDate; }
    public LocalDate   getEndDate()     { return endDate; }
    public long        getMonthlyFee()  { return monthlyFee; }
    public String      getNote()        { return note; }
    public Status      getStatus()      { return status; }

    /** Ngày bắt đầu dạng chuỗi dd/MM/yyyy */
    public String getStartDateStr() { return startDate != null ? startDate.format(FMT) : ""; }
    /** Ngày kết thúc dạng chuỗi dd/MM/yyyy */
    public String getEndDateStr()   { return endDate   != null ? endDate.format(FMT)   : ""; }

    // ── Setters ───────────────────────────────────────────────────
    public void setStudentId(String v)   { this.studentId   = v; }
    public void setStudentName(String v) { this.studentName = v; }
    public void setRoomId(String v)      { this.roomId      = v; }
    public void setStartDate(LocalDate v){ this.startDate   = v; refreshStatus(); }
    public void setEndDate(LocalDate v)  { this.endDate     = v; refreshStatus(); }
    public void setMonthlyFee(long v)    { this.monthlyFee  = v; }
    public void setNote(String v)        { this.note        = v; }
    public void setStatus(Status v)      { this.status      = v; }

    // ── Tiện ích parse ngày từ chuỗi dd/MM/yyyy ──────────────────
    /**
     * Chuyển chuỗi "dd/MM/yyyy" thành LocalDate.
     * @return LocalDate hoặc null nếu không parse được
     */
    public static LocalDate parseDate(String str) {
        try {
            return LocalDate.parse(str.trim(), FMT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sinh mã hợp đồng tiếp theo theo dạng "HĐ0001", "HĐ0002", ...
     * @param lastId mã hiện tại cuối cùng trong danh sách
     */
    public static String nextId(String lastId) {
        try {
            int num = Integer.parseInt(lastId.replace("HĐ", "").trim());
            return String.format("HĐ%04d", num + 1);
        } catch (Exception e) {
            return "HĐ0001";
        }
    }
}