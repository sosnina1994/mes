package ru.pnppk.mes.algorithm;

import ru.pnppk.mes.MainListener;
import ru.pnppk.mes.algorithm.helper.Dispenser;
import ru.pnppk.mes.algorithm.helper.Helper;
import ru.pnppk.mes.algorithm.pojo.Chromosome;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.pojo.manufacture.Area;
import ru.pnppk.mes.pojo.manufacture.workplace.Workplace;
import ru.pnppk.mes.pojo.manufacture.plan.order.Order;
import ru.pnppk.mes.pojo.manufacture.plan.operation.PlanOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Manager {

    static final int POPULATION_SIZE = 20; //Minimum population = 20
    private static final int ITERATION_SIZE = 10;

    public static Chromosome runAlgorithm(int areaNumber, List<Long> fitnessFunctionValues) {
        Chromosome chromosome = MainListener.getAreasChromosome().get(new Area(areaNumber));

        List<Order> orders = MainListener.getGlobalPlan().get(new Area(areaNumber));
        List<Workplace> workplaces = MainListener.getAreasWorkplaces().get(new Area(areaNumber));

        List<Workplace> actualWorkplaces = ApiHelper.prepareWorkplaceForAlgorithm(workplaces, orders, areaNumber);
        chromosome.setWorkplaces(actualWorkplaces);

        List<PlanOperation> actualPlanOperations = ApiHelper.preparePlanForAlgorithm(orders, areaNumber);
        chromosome.setPlanOperations(actualPlanOperations);

        // TODO: Don't run algorithm when all operation started, but not finished.
        //  NULL moment - all operations are fact finish = null ! ! !
        boolean checkOperationForAlgorithm = chromosome.getPlanOperations().stream()
                .allMatch(planOperation -> planOperation.getFactFinish() != null);
        if (checkOperationForAlgorithm) {
            chromosome.setWorkplaces(Helper.copyWorkplace(chromosome.getWorkplaces(), actualPlanOperations));
            chromosome.resetWorkplaces();
            return chromosome;

            // chromosome.getWorkplaces().clear(); --- If all operations finished ! ! !
        }

        List<Chromosome> population = generateStartPopulation(chromosome);

        chromosome = startAlgorithm(population, fitnessFunctionValues);

        MainListener.getAreasChromosome().put(new Area(areaNumber), chromosome);

        return chromosome;
    }

    //генерация стартовой популяции
    private static List<Chromosome> generateStartPopulation(Chromosome baseChromosome) {
        List<Chromosome> population = new ArrayList<>();

        List<Order> orders = baseChromosome.getPlanOperations()
                .stream()
                .map(PlanOperation::getOrder)
                .distinct()
                .collect(Collectors.toList());

        LinkedList<Long> encodedChromosome = Helper.encode(baseChromosome);

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Collections.shuffle(encodedChromosome);
            List<PlanOperation> po = Helper.decode(encodedChromosome, Helper.copyOrders(orders));
            Chromosome chromosome = new Chromosome(po, Helper.copyWorkplace(baseChromosome.getWorkplaces(), po));
            Dispenser.dispense(chromosome);
            population.add(chromosome);
        }
        return population;
    }

    //запуск алгоритма
    private static Chromosome startAlgorithm(List<Chromosome> population, List<Long> resultMinFF) {
        long min = Long.MAX_VALUE;
        int minIndex = 0;

        List<Long> rates = new ArrayList<>();
        for (Chromosome chromosome : population) {
            rates.add(Engine.fitnessFunction(chromosome.getWorkplaces(), chromosome.getPlanOperations()));
        }

        for (int count = 0; count < ITERATION_SIZE; count++) {
            List<Integer> indexes = Engine.selection(rates);

            for (int i = 0; i < indexes.size(); i += 2) {
                List<Chromosome> children = Engine.crossover(population.get(indexes.get(i)),
                        population.get(indexes.get(i + 1)));

                for (Chromosome child : children) {
                    long max = Collections.max(rates);
                    int maxIndex = rates.indexOf(max);
                    child.resetWorkplaces();
                    Dispenser.dispense(child);
                    long childFitFunc = Engine.fitnessFunction(child.getWorkplaces(), child.getPlanOperations());
                    if (childFitFunc <= max) {
                        population.get(maxIndex).setPlanOperations(child.getPlanOperations());
                        rates.set(maxIndex, 0L);
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                int mutationIndex = (int) (Math.random() * population.size());
                Engine.mutation(population.get(mutationIndex));
            }

            for (Chromosome chromosome : population) {
                chromosome.resetWorkplaces();
                Dispenser.dispense(chromosome);
            }

            rates.clear();
            for (Chromosome chromosome : population) {
                rates.add(Engine.fitnessFunction(chromosome.getWorkplaces(), chromosome.getPlanOperations()));
            }

            min = Collections.min(rates);
            minIndex = rates.indexOf(min);
            resultMinFF.add(min);
        }

        System.out.println("BestFunction: " + min);
        return population.get(minIndex);
    }
}
