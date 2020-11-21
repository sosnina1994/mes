package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
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
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/plan")
public class PlanApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));

        try {
            List<Order> areaOrders = MainListener.getGlobalPlan().get(new Area(areaNumber));

            Chromosome chromosome = MainListener.getAreasChromosome().get(new Area(areaNumber));

            List<Workplace> workplaces = MainListener.getAreasWorkplaces().get(new Area(areaNumber));

            ObjectNode mainNode = mapper.createObjectNode();
            ObjectNode plan = ApiHelper.saveChart(chromosome, workplaces);
            mainNode.putObject("chart");
            mainNode.replace("chart", plan);

            ArrayNode orders = mainNode.putArray("orders");
            for (Order o : areaOrders) {
                boolean blank = false;
                List<PlanOperation> blankedOps = o.getPlanOperations().stream()
                        .filter(PlanOperation::isBlank)
                        .collect(Collectors.toList());
                if (blankedOps.size() > 0) {
                    PlanOperation planOp = blankedOps.get(blankedOps.size() - 1);
                    TechOperation techOp = planOp.getOrder().getTechOperationByNumber(planOp.getNumber());

                    int areaWp = -1;
                    if (techOp.getPermittedWorkplaces().size() > 0) areaWp = ApiHelper.getAreaNumberById(techOp.getPermittedWorkplaces().get(0));
                    if (areaWp == areaNumber && planOp.getFactFinish() == null) {
                        blank = true;
                    }
                }

                int priority = o.getParametersMap().get(new Area(areaNumber)).getPriority();
                boolean needToPlan = o.getParametersMap().get(new Area(areaNumber)).isNeedToPlan();

                ObjectNode order = orders.addObject();
                order.put("cipher", o.getTechCard().getCipher());
                order.put("name", o.getTechCard().getName());
                order.put("orderNumber", o.getNumber());
                order.put("routeMap", o.getRouteMapNumber());
                order.put("count", o.getBatch());
                order.put("blank", blank);
                order.put("closeDate", o.getLimitation().toString());
                order.put("priority", priority);
                order.put("needToPlan", needToPlan);
            }
            writer.write(mapper.writeValueAsString(mainNode));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}