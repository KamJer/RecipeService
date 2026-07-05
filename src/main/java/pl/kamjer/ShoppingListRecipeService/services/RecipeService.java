package pl.kamjer.ShoppingListRecipeService.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.exceptions.WrongRecipeElementException;
import pl.kamjer.ShoppingListRecipeService.model.*;
import pl.kamjer.ShoppingListRecipeService.repository.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Slf4j
public class RecipeService extends CustomService{

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final StepRepository stepRepository;
    private final TagRepository tagRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RecipeService(UserClient secClient, ObjectMapper objectMapper, RecipeRepository recipeRepository, IngredientRepository ingredientRepository, StepRepository stepRepository, TagRepository tagRepository) {
        super(secClient, objectMapper);
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Recipe insertRecipe(Recipe recipe)throws WrongRecipeElementException {
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(() -> new BadCredentialsException("User must be authenticated to insert a recipe"));
        log.info("Inserting recipe '{}' by user '{}'", recipe.getName(), user.getUserName());
        recipe.setRecipeId(null);
        Optional.ofNullable(recipe.getIngredients()).orElse(new ArrayList<>()).forEach(ingredient -> ingredient.setRecipe(recipe));
        Optional.ofNullable(recipe.getSteps()).orElse(new ArrayList<>()).forEach(step -> step.setRecipe(recipe));
        recipe.setUserName(user.getUserName());
        if (recipe.getSource() == null || recipe.getSource().isEmpty()) {
            recipe.setSource(user.getUserName());
        }
        validateTags(recipe);

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void updateRecipe(Recipe recipe) throws IllegalAccessException, WrongRecipeElementException {
        Recipe recipeToUpdate = recipeRepository.findById(recipe.getRecipeId()).orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + recipe.getRecipeId()));
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(() -> new BadCredentialsException("User must be authenticated to update a recipe"));
        log.info("Updating recipe '{}' (id={}) by user '{}'", recipe.getName(), recipe.getRecipeId(), user.getUserName());
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
    public void deleteRecipe(Long id) throws IllegalAccessException {
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(() -> new BadCredentialsException("User must be authenticated to delete a recipe"));
        Recipe recipeToDelete = recipeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + id));
        if (!user.getUserName().equals(recipeToDelete.getUserName())) {
            throw new IllegalAccessException("This recipe does not belong to user: %s, you can not delete it!".formatted(user.getUserName()));
        }
        log.info("Deleting recipe '{}' (id={}) by user '{}'", recipeToDelete.getName(), id, user.getUserName());
        recipeRepository.deleteById(id);
    }

    @Transactional
    public Recipe getRecipeById(Long id) throws NoSuchElementException {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByIdCustom(id, user.getUserName()).orElseThrow(NoSuchElementException::new))
                .orElseGet(() -> recipeRepository.findByIdCustom(id, null).orElse(null));
    }

    @Transactional
    public Page<Recipe> getRecipeByQuery(String query, Pageable pageable) {
        String sanitized = sanitizeBooleanModeTerm(query);
        try {
            return Optional.ofNullable(getUserFromAuth())
                    .map(user -> recipeRepository.searchByNameBoolean(sanitized, user.getUserName(), pageable))
                    .orElseGet(() -> recipeRepository.searchByNameBoolean(sanitized, null, pageable));
        } catch (RuntimeException e) {
            log.warn("BOOLEAN MODE name search failed for query '{}': {}", query, e.getMessage());
            throw new WrongRecipeElementException("Invalid search query. Please avoid special characters like *, +, -, etc.");
        }
    }

    @Transactional
    public Page<Recipe> getRecipeByTags(Set<String> tags, Pageable pageable) {
        Set<String> normalizedTags = tags.stream()
                .map(String::trim)
                .collect(Collectors.toSet());
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByAnyTag(normalizedTags, user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findByAnyTag(normalizedTags, null, pageable));
    }

    @Transactional
    public Page<Recipe> getRecipeByTagsRequired(Set<String> tags, Pageable pageable) {
        Set<String> normalizedTags = tags.stream()
                .map(String::trim)
                .collect(Collectors.toSet());
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findByAllTags(normalizedTags, normalizedTags.size(), user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findByAllTags(normalizedTags, normalizedTags.size(), null, pageable));
    }

    @Transactional
    public Page<Recipe> getAllRecipes(Pageable pageable) {
        return Optional.ofNullable(getUserFromAuth())
                .map(user -> recipeRepository.findAllRecipe(user.getUserName(), pageable))
                .orElseGet(() -> recipeRepository.findAllRecipe(null, pageable));
    }

    public Optional<Recipe> getRecipeByExactName(String name) {
        return recipeRepository.findFirstByNameIgnoreCase(name);
    }

    @Transactional
    public Page<Recipe> getRecipeByUser(Pageable pageable) throws IllegalAccessException {
        User user = Optional.ofNullable(getUserFromAuth()).orElseThrow(IllegalAccessException::new);
        return recipeRepository.findByUserName(user.getUserName(), pageable);
    }

    @Transactional
    public Page<Recipe> getRecipesByIngredientNames(List<String> names, Pageable pageable) {
        if (names == null || names.isEmpty()) {
            return Page.empty();
        }

        String userName = Optional.ofNullable(getUserFromAuth())
                .map(User::getUserName)
                .orElse(null);

        String matchClauses = IntStream.range(0, names.size())
                .mapToObj(i -> "MATCH(i.name) AGAINST (:term" + i + " IN BOOLEAN MODE)")
                .collect(Collectors.joining(" OR "));

        String havingClauses = IntStream.range(0, names.size())
                .mapToObj(i -> "SUM(CASE WHEN MATCH(i.name) AGAINST (:term" + i + " IN BOOLEAN MODE) THEN 1 ELSE 0 END) > 0")
                .collect(Collectors.joining(" AND "));

        String baseSql = """
                FROM recipe r
                JOIN ingredient i ON i.recipe_id = r.recipe_id
                WHERE (r.published = true OR (:userName IS NOT NULL AND r.user_name = :userName))
                  AND (%s)
                GROUP BY r.recipe_id
                HAVING %s
                """.formatted(matchClauses, havingClauses);

        String countSql = "SELECT COUNT(*) FROM (SELECT r.recipe_id " + baseSql + ") AS count_query";
        String dataSql = "SELECT r.* " + baseSql + " ORDER BY r.recipe_id";

        String[] sanitizedTerms = names.stream()
                .map(this::sanitizeBooleanModeTerm)
                .toArray(String[]::new);

        Query countQuery = entityManager.createNativeQuery(countSql);
        Query dataQuery = entityManager.createNativeQuery(dataSql, Recipe.class);

        try {
            for (int i = 0; i < sanitizedTerms.length; i++) {
                countQuery.setParameter("term" + i, sanitizedTerms[i]);
                dataQuery.setParameter("term" + i, sanitizedTerms[i]);
            }
            countQuery.setParameter("userName", userName);
            dataQuery.setParameter("userName", userName);

            long total = ((Number) countQuery.getSingleResult()).longValue();

            dataQuery.setFirstResult((int) pageable.getOffset());
            dataQuery.setMaxResults(pageable.getPageSize());

            @SuppressWarnings("unchecked")
            List<Recipe> recipes = dataQuery.getResultList();

            return new PageImpl<>(recipes, pageable, total);
        } catch (RuntimeException e) {
            log.warn("BOOLEAN MODE ingredient search failed for terms '{}': {}", names, e.getMessage());
            throw new WrongRecipeElementException("Invalid search query. Please avoid special characters like *, +, -, etc.");
        }
    }

    /**
     * Escapes MySQL FULLTEXT BOOLEAN MODE operators and appends "*" suffix.
     * This prevents syntax errors from user input like "*", "+", "-" while letting
     * partial terms (e.g. "mąk") match derived words ("mąka", "mąki", "mąkowy").
     */
    private String sanitizeBooleanModeTerm(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String escaped = input
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("@", "\\@")
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("\"", "\\\"")
                .trim();
        if (escaped.isEmpty()) {
            return escaped;
        }
        return escaped + "*";
    }

    private void validateTags(Recipe recipe) throws WrongRecipeElementException {
        Set<Tag> recipeTags = Optional.ofNullable(recipe.getTags()).orElseGet(HashSet::new);
        if (recipeTags.isEmpty()) {
            return;
        }
        Set<String> tagNames = recipeTags.stream()
                .map(t -> t.getTag().trim())
                .collect(Collectors.toSet());
        Set<Tag> existing = tagRepository.findAllByTagIn(tagNames);
        Set<String> existingLower = existing.stream()
                .map(t -> t.getTag().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> missing = tagNames.stream()
                .filter(t -> !existingLower.contains(t.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Passed tags don't exist, insert correct tags\n");
            missing.forEach(tag -> errorMessage.append(tag).append(" "));
            throw new WrongRecipeElementException(errorMessage.toString());
        }
        recipe.setTags(existing);
    }
}
