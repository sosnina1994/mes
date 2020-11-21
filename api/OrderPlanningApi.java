package ru.pnppk.mes.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

@WebServlet("/ban")
public class OrderPlanningApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        boolean needToPlan = Boolean.parseBoolean(req.getParameter("needToPlan"));
        boolean force = Boolean.parseBoolean(req.getParameter("force"));

        boolean reschedule = false;

        try {
            Order order = ApiHelper.getOrderByRouteMapNumber(areaNumber, routeMapNumber);

            // Проверка начатых операций данного заказа
            boolean checkOperation = order.getPlanOperations().stream()
                    .anyMatch(po -> po.getFactStart() != null && po.getFactFinish() == null);

            if (!force) {
                // Нет начатых операций
                if (!checkOperation) {
                    // находим первую операцию на текущем участке
                    PlanOperation planOperation = order.getPlanOperations().stream()
                            .filter(po -> {
                                TechOperation techOperation = order.getTechOperationByNumber(po.getNumber());
                                int areaWp = ApiHelper.getAreaNumberById(techOperation.getPermittedWorkplaces().get(0));
                                return po.getFactStart() == null && areaWp == areaNumber;
                            }).findFirst()
                            .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));

                    order.getParametersMap().get(new Area(areaNumber)).setNeedToPlan(needToPlan);
                    setOrderStatusInDatabase(order, needToPlan, areaNumber);
                    resetPlanOperationsAfterCurrent(planOperation.getNumber(), order);
                    reschedule = true;
                }
            } else {
                PlanOperation planOperation = order.getPlanOperations().stream()
                        .filter(po -> {
                            TechOperation techOperation = order.getTechOperationByNumber(po.getNumber());
                            int areaWp = ApiHelper.getAreaNumberById(techOperation.getPermittedWorkplaces().get(0));
                            return po.getFactStart() != null && po.getFactFinish() == null && areaWp == areaNumber;
                        })
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));

                planOperation.setFactStart(null);
                order.getParametersMap().get(new Area(areaNumber)).setNeedToPlan(needToPlan);
                setOrderStatusInDatabase(order, needToPlan, areaNumber);
                resetPlanOperationsAfterCurrent(planOperation.getNumber(), order);
                reschedule = true;
            }

            if (reschedule) {
                req.getRequestDispatcher("/start").forward(req, resp);
            } else {
                writer.write("{\"code\":1}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void setOrderStatusInDatabase(Order order, boolean needToPlan, int areaNumber) {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            ObjectNode mainNode;

            PreparedStatement pSt = sql.prepareStatement("SELECT params FROM orders " +
                    "WHERE route_map_number = ?");
            pSt.setString(1, order.getRouteMapNumber());
            ResultSet rs = pSt.executeQuery();
            if (rs.isBeforeFirst()) {
                rs.next();
                mainNode = (ObjectNode) mapper.readTree(rs.getString("params"));
            } else throw new NoSuchElementException("Can't find column for order");
            pSt.close();

            BooleanNode booleanNode = mapper.getNodeFactory().booleanNode(needToPlan);
            ((ObjectNode) mainNode.get("" + areaNumber)).set("needToPlan", booleanNode);

            pSt = sql.prepareStatement("UPDATE orders " +
                    "SET params = ?::jsonb WHERE route_map_number = ?");
            pSt.setString(1, mapper.writeValueAsString(mainNode));
            pSt.setString(2, order.getRouteMapNumber());
            pSt.executeUpdate();
            pSt.close();

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void resetPlanOperationsAfterCurrent(int operationNumber, Order order) {
        for (PlanOperation po : order.getPlanOperations()) {
            if (po.getNumber() >= operationNumber) {
                po.setFactStart(null);
                if (po.getAdjustmentTask() != null) {
                    po.getAdjustmentTask().setPlanStart(null);
                    po.getAdjustmentTask().setPlanFinish(null);
                }

                if (po.getParallelTask().getBatch() > 0) {
                    po.getParallelTask().setPlanStart(null);
                    po.getParallelTask().setPlanFinish(null);
                }

                if (po.getSequentialTask().getBatch() > 0) {
                    po.getSequentialTask().setPlanStart(null);
                    po.getSequentialTask().setPlanFinish(null);
                }
            }
        }
    }
}