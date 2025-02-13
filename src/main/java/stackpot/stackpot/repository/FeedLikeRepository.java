package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.FeedLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    // 특정 사용자가 특정 게시물에 좋아요를 눌렀는지 확인
    Optional<FeedLike> findByFeedAndUser(Feed feed, User user);

    // 특정 게시물의 좋아요 개수 조회
    @Query("SELECT COUNT(fl) FROM FeedLike fl WHERE fl.feed = :feed")
    Long countByFeed(@Param("feed") Feed feed);

    @Query("SELECT fl.feed.id FROM FeedLike fl WHERE fl.user.id = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM FeedLike f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
