package me.kadarh.mecaworks.repo;

import me.kadarh.mecaworks.domain.Chantier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChantierRepo extends JpaRepository<Chantier,Long> {

}
