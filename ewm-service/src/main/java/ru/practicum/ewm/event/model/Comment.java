package ru.practicum.ewm.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime editedOn;

    @Column(nullable = false)
    private CommentStatus status;

}
