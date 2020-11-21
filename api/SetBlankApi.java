package ru.pnppk.mes.api;

import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.tech.TechOperation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/blank")
public class SetBlankApi extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        boolean force = Boolean.parseBoolean(req.getParameter("force"));

        PlanOperation planOp = null;
        TechOperation techOp;
        PlanOperation prevPlanOp;

        try {
            List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));
            Order currentOrder = orders.stream()
                    .filter(order -> order.getRouteMapNumber().equals(routeMapNumber))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Order not found"));

            for (PlanOperation po : currentOrder.getPlanOperations()) {
                if (!po.isBlank()) {
                    planOp = po;
                    break;
                }
            }

            if (planOp != null) {
                techOp = planOp.getOrder().getTechOperationByNumber(planOp.getNumber());
                prevPlanOp = planOp.getOrder().getPrevPlanOperation(planOp);
            } else throw new ServletException("Can't find plan operation.");

            if (!force) {
                int areaWp = -1;
                if (techOp.getPermittedWorkplaces().size() > 0) {
                    areaWp = ApiHelper.getAreaNumberById(techOp.getPermittedWorkplaces().get(0));
                }

                if (areaWp == areaNumber && (prevPlanOp == null || planOp.getParallelTask().getBatch() > 0)) {
                    setBlankForOperationsFromFirstToCurrentIncluded(planOp.getNumber(), currentOrder);
                    setBlankForOperationsFromCurrentToEnd(planOp.getNumber(), currentOrder);
                    writer.write("{\"code\":0}");

                } else writer.write("{\"code\":1}");
            } else {
                planOp = null;
                for (PlanOperation  po : currentOrder.getPlanOperations()) {
                    TechOperation to = po.getOrder().getTechOperationByNumber(po.getNumber());


                    int areaWp = -1;
                    if (to.getPermittedWorkplaces().size() > 0) {
                        areaWp = ApiHelper.getAreaNumberById(to.getPermittedWorkplaces().get(0));
                    }
                    if (areaWp == areaNumber && !po.isBlank()) {
                        planOp = po;
                        break;
                    }
                }

                if (planOp != null) {
                    setBlankForOperationsFromFirstToCurrentIncluded(planOp.getNumber(), currentOrder);
                    resetPlanOperationsBeforeCurrent(planOp.getNumber(), currentOrder);
                    updateOrderInDatabase(currentOrder);
                    setBlankForOperationsFromCurrentToEnd(planOp.getNumber(), currentOrder);
                    writer.write("{\"code\":0}");

                    // Добавить проверку на пользователя. Внесение данных по приходу заготовок доступно только для распреда
                    if (true) updateBlanksJournal(currentOrder, areaNumber);


                } else writer.write("{\"code\":2}");


            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void setBlankForOperationsFromFirstToCurrentIncluded(int operationNumber, Order order) throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            for (PlanOperation po : order.getPlanOperations()) {
                if (!po.isBlank()) {
                    po.setBlank(true);

                    PreparedStatement pSt = sql.prepareStatement("UPDATE plan_operations SET blank = true " +
                            "WHERE route_map_number = ? AND number = ?");
                    pSt.setString(1, order.getRouteMapNumber());
                    pSt.setInt(2, po.getNumber());
                    pSt.executeUpdate();
                    pSt.close();

                    if (po.getNumber() == operationNumber) break;
                }
            }
        }
    }

    private void setBlankForOperationsFromCurrentToEnd(int operationNumber, Order order) throws SQLException {

        TechOperation to = order.getTechOperationByNumber(operationNumber);
        // Номер участка, на котором выполняется текущая операция
        int areaNumber = -1;
        if (to.getPermittedWorkplaces().size() > 0) {
            areaNumber = ApiHelper.getAreaNumberById(to.getPermittedWorkplaces().get(0));
        }

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            for (PlanOperation po : order.getPlanOperations()) {
                if (po.getNumber() <= operationNumber) continue;

                TechOperation techOperation = po.getOrder().getTechOperationByNumber(po.getNumber());
                int areaWp = -1;
                if (techOperation.getPermittedWorkplaces().size() > 0)  {
                    areaWp = ApiHelper.getAreaNumberById(techOperation.getPermittedWorkplaces().get(0));
                }
                if (areaWp == areaNumber) {
                    po.setBlank(true);

                    PreparedStatement pSt = sql.prepareStatement("UPDATE plan_operations SET blank = true " +
                            "WHERE route_map_number = ? AND number = ?");
                    pSt.setString(1, order.getRouteMapNumber());
                    pSt.setInt(2, po.getNumber());
                    pSt.executeUpdate();
                    pSt.close();
                } else break;
            }
        }
    }

    private void resetPlanOperationsBeforeCurrent(int operationNumber, Order order) {
        LocalDateTime currentTime = LocalDateTime.now();

        for (PlanOperation po : order.getPlanOperations()) {
            if (po.getNumber() != operationNumber) {
                if (po.getFactStart() == null) po.setFactStart(currentTime);
                if (po.getFactFinish() == null) po.setFactFinish(currentTime);

                if (po.getAdjustmentTask() != null) {
                    po.getAdjustmentTask().setPlanStart(null);
                    po.getAdjustmentTask().setPlanFinish(null);
                }

                if (po.getParallelTask().getBatch() > 0) {
                    po.getParallelTask().setPlanStart(null);
                    po.getParallelTask().setPlanFinish(null);
                    po.getParallelTask().setBatch(0);
                }

                if (po.getSequentialTask().getBatch() > 0) {
                    po.getSequentialTask().setPlanStart(null);
                    po.getSequentialTask().setPlanFinish(null);
                    po.getSequentialTask().setBatch(0);
                }
                po.setParallelPlanToLeft(false);
            } else if (po.getNumber() == operationNumber) {
                po.getParallelTask().setBatch(order.getBatch());
                po.getSequentialTask().setBatch(0);
                break;
            }
        }
    }

    private void updateOrderInDatabase(Order order) throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            for (PlanOperation po : order.getPlanOperations()) {
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
                pSt.setInt(12, -1);
                pSt.setString(13, po.getOrder().getRouteMapNumber());
                pSt.setInt(14, po.getNumber());
                pSt.executeUpdate();
                pSt.close();
            }
        }
    }

    private void updateBlanksJournal(Order order, int areaNumber) throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("INSERT INTO blanks_journal " +
                    "(date, cipher, name, route_map_number, order_number, " +
                    "count, area_number) VALUES (?, ?, ?, ?, ?, ?, ?)");

            pSt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pSt.setString(2, order.getTechCard().getCipher());
            pSt.setString(3, order.getTechCard().getName());
            pSt.setString(4, order.getRouteMapNumber());
            pSt.setLong(5, order.getNumber());
            pSt.setInt(6, order.getBatch());
            pSt.setInt(7, areaNumber);

            pSt.executeUpdate();
            pSt.close();
        }
    }
}



