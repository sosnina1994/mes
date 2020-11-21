package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/task")
public class TaskApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        //int workplaceNumber = Integer.parseInt(req.getParameter("workplaceNumber"));

        try {
            Chromosome chromosome = MainListener.getAreasChromosome().get(new Area(areaNumber));

            //Операции, доступные для старта
            Set<PlanOperation> availableForStart = chromosome.getWorkplaces()
                    .stream()
                    .filter(workplace -> workplace.getDailyTasks().size() > 0)
                    .map(workplace -> workplace.getDailyTasks().get(0))
                    .filter(task -> task.getPlanOperation().getFactStart() == null)
                    .filter(task -> {
                        PlanOperation prevOp = task.getPlanOperation().getOrder().getPrevPlanOperation(task.getPlanOperation());
                        if (prevOp == null) return true;
                        else return task.getPlanOperation().getParallelTask().getBatch() > 0;
                    })
                    .map(AbstractTask::getPlanOperation)
                    .collect(Collectors.toSet());

            PrintWriter writer = resp.getWriter();
            ArrayNode mainNode = mapper.createArrayNode();

            for (PlanOperation p : availableForStart) {
                ObjectNode planOp = mainNode.addObject();
                planOp.put("cipher", p.getOrder().getTechCard().getCipher());
                planOp.put("name", p.getOrder().getTechCard().getName());
                planOp.put("orderNumber", p.getOrder().getNumber());
                planOp.put("routeMap", p.getOrder().getRouteMapNumber());
                planOp.put("operationNumber", p.getNumber());
                planOp.put("count", p.getOrder().getBatch());
                planOp.put("currentCount", p.getParallelTask().getBatch() > 0 ?
                        p.getParallelTask().getBatch() : p.getSequentialTask().getBatch());
                planOp.put("priority", p.getOrder().getParametersMap().get(new Area(areaNumber)).getPriority());

                Workplace wp = chromosome.getWorkplaces().stream()
                        .filter(workplace -> workplace.getDailyTasks().contains(p.getParallelTask()) || workplace.getDailyTasks().contains(p.getSequentialTask()))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("workplace not found"));

                planOp.put("workplaceId", wp.getId());
                planOp.put("workplaceInvNumber", wp.getInvNumber());
                planOp.put("workplaceName", wp.getName());
                planOp.put("startStatus", false);
            }

            //Операции, доступные для подтверждения
            Set<PlanOperation> availableForFinish = new HashSet<>();
            chromosome.getWorkplaces().forEach(workplace -> {
                workplace.getDailyTasks().stream()
                        .filter(task -> task.getPlanOperation().getFactStart() != null && task.getPlanOperation().getFactFinish() == null)
                        .forEach(task -> availableForFinish.add(task.getPlanOperation()));
            });

            for (PlanOperation p : availableForFinish) {
                ObjectNode planOp = mainNode.addObject();
                planOp.put("cipher", p.getOrder().getTechCard().getCipher());
                planOp.put("name", p.getOrder().getTechCard().getName());
                planOp.put("orderNumber", p.getOrder().getNumber());
                planOp.put("routeMap", p.getOrder().getRouteMapNumber());
                planOp.put("operationNumber", p.getNumber());
                planOp.put("count", p.getOrder().getBatch());
                planOp.put("currentCount", p.getParallelTask().getBatch() > 0 ?
                        p.getParallelTask().getBatch() : p.getSequentialTask().getBatch());
                planOp.put("priority", p.getOrder().getParametersMap().get(new Area(areaNumber)).getPriority());

                Workplace wp = chromosome.getWorkplaces().stream()
                        .filter(workplace -> workplace.getDailyTasks().contains(p.getParallelTask()) || workplace.getDailyTasks().contains(p.getSequentialTask()))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("workplace not found"));

                planOp.put("workplaceId", wp.getId());
                planOp.put("workplaceInvNumber", wp.getInvNumber());
                planOp.put("workplaceName", wp.getName());
                planOp.put("startStatus", true);

            }
            writer.write(mapper.writeValueAsString(mainNode));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
