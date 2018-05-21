package me.kadarh.mecaworks.service.impl.user;

import me.kadarh.mecaworks.domain.bons.BonEngin;
import me.kadarh.mecaworks.domain.others.Chantier;
import me.kadarh.mecaworks.domain.user.ChantierBatch;
import me.kadarh.mecaworks.repo.bons.BonEnginRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PROJECT mecaworks
 *
 * @author kadarH
 * Created at 20/05/18
 */

@Service
@Transactional
public class UserCalculService {

    private final BonEnginRepo bonEnginRepo;

    public UserCalculService(BonEnginRepo bonEnginRepo) {
        this.bonEnginRepo = bonEnginRepo;
    }

    public List<ChantierBatch> getListChantierWithQuantities(int month, int year) {
        List<ChantierBatch> list = new ArrayList<>();
        ChantierBatch chantierBatch;
        List<BonEngin> bonEngins = bonEnginRepo.findAllBetweenDates(LocalDate.of(year, Month.of(month).minus(1).getValue(), 1), LocalDate.of(year, Month.of(month).getValue(), 1));
        Map<Chantier, Long> sum = bonEngins.stream().collect(Collectors.groupingBy(BonEngin::getChantierTravail, Collectors.summingLong(BonEngin::getQuantite)));
        Map<Chantier, Long> sum2 = bonEngins.stream().filter(bonEngin -> bonEngin.getEngin().getGroupe().getLocataire()).collect(Collectors.groupingBy(BonEngin::getChantierTravail, Collectors.summingLong(BonEngin::getQuantite)));
        for (Map.Entry<Chantier, Long> entry : sum.entrySet()) {
            chantierBatch = new ChantierBatch(month, year, entry.getValue(), sum2.get(entry.getKey()), entry.getKey());
            if (chantierBatch.getQuantiteLocation() == null) chantierBatch.setQuantiteLocation(0L);
            list.add(chantierBatch);
        }
        return list;
    }
}