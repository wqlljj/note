package com.cloudminds.register.repository.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import java.io.File;

/**
 * Created
 */

@Entity(tableName = "employee")
public class EmployeeEntity {
    /**
     * Id : 13
     * Live : Yes
     * Eid : 177
     * Name : 111
     * Ename :
     * DepartmentID : 222
     * Gender : Female
     * Birthday : 2018-01-02
     * Ed : 2018-01-04
     * Phone :
     * PhotoPath : ./uploadfile/2018-01-02/177_164618_bill.jpg
     * Position : PK
     * Email : trr@qq.com
     */
    @PrimaryKey
    private int Id = -1;
    private String Live = "Yes";
    private String Eid="";
    private String Name="";
    private String Ename="";
    private int DepartmentID = -1;
    private String Gender = "Male";
    private String Birthday = "";
    private String Ed = "";
    private String Phone="";
    private String PhotoPath;
    private String Position = "PK";
    private String Email="";
    @Ignore
    private File avatar;
    private String symbol = "FTE";

    public File getAvatar() {
        return avatar;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
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
        if (!"".equals(PhotoPath)) {
            this.PhotoPath = PhotoPath;
        } else {
            Log.w("EmployeeEntity", "PhotoPath is null = " + PhotoPath);
        }
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "Id=" + Id +
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
                ", avatar=" + avatar +
                ", symbol='" + symbol + '\'' +
                "}\n";
    }
}
