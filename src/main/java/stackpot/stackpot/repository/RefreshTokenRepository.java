package stackpot.stackpot.repository;


import org.springframework.data.repository.CrudRepository;
import stackpot.stackpot.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
