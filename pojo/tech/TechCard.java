package ru.pnppk.mes.pojo.tech;

import java.util.List;
import java.util.Objects;

public class TechCard {
    private String cipher;                           //шифр детали
    private String name;
    private int group;                               //группа техкарт (R3)
    private int groupCounter;                        //счетчик группы (R3)
    private List<TechOperation> techOperations;      //лист техопеарций операций

    public TechCard(String cipher, String name, List<TechOperation> techOperations, int group, int groupCounter) {
        this.cipher = cipher;
        this.name = name;
        this.techOperations = techOperations;
        this.group = group;
        this.groupCounter = groupCounter;
    }
    public String getCipher() {
        return cipher;
    }

    public String getName() {
        return name;
    }

    public List<TechOperation> getTechOperations() {
        return techOperations;
    }

    public int getGroup() {
        return group;
    }

    public int getGroupCounter() {
        return groupCounter;
    }

    public TechOperation getTechOperationByNumber(int number) {
        for (TechOperation techOperation : techOperations) {
             if (techOperation.getNumber() == number) return techOperation;
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TechCard)) return false;
        TechCard techCard = (TechCard) o;
        return group == techCard.group &&
                groupCounter == techCard.groupCounter &&
                Objects.equals(cipher, techCard.cipher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cipher, group, groupCounter);
    }
}
