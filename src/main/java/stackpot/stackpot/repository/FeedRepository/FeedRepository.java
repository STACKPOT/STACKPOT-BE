package stackpot.stackpot.repository.FeedRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.enums.Category;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("SELECT f " +
            "FROM Feed f " +
            "WHERE (:category IS NULL OR f.category = :category) " +
            "AND ( " +
            "     (:sort = 'new' AND f.feedId < :lastFeedId) OR " +
            "     (:sort = 'old' AND f.feedId > :lastFeedId) OR " +
            "     (:sort = 'popular' AND f.feedId < :lastFeedId) " + // ✅ feedId 기준으로 페이징
            ") " +
            "ORDER BY " +
            "     (CASE WHEN :sort = 'popular' THEN f.likeCount ELSE 0 END) DESC, " +
            "     (CASE WHEN :sort = 'new' THEN f.feedId ELSE NULL END) DESC, " +
            "     (CASE WHEN :sort = 'old' THEN f.feedId ELSE NULL END) ASC")  // ✅ `old` 정렬을 ASC로 변경
    List<Feed> findFeeds(
            @Param("category") Category category,
            @Param("sort") String sort,
            @Param("lastFeedId") Integer lastFeedId, // ✅ Integer로 변경
            Pageable pageable);
    List<Feed> findByUser_Id(Long userId);
    Page<Feed> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String titleKeyword, String contentKeyword, Pageable pageable);

    // 기본 페이징 조회
    List<Feed> findByUser_Id(Long userId, Pageable pageable);

    // 커서 기반 페이징 조회
    List<Feed> findByUserIdAndCreatedAtBefore(Long userId, LocalDateTime createdAt, Pageable pageable);

}