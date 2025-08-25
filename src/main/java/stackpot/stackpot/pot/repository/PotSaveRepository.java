package stackpot.stackpot.pot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotSave;
import stackpot.stackpot.user.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface PotSaveRepository extends JpaRepository<PotSave, Long> {
    Optional<PotSave> findByUserAndPot(User user, Pot pot);
    boolean existsByUserAndPot_PotId(User user, Long potPotId);
    void deleteByUserAndPot(User user, Pot pot);

    // 저장 수 조회 (기존과 동일)
    @Query("SELECT ps.pot.potId, COUNT(ps) FROM PotSave ps WHERE ps.pot.potId IN :potIds GROUP BY ps.pot.potId")
    List<Object[]> countSavesByPotIdsRaw(@Param("potIds") List<Long> potIds);

    default Map<Long, Integer> countSavesByPotIds(List<Long> potIds) {
        return countSavesByPotIdsRaw(potIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    @Query("""
    SELECT ps.pot.potId
    FROM PotSave ps
    JOIN ps.pot p
    WHERE ps.user.id = :userId AND p.potId IN :potIds
    """)
    Set<Long> findPotIdsByUserIdAndPotIds(@Param("userId") Long userId, @Param("potIds") List<Long> potIds);

    // PotSaveRepository.java

    @Query("SELECT ps.pot FROM PotSave ps WHERE ps.user.id = :userId")
    Page<Pot> findSavedPotsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM PotSave ps WHERE ps.user = :user AND ps.pot IN :pots")
    void deleteAllByUserAndPots(@Param("user") User user, @Param("pots") List<Pot> pots);

    List<PotSave> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);

    @Modifying @Transactional
    @Query("delete from PotSave ps where ps.pot.potId in :potIds")
    int deleteByPotIds(@Param("potIds") List<Long> potIds);
}