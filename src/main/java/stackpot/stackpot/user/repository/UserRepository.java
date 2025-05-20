package stackpot.stackpot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Provider;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, Long providerId);

    boolean existsByNickname(String nickname);
}
