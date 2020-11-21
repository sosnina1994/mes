package ru.pnppk.mes.pojo.manufacture;

import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Area {
    private int number;                                   //номер участка
    private List<Workplace> workplaces;                      //рабочие места участка

    public Area(int number, List<Workplace> workplaces) {
        this.number = number;
        this.workplaces = workplaces;
    }

    public Area(int number) {
        this.number = number;
    }

    public List<Workplace> getWorkplaces() {
        return workplaces;
    }

    public void setWorkplaces(List<Workplace> workplaces) {
        this.workplaces = workplaces;
    }

    public void resetArea() {
        for (Workplace workplace : workplaces) {
            workplace.setAvailability(LocalDateTime.parse("2020-01-29 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            workplace.getDailyTasks().clear();
        }
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area)) return false;

        Area area = (Area) o;

        return number == area.number;
    }

    @Override
    public int hashCode() {
        return number;
    }
}
