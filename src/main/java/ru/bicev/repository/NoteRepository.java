package ru.bicev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.bicev.notes.entity.Note;
import ru.bicev.notes.entity.User;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUser(User user);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t = :tag")
    List<Note> findByTag(@Param("tag") String tag);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t LIKE %:tagPart%")
    List<Note> findByTagPart(@Param("tagPart") String tagPart);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t IN :tags")
    List<Note> findByAnyTags(@Param("tags") List<String> tags);

    @Query("""
                SELECT n FROM Note n JOIN n.tags t
                WHERE t IN :tags
                GROUP BY n
                HAVING COUNT(DISTINCT t) = :tagCount
            """)
    List<Note> findByAllTags(@Param("tags") List<String> tags, @Param("tagCount") long tagCount);

}
