package stackpot.stackpot.pot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.dto.PotCommentDto;
import stackpot.stackpot.pot.entity.mapping.PotComment;

import java.util.List;
import java.util.Optional;

public interface PotCommentRepository extends JpaRepository<PotComment, Long> {

    @Query("select pc from PotComment pc where pc.id = :commentId")
    Optional<PotComment> findByCommentId(@Param("commentId") Long commentId);

    @Query("select new stackpot.stackpot.pot.dto.PotCommentDto$PotCommentInfoDto(pc.user.id, pc.user.nickname" +
            ", pc.pot.user.id, pc.id, pc.comment, pc.parent.id, pc.createdAt) " +
            "from PotComment pc where pc.pot.potId = :potId")
    List<PotCommentDto.PotCommentInfoDto> findAllCommentInfoDtoByPotId(@Param("potId") Long potId);

    @Query("select count(*) from PotComment pc where pc.pot.potId = :potId")
    Long countByPotId(@Param("potId") Long potId);
}
