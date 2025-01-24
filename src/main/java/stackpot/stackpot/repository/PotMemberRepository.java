package stackpot.stackpot.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotMember;

import java.util.List;
import java.util.Optional;

@Repository
// 8. PotMemberRepository
public interface PotMemberRepository extends JpaRepository<PotMember, Long> {
    @Query("SELECT pm.user.id FROM PotMember pm WHERE pm.pot.potId = :potId")
    List<Long> findUserIdsByPotId(@Param("potId") Long potId);
}
