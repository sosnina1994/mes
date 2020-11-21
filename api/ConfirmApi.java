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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@WebServlet("/confirm")
public class ConfirmApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        String routeMapNumber = req.getParameter("routeMapNumber");
        int operationNumber = Integer.parseInt(req.getParameter("operationNumber"));
        int count = Integer.parseInt(req.getParameter("detailCount"));

        try {
            Order order = ApiHelper.getOrderByRouteMapNumber(areaNumber, routeMapNumber);
            PlanOperation poConfirm = null;
            for (PlanOperation planOperation : order.getPlanOperations()) {
                if (planOperation.getOrder().getRouteMapNumber().equals(routeMapNumber)) {
                    if (planOperation.getNumber() == operationNumber) poConfirm = planOperation;
                }
            }
            confirmWork(poConfirm, count, LocalDateTime.now());

            req.getRequestDispatcher("/start").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void confirmWork(PlanOperation po, int count, LocalDateTime confirmTime) throws IllegalArgumentException {
        if (po.getAdjustmentTask() != null) {
            po.getAdjustmentTask().setPlanStart(null);
            po.getAdjustmentTask().setPlanFinish(null);
        }

        if (po.getOrder().getPlanOperations().indexOf(po) != 0) {
            if (count > po.getParallelTask().getBatch()) {
                throw new IllegalArgumentException("Incorrect confirm number");
            }
            po.getParallelTask().decrease(count);

        } else {
            if (count > po.getSequentialTask().getBatch()) {
                throw new IllegalArgumentException("Incorrect confirm number");
            }
            po.getSequentialTask().decrease(count);
        }

        if (po.getSequentialTask().getBatch() == 0 && po.getParallelTask().getBatch() == 0) {
            po.setFactFinish(confirmTime);
            po.getParallelTask().setPlanStart(null);
            po.getParallelTask().setPlanFinish(null);
            po.getSequentialTask().setPlanStart(null);
            po.getSequentialTask().setPlanFinish(null);
        } else {
            if (po.getSequentialTask().getBatch() > 0 && po.getParallelTask().getBatch() > 0) {
                ApiHelper.refreshDateOfTask(po.getParallelTask(), confirmTime);

                if (po.getParallelTask().getPlanFinish().isAfter(po.getSequentialTask().getPlanStart())) {
                    ApiHelper.refreshDateOfTask(po.getSequentialTask(), po.getParallelTask().getPlanFinish());
                } else {
                    PlanOperation prevOperation = po.getOrder().getPrevPlanOperation(po);
                    LocalDateTime prevFinish;
                    if (prevOperation.getSequentialTask().getBatch() != 0) {
                        prevFinish = prevOperation.getSequentialTask().getPlanFinish();
                    } else {
                        prevFinish = prevOperation.getParallelTask().getPlanFinish();
                    }

                    if (prevFinish.isAfter(po.getParallelTask().getPlanFinish())) {
                        ApiHelper.refreshDateOfTask(po.getSequentialTask(), prevFinish);
                    } else {
                        ApiHelper.refreshDateOfTask(po.getSequentialTask(), po.getParallelTask().getPlanFinish());
                    }
                }
            } else {
                if (po.getSequentialTask().getBatch() > 0) {
                    ApiHelper.refreshDateOfTask(po.getSequentialTask(), confirmTime);
                } else {
                    ApiHelper.refreshDateOfTask(po.getParallelTask(), confirmTime);
                }
            }
        }

        try {
            LocalDateTime planFinish;
            if (po.getSequentialTask().getBatch() != 0) {
                planFinish = po.getSequentialTask().getPlanFinish();
            } else if (po.getParallelTask().getBatch() != 0) {
                planFinish = po.getParallelTask().getPlanFinish();
            } else {
                planFinish = po.getFactFinish();
            }

            PlanOperation nextOperation = po.getOrder().getNextPlanOperation(po);

            long nextDuration = po.getOrder().getTechOperationByNumber(po.getNumber()).getNextOperation().getDuration();
            nextOperation.getParallelTask().increase(count);
            nextOperation.getSequentialTask().decrease(count);

            long b = Duration.between(confirmTime, planFinish).get(ChronoUnit.SECONDS);
            //если длительность изготовления партии >= участка времени, то планируем вправо
            // иначе планируем влево
            if (nextDuration * nextOperation.getParallelTask().getBatch() >= b) nextOperation.setParallelPlanToLeft(false);
            else nextOperation.setParallelPlanToLeft(true);

            if (nextOperation.getFactStart() != null) {
                LocalDateTime nextParallelStart = nextOperation.getParallelTask().getPlanStart();
                nextOperation.getParallelTask().setPlanFinish(nextParallelStart.plusSeconds(nextOperation.getParallelTask().getBatch() * nextDuration));

                nextOperation.getSequentialTask().setPlanStart(planFinish);
                LocalDateTime nextSeqStart = nextOperation.getSequentialTask().getPlanStart();
                nextOperation.getSequentialTask().setPlanFinish(nextSeqStart.plusSeconds(nextOperation.getSequentialTask().getBatch() * nextDuration));

                if (nextOperation.getParallelTask().getPlanFinish().isAfter(nextSeqStart)) {
                    nextOperation.getSequentialTask().setPlanStart(nextOperation.getParallelTask().getPlanFinish());

                    nextSeqStart = nextOperation.getSequentialTask().getPlanStart();
                    nextOperation.getSequentialTask().setPlanFinish(nextSeqStart.plusSeconds(nextOperation.getSequentialTask().getBatch() * nextDuration));
                }
            }

            refreshDateOfNextOperation(nextOperation);

        } catch (Exception e) {e.printStackTrace();}
    }

    private void refreshDateOfNextOperation(PlanOperation po) {
        LocalDateTime planFinish;
        while (po.getOrder().getNextPlanOperation(po) != null) {
            if (po.getSequentialTask().getBatch() != 0) {
                planFinish = po.getSequentialTask().getPlanFinish();
            } else {
                planFinish = po.getParallelTask().getPlanFinish();
            }

            PlanOperation nextOperation = po.getOrder().getNextPlanOperation(po);

            long nextDuration = po.getOrder().getTechOperationByNumber(po.getNumber()).getNextOperation().getDuration();

            if (nextOperation.getFactStart() != null) {
                nextOperation.getSequentialTask().setPlanStart(planFinish);
                LocalDateTime nextSeqStart = nextOperation.getSequentialTask().getPlanStart();
                nextOperation.getSequentialTask().setPlanFinish(nextSeqStart.plusSeconds(nextOperation.getSequentialTask().getBatch() * nextDuration));
            }
            po = nextOperation;
        }
    }
}
