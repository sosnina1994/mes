package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/stoporder")
public class StopOrderApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        int operationNumber = Integer.parseInt(req.getParameter("operationNumber"));

        try {
            Order order = ApiHelper.getOrderByRouteMapNumber(areaNumber, routeMapNumber);

            PlanOperation currentPlanOp = null;
            for (PlanOperation planOperation : order.getPlanOperations()) {
                if (planOperation.getOrder().getRouteMapNumber().equals(routeMapNumber)) {
                    if (planOperation.getNumber() == operationNumber) currentPlanOp = planOperation;
                }
            }

            currentPlanOp.setFactStart(null);

            req.getRequestDispatcher("/start").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
