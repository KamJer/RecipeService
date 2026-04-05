package pl.kamjer.ShoppingListRecipeService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.ShoppingListRecipeService.model.Step;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
}
