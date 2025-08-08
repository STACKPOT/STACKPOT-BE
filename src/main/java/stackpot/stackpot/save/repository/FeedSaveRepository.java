package stackpot.stackpot.save.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.mapping.FeedSave;
import stackpot.stackpot.user.entity.User;

import java.util.*;

@Repository
public interface FeedSaveRepository extends JpaRepository<FeedSave, Long> {
    Optional<FeedSave> findByFeedAndUser(Feed feed, User user);
    @Query("SELECT fs.feed.feedId FROM FeedSave fs WHERE fs.user.id = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);

    int countByFeed(Feed feed);
    @Query("SELECT fs.feed FROM FeedSave fs WHERE fs.user.id = :userId")
    Page<Feed> findSavedFeedsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fs.feed.feedId, COUNT(fs) FROM FeedSave fs WHERE fs.feed.feedId IN :feedIds GROUP BY fs.feed.feedId")
    List<Object[]> countSavesByFeedIds(@Param("feedIds") List<Long> feedIds);



    @Query("SELECT COUNT(fs) FROM FeedSave fs WHERE fs.feed.feedId = :feedId")
    long countByFeedId(@Param("feedId") Long feedId);
    @Query("""
        SELECT fs.feed.feedId, COUNT(fs)
        FROM FeedSave fs
        WHERE fs.feed.feedId IN :feedIds
        GROUP BY fs.feed.feedId
    """)
    List<Object[]> countByFeedIdsRaw(@Param("feedIds") List<Long> feedIds);

    default Map<Long, Long> countByFeedIds(List<Long> feedIds) {
        if (feedIds == null || feedIds.isEmpty()) return Collections.emptyMap();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : countByFeedIdsRaw(feedIds)) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }


}

