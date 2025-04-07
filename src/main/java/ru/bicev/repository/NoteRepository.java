package ru.bicev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.bicev.notes.entity.Note;
import ru.bicev.notes.entity.User;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUser(User user);

    Optional<Note> findByIdAndUser(Long id, User user);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t LIKE %:tagPart% AND n.user = :user")
    List<Note> findByTagPart(@Param("tagPart") String tagPart, @Param("user") User user);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t IN :tags AND n.user = :user")
    List<Note> findByAnyTags(@Param("tags") List<String> tags, @Param("user") User user);

    @Query("""
                SELECT n FROM Note n JOIN n.tags t
                WHERE t IN :tags AND n.user = :user
                GROUP BY n
                HAVING COUNT(DISTINCT t) = :tagCount
            """)
    List<Note> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount,
            @Param("user") User user);

}
