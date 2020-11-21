package ru.pnppk.mes.algorithm.helper;

import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

    // Метод преобразует лист плановых операций хромосомы родителя в последовательность номеров заказов
    public static LinkedList<Long> encode(Chromosome parent) {
        return parent.getPlanOperations()
                .stream()
                .map(planOperation -> planOperation.getOrder().getNumber())
                .collect(Collectors.toCollection(LinkedList::new));

    }

    // метод преобразует последовательность из номеров заказов в лист плановых операций
    public static List<PlanOperation> decode(LinkedList<Long> ordersChild, List<Order> orders) {

        List<PlanOperation> planOperationsChild = new ArrayList<>();    //Плановые операции потомка

        for (Long orderNumber : ordersChild) {
            Order order = orders.stream()
                    .filter(order1 -> order1.getNumber() == orderNumber)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ошибка в получении заказа по номеру"));


            PlanOperation po = order.getPlanOperations().get(0);
            planOperationsChild.add(po);
            order.getPlanOperations().remove(po);
        }
        planOperationsChild.forEach(po -> po.getOrder().getPlanOperations().add(po));

        return planOperationsChild;
    }

    //создание копии плана(копирование операций в констукторе заказов)
    public static List<Order> copyOrders(List<Order> orders) {
        List<Order> copiedOrders = new ArrayList<>();

        for (Order order : orders) {
            copiedOrders.add(new Order(order));
        }
        return copiedOrders;
    }

    //создание копии рабочих мест
    public static List<Workplace> copyWorkplace(List<Workplace> workplaces, List<PlanOperation> planOperations) {
        List<Workplace> copiedWorkplaces = new ArrayList<>();

        for (Workplace wp : workplaces) {
            copiedWorkplaces.add(new Workplace(wp, planOperations));
        }
        return copiedWorkplaces;
    }
}
