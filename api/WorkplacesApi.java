package ru.pnppk.mes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.Manager;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;
import ru.pnppk.mes.pojo.manufacture.plan.operation.task.AbstractTask;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet("/wp")
public class WorkplacesApi  extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        String cmd = req.getParameter("cmd");
        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));
        boolean force = Boolean.parseBoolean(req.getParameter("force"));
        boolean isNeedToPlan = Boolean.parseBoolean(req.getParameter("isNeedToPlan"));

        try {
            List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));
            List<Workplace> workplaces = MainListener.getAreasWorkplaces().get(new Area(areaNumber));

            if ("get".equals(cmd)) {
                writer.write(mapper.writeValueAsString(workplaces));
            } else if ("set".equals(cmd)) {

                int id = Integer.parseInt(req.getParameter("id"));
                int shift = Integer.parseInt(req.getParameter("shift"));

                Workplace currentWp = workplaces.stream()
                        .filter(wp -> wp.getId() == id)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("Workplace not found"));

                // Текущая смена у оборудования
                int oldShift = currentWp.getShift();

                // проверить, есть ли начатые операции на текущем рабочем месте
                boolean startedOp = currentWp.getDailyTasks().stream()
                        .anyMatch(task -> task.getPlanOperation().getFactStart() != null);

                if (!force) {
                    /*  Блок выполняется если нет начатых задач
                    Выполняеться:
                    1. Присвоение новой смены в общем листе рабочих мест
                    2. Обновление базы данных */

                    currentWp.setShift(shift);
                    updateWorkplaceInDataBase(currentWp);

                    if (currentWp.getDailyTasks().size() == 0) {
                        /* Если лист задач рабочего места пустой, то перепланирование должно быть вызвано только тогда,
                        когда мы вводим оборудование в планирование. */

                        if (oldShift == 0 && shift != 0) isNeedToPlan = true;
                        writer.write("{\"code\":0}");

                    } else if (currentWp.getDailyTasks().size() > 0 && !startedOp) {
                        /*  Вывод оборудования из планирования при наличии запланированных на данное рабочее место задач:
                        Чистка DailyTasks */

                        if (shift == 0) {
                            cleanDailyTasks(currentWp);
                            isNeedToPlan = true;
                        }
                        writer.write("{\"code\":0}");

                    } else {
                        writer.write("{\"code\":1}");
                        isNeedToPlan = false;
                    }

                } else {
                    /* Вывод оборудования из планирования
                    при наличии взятых на данном рабочем месте задач.
                    Выполняем приостановку выполнения операции.
                    Одновременно на одном рабочем месте может быть взята в работу только одна операция!
                    В случае изменения смености на совпадающую, очистка DailyTasks не происходит,
                    а операция не приостановливается */

                    boolean isCrossingShift = checkShiftCrossing(currentWp.getShift(), shift);

                    if (shift == 0 || !isCrossingShift) {
                        PlanOperation planOp = currentWp.getDailyTasks().stream()
                                .filter(task -> task.getPlanOperation().getFactStart() != null)
                                .map(AbstractTask::getPlanOperation)
                                .findFirst()
                                .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));

                        PlanOperation planOperation = orders.stream()
                                .flatMap(order -> order.getPlanOperations().stream())
                                .filter(po -> po.equals(planOp))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));


                        planOperation.setFactStart(null);
                        currentWp.setShift(shift);
                        updateWorkplaceInDataBase(currentWp);
                        cleanDailyTasks(currentWp);
                        writer.write("{\"code\":0}");

                        isNeedToPlan = true;

                    } else {
                        currentWp.setShift(shift);
                        updateWorkplaceInDataBase(currentWp);
                        writer.write("{\"code\":0}");
                    }
                }

                /* Перепланирование будет происходить автоматически:
                    1. На данном рабочем месте есть запланированные (не взятые в работу) задачи
                и рабочему месту присваивается сменость 0;
                    2. Рабочее место вводиться в планирование;
                    3. На данном рабочем месте есть начатая задача (она всегда одна),
                рабочему месту присвоивается сменность 0
                или присваиваемая сменность не сходиться с текущей сменностью. */

                if (isNeedToPlan) {
                    List<Long> resultMinFF = new ArrayList<>();

                    Chromosome bestSolution = Manager.runAlgorithm(areaNumber, resultMinFF);

                    ApiHelper.updateOrders(bestSolution, orders);
                    ApiHelper.updateWorkplaces(bestSolution, workplaces, orders);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    private void updateWorkplaceInDataBase(Workplace workplace) {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("UPDATE workplaces SET shift = ? WHERE id = ?");
            pSt.setInt(1, workplace.getShift());
            pSt.setInt(2, workplace.getId());
            pSt.executeUpdate();
            pSt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cleanDailyTasks(Workplace workplace) {
        int areaNumber = workplace.getAreaNumber();
        List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));

        for (AbstractTask task : workplace.getDailyTasks()) {
            PlanOperation planOperation = orders.stream()
                    .flatMap(order -> order.getPlanOperations().stream())
                    .filter(po -> po.equals(task.getPlanOperation()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("PlanOperation not found"));

            Order order = planOperation.getOrder();
            for (PlanOperation po : order.getPlanOperations()) {
                if (po.getNumber() >= planOperation.getNumber()) {
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
        workplace.getDailyTasks().clear();
        LocalDateTime currentTime = LocalDateTime.now();
        workplace.setAvailability(currentTime);
    }

    private static boolean checkShiftCrossing(int currentShiftWp, int transmittedShift) {
        return ((currentShiftWp & transmittedShift) >= currentShiftWp);
    }
}