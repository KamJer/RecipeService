package pl.kamjer.ShoppingListRecipeService.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.HashSet;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagService{

    private TagRepository tagRepository;

    public Set<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> TagDto.builder().tag(tag.getTag()).build())
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Transactional
    public TagDto createTag(TagDto tagDto) {
        String tagName = tagDto.getTag().trim().toLowerCase(Locale.ROOT);
        if (tagRepository.existsByTag(tagName)) {
            throw new WrongRecipeElementException("Tag '" + tagName + "' already exists");
        }
        Tag saved = tagRepository.save(Tag.builder().tag(tagName).build());
        return TagDto.builder().tag(saved.getTag()).build();
    }

    @Transactional
    public void deleteTag(String tagName) {
        if (!tagRepository.existsById(tagName)) {
            throw new NoSuchElementException("Tag '" + tagName + "' not found");
        }
        tagRepository.deleteById(tagName);
    }

    @Transactional
    public TagDto updateTag(String oldTagName, TagDto newTagDto) {
        String newTagName = newTagDto.getTag().trim().toLowerCase(Locale.ROOT);
        if (!tagRepository.existsById(oldTagName)) {
            throw new NoSuchElementException("Tag '" + oldTagName + "' not found");
        }
        if (!oldTagName.equals(newTagName) && tagRepository.existsByTag(newTagName)) {
            throw new WrongRecipeElementException("Tag '" + newTagName + "' already exists");
        }
        tagRepository.updateTagName(oldTagName, newTagName);
        return TagDto.builder().tag(newTagName).build();
    }
}
