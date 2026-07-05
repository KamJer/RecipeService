package pl.kamjer.ShoppingListRecipeService.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamjer.ShoppingListRecipeService.services.TagService;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/tags")
public class TagController {

    private TagService tagService;

    @GetMapping
    public ResponseEntity<Set<String>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    public ResponseEntity<String> createTag(@RequestBody String tag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tag));
    }

    @PutMapping(path = "/{tag}")
    public ResponseEntity<String> updateTag(@PathVariable String tag, @RequestBody String newTag) {
        return ResponseEntity.ok(tagService.updateTag(tag, newTag));
    }

    @DeleteMapping(path = "/{tag}")
    public ResponseEntity<Void> deleteTag(@PathVariable String tag) {
        tagService.deleteTag(tag);
        return ResponseEntity.noContent().build();
    }
}
