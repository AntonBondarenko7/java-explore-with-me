package ru.practicum.ewm.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.StatsClient;

@Component
public class Stats {

    public final StatsClient statsClient;

    @Autowired
    public Stats(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

}
