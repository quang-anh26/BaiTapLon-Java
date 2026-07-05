package com.sdms.utils;

import com.sdms.model.*;
import com.sdms.utils.DataStore.PendingAccount;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Lớp dịch vụ dữ liệu — thay thế hoàn toàn DataStore (in-memory).
 * Bao gồm CRUD đầy đủ cho tất cả 11 bảng trong database quanly-ktx.
 *
 * Cách dùng: thay mọi lời gọi DataStore.xxx() bằng DatabaseService.xxx()
 */
public class DatabaseService {

    // ════════════════════════════════════════════════════════════
    // 1. ROOMS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ danh sách phòng */
    public static List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT id, name, type, floor, capacity, occupied FROM Rooms ORDER BY id";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Room(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("floor"),
                        rs.getInt("capacity"),
                        rs.getInt("occupied")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm phòng mới */
    public static boolean addRoom(Room r) {
        String sql = "INSERT INTO Rooms (id, name, type, floor, capacity, occupied) VALUES (?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getName());
            ps.setString(3, r.getType());
            ps.setInt(4, r.getFloor());
            ps.setInt(5, r.getCapacity());
            ps.setInt(6, r.getOccupied());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật số người đang ở của phòng */
    public static boolean updateRoomOccupied(String roomId, int occupied) {
        String sql = "UPDATE Rooms SET occupied=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, occupied);
            ps.setString(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật toàn bộ thông tin phòng */
    public static boolean updateRoom(Room r) {
        String sql = "UPDATE Rooms SET name=?, type=?, floor=?, capacity=?, occupied=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getType());
            ps.setInt(3, r.getFloor());
            ps.setInt(4, r.getCapacity());
            ps.setInt(5, r.getOccupied());
            ps.setString(6, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa phòng */
    public static boolean deleteRoom(String roomId) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Rooms WHERE id=?")) {
            ps.setString(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đếm tổng số phòng */
    public static int totalRooms() {
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Rooms")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Đếm số phòng còn trống (occupied < capacity) */
    public static long emptyRooms() {
        String sql = "SELECT COUNT(*) FROM Rooms WHERE occupied < capacity";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════
    // 2. STUDENTS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ danh sách sinh viên */
    public static List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, full_name, birth_date, gender, id_card, phone, email, "
                + "university, faculty, class_name, address, room_id, status "
                + "FROM Students ORDER BY id";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Tìm sinh viên theo ID */
    public static Student getStudentById(String id) {
        String sql = "SELECT * FROM Students WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapStudent(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Thêm sinh viên mới */
    public static boolean addStudent(Student s) {
        String sql = "INSERT INTO Students "
                + "(id, full_name, birth_date, gender, id_card, phone, email, "
                + "university, faculty, class_name, address, room_id, status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getFullName());
            ps.setString(3, s.getBirthDate());
            ps.setString(4, s.getGender());
            ps.setString(5, nullIfEmpty(s.getIdCard()));
            ps.setString(6, nullIfEmpty(s.getPhone()));
            ps.setString(7, nullIfEmpty(s.getEmail()));
            ps.setString(8, s.getUniversity());
            ps.setString(9, s.getFaculty());
            ps.setString(10, s.getClassName());
            ps.setString(11, s.getAddress());
            ps.setString(12, nullIfEmpty(s.getRoomId()));
            ps.setString(13, s.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật thông tin sinh viên */
    public static boolean updateStudent(Student s) {
        String sql = "UPDATE Students SET full_name=?, birth_date=?, gender=?, id_card=?, "
                + "phone=?, email=?, university=?, faculty=?, class_name=?, "
                + "address=?, room_id=?, status=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getBirthDate());
            ps.setString(3, s.getGender());
            ps.setString(4, nullIfEmpty(s.getIdCard()));
            ps.setString(5, nullIfEmpty(s.getPhone()));
            ps.setString(6, nullIfEmpty(s.getEmail()));
            ps.setString(7, s.getUniversity());
            ps.setString(8, s.getFaculty());
            ps.setString(9, s.getClassName());
            ps.setString(10, s.getAddress());
            ps.setString(11, nullIfEmpty(s.getRoomId()));
            ps.setString(12, s.getStatus());
            ps.setString(13, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa sinh viên theo đối tượng */
    public static boolean removeStudent(Student s) {
        return deleteStudent(s.getId());
    }

    /** Xóa sinh viên theo ID */
    public static boolean deleteStudent(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Students WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đếm tổng số sinh viên */
    public static int totalStudents() {
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Students")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Đếm số sinh viên đang ở */
    public static long activeStudents() {
        return countStudentsByStatus("Đang ở");
    }

    /** Đếm theo trạng thái */
    public static long countStudentsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Students WHERE status=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Sinh mã sinh viên tiếp theo (VD: SV001249 → SV001250) */
    public static String nextStudentId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(id,3,LEN(id)) AS INT)) FROM Students WHERE id LIKE 'SV%'";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("SV%06d", max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "SV001001";
    }

    // ════════════════════════════════════════════════════════════
    // 3. USERS
    // ════════════════════════════════════════════════════════════

    /**
     * Xác thực đăng nhập.
     * 
     * @param username       tên đăng nhập
     * @param sha256Password mật khẩu đã hash SHA-256 (64 ký tự hex)
     * @return User nếu đúng, null nếu sai
     */
    public static User authenticate(String username, String sha256Password) {
        String sql = "SELECT username, password, role, full_name, student_id "
                + "FROM Users WHERE username=? AND password=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha256Password);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Lấy user theo username */
    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Thêm user mới */
    public static boolean addUser(String username, String sha256Password,
            String role, String fullName, String studentId) {
        String sql = "INSERT INTO Users (username, password, role, full_name, student_id) VALUES (?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha256Password);
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.setString(5, nullIfEmpty(studentId));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đổi mật khẩu */
    public static boolean changePassword(String username, String newSha256Password) {
        String sql = "UPDATE Users SET password=? WHERE username=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newSha256Password);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa user */
    public static boolean deleteUser(String username) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Users WHERE username=?")) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════
    // 4. PENDING ACCOUNTS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ đơn đăng ký */
    public static List<PendingAccount> getAllPendingAccounts() {
        List<PendingAccount> list = new ArrayList<>();
        String sql = "SELECT id, username, full_name, phone, dob, cccd, gender, "
                + "registered_at, status, note, password FROM PendingAccounts ORDER BY registered_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PendingAccount pa = new PendingAccount(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("dob"),
                        rs.getString("cccd"),
                        rs.getString("gender"),
                        rs.getString("registered_at"),
                        rs.getString("password"));
                // Map trạng thái từ DB sang enum
                String dbStatus = rs.getString("status");
                if ("Đã duyệt".equals(dbStatus))
                    pa.setStatus(PendingAccount.Status.APPROVED);
                else if ("Từ chối".equals(dbStatus))
                    pa.setStatus(PendingAccount.Status.REJECTED);
                // mặc định PENDING
                pa.setNote(rs.getString("note"));
                list.add(pa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm đơn đăng ký mới */
    public static boolean addPendingAccount(PendingAccount pa) {
        String sql = "INSERT INTO PendingAccounts "
                + "(id, username, full_name, phone, dob, cccd, gender, registered_at, status, note, password) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pa.getId());
            ps.setString(2, pa.getUsername());
            ps.setString(3, pa.getFullName());
            ps.setString(4, pa.getPhone());
            ps.setString(5, pa.getDob());
            ps.setString(6, pa.getCccd());
            ps.setString(7, pa.getGender());
            ps.setString(8, pa.getRegisteredAt());
            ps.setString(9, pa.getStatusText());
            ps.setString(10, pa.getNote() == null ? "" : pa.getNote());
            ps.setString(11, pa.getPassword() != null ? pa.getPassword() : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /** Cập nhật trạng thái + ghi chú đơn đăng ký */
    public static boolean updatePendingAccountStatus(String id, String status, String note) {
        String sql = "UPDATE PendingAccounts SET status=?, note=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đếm số đơn đang chờ duyệt */
    public static long pendingCount() {
        String sql = "SELECT COUNT(*) FROM PendingAccounts WHERE status=N'Chờ duyệt'";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Sinh mã đơn đăng ký tiếp theo */
    public static String nextPendingId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(id,3,LEN(id)) AS INT)) FROM PendingAccounts WHERE id LIKE 'PA%'";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return String.format("PA%03d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PA001";
    }

    // ════════════════════════════════════════════════════════════
    // 5. CONTRACTS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ hợp đồng */
    public static List<Contract> getAllContracts() {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT id, student_id, student_name, room_id, start_date, end_date, "
                + "monthly_fee, note, status FROM Contracts ORDER BY id";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapContract(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy hợp đồng theo sinh viên */
    public static List<Contract> getContractsByStudent(String studentId) {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT * FROM Contracts WHERE student_id=? ORDER BY start_date DESC";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapContract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm hợp đồng mới */
    public static boolean addContract(Contract c) {
        String sql = "INSERT INTO Contracts "
                + "(id, student_id, student_name, room_id, start_date, end_date, "
                + "monthly_fee, note, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getStudentId());
            ps.setString(3, c.getStudentName());
            ps.setString(4, c.getRoomId());
            ps.setDate(5, Date.valueOf(c.getStartDate()));
            ps.setDate(6, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            ps.setLong(7, c.getMonthlyFee());
            ps.setString(8, c.getNote());
            ps.setString(9, c.getStatus().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật hợp đồng */
    public static boolean updateContract(Contract c) {
        String sql = "UPDATE Contracts SET student_id=?, student_name=?, room_id=?, "
                + "start_date=?, end_date=?, monthly_fee=?, note=?, status=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getStudentId());
            ps.setString(2, c.getStudentName());
            ps.setString(3, c.getRoomId());
            ps.setDate(4, Date.valueOf(c.getStartDate()));
            ps.setDate(5, c.getEndDate() != null ? Date.valueOf(c.getEndDate()) : null);
            ps.setLong(6, c.getMonthlyFee());
            ps.setString(7, c.getNote());
            ps.setString(8, c.getStatus().name());
            ps.setString(9, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa hợp đồng */
    public static boolean deleteContract(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Contracts WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sinh mã hợp đồng tiếp theo */
    public static String nextContractId() {
        // Dùng SUBSTRING từ vị trí 3 để bỏ 2 byte "HĐ" (ký tự Unicode)
        String sql = "SELECT TOP 1 id FROM Contracts ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return Contract.nextId(rs.getString("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "HĐ0001";
    }

    // ════════════════════════════════════════════════════════════
    // 6. UTILITIES
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ bản ghi điện nước */
    public static List<Utility> getAllUtilities() {
        List<Utility> list = new ArrayList<>();
        String sql = "SELECT id, room_id, month, electric_prev, electric_curr, "
                + "water_prev, water_curr, electric_unit_price, water_unit_price, "
                + "note, confirmed FROM Utilities ORDER BY month DESC, room_id";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapUtility(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy theo phòng và tháng */
    public static Utility getUtility(String roomId, String month) {
        String sql = "SELECT * FROM Utilities WHERE room_id=? AND month=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomId);
            ps.setString(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapUtility(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Thêm bản ghi điện nước */
    public static boolean addUtility(Utility u) {
        String sql = "INSERT INTO Utilities "
                + "(id, room_id, month, electric_prev, electric_curr, water_prev, water_curr, "
                + "electric_unit_price, water_unit_price, note, confirmed) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getRoomId());
            ps.setString(3, u.getMonth());
            ps.setDouble(4, u.getElectricPrev());
            ps.setDouble(5, u.getElectricCurr());
            ps.setDouble(6, u.getWaterPrev());
            ps.setDouble(7, u.getWaterCurr());
            ps.setLong(8, u.getElectricUnitPrice());
            ps.setLong(9, u.getWaterUnitPrice());
            ps.setString(10, u.getNote());
            ps.setBoolean(11, u.isConfirmed());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật bản ghi điện nước */
    public static boolean updateUtility(Utility u) {
        String sql = "UPDATE Utilities SET electric_prev=?, electric_curr=?, water_prev=?, "
                + "water_curr=?, electric_unit_price=?, water_unit_price=?, "
                + "note=?, confirmed=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, u.getElectricPrev());
            ps.setDouble(2, u.getElectricCurr());
            ps.setDouble(3, u.getWaterPrev());
            ps.setDouble(4, u.getWaterCurr());
            ps.setLong(5, u.getElectricUnitPrice());
            ps.setLong(6, u.getWaterUnitPrice());
            ps.setString(7, u.getNote());
            ps.setBoolean(8, u.isConfirmed());
            ps.setString(9, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa bản ghi điện nước */
    public static boolean deleteUtility(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Utilities WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sinh mã tiếp theo */
    public static String nextUtilityId() {
        String sql = "SELECT TOP 1 id FROM Utilities ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return Utility.nextId(rs.getString("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "UT0001";
    }

    // ════════════════════════════════════════════════════════════
    // 7. INVOICES
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ hóa đơn */
    public static List<Invoice> getAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT id, student_id, student_name, room_id, month, "
                + "room_fee, electric_fee, water_fee, paid FROM Invoices ORDER BY month DESC, id";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapInvoice(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy hóa đơn theo sinh viên */
    public static List<Invoice> getInvoicesByStudent(String studentId) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM Invoices WHERE student_id=? ORDER BY month DESC";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapInvoice(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm hóa đơn */
    public static boolean addInvoice(Invoice inv) {
        String sql = "INSERT INTO Invoices "
                + "(id, student_id, student_name, room_id, utility_id, month, "
                + "room_fee, electric_fee, water_fee, paid) VALUES (?,?,?,?,NULL,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, inv.getId());
            ps.setString(2, inv.getStudentId());
            ps.setString(3, inv.getStudentName());
            ps.setString(4, inv.getRoomId());
            ps.setString(5, inv.getMonth());
            ps.setLong(6, inv.getRoomFee());
            ps.setLong(7, inv.getElectricFee());
            ps.setLong(8, inv.getWaterFee());
            ps.setBoolean(9, inv.isPaid());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật tiền điện và nước vào hóa đơn đã tồn tại (dùng khi chốt chỉ số sau
     * khi đã có hóa đơn trước hạn)
     */
    public static boolean updateInvoiceFees(String invoiceId, long electricFee, long waterFee) {
        String sql = "UPDATE Invoices SET electric_fee=?, water_fee=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, electricFee);
            ps.setLong(2, waterFee);
            ps.setString(3, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đánh dấu hóa đơn đã thanh toán */
    public static boolean markInvoicePaid(String invoiceId, boolean paid) {
        String sql = "UPDATE Invoices SET paid=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, paid);
            ps.setString(2, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa hóa đơn */
    public static boolean deleteInvoice(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Invoices WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Tổng doanh thu từ hóa đơn đã trả (tháng hiện tại) */
    public static long monthRevenue() {
        String sql = "SELECT ISNULL(SUM(room_fee+electric_fee+water_fee),0) "
                + "FROM Invoices WHERE paid=1";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Tổng doanh thu theo tháng cụ thể */
    public static long monthRevenue(String month) {
        String sql = "SELECT ISNULL(SUM(room_fee+electric_fee+water_fee),0) "
                + "FROM Invoices WHERE paid=1 AND month=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Sinh mã hóa đơn tiếp theo */
    public static String nextInvoiceId() {
        String sql = "SELECT TOP 1 id FROM Invoices ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("id"); // VD: "HD006"
                int num = Integer.parseInt(last.replace("HD", ""));
                return String.format("HD%03d", num + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "HD001";
    }

    // ════════════════════════════════════════════════════════════
    // 8. VIOLATIONS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ vi phạm */
    public static List<Violation> getAllViolations() {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT id, student_id, student_name, room_id, vio_date, type, "
                + "description, severity, fine, handled_by, status, note "
                + "FROM Violations ORDER BY vio_date DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapViolation(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy vi phạm theo sinh viên */
    public static List<Violation> getViolationsByStudent(String studentId) {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT * FROM Violations WHERE student_id=? ORDER BY vio_date DESC";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapViolation(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm vi phạm */
    public static boolean addViolation(Violation v) {
        String sql = "INSERT INTO Violations "
                + "(id, student_id, student_name, room_id, vio_date, type, description, "
                + "severity, fine, handled_by, status, note) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getId());
            ps.setString(2, v.getStudentId());
            ps.setString(3, v.getStudentName());
            ps.setString(4, v.getRoomId());
            ps.setDate(5, Date.valueOf(v.getDate()));
            ps.setString(6, v.getType());
            ps.setString(7, v.getDescription());
            ps.setString(8, v.getSeverity().name());
            ps.setLong(9, v.getFine());
            ps.setString(10, v.getHandledBy());
            ps.setString(11, v.getStatus().name());
            ps.setString(12, v.getNote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật vi phạm */
    public static boolean updateViolation(Violation v) {
        String sql = "UPDATE Violations SET student_id=?, student_name=?, room_id=?, "
                + "vio_date=?, type=?, description=?, severity=?, fine=?, "
                + "handled_by=?, status=?, note=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getStudentId());
            ps.setString(2, v.getStudentName());
            ps.setString(3, v.getRoomId());
            ps.setDate(4, Date.valueOf(v.getDate()));
            ps.setString(5, v.getType());
            ps.setString(6, v.getDescription());
            ps.setString(7, v.getSeverity().name());
            ps.setLong(8, v.getFine());
            ps.setString(9, v.getHandledBy());
            ps.setString(10, v.getStatus().name());
            ps.setString(11, v.getNote());
            ps.setString(12, v.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa vi phạm */
    public static boolean deleteViolation(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Violations WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String username, String newHash) {
        String sql = "UPDATE Users SET password=? WHERE username=?";

        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sinh mã vi phạm tiếp theo */
    public static String nextViolationId() {
        String sql = "SELECT TOP 1 id FROM Violations ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return Violation.nextId(rs.getString("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "VP0001";
    }

    // ════════════════════════════════════════════════════════════
    // 9. NOTIFICATIONS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ thông báo */
    public static List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, title, content, type, target, target_id, "
                + "created_at, created_by, pinned FROM Notifications ORDER BY pinned DESC, created_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapNotification(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thông báo dành cho sinh viên (ALL + phòng của SV + cá nhân SV)
     * và đánh dấu trạng thái đã đọc theo studentId.
     */
    public static List<Notification> getNotificationsForStudent(String studentId, String roomId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, "
                + "CASE WHEN nr.student_id IS NOT NULL THEN 1 ELSE 0 END AS is_read "
                + "FROM Notifications n "
                + "LEFT JOIN NotificationReads nr ON nr.notification_id=n.id AND nr.student_id=? "
                + "WHERE n.target='ALL' "
                + "   OR (n.target='ROOM'    AND n.target_id=?) "
                + "   OR (n.target='STUDENT' AND n.target_id=?) "
                + "ORDER BY n.pinned DESC, n.created_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, roomId == null ? "" : roomId);
            ps.setString(3, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Notification n = mapNotification(rs);
                n.setRead(rs.getInt("is_read") == 1);
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Thêm thông báo */
    public static boolean addNotification(Notification n) {
        String sql = "INSERT INTO Notifications "
                + "(id, title, content, type, target, target_id, created_by, pinned) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, n.getId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getContent());
            ps.setString(4, n.getType().name());
            ps.setString(5, n.getTarget().name());
            ps.setString(6, n.getTargetId() == null ? "" : n.getTargetId());
            ps.setString(7, n.getCreatedBy());
            ps.setBoolean(8, n.isPinned());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Cập nhật thông báo */
    public static boolean updateNotification(Notification n) {
        String sql = "UPDATE Notifications SET title=?, content=?, type=?, target=?, "
                + "target_id=?, created_by=?, pinned=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getContent());
            ps.setString(3, n.getType().name());
            ps.setString(4, n.getTarget().name());
            ps.setString(5, n.getTargetId() == null ? "" : n.getTargetId());
            ps.setString(6, n.getCreatedBy());
            ps.setBoolean(7, n.isPinned());
            ps.setString(8, n.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa thông báo */
    public static boolean deleteNotification(String id) {
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM Notifications WHERE id=?")) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sinh mã thông báo tiếp theo */
    public static String nextNotificationId() {
        String sql = "SELECT TOP 1 id FROM Notifications ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return Notification.nextId(rs.getString("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "TB0001";
    }

    // ════════════════════════════════════════════════════════════
    // 10. NOTIFICATION READS
    // ════════════════════════════════════════════════════════════

    /** Đánh dấu sinh viên đã đọc một thông báo */
    public static boolean markNotificationRead(String notificationId, String studentId) {
        // INSERT OR IGNORE tương đương trong SQL Server dùng IF NOT EXISTS
        String sql = "IF NOT EXISTS ("
                + "  SELECT 1 FROM NotificationReads WHERE notification_id=? AND student_id=?"
                + ") INSERT INTO NotificationReads (notification_id, student_id) VALUES (?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, notificationId);
            ps.setString(2, studentId);
            ps.setString(3, notificationId);
            ps.setString(4, studentId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Đếm số thông báo chưa đọc của sinh viên */
    public static int countUnreadNotifications(String studentId, String roomId) {
        String sql = "SELECT COUNT(*) FROM Notifications n "
                + "WHERE (n.target='ALL' OR (n.target='ROOM' AND n.target_id=?) "
                + "   OR (n.target='STUDENT' AND n.target_id=?)) "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM NotificationReads nr "
                + "  WHERE nr.notification_id=n.id AND nr.student_id=?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomId == null ? "" : roomId);
            ps.setString(2, studentId);
            ps.setString(3, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════
    // 11. SETTINGS
    // ════════════════════════════════════════════════════════════

    /** Lấy toàn bộ settings dưới dạng Map<key, value> */
    public static Map<String, String> getAllSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT setting_key, setting_value FROM Settings ORDER BY setting_key";
        try (Connection con = DatabaseConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Lấy một setting theo key */
    public static String getSetting(String key) {
        String sql = "SELECT setting_value FROM Settings WHERE setting_key=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("setting_value");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Cập nhật một setting (UPSERT: update nếu key đã tồn tại, insert nếu chưa có)
     */
    public static boolean setSetting(String key, String value) {
        String update = "UPDATE Settings SET setting_value=? WHERE setting_key=?";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(update)) {
            ps.setString(1, value);
            ps.setString(2, key);
            if (ps.executeUpdate() > 0)
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Key chưa tồn tại trong bảng Settings -> insert mới
        String insert = "INSERT INTO Settings (setting_key, setting_value) VALUES (?,?)";
        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setString(1, key);
            ps.setString(2, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lưu nhiều setting cùng lúc (từ SettingsPanel) — UPSERT từng key, trả về true
     * nếu tất cả thành công
     */
    public static boolean saveSettings(Map<String, String> settings) {
        boolean allOk = true;
        for (Map.Entry<String, String> e : settings.entrySet()) {
            if (!setSetting(e.getKey(), e.getValue()))
                allOk = false;
        }
        return allOk;
    }

    // ════════════════════════════════════════════════════════════
    // PRIVATE MAPPER HELPERS
    // ════════════════════════════════════════════════════════════

    private static Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("id"),
                rs.getString("full_name"),
                rs.getString("birth_date"),
                rs.getString("gender"),
                emptyIfNull(rs.getString("id_card")),
                emptyIfNull(rs.getString("phone")),
                emptyIfNull(rs.getString("email")),
                emptyIfNull(rs.getString("university")),
                emptyIfNull(rs.getString("faculty")),
                emptyIfNull(rs.getString("class_name")),
                emptyIfNull(rs.getString("address")),
                emptyIfNull(rs.getString("room_id")),
                rs.getString("status"));
    }

    private static Room mapRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getInt("floor"),
                rs.getInt("capacity"),
                rs.getInt("occupied"));
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        User.Role role = "ADMIN".equals(roleStr) ? User.Role.ADMIN : User.Role.STUDENT;
        return new User(
                rs.getString("username"),
                rs.getString("password"),
                role,
                rs.getString("full_name"),
                emptyIfNull(rs.getString("student_id")));
    }

    private static Contract mapContract(ResultSet rs) throws SQLException {
        Date startSql = rs.getDate("start_date");
        Date endSql = rs.getDate("end_date");
        LocalDate startDate = startSql != null ? startSql.toLocalDate() : LocalDate.now();
        LocalDate endDate = endSql != null ? endSql.toLocalDate() : null;

        String statusStr = rs.getString("status");
        Contract.Status status;
        try {
            status = Contract.Status.valueOf(statusStr);
        } catch (Exception ex) {
            status = Contract.Status.PENDING;
        }

        return new Contract(
                rs.getString("id"),
                rs.getString("student_id"),
                rs.getString("student_name"),
                rs.getString("room_id"),
                startDate,
                endDate,
                rs.getLong("monthly_fee"),
                emptyIfNull(rs.getString("note")),
                status);
    }

    private static Utility mapUtility(ResultSet rs) throws SQLException {
        return new Utility(
                rs.getString("id"),
                rs.getString("room_id"),
                rs.getString("month"),
                rs.getDouble("electric_prev"),
                rs.getDouble("electric_curr"),
                rs.getDouble("water_prev"),
                rs.getDouble("water_curr"),
                rs.getLong("electric_unit_price"),
                rs.getLong("water_unit_price"),
                emptyIfNull(rs.getString("note")),
                rs.getBoolean("confirmed"));
    }

    private static Invoice mapInvoice(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getString("id"),
                rs.getString("student_id"),
                rs.getString("student_name"),
                rs.getString("room_id"),
                rs.getString("month"),
                rs.getLong("room_fee"),
                rs.getLong("electric_fee"),
                rs.getLong("water_fee"),
                rs.getBoolean("paid"));
    }

    private static Violation mapViolation(ResultSet rs) throws SQLException {
        Date vioSql = rs.getDate("vio_date");
        LocalDate vioDate = vioSql != null ? vioSql.toLocalDate() : LocalDate.now();

        Violation.Severity severity;
        try {
            severity = Violation.Severity.valueOf(rs.getString("severity"));
        } catch (Exception e) {
            severity = Violation.Severity.LOW;
        }

        Violation.Status status;
        try {
            status = Violation.Status.valueOf(rs.getString("status"));
        } catch (Exception e) {
            status = Violation.Status.PENDING;
        }

        return new Violation(
                rs.getString("id"),
                rs.getString("student_id"),
                rs.getString("student_name"),
                rs.getString("room_id"),
                vioDate,
                rs.getString("type"),
                emptyIfNull(rs.getString("description")),
                severity,
                rs.getLong("fine"),
                emptyIfNull(rs.getString("handled_by")),
                status,
                emptyIfNull(rs.getString("note")));
    }

    private static Notification mapNotification(ResultSet rs) throws SQLException {
        Notification.Type type;
        try {
            type = Notification.Type.valueOf(rs.getString("type"));
        } catch (Exception e) {
            type = Notification.Type.GENERAL;
        }

        Notification.Target target;
        try {
            target = Notification.Target.valueOf(rs.getString("target"));
        } catch (Exception e) {
            target = Notification.Target.ALL;
        }

        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        return new Notification(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("content"),
                type,
                target,
                emptyIfNull(rs.getString("target_id")),
                createdAt,
                rs.getString("created_by"),
                false, // read — sẽ được set riêng khi cần
                rs.getBoolean("pinned"));
    }

    // ════════════════════════════════════════════════════════════
    // UTILITY HELPERS
    // ════════════════════════════════════════════════════════════

    /**
     * Trả về null nếu chuỗi rỗng hoặc null — dùng cho các cột UNIQUE có thể NULL
     */
    private static String nullIfEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Trả về "" nếu chuỗi null — tránh NullPointerException */
    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }
}