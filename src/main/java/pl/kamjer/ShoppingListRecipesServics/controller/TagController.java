package pl.kamjer.ShoppingListRecipesServics.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kamjer.ShoppingListRecipesServics.model.Tag;
import pl.kamjer.ShoppingListRecipesServics.services.TagService;

import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/tags")
public class TagController {

    private TagService tagService;

    @GetMapping
    public ResponseEntity<Set<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }
}
