package com.wdkl.callingbed.entity;

import java.util.List;

/**
 * Created by 胡博文 on 2017/9/5.
 */

public class CostDataEntity {

    private String name;
    private String inpatientNum;
    private List<CostArray> CostArray;
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

    public void setCostArray(List<CostArray> CostArray) {
        this.CostArray = CostArray;
    }

    public List<CostArray> getCostArray() {
        return CostArray;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }

    public String getCode() {
        return Code == null ? "暂无" : Code;
    }

    public static class CostArray {

        private String date;
        private String costName;
        private String money;

        public void setDate(String date) {
            this.date = date;
        }

        public String getDate() {
            return date == null ? "暂无" : date;
        }

        public void setCostName(String costName) {
            this.costName = costName;
        }

        public String getCostName() {
            return costName == null ? "暂无" : costName;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getMoney() {
            return money == null ? "0" : money;
        }
    }
}
