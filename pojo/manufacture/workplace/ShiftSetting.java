package ru.pnppk.mes.pojo.manufacture.workplace;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public class ShiftSetting {
    @JsonProperty("number")
    private int shiftNumber;
    @JsonProperty("start")
    private LocalTime startShift;
    @JsonProperty("finish")
    private LocalTime finishShift;

    public ShiftSetting() {
    }

    public ShiftSetting(int shiftNumber, LocalTime startShift, LocalTime finishShift) {
        this.shiftNumber = shiftNumber;
        this.startShift = startShift;
        this.finishShift = finishShift;
    }

    // Конструктор для копии
    public ShiftSetting(ShiftSetting shiftSetting) {
        this.shiftNumber = shiftSetting.getShiftNumber();
        this.startShift = LocalTime.of(shiftSetting.getStartShift().getHour(), shiftSetting.getStartShift().getMinute());
        this.finishShift = LocalTime.of(shiftSetting.getFinishShift().getHour(), shiftSetting.getFinishShift().getMinute());
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public LocalTime getStartShift() {
        return startShift;
    }

    public LocalTime getFinishShift() {
        return finishShift;
    }
}