package stackpot.stackpot.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stackpot.stackpot.feed.entity.Series;
import stackpot.stackpot.user.entity.User;

import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {
    List<Series> findAllByUser(User user);

}