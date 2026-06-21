package pl.kamjer.ShoppingListRecipeService.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService service;

    @BeforeEach
    void setUp() {
        service = new TagService(tagRepository);
    }

    @Test
    void getAllTags_returnsAllTags() {
        Tag italian = Tag.builder().tag("Italian").build();
        Tag dessert = Tag.builder().tag("Dessert").build();
        when(tagRepository.findAll()).thenReturn(List.of(italian, dessert));

        Set<Tag> result = service.getAllTags();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(italian, dessert);
    }

    @Test
    void getAllTags_whenEmpty_returnsEmptySet() {
        when(tagRepository.findAll()).thenReturn(List.of());

        Set<Tag> result = service.getAllTags();

        assertThat(result).isEmpty();
    }
}
