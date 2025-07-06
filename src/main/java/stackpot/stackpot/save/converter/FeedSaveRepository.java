package stackpot.stackpot.save.converter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Feed;
import stackpot.stackpot.feed.entity.mapping.FeedSave;
import stackpot.stackpot.user.entity.User;

import java.util.Optional;

@Repository
public interface FeedSaveRepository extends JpaRepository<FeedSave, Long> {
    Optional<FeedSave> findByFeedAndUser(Feed feed, User user);
}
