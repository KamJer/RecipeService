package pl.kamjer.ShoppingListRecipesServics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Tag {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String tag;
}
