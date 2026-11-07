package com.example.service_creneaux.service;

import com.example.service_creneaux.dto.CreneauRequest;
import com.example.service_creneaux.dto.CreneauResponse;
import com.example.service_creneaux.exception.RessourceIntrouvableException;
import com.example.service_creneaux.model.Creneau;
import com.example.service_creneaux.model.StatutCreneau;
import com.example.service_creneaux.repository.CreneauRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreneauServiceTest {

    @Mock
    private CreneauRepository creneauRepository;

    private CreneauService creneauService;

    @BeforeEach
    void initialiser() {
        creneauService = new CreneauService(creneauRepository);
    }

    @Test
    void creer_doitEnregistrerUnCreneauLibre_quandLesHeuresSontValides() {
        CreneauRequest request = new CreneauRequest(
                3L, LocalDate.of(2026, 11, 20),
                LocalTime.of(9, 0), LocalTime.of(9, 30));
        when(creneauRepository
                .existsByMedecinIdAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        3L, request.date(), request.heureFin(), request.heureDebut()))
                .thenReturn(false);
        when(creneauRepository.save(any(Creneau.class))).thenAnswer(invocation -> {
            Creneau creneau = invocation.getArgument(0);
            creneau.setId(1L);
            return creneau;
        });

        CreneauResponse resultat = creneauService.creer(request);

        assertEquals(1L, resultat.id());
        assertEquals(StatutCreneau.LIBRE, resultat.statut());
        ArgumentCaptor<Creneau> captor = ArgumentCaptor.forClass(Creneau.class);
        verify(creneauRepository).save(captor.capture());
        assertEquals(3L, captor.getValue().getMedecinId());
    }

    @Test
    void creer_doitRefuserQuandHeureFinEgaleHeureDebut_casLimite() {
        CreneauRequest request = new CreneauRequest(
                3L, LocalDate.of(2026, 11, 20),
                LocalTime.of(9, 0), LocalTime.of(9, 0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> creneauService.creer(request));

        assertEquals("L'heure de fin doit être après l'heure de début", exception.getMessage());
        verify(creneauRepository, never()).save(any(Creneau.class));
    }

    @Test
    void creer_doitRefuserUnChevauchement() {
        CreneauRequest request = new CreneauRequest(
                3L, LocalDate.of(2026, 11, 20),
                LocalTime.of(9, 15), LocalTime.of(9, 45));
        when(creneauRepository
                .existsByMedecinIdAndDateAndHeureDebutLessThanAndHeureFinGreaterThan(
                        eq(3L), eq(request.date()), eq(request.heureFin()), eq(request.heureDebut())))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> creneauService.creer(request));

        assertEquals("Ce créneau chevauche déjà un créneau existant", exception.getMessage());
        verify(creneauRepository, never()).save(any(Creneau.class));
    }

    @Test
    void reserver_doitPasserUnCreneauLibreAReserve() {
        Creneau creneau = creneauLibre();
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(creneau));
        when(creneauRepository.save(creneau)).thenReturn(creneau);

        CreneauResponse resultat = creneauService.reserver(1L);

        assertEquals(StatutCreneau.RESERVE, resultat.statut());
        verify(creneauRepository).save(creneau);
    }

    @Test
    void liberer_doitRefuserUnCreneauNonReserve() {
        Creneau creneau = creneauLibre();
        when(creneauRepository.findById(1L)).thenReturn(Optional.of(creneau));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> creneauService.liberer(1L));

        assertEquals("Seul un créneau réservé peut être libéré", exception.getMessage());
        assertFalse(creneau.getStatut() == StatutCreneau.RESERVE);
        verify(creneauRepository, never()).save(any(Creneau.class));
    }

    @Test
    void reserver_doitSignalerUnCreneauIntrouvable() {
        when(creneauRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RessourceIntrouvableException.class,
                () -> creneauService.reserver(99L));

        verify(creneauRepository, never()).save(any(Creneau.class));
    }

    private Creneau creneauLibre() {
        Creneau creneau = new Creneau(
                3L, LocalDate.of(2026, 11, 20),
                LocalTime.of(9, 0), LocalTime.of(9, 30));
        creneau.setId(1L);
        assertTrue(creneau.getStatut() == StatutCreneau.LIBRE);
        return creneau;
    }
}
