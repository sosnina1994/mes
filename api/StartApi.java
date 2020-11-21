package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.Manager;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/start")
public class StartApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        resp.setContentType("application/json; charset=UTF-8");
        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));

        try {
            List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));

            List<Long> resultMinFF = new ArrayList<>();

            Chromosome bestSolution = Manager.runAlgorithm(areaNumber, resultMinFF);

            List<Workplace> workplaces = MainListener.getAreasWorkplaces().get(new Area(areaNumber));

            ApiHelper.updateOrders(bestSolution, orders);

            ApiHelper.updateWorkplaces(bestSolution, workplaces, orders);

            ObjectNode mainNode = mapper.createObjectNode();
            ObjectNode chart = ApiHelper.saveChart(bestSolution, workplaces);
            mainNode.putObject("chart");
            mainNode.replace("chart", chart);

            PrintWriter pw = resp.getWriter();
            pw.write(mapper.writeValueAsString(mainNode));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}