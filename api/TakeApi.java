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
import java.time.LocalDateTime;

@WebServlet("/take")
public class TakeApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        int operationNumber = Integer.parseInt(req.getParameter("operationNumber"));

        try {
            Order order = ApiHelper.getOrderByRouteMapNumber(areaNumber, routeMapNumber);

            PlanOperation poTake = null;
            for (PlanOperation planOperation : order.getPlanOperations()) {
                if (planOperation.getOrder().getRouteMapNumber().equals(routeMapNumber)) {
                    if (planOperation.getNumber() == operationNumber) poTake = planOperation;
                }
            }

            takeToWork(poTake, LocalDateTime.now());

            req.getRequestDispatcher("/start").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    //назаначение плановых стартов и финишей при взятии операции в работу
    private void takeToWork(PlanOperation po, LocalDateTime start) {
        po.setFactStart(start);
        LocalDateTime mainStart;

        if (po.getAdjustmentTask() != null) {
            ApiHelper.refreshDateOfTask(po.getAdjustmentTask(), po.getFactStart());
            mainStart = po.getAdjustmentTask().getPlanFinish();
        } else mainStart = po.getFactStart();

        if (po.getSequentialTask().getBatch() > 0 && po.getParallelTask().getBatch() > 0) {
            ApiHelper.refreshDateOfTask(po.getParallelTask(), mainStart);
        } else {
            if (po.getParallelTask().getBatch() > 0) {
                ApiHelper.refreshDateOfTask(po.getParallelTask(), mainStart);
                po.getSequentialTask().setPlanStart(null);
                po.getSequentialTask().setPlanFinish(null);
            } else {
                ApiHelper.refreshDateOfTask(po.getSequentialTask(), mainStart);
                po.getParallelTask().setPlanStart(null);
                po.getParallelTask().setPlanFinish(null);
            }
        }
    }
}