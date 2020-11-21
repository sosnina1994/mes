package ru.pnppk.mes.pojo.journal;

import java.time.LocalDateTime;

public class LogEntry {
    private LocalDateTime date;
    private String cipher;
    private String name;
    private String routeMapNumber;
    private long orderNumber;
    private int batch;
    private int areaNumber;


    public LogEntry() {}

    public LogEntry(LocalDateTime date, String cipher, String name, String routeMapNumber, long orderNumber, int batch, int areaNumber) {
        this.date = date;
        this.cipher = cipher;
        this.name = name;
        this.routeMapNumber = routeMapNumber;
        this.orderNumber = orderNumber;
        this.batch = batch;
        this.areaNumber = areaNumber;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRouteMapNumber() {
        return routeMapNumber;
    }

    public void setRouteMapNumber(String routeMapNumber) {
        this.routeMapNumber = routeMapNumber;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }
}
