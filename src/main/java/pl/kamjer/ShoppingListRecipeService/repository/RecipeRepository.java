package pl.kamjer.ShoppingListRecipeService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kamjer.ShoppingListRecipeService.model.Recipe;

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

    @Query(
            value = """
                    SELECT *
                    FROM recipe
                    WHERE MATCH(name)
                    AGAINST (:query IN BOOLEAN MODE)
                    AND (published = true OR (:userName IS NOT NULL AND user_name = :userName))
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM recipe
                    WHERE MATCH(name)
                    AGAINST (:query IN BOOLEAN MODE)
                    AND (published = true OR (:userName IS NOT NULL AND user_name = :userName))
                    """,
            nativeQuery = true
    )
    Page<Recipe> searchByNameBoolean(@Param("query") String query, String userName, Pageable pageable);

    @Query(value = """
                SELECT DISTINCT r
                FROM Recipe r
                JOIN r.tags t
                WHERE t.tag IN :tags
                AND (published = true OR (:userName IS NOT NULL AND r.userName = :userName))
            """,
            countQuery = """
                        SELECT COUNT(DISTINCT r)
                        FROM Recipe r
                        JOIN r.tags t
                        WHERE t.tag IN :tags
                        AND (published = true OR (:userName IS NOT NULL AND r.userName = :userName))
                    """)
    Page<Recipe> findByAnyTag(@Param("tags") Set<String> tags, String userName, Pageable pageable);

    @Query(value = """
                SELECT r
                FROM Recipe r
                JOIN r.tags t
                WHERE t.tag IN :tags
                  AND (r.published = true OR (:userName IS NOT NULL AND r.userName = :userName))
                GROUP BY r
                HAVING COUNT(DISTINCT t.tag) = :tagsSize
            """,
            countQuery = """
                        select count(r)
                        FROM Recipe r
                        JOIN r.tags t
                        WHERE t.tag IN :tags
                          AND (r.published = true OR (:userName IS NOT NULL AND r.userName = :userName))
                        GROUP BY r
                        HAVING COUNT(DISTINCT t.tag) = :tagsSize
                    """)
    Page<Recipe> findByAllTags(
            @Param("tags") Set<String> tags,
            @Param("tagsSize") long tagsSize,
            @Param("userName") String userName,
            Pageable pageable
    );

    Page<Recipe> findByUserName(String userName, Pageable pageable);

    Optional<Recipe> findFirstByNameIgnoreCase(String name);

    @Query(value = """
                SELECT r
                FROM Recipe r
                WHERE r.published = true
                OR (:userName IS NOT NULL AND r.userName = :userName)
            """)
    Page<Recipe> findAllRecipe(@Param("userName") String userName, Pageable pageable);
}
