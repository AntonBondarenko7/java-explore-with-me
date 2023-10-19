package ru.practicum.ewm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.ewm.repository.StatsServerRepository;

@DataJpaTest
public class StatsServerRepositoryTest {

    @Autowired
    private StatsServerRepository statsServerRepository;

    @Test
    void getAllStatsWithUris() {
    }

    @Test
    void getAllUniqueStatsWithUris() {
    }

    @Test
    void getAllStats() {
    }

    @Test
    void getAllUniqueStats() {
    }

}
