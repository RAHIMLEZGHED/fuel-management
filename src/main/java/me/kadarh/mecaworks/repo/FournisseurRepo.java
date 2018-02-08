package me.kadarh.mecaworks.repo;

import me.kadarh.mecaworks.domain.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FournisseurRepo extends JpaRepository<Fournisseur, Long> {
}
