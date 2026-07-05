package com.sdms.model;

public class Student {
    private String id, fullName, birthDate, gender, idCard, phone, email;
    private String university, faculty, className, address, roomId, status;

    public Student(String id, String fullName, String birthDate, String gender,
                   String idCard, String phone, String email, String university,
                   String faculty, String className, String address, String roomId, String status) {
        this.id=id; this.fullName=fullName; this.birthDate=birthDate; this.gender=gender;
        this.idCard=idCard; this.phone=phone; this.email=email; this.university=university;
        this.faculty=faculty; this.className=className; this.address=address;
        this.roomId=roomId; this.status=status;
    }

    public String getId()         { return id; }
    public String getFullName()   { return fullName; }
    public String getBirthDate()  { return birthDate; }
    public String getGender()     { return gender; }
    public String getIdCard()     { return idCard; }
    public String getPhone()      { return phone; }
    public String getEmail()      { return email; }
    public String getUniversity() { return university; }
    public String getFaculty()    { return faculty; }
    public String getClassName()  { return className; }
    public String getAddress()    { return address; }
    public String getRoomId()     { return roomId; }
    public String getStatus()     { return status; }

    public void setFullName(String v)   { this.fullName=v; }
    public void setPhone(String v)      { this.phone=v; }
    public void setEmail(String v)      { this.email=v; }
    public void setRoomId(String v)     { this.roomId=v; }
    public void setStatus(String v)     { this.status=v; }
    public void setFaculty(String v)    { this.faculty=v; }
    public void setClassName(String v)  { this.className=v; }
    public void setUniversity(String v) { this.university=v; }
    public void setAddress(String v)    { this.address=v; }
    public void setBirthDate(String v)  { this.birthDate=v; }
    public void setGender(String v)     { this.gender=v; }
    public void setIdCard(String v)     { this.idCard=v; }

    public Object[] toRow() {
        return new Object[]{id, fullName, roomId.isEmpty()?"—":roomId, faculty, phone, email, status};
    }
}