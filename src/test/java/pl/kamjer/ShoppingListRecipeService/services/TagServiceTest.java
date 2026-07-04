package pl.kamjer.ShoppingListRecipeService.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.model.dto.TagDto;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
        Tag italian = Tag.builder().tag("italian").build();
        Tag dessert = Tag.builder().tag("dessert").build();
        when(tagRepository.findAll()).thenReturn(List.of(italian, dessert));

        Set<TagDto> result = service.getAllTags();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TagDto::getTag)
                .containsExactlyInAnyOrder("italian", "dessert");
    }

    @Test
    void getAllTags_whenEmpty_returnsEmptySet() {
        when(tagRepository.findAll()).thenReturn(List.of());

        Set<TagDto> result = service.getAllTags();

        assertThat(result).isEmpty();
    }

    @Test
    void createTag_createsAndReturnsNewTag() {
        TagDto input = TagDto.builder().tag("  Italian ").build();
        when(tagRepository.existsByTag("Italian")).thenReturn(false);
        when(tagRepository.save(any())).thenReturn(Tag.builder().tag("Italian").build());

        TagDto result = service.createTag(input);

        assertThat(result.getTag()).isEqualTo("Italian");
        verify(tagRepository).save(Tag.builder().tag("Italian").build());
    }

    @Test
    void createTag_whenAlreadyExists_throws() {
        when(tagRepository.existsByTag("Italian")).thenReturn(true);

        TagDto input = TagDto.builder().tag("Italian").build();

        assertThatThrownBy(() -> service.createTag(input))
                .isInstanceOf(WrongRecipeElementException.class)
                .hasMessageContaining("already exists");
        verify(tagRepository, never()).save(any());
    }

    @Test
    void deleteTag_deletesExistingTag() {
        when(tagRepository.existsById("italian")).thenReturn(true);

        service.deleteTag("italian");

        verify(tagRepository).deleteById("italian");
    }

    @Test
    void deleteTag_whenNotFound_throws() {
        when(tagRepository.existsById("nonexistent")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTag("nonexistent"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("nonexistent");
        verify(tagRepository, never()).deleteById(anyString());
    }

    @Test
    void updateTag_updatesAndReturnsNewTag() {
        TagDto input = TagDto.builder().tag("  Italian ").build();
        when(tagRepository.existsById("old")).thenReturn(true);
        when(tagRepository.existsByTag("Italian")).thenReturn(false);

        TagDto result = service.updateTag("old", input);

        assertThat(result.getTag()).isEqualTo("Italian");
        verify(tagRepository).updateTagName("old", "Italian");
    }

    @Test
    void updateTag_whenOldNotFound_throws() {
        when(tagRepository.existsById("nonexistent")).thenReturn(false);

        TagDto input = TagDto.builder().tag("new").build();

        assertThatThrownBy(() -> service.updateTag("nonexistent", input))
                .isInstanceOf(NoSuchElementException.class);
        verify(tagRepository, never()).updateTagName(anyString(), anyString());
    }

    @Test
    void updateTag_whenNewNameAlreadyExists_throws() {
        when(tagRepository.existsById("old")).thenReturn(true);
        when(tagRepository.existsByTag("existing")).thenReturn(true);

        TagDto input = TagDto.builder().tag("existing").build();

        assertThatThrownBy(() -> service.updateTag("old", input))
                .isInstanceOf(WrongRecipeElementException.class)
                .hasMessageContaining("already exists");
        verify(tagRepository, never()).updateTagName(anyString(), anyString());
    }

    @Test
    void updateTag_whenSameName_doesNotCheckDuplicate() {
        TagDto input = TagDto.builder().tag(" same ").build();
        when(tagRepository.existsById("same")).thenReturn(true);

        service.updateTag("same", input);

        verify(tagRepository, never()).existsByTag(anyString());
        verify(tagRepository).updateTagName("same", "same");
    }
}
