package com.xjosiah.crawler.beans;

import java.io.File;
import java.net.URL;

public class Teacher {
    private String name;
    private byte[] pic;
    private String title;
    private String researchInstitute;
    private String specialism;
    private String course;
    private String email;
    private String office;
    private String phoneNum;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    private URL page;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResearchInstitute() {
        return researchInstitute;
    }

    public void setResearchInstitute(String researchInstitute) {
        this.researchInstitute = researchInstitute;
    }

    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public URL getPage() {
        return page;
    }

    public void setPage(URL page) {
        this.page = page;
    }

    @Override
    public String toString() {
        String teacherPic = pic == null ? "null" : (name + ".jpg");
        return "Teacher{" +
                "name='" + name + '\'' +
                ", pic='" + teacherPic + '\'' +
                ", title='" + title + '\'' +
                ", researchInstitute='" + researchInstitute + '\'' +
                ", specialism='" + specialism + '\'' +
                ", course='" + course + '\'' +
                ", email='" + email + '\'' +
                ", office='" + office + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", page=" + page +
                '}';
    }
}
