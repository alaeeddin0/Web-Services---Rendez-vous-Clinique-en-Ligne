package com.example.service_creneaux.service;

import com.example.service_creneaux.client.MedecinClient;
import com.example.service_creneaux.dto.CreneauRequest;
import com.example.service_creneaux.dto.CreneauResponse;
import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;
import com.example.service_creneaux.repository.CreneauRepository;
import org.springframework.stereotype.Service;
import com.example.service_creneaux.exception.RessourceIntrouvableException;

import java.time.LocalDate;
import java.util.List;
import com.example.service_creneaux.dto.DisponibiliteResponse;

@Service
public class CreneauService {

    private final CreneauRepository creneauRepository;
    private final MedecinClient medecinClient;

    public CreneauService(
            CreneauRepository creneauRepository,
            MedecinClient medecinClient) {
        this.creneauRepository = creneauRepository;
        this.medecinClient = medecinClient;
    }

    public CreneauResponse creer(CreneauRequest request) {
        medecinClient.verifierExistence(request.medecinId());
        verifierHeures(request);
        verifierChevauchement(request, null);

        Creneau creneau = new Creneau(
                request.medecinId(),
                request.date(),
                request.heureDebut(),
                request.heureFin());

        return CreneauResponse.from(creneauRepository.save(creneau));
    }

    public List<CreneauResponse> lister(
            Long medecinId,
            LocalDate date,
            StatutCreneau statut) {
        return creneauRepository.findAll()
                .stream()
                .filter(creneau -> medecinId == null
                        || creneau.getMedecinId().equals(medecinId))
                .filter(creneau -> date == null
                        || creneau.getDate().equals(date))
                .filter(creneau -> statut == null
                        || creneau.getStatut() == statut)
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
        verifierChevauchement(request, id);

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

    private void verifierChevauchement(CreneauRequest request, Long idExclu) {
        boolean chevauchement = creneauRepository.existeChevauchement(
                request.medecinId(),
                request.date(),
                request.heureFin(),
                request.heureDebut(),
                idExclu,
                StatutCreneau.ANNULE);

        if (chevauchement) {
            throw new IllegalStateException(
                    "Ce créneau chevauche déjà un créneau existant");
        }
    }
}