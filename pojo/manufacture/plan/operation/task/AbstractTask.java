package ru.pnppk.mes.pojo.manufacture.plan.operation.task;

import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class AbstractTask {

    private LocalDateTime planStart;             //плановый старт
    private LocalDateTime planFinish;            //плановый финиш
    private PlanOperation planOperation;         //ссылка на плановую операцию
    private TaskType type;                       //тип задачи (наладка, паралельная или последовательная)

    public AbstractTask(PlanOperation planOperation, TaskType type) {
        this.planOperation = planOperation;
        this.type = type;
    }

    public AbstractTask(PlanOperation po, AbstractTask task) {
        this.planOperation = po;
        this.planStart = task.getPlanStart();
        this.planFinish = task.getPlanFinish();
        this.type = task.getType();
    }

    public LocalDateTime getPlanStart() {
        return planStart;
    }

    public LocalDateTime getPlanFinish() {
        return planFinish;
    }

    public PlanOperation getPlanOperation() {
        return planOperation;
    }

    public void setPlanStart(LocalDateTime planStart) {
        this.planStart = planStart;
    }

    public void setPlanFinish(LocalDateTime planFinish) {
        this.planFinish = planFinish;
    }

    public TaskType getType() {
        return type;
    }

    public abstract void decrease(int n);

    public abstract void increase(int n);

    public abstract int getBatch();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTask)) return false;
        AbstractTask that = (AbstractTask) o;
        return planOperation.equals(that.planOperation) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(planOperation, type);
    }
}
