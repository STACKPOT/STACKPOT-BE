package stackpot.stackpot.repository.PotRepository;



import org.springframework.data.jpa.repository.JpaRepository;
import stackpot.stackpot.domain.Pot;

public interface PotRepository extends JpaRepository<Pot, Long> {
}
