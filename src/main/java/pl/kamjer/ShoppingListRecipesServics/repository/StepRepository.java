package pl.kamjer.ShoppingListRecipesServics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kamjer.ShoppingListRecipesServics.model.Step;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
}
