package ru.pnppk.mes.pojo.tech;


import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;

import java.util.ArrayList;
import java.util.List;


public class TechOperation {
    private int number;                                                 //номер Техоперации
    private String name;                                                //название операции
    private long duration;                                              //длительность операции в секундах
    private TechOperation nextOperation = null;                         //сл.операция
    private List<Integer> permittedWorkplaces = new ArrayList<>();      //набор рабочих мест для выполнения одной операции (по номеру ID)
    private long adjDuration;

    public TechOperation(int number, String name, long duration, long adjDuration) {
        this.number = number;
        this.name = name;
        this.duration = duration;
        this.adjDuration = adjDuration;
    }

    //проверка рабочих мест
    public boolean isWorkplacesPermitted(Workplace workplace) {
        return permittedWorkplaces.contains(workplace.getId());
    }

    public int getNumber() {
        return number;
    }

    public long getDuration() {
        return duration;
    }

    public TechOperation getNextOperation() {
        return nextOperation;
    }

    public void setNextOperation(TechOperation nextOperation) {
        this.nextOperation = nextOperation;
    }

    public List<Integer> getPermittedWorkplaces() {
        return permittedWorkplaces;
    }

    public long getAdjDuration() {
        return adjDuration;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}