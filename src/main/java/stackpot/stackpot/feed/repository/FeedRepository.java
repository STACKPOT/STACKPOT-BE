package stackpot.stackpot.feed.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.enums.Category;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("""
    SELECT f
    FROM Feed f
    JOIN f.categories c
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

    @Modifying
    @Query("DELETE FROM Feed f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    default String getNextCursor(List<Feed> feeds) {
        if (feeds.isEmpty()) {
            throw new IllegalStateException("더 이상 불러올 피드가 없습니다.");
        }
        return feeds.get(feeds.size() - 1).getFeedId().toString();
    }

    @Modifying
    @Query("UPDATE Feed f SET f.series = null WHERE f.series.seriesId = :seriesId")
    void clearSeriesReference(@Param("seriesId") Long seriesId);

}