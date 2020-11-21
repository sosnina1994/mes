package ru.pnppk.mes.api;

import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/reset")
public class ResetApi extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        try {
            resetOrder();
            resetPlanOp();
            resetBlanksJournal();

            MainListener.getAreasWorkplaces().values().forEach(workplaces -> {
                for (Workplace wp : workplaces) {
                    if (wp.getDailyTasks().size() > 0) {
                        wp.getDailyTasks().clear();
                    }
                }
            });

            MainListener.getGlobalPlan().values().forEach(List::clear);
            ApiHelper.getGlobalPlan();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        MainListener.getGlobalPlan().keySet().forEach(area -> {
                    List<Order> orders = MainListener.getGlobalPlan().get(area);
                    List<Workplace> workplaces = MainListener.getAreasWorkplaces().get(area);
                    MainListener.getAreasChromosome()
                            .get(area)
                            .setPlanOperations(ApiHelper.preparePlanForAlgorithm(orders, area.getNumber()));

                    MainListener.getAreasChromosome()
                            .get(area)
                            .setWorkplaces(ApiHelper.prepareWorkplaceForAlgorithm(workplaces, orders, area.getNumber()));

                });
    }

    private void resetOrder() throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("UPDATE orders " +
                    "SET params = '{\"5\":{\"priority\":0,\"needToPlan\":true}}'::jsonb");
            pSt.executeUpdate();
            pSt.close();
        }
    }

    private void resetPlanOp() throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("UPDATE plan_operations " +
                    "SET fact_start = null, fact_finish = null," +
                    "parallel_plan_start = null, parallel_plan_finish = null, parallel_batch = 0," +
                    "sequential_plan_start = null, sequential_plan_finish = null, sequential_batch = 50," +
                    "adjustment_plan_start = null, adjustment_plan_finish = null, " +
                    "parallel_plan_to_left = false, workplace_id = -1, blank = false");

            pSt.executeUpdate();
            pSt.close();
        }
    }

    private void resetBlanksJournal() throws SQLException {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()){
            PreparedStatement pSt = sql.prepareStatement("DELETE FROM blanks_journal");

            pSt.executeUpdate();
            pSt.close();
        }
    }
}