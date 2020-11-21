package ru.pnppk.mes.algorithm.helper;

import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AdjustmentTask;
import ru.pnppk.mes.pojo.tech.TechOperation;

import java.time.LocalDateTime;
import java.util.Comparator;

public class Dispenser {
    //метод по распределению списка плановых операций по рабочим местам
    public static void dispense(Chromosome chromosome) {
        for (PlanOperation po : chromosome.getPlanOperations()) {

            if (po.getFactFinish() != null) {
                continue;
            }
            TechOperation to = po.getOrder().getTechOperationByNumber(po.getNumber());

            chromosome.getWorkplaces().forEach(Workplace::fixWorkplaceAvailability);
            chromosome.getWorkplaces().sort(Comparator.comparing(Workplace::getAvailability));

            for (Workplace wp : chromosome.getWorkplaces()) {
                if (to.isWorkplacesPermitted(wp)) {
                    // запланирована ли задача на любом из рабочих мест
                    boolean check = chromosome.getWorkplaces()
                            .stream()
                            .map(workplace -> workplace.getDailyTasks())
                            .anyMatch(tasks -> tasks.contains(po.getParallelTask()) || tasks.contains(po.getSequentialTask()));


                    // если любой из тасков запланирован на любом рабочем место, но не на этом, то получить следующее рабочее место
                    if (check && !(wp.getDailyTasks().contains(po.getParallelTask()) || wp.getDailyTasks().contains(po.getSequentialTask()))) {
                        continue;
                    }

                    //получаем предыдущую операцию
                    PlanOperation prevOperation;
                    try {
                        int currentPlanOperationIndex = po.getOrder().getPlanOperations().indexOf(po);
                        prevOperation = po.getOrder().getPlanOperations().get(currentPlanOperationIndex - 1);
                    } catch (Exception e) {
                        prevOperation = null;
                    }

                    //Получение плановых стартов и фактических финишей на предыдущую операцию
                    //по параллельной или последовательной задаче
                    LocalDateTime[] prevOperationPlanDates = getPlanOperationPlanDates(prevOperation);
                    LocalDateTime prevOperationPlanStart = prevOperationPlanDates[0];
                    LocalDateTime prevOperationPlanFinish = prevOperationPlanDates[1];

                    //параллельная партия > 0 и Лист задач содержит параллельную задачу
                    if (po.getParallelTask().getBatch() > 0 && !wp.getDailyTasks().contains(po.getParallelTask())) {
                        // Если start не null, то и finish не null
                        if (prevOperationPlanStart != null) {
                            if (po.isParallelPlanToLeft()) {
                                //если планируем влево
                                //параллельное планирование задается в методе confirmWork()
                                long durationOp = po.getOrder().getTechOperationByNumber(po.getNumber()).getDuration();
                                long adjDuration = 0;
                                if (po.getAdjustmentTask() != null) {
                                    adjDuration = po.getAdjustmentTask().getDuration();
                                }
                                LocalDateTime planStart = prevOperationPlanFinish.minusSeconds(durationOp * po.getParallelTask().getBatch() + adjDuration);

                                if (wp.getAvailability().isBefore(planStart)) {
                                    wp.setAvailability(planStart);
                                }
                            } else {
                                //иначе планируев вправо
                                if (prevOperationPlanStart.isAfter(wp.getAvailability())) {
                                    wp.setAvailability(prevOperationPlanStart);
                                }
                            }
                        }

                        //проверка доступности рабочего места сменности
                        wp.fixWorkplaceAvailability();

                        //если плановая операция содержит наладку
                        if (po.getAdjustmentTask() != null) {
                            //проверка текущей доступности на 3 смену
                            wp.fixThirdShift();
                            addTaskToWorkplace(wp, po.getAdjustmentTask());
                        }
                        addTaskToWorkplace(wp, po.getParallelTask());
                    }

                    //плановый финиш предыдущей операции не ноль и после доступности рабочего места
                    if (prevOperationPlanFinish != null && prevOperationPlanFinish.isAfter(wp.getAvailability())) {
                        //если плановая операция содержит наладку, то планируем ее влево,
                        // иначе назначаем доступность текущего рабочего места по плановому финишу предыдущей операции
                        if (po.getAdjustmentTask() != null) {
                            LocalDateTime adjPlanStart = prevOperationPlanFinish.minusSeconds(po.getAdjustmentTask().getDuration());
                            if (adjPlanStart.isAfter(wp.getAvailability())) {
                                wp.setAvailability(adjPlanStart);
                            }
                        } else wp.setAvailability(prevOperationPlanFinish);
                    }

                    //проверка доступности рабочего места сменности
                    wp.fixWorkplaceAvailability();

                    //лист задач не содержит последовательных задач
                    if (!wp.getDailyTasks().contains(po.getSequentialTask())) {
                        // плановая операция содержит наладку
                        //лист задач не содержит наладочную задачу
                        if (po.getAdjustmentTask() != null && !wp.getDailyTasks().contains(po.getAdjustmentTask()) ) {
                            // проверка текущей доступности на 3 смену
                            wp.fixThirdShift();
                            addTaskToWorkplace(wp, po.getAdjustmentTask());
                        }
                        addTaskToWorkplace(wp, po.getSequentialTask());
                    }

                    //назначение доступности рабочего места по финишу текущей операции
                    LocalDateTime currentPlanOpFinish = po.getSequentialTask().getBatch() > 0 ?
                            po.getSequentialTask().getPlanFinish() :
                            po.getParallelTask().getPlanFinish();
                    if (wp.getAvailability().isAfter(currentPlanOpFinish)) {
                        wp.setAvailability(currentPlanOpFinish);
                    }
                    break;
                }
            }
        }
    }

    private static LocalDateTime[] getPlanOperationPlanDates(PlanOperation planOperation) {
        LocalDateTime[] dates = new LocalDateTime[]{null, null};

        if (planOperation != null) {
            if (planOperation.getParallelTask().getBatch() > 0) {
                dates[0] = planOperation.getParallelTask().getPlanStart();
            } else if (planOperation.getSequentialTask().getBatch() > 0) {
                dates[0] = planOperation.getSequentialTask().getPlanStart();
            } else dates[0] = planOperation.getFactFinish();

            if (planOperation.getSequentialTask().getBatch() > 0) {
                dates[1] = planOperation.getSequentialTask().getPlanFinish();
            } else if (planOperation.getParallelTask().getBatch() > 0) {
                dates[1] = planOperation.getParallelTask().getPlanFinish();
            } else dates[1] = planOperation.getFactFinish();
        }

        return dates;
    }

    private static void addTaskToWorkplace(Workplace wp, AbstractTask task) {
        PlanOperation po = task.getPlanOperation();
        TechOperation to = po.getOrder().getTechOperationByNumber(po.getNumber());

        long duration;
        if (task instanceof AdjustmentTask) {
            duration = ((AdjustmentTask) task).getDuration();
        } else {
            duration = to.getDuration();
        }

        wp.addTask(task);
        task.setPlanStart(wp.getAvailability());
        wp.setAvailability(wp.getAvailability().plusSeconds(duration * task.getBatch()));
        task.setPlanFinish(wp.getAvailability());
    }
}
