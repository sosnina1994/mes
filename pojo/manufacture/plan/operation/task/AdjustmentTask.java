package ru.pnppk.mes.pojo.manufacture.plan.operation.task;


import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

public class AdjustmentTask extends AbstractTask {
    private long duration;

    public AdjustmentTask(PlanOperation planOperation, long duration) {
        super(planOperation, TaskType.ADJUSTMENT);
        this.duration = duration;
    }

    public AdjustmentTask(PlanOperation po, AdjustmentTask task) {
        super(po, task);
        this.duration = task.getDuration();
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public void decrease(int n) {
        throw new IllegalArgumentException();
    }

    @Override
    public void increase(int n) {
        throw new IllegalArgumentException();
    }

    @Override
    public int getBatch() {
        return  1;
    }
}
