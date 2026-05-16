package pl.kamjer.ShoppingListRecipeService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamjer.ShoppingListRecipeService.model.Tag;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Set<Tag> findAllByTagIn(Set<String> names);
}
