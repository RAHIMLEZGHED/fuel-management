package me.kadarh.mecaworks.service.impl.bons.bonEngin;

import lombok.extern.slf4j.Slf4j;
import me.kadarh.mecaworks.domain.alertes.TypeAlerte;
import me.kadarh.mecaworks.domain.bons.BonEngin;
import me.kadarh.mecaworks.domain.others.TypeCompteur;
import me.kadarh.mecaworks.repo.bons.BonEnginRepo;
import me.kadarh.mecaworks.repo.others.ChantierRepo;
import me.kadarh.mecaworks.repo.others.EmployeRepo;
import me.kadarh.mecaworks.repo.others.EnginRepo;
import me.kadarh.mecaworks.service.exceptions.OperationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author kadarH
 */

@Service
@Slf4j
@Transactional
public class CalculServiceImpl {

    private final BonEnginRepo bonEnginRepo;
    private final EnginRepo enginRepo;
    private final ChantierRepo chantierRepo;
    private final EmployeRepo employeRepo;
    private PersistServiceImpl persistService;

    public CalculServiceImpl(BonEnginRepo bonEnginRepo, EnginRepo enginRepo, ChantierRepo chantierRepo, EmployeRepo employeRepo, PersistServiceImpl persistService) {
        this.bonEnginRepo = bonEnginRepo;
        this.enginRepo = enginRepo;
        this.chantierRepo = chantierRepo;
        this.employeRepo = employeRepo;
        this.persistService = persistService;
    }

    public BonEngin fillBon(BonEngin bon) {
        try {
            bon.setEngin(enginRepo.findById(bon.getEngin().getId()).get());
            bon.setChauffeur(employeRepo.findById(bon.getChauffeur().getId()).get());
            bon.setPompiste(employeRepo.findById(bon.getPompiste().getId()).get());
            bon.setChantierTravail(chantierRepo.findById(bon.getChantierTravail().getId()).get());
            bon.setChantierGazoil(chantierRepo.findById(bon.getChantierGazoil().getId()).get());
            calculCompteursAbsolu(bon, persistService.getLastBonEngin(bon.getEngin()));
            return bon;
        } catch (Exception e) {
            throw new OperationFailedException("Operation echouée", e);
        }
    }

    public void calculCompteursAbsolu(BonEngin bonEngin, BonEngin lastBon) {
        log.info("--- > Calcul compteur Absolu");
        String typeCompteur = bonEngin.getEngin().getSousFamille().getTypeCompteur().name();
        BonEngin bonEngin1 = lastBon;
        if (typeCompteur.equals(TypeCompteur.H.name())) {
            if (bonEngin1 != null)
                bonEngin.setCompteurAbsoluH(bonEngin1.getCompteurAbsoluH() + bonEngin.getCompteurH());
            else
                bonEngin.setCompteurAbsoluH(bonEngin.getCompteurH());
        } else if (typeCompteur.equals(TypeCompteur.KM.name())) {
            if (bonEngin1 != null)
                bonEngin.setCompteurAbsoluKm(bonEngin1.getCompteurAbsoluKm() + bonEngin.getCompteurKm());
            else
                bonEngin.setCompteurAbsoluKm(bonEngin.getCompteurKm());
        } else if (typeCompteur.equals(TypeCompteur.KM_H.name())) {
            if (bonEngin1 != null) {
                bonEngin.setCompteurAbsoluH(bonEngin1.getCompteurAbsoluH() + bonEngin.getCompteurH());
                bonEngin.setCompteurAbsoluKm(bonEngin1.getCompteurAbsoluKm() + bonEngin.getCompteurKm());
            } else {
                bonEngin.setCompteurAbsoluH(bonEngin.getCompteurH());
                bonEngin.setCompteurAbsoluKm(bonEngin.getCompteurKm());
            }
        }
        log.info("--- > Calcul compteur AbsoluKm = " + bonEngin.getCompteurKm());
        log.info("--- > Calcul compteur AbsoluH = " + bonEngin.getCompteurH());
    }

    public BonEngin calculConsommation(BonEngin bonEngin) {
        //get last bon Engin [ BX ] with cmpEnpanne=non && plein =oui
        //get list of bons between BX and the current bon
        //SOM_Q = calculate lmejmou3 dial les quantite ,
        // AA = absolu current bon - absolu BX
        //calculate consommatio = SOM_Q/AA ( l7ala dial km : SOM_Q*100/AA
        TypeCompteur typeCompteur = bonEngin.getEngin().getSousFamille().getTypeCompteur();
        BonEngin lastBon, lastBon2;
        long som_Q = 0;
        long som_Q_2 = 0;
        if (typeCompteur.equals(TypeCompteur.H)) {
            lastBon = bonEnginRepo.findLastBonEnginH_toConsommation(bonEngin.getEngin().getId());
            if (lastBon != null) {
                for (BonEngin b : bonEnginRepo.findAllBetweenLastBonAndCurrentBon_H(lastBon.getCompteurAbsoluH()))
                    som_Q += b.getQuantite();
                bonEngin.setConsommationH((float) som_Q / (bonEngin.getCompteurAbsoluH() - lastBon.getCompteurAbsoluH()));
            }
            if (bonEngin.getConsommationH() > bonEngin.getEngin().getSousFamille().getConsommationHMax())
                persistService.insertAlerte(bonEngin, "La consommation H est Annormale", TypeAlerte.CONSOMMATION_H_ANNORMALE);
        }
        if (typeCompteur.equals(TypeCompteur.KM)) {
            lastBon = bonEnginRepo.findLastBonEnginKm_toConsommation(bonEngin.getEngin().getId());
            if (lastBon != null) {
                for (BonEngin b : bonEnginRepo.findAllBetweenLastBonAndCurrentBon_Km(lastBon.getCompteurAbsoluKm()))
                    som_Q += b.getQuantite();
                bonEngin.setConsommationKm((float) som_Q * 100 / (bonEngin.getCompteurAbsoluKm() - lastBon.getCompteurAbsoluKm()));
            }
            if (bonEngin.getConsommationKm() > bonEngin.getEngin().getSousFamille().getConsommationKmMax())
                persistService.insertAlerte(bonEngin, "La consommation Km est Annormale", TypeAlerte.CONSOMMATION_KM_ANNORMALE);

        }
        if (typeCompteur.equals(TypeCompteur.KM_H)) {
            lastBon = bonEnginRepo.findLastBonEnginKm_toConsommation(bonEngin.getEngin().getId());
            lastBon2 = bonEnginRepo.findLastBonEnginH_toConsommation(bonEngin.getEngin().getId());
            if (lastBon != null) {
                for (BonEngin b : bonEnginRepo.findAllBetweenLastBonAndCurrentBon_Km(lastBon.getCompteurAbsoluKm())) {
                    if (b.getQuantite() != null)
                        som_Q += b.getQuantite();
                }
                bonEngin.setConsommationKm((float) som_Q * 100 / (bonEngin.getCompteurAbsoluKm() - lastBon.getCompteurAbsoluKm()));
            }
            if (lastBon2 != null) {
                for (BonEngin b : bonEnginRepo.findAllBetweenLastBonAndCurrentBon_H(lastBon2.getCompteurAbsoluH())) {
                    if (b.getQuantite() != null)
                        som_Q_2 += b.getQuantite();
                }
                bonEngin.setConsommationH((float) som_Q_2 / (bonEngin.getCompteurAbsoluH() - lastBon2.getCompteurAbsoluH()));
            }
            if (bonEngin.getConsommationKm() > bonEngin.getEngin().getSousFamille().getConsommationKmMax())
                persistService.insertAlerte(bonEngin, "La consommation Km est Annormale", TypeAlerte.CONSOMMATION_KM_ANNORMALE);
            if (bonEngin.getConsommationH() > bonEngin.getEngin().getSousFamille().getConsommationHMax())
                persistService.insertAlerte(bonEngin, "La consommation H est Annormale", TypeAlerte.CONSOMMATION_H_ANNORMALE);
        }
        return bonEngin;
    }

}