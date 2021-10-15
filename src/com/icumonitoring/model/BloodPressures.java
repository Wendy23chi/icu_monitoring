package com.icumonitoring.model;

public class BloodPressures {

    private Integer systolic;
    private Integer diastolic;

    public BloodPressures(){

    }

    public BloodPressures(Integer systolic, Integer diastolic){
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    @Override
    public String toString() {
        super.toString();
        return "BloodPressures{" + "systolic=" + systolic + ", diastolic=" + diastolic + '}';
    }
}
