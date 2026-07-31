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

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String      id;
    private String      studentId;
    private String      studentName;
    private String      roomId;
    private LocalDate   startDate;
    private LocalDate   endDate;
    private long        monthlyFee;
    private String      note;
    private Status      status;

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
        refreshStatus();
    }

    public void refreshStatus() {
        if (status == Status.TERMINATED || status == Status.PENDING) return;
        if (endDate != null && LocalDate.now().isAfter(endDate)) {
            status = Status.EXPIRED;
        } else {
            status = Status.ACTIVE;
        }
    }

    public long getDaysRemaining() {
        if (endDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return Math.max(0, days);
    }

    public long getDurationMonths() {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.MONTHS.between(startDate, endDate);
    }

    /**
     * Phần trăm thời gian đã sử dụng của hợp đồng (0–100).
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

    public String getStatusText() {
        return switch (status) {
            case ACTIVE     -> "Đang hiệu lực";
            case EXPIRED    -> "Đã hết hạn";
            case TERMINATED -> "Đã chấm dứt";
            case PENDING    -> "Chờ ký kết";
        };
    }

    public Object[] toRow() {
        return new Object[]{
            id, studentId, studentName, roomId,
            startDate != null ? startDate.format(FMT) : "—",
            endDate   != null ? endDate.format(FMT)   : "—",
            String.format("%,d đ", monthlyFee),
            getStatusText()
        };
    }

    public String      getId()          { return id; }
    public String      getStudentId()   { return studentId; }
    public String      getStudentName() { return studentName; }
    public String      getRoomId()      { return roomId; }
    public LocalDate   getStartDate()   { return startDate; }
    public LocalDate   getEndDate()     { return endDate; }
    public long        getMonthlyFee()  { return monthlyFee; }
    public String      getNote()        { return note; }
    public Status      getStatus()      { return status; }
    public String      getStartDateStr(){ return startDate != null ? startDate.format(FMT) : ""; }
    public String      getEndDateStr()  { return endDate   != null ? endDate.format(FMT)   : ""; }

    public void setStudentId(String v)   { this.studentId   = v; }
    public void setStudentName(String v) { this.studentName = v; }
    public void setRoomId(String v)      { this.roomId      = v; }
    public void setStartDate(LocalDate v){ this.startDate   = v; refreshStatus(); }
    public void setEndDate(LocalDate v)  { this.endDate     = v; refreshStatus(); }
    public void setMonthlyFee(long v)    { this.monthlyFee  = v; }
    public void setNote(String v)        { this.note        = v; }
    public void setStatus(Status v)      { this.status      = v; }

    public static LocalDate parseDate(String str) {
        try { return LocalDate.parse(str.trim(), FMT); } catch (Exception e) { return null; }
    }

    public static String nextId(String lastId) {
        try {
            int num = Integer.parseInt(lastId.replace("HĐ", "").trim());
            return String.format("HĐ%04d", num + 1);
        } catch (Exception e) { return "HĐ0001"; }
    }
}