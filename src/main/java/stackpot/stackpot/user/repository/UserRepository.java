package stackpot.stackpot.user.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Provider;
import stackpot.stackpot.user.entity.enums.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.id = :userId")
    Optional<User> findByUserId(@Param("userId") Long userId);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByNickname(String nickname);

    @Query("select u.nickname from User u where u.id = :userId")
    Optional<String> findNameByUserId(@Param("userId") Long userId);
}
