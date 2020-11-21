package ru.pnppk.mes.mock;

public class Generator {

//    public static List<Order> generate(int orderCount, TechCardArchive techCardArchive, List<Workplace> workplaces, LocalDateTime beginningTime) {
//        List<Order> orders = new ArrayList<>();
//
//        for (int i = 1; i <= orderCount; i++) {
//            List<TechOperation> techOperations = new LinkedList<>();
//
//            TechCard techCard = new TechCard("PIKV.00" + i, techOperations);
//            techCardArchive.addTechCardToArchive(techCard);
//
//            Order order = new Order(i, (int) (Math.random() * 10 + 10), beginningTime.plusDays(3), techCard, "" + (int) (Math.random() * 89 + 10)
//                    + (int) (Math.random() * 89 + 10) + (int) (Math.random() * 89 + 10));
//            List<PlanOperation> planOperations = order.getPlanOperations();
//            orders.add(order);
//
//            for (int j = 1; j <= (int) (Math.random() * 10 + 5); j++) {
//                TechOperation techOperation = new TechOperation((j * 10), (long) (Math.random() * 60 * 4 + 4 * 60));
//
//                int operationType = (int) (Math.random() * WorkplaceType.values().length);
//                int workplaceCount = (int) (Math.random() * 2 + 1);
//
//                List<Workplace> list = workplaces.stream()
//                        .filter(workplace -> workplace.getType() == WorkplaceType.values()[operationType])
//                        .collect(Collectors.toList());
//                if (workplaceCount == 2) {
//                    list.forEach(workplace -> techOperation.addPermittedWorkplaces(workplace));
//                } else {
//                    Collections.shuffle(list);
//                    techOperation.addPermittedWorkplaces(list.get(0));
//                }
//
//
//
//                techOperations.add(techOperation);
//                for (int k = 0; k < techOperations.size() - 1; k++) {
//                    techOperations.get(k).setNextOperation(techOperations.get(k + 1));
//                }
//
//                PlanOperation planOperation = new PlanOperation((j * 10), order);
//                planOperations.add(planOperation);
//
//                if (techOperation.getPermittedWorkplaces()
//                        .stream()
//                        .noneMatch(wp -> wp.getInvNumber() == 1000 || wp.getInvNumber() == 1001)) {
//
//                    planOperation.setAdjustmentTask(new AdjustmentTask(planOperation, (long) (Math.random() * 60 * 4 + 4 * 60)));
//                }
//            }
//        }
//        return orders;
//    }

//    public static List<Order> staticGenerate(List<Workplace> wp, TechCardArchive techCardArchive, LocalDateTime beginningTime) {
//        //TechOp #1
//        TechOperation to101 = new TechOperation(10, "Токарная",300);  //Токарная
//        to101.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to102 = new TechOperation(20, "Слесарная",80);  //Слесарная
//        to102.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to103 = new TechOperation(30, "Фрезерная",600);   //Фрезерная
//        to103.addPermittedWorkplaces(wp.get(4));
//        TechOperation to104 = new TechOperation(40, "Фрезерная", 900);   //Фрезерная
//        to104.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to105 = new TechOperation(50, "Слесарная", 30);  //Слесарная
//        to105.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to106 = new TechOperation(60, "Токарная", 300);  //Токарная
//        to106.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to107 = new TechOperation(70, "Слесарная",80);  //Слесарная
//        to107.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to108 = new TechOperation(80, "Фрезерная", 400);   //Фрезерная
//        to108.addPermittedWorkplaces(wp.get(4));
//        TechOperation to109 = new TechOperation(90, "Фрезерная", 90);   //Фрезерная
//        to109.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to110 = new TechOperation(100, "Слесарная",30);  //Слесарная
//        to110.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_1 = new ArrayList<>();
//        techOperations_1.add(to101);
//        techOperations_1.add(to102);
//        techOperations_1.add(to103);
//        techOperations_1.add(to104);
//        techOperations_1.add(to105);
//        techOperations_1.add(to106);
//        techOperations_1.add(to107);
//        techOperations_1.add(to108);
//        techOperations_1.add(to109);
//        techOperations_1.add(to110);
//
//        for (int i = 0; i < techOperations_1.size() - 1; i++) {
//            techOperations_1.get(i).setNextOperation(techOperations_1.get(i + 1));
//        }
//
//        //TechOp #2
//        TechOperation to201 = new TechOperation(10, "Токарная", 200);  //Токарная
//        to201.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to202 = new TechOperation(20, "Слесарная", 60);  //Слесарная
//        to202.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to203 = new TechOperation(30, "Токарная", 150);   //Токарная
//        to203.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to204 = new TechOperation(40, "Слесарная", 60);  //Слесарная
//        to204.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to205 = new TechOperation(50, "Слесарная", 60);  //Слесарная
//        to205.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to206 = new TechOperation(60, "Токарная", 100);   //Токарная
//        to206.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to207 = new TechOperation(70, "Слесарная", 60);  //Слесарная
//        to207.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to208 = new TechOperation(80, "Фрезерная", 400);   //Фрезерная
//        to208.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to209 = new TechOperation(90,"Фрезерная", 90);   //Фрезерная
//        to209.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to210 = new TechOperation(100, "Слесарная", 30);  //Слесарная
//        to210.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_2 = new ArrayList<>();
//        techOperations_2.add(to201);
//        techOperations_2.add(to202);
//        techOperations_2.add(to203);
//        techOperations_2.add(to204);
//        techOperations_2.add(to205);
//        techOperations_2.add(to206);
//        techOperations_2.add(to207);
//        techOperations_2.add(to208);
//        techOperations_2.add(to209);
//        techOperations_2.add(to210);
//
//        for (int i = 0; i < techOperations_2.size() - 1; i++) {
//            techOperations_2.get(i).setNextOperation(techOperations_2.get(i + 1));
//        }
//
//        //TechOp #3
//        TechOperation to301 = new TechOperation(10, "Слесарная", 30);  //Слесарная
//        to301.addPermittedWorkplaces(wp.get(0));
//        TechOperation to302 = new TechOperation(20, "Токарная", 360);  //Токарная
//        to302.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to303 = new TechOperation(30, "Токарная", 1500);   //Токарная
//        to303.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to304 = new TechOperation(40, "Слесарная", 180);  //Слесарная
//        to304.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to305 = new TechOperation(50, "Фрезерная", 600);  //Фрезерная
//        to305.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to306 = new TechOperation(60, "Слесарная", 40);  //Слесарная
//        to306.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to307 = new TechOperation(70, "Токарная", 600);  //Токарная
//        to307.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to308 = new TechOperation(80, "Слесарная", 120);  //Слесарная
//        to308.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to309 = new TechOperation(90, "Фрезерная", 600);  //Фрезерная
//        to309.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to310 = new TechOperation(100, "Слесарная", 40);  //Слесарная
//        to310.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_3 = new ArrayList<>();
//        techOperations_3.add(to301);
//        techOperations_3.add(to302);
//        techOperations_3.add(to303);
//        techOperations_3.add(to304);
//        techOperations_3.add(to305);
//        techOperations_3.add(to306);
//        techOperations_3.add(to307);
//        techOperations_3.add(to308);
//        techOperations_3.add(to309);
//        techOperations_3.add(to310);
//
//        for (int i = 0; i < techOperations_3.size() - 1; i++) {
//            techOperations_3.get(i).setNextOperation(techOperations_3.get(i + 1));
//        }
//
//        //TechOp #4
//        TechOperation to401 = new TechOperation(10, "Токарная", 480);  //Токарная
//        to401.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to402 = new TechOperation(20, "Фрезерная", 480);  //Фрезерная
//        to402.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to403 = new TechOperation(30, "Слесарная", 20);  //Слесарная
//        to403.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to404 = new TechOperation(40, "Токарная", 1200);  //Токарная
//        to404.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to405 = new TechOperation(50, "Слесарная", 180);  //Слесарная
//        to405.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to406 = new TechOperation(60, "Токарная", 480);  //Токарная
//        to406.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to407 = new TechOperation(70, "Фрезерная", 480);  //Фрезерная
//        to407.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to408 = new TechOperation(80, "Слесарная", 20);  //Слесарная
//        to408.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to409 = new TechOperation(90, "Токарная", 1200);  //Токарная
//        to409.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to410 = new TechOperation(100, "Слесарная", 180);  //Слесарная
//        to410.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_4 = new ArrayList<>();
//        techOperations_4.add(to401);
//        techOperations_4.add(to402);
//        techOperations_4.add(to403);
//        techOperations_4.add(to404);
//        techOperations_4.add(to405);
//        techOperations_4.add(to406);
//        techOperations_4.add(to407);
//        techOperations_4.add(to408);
//        techOperations_4.add(to409);
//        techOperations_4.add(to410);
//
//        for (int i = 0; i < techOperations_4.size() - 1; i++) {
//            techOperations_4.get(i).setNextOperation(techOperations_4.get(i + 1));
//        }
//
//        //TechOp #5
//        TechOperation to501 = new TechOperation(10, "Слесарная", 100);  //слесарная
//        to501.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to502 = new TechOperation(20, "Токарная", 300);  //токарная
//        to502.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to503 = new TechOperation(30, "Фрезерная", 120);   //фрезерная
//        to503.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to504 = new TechOperation(40, "Токарная", 120);   //токарная
//        to504.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to505 = new TechOperation(50, "Фрезерная", 120);  //фрезерная
//        to505.addPermittedWorkplaces(wp.get(4));
//        TechOperation to506 = new TechOperation(60, "Фрезерная", 300);  //фрезерная
//        to506.addPermittedWorkplaces(wp.get(5));
//        TechOperation to507 = new TechOperation(70, "Токарная", 160);  //токарная
//        to507.addPermittedWorkplaces(wp.get(2));
//        TechOperation to508 = new TechOperation(80, "Токарная", 200);  // токарная
//        to508.addPermittedWorkplaces(wp.get(3));
//        TechOperation to509 = new TechOperation(90, "Слесарная", 80);   // слесарная
//        to509.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to510 = new TechOperation(100, "Слесарная", 80);   //слесарная
//        to510.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_5 = new ArrayList<>();
//        techOperations_5.add(to501);
//        techOperations_5.add(to502);
//        techOperations_5.add(to503);
//        techOperations_5.add(to504);
//        techOperations_5.add(to505);
//        techOperations_5.add(to506);
//        techOperations_5.add(to507);
//        techOperations_5.add(to508);
//        techOperations_5.add(to509);
//        techOperations_5.add(to510);
//
//
//        for (int i = 0; i < techOperations_5.size() - 1; i++) {
//            techOperations_5.get(i).setNextOperation(techOperations_5.get(i + 1));
//        }
//
//        //TechCard #6
//        TechOperation to601 = new TechOperation(10, "Слесарная", 60);    //слесарная
//        to601.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to602 = new TechOperation(20, "Фрезерная", 100);    //фрезерная
//        to602.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to603 = new TechOperation(30, "Токарная", 80);    //токарная
//        to603.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to604 = new TechOperation(40, "Токарная", 120);  //токарная
//        to604.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to605 = new TechOperation(50, "Слесарная", 60);    //слесарная
//        to605.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to606 = new TechOperation(60, "Фрезерная", 180);  //фрезерная
//        to606.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to607 = new TechOperation(70, "Слесарная", 60);   //слесарная
//        to607.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to608 = new TechOperation(80, "Фрезерная", 120);   //фрезерная
//        to608.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to609 = new TechOperation(90, "Токарная", 80);   //токарная
//        to609.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to610 = new TechOperation(100, "Слесарная", 60);    //слесарная
//        to610.addPermittedWorkplaces(wp.get(0), wp.get(1));
//
//        List<TechOperation> techOperations_6 = new ArrayList<>();
//        techOperations_6.add(to601);
//        techOperations_6.add(to602);
//        techOperations_6.add(to603);
//        techOperations_6.add(to604);
//        techOperations_6.add(to605);
//        techOperations_6.add(to606);
//        techOperations_6.add(to607);
//        techOperations_6.add(to607);
//        techOperations_6.add(to608);
//        techOperations_6.add(to609);
//        techOperations_6.add(to610);
//
//        for (int i = 0; i < techOperations_6.size() - 1; i++) {
//            techOperations_6.get(i).setNextOperation(techOperations_6.get(i + 1));
//        }
//
//        //TechCard #7
//        TechOperation to701 = new TechOperation(10, "Фрезерная", 100);     //фрезерная
//        to701.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to702 = new TechOperation(20, "Слесарная", 60);   //слесарная
//        to702.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to703 = new TechOperation(30, "Токарная", 80);   // токарная
//        to703.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to704 = new TechOperation(40, "Фрезерная", 100);  //фрезерная
//        to704.addPermittedWorkplaces(wp.get(4));
//        TechOperation to705 = new TechOperation(50, "Слесарная", 30);    //слесарная
//        to705.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to706 = new TechOperation(60, "Фрезерная", 250);  //фрезерная
//        to706.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to707 = new TechOperation(70, "Токарная", 80);   //токарная
//        to707.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to708 = new TechOperation(80, "Токарная", 300);  //токарная
//        to708.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to709 = new TechOperation(90, "Слесарная", 60);   //слесарная
//        to709.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to710 = new TechOperation(100, "Фрезерная", 100);    //фрезерная
//        to710.addPermittedWorkplaces(wp.get(5));
//
//        List<TechOperation> techOperations_7 = new ArrayList<>();
//        techOperations_7.add(to701);
//        techOperations_7.add(to702);
//        techOperations_7.add(to703);
//        techOperations_7.add(to704);
//        techOperations_7.add(to705);
//        techOperations_7.add(to706);
//        techOperations_7.add(to707);
//        techOperations_7.add(to708);
//        techOperations_7.add(to709);
//        techOperations_7.add(to710);
//
//        for (int i = 0; i < techOperations_7.size() - 1; i++) {
//            techOperations_7.get(i).setNextOperation(techOperations_7.get(i + 1));
//        }
//
//        //TechCard #8
//        TechOperation to801 = new TechOperation(10, "Токарная", 120);      //токарная
//        to801.addPermittedWorkplaces(wp.get(3));
//        TechOperation to802 = new TechOperation(20, "Слесарная", 120);       //слесарная
//        to802.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to803 = new TechOperation(30, "Фрезерная", 80);       //фрезерная
//        to803.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to804 = new TechOperation(40, "Токарная", 120);      //токарная
//        to804.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to805 = new TechOperation(50, "Слесарная", 30);        //слесарная
//        to805.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to806 = new TechOperation(60, "Фрезерная", 250);      //фрезерная
//        to806.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to807 = new TechOperation(70, "Слесарная", 80);       //слесарная
//        to807.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to808 = new TechOperation(80, "Токарная", 300);      //токарная
//        to808.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to809 = new TechOperation(90, "Слесарная", 80);       //слесарная
//        to809.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to810 = new TechOperation(100, "Токарная", 100);         //токарная
//        to810.addPermittedWorkplaces(wp.get(2), wp.get(3));
//
//        List<TechOperation> techOperations_8 = new ArrayList<>();
//        techOperations_8.add(to801);
//        techOperations_8.add(to802);
//        techOperations_8.add(to803);
//        techOperations_8.add(to804);
//        techOperations_8.add(to805);
//        techOperations_8.add(to806);
//        techOperations_8.add(to807);
//        techOperations_8.add(to808);
//        techOperations_8.add(to809);
//        techOperations_8.add(to810);
//
//        for (int i = 0; i < techOperations_8.size() - 1; i++) {
//            techOperations_8.get(i).setNextOperation(techOperations_8.get(i + 1));
//        }
//
//        //TechCard #9
//        TechOperation to901 = new TechOperation(10, "Токарная", 120);      //токарная
//        to901.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to902 = new TechOperation(20, "Слесарная", 120);       //слесарная
//        to902.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to903 = new TechOperation(30, "Фрезерная", 80);       //фрезерная
//        to903.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to904 = new TechOperation(40, "Токарная", 120);      //токарная
//        to904.addPermittedWorkplaces(wp.get(2));
//        TechOperation to905 = new TechOperation(50, "Слесарная", 30);        //слесарная
//        to905.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to906 = new TechOperation(60, "Фрезерная", 250);      //фрезерная
//        to906.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to907 = new TechOperation(70, "Слесарная", 80);       //слесарная
//        to907.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to908 = new TechOperation(80, "Токарная", 300);      //токарная
//        to908.addPermittedWorkplaces(wp.get(2), wp.get(3));
//        TechOperation to909 = new TechOperation(90, "Фрезерная", 80);       //фрезерная
//        to909.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to910 = new TechOperation(100, "Токарная", 120);      //токарная
//        to910.addPermittedWorkplaces(wp.get(2), wp.get(3));
//
//        List<TechOperation> techOperations_9 = new ArrayList<>();
//        techOperations_9.add(to901);
//        techOperations_9.add(to902);
//        techOperations_9.add(to903);
//        techOperations_9.add(to904);
//        techOperations_9.add(to905);
//        techOperations_9.add(to906);
//        techOperations_9.add(to907);
//        techOperations_9.add(to908);
//        techOperations_9.add(to909);
//        techOperations_9.add(to910);
//
//        for (int i = 0; i < techOperations_9.size() - 1; i++) {
//            techOperations_9.get(i).setNextOperation(techOperations_9.get(i + 1));
//        }
//
//        //TechCard #10
//        TechOperation to1001 = new TechOperation(10, "Токарная", 150);      //токарная
//        to1001.addPermittedWorkplaces(wp.get(3));
//        TechOperation to1002 = new TechOperation(20, "Слесарная", 20);       //слесарная
//        to1002.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to1003 = new TechOperation(30, "Фрезерная", 200);       //фрезерная
//        to1003.addPermittedWorkplaces(wp.get(5));
//        TechOperation to1004 = new TechOperation(40, "Токарная", 180);      //токарная
//        to1004.addPermittedWorkplaces(wp.get(3));
//        TechOperation to1005 = new TechOperation(50, "Слесарная", 50);        //слесарная
//        to1005.addPermittedWorkplaces(wp.get(1));
//        TechOperation to1006 = new TechOperation(60, "Фрезерная", 250);      //фрезерная
//        to1006.addPermittedWorkplaces(wp.get(4), wp.get(5));
//        TechOperation to1007 = new TechOperation(70, "Токарная", 250);      //токарная
//        to1007.addPermittedWorkplaces(wp.get(3));
//        TechOperation to1008 = new TechOperation(80, "Слесарная", 60);       //слесарная
//        to1008.addPermittedWorkplaces(wp.get(1));
//        TechOperation to1009 = new TechOperation(90, "Слесарная", 30);       //слесарная
//        to1009.addPermittedWorkplaces(wp.get(0), wp.get(1));
//        TechOperation to1010 = new TechOperation(100, "Фрезерная", 180);       //фрезерная
//        to1010.addPermittedWorkplaces(wp.get(4), wp.get(5));
//
//        List<TechOperation> techOperations_10 = new ArrayList<>();
//        techOperations_10.add(to1001);
//        techOperations_10.add(to1002);
//        techOperations_10.add(to1003);
//        techOperations_10.add(to1004);
//        techOperations_10.add(to1005);
//        techOperations_10.add(to1006);
//        techOperations_10.add(to1007);
//        techOperations_10.add(to1008);
//        techOperations_10.add(to1009);
//        techOperations_10.add(to1010);
//
//        for (int i = 0; i < techOperations_10.size() - 1; i++) {
//            techOperations_10.get(i).setNextOperation(techOperations_10.get(i + 1));
//        }
//
//        //техкарта
//        TechCard tm1 = new TechCard("PIKV100", techOperations_1);
//        TechCard tm2 = new TechCard("PIKV200", techOperations_2);
//        TechCard tm3 = new TechCard("PIKV300", techOperations_3);
//        TechCard tm4 = new TechCard("PIKV400", techOperations_4);
//        TechCard tm5 = new TechCard("PIKV500", techOperations_5);
//        TechCard tm6 = new TechCard("PIKV600", techOperations_6);
//        TechCard tm7 = new TechCard("PIKV700", techOperations_7);
//        TechCard tm8 = new TechCard("PIKV800", techOperations_8);
//        TechCard tm9 = new TechCard("PIKV900", techOperations_9);
//        TechCard tm10 = new TechCard("PIKV1000", techOperations_10);
//
//        TechCard tm11 = new TechCard("PIKV1100", techOperations_1);
//        TechCard tm12 = new TechCard("PIKV1200", techOperations_2);
//        TechCard tm13 = new TechCard("PIKV1300", techOperations_3);
//        TechCard tm14 = new TechCard("PIKV1400", techOperations_4);
//        TechCard tm15 = new TechCard("PIKV1500", techOperations_5);
//        TechCard tm16 = new TechCard("PIKV1600", techOperations_6);
//        TechCard tm17 = new TechCard("PIKV1700", techOperations_7);
//        TechCard tm18 = new TechCard("PIKV1800", techOperations_8);
//        TechCard tm19 = new TechCard("PIKV1900", techOperations_9);
//        TechCard tm20 = new TechCard("PIKV2000", techOperations_10);
//
//
//        //архив
//        techCardArchive.addTechCardToArchive(tm1);
//        techCardArchive.addTechCardToArchive(tm2);
//        techCardArchive.addTechCardToArchive(tm3);
//        techCardArchive.addTechCardToArchive(tm4);
//        techCardArchive.addTechCardToArchive(tm5);
//        techCardArchive.addTechCardToArchive(tm6);
//        techCardArchive.addTechCardToArchive(tm7);
//        techCardArchive.addTechCardToArchive(tm8);
//        techCardArchive.addTechCardToArchive(tm9);
//        techCardArchive.addTechCardToArchive(tm10);
//
//        //ПРОИЗВОДСТВО
//        List<Order> orders = new ArrayList<>();
//
//        Order order1 = new Order("ПИКВ100", "Корпус", "11111110100", 1, 50, beginningTime.plusDays(20), tm1, "0000d6");
////        order1.setType(OrderType.EXPRESS);
////        order1.setPriorityNumber(1);
//
//        Order order2 = new Order("ПИКВ200", "Основание", "11111110200", 2, 45, beginningTime.plusDays(25), tm2, "008b00");
////        order2.setType(OrderType.EXPRESS);
////        order2.setPriorityNumber(2);
//
//        Order order3 = new Order("ПИКВ300", "Ось", "11111110300",3, 40, beginningTime.plusDays(4), tm3, "d1c4e9");
////        order3.setType(OrderType.EXPRESS);
////        order3.setPriorityNumber(3);
//
//        Order order4 = new Order("ПИКВ400", "Основание", "11111110400",4, 60, beginningTime.plusDays(6), tm4, "004D40");
////        order4.setType(OrderType.EXPRESS);
////        order4.setPriorityNumber(4);
//
//        Order order5 = new Order("ПИКВ500", "Штуцер", "11111110500",5, 20, beginningTime.plusDays(2), tm5, "03a9f4");
////        order5.setType(OrderType.EXPRESS);
////        order5.setPriorityNumber(5);
//
//        Order order6 = new Order("ПИКВ600", "Ось", "11111110600",6, 30, beginningTime.plusDays(3), tm6, "005b9f");
////        order6.setType(OrderType.EXPRESS);
////        order6.setPriorityNumber(6);
//
//        Order order7 = new Order("ПИКВ700", "Корпус", "11111110700",7, 70, beginningTime.plusDays(7), tm7, "d7ccc8");
////        order7.setType(OrderType.EXPRESS);
////        order7.setPriorityNumber(7);
//
//
//        Order order8 = new Order("ПИКВ800", "Втулка", "11111110800",8, 80, beginningTime.plusDays(8), tm8, "ff6f00");
////        order8.setType(OrderType.EXPRESS);
////        order8.setPriorityNumber(8);
//
//        Order order9 = new Order("ПИКВ900", "Основание", "11111110900",9, 40, beginningTime.plusDays(18), tm9, "a1887f");
////        order9.setType(OrderType.EXPRESS);
////        order9.setPriorityNumber(9);
//
//        Order order10 = new Order("ПИКВ1000", "Ось", "11111101000",10, 10, beginningTime.plusDays(18), tm10, "7986cb");
////        order10.setType(OrderType.EXPRESS);
////        order10.setPriorityNumber(10);
//
//        Order order11 = new Order("ПИКВ1100", "Основание", "11111101100",11, 15, beginningTime.plusDays(18), tm11, "b388ff");
////        order11.setType(OrderType.EXPRESS);
////        order11.setPriorityNumber(11);
//
//        Order order12 = new Order("ПИКВ1200", "Штуцер", "11111101200",12, 10, beginningTime.plusDays(18), tm12, "880e4f");
////        order12.setType(OrderType.EXPRESS);
////        order12.setPriorityNumber(12);
//
//        Order order13 = new Order("ПИКВ1300", "Ось", "11111101300",13, 20, beginningTime.plusDays(18), tm13, "4a148c");
////        order13.setType(OrderType.EXPRESS);
////        order13.setPriorityNumber(13);
//
//        Order order14 = new Order("ПИКВ1400", "Основание", "11111101400",14, 20, beginningTime.plusDays(18), tm14, "00e676");
////        order14.setType(OrderType.EXPRESS);
////        order14.setPriorityNumber(14);
//
//        Order order15 = new Order("ПИКВ1500", "Втулка", "11111101500",15, 30, beginningTime.plusDays(20), tm15, "795548");
////        order15.setType(OrderType.EXPRESS);
////        order15.setPriorityNumber(15);
//
//        Order order16 = new Order("ПИКВ1600", "Корпус", "11111101600",16, 10, beginningTime.plusDays(10), tm16, "37474f");
////        order16.setType(OrderType.EXPRESS);
////        order16.setPriorityNumber(16);
//
//        Order order17 = new Order("ПИКВ1700", "Шайба", "11111101700",17, 20, beginningTime.plusDays(30), tm17, "424242");
////        order17.setType(OrderType.EXPRESS);
////        order16.setPriorityNumber(17);
//
//        Order order18 = new Order("ПИКВ1800", "Основание", "11111101800",18, 25, beginningTime.plusDays(30), tm18, "311b92");
////        order18.setType(OrderType.EXPRESS);
////        order18.setPriorityNumber(18);
//
//        Order order19 = new Order("ПИКВ1900", "Колесо", "11111101900",19, 10, beginningTime.plusDays(10), tm19, "6200ea");
////        order19.setType(OrderType.EXPRESS);
////        order19.setPriorityNumber(19);
//
//        Order order20 = new Order("ПИКВ2000", "Основание", "11111102000",20, 30, beginningTime.plusDays(30), tm20, "ff9e80");
////        order20.setType(OrderType.EXPRESS);
////        order20.setPriorityNumber(20);
//
//        order1.addOperation(new PlanOperation(10, order1));
//        order1.addOperation(new PlanOperation(20, order1));
//        order1.addOperation(new PlanOperation(30, order1));
//        order1.addOperation(new PlanOperation(40, order1));
//        order1.addOperation(new PlanOperation(50, order1));
//        order1.addOperation(new PlanOperation(60, order1));
//        order1.addOperation(new PlanOperation(70, order1));
//        order1.addOperation(new PlanOperation(80, order1));
//        order1.addOperation(new PlanOperation(90, order1));
//        order1.addOperation(new PlanOperation(100, order1));
//
//        order2.addOperation(new PlanOperation(10, order2));
//        order2.addOperation(new PlanOperation(20, order2));
//        order2.addOperation(new PlanOperation(30, order2));
//        order2.addOperation(new PlanOperation(40, order2));
//        order2.addOperation(new PlanOperation(50, order2));
//        order2.addOperation(new PlanOperation(60, order2));
//        order2.addOperation(new PlanOperation(70, order2));
//        order2.addOperation(new PlanOperation(80, order2));
//        order2.addOperation(new PlanOperation(90, order2));
//        order2.addOperation(new PlanOperation(100, order2));
//
//        order3.addOperation(new PlanOperation(10, order3));
//        order3.addOperation(new PlanOperation(20, order3));
//        order3.addOperation(new PlanOperation(30, order3));
//        order3.addOperation(new PlanOperation(40, order3));
//        order3.addOperation(new PlanOperation(50, order3));
//        order3.addOperation(new PlanOperation(60, order3));
//        order3.addOperation(new PlanOperation(70, order3));
//        order3.addOperation(new PlanOperation(80, order3));
//        order3.addOperation(new PlanOperation(90, order3));
//        order3.addOperation(new PlanOperation(100, order3));
//
//        order4.addOperation(new PlanOperation(10, order4));
//        order4.addOperation(new PlanOperation(20, order4));
//        order4.addOperation(new PlanOperation(30, order4));
//        order4.addOperation(new PlanOperation(40, order4));
//        order4.addOperation(new PlanOperation(50, order4));
//        order4.addOperation(new PlanOperation(60, order4));
//        order4.addOperation(new PlanOperation(70, order4));
//        order4.addOperation(new PlanOperation(80, order4));
//        order4.addOperation(new PlanOperation(90, order4));
//        order4.addOperation(new PlanOperation(100, order4));
//
//        order5.addOperation(new PlanOperation(10, order5));
//        order5.addOperation(new PlanOperation(20, order5));
//        order5.addOperation(new PlanOperation(30, order5));
//        order5.addOperation(new PlanOperation(40, order5));
//        order5.addOperation(new PlanOperation(50, order5));
//        order5.addOperation(new PlanOperation(60, order5));
//        order5.addOperation(new PlanOperation(70, order5));
//        order5.addOperation(new PlanOperation(80, order5));
//        order5.addOperation(new PlanOperation(90, order5));
//        order5.addOperation(new PlanOperation(100, order5));
//
//        order6.addOperation(new PlanOperation(10, order6));
//        order6.addOperation(new PlanOperation(20, order6));
//        order6.addOperation(new PlanOperation(30, order6));
//        order6.addOperation(new PlanOperation(40, order6));
//        order6.addOperation(new PlanOperation(50, order6));
//        order6.addOperation(new PlanOperation(60, order6));
//        order6.addOperation(new PlanOperation(70, order6));
//        order6.addOperation(new PlanOperation(80, order6));
//        order6.addOperation(new PlanOperation(90, order6));
//        order6.addOperation(new PlanOperation(100, order6));
//
//        order7.addOperation(new PlanOperation(10, order7));
//        order7.addOperation(new PlanOperation(20, order7));
//        order7.addOperation(new PlanOperation(30, order7));
//        order7.addOperation(new PlanOperation(40, order7));
//        order7.addOperation(new PlanOperation(50, order7));
//        order7.addOperation(new PlanOperation(60, order7));
//        order7.addOperation(new PlanOperation(70, order7));
//        order7.addOperation(new PlanOperation(80, order7));
//        order7.addOperation(new PlanOperation(90, order7));
//        order7.addOperation(new PlanOperation(100, order7));
//
//        order8.addOperation(new PlanOperation(10, order8));
//        order8.addOperation(new PlanOperation(20, order8));
//        order8.addOperation(new PlanOperation(30, order8));
//        order8.addOperation(new PlanOperation(40, order8));
//        order8.addOperation(new PlanOperation(50, order8));
//        order8.addOperation(new PlanOperation(60, order8));
//        order8.addOperation(new PlanOperation(70, order8));
//        order8.addOperation(new PlanOperation(80, order8));
//        order8.addOperation(new PlanOperation(90, order8));
//        order8.addOperation(new PlanOperation(100, order8));
//
//        order9.addOperation(new PlanOperation(10, order9));
//        order9.addOperation(new PlanOperation(20, order9));
//        order9.addOperation(new PlanOperation(30, order9));
//        order9.addOperation(new PlanOperation(40, order9));
//        order9.addOperation(new PlanOperation(50, order9));
//        order9.addOperation(new PlanOperation(60, order9));
//        order9.addOperation(new PlanOperation(70, order9));
//        order9.addOperation(new PlanOperation(80, order9));
//        order9.addOperation(new PlanOperation(90, order9));
//        order9.addOperation(new PlanOperation(100, order9));
//
//        order10.addOperation(new PlanOperation(10, order10));
//        order10.addOperation(new PlanOperation(20, order10));
//        order10.addOperation(new PlanOperation(30, order10));
//        order10.addOperation(new PlanOperation(40, order10));
//        order10.addOperation(new PlanOperation(50, order10));
//        order10.addOperation(new PlanOperation(60, order10));
//        order10.addOperation(new PlanOperation(70, order10));
//        order10.addOperation(new PlanOperation(80, order10));
//        order10.addOperation(new PlanOperation(90, order10));
//        order10.addOperation(new PlanOperation(100, order10));
//
//
//        order11.addOperation(new PlanOperation(10, order11));
//        order11.addOperation(new PlanOperation(20, order11));
//        order11.addOperation(new PlanOperation(30, order11));
//        order11.addOperation(new PlanOperation(40, order11));
//        order11.addOperation(new PlanOperation(50, order11));
//        order11.addOperation(new PlanOperation(60, order11));
//        order11.addOperation(new PlanOperation(70, order11));
//        order11.addOperation(new PlanOperation(80, order11));
//        order11.addOperation(new PlanOperation(90, order11));
//        order11.addOperation(new PlanOperation(100, order11));
//
//        order12.addOperation(new PlanOperation(10, order12));
//        order12.addOperation(new PlanOperation(20, order12));
//        order12.addOperation(new PlanOperation(30, order12));
//        order12.addOperation(new PlanOperation(40, order12));
//        order12.addOperation(new PlanOperation(50, order12));
//        order12.addOperation(new PlanOperation(60, order12));
//        order12.addOperation(new PlanOperation(70, order12));
//        order12.addOperation(new PlanOperation(80, order12));
//        order12.addOperation(new PlanOperation(90, order12));
//        order12.addOperation(new PlanOperation(100, order12));
//
//        order13.addOperation(new PlanOperation(10, order13));
//        order13.addOperation(new PlanOperation(20, order13));
//        order13.addOperation(new PlanOperation(30, order13));
//        order13.addOperation(new PlanOperation(40, order13));
//        order13.addOperation(new PlanOperation(50, order13));
//        order13.addOperation(new PlanOperation(60, order13));
//        order13.addOperation(new PlanOperation(70, order13));
//        order13.addOperation(new PlanOperation(80, order13));
//        order13.addOperation(new PlanOperation(90, order13));
//        order13.addOperation(new PlanOperation(100, order13));
//
//        order14.addOperation(new PlanOperation(10, order14));
//        order14.addOperation(new PlanOperation(20, order14));
//        order14.addOperation(new PlanOperation(30, order14));
//        order14.addOperation(new PlanOperation(40, order14));
//        order14.addOperation(new PlanOperation(50, order14));
//        order14.addOperation(new PlanOperation(60, order14));
//        order14.addOperation(new PlanOperation(70, order14));
//        order14.addOperation(new PlanOperation(80, order14));
//        order14.addOperation(new PlanOperation(90, order14));
//        order14.addOperation(new PlanOperation(100, order14));
//
//        order15.addOperation(new PlanOperation(10, order15));
//        order15.addOperation(new PlanOperation(20, order15));
//        order15.addOperation(new PlanOperation(30, order15));
//        order15.addOperation(new PlanOperation(40, order15));
//        order15.addOperation(new PlanOperation(50, order15));
//        order15.addOperation(new PlanOperation(60, order15));
//        order15.addOperation(new PlanOperation(70, order15));
//        order15.addOperation(new PlanOperation(80, order15));
//        order15.addOperation(new PlanOperation(90, order15));
//        order15.addOperation(new PlanOperation(100, order15));
//
//        order16.addOperation(new PlanOperation(10, order16));
//        order16.addOperation(new PlanOperation(20, order16));
//        order16.addOperation(new PlanOperation(30, order16));
//        order16.addOperation(new PlanOperation(40, order16));
//        order16.addOperation(new PlanOperation(50, order16));
//        order16.addOperation(new PlanOperation(60, order16));
//        order16.addOperation(new PlanOperation(70, order16));
//        order16.addOperation(new PlanOperation(80, order16));
//        order16.addOperation(new PlanOperation(90, order16));
//        order16.addOperation(new PlanOperation(100, order16));
//
//        order17.addOperation(new PlanOperation(10, order17));
//        order17.addOperation(new PlanOperation(20, order17));
//        order17.addOperation(new PlanOperation(30, order17));
//        order17.addOperation(new PlanOperation(40, order17));
//        order17.addOperation(new PlanOperation(50, order17));
//        order17.addOperation(new PlanOperation(60, order17));
//        order17.addOperation(new PlanOperation(70, order17));
//        order17.addOperation(new PlanOperation(80, order17));
//        order17.addOperation(new PlanOperation(90, order17));
//        order17.addOperation(new PlanOperation(100, order17));
//
//        order18.addOperation(new PlanOperation(10, order18));
//        order18.addOperation(new PlanOperation(20, order18));
//        order18.addOperation(new PlanOperation(30, order18));
//        order18.addOperation(new PlanOperation(40, order18));
//        order18.addOperation(new PlanOperation(50, order18));
//        order18.addOperation(new PlanOperation(60, order18));
//        order18.addOperation(new PlanOperation(70, order18));
//        order18.addOperation(new PlanOperation(80, order18));
//        order18.addOperation(new PlanOperation(90, order18));
//        order18.addOperation(new PlanOperation(100, order18));
//
//        order19.addOperation(new PlanOperation(10, order19));
//        order19.addOperation(new PlanOperation(20, order19));
//        order19.addOperation(new PlanOperation(30, order19));
//        order19.addOperation(new PlanOperation(40, order19));
//        order19.addOperation(new PlanOperation(50, order19));
//        order19.addOperation(new PlanOperation(60, order19));
//        order19.addOperation(new PlanOperation(70, order19));
//        order19.addOperation(new PlanOperation(80, order19));
//        order19.addOperation(new PlanOperation(90, order19));
//        order19.addOperation(new PlanOperation(100, order19));
//
//        order20.addOperation(new PlanOperation(10, order20));
//        order20.addOperation(new PlanOperation(20, order20));
//        order20.addOperation(new PlanOperation(30, order20));
//        order20.addOperation(new PlanOperation(40, order20));
//        order20.addOperation(new PlanOperation(50, order20));
//        order20.addOperation(new PlanOperation(60, order20));
//        order20.addOperation(new PlanOperation(70, order20));
//        order20.addOperation(new PlanOperation(80, order20));
//        order20.addOperation(new PlanOperation(90, order20));
//        order20.addOperation(new PlanOperation(100, order20));
//
//
//        orders.add(order1);
//        orders.add(order2);
//        orders.add(order3);
//        orders.add(order4);
//        orders.add(order5);
//        orders.add(order6);
//        orders.add(order7);
//        orders.add(order8);
//        /*orders.add(order9);
//        orders.add(order10);
//        orders.add(order11);
//        orders.add(order12);
//        orders.add(order13);
//        orders.add(order14);
//        orders.add(order15);
//        orders.add(order16);
//        orders.add(order17);
//        orders.add(order18);
//        orders.add(order19);
//        orders.add(order20);*/
//        return orders;
//    }
//
//    public static List<Workplace> createWorkplace() {
//        Workplace benchTable1 = new Workplace(1001, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 1);             //слесарная
//        Workplace benchTable2 = new Workplace(1002, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 1);             //слесарная
//        Workplace turningMachine1 = new Workplace(2001, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 1);         // Токарный
//        Workplace turningMachine2 = new Workplace(2002, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 1);         // Токарный
//        Workplace millingMachine1 = new Workplace(3001, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 2);         //Фрезерный
//        Workplace millingMachine2 = new Workplace(3002, 7, LocalDateTime.parse("2020-06-01 00:00:00", ApiHelper.DATE_FORMAT), 2);         //Фрезерный
//
//        //Лист с рабочими местами
//        List<Workplace> workplaces = new ArrayList<>();
//        workplaces.add(benchTable1);
//        workplaces.add(benchTable2);
//        workplaces.add(turningMachine1);
//        workplaces.add(turningMachine2);
//        workplaces.add(millingMachine1);
//        workplaces.add(millingMachine2);
//
//        return workplaces;
//    }
}
