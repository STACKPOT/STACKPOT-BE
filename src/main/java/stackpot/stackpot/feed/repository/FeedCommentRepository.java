package stackpot.stackpot.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.feed.dto.FeedCommentDto;
import stackpot.stackpot.feed.entity.mapping.FeedComment;

import java.util.List;
import java.util.Optional;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    @Query("select fc from FeedComment fc where fc.id = :commentId")
    Optional<FeedComment> findByCommentId(@Param("commentId") Long commentId);

    @Query("select new stackpot.stackpot.feed.dto.FeedCommentDto$FeedCommentInfoDto(fc.user.id, fc.user.nickname, " +
            "fc.feed.user.id, fc.id, fc.comment, fc.parent.id, fc.createdAt) " +
            "from FeedComment fc where fc.feed.feedId = :feedId")
    List<FeedCommentDto.FeedCommentInfoDto> findAllCommentInfoDtoByFeedId(@Param("feedId") Long feedId);

    @Query("SELECT COUNT(fc) FROM FeedComment fc WHERE fc.feed.feedId = :feedId")
    Long countByFeedId(@Param("feedId") Long feedId);
}

