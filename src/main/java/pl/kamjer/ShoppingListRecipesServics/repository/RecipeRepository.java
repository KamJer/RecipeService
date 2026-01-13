package pl.kamjer.ShoppingListRecipesServics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kamjer.ShoppingListRecipesServics.model.Ingredient;
import pl.kamjer.ShoppingListRecipesServics.model.Recipe;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query(value = """
            SELECT r
            FROM Recipe r 
            WHERE r.recipeId = :recipeId 
            AND (r.published = true OR (:userName IS NOT NULL AND r.userName = :userName))          
            """)
    Optional<Recipe> findByIdCustom(
            Long recipeId,
            @Param("userName") String userName
    );

    @Query(value = """
                SELECT r.*
                FROM recipe r
                JOIN ingredient allIng ON allIng.recipe_id = r.recipe_id
                LEFT JOIN ingredient i ON i.recipe_id = r.recipe_id
                    AND i.name IN (:products)
                WHERE (published = true OR (:userName IS NOT NULL AND r.user_name = :userName))
                GROUP BY r.recipe_id
                HAVING COUNT(DISTINCT i.ingredient_id) > 0
                   AND COUNT(DISTINCT allIng.ingredient_id) - COUNT(DISTINCT i.ingredient_id) <= :maxMissing
            """,
            nativeQuery = true)
    List<Recipe> findCookableRecipes(
            @Param("products") List<String> products,
            @Param("maxMissing") long maxMissing,
            @Param("userName") String userName
    );

    @Query(
            value = """
                         SELECT *
                         FROM recipe
                         WHERE MATCH(name)
                         AGAINST (:query IN BOOLEAN MODE)
                         AND (published = true OR (:userName IS NOT NULL AND user_name = :userName))
                    """,
            nativeQuery = true
    )
    List<Recipe> searchByNameBoolean(@Param("query") String query, String userName);

    @Query("""
                SELECT DISTINCT r
                FROM Recipe r
                JOIN r.tags t
                WHERE t.tag IN :tags
                AND (published = true OR (:userName IS NOT NULL AND r.userName = :userName))
            """)
    List<Recipe> findByAnyTag(@Param("tags") Set<String> tags, String userName);

    @Query("""
                SELECT r
                FROM Recipe r
                JOIN r.tags t
                WHERE t.tag IN :tags
                  AND (r.published = true OR (:userName IS NOT NULL AND r.userName = :userName))
                GROUP BY r
                HAVING COUNT(DISTINCT t.tag) = :tagsSize
            """)
    List<Recipe> findByAllTags(
            @Param("tags") Set<String> tags,
            @Param("tagsSize") long tagsSize,
            @Param("userName") String userName
    );

}
