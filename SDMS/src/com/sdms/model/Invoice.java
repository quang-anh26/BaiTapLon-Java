package com.sdms.model;

public class Invoice {

    // ── Trạng thái hóa đơn ──────────────────────────────────────
    // UNPAID  : Chưa thanh toán      (sinh viên chưa thao tác gì)
    // PENDING : Chờ xử lí            (sinh viên đã ấn "Thanh toán", chờ admin duyệt)
    // PAID    : Đã thanh toán        (admin đã xác nhận thành công)
    public static final String STATUS_UNPAID  = "UNPAID";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID    = "PAID";

    private String id, studentId, studentName, roomId, month;
    private long roomFee, electricFee, waterFee;
    private String status; // một trong 3 hằng số STATUS_* ở trên

    /** Constructor đầy đủ dùng trạng thái dạng chuỗi (khuyến khích dùng). */
    public Invoice(String id, String studentId, String studentName, String roomId,
                   String month, long roomFee, long electricFee, long waterFee, String status) {
        this.id=id; this.studentId=studentId; this.studentName=studentName;
        this.roomId=roomId; this.month=month;
        this.roomFee=roomFee; this.electricFee=electricFee; this.waterFee=waterFee;
        this.status = normalize(status);
    }

    /**
     * Constructor tương thích ngược với code cũ dùng boolean paid.
     * true -> PAID, false -> UNPAID.
     */
    public Invoice(String id, String studentId, String studentName, String roomId,
                   String month, long roomFee, long electricFee, long waterFee, boolean paid) {
        this(id, studentId, studentName, roomId, month, roomFee, electricFee, waterFee,
             paid ? STATUS_PAID : STATUS_UNPAID);
    }

    private String normalize(String s) {
        if (s == null) return STATUS_UNPAID;
        String up = s.trim().toUpperCase();
        if (up.equals(STATUS_PAID) || up.equals(STATUS_PENDING) || up.equals(STATUS_UNPAID)) return up;
        return STATUS_UNPAID;
    }

    public String getId()           { return id; }
    public String getStudentId()    { return studentId; }
    public String getStudentName()  { return studentName; }
    public String getRoomId()       { return roomId; }
    public String getMonth()        { return month; }
    public long   getRoomFee()      { return roomFee; }
    public long   getElectricFee()  { return electricFee; }
    public long   getWaterFee()     { return waterFee; }
    public long   getTotal()        { return roomFee + electricFee + waterFee; }

    public String  getStatus()          { return status; }
    public void    setStatus(String v)  { this.status = normalize(v); }

    /** true nếu trạng thái là PAID (đã thanh toán). */
    public boolean isPaid()             { return STATUS_PAID.equals(status); }
    /** true nếu trạng thái là PENDING (đang chờ admin duyệt). */
    public boolean isPending()          { return STATUS_PENDING.equals(status); }
    /** true nếu trạng thái là UNPAID (chưa thanh toán). */
    public boolean isUnpaid()           { return STATUS_UNPAID.equals(status); }

    /**
     * Giữ tương thích ngược: setPaid(true/false) vẫn hoạt động như cũ,
     * nhưng nên dùng setStatus(...) để có đủ 3 trạng thái.
     */
    public void setPaid(boolean v) { this.status = v ? STATUS_PAID : STATUS_UNPAID; }

    /** Nhãn tiếng Việt hiển thị trên giao diện. */
    public String getStatusText() {
        switch (status) {
            case STATUS_PAID:    return "Đã thanh toán";
            case STATUS_PENDING: return "Chờ xử lí";
            default:             return "Chưa thanh toán";
        }
    }

    public Object[] toRow() {
        return new Object[]{id, studentName, roomId, month,
            String.format("%,d đ", roomFee),
            String.format("%,d đ", electricFee),
            String.format("%,d đ", waterFee),
            String.format("%,d đ", getTotal()),
            getStatusText()};
    }
}
