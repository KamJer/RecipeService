package pl.kamjer.ShoppingListRecipesServics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Step {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stepId;

    private Integer stepNumber;

    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @JsonIgnore
    private Recipe recipe;
}

