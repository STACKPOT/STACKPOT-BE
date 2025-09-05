package stackpot.stackpot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.user.entity.TempUser;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser,Long> {
    int deleteByCreatedAtBefore(LocalDateTime time);

    @Query("""
        select tu
        from TempUser tu
        left join fetch tu.roles
        where tu.id = :id
    """)
    Optional<TempUser> findWithRolesById(@Param("id") Long id);
}
