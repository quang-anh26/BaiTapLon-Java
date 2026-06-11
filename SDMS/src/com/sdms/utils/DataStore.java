package com.sdms.utils;

import com.sdms.model.*;
import java.util.*;



public class DataStore {
    private static final List<Student>        students        = new ArrayList<>();
    private static final List<Room>           rooms           = new ArrayList<>();
    private static final List<Invoice>        invoices        = new ArrayList<>();
    private static final List<PendingAccount> pendingAccounts = new ArrayList<>();

    /** Tài khoản sinh viên đang chờ duyệt (từ form đăng ký) */
    public static class PendingAccount {
        public enum Status { PENDING, APPROVED, REJECTED }

        private final String id;
        private final String username;   // mã SV / tên đăng nhập
        private final String fullName;
        private final String phone;
        private final String dob;
        private final String cccd;
        private final String gender;
        private final String registeredAt;
        private Status status;
        private String note;

        public PendingAccount(String id, String username, String fullName,
                              String phone, String dob, String cccd,
                              String gender, String registeredAt) {
            this.id           = id;
            this.username     = username;
            this.fullName     = fullName;
            this.phone        = phone;
            this.dob          = dob;
            this.cccd         = cccd;
            this.gender       = gender;
            this.registeredAt = registeredAt;
            this.status       = Status.PENDING;
            this.note         = "";
        }

        public String getId()            { return id; }
        public String getUsername()      { return username; }
        public String getFullName()      { return fullName; }
        public String getPhone()         { return phone; }
        public String getDob()           { return dob; }
        public String getCccd()          { return cccd; }
        public String getGender()        { return gender; }
        public String getRegisteredAt()  { return registeredAt; }
        public Status getStatus()        { return status; }
        public String getNote()          { return note; }
        public void setStatus(Status s)  { this.status = s; }
        public void setNote(String n)    { this.note   = n; }

        public String getStatusText() {
            return switch (status) {
                case PENDING  -> "Chờ duyệt";
                case APPROVED -> "Đã duyệt";
                case REJECTED -> "Từ chối";
            };
        }

        public Object[] toRow() {
            return new Object[]{ id, username, fullName, gender, dob, phone, cccd, registeredAt, getStatusText() };
        }
    }

    static {
        // Dữ liệu được load từ SQL Server qua DatabaseService
        // Không có dữ liệu ảo (mock data) ở đây
    }

    public static List<Student> getStudents()         { return students; }
    public static List<Room>    getRooms()            { return rooms; }
    public static List<Invoice> getInvoices()         { return invoices; }
    public static List<PendingAccount> getPendingAccounts() { return pendingAccounts; }
    public static void addPendingAccount(PendingAccount a) { if (a != null) pendingAccounts.add(a); }
    public static long pendingCount() { return pendingAccounts.stream().filter(a -> a.getStatus()== PendingAccount.Status.PENDING).count(); }

    public static int   totalStudents()   { return students.size(); }
    public static int   totalRooms()      { return rooms.size(); }
    public static long  emptyRooms()      { return rooms.stream().filter(r -> r.getStatus()==Room.Status.AVAILABLE).count(); }
    public static long  activeStudents()  { return students.stream().filter(s -> "Đang ở".equals(s.getStatus())).count(); }
    public static long  monthRevenue()    { return invoices.stream().filter(Invoice::isPaid).mapToLong(Invoice::getTotal).sum(); }

    public static String nextStudentId()  {
        int max = students.stream().mapToInt(s -> {
            try { return Integer.parseInt(s.getId().replace("SV00","").trim()); } catch(Exception e){return 0;}
        }).max().orElse(1248);
        return String.format("SV%06d", max+1);
    }

    // ========== THÊM 2 PHƯƠNG THỨC CÒN THIẾU ==========
    
    // Thêm sinh viên mới
    public static void addStudent(Student student) {
        if (student != null) {
            students.add(student);
        }
    }
    
    // Xóa sinh viên
    public static void removeStudent(Student student) {
        if (student != null) {
            students.remove(student);
        }
    }
}