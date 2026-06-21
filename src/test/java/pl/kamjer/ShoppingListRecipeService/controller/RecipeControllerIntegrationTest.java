package pl.kamjer.ShoppingListRecipeService.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.config.TestSecurityConfig;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.model.dto.*;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RecipeControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TagRepository tagRepository;

    @MockitoBean
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        when(userClient.getUserByUserName(anyString(), anyString()))
                .thenReturn(UserDto.builder().userName("tester").build());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("tester", null, List.of());
        auth.setDetails("test-token");
        SecurityContextHolder.getContext().setAuthentication(auth);

        tagRepository.save(Tag.builder().tag("italian").build());
        tagRepository.save(Tag.builder().tag("dessert").build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        tagRepository.deleteAll();
    }

    @Test
    void putRecipe_createsAndReturnsRecipe() {
        RecipeDto dto = RecipeDto.builder()
                .name("Pasta Carbonara")
                .description("Classic Italian pasta")
                .ingredients(List.of(
                        IngredientDto.builder().name("Pasta").quantity(200.0).unit("g").build(),
                        IngredientDto.builder().name("Eggs").quantity(2.0).unit("pcs").build()
                ))
                .steps(List.of(
                        StepDto.builder().stepNumber(1).instruction("Boil pasta").build(),
                        StepDto.builder().stepNumber(2).instruction("Mix eggs").build()
                ))
                .tags(List.of(TagDto.builder().tag("italian").build()))
                .published(true)
                .build();

        ResponseEntity<RecipeDto> response = restTemplate.exchange(
                "/recipe", HttpMethod.PUT, new HttpEntity<>(dto), RecipeDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Pasta Carbonara");
        assertThat(response.getBody().getRecipeId()).isPositive();
        assertThat(response.getBody().getIngredients()).hasSize(2);
        assertThat(response.getBody().getSteps()).hasSize(2);
        assertThat(response.getBody().getUserName()).isEqualTo("tester");
        assertThat(response.getBody().getSource()).isEqualTo("tester");
    }

    @Test
    void putRecipe_withEmptyName_returns400() {
        RecipeDto dto = RecipeDto.builder().name("").build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/recipe", HttpMethod.PUT, new HttpEntity<>(dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void putRecipe_withMissingTags_returns409() {
        RecipeDto dto = RecipeDto.builder()
                .name("Test")
                .tags(List.of(TagDto.builder().tag("nonexistent").build()))
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/recipe", HttpMethod.PUT, new HttpEntity<>(dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Passed tags don't exist");
    }

    @Test
    void putRecipe_whenNotAuthenticated_returns401() {
        SecurityContextHolder.clearContext();

        RecipeDto dto = RecipeDto.builder().name("Test").build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/recipe", HttpMethod.PUT, new HttpEntity<>(dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postRecipe_updatesExistingRecipe() {
        RecipeDto created = restTemplate.exchange(
                "/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder().name("Original").tags(List.of()).build()),
                RecipeDto.class).getBody();
        Long recipeId = created.getRecipeId();

        RecipeDto update = RecipeDto.builder()
                .recipeId(recipeId)
                .name("Updated")
                .description("New description")
                .ingredients(List.of(IngredientDto.builder().name("Salt").build()))
                .steps(List.of(StepDto.builder().stepNumber(1).instruction("Season").build()))
                .tags(List.of(TagDto.builder().tag("italian").build()))
                .published(true)
                .build();

        ResponseEntity<Boolean> response = restTemplate.exchange(
                "/recipe", HttpMethod.POST, new HttpEntity<>(update), Boolean.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();

        RecipeDto fetched = restTemplate.exchange(
                "/recipe/id/" + recipeId, HttpMethod.GET, null, RecipeDto.class).getBody();
        assertThat(fetched.getName()).isEqualTo("Updated");
        assertThat(fetched.getDescription()).isEqualTo("New description");
        assertThat(fetched.getIngredients()).hasSize(1);
        assertThat(fetched.getSteps()).hasSize(1);
    }

    @Test
    void deleteRecipe_removesRecipe() {
        RecipeDto created = restTemplate.exchange(
                "/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder().name("ToDelete").tags(List.of()).build()),
                RecipeDto.class).getBody();
        Long recipeId = created.getRecipeId();

        ResponseEntity<Boolean> deleteResponse = restTemplate.exchange(
                "/recipe/" + recipeId, HttpMethod.DELETE, null, Boolean.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isTrue();

        ResponseEntity<String> getResponse = restTemplate.exchange(
                "/recipe/id/" + recipeId, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getRecipeById_returnsRecipe() {
        RecipeDto created = restTemplate.exchange(
                "/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder().name("FindMe").tags(List.of()).build()),
                RecipeDto.class).getBody();
        Long recipeId = created.getRecipeId();

        ResponseEntity<RecipeDto> response = restTemplate.exchange(
                "/recipe/id/" + recipeId, HttpMethod.GET, null, RecipeDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("FindMe");
        assertThat(response.getBody().getRecipeId()).isEqualTo(recipeId);
    }

    @Test
    void getRecipeById_notFound_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/recipe/id/99999", HttpMethod.GET, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllRecipes_returnsAllRecipes() {
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder().name("Recipe A").tags(List.of()).build()),
                RecipeDto.class);
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder().name("Recipe B").tags(List.of()).build()),
                RecipeDto.class);

        ResponseEntity<Page<RecipeDto>> response = restTemplate.exchange(
                "/recipe", HttpMethod.GET, null,
                new ParameterizedTypeReference<Page<RecipeDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
    }

    @Test
    void getRecipeByTags_returnsMatchingRecipes() {
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder()
                        .name("Italian Dish").tags(List.of(TagDto.builder().tag("italian").build())).build()),
                RecipeDto.class);
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder()
                        .name("Dessert").tags(List.of(TagDto.builder().tag("dessert").build())).build()),
                RecipeDto.class);

        ResponseEntity<Page<RecipeDto>> response = restTemplate.exchange(
                "/recipe/tag?page=0&size=10", HttpMethod.POST,
                new HttpEntity<>(Set.of(TagDto.builder().tag("italian").build())),
                new ParameterizedTypeReference<Page<RecipeDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        assertThat(response.getBody().getContent().getFirst().getName()).isEqualTo("Italian Dish");
    }

    @Test
    void getRecipeByTagsRequired_returnsExactMatch() {
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder()
                        .name("Italian Dessert")
                        .tags(List.of(TagDto.builder().tag("italian").build(), TagDto.builder().tag("dessert").build()))
                        .build()),
                RecipeDto.class);
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder()
                        .name("Only Italian")
                        .tags(List.of(TagDto.builder().tag("italian").build()))
                        .build()),
                RecipeDto.class);

        ResponseEntity<Page<RecipeDto>> response = restTemplate.exchange(
                "/recipe/tag/required?page=0&size=10", HttpMethod.POST,
                new HttpEntity<>(Set.of(
                        TagDto.builder().tag("italian").build(),
                        TagDto.builder().tag("dessert").build()
                )),
                new ParameterizedTypeReference<Page<RecipeDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        assertThat(response.getBody().getContent().getFirst().getName()).isEqualTo("Italian Dessert");
    }

    @Test
    void getRecipeByTagsRequired_whenTagMissing_returnsEmpty() {
        restTemplate.exchange("/recipe", HttpMethod.PUT,
                new HttpEntity<>(RecipeDto.builder()
                        .name("Italian Dish")
                        .tags(List.of(TagDto.builder().tag("italian").build()))
                        .build()),
                RecipeDto.class);

        ResponseEntity<Page<RecipeDto>> response = restTemplate.exchange(
                "/recipe/tag/required?page=0&size=10", HttpMethod.POST,
                new HttpEntity<>(Set.of(
                        TagDto.builder().tag("italian").build(),
                        TagDto.builder().tag("dessert").build()
                )),
                new ParameterizedTypeReference<Page<RecipeDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }
}
