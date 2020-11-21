package ru.pnppk.mes.pojo.manufacture.plan.order;

public class OrderParameters {
    private int priority;
    private boolean needToPlan;

    public OrderParameters() {}

    // Конструктор для копии
    public OrderParameters(OrderParameters parameters) {
        this.priority = parameters.getPriority();
        this.needToPlan = parameters.isNeedToPlan();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isNeedToPlan() {
        return needToPlan;
    }

    public void setNeedToPlan(boolean needToPlan) {
        this.needToPlan = needToPlan;
    }
}
