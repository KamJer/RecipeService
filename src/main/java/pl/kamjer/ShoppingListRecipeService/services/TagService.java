package pl.kamjer.ShoppingListRecipeService.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class TagService{

    private TagRepository tagRepository;

    public Set<Tag> getAllTags() {
        return new HashSet<>(tagRepository.findAll());
    }
}
