package me.kadarh.mecaworks.repo.bons;

import me.kadarh.mecaworks.domain.Bons.BonEngin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonEnginRepo extends JpaRepository<BonEngin, Long> {


}
