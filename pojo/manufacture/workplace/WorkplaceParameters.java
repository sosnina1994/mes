package ru.pnppk.mes.pojo.manufacture.workplace;

import java.util.ArrayList;
import java.util.List;

public class WorkplaceParameters {

    private List<ShiftSetting> shiftSettings = new ArrayList<>();

    public WorkplaceParameters() {}

    // Конструктор для копии
    public WorkplaceParameters(WorkplaceParameters parameters) {
        this.shiftSettings = new ArrayList<>();
        for (ShiftSetting shiftSetting : parameters.getShiftSettings()) {
            this.shiftSettings.add(new ShiftSetting(shiftSetting));
        }
    }

    public List<ShiftSetting> getShiftSettings() {
        return shiftSettings;
    }
}