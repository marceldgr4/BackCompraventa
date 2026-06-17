package com.CompraVenta.Backend.Modules.Articles.Repository;

import com.CompraVenta.Backend.Modules.Articles.Entity.Article;
import com.CompraVenta.Backend.Modules.Articles.Enums.ArticleCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article,Long> {
    Optional<Article>findByGlobalId(UUID globalId);
    boolean existsByGlobalId(UUID globalId);

    @Query("""
        SELECT a FROM Article a
        WHERE(:search IS NULL OR LOWER(a.nameArticle)LIKE LOWER(CONCAT('%',:search,'%')))
        AND(:category IS NULL OR a.category =:category)
        AND(:onlyAvailable IS NULL OR :onlyAvailable = FALSE OR a.amount > 0)
""")
    Page<Article> findByFilters(
            @Param("search") String search,
            @Param("category") ArticleCategory category,
            @Param("onlyAvailable") Boolean onlyAvailabe,
            Pageable pageable
    );

    @Query(value = "SELECT COUNT(*)>0 FROM public.sales_details sd WHERE sd.article_id = :articleId",
    nativeQuery = true)
    boolean hasActiveSales(@Param("articleId") long articleId);

    @Query(value = """
SELECT COUNT (*) >0 FROM public.pawns p
WHERE p.article_id =:articleId AND p.status IN ('ACTIVO','VENCIDO')
""", nativeQuery = true)
    boolean hasActivePawns(@Param("articleId") long articleId);

    @Modifying
    @Query("UPDATE Article a SET a.amount= a.amount + :quantity WHERE a.id =:id AND(a.amount +:quantity)>=0")
    int adjustStock(@Param("id") long id, @Param("quantity") int quantity);
}
