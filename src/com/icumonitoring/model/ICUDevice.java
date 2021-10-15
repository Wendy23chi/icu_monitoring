package com.icumonitoring.model;

public class ICUDevice {

    private String name;
    private String idNumber;

    public ICUDevice() {
    }

    public ICUDevice(String name, String idNumber) {
        this.name = name;
        this.idNumber = idNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Override
    public String toString() {
        return "ICUDevice{" + "name=" + name + ", idNumber=" + idNumber + '}';
    }
}
