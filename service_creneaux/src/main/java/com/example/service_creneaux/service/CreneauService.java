package com.example.service_creneaux.service;

import com.example.service_creneaux.dto.CreneauRequest;
import com.example.service_creneaux.dto.CreneauResponse;
import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;
import com.example.service_creneaux.repository.CreneauRepository;
import org.springframework.stereotype.Service;
import com.example.service_creneaux.exception.RessourceIntrouvableException;
import java.util.List;
import com.example.service_creneaux.dto.DisponibiliteResponse;

@Service
public class CreneauService {

    private final CreneauRepository creneauRepository;

    public CreneauService(CreneauRepository creneauRepository) {
        this.creneauRepository = creneauRepository;
    }

    public CreneauResponse creer(CreneauRequest request) {
        verifierHeures(request);
        verifierChevauchement(request);

        Creneau creneau = new Creneau(
                request.medecinId(),
                request.date(),
                request.heureDebut(),
                request.heureFin());

        return CreneauResponse.from(creneauRepository.save(creneau));
    }

    public List<CreneauResponse> lister() {
        return creneauRepository.findAll()
                .stream()
                .map(CreneauResponse::from)
                .toList();
    }
    
    public DisponibiliteResponse verifierDisponibilite(Long id) {
        Creneau creneau = trouverEntiteParId(id);

        return new DisponibiliteResponse(
                creneau.getStatut() == StatutCreneau.LIBRE);
    }
    
    public CreneauResponse reserver(Long id) {
        Creneau creneau = trouverEntiteParId(id);

        if (creneau.getStatut() != StatutCreneau.LIBRE) {
            throw new IllegalStateException(
                    "Seul un créneau libre peut être réservé");
        }

        creneau.setStatut(StatutCreneau.RESERVE);

        return CreneauResponse.from(creneauRepository.save(creneau));
    }
    
    public CreneauResponse liberer(Long id) {
        Creneau creneau = trouverEntiteParId(id);

        if (creneau.getStatut() != StatutCreneau.RESERVE) {
            throw new IllegalStateException(
                    "Seul un créneau réservé peut être libéré");
        }

        creneau.setStatut(StatutCreneau.LIBRE);

        return CreneauResponse.from(creneauRepository.save(creneau));
    }

    public CreneauResponse trouverParId(Long id) {
        return CreneauResponse.from(trouverEntiteParId(id));
    }

    public CreneauResponse modifier(Long id, CreneauRequest request) {
        Creneau creneau = trouverEntiteParId(id);

        if (creneau.getStatut() == StatutCreneau.RESERVE) {
            throw new IllegalStateException(
                    "Un créneau réservé ne peut pas être modifié");
        }

        verifierHeures(request);

        /*
         * La vérification complète de chevauchement à la modification
         * sera améliorée juste après le contrôleur, car il faut exclure
         * le créneau courant de la recherche.
         */
        creneau.setMedecinId(request.medecinId());
        creneau.setDate(request.date());
        creneau.setHeureDebut(request.heureDebut());
        creneau.setHeureFin(request.heureFin());

        return CreneauResponse.from(creneauRepository.save(creneau));
    }

    public void supprimer(Long id) {
        Creneau creneau = trouverEntiteParId(id);

        if (creneau.getStatut() == StatutCreneau.RESERVE) {
            throw new IllegalStateException(
                    "Un créneau réservé ne peut pas être supprimé");
        }

        creneauRepository.delete(creneau);
    }

    private Creneau trouverEntiteParId(Long id) {
        return creneauRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Créneau introuvable avec l'id : " + id));
    }

    private void verifierHeures(CreneauRequest request) {
        if (!request.heureFin().isAfter(request.heureDebut())) {
            throw new IllegalArgumentException(
                    "L'heure de fin doit être après l'heure de début");
        }
    }

    private void verifierChevauchement(CreneauRequest request) {
        boolean chevauchement = creneauRepository
                .existsByMedecinIdAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        request.medecinId(),
                        request.date(),
                        request.heureFin(),
                        request.heureDebut());

        if (chevauchement) {
            throw new IllegalStateException(
                    "Ce créneau chevauche déjà un créneau existant");
        }
    }
}