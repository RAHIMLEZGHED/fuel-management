package me.kadarh.mecaworks.repo.bons;

import me.kadarh.mecaworks.domain.bons.BonLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonLivraisonRepo extends JpaRepository<BonLivraison, Long> {


}
