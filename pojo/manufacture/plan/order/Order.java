package ru.pnppk.mes.pojo.manufacture.plan.order;

import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.tech.TechCard;
import ru.pnppk.mes.pojo.tech.TechOperation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    private String routeMapNumber;              //номер маршрутной карты
    private long number;                         //номер заказа
    private int batch;                          // размерность партии
    private LocalDate limitation;           // дедлайн
    private TechCard techCard;                  // ссылка на техкарту
    private boolean orderCompletion;            // статус закрытия заказа
    private List<PlanOperation> planOperations = new ArrayList<>();
    private String color;
    private Map<Area, OrderParameters> parametersMap = new HashMap<>();
    private int group;                                                  //группа техкарт (R3)
    private int groupCounter;                                             //счетчик группы (R3)

    public Order(String routeMapNumber, long number, int batch, LocalDate limitation, TechCard techCard, String color, int group, int groupCounter) {
        this.routeMapNumber = routeMapNumber;
        this.number = number;
        this.batch = batch;
        this.limitation = limitation;
        this.techCard = techCard;
        this.color = color;
        this.group = group;
        this.groupCounter = groupCounter;
    }

    //для копии
    public Order (Order order) {
        this.routeMapNumber = order.getRouteMapNumber();
        this.number = order.getNumber();
        this.batch = order.getBatch();
        this.limitation = order.getLimitation();
        this.techCard = order.getTechCard();
        this.group = order.getGroup();
        this.groupCounter = order.getGroupCounter();
        this.color = order.getColor();

        for (PlanOperation planOperation : order.getPlanOperations()) {
            PlanOperation plOperation = new PlanOperation(planOperation, this);
            this.planOperations.add(plOperation);
        }

        for (Map.Entry<Area, OrderParameters> pair : order.getParametersMap().entrySet()) {
            this.parametersMap.put(pair.getKey(), new OrderParameters(pair.getValue()));
        }
    }

    //Получение техоперации по номеру плановой операции
    public TechOperation getTechOperationByNumber(int number) {
        for (TechOperation techOperation : techCard.getTechOperations()) {
            if (techOperation.getNumber() == number) return techOperation;
        }
        throw new IllegalArgumentException("TechOperation not found");
    }

    //Получение плановой операции по номеру техоперации
    public PlanOperation getPlanOperationByNumber(int number) {
        for (PlanOperation planOperation : planOperations) {
            if (planOperation.getNumber() == number) return planOperation;
        }
        throw new IllegalArgumentException("PlanOperation not found");
    }

    //получение предыдущей операции
    public PlanOperation getPrevPlanOperation(PlanOperation po) {
        int currentPlanOperationIndex = po.getOrder().getPlanOperations().indexOf(po);

        if (currentPlanOperationIndex != 0) {
            return po.getOrder().getPlanOperations().get(currentPlanOperationIndex - 1);
        } else {
            return null;
        }
    }

    //получение следующей плановой операции
    public PlanOperation getNextPlanOperation(PlanOperation po) {
        int currentPlanOperationIndex = po.getOrder().getPlanOperations().indexOf(po);

        if (currentPlanOperationIndex != po.getOrder().getPlanOperations().size() - 1) {
            return po.getOrder().getPlanOperations().get(currentPlanOperationIndex + 1);
        } else {
            return null;
        }
    }

    public void addOperation(PlanOperation planOperation) {
        planOperations.add(planOperation);
    }

    public void finishOrder() {
        orderCompletion = true;
    }

    public boolean isOrderCompletion() {
        return orderCompletion;
    }

    public long getNumber() {
        return number;
    }

    public int getBatch() {
        return batch;
    }

    public List<PlanOperation> getPlanOperations() {
        return planOperations;
    }

    public TechCard getTechCard() {
        return techCard;
    }

    public String getColor() {
        return color;
    }

    public LocalDate getLimitation() {
        return limitation;
    }

    public String getRouteMapNumber() {
        return routeMapNumber;
    }

    public void setRouteMapNumber(String routeMapNumber) {
        this.routeMapNumber = routeMapNumber;
    }

    public Map<Area, OrderParameters> getParametersMap() {
        return parametersMap;
    }

    public void setParametersMap(Map<Area, OrderParameters> parametersMap) {
        this.parametersMap = parametersMap;
    }

    public int getGroup() {
        return group;
    }

    public int getGroupCounter() {
        return groupCounter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        Order order = (Order) o;

        if (number != order.number) return false;
        return routeMapNumber.equals(order.routeMapNumber);
    }

    @Override
    public int hashCode() {
        int result = routeMapNumber.hashCode();
        result = 31 * result + (int) (number ^ (number >>> 32));
        return result;
    }
}