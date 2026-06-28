package pl.kamjer.ShoppingListRecipeService.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.ShoppingListRecipeService.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipeService.services.TagService;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/tags")
public class TagController {

    private TagService tagService;

    @GetMapping
    public ResponseEntity<Set<TagDto>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody TagDto tagDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tagDto));
    }

    @PutMapping(path = "/{tag}")
    public ResponseEntity<TagDto> updateTag(@PathVariable String tag, @Valid @RequestBody TagDto tagDto) {
        return ResponseEntity.ok(tagService.updateTag(tag, tagDto));
    }

    @DeleteMapping(path = "/{tag}")
    public ResponseEntity<Void> deleteTag(@PathVariable String tag) {
        tagService.deleteTag(tag);
        return ResponseEntity.noContent().build();
    }
}
