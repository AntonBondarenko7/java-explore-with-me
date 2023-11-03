package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Comment;
import ru.practicum.ewm.event.model.CommentStatus;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c " +
            "from Comment c " +
            "where c.eventId = ?1 " +
            "AND (status = 'MODERATED' " +
            "or status is null) " +
            "order by c.createdOn desc ")
    List<Comment> findAllByEventIdAndStatusOrderByCreatedOnDesc(Long eventId);

    List<Comment> findAllByStatusOrderByCreatedOnAsc(CommentStatus status);

    @Modifying
    @Query("delete from Comment c where c.id = ?1")
    Integer deleteByIdWithReturnedLines(Long commentId);

}
