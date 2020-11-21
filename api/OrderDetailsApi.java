package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;

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
import java.sql.Timestamp;

@WebServlet("/orderdetails")
public class OrderDetailsApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String routeMapNumber = req.getParameter("routeMapNumber");
        String cipher = req.getParameter("cipher");

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            //Плановые операции заказа
            ArrayNode mainNode = mapper.createArrayNode();

            PreparedStatement pSt = sql.prepareStatement("SELECT plan_operations.*, tech_cards.operation_name, " +
                    "tech_cards.adjustment_duration " +
                    "FROM plan_operations, tech_cards " +
                    "WHERE route_map_number = ? AND cipher = ? AND plan_operations.number = tech_cards.operation_number " +
                    "ORDER BY number");
            pSt.setString(1, routeMapNumber);
            pSt.setString(2, cipher);

            ResultSet rs = pSt.executeQuery();

            while (rs.next()) {
                ObjectNode planOperation = mainNode.addObject();

                planOperation.put("number", rs.getInt("number"));
                planOperation.put("name", rs.getString("operation_name"));
                planOperation.put("blank", rs.getBoolean("blank"));

                int parallelBatch = rs.getInt("parallel_batch");
                int sequentialBatch = rs.getInt("sequential_batch");
                long adjDuration = rs.getLong("adjustment_duration");

                Timestamp planDate;
                if (adjDuration != -1) {
                    planDate = rs.getTimestamp("adjustment_plan_start");
                } else if (parallelBatch > 0) {
                    planDate = rs.getTimestamp("parallel_plan_start");
                } else {
                    planDate = rs.getTimestamp("sequential_plan_start");
                }
                planOperation.put("planStart", planDate == null ? null : ApiHelper.DATE_TIME_FORMATTER.format(planDate.toLocalDateTime()));

                if (sequentialBatch > 0) {
                    planDate = rs.getTimestamp("sequential_plan_finish");
                } else {
                    planDate = rs.getTimestamp("parallel_plan_finish");
                }
                planOperation.put("planFinish", planDate == null ? null : ApiHelper.DATE_TIME_FORMATTER.format(planDate.toLocalDateTime()));

                planDate = rs.getTimestamp("fact_start");
                planOperation.put("factStart", planDate == null ? null : ApiHelper.DATE_TIME_FORMATTER.format(planDate.toLocalDateTime()));

                planDate = rs.getTimestamp("fact_finish");
                planOperation.put("factFinish", planDate == null ? null : ApiHelper.DATE_TIME_FORMATTER.format(planDate.toLocalDateTime()));
            }
            rs.close();
            pSt.close();

            PrintWriter pw = resp.getWriter();
            pw.write(mapper.writeValueAsString(mainNode));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }

    }
}
