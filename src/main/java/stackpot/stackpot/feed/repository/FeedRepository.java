package stackpot.stackpot.feed.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.enums.Category;
import stackpot.stackpot.feed.entity.enums.Interest;
import stackpot.stackpot.user.entity.User;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("""
SELECT f
FROM Feed f
LEFT JOIN f.categories c
WHERE (:category IS NULL OR :category = c)
  AND (
      (:sort = 'new' AND f.feedId < :lastFeedId) OR
      (:sort = 'old' AND f.feedId > :lastFeedId) OR
      (:sort = 'popular' AND (
          f.likeCount < :lastLikeCount OR 
          (f.likeCount = :lastLikeCount AND f.feedId < :lastFeedId)
      ))
  )
ORDER BY 
  CASE WHEN :sort = 'popular' THEN f.likeCount ELSE 0 END DESC,
  CASE WHEN :sort = 'old' THEN f.feedId ELSE NULL END ASC,
  CASE WHEN :sort = 'new' THEN f.feedId ELSE NULL END DESC,
  f.feedId DESC
""")
    List<Feed> findFeeds(
            @Param("category") Category category,
            @Param("sort") String sort,
            @Param("lastFeedId") long lastFeedId,
            @Param("lastLikeCount") long lastLikeCount,
            Pageable pageable);


    List<Feed> findByUser_Id(Long userId);
    Page<Feed> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String titleKeyword, String contentKeyword, Pageable pageable);

    // 기본 페이징 조회
    List<Feed> findByUser_Id(Long userId, Pageable pageable);

    // 커서 기반 페이징 조회
    List<Feed> findByUserIdAndFeedIdBefore(Long userId, Long cursorFeedId, Pageable pageable);

    // 시리즈 필터링 추가된 페이징 조회
    List<Feed> findByUser_IdAndSeries_SeriesId(Long userId, Long seriesId, Pageable pageable);
    List<Feed> findByUser_IdAndSeries_SeriesIdAndFeedIdBefore(Long userId, Long seriesId, Long cursorFeedId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Feed f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);


    @Modifying
    @Query("UPDATE Feed f SET f.series = null WHERE f.series.seriesId = :seriesId")
    void clearSeriesReference(@Param("seriesId") Long seriesId);



    @Query("""
SELECT f FROM Feed f
WHERE :interest MEMBER OF f.interests
  AND (:category IS NULL OR :category MEMBER OF f.categories)
  AND (f.likeCount < :lastLikeCount OR (f.likeCount = :lastLikeCount AND f.feedId < :lastFeedId))
ORDER BY f.likeCount DESC, f.feedId DESC
""")
    List<Feed> findFeedsByInterestAndCategoryWithCursor(
            @Param("interest") Interest interest,
            @Param("category") Category category,
            @Param("lastLikeCount") Long lastLikeCount,
            @Param("lastFeedId") Long lastFeedId,
            Pageable pageable
    );


    @Query("""
SELECT f FROM Feed f
WHERE EXISTS (
    SELECT 1 FROM f.interests i WHERE i IN :interests
)
  AND (:category IS NULL OR :category MEMBER OF f.categories)
  AND (f.likeCount < :lastLikeCount OR (f.likeCount = :lastLikeCount AND f.feedId < :lastFeedId))
ORDER BY f.likeCount DESC, f.feedId DESC
""")
    List<Feed> findFeedsByInterestsAndCategoryWithCursor(
            @Param("interests") List<Interest> interests,
            @Param("category") Category category,
            @Param("lastLikeCount") Long lastLikeCount,
            @Param("lastFeedId") Long lastFeedId,
            Pageable pageable
    );


    @Query("""
    SELECT f FROM Feed f
    WHERE f.user.id = :userId
      AND (:keyword IS NULL OR :keyword = '' OR
           LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursor IS NULL OR f.feedId< :cursor)
    ORDER BY f.feedId DESC
""")
    List<Feed> searchFeedsByUserAndKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND (:cursor IS NULL OR f.feedId < :cursor) ORDER BY f.feedId DESC")
    List<Feed> findMyFeedsByCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);



}