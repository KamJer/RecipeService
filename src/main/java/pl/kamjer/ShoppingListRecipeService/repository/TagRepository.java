package pl.kamjer.ShoppingListRecipeService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamjer.ShoppingListRecipeService.model.Tag;

import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, String> {
    Set<Tag> findAllByTagIn(Set<String> names);

    boolean existsByTag(String tag);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Tag t SET t.tag = :newTag WHERE t.tag = :oldTag")
    int updateTagName(@Param("oldTag") String oldTag, @Param("newTag") String newTag);
}
