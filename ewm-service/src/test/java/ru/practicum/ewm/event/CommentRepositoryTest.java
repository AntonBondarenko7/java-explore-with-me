package ru.practicum.ewm.event;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class CommentRepositoryTest {

    @Test
    void testFindAllByEventIdAndStatusOrderByCreatedOnDesc() {}

    @Test
    void testFindAllByStatusOrderByCreatedOnAsc() {}

    @Test
    void testDeleteByIdWithReturnedLines() {}

}
