package ru.practicum.ewmservice.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId);

    List<Comment> findAllByAuthorIdAndEventId(Long userId, Long eventId);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);
}
