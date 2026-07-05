package pl.kamjer.ShoppingListRecipeService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.*;
import pl.kamjer.ShoppingListRecipeService.repository.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    private static final User USER = User.builder().userName("tester").build();
    private static final Pageable PAGEABLE = Pageable.unpaged();

    @Mock private UserClient secClient;
    @Mock private RecipeRepository recipeRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private StepRepository stepRepository;
    @Mock private TagRepository tagRepository;

    private RecipeService service;

    @BeforeEach
    void setUp() {
        service = new RecipeService(secClient, new ObjectMapper(),
                recipeRepository, ingredientRepository, stepRepository, tagRepository);
        service = spy(service);
    }

    // ---- insertRecipe ----

    @Test
    void insertRecipe_whenAuthenticated_setsRecipeIdNullAndUserNameAndSaves() {
        doReturn(USER).when(service).getUserFromAuth();

        Recipe recipe = Recipe.builder()
                .recipeId(99L)
                .name("Pasta")
                .ingredients(List.of(Ingredient.builder().name("Flour").build()))
                .steps(List.of(Step.builder().instruction("Mix").build()))
                .tags(Set.of(Tag.builder().tag("Italian").build()))
                .build();

        when(tagRepository.findAllByTagIn(any())).thenReturn(Set.of(Tag.builder().tag("italian").build()));
        when(recipeRepository.save(any())).then(returnsFirstArg());

        Recipe result = service.insertRecipe(recipe);

        assertThat(recipe.getRecipeId()).isNull();
        assertThat(recipe.getUserName()).isEqualTo("tester");
        assertThat(recipe.getSource()).isEqualTo("tester");
        assertThat(recipe.getIngredients().getFirst().getRecipe()).isSameAs(recipe);
        assertThat(recipe.getSteps().getFirst().getRecipe()).isSameAs(recipe);
        verify(recipeRepository).save(recipe);
        assertThat(result).isSameAs(recipe);
    }

    @Test
    void insertRecipe_whenAuthenticatedWithSource_preservesSource() {
        doReturn(USER).when(service).getUserFromAuth();

        Recipe recipe = Recipe.builder()
                .name("Pasta")
                .source("Cookbook")
                .tags(Set.of())
                .build();

        when(recipeRepository.save(any())).then(returnsFirstArg());

        service.insertRecipe(recipe);

        assertThat(recipe.getSource()).isEqualTo("Cookbook");
    }

    @Test
    void insertRecipe_whenNotAuthenticated_throwsBadCredentials() {
        doReturn(null).when(service).getUserFromAuth();

        Recipe recipe = Recipe.builder().name("Pasta").build();

        assertThatThrownBy(() -> service.insertRecipe(recipe))
                .isInstanceOf(BadCredentialsException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void insertRecipe_whenTagsMissing_throwsWrongRecipeElementException() {
        doReturn(USER).when(service).getUserFromAuth();

        Recipe recipe = Recipe.builder()
                .name("Pasta")
                .tags(Set.of(Tag.builder().tag("Nonexistent").build()))
                .build();

        when(tagRepository.findAllByTagIn(any())).thenReturn(Set.of());

        assertThatThrownBy(() -> service.insertRecipe(recipe))
                .isInstanceOf(WrongRecipeElementException.class)
                .hasMessageContaining("Passed tags don't exist");
        verify(recipeRepository, never()).save(any());
    }

    // ---- updateRecipe ----

    @Test
    void updateRecipe_whenAuthenticatedAndOwner_updatesIngredientsStepsAndFields() throws IllegalAccessException {
        Recipe recipeToUpdate = Recipe.builder()
                .recipeId(10L)
                .userName("tester")
                .name("OldName")
                .description("OldDesc")
                .published(false)
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .tags(new HashSet<>())
                .build();

        Ingredient existingIngredientInDb = Ingredient.builder()
                .ingredientId(5L)
                .name("OldSalt")
                .unit("tsp")
                .quantity(1.0)
                .recipe(recipeToUpdate)
                .build();
        recipeToUpdate.getIngredients().add(existingIngredientInDb);

        Step existingStepInDb = Step.builder()
                .stepId(3L)
                .stepNumber(1)
                .instruction("OldInstruction")
                .recipe(recipeToUpdate)
                .build();
        recipeToUpdate.getSteps().add(existingStepInDb);

        Ingredient updatedIngredient = Ingredient.builder()
                .ingredientId(5L)
                .name("NewSalt")
                .unit("tbsp")
                .quantity(2.0)
                .build();

        Ingredient newIngredient = Ingredient.builder()
                .name("Sugar")
                .unit("cup")
                .quantity(1.0)
                .build();

        Step updatedStep = Step.builder()
                .stepId(3L)
                .stepNumber(2)
                .instruction("NewInstruction")
                .build();

        Step newStep = Step.builder()
                .stepNumber(3)
                .instruction("Serve")
                .build();

        Recipe inputRecipe = Recipe.builder()
                .recipeId(10L)
                .name("NewName")
                .description("NewDesc")
                .published(true)
                .ingredients(List.of(updatedIngredient, newIngredient))
                .steps(List.of(updatedStep, newStep))
                .tags(Set.of(Tag.builder().tag("Updated").build()))
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToUpdate));
        doReturn(USER).when(service).getUserFromAuth();
        when(tagRepository.findAllByTagIn(any())).thenReturn(Set.of(Tag.builder().tag("updated").build()));
        when(ingredientRepository.findById(5L)).thenReturn(Optional.of(existingIngredientInDb));
        when(stepRepository.findById(3L)).thenReturn(Optional.of(existingStepInDb));

        service.updateRecipe(inputRecipe);

        assertThat(recipeToUpdate.getName()).isEqualTo("NewName");
        assertThat(recipeToUpdate.getDescription()).isEqualTo("NewDesc");
        assertThat(recipeToUpdate.getPublished()).isTrue();
        assertThat(recipeToUpdate.getIngredients()).hasSize(2);
        assertThat(existingIngredientInDb.getName()).isEqualTo("NewSalt");
        assertThat(existingIngredientInDb.getUnit()).isEqualTo("tbsp");
        assertThat(existingIngredientInDb.getQuantity()).isEqualTo(2.0);
        assertThat(newIngredient.getRecipe()).isSameAs(recipeToUpdate);
        assertThat(recipeToUpdate.getSteps()).hasSize(2);
        assertThat(existingStepInDb.getStepNumber()).isEqualTo(2);
        assertThat(existingStepInDb.getInstruction()).isEqualTo("NewInstruction");
        assertThat(newStep.getRecipe()).isSameAs(recipeToUpdate);
    }

    @Test
    void updateRecipe_whenNotAuthenticated_throwsBadCredentials() {
        Recipe recipe = Recipe.builder().recipeId(1L).build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(Recipe.builder().build()));
        doReturn(null).when(service).getUserFromAuth();

        assertThatThrownBy(() -> service.updateRecipe(recipe))
                .isInstanceOf(BadCredentialsException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void updateRecipe_whenRecipeNotFound_throwsNoSuchElementException() {
        when(recipeRepository.findById(anyLong())).thenReturn(Optional.empty());

        Recipe recipe = Recipe.builder().recipeId(999L).build();

        assertThatThrownBy(() -> service.updateRecipe(recipe))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateRecipe_whenNotOwner_throwsIllegalAccessException() {
        Recipe recipeToUpdate = Recipe.builder()
                .recipeId(10L)
                .userName("other")
                .build();
        Recipe inputRecipe = Recipe.builder().recipeId(10L).build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToUpdate));
        doReturn(USER).when(service).getUserFromAuth();

        assertThatThrownBy(() -> service.updateRecipe(inputRecipe))
                .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    void updateRecipe_whenIngredientBelongsToWrongRecipe_throwsWrongRecipeElementException() {
        Recipe recipeToUpdate = Recipe.builder()
                .recipeId(10L)
                .userName("tester")
                .ingredients(new ArrayList<>())
                .build();

        Recipe wrongRecipe = Recipe.builder().recipeId(999L).build();
        Ingredient wrongIngredientInDb = Ingredient.builder()
                .ingredientId(5L)
                .recipe(wrongRecipe)
                .build();

        Ingredient inputIngredient = Ingredient.builder()
                .ingredientId(5L)
                .name("Test")
                .build();

        Recipe inputRecipe = Recipe.builder()
                .recipeId(10L)
                .ingredients(List.of(inputIngredient))
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToUpdate));
        doReturn(USER).when(service).getUserFromAuth();
        when(ingredientRepository.findById(5L)).thenReturn(Optional.of(wrongIngredientInDb));

        assertThatThrownBy(() -> service.updateRecipe(inputRecipe))
                .isInstanceOf(WrongRecipeElementException.class)
                .hasMessageContaining("ingredient");
    }

    @Test
    void updateRecipe_whenStepBelongsToWrongRecipe_throwsWrongRecipeElementException() {
        Recipe recipeToUpdate = Recipe.builder()
                .recipeId(10L)
                .userName("tester")
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .build();

        Recipe wrongRecipe = Recipe.builder().recipeId(999L).build();
        Step wrongStepInDb = Step.builder()
                .stepId(3L)
                .recipe(wrongRecipe)
                .build();

        Step inputStep = Step.builder()
                .stepId(3L)
                .instruction("Test")
                .build();

        Recipe inputRecipe = Recipe.builder()
                .recipeId(10L)
                .ingredients(List.of())
                .steps(List.of(inputStep))
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToUpdate));
        doReturn(USER).when(service).getUserFromAuth();
        when(stepRepository.findById(3L)).thenReturn(Optional.of(wrongStepInDb));

        assertThatThrownBy(() -> service.updateRecipe(inputRecipe))
                .isInstanceOf(WrongRecipeElementException.class)
                .hasMessageContaining("step");
    }

    // ---- deleteRecipe ----

    @Test
    void deleteRecipe_whenAuthenticatedAndOwner_deletesRecipe() throws IllegalAccessException {
        Recipe recipeToDelete = Recipe.builder()
                .recipeId(10L)
                .userName("tester")
                .build();

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToDelete));
        doReturn(USER).when(service).getUserFromAuth();

        service.deleteRecipe(10L);

        verify(recipeRepository).deleteById(10L);
    }

    @Test
    void deleteRecipe_whenNotAuthenticated_throwsBadCredentials() {
        doReturn(null).when(service).getUserFromAuth();

        assertThatThrownBy(() -> service.deleteRecipe(1L))
                .isInstanceOf(BadCredentialsException.class);
        verify(recipeRepository, never()).deleteById(any());
    }

    @Test
    void deleteRecipe_whenRecipeNotFound_throwsNoSuchElementException() {
        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteRecipe(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deleteRecipe_whenNotOwner_throwsIllegalAccessException() {
        Recipe recipeToDelete = Recipe.builder()
                .recipeId(10L)
                .userName("other")
                .build();

        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipeToDelete));

        assertThatThrownBy(() -> service.deleteRecipe(10L))
                .isInstanceOf(IllegalAccessException.class);
        verify(recipeRepository, never()).deleteById(any());
    }

    // ---- getRecipeById ----

    @Test
    void getRecipeById_whenAuthenticated_usesUserScopedQuery() {
        Recipe found = Recipe.builder().recipeId(1L).name("Pasta").build();

        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findByIdCustom(1L, "tester")).thenReturn(Optional.of(found));

        Recipe result = service.getRecipeById(1L);

        assertThat(result).isSameAs(found);
        verify(recipeRepository).findByIdCustom(1L, "tester");
    }

    @Test
    void getRecipeById_whenNotAuthenticated_usesPublicQuery() {
        Recipe found = Recipe.builder().recipeId(1L).name("Pasta").build();

        doReturn(null).when(service).getUserFromAuth();
        when(recipeRepository.findByIdCustom(1L, null)).thenReturn(Optional.of(found));

        Recipe result = service.getRecipeById(1L);

        assertThat(result).isSameAs(found);
        verify(recipeRepository).findByIdCustom(1L, null);
    }

    // ---- getRecipeByUser ----

    @Test
    void getRecipeByUser_whenAuthenticated_returnsUserRecipes() throws IllegalAccessException {
        Page<Recipe> page = Page.empty();
        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findByUserName("tester", PAGEABLE)).thenReturn(page);

        Page<Recipe> result = service.getRecipeByUser(PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getRecipeByUser_whenNotAuthenticated_throwsIllegalAccessException() {
        doReturn(null).when(service).getUserFromAuth();

        assertThatThrownBy(() -> service.getRecipeByUser(PAGEABLE))
                .isInstanceOf(IllegalAccessException.class);
        verify(recipeRepository, never()).findByUserName(any(), any());
    }

    // ---- getAllRecipes ----

    @Test
    void getAllRecipes_whenAuthenticated_passesUserName() {
        Page<Recipe> page = Page.empty();
        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findAllRecipe("tester", PAGEABLE)).thenReturn(page);

        Page<Recipe> result = service.getAllRecipes(PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getAllRecipes_whenNotAuthenticated_passesNull() {
        Page<Recipe> page = Page.empty();
        doReturn(null).when(service).getUserFromAuth();
        when(recipeRepository.findAllRecipe(null, PAGEABLE)).thenReturn(page);

        Page<Recipe> result = service.getAllRecipes(PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    // ---- getRecipeByQuery ----

    @Test
    void getRecipeByQuery_whenAuthenticated_passesUserName() {
        Page<Recipe> page = Page.empty();
        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.searchByNameBoolean(eq("pasta*"), eq("tester"), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByQuery("pasta", PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getRecipeByQuery_whenNotAuthenticated_passesNull() {
        Page<Recipe> page = Page.empty();
        doReturn(null).when(service).getUserFromAuth();
        when(recipeRepository.searchByNameBoolean(eq("pasta*"), isNull(), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByQuery("pasta", PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    // ---- getRecipeByTags ----

    @Test
    void getRecipeByTags_whenAuthenticated_passesUserName() {
        Page<Recipe> page = Page.empty();
        Set<String> tags = Set.of("Italian");

        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findByAnyTag(anySet(), eq("tester"), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByTags(tags, PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getRecipeByTags_whenNotAuthenticated_passesNull() {
        Page<Recipe> page = Page.empty();
        Set<String> tags = Set.of("Italian");

        doReturn(null).when(service).getUserFromAuth();
        when(recipeRepository.findByAnyTag(anySet(), isNull(), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByTags(tags, PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    // ---- getRecipeByTagsRequired ----

    @Test
    void getRecipeByTagsRequired_whenAuthenticated_passesUserName() {
        Page<Recipe> page = Page.empty();
        Set<String> tags = Set.of("Italian", "Dessert");

        doReturn(USER).when(service).getUserFromAuth();
        when(recipeRepository.findByAllTags(anySet(), eq(2L), eq("tester"), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByTagsRequired(tags, PAGEABLE);

        assertThat(result).isSameAs(page);
    }

    @Test
    void getRecipeByTagsRequired_whenNotAuthenticated_passesNull() {
        Page<Recipe> page = Page.empty();
        Set<String> tags = Set.of("Italian");

        doReturn(null).when(service).getUserFromAuth();
        when(recipeRepository.findByAllTags(anySet(), eq(1L), isNull(), eq(PAGEABLE))).thenReturn(page);

        Page<Recipe> result = service.getRecipeByTagsRequired(tags, PAGEABLE);

        assertThat(result).isSameAs(page);
    }

}
