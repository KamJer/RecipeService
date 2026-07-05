package pl.kamjer.ShoppingListRecipeService.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagService{

    private TagRepository tagRepository;

    public Set<String> getAllTags() {
        return tagRepository.findAll().stream()
                .map(Tag::getTag)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Transactional
    public String createTag(String tagName) {
        String trimmed = tagName.trim();
        if (tagRepository.existsByTag(trimmed)) {
            throw new WrongRecipeElementException("Tag '" + trimmed + "' already exists");
        }
        Tag saved = tagRepository.save(Tag.builder().tag(trimmed).build());
        return saved.getTag();
    }

    @Transactional
    public void deleteTag(String tagName) {
        if (!tagRepository.existsById(tagName)) {
            throw new NoSuchElementException("Tag '" + tagName + "' not found");
        }
        tagRepository.deleteById(tagName);
    }

    @Transactional
    public String updateTag(String oldTagName, String newTagName) {
        String trimmed = newTagName.trim();
        if (!tagRepository.existsById(oldTagName)) {
            throw new NoSuchElementException("Tag '" + oldTagName + "' not found");
        }
        if (!oldTagName.equals(trimmed) && !oldTagName.equalsIgnoreCase(trimmed) && tagRepository.existsByTag(trimmed)) {
            throw new WrongRecipeElementException("Tag '" + trimmed + "' already exists");
        }
        tagRepository.updateTagName(oldTagName, trimmed);
        return trimmed;
    }
}
