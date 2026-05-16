package pl.kamjer.ShoppingListRecipeService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.*;
import pl.kamjer.ShoppingListRecipeService.repository.*;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class RecipeService extends CustomService{

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final StepRepository stepRepository;
    private final TagRepository tagRepository;

    public RecipeService(UserClient secClient, ObjectMapper objectMapper, RecipeRepository recipeRepository, IngredientRepository ingredientRepository, StepRepository stepRepository, TagRepository tagRepository) {
        super(secClient, objectMapper);
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Recipe insertRecipe(Recipe recipe)throws WrongRecipeElementException {
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(() -> new BadCredentialsException(""));
        recipe.getIngredients().forEach(ingredient -> ingredient.setRecipe(recipe));
        recipe.getSteps().forEach(step -> step.setRecipe(recipe));
        recipe.setUserName(user.getUserName());
        if (recipe.getSource() == null || recipe.getSource().isEmpty()) {
            recipe.setSource(user.getUserName());
        }
        validateTags(recipe);

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void updateRecipe(Recipe recipe) throws IllegalAccessException, WrongRecipeElementException {
        Recipe recipeToUpdate = recipeRepository.findById(recipe.getRecipeId()).orElseThrow(NoSuchElementException::new);
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(() -> new BadCredentialsException(""));
        if (!recipeToUpdate.getUserName().equals(user.getUserName())) {
            throw new IllegalAccessException("This recipe does not belong to this user: %s, you can not update it!".formatted(user.getUserName()));
        }
        validateTags(recipe);
        Map<Boolean, List<Ingredient>> partIngredientList = recipe.getIngredients().stream()
                .collect(Collectors.partitioningBy(o -> o.getIngredientId() == null));

        List<Ingredient> newIngredients = partIngredientList.get(true).stream().peek(ingredient -> ingredient.setRecipe(recipeToUpdate)).toList();
        List<Ingredient> ingredientsToUpdate = new java.util.ArrayList<>(partIngredientList.get(false).stream().map(ingredient -> {
            Ingredient ingredientToUpdate = ingredientRepository.findById(ingredient.getIngredientId()).orElseThrow(NoSuchElementException::new);
            if (!Objects.equals(ingredientToUpdate.getRecipe().getRecipeId(), recipeToUpdate.getRecipeId())) {
                throw new WrongRecipeElementException("Wrong ingredient, it does not belong to this recipe");
            }
            ingredientToUpdate.setUnit(ingredient.getUnit());
            ingredientToUpdate.setQuantity(ingredient.getQuantity());
            ingredientToUpdate.setName(ingredient.getName());
            return ingredientToUpdate;
        }).toList());
        ingredientsToUpdate.addAll(newIngredients);
        recipeToUpdate.getIngredients().clear();
        recipeToUpdate.getIngredients().addAll(ingredientsToUpdate);

        Map<Boolean, List<Step>> partStepList = recipe.getSteps().stream().collect(Collectors.partitioningBy(o -> o.getStepId() == null));
        List<Step> steps = partStepList.get(true).stream().peek(step -> step.setRecipe(recipeToUpdate)).toList();
        List<Step> stepsToUpdate = new java.util.ArrayList<>(partStepList.get(false).stream().map(step -> {
            Step stepToUpdate = stepRepository.findById(step.getStepId()).orElseThrow(NoSuchElementException::new);
            if (!Objects.equals(stepToUpdate.getRecipe().getRecipeId(), recipeToUpdate.getRecipeId())) {
                throw new WrongRecipeElementException("Wrong step, it does not belong to this recipe");
            }
            stepToUpdate.setStepNumber(step.getStepNumber());
            stepToUpdate.setInstruction(step.getInstruction());
            return stepToUpdate;
        }).toList());
        stepsToUpdate.addAll(steps);
        recipeToUpdate.getSteps().clear();
        recipeToUpdate.getSteps().addAll(stepsToUpdate);

        recipeToUpdate.setDescription(recipe.getDescription());
        recipeToUpdate.setName(recipe.getName());
        recipeToUpdate.setPublished(recipe.getPublished());
        recipeToUpdate.setTags(recipe.getTags());
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Optional.ofNullable(getUserFromAuth()).ifPresent(user -> {
            Recipe recipeToDelete = recipeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Such recipe does not exists"));
            if (user.getUserName().equals(recipeToDelete.getUserName())) {
                recipeRepository.deleteById(id);
            }
        });
    }

    @Transactional
    public Recipe getRecipeById(Long id) throws NoSuchElementException {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByIdCustom(id, user.getUserName()).orElseThrow(NoSuchElementException::new))
                .orElseGet(() -> recipeRepository.findByIdCustom(id, null).orElse(null));
    }

    @Transactional
    public Page<Recipe> getRecipeByProducts(List<String> products, int maxMissing, Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findCookableRecipes(products, maxMissing, user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findCookableRecipes(products, maxMissing, null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByProductsRequired(List<String> products, Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findRecipesContainingAllIngredients(products, user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findRecipesContainingAllIngredients(products, null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByQuery(String query, Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.searchByNameBoolean(query, user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.searchByNameBoolean(query, null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByTags(Set<Tag> tags, Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByAnyTag(tags.stream().map(Tag::getTag).collect(Collectors.toSet()), user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findByAnyTag(tags.stream().map(Tag::getTag).collect(Collectors.toSet()), null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByTagsRequired(Set<Tag> tags, Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByAllTags(tags.stream().map(Tag::getTag).collect(Collectors.toSet()), tags.size(), user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findByAllTags(tags.stream().map(Tag::getTag).collect(Collectors.toSet()), tags.size(), null, pageable));
    }

    @Transactional
    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findAllRecipe(user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findAllRecipe(null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByUser(Pageable pageable) throws IllegalAccessException {
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(IllegalAccessException::new);
        return recipeRepository.findByUserName(user.getUserName(), pageable);
    }

    private void validateTags(Recipe recipe) throws WrongRecipeElementException {
        Set<Tag> missingTags = findMissingTags(Optional.ofNullable(recipe.getTags()).orElseGet(HashSet::new));
        //        TODO: make proper validation
        if (!missingTags.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Passed tags don't exist, insert correct tags").append("\n");
            missingTags.forEach(tag -> errorMessage.append(tag.getTag()).append(" "));
            throw new WrongRecipeElementException(errorMessage.toString());
        }
    }

    private Set<Tag> findMissingTags(Set<Tag> tagsToCheck) {
        Set<Tag> normalized = tagsToCheck.stream()
                .map(t -> Tag.builder().tag(t
                        .getTag().trim().toLowerCase(Locale.ROOT)).build())
                .collect(Collectors.toSet());
        Set<Tag> existing = tagRepository.findAllByTagIn(normalized.stream().map(Tag::getTag).collect(Collectors.toSet()));
        normalized.removeAll(existing);
        return normalized;
    }
}
