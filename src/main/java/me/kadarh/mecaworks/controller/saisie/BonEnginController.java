package me.kadarh.mecaworks.controller.saisie;

import lombok.extern.slf4j.Slf4j;
import me.kadarh.mecaworks.domain.bons.BonEngin;
import me.kadarh.mecaworks.domain.bons.Carburant;
import me.kadarh.mecaworks.domain.others.TypeBon;
import me.kadarh.mecaworks.domain.others.TypeCompteur;
import me.kadarh.mecaworks.service.BonEnginConfirmation;
import me.kadarh.mecaworks.service.ChantierService;
import me.kadarh.mecaworks.service.EmployeService;
import me.kadarh.mecaworks.service.EnginService;
import me.kadarh.mecaworks.service.bons.BonEnginService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by salah on 3/5/18 10:15 PM.
 */
@Controller
@Slf4j
@RequestMapping("/saisi/engins")
public class BonEnginController {

	private final BonEnginService bonEnginService;
	private final EmployeService employeService;
	private final ChantierService chantierService;
	private final EnginService enginService;
	private final BonEnginConfirmation bonEnginConfirmation;

    public BonEnginController(BonEnginService bonEnginService, EmployeService employeService, ChantierService chantierService, EnginService enginService, BonEnginConfirmation bonEnginConfirmation) {
        this.bonEnginService = bonEnginService;
        this.employeService = employeService;
		this.chantierService = chantierService;
		this.enginService = enginService;
		this.bonEnginConfirmation = bonEnginConfirmation;
	}

	@GetMapping("")
	public String list(Model model, Pageable pageable, @RequestParam(defaultValue = "") String search) {
		model.addAttribute("page", bonEnginService.getPage(pageable, search));
		model.addAttribute("search", search);
		return "saisi/engins/list";
	}

	@GetMapping("/add")
	public String addGet(Model model) {
		model.addAttribute("bonEngin", new BonEngin());
		model.addAttribute("chantiers", chantierService.getList());
		model.addAttribute("engins", enginService.getList());
		model.addAttribute("employes", employeService.getList());
		model.addAttribute("carburants", Carburant.values());
		return "saisi/engins/add";
	}

	@PostMapping("/add")
	public String add(Model model, @Valid BonEngin bonEngin, BindingResult result) {
        /*if dateBon < dateDernierBon
			- verifier la quantité
				si Q non logique ?
					return formulaire
				sinon
					return page confirmation ancien bon
		  sinon
		  	bon 3adi
		*/
		boolean x = false;
		boolean error = false;
		if (result.hasErrors()) {
			x = true;
		}
		if(bonEnginService.isAncienBon(bonEngin)){
			if(bonEnginService.checkQuantite(bonEngin)){
				model.addAttribute("bonEngin",bonEngin);
				return "saisi/engins/confirm";
			}else {
				x = true;
				model.addAttribute("hasError", true);
			}
		}else {
			if (error = bonEnginService.hasErrors(bonEngin)) {
				model.addAttribute("hasError", error);
				x = true;
			} else if (bonEnginService.hasErrorsAttention(bonEngin)) {
					model.addAttribute("bonEngin", bonEngin);
					return "saisi/engins/confirm";
				}
		}
		if(x){
			model.addAttribute("chantiers", chantierService.getList());
			model.addAttribute("engins", enginService.getList());
			model.addAttribute("employes", employeService.getList());
			model.addAttribute("carburants", Carburant.values());
			return "saisi/engins/add";
		}
		bonEnginService.add(bonEngin);
		return "redirect:/saisi/engins";
	}

	@PostMapping("/confirmAncienBon")
	public String confirmAncienBon(Model model,@Valid BonEngin bonEngin, BindingResult result) {
		bonEnginService.add(bonEnginService.setCmpEnpanneAndChangeCode(bonEngin));
		return "redirect:/saisi/engins";
	}

	@PostMapping("/confirm")
	public String confirm(@RequestParam Integer confirmCode, Model model,
						  @Valid BonEngin bonEngin, BindingResult result) {
		if (result.hasErrors()) {
			return "saisi/engins/confirm";
		}
		if (!bonEnginConfirmation.isCorrect(confirmCode)) {
			model.addAttribute("error", true);
			return "saisi/engins/confirm";
		}
		bonEnginService.add(bonEngin);
		bonEnginConfirmation.generateNewCode();
		return "redirect:/saisi/engins";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id) {
		bonEnginService.delete(id);
		return "redirect:/saisi/engins";
	}
}
