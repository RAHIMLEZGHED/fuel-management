package me.kadarh.mecaworks.repo.bons;

import lombok.extern.slf4j.Slf4j;
import me.kadarh.mecaworks.domain.bons.BonEngin;
import me.kadarh.mecaworks.domain.bons.BonFournisseur;
import me.kadarh.mecaworks.domain.bons.BonLivraison;
import me.kadarh.mecaworks.domain.others.*;
import me.kadarh.mecaworks.repo.others.ChantierRepo;
import me.kadarh.mecaworks.repo.others.EmployeRepo;
import me.kadarh.mecaworks.repo.others.EnginRepo;
import me.kadarh.mecaworks.repo.others.FournisseurRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author kadarH
 */

@Component
@Transactional
@Slf4j
@Profile("dev")
public class DataFakerB {

	@Autowired
	private BonEnginRepo bonEnginRepo;

	@Autowired
	private BonLivraisonRepo bonLivraisonRepo;

	@Autowired
	private BonFournisseurRepo bonFournisseurRepo;

	@Autowired
	private EnginRepo enginRepo;

	@Autowired
	private ChantierRepo chantierRepo;

	@Autowired
	private FournisseurRepo fournisseurRepo;

	@Autowired
	private EmployeRepo employeRepo;

	public void run() {
		log.info("This is DataFaker of Bons");
		//loadBonsEngin(80, 20);

        //loadBonFournisseur(20);
	}

	private void loadBonsEngin(int n, int m) {
		for (int i = 0; i < n; i++) {
			Chantier chantierGazoil = chantierRepo.getOne(i / 20 + 1L);
			Chantier chantierTravail = chantierRepo.getOne(i / 20 + 2L);
			//Getting chantier + engin +
			Engin engin = enginRepo.getOne(i / 4 + 1L);
			//Creating the object
			BonEngin bonEngin = new BonEngin();
			bonEngin.setCode("2093" + (i * 13));
			bonEngin.setCompteurH(100L + i * 5);
			bonEngin.setCompteurAbsoluH(100L + i * 10);
			bonEngin.setCompteurKm(100L + i * 5);
			bonEngin.setCompteurAbsoluKm(100L + i * 10);
			bonEngin.setQuantite(20);
			bonEngin.setChantierGazoil(chantierGazoil);
			bonEngin.setChantierTravail(chantierTravail);

			Employe chauffeur = new Employe();
			chauffeur.setNom("Mr C" + i);
			chauffeur.setMetier(Metier.CHAUFFEUR.name());
			bonEngin.setChauffeur(employeRepo.save(chauffeur));
			Employe pompiste = new Employe();
			pompiste.setNom("Mr P" + i);
			pompiste.setMetier(Metier.POMPISTE.name());
			bonEngin.setPompiste(employeRepo.save(pompiste));
			DateTimeFormatter d = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate date = LocalDate.parse(LocalDate.now().format(d), d);
			bonEngin.setDate(date);
			if (i % 2 == 1) {
				bonEngin.setCompteurKmenPanne(true);
				bonEngin.setCompteurHenPanne(true);
				bonEngin.setPlein(true);
			} else {
				bonEngin.setCompteurKmenPanne(false);
				bonEngin.setCompteurHenPanne(false);
				bonEngin.setPlein(false);
			}
			bonEngin.setEngin(engin);
			bonEnginRepo.save(bonEngin);
		}
		log.info(n + " Bons Engin Loaded *****");

		for (int i = 0; i < m; i++) {
			Chantier chantierGazoil = chantierRepo.getOne(i / 10 + 1L);
			Chantier chantierTravail = chantierRepo.getOne(i / 10 + 2L);
			//Getting chantier + engin +
			Engin engin = enginRepo.getOne(i / 4 + 1L);
			BonEngin bonEngin = new BonEngin();
			bonEngin.setCode("3297" + (i * 10));
			bonEngin.setCompteurH(100L + i * 5);
			bonEngin.setCompteurAbsoluH(100L + i * 10);
			bonEngin.setCompteurKm(100L + i * 5);
			bonEngin.setCompteurAbsoluKm(100L + i * 10);
			bonEngin.setQuantite(20 + i * 10);
			bonEngin.setChantierGazoil(chantierGazoil);
			bonEngin.setChantierTravail(chantierTravail);
			bonEngin.setDate(LocalDate.now());
			if (i % 2 == 0) {
				bonEngin.setCompteurKmenPanne(true);
				bonEngin.setCompteurHenPanne(false);
				bonEngin.setPlein(true);
			} else {
				bonEngin.setCompteurKmenPanne(false);
				bonEngin.setCompteurHenPanne(false);
				bonEngin.setPlein(false);
			}
			bonEngin.setEngin(engin);
			bonEnginRepo.save(bonEngin);
			BonLivraison bonLivraison = new BonLivraison();
			bonLivraison.setCode("KHS" + i + "LL");
			bonLivraison.setChantierDepart(chantierGazoil);
			bonLivraison.setChantierArrivee(chantierTravail);
			bonLivraison.setQuantite(20 + i * 10);
			bonLivraison.setDate(LocalDate.now());
			Employe transporteur = new Employe();
			transporteur.setNom("Mr Tr" + i);
			transporteur.setMetier(Metier.CHAUFFEUR.name());
			bonLivraison.setTransporteur(employeRepo.save(transporteur));
			Employe pompiste = new Employe();
			pompiste.setNom("Mr Pom" + i);
			pompiste.setMetier(Metier.POMPISTE.name());
			bonEngin.setPompiste(employeRepo.save(pompiste));

			bonLivraison.setPompiste(pompiste);
			bonLivraisonRepo.save(bonLivraison);
		}
		log.info(n + " Bons Engin + Bons Livraison direct Loaded *****");
	}


	private void loadBonFournisseur(int n) {
		for (int i = 0; i < n; i++) {
			Fournisseur fournisseur = fournisseurRepo.getOne(i / 4 + 1L);
			Chantier chantier = chantierRepo.getOne(i / 10 + 1L);

			BonFournisseur bonFournisseur = new BonFournisseur();
			bonFournisseur.setFournisseur(fournisseur);
			bonFournisseur.setChantier(chantier);
			bonFournisseur.setCode("12332" + i * 11);
			bonFournisseur.setDate(LocalDate.now());
			bonFournisseur.setQuantite(i * 10 + 1000);
			bonFournisseur.setPrixUnitaire(9f + (float) i / 123);
			bonFournisseurRepo.save(bonFournisseur);
		}
		log.info(n + " Bons Fournisseur Loaded *****");
	}
}
