package ru.pnppk.mes.algorithm;

import ru.pnppk.mes.algorithm.helper.Helper;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

class Engine {
    //генетический алгоритм

    //Фитнесс-функция
    //https://www.researchgate.net/publication/242081421_A_JOB-SHOP_SCHEDULING_PROBLEM_JSSP_USING_GENETIC_ALGORITHM_GA
    static long fitnessFunction(List<Workplace> workplaces, List<PlanOperation> planOperations) {

        // Получение номера участка
        int areaNumber = workplaces.get(0).getAreaNumber();

        //ДЛЯ СРОЧНЫХ ЗАКАЗОВ
        List<AbstractTask> expressOperation = new ArrayList<>();

        //Добавить задачи
        workplaces.stream().flatMap(workplace -> workplace.getDailyTasks().stream()).filter(abstractTask -> {
            return abstractTask.getPlanOperation().getOrder().getParametersMap().get(new Area(areaNumber)).getPriority() > 0;
        }).forEach(expressOperation::add);


        long expressTime = 0L;
        int violationCount = 0;
        int countExpressOrder = 0;
        LocalDateTime startExpressOp = null;
        if (expressOperation.size() > 0) {
            //удалить задачи, которые закончены
            expressOperation.removeIf(abstractTask -> abstractTask.getPlanFinish() == null);

            //Счетчик нарушения приоритетности
            List<Integer> numbersPriority = expressOperation.stream()
                    .map(abstractTask -> abstractTask.getPlanOperation().getOrder())
                    .distinct()
                    .map(order -> order.getParametersMap().get(new Area(areaNumber)).getPriority())
                    .collect(Collectors.toList());

            countExpressOrder = numbersPriority.size();

            for (int i = 0; i < numbersPriority.size() - 1; i++) {
                if (numbersPriority.get(i) > numbersPriority.get(i + 1)) {
                    violationCount++;
                }
            }

            //сортировать по плановому старту
            expressOperation.sort(Comparator.comparing(AbstractTask::getPlanStart));
            //получить плановый старт первого заказа первой операции
            startExpressOp = expressOperation.get(0).getPlanStart();

            //сортировать по плановому финишу
            expressOperation.sort(Comparator.comparing(AbstractTask::getPlanFinish));
            //получить плановый финиш срочной операции
            LocalDateTime finishExpressOp = expressOperation.get(expressOperation.size() - 1).getPlanFinish();

            expressTime = Duration.between(startExpressOp, finishExpressOp).get(ChronoUnit.SECONDS) ;
        }

        List<AbstractTask> operations = new ArrayList<>();

        workplaces.stream()
                .map(Workplace::getDailyTasks)
                .forEach(operations::addAll);

        operations.removeIf(task -> task.getPlanFinish() == null);

        //определение планового старта первой операции
        operations.sort(Comparator.comparing(AbstractTask::getPlanStart));
        LocalDateTime planStart = operations.get(0).getPlanStart();

        //определение планового финиша последней операции
        operations.sort(Comparator.comparing(AbstractTask::getPlanFinish));
        LocalDateTime planFinish = operations.get(operations.size() - 1).getPlanFinish();

        //Длительность свего плана в секундах
        long durationOrders = Duration.between(planStart, planFinish).get(ChronoUnit.SECONDS);

        //разница в запуске срочных, относительно старта плана
        long lostTime = 0;
        if (startExpressOp != null) lostTime = Duration.between(planStart, startExpressOp).get(ChronoUnit.SECONDS);


        //добавочный коэфициент расчета fitFunction при превышении дедлайна (основан на количестве заказов, которые привышают дедлайн)
        long sumOrders = planOperations.stream().map(PlanOperation::getOrder).distinct().count();

        long orderCount = planOperations.stream().map(PlanOperation::getOrder).distinct().filter(order -> {
            PlanOperation lastPlanOp = order.getPlanOperations().get(order.getPlanOperations().size() - 1);
            if (lastPlanOp.getSequentialTask().getBatch() > 0) {
                return LocalDate.from(lastPlanOp.getSequentialTask().getPlanFinish()).isAfter(order.getLimitation());
            } else {
                return LocalDate.from(lastPlanOp.getParallelTask().getPlanFinish()).isAfter(order.getLimitation());
            }
        }).count();

        return 100 * lostTime + durationOrders * (1 + orderCount / sumOrders) + 10 * expressTime * (1 + (violationCount /(countExpressOrder + 1)));
    }

    //Скрещивание(PPX crossover)
    //статья: https://www.researchgate.net/publication/2753293_On_Permutation_Representations_for_Scheduling_Problems
    static List<Chromosome> crossover(Chromosome parent1, Chromosome parent2){

        LinkedList<Long> ordersNumbersParent1 = Helper.encode(parent1);
        LinkedList<Long> ordersNumbersParent2 = Helper.encode(parent2);

        //массив - цепь хромосом
        boolean[] permutation = new boolean[parent1.getPlanOperations().size()];
        Random random = new Random();
        for (int i = 0; i < permutation.length; i++) {
            permutation[i] = random.nextBoolean();
        }

        LinkedList<Long> ordersChild1 = cross(ordersNumbersParent1, ordersNumbersParent2, permutation);
        LinkedList<Long> ordersChild2 = cross(ordersNumbersParent2, ordersNumbersParent1, permutation);

        List<Order> ordersParent1 = parent1.getPlanOperations()
                .stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .collect(Collectors.toList());

        List<Order> ordersParent2 = parent2.getPlanOperations()
                .stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .collect(Collectors.toList());


        List<PlanOperation> planOperationsChild1 = Helper.decode(ordersChild1, Helper.copyOrders(ordersParent1));
        List<PlanOperation> planOperationsChild2 = Helper.decode(ordersChild2, Helper.copyOrders(ordersParent2));

        Chromosome child1 = new Chromosome(planOperationsChild1, Helper.copyWorkplace(parent1.getWorkplaces(), planOperationsChild1));
        Chromosome child2 = new Chromosome(planOperationsChild2, Helper.copyWorkplace(parent2.getWorkplaces(), planOperationsChild2));

        List<Chromosome> children = new ArrayList<>();
        children.add(child1);
        children.add(child2);
        return children;
    }

    // Метод скрещивает двух родителей для создания потомка
    private static LinkedList<Long> cross(LinkedList<Long> ordersNumbersParent1,
                                      LinkedList<Long> ordersNumbersParent2,
                                      boolean[] permutation) {

        ordersNumbersParent1 = new LinkedList<>(ordersNumbersParent1);
        ordersNumbersParent2 = new LinkedList<>(ordersNumbersParent2);

        LinkedList<Long> ordersChild = new LinkedList<>();

        for (boolean b : permutation) {

            long indexParent = -1;
            if (b) {
                indexParent = ordersNumbersParent1.pop();
                ordersChild.add(indexParent);
                ordersNumbersParent2.removeFirstOccurrence(indexParent);
            } else {
                indexParent = ordersNumbersParent2.pop();

                ordersChild.add(indexParent);
                ordersNumbersParent1.removeFirstOccurrence(indexParent);
            }
        }
        return ordersChild;
    }

    //Мутация
    static void mutation(Chromosome chromosome) {
        LinkedList<Long> ordersNumber = Helper.encode(chromosome);

        int pair = -1;
        pair = (int) (ordersNumber.size() * 0.05);

        for (int i = 0; i < pair; i++) {
            int firstIndex = -1;
            int secondIndex = -1;
            while (firstIndex == secondIndex) {
                firstIndex = (int) (Math.random() * ordersNumber.size());
                secondIndex = (int) (Math.random() * ordersNumber.size());
            }
            long indexElement = ordersNumber.get(firstIndex);
            ordersNumber.set(firstIndex, ordersNumber.get(secondIndex));
            ordersNumber.set(secondIndex, indexElement);
        }

        List<Order> orders = chromosome.getPlanOperations()
                .stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .collect(Collectors.toList());

        List<PlanOperation> planOperations = Helper.decode(ordersNumber, Helper.copyOrders(orders));
        chromosome.setPlanOperations(planOperations);
    }

    //Селекция
    static List<Integer> selection(List<Long> rates) {
        long totalRate = rates.stream().mapToLong(f -> f).sum();
        List<Double> probList = new ArrayList<>();
        rates.forEach(aLong -> probList.add((double) aLong / totalRate));

        List<Double> wheel = new ArrayList<>();
        wheel.add(probList.get(0));
        for (int i = 1; i < probList.size(); i++) {
            wheel.add(wheel.get(i - 1) + probList.get(i));
        }

        int indexSize = Manager.POPULATION_SIZE / 10;
        if (indexSize % 2 == 1) indexSize--;

        List<Integer> indexes = new ArrayList<>();
        while (indexes.size() < indexSize) {
            double p = Math.random();
            for (int j = wheel.size() - 1; j >= 0; j--) {
                if (p >= wheel.get(j)) {
                    if (!indexes.contains(j)) indexes.add(j + 1);
                    break;
                }
            }
        }
        return indexes;
    }
}
