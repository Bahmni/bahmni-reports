package org.bahmni.reports.extension.icd10.bean;

public class ICDMap {
    String snomedCode;
    int age;
    String gender;
    String icdCodes;

    public String getSnomedCode() {
        return snomedCode;
    }

    public void setSnomedCode(String snomedCode) {
        this.snomedCode = snomedCode;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIcdCodes() {
        return icdCodes;
    }

    public void setIcdCodes(String icdCodes) {
        this.icdCodes = icdCodes;
    }
}
