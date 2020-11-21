package ru.pnppk.mes.pojo.manufacture.plan.operation.task;

import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

public class SequentialTask extends AbstractTask {
    private int batch;                           // размерность партии

    public SequentialTask(PlanOperation planOperation, int batch) {
        super(planOperation, TaskType.SEQUENCE);
        this.batch = batch;
    }

    public SequentialTask(PlanOperation po, SequentialTask task) {
        super(po, task);
        this.batch = task.getBatch();
    }

    public void decrease(int n) {
        this.batch = this.batch - n;
    }

    public void increase(int n) {
        this.batch = this.batch + n;
    }

    public int getBatch() {
        return batch;
    }

    public void setBatch(int batch) {
        this.batch = batch;
    }
}
