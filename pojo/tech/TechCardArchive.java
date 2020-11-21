package ru.pnppk.mes.pojo.tech;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TechCardArchive {
    private Map<String, List<TechCard>> archive = new ConcurrentHashMap<>();

    public Map<String, List<TechCard>> getArchive() {
        return archive;
    }
}
