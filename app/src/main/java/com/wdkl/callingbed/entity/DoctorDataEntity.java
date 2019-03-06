package com.wdkl.callingbed.entity;

import java.util.List;

/**
 * Created by 胡博文 on 2017/9/5.
 */

public class DoctorDataEntity {
    private String name;
    private String inpatientNum;
    private List<DoctorChargeArray> DoctorChargeArray;
    private String Code;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name == null ? "暂无" : name;
    }

    public void setInpatientNum(String inpatientNum) {
        this.inpatientNum = inpatientNum;
    }

    public String getInpatientNum() {
        return inpatientNum == null ? "0" : inpatientNum;
    }

    public void setDoctorChargeArray(List<DoctorChargeArray> DoctorChargeArray) {
        this.DoctorChargeArray = DoctorChargeArray;
    }

    public List<DoctorChargeArray> getDoctorChargeArray() {
        return DoctorChargeArray;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getCode() {
        return Code == null ? "0" : Code;
    }


    public class DoctorChargeArray {
        private String DoctorCharge;

        public void setDoctorCharge(String DoctorCharge) {
            this.DoctorCharge = DoctorCharge;
        }

        public String getDoctorCharge() {
            return DoctorCharge == null ? "暂无" : DoctorCharge;
        }

    }

}
