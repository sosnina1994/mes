package ru.pnppk.mes.pojo.manufacture.workplace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/*Расшифровка сменности:     Смены 3/2/1
 0 - выведено из планирования
 1 - смена 1                 |00000001
 2 - смена 2                 |00000010
 3 - смена 1 и 2             |00000011
 4 - смена 3                 |00000100
 5 - смена 1 и 3             |00000101
 6 - смена 2 и 3             |00000110
 7 - смена 1, 2 и 3          |00000111
 */

public class Workplace {
    private int id;
    private int invNumber;                                       //номер рабочего места
    private String name;                                          //назавание оборудования
    private int shift;                                           //сменность
    private LocalDateTime availability;                          // доступность рабочего места
    @JsonIgnore
    private List<AbstractTask> dailyTasks = new ArrayList<>();   //задачи на рабочее место
    private int areaNumber;                                           //номер участка

    private WorkplaceParameters workplaceParameters;

    public Workplace(int id, int invNumber, String name, int shift, LocalDateTime availability, int areaNumber, WorkplaceParameters workplaceParameters) {
        this.id = id;
        this.invNumber = invNumber;
        this.name = name;
        this.shift = shift;
        this.availability = availability;
        this.areaNumber = areaNumber;
        this.workplaceParameters = workplaceParameters;
    }

    //Для копии листа рабочих мест
    public Workplace (Workplace workplace, List<PlanOperation> planOperations) {
        this.id = workplace.getId();
        this.invNumber = workplace.getInvNumber();
        this.name = workplace.getName();
        this.shift = workplace.getShift();
        this.availability = workplace.getAvailability();
        this.areaNumber = workplace.getAreaNumber();

        this.workplaceParameters = new WorkplaceParameters(workplace.getWorkplaceParameters());

        List<AbstractTask> abstractTasksList = new ArrayList<>();
        planOperations.forEach(planOperation -> {
            abstractTasksList.add(planOperation.getParallelTask());
            abstractTasksList.add(planOperation.getSequentialTask());

            if (planOperation.getAdjustmentTask() != null) abstractTasksList.add(planOperation.getAdjustmentTask());
        });
        this.dailyTasks = abstractTasksList.stream().collect(ArrayList::new, (list, task) -> {
            if (workplace.getDailyTasks().contains(task)) list.add(task);
        }, ArrayList::addAll);

    }

    //смещение доступности рабочих мест
    public void fixWorkplaceAvailability() {
        if (shift != 0) {
            long duration = getAvailabilityInSecond();
            int currentShift = getNumberCurrentShift(duration);
            boolean result = checkCurrentShift(currentShift);
            if (!result) {
                currentShift++;
                if (currentShift > workplaceParameters.getShiftSettings().size()) {
                    currentShift = 1;
                }
                fixAvailability(currentShift);
                fixWorkplaceAvailability();
            }
            fixAvailabilityForWeekends(duration);
            //это ДЕРЬМО выполняется много раз!!!
        }
    }

    //получить кол-во секунд с начала смены
    public long getAvailabilityInSecond() {
        return availability.getHour() * 60 * 60 +
                availability.getMinute() * 60 +
                availability.getSecond();
    }

    public int getNumberCurrentShift(long time) {
        for (ShiftSetting wm : workplaceParameters.getShiftSettings()) {
            long start = wm.getStartShift().toSecondOfDay();
            long finish = wm.getFinishShift().toSecondOfDay();

            if (start < finish) {
                if (time >= start && time < finish) return wm.getShiftNumber();
            } else {
                if ((time >= start && time < 86400) || (time >= 0 && time < finish)) return wm.getShiftNumber();
            }
        }
        return -1;
    }

    //проверка можно ли планировать
    public boolean checkCurrentShift(int currentShift) {
        boolean result = false;
        switch (currentShift) {
            case 1:
                result = shift == 1 || shift == 3 || shift == 5 || shift == 7;
                break;
            case 2:
                result = shift == 2 || shift == 6 || shift == 3 || shift == 7;
                break;
            case 3:
                result = shift == 4 || shift == 5 || shift == 6 || shift == 7;
                break;
        }
        return result;
    }

    //смещение доступности рабочего места
    public void fixAvailability(int currentShift) {
        ShiftSetting shiftSetting = workplaceParameters.getShiftSettings().stream()
                .filter(mode -> mode.getShiftNumber() == currentShift)
                .findFirst()
                .orElseThrow(NullPointerException::new);

        ShiftSetting prevShift = currentShift - 1 > 0 ?
                workplaceParameters.getShiftSettings().get(workplaceParameters.getShiftSettings().indexOf(shiftSetting) - 1) :
                workplaceParameters.getShiftSettings().get(workplaceParameters.getShiftSettings().size() - 1);
        if (prevShift.getStartShift().toSecondOfDay() < prevShift.getFinishShift().toSecondOfDay()) {
            availability = availability.withHour(shiftSetting.getStartShift().getHour())
                    .withMinute(shiftSetting.getStartShift().getMinute())
                    .withSecond(shiftSetting.getStartShift().getSecond());
        } else {
            if (availability.getHour() == prevShift.getFinishShift().getHour() &&
                    availability.getMinute() < prevShift.getFinishShift().getMinute()) {
                availability = availability.withHour(shiftSetting.getStartShift().getHour())
                        .withMinute(shiftSetting.getStartShift().getMinute())
                        .withSecond(shiftSetting.getStartShift().getSecond());
            } else {
                availability = availability.plusDays(1).withHour(shiftSetting.getStartShift().getHour())
                        .withMinute(shiftSetting.getStartShift().getMinute())
                        .withSecond(shiftSetting.getStartShift().getSecond());
            }
        }
    }

    //смещение доступности с выходных дней на рабочие
    public void fixAvailabilityForWeekends(long time) {
        ShiftSetting shiftSetting = workplaceParameters.getShiftSettings()
                .stream().filter(mode -> mode.getShiftNumber() == 1)
                .findFirst()
                .orElse(null);

        int dayOfWeek = availability.getDayOfWeek().getValue();

        if (shiftSetting != null) {
            if (dayOfWeek == 6 && time >= shiftSetting.getStartShift().toSecondOfDay()) {
                availability = availability.plusDays(2);
                availability = availability.withHour(shiftSetting.getStartShift().getHour())
                        .withMinute(shiftSetting.getStartShift().getMinute())
                        .withSecond(shiftSetting.getStartShift().getSecond());

            } else if (dayOfWeek == 7) {
                availability = availability.plusDays(1);
                availability = availability.withHour(shiftSetting.getStartShift().getHour())
                        .withMinute(shiftSetting.getStartShift().getMinute())
                        .withSecond(shiftSetting.getStartShift().getSecond());

            } else if (dayOfWeek == 1 && time < shiftSetting.getStartShift().toSecondOfDay()) {
                availability = availability.withHour(shiftSetting.getStartShift().getHour())
                        .withMinute(shiftSetting.getStartShift().getMinute())
                        .withSecond(shiftSetting.getStartShift().getSecond());
            }
        }
    }

    public void fixThirdShift() {
        ShiftSetting shiftSetting = workplaceParameters.getShiftSettings()
                .stream().filter(mode -> mode.getShiftNumber() == 1)
                .findFirst()
                .orElse(null);

        int currentShift = getNumberCurrentShift(getAvailabilityInSecond());
        if (currentShift == 3 && shiftSetting != null) {
            availability = availability.withHour(shiftSetting.getStartShift().getHour())
                    .withMinute(shiftSetting.getStartShift().getMinute())
                    .withSecond(shiftSetting.getStartShift().getSecond());
            fixWorkplaceAvailability();
        }
        //если оборудование работает только в 3 смену, то этот метод не сработает
    }


    public void addTask(AbstractTask task) {
        dailyTasks.add(task);
    }

    public List<AbstractTask> getDailyTasks() {
        return dailyTasks;
    }

    public LocalDateTime getAvailability() {
        return availability;
    }

    public void setAvailability(LocalDateTime availability) {
        this.availability = availability;
    }

    public int getInvNumber() {
        return invNumber;
    }

    public int getShift() {
        return shift;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public WorkplaceParameters getWorkplaceParameters() {
        return workplaceParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workplace)) return false;
        Workplace that = (Workplace) o;
        return invNumber == that.invNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(invNumber);
    }

    @Override
    public String toString() {
        return "Workplace{" +
                "id=" + id +
                ", invNumber=" + invNumber +
                ", availability=" + availability +
                '}';
    }
}