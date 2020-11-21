package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

@WebServlet("/priority")
public class PriorityApi extends HttpServlet {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        int priority = Integer.parseInt(req.getParameter("priority"));

        try {
            try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
                ObjectNode mainNode;

                PreparedStatement pSt = sql.prepareStatement("SELECT params FROM orders " +
                        "WHERE route_map_number = ?");
                pSt.setString(1, routeMapNumber);
                ResultSet rs = pSt.executeQuery();
                if (rs.isBeforeFirst()) {
                    rs.next();
                    mainNode = (ObjectNode) mapper.readTree(rs.getString("params"));
                } else throw new NoSuchElementException("Can't find column for order");
                pSt.close();

                NumericNode priorityNode = mapper.getNodeFactory().numberNode(priority);
                ((ObjectNode) mainNode.get("" + areaNumber)).set("priority", priorityNode);

                pSt = sql.prepareStatement("UPDATE orders " +
                        "SET params = ?::jsonb WHERE route_map_number = ?");
                pSt.setString(1, mapper.writeValueAsString(mainNode));
                pSt.setString(2, routeMapNumber);
                pSt.executeUpdate();
                pSt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Order order = ApiHelper.getOrderByRouteMapNumber(areaNumber, routeMapNumber);
            order.getParametersMap().get(new Area(areaNumber)).setPriority(priority);

            req.getRequestDispatcher("/start").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
