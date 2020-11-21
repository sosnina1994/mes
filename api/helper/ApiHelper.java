package ru.pnppk.mes.api.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.helper.Helper;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.order.OrderParameters;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.*;
import ru.pnppk.mes.pojo.tech.TechCard;
import ru.pnppk.mes.pojo.tech.TechOperation;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ApiHelper {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static ObjectMapper mapper = new ObjectMapper();

    public static void refreshDateOfTask(AbstractTask task, LocalDateTime time) {
        int remainder = task.getBatch();
        PlanOperation planOp = task.getPlanOperation();
        TechOperation techOp = planOp.getOrder().getTechOperationByNumber(planOp.getNumber());

        long duration;
        if (task instanceof AdjustmentTask) duration = ((AdjustmentTask) task).getDuration();
        else duration = techOp.getDuration();

        task.setPlanStart(time);
        task.setPlanFinish(time.plusSeconds(remainder * duration));
    }

    public static ObjectNode saveChart(Chromosome solution, List<Workplace> workplaces) {
        ObjectNode mainNode = mapper.createObjectNode();
        ArrayNode details = mainNode.putArray("details");
        ArrayNode tasks = mainNode.putArray("tasks");

        if (solution.getPlanOperations().size() == 0) return mainNode;

        List<Order> ordersList = solution.getPlanOperations().stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .collect(Collectors.toList());

        for (Order order : ordersList) {
            details.add(order.getTechCard().getCipher());
        }

        workplaces.sort(Comparator.comparingInt(Workplace::getId));

        for (Workplace w : workplaces) {
            if (w.getDailyTasks().size() == 0) continue;

            for (AbstractTask task : w.getDailyTasks()) {
                ObjectNode taskNode = tasks.addObject();
                taskNode.put("dateStart", DATE_TIME_FORMATTER.format(task.getPlanStart()));
                taskNode.put("dateFinish", DATE_TIME_FORMATTER.format(task.getPlanFinish()));
                taskNode.put("color", task.getPlanOperation().getOrder().getColor());
                taskNode.put("cipher", task.getPlanOperation().getOrder().getTechCard().getCipher());
                taskNode.put("workplace", w.getInvNumber());
                taskNode.put("number", task.getPlanOperation().getNumber());
                taskNode.put("batchSize", task.getType() == TaskType.ADJUSTMENT ? 1 : task.getBatch());
            }
        }
        return mainNode;
    }

    public static void getGlobalPlan() throws SQLException, IOException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("SELECT cipher, count, order_number, " +
                    "closing_date, params, tech_group, group_counter, plan_operations.* " +
                    "FROM orders, plan_operations " +
                    "WHERE orders.route_map_number = plan_operations.route_map_number " +
                    "ORDER BY route_map_number, number");
            ResultSet rs = pSt.executeQuery();

            String routeMapNumber = null;
            Order order = null;
            while (rs.next()) {
                String cipher = rs.getString("cipher");
                long orderNumber = rs.getLong("order_number");
                int batch = rs.getInt("count");
                LocalDate limitation = rs.getDate("closing_date").toLocalDate();
                int group = rs.getInt("tech_group");
                int groupCounter = rs.getInt("group_counter");

                TechCard techCard = MainListener.getTechCardArchive().getArchive().get(cipher).stream()
                        .filter(tCard -> tCard.getGroup() == group && tCard.getGroupCounter() == groupCounter)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("TechCard not found"));

                String color = colorGenerate();
                JsonNode areas = mapper.readTree(rs.getString("params"));

                int operationNumber = rs.getInt("number");
                Timestamp timestamp;

                LocalDateTime factStart = null;
                if ((timestamp = rs.getTimestamp("fact_start")) != null) {
                    factStart = timestamp.toLocalDateTime();
                }

                LocalDateTime factFinish = null;
                if ((timestamp = rs.getTimestamp("fact_finish")) != null) {
                    factFinish = timestamp.toLocalDateTime();
                }

                LocalDateTime parallelPlanStart = null;
                if ((timestamp = rs.getTimestamp("parallel_plan_start")) != null) {
                    parallelPlanStart = timestamp.toLocalDateTime();
                }

                LocalDateTime parallelPlanFinish = null;
                if ((timestamp = rs.getTimestamp("parallel_plan_finish")) != null) {
                    parallelPlanFinish = timestamp.toLocalDateTime();
                }

                LocalDateTime sequentialPlanStart = null;
                if ((timestamp = rs.getTimestamp("sequential_plan_start")) != null) {
                    sequentialPlanStart = timestamp.toLocalDateTime();
                }

                LocalDateTime sequentialPlanFinish = null;
                if ((timestamp = rs.getTimestamp("sequential_plan_finish")) != null) {
                    sequentialPlanFinish = timestamp.toLocalDateTime();
                }

                LocalDateTime adjustmentPlanStart = null;
                if ((timestamp = rs.getTimestamp("adjustment_plan_start")) != null) {
                    adjustmentPlanStart = timestamp.toLocalDateTime();
                }

                LocalDateTime adjustmentPlanFinish = null;
                if ((timestamp = rs.getTimestamp("adjustment_plan_finish")) != null) {
                    adjustmentPlanFinish = timestamp.toLocalDateTime();
                }

                int parallelBatch = rs.getInt("parallel_batch");
                int sequentialBatch = rs.getInt("sequential_batch");
                boolean blank = rs.getBoolean("blank");
                boolean parallelPlanToLeft = rs.getBoolean("parallel_plan_to_left");
                int workplaceId = rs.getInt("workplace_id");

                if (!rs.getString("route_map_number").equals(routeMapNumber)) {
                    routeMapNumber = rs.getString("route_map_number");

                    order = new Order(routeMapNumber, orderNumber, batch, limitation, techCard, color, group, groupCounter);

                    Iterator<Map.Entry<String, JsonNode>> it = areas.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> entry = it.next();
                        int areaNumber = Integer.parseInt(entry.getKey());
                        OrderParameters parameters = mapper.treeToValue(entry.getValue(), OrderParameters.class);
                        order.getParametersMap().put(new Area(areaNumber), parameters);
                        MainListener.getGlobalPlan().get(new Area(areaNumber)).add(order);
                    }
                }
                PlanOperation planOperation = new PlanOperation(operationNumber, order);
                planOperation.setFactStart(factStart);
                planOperation.setFactFinish(factFinish);
                planOperation.setBlank(blank);
                planOperation.setParallelPlanToLeft(parallelPlanToLeft);
                order.getPlanOperations().add(planOperation);

                ParallelTask parallelTask = new ParallelTask(planOperation, parallelBatch);
                parallelTask.setPlanStart(parallelPlanStart);
                parallelTask.setPlanFinish(parallelPlanFinish);
                planOperation.setParallelTask(parallelTask);

                SequentialTask sequentialTask = new SequentialTask(planOperation, sequentialBatch);
                sequentialTask.setPlanStart(sequentialPlanStart);
                sequentialTask.setPlanFinish(sequentialPlanFinish);
                planOperation.setSequentialTask(sequentialTask);

                long adjDuration = techCard.getTechOperationByNumber(operationNumber).getAdjDuration();
                AdjustmentTask adjustmentTask = null;
                if (adjDuration != -1) {
                    adjustmentTask = new AdjustmentTask(planOperation, adjDuration);
                    adjustmentTask.setPlanStart(adjustmentPlanStart);
                    adjustmentTask.setPlanFinish(adjustmentPlanFinish);
                    planOperation.setAdjustmentTask(adjustmentTask);
                }

                if (workplaceId != -1) {
                    List<Workplace> workplaces = MainListener.getAreasWorkplaces()
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());

                    for (Workplace wp : workplaces) {
                        if (wp.getId() == workplaceId) {
                            if (adjDuration != -1 && adjustmentPlanStart != null) wp.addTask(adjustmentTask);
                            if (parallelBatch != 0) wp.addTask(parallelTask);
                            if (sequentialBatch != 0) wp.addTask(sequentialTask);
                            break;
                        }
                    }
                }
            }
            rs.close();
            pSt.close();
        }
    }

    public static List<Workplace> prepareWorkplaceForAlgorithm(List<Workplace> areaWp, List<Order> orders, int areaNumber) {
        List<Order> copyOrder = Helper.copyOrders(orders);

        List<PlanOperation> planOperations = copyOrder.stream()
                .flatMap(order -> order.getPlanOperations().stream())
                .collect(Collectors.toList());

        List<Workplace> copyWorkplaces = Helper.copyWorkplace(areaWp, planOperations);

        // Список рабочих мест, доступных для планирования
        List<Workplace> availableWorkplaces = copyWorkplaces.stream()
                .filter(w -> w.getAreaNumber() == areaNumber && w.getShift() != 0)
                .collect(Collectors.toList());

        // Чистим список всех рабочих мест от всех, которые не содержатся в фильтрованном листе
        copyWorkplaces.removeIf(wp -> !availableWorkplaces.contains(wp));
        return copyWorkplaces;
    }

    public static List<PlanOperation> preparePlanForAlgorithm(List<Order> orders, int areaNumber) {
        List<Order> copyOrder = Helper.copyOrders(orders);
        // Фильтрация заказов, которые заблокированы для планирования
        copyOrder = copyOrder.stream()
                .filter(order -> order.getParametersMap().get(new Area(areaNumber)).isNeedToPlan())
                .collect(Collectors.toList());

        // Фильтрация плановых операций по наличию заготовки и изготовлению на текущем участке
        List<PlanOperation> operations = copyOrder.stream()
                .flatMap(order -> order.getPlanOperations().stream())
                .filter(planOp -> {
                    TechOperation techOp = planOp.getOrder().getTechOperationByNumber(planOp.getNumber());
                    int areaWp = -1;
                    if (techOp.getPermittedWorkplaces().size() > 0) {
                        areaWp = ApiHelper.getAreaNumberById(techOp.getPermittedWorkplaces().get(0));
                    }

                    return planOp.isBlank() && areaWp == areaNumber;
                })
                .filter(planOp -> planOp.getFactFinish() == null)
                .collect(Collectors.toList());

        // Вычистить все плановые операции, которые не содержатся в фильтрованном листе
        operations.stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .map(Order::getPlanOperations)
                .forEach(planOperations -> {
                    planOperations.removeIf(planOperation -> !operations.contains(planOperation));
                });

        // Получить рабочие места из хромосомы
        List<Workplace> workplaces = MainListener.getAreasChromosome().get(new Area(areaNumber)).getWorkplaces();

        // Операции, которые не могут быть выполнены на рабочих местах, выведенных из планирования (wp.getShift == 0)
        operations.stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .map(Order::getPlanOperations)
                .forEach(planOperations -> {

                    int indexFirstRemovedPlanOp = -1;
                    for (PlanOperation planOp : planOperations) {
                        TechOperation techOp = planOp.getOrder().getTechOperationByNumber(planOp.getNumber());

                        boolean a = workplaces.stream()
                                .map(Workplace::getId)
                                .anyMatch(integer -> techOp.getPermittedWorkplaces().contains(integer));

                        if (!a) {
                            indexFirstRemovedPlanOp = planOp.getOrder().getPlanOperations().indexOf(planOp);
                            break;
                        }
                    }

                    if (indexFirstRemovedPlanOp != -1) {
                        List<PlanOperation> subList = planOperations.subList(indexFirstRemovedPlanOp, planOperations.size());
                        planOperations.removeAll(subList);
                    }
                });

        // Вычистить и вернуть все плновые операции, которые не содержаться в фильтрованном листе
        return operations.stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .flatMap(order -> order.getPlanOperations().stream())
                .collect(Collectors.toList());
    }

    // Вызывается после UPDATEORDERS (иначе данные из Orders будут неактуальны)
    public static void updateWorkplaces(Chromosome bestSolution, List<Workplace> workplaces, List<Order> orders) {
        bestSolution.getWorkplaces().forEach(workplace -> {
            Workplace wp = workplaces.stream()
                    .filter(w -> w.equals(workplace))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);

            wp.getDailyTasks().clear();

            for (AbstractTask task : workplace.getDailyTasks()) {

                PlanOperation planOperation = orders.stream()
                        .flatMap(order -> order.getPlanOperations().stream())
                        .filter(po -> po.equals(task.getPlanOperation()))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));

                AbstractTask taskPlanOpFromOrder = null;
                if (task instanceof AdjustmentTask) {
                    taskPlanOpFromOrder = planOperation.getAdjustmentTask();
                } else if (task instanceof ParallelTask) {
                    taskPlanOpFromOrder = planOperation.getParallelTask();
                } else if (task instanceof SequentialTask){
                    taskPlanOpFromOrder = planOperation.getSequentialTask();
                }
                wp.getDailyTasks().add(taskPlanOpFromOrder);
            }
        });
    }

    public static void updateOrders(Chromosome bestSolution, List<Order> orders) throws SQLException {
        /*Данный метод вносит изменения в таблицу плановый операций,
        а также изменяет содержимое заказов (заменяет плановые операции на новые)*/

        //получение плановой операции из ORDERS для каждой операции в хромосоме
        bestSolution.getPlanOperations().forEach(po1 -> {
            PlanOperation po = orders.stream()
                    .flatMap(o -> o.getPlanOperations().stream())
                    .filter(po2 -> po2.equals(po1))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);


            int orderIdx = orders.indexOf(po.getOrder());
            int poIdx = orders.get(orderIdx).getPlanOperations().indexOf(po);
            PlanOperation newPo = new PlanOperation(po1, orders.get(orderIdx));

            orders.get(orderIdx).getPlanOperations().set(poIdx, newPo);
        });

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            List<PlanOperation> ops = orders.stream()
                    .flatMap(order -> order.getPlanOperations().stream())
                    .collect(Collectors.toList());

            for (PlanOperation po : ops) {
                PreparedStatement pSt = sql.prepareStatement("UPDATE plan_operations " +
                        "SET fact_start = ?, fact_finish = ?, parallel_plan_start = ?, parallel_plan_finish = ?, " +
                        "parallel_batch = ?, sequential_plan_start = ?, sequential_plan_finish = ?, " +
                        "sequential_batch = ?, adjustment_plan_start = ?, adjustment_plan_finish = ?, " +
                        "parallel_plan_to_left = ?, workplace_id = ? " +
                        "WHERE route_map_number = ? AND number = ?");

                if (po.getFactStart() != null) {
                    pSt.setTimestamp(1, Timestamp.valueOf(po.getFactStart()));
                } else pSt.setTimestamp(1, null);

                if (po.getFactFinish() != null) {
                    pSt.setTimestamp(2, Timestamp.valueOf(po.getFactFinish()));
                } else pSt.setTimestamp(2, null);

                if (po.getParallelTask().getPlanStart() != null) {
                    pSt.setTimestamp(3, Timestamp.valueOf(po.getParallelTask().getPlanStart()));
                } else pSt.setTimestamp(3, null);

                if (po.getParallelTask().getPlanFinish() != null) {
                    pSt.setTimestamp(4, Timestamp.valueOf(po.getParallelTask().getPlanFinish()));
                } else pSt.setTimestamp(4, null);

                pSt.setInt(5, po.getParallelTask().getBatch());

                if (po.getSequentialTask().getPlanStart() != null) {
                    pSt.setTimestamp(6, Timestamp.valueOf(po.getSequentialTask().getPlanStart()));
                } else pSt.setTimestamp(6, null);

                if (po.getSequentialTask().getPlanFinish() != null) {
                    pSt.setTimestamp(7, Timestamp.valueOf(po.getSequentialTask().getPlanFinish()));
                } else pSt.setTimestamp(7, null);

                pSt.setInt(8, po.getSequentialTask().getBatch());

                if (po.getAdjustmentTask() != null) {
                    if (po.getAdjustmentTask().getPlanStart() == null) {
                        pSt.setTimestamp(9, null);
                    } else {
                        pSt.setTimestamp(9, Timestamp.valueOf(po.getAdjustmentTask().getPlanStart()));
                    }

                    if (po.getAdjustmentTask().getPlanFinish() == null) {
                        pSt.setTimestamp(10, null);
                    } else {
                        pSt.setTimestamp(10, Timestamp.valueOf(po.getAdjustmentTask().getPlanFinish()));
                    }
                } else {
                    pSt.setTimestamp(9, null);
                    pSt.setTimestamp(10, null);
                }

                pSt.setBoolean(11, po.isParallelPlanToLeft());

                int workplaceId = -1;
                for (Workplace wp : bestSolution.getWorkplaces()) {
                    if (wp.getDailyTasks().contains(po.getAdjustmentTask()) ||
                            wp.getDailyTasks().contains(po.getParallelTask()) ||
                            wp.getDailyTasks().contains(po.getSequentialTask())) {
                        workplaceId = wp.getId();
                        break;
                    }
                }
                pSt.setInt(12, workplaceId);
                pSt.setString(13, po.getOrder().getRouteMapNumber());
                pSt.setInt(14, po.getNumber());
                pSt.executeUpdate();
                pSt.close();
            }
        }
    }

    public static int getAreaNumberById(int id) {
        Map<Area, List<Workplace>> areasWorkplaces = MainListener.getAreasWorkplaces();

        for (List <Workplace> value : areasWorkplaces.values()) {
            for (Workplace wp : value) {
                if (wp.getId() == id) return wp.getAreaNumber();
            }
        }
        return -1;
    }

    public static Order getOrderByRouteMapNumber(int areaNumber, String routeMapNumber) {
        List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));
        return orders.stream()
                .filter(order -> order.getRouteMapNumber().equals(routeMapNumber))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("There is no order with route map number " + routeMapNumber));
    }

    private static String colorGenerate() {
        return "#" + (int) (Math.random() * 89 + 10) + (int) (Math.random() * 89 + 10) + (int) (Math.random() * 89 + 10);
    }
}