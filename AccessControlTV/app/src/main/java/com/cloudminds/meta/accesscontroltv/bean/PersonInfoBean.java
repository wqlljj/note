package com.cloudminds.meta.accesscontroltv.bean;

/**
 * Created by WQ on 2018/4/8.
 */

public class PersonInfoBean {

    /**
     * Id : 17
     * Live : Yes
     * Eid : 180
     * Name : 111
     * Ename :
     * DepartmentID : 6
     * Gender : Female
     * Birthday : 2018-01-03
     * Ed : 2018-01-02
     * Phone : 13901234567
     * PhotoPath : ./uploadfile/2018-01-03/180_155516_bill.jpg
     * Position : PK
     * Email : trr@qq.com
     */

    private boolean isAvailable=true;
    private int Id;
    private String Live;
    private String Eid;
    private String Name;
    private String Ename;
    private int DepartmentID;
    private String Gender;
    private String Birthday;
    private String Ed;
    private String Phone;
    private String PhotoPath;
    private String Position;
    private String Email;
    private String Company;
    private String Visitors;
    private String Purpose;
    private String Interviewer;
    private String PdfPath;
    private String Pubtime;
    private String category;
    private String WelcomeMsg;

    public String getWelcomeMsg() {
        return WelcomeMsg;
    }

    public void setWelcomeMsg(String welcomeMsg) {
        WelcomeMsg = welcomeMsg;
    }

    public PersonInfoBean(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public PersonInfoBean() {
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getVisitors() {
        return Visitors;
    }

    public void setVisitors(String visitors) {
        Visitors = visitors;
    }

    public String getPurpose() {
        return Purpose;
    }

    public void setPurpose(String purpose) {
        Purpose = purpose;
    }

    public String getInterviewer() {
        return Interviewer;
    }

    public void setInterviewer(String interviewer) {
        Interviewer = interviewer;
    }

    public String getPdfPath() {
        return PdfPath;
    }

    public void setPdfPath(String pdfPath) {
        PdfPath = pdfPath;
    }

    public String getPubtime() {
        return Pubtime;
    }

    public void setPubtime(String pubtime) {
        Pubtime = pubtime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getLive() {
        return Live;
    }

    public void setLive(String Live) {
        this.Live = Live;
    }

    public String getEid() {
        return Eid;
    }

    public void setEid(String Eid) {
        this.Eid = Eid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getEname() {
        return Ename;
    }

    public void setEname(String Ename) {
        this.Ename = Ename;
    }

    public int getDepartmentID() {
        return DepartmentID;
    }

    public void setDepartmentID(int DepartmentID) {
        this.DepartmentID = DepartmentID;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String Birthday) {
        this.Birthday = Birthday;
    }

    public String getEd() {
        return Ed;
    }

    public void setEd(String Ed) {
        this.Ed = Ed;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String Phone) {
        this.Phone = Phone;
    }

    public String getPhotoPath() {
        return PhotoPath;
    }

    public void setPhotoPath(String PhotoPath) {
        this.PhotoPath = PhotoPath;
    }

    public String getPosition() {
        return Position;
    }

    public void setPosition(String Position) {
        this.Position = Position;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    @Override
    public String toString() {
        return "PersonInfoBean{" +
                "isAvailable=" + isAvailable +
                ", Id=" + Id +
                ", Live='" + Live + '\'' +
                ", Eid='" + Eid + '\'' +
                ", Name='" + Name + '\'' +
                ", Ename='" + Ename + '\'' +
                ", DepartmentID=" + DepartmentID +
                ", Gender='" + Gender + '\'' +
                ", Birthday='" + Birthday + '\'' +
                ", Ed='" + Ed + '\'' +
                ", Phone='" + Phone + '\'' +
                ", PhotoPath='" + PhotoPath + '\'' +
                ", Position='" + Position + '\'' +
                ", Email='" + Email + '\'' +
                ", Company='" + Company + '\'' +
                ", Visitors='" + Visitors + '\'' +
                ", Purpose='" + Purpose + '\'' +
                ", Interviewer='" + Interviewer + '\'' +
                ", PdfPath='" + PdfPath + '\'' +
                ", Pubtime='" + Pubtime + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
