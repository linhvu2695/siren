package vlinh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vlinh.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
}
