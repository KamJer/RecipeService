package pl.kamjer.ShoppingListRecipeService.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.kamjer.ShoppingListRecipeService.client.UserClient;
import pl.kamjer.ShoppingListRecipeService.config.TestSecurityConfig;
import pl.kamjer.ShoppingListRecipeService.model.Tag;
import pl.kamjer.ShoppingListRecipeService.model.dto.UserDto;
import pl.kamjer.ShoppingListRecipeService.repository.RecipeRepository;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TagControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @MockitoBean
    private UserClient userClient;

    void authenticateAs(String username, String... authorities) {
        List<SimpleGrantedAuthority> authList = authorities == null || authorities.length == 0
                ? List.of()
                : List.of(new SimpleGrantedAuthority(authorities[0]));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authList);
        auth.setDetails("test-token");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        recipeRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    void getAllTags_returnsAllTags() {
        tagRepository.save(Tag.builder().tag("italian").build());
        tagRepository.save(Tag.builder().tag("dessert").build());
        tagRepository.save(Tag.builder().tag("vegan").build());

        ResponseEntity<Set<String>> response = restTemplate.exchange(
                "/tags",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Set<String>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .containsExactlyInAnyOrder("italian", "dessert", "vegan");
    }

    @Test
    void createTag_createsAndReturnsTag() {
        authenticateAs("admin", "admin");

        ResponseEntity<String> response = restTemplate.exchange(
                "/tags",
                HttpMethod.POST,
                new HttpEntity<>("newtag"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("newtag");
        assertThat(tagRepository.existsByTag("newtag")).isTrue();
    }

    @Test
    void createTag_whenDuplicate_returns409() {
        authenticateAs("admin", "admin");
        tagRepository.save(Tag.builder().tag("existing").build());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tags",
                HttpMethod.POST,
                new HttpEntity<>("existing"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateTag_updatesTagName() {
        authenticateAs("admin", "admin");
        tagRepository.save(Tag.builder().tag("old").build());

        ResponseEntity<String> response = restTemplate.exchange(
                "/tags/old",
                HttpMethod.PUT,
                new HttpEntity<>("new"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("new");
        assertThat(tagRepository.existsByTag("new")).isTrue();
        assertThat(tagRepository.existsByTag("old")).isFalse();
    }

    @Test
    void deleteTag_deletesTag() {
        authenticateAs("admin", "admin");
        tagRepository.save(Tag.builder().tag("todelete").build());

        ResponseEntity<Void> response = restTemplate.exchange(
                "/tags/todelete",
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(tagRepository.existsByTag("todelete")).isFalse();
    }

    @Test
    void deleteTag_whenNotFound_returns404() {
        authenticateAs("admin", "admin");

        ResponseEntity<String> response = restTemplate.exchange(
                "/tags/nonexistent",
                HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
