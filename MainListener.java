package ru.pnppk.mes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.workplace.WorkplaceParameters;
import ru.pnppk.mes.pojo.tech.TechCard;
import ru.pnppk.mes.pojo.tech.TechCardArchive;
import ru.pnppk.mes.pojo.tech.TechOperation;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebListener
public class MainListener implements ServletContextListener {
    private static ObjectMapper mapper = new ObjectMapper();

    private static Map<Area, List<Order>> globalPlan = new ConcurrentHashMap<>();

    private static Map<Area, Chromosome> areasChromosome = new ConcurrentHashMap<>();

    private static Map<Area, List<Workplace>> areasWorkplaces = new ConcurrentHashMap<>();

    private static TechCardArchive techCardArchive;

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        List<Integer> areaNumbers = readAreaNumbers();

        List<Workplace> allWorkplaces = readWorkplacesForAreas();

        for (int areaNumber : areaNumbers) {
            Area area = new Area(areaNumber);
            List<Workplace> areaWorkplaces = allWorkplaces.stream()
                    .filter(workplace -> workplace.getAreaNumber() == areaNumber)
                    .collect(Collectors.toList());

            globalPlan.put(area, new ArrayList<>());

            areasWorkplaces.put(area, areaWorkplaces);

            areasChromosome.put(area, new Chromosome(null, areaWorkplaces));
        }

        techCardArchive = readTechCardArchive();

        try {
            ApiHelper.getGlobalPlan();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        for (int areaNumber : areaNumbers) {
            Area area = new Area(areaNumber);
            List<Order> orders = globalPlan.get(area);
            areasChromosome.get(area).setPlanOperations(ApiHelper.preparePlanForAlgorithm(orders, areaNumber));
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public static Map<Area, List<Order>> getGlobalPlan() {
        return globalPlan;
    }

    public static Map<Area, Chromosome> getAreasChromosome() {
        return areasChromosome;
    }

    public static TechCardArchive getTechCardArchive() {
        return techCardArchive;
    }

    public static Map<Area, List<Workplace>> getAreasWorkplaces() {
        return areasWorkplaces;
    }

    private static List<Integer> readAreaNumbers() {
        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("SELECT ARRAY(SELECT number FROM base.areas) AS arr");
            ResultSet rs = pSt.executeQuery();
            rs.next();
            return Arrays.asList((Integer[]) rs.getArray("arr").getArray());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Workplace> readWorkplacesForAreas() {
        List<Workplace> workplaces = new ArrayList<>();

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("SELECT id, number, name, shift, parameters, area_number FROM workplaces");
            ResultSet resultSet = pSt.executeQuery();
            LocalDateTime availability = LocalDateTime.now();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int invNumber = resultSet.getInt("number");
                String name = resultSet.getString("name");
                int shift = resultSet.getInt("shift");
                int areaNumber = resultSet.getInt("area_number");
                WorkplaceParameters workplaceParameters = mapper.readValue(resultSet.getString("parameters"), WorkplaceParameters.class);
                Workplace wp = new Workplace(id, invNumber, name, shift, availability, areaNumber, workplaceParameters);
                workplaces.add(wp);
            }
            resultSet.close();
            pSt.close();

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return workplaces;
    }

    private TechCardArchive readTechCardArchive() {
        TechCardArchive techCardArchive = new TechCardArchive();

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("SELECT * FROM tech_cards ORDER BY cipher, operation_number");
            ResultSet resultSet = pSt.executeQuery();

            TechCard techCard = null;

            while (resultSet.next()) {
                String cipher = resultSet.getString("cipher");
                String name = resultSet.getString("name");
                int group = resultSet.getInt("tech_group");
                int groupCounter = resultSet.getInt("group_counter");


                int operationNumber = resultSet.getInt("operation_number");
                String operationName = resultSet.getString("operation_name");
                long duration = resultSet.getLong("duration");
                long adjDuration = resultSet.getLong("adjustment_duration");

                List<Integer> workplaceIds = Arrays.asList((Integer[]) resultSet.getArray("workplace_ids").getArray());
                List<Integer> permittedWorkplaces = new ArrayList<>();

                areasWorkplaces.forEach((area, workplaces) -> {
                    permittedWorkplaces.addAll(workplaces.stream()
                            .filter(workplace -> workplaceIds.contains(workplace.getInvNumber()))
                            .map(Workplace::getId)
                            .collect(Collectors.toList()));
                });

                if (techCard == null || !techCard.getCipher().equals(cipher)) {

                    techCard = new TechCard(cipher, name, new ArrayList<>(), group, groupCounter);

                    techCardArchive.getArchive().computeIfAbsent(cipher, s -> new ArrayList<>()).add(techCard);
                }

                TechOperation techOperation = new TechOperation(operationNumber, operationName, duration, adjDuration);
                techOperation.getPermittedWorkplaces().addAll(permittedWorkplaces);
                techCard.getTechOperations().add(techOperation);
            }

            techCardArchive.getArchive().values().stream().flatMap(techCards -> techCards.stream()).forEach(card -> {
                List<TechOperation> techOperations = card.getTechOperations();
                for (int i = 0; i < techOperations.size() - 1; i++) {
                    techOperations.get(i).setNextOperation(techOperations.get(i + 1));
                }
            });

            resultSet.close();
            pSt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return techCardArchive;
    }
}
