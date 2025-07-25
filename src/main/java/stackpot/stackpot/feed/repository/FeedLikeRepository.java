package stackpot.stackpot.feed.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.feed.entity.mapping.FeedLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    // 특정 사용자가 특정 게시물에 좋아요를 눌렀는지 확인
    Optional<FeedLike> findByFeedAndUser(Feed feed, User user);

    // 특정 게시물의 좋아요 개수 조회
    @Query("SELECT COUNT(fl) FROM FeedLike fl WHERE fl.feed = :feed")
    Long countByFeed(@Param("feed") Feed feed);

    @Query("SELECT fl.feed.feedId FROM FeedLike fl WHERE fl.user.id = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FeedLike f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    boolean existsByFeedAndUser(Feed feed, User user);

    @Query("SELECT fs.feed FROM FeedLike fs WHERE fs.user.id = :userId")
    Page<Feed> findLikedFeedsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(fl) FROM FeedLike fl WHERE fl.feed.feedId = :feedId")
    long countByFeedId(@Param("feedId") Long feedId);


}
