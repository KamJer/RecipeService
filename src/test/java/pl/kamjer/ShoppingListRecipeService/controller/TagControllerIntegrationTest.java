package pl.kamjer.ShoppingListRecipeService.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
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
import pl.kamjer.ShoppingListRecipeService.model.dto.UserDto;
import pl.kamjer.ShoppingListRecipeService.repository.TagRepository;
import org.springframework.context.annotation.Import;

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
        tagRepository.save(Tag.builder().tag("vegan").build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        tagRepository.deleteAll();
    }

    @Test
    void getAllTags_returnsAllTags() {
        ResponseEntity<Set<Tag>> response = restTemplate.exchange(
                "/tags",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Set<Tag>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).extracting(Tag::getTag)
                .containsExactlyInAnyOrder("italian", "dessert", "vegan");
    }
}
