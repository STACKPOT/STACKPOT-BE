package stackpot.stackpot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Feed;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.FeedSave;

import java.util.Optional;

@Repository
public interface FeedSaveRepository extends JpaRepository<FeedSave, Long> {
    // 특정 사용자가 특정 게시물에 좋아요를 눌렀는지 확인
    Optional<FeedSave> findByFeedAndUser(Feed feed, User user);
}

