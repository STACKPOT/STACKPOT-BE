package stackpot.stackpot.pot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.entity.PotRecruitmentDetails;
import stackpot.stackpot.pot.entity.mapping.PotSave;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface PotSaveRepository extends JpaRepository<PotSave, Long> {
    // 저장 수 조회 (기존과 동일)
    @Query("SELECT ps.pot.id, COUNT(ps) FROM PotSave ps WHERE ps.pot.id IN :potIds GROUP BY ps.pot.id")
    List<Object[]> countSavesByPotIdsRaw(@Param("potIds") List<Long> potIds);

    default Map<Long, Integer> countSavesByPotIds(List<Long> potIds) {
        return countSavesByPotIdsRaw(potIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    // 사용자가 저장한 팟 ID 목록
    @Query("SELECT ps.pot.id FROM PotSave ps WHERE ps.user.id = :userId AND ps.pot.id IN :potIds")
    Set<Long> findPotIdsByUserIdAndPotIds(@Param("userId") Long userId, @Param("potIds") List<Long> potIds);
}