package ru.pnppk.mes.pojo.manufacture.plan.operation;

import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AdjustmentTask;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.ParallelTask;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.SequentialTask;

import java.time.LocalDateTime;
import java.util.Objects;

public class PlanOperation {

    private int number;                          //номер операции
    private Order order;                         //ссылка на заказ
    private LocalDateTime factStart;             //Фактический старт
    private LocalDateTime factFinish;            //Фактический финиш
    private ParallelTask parallelTask;
    private SequentialTask sequentialTask;
    private AdjustmentTask adjustmentTask = null;

    private boolean blank = false;                //наличие заготовки для операции

    private boolean parallelPlanToLeft = false;   //последовательное планирование по умолчанию

    //Для создания
    public PlanOperation(int number, Order order) {
        this.number = number;
        this.order = order;
    }

    //Для копии
    public PlanOperation(PlanOperation po, Order order) {
        this.number = po.getNumber();
        this.order = order;
        this.factStart = po.getFactStart();
        this.factFinish = po.getFactFinish();
        this.parallelTask = new ParallelTask(this, po.getParallelTask());
        this.sequentialTask = new SequentialTask(this, po.getSequentialTask());
        if (po.getAdjustmentTask() != null) {
            this.adjustmentTask = new AdjustmentTask(this, po.getAdjustmentTask());
        } else this.adjustmentTask = null;
        this.parallelPlanToLeft = po.isParallelPlanToLeft();
        this.blank = po.isBlank();
    }

    public ParallelTask getParallelTask() {
        return parallelTask;
    }

    public SequentialTask getSequentialTask() {
        return sequentialTask;
    }

    public AdjustmentTask getAdjustmentTask() {
        return adjustmentTask;
    }

    public void setAdjustmentTask(AdjustmentTask adjustmentTask) {
        this.adjustmentTask = adjustmentTask;
    }

    public LocalDateTime getFactStart() {
        return factStart;
    }

    public LocalDateTime getFactFinish() {
        return factFinish;
    }

    public void setFactStart(LocalDateTime factStart) {
        this.factStart = factStart;
    }

    public void setFactFinish(LocalDateTime factFinish) {
        this.factFinish = factFinish;
    }

    public void setParallelPlanToLeft(boolean parallelPlanToLeft) {
        this.parallelPlanToLeft = parallelPlanToLeft;
    }

    public boolean isParallelPlanToLeft() {
        return parallelPlanToLeft;
    }

    public int getNumber() {
        return number;
    }


    public Order getOrder() {
        return order;
    }

    public boolean isBlank() {
        return blank;
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
    }

    public void setParallelTask(ParallelTask parallelTask) {
        this.parallelTask = parallelTask;
    }

    public void setSequentialTask(SequentialTask sequentialTask) {
        this.sequentialTask = sequentialTask;
    }

    public String toString() {
        return order.getNumber()+
                " - " + number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanOperation)) return false;
        PlanOperation that = (PlanOperation) o;
        return number == that.number &&
                order.equals(that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, order);
    }
}