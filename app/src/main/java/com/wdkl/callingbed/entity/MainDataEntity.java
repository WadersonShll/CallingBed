package com.wdkl.callingbed.entity;

/**
 * Created by 胡博文 on 2017/9/5.
 * <p>
 * 病人的个人信息 （Waderson）
 */

public class MainDataEntity {

    private String hospital;
    private String departments;
    private String leftOneTitle;
    private String leftTwoTitle;
    private String leftThreeTitle;
    private String leftFourTitle;
    private String leftFiveTitle;
    private String leftOneColor;
    private String leftTwoColor;
    private String leftThreeColor;
    private String leftFourColor;
    private String leftFiveColor;
    private String leftOneContent;
    private String leftTwoContent;
    private String leftThreeContent;
    private String leftFourContent;
    private String leftFiveContent;
    private String inpatientNum;
    private String name;
    private String sex;
    private String ageNum;
    private String ageUnit;
    private String illness;
    private String admissionTime;
    private String responsDoctor;
    private String responsDoctorPic;
    private String responsNurse;
    private String responsNursePic;
    private String qrCodeSwitch;
    private String Code;
    private String nurseLevel;//护理等级 （add by Waderson 2017-10-31）

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getHospital() {
        return hospital == null ? "暂无" : hospital;
    }

    public void setDepartments(String departments) {
        this.departments = departments;
    }

    public String getDepartments() {
        return departments == null ? "暂无" : departments;
    }

    public void setLeftOneTitle(String leftOneTitle) {
        this.leftOneTitle = leftOneTitle;
    }

    public String getLeftOneTitle() {
        return leftOneTitle == null ? "暂无" : leftOneTitle;
    }

    public void setLeftTwoTitle(String leftTwoTitle) {
        this.leftTwoTitle = leftTwoTitle;
    }

    public String getLeftTwoTitle() {
        return leftTwoTitle == null ? "暂无" : leftTwoTitle;
    }

    public void setLeftThreeTitle(String leftThreeTitle) {
        this.leftThreeTitle = leftThreeTitle;
    }

    public String getLeftThreeTitle() {
        return leftThreeTitle == null ? "暂无" : leftThreeTitle;
    }

    public void setLeftFourTitle(String leftFourTitle) {
        this.leftFourTitle = leftFourTitle;
    }

    public String getLeftFourTitle() {
        return leftFourTitle == null ? "暂无" : leftFourTitle;
    }

    public void setLeftFiveTitle(String leftFiveTitle) {
        this.leftFiveTitle = leftFiveTitle;
    }

    public String getLeftFiveTitle() {
        return leftFiveTitle == null ? "暂无" : leftFiveTitle;
    }

    public void setLeftOneColor(String leftOneColor) {
        this.leftOneColor = leftOneColor;
    }

    public String getLeftOneColor() {
        return leftOneColor == null ? "000000" : leftOneColor;
    }

    public void setLeftTwoColor(String leftTwoColor) {
        this.leftTwoColor = leftTwoColor;
    }

    public String getLeftTwoColor() {
        return leftTwoColor == null ? "000000" : leftTwoColor;
    }

    public void setLeftThreeColor(String leftThreeColor) {
        this.leftThreeColor = leftThreeColor;
    }

    public String getLeftThreeColor() {
        return leftThreeColor == null ? "000000" : leftThreeColor;
    }

    public void setLeftFourColor(String leftFourColor) {
        this.leftFourColor = leftFourColor;
    }

    public String getLeftFourColor() {
        return leftFourColor == null ? "000000" : leftFourColor;
    }

    public void setLeftFiveColor(String leftFiveColor) {
        this.leftFiveColor = leftFiveColor;
    }

    public String getLeftFiveColor() {
        return leftFiveColor == null ? "000000" : leftFiveColor;
    }

    public void setLeftOneContent(String leftOneContent) {
        this.leftOneContent = leftOneContent;
    }

    public String getLeftOneContent() {
        return leftOneContent == null ? "暂无" : leftOneContent;
    }

    public void setLeftTwoContent(String leftTwoContent) {
        this.leftTwoContent = leftTwoContent;
    }

    public String getLeftTwoContent() {
        return leftTwoContent == null ? "暂无" : leftTwoContent;
    }

    public void setLeftThreeContent(String leftThreeContent) {
        this.leftThreeContent = leftThreeContent;
    }

    public String getLeftThreeContent() {
        return leftThreeContent == null ? "暂无" : leftThreeContent;
    }

    public void setLeftFourContent(String leftFourContent) {
        this.leftFourContent = leftFourContent;
    }

    public String getLeftFourContent() {
        return leftFourContent == null ? "暂无" : leftFourContent;
    }

    public void setLeftFiveContent(String leftFiveContent) {
        this.leftFiveContent = leftFiveContent;
    }

    public String getLeftFiveContent() {
        return leftFiveContent == null ? "暂无" : leftFiveContent;
    }

    public void setInpatientNum(String inpatientNum) {
        this.inpatientNum = inpatientNum;
    }

    public String getInpatientNum() {
        return inpatientNum == null ? "暂无" : inpatientNum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name == null ? "暂无" : name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return sex == null ? "暂无" : sex;
    }

    public void setAgeNum(String ageNum) {
        this.ageNum = ageNum;
    }

    public String getAgeNum() {
        return ageNum == null ? "0" : ageNum;
    }

    public void setAgeUnit(String ageUnit) {
        this.ageUnit = ageUnit;
    }

    public String getAgeUnit() {
        return ageUnit == null ? "暂无" : ageUnit;
    }

    public void setIllness(String illness) {
        this.illness = illness;
    }

    public String getIllness() {
        return illness == null ? "暂无" : illness;
    }

    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }

    public String getAdmissionTime() {
        return admissionTime == null ? "0" : admissionTime;
    }

    public void setResponsDoctor(String responsDoctor) {
        this.responsDoctor = responsDoctor;
    }

    public String getResponsDoctor() {
        return responsDoctor == null ? "暂无" : responsDoctor;
    }

    public void setResponsDoctorPic(String responsDoctorPic) {
        this.responsDoctorPic = responsDoctorPic;
    }

    public String getResponsDoctorPic() {
        return responsDoctorPic == null ? "暂无" : responsDoctorPic;
    }

    public void setResponsNurse(String responsNurse) {
        this.responsNurse = responsNurse;
    }

    public String getResponsNurse() {
        return responsNurse == null ? "暂无" : responsNurse;
    }

    public void setQrCodeSwitch(String qrCodeSwitch) {
        this.qrCodeSwitch = qrCodeSwitch;
    }

    public String getQrCodeSwitch() {
        return qrCodeSwitch == null ? "暂无" : qrCodeSwitch;
    }

    public String getResponsNursePic() {
        return responsNursePic == null ? "" : responsNursePic;
    }

    public void setResponsNursePic(String responsNursePic) {
        this.responsNursePic = responsNursePic;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getCode() {
        return Code == null ? "0" : Code;
    }

    public String getNurseLevel() {
        return nurseLevel == null ? "0" : nurseLevel;
    }

    public void setNurseLevel(String nurseLevel) {
        this.nurseLevel = nurseLevel;
    }
}
