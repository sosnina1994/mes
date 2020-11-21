package ru.pnppk.mes.algorithm.pojo;

import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.SequentialTask;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class Chromosome {
    private List<PlanOperation> planOperations;                  //плановые операции всего заказа
    private List<Workplace> workplaces;                         //лист рабочих мест

    public Chromosome(List<PlanOperation> planOperation, List<Workplace> workplaces) {
        this.planOperations = planOperation;
        this.workplaces = workplaces;
    }

    public List<PlanOperation> getPlanOperations() {
        return planOperations;
    }

    public List<Workplace> getWorkplaces() {
        return workplaces;
    }

    public void setPlanOperations(List<PlanOperation> planOperations) {
        this.planOperations = planOperations;
    }

    public void setWorkplaces(List<Workplace> workplaces) {
        this.workplaces = workplaces;
    }

    public void resetWorkplaces() {
        for (Workplace workplace : workplaces) {
            workplace.getDailyTasks().removeIf(task -> {
                return task.getPlanOperation().getFactStart() == null ||
                        task.getPlanOperation().getFactFinish() != null ||
                        (task.getPlanStart() == null && task.getPlanFinish() == null) ||
                        (task.getPlanOperation().getParallelTask().getBatch() > 0 &&
                                task.getPlanOperation().getSequentialTask().getBatch() > 0 &&
                                task.getPlanOperation().getFactStart() != null &&
                                task instanceof SequentialTask);
            });

            LocalDateTime currentTime = LocalDateTime.now();

            if (workplace.getDailyTasks().size() == 0) {
                workplace.setAvailability(currentTime);
            } else {
                workplace.setAvailability(workplace.getDailyTasks().stream()
                        .map(AbstractTask::getPlanFinish)
                        .max(Comparator.naturalOrder()).get());
            }
        }
    }
}
