package com.clinique.service_notifications.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.clinique.service_notifications.dto.NotificationRequest;
import com.clinique.service_notifications.entity.Notification;
import com.clinique.service_notifications.exception.ResourceNotFoundException;
import com.clinique.service_notifications.model.TypeNotification;
import com.clinique.service_notifications.repository.NotificationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification(
                101, 10, TypeNotification.CONFIRMATION,
                "Votre rendez-vous est confirmé"
        );
    }

    @Test
    void testEnvoyerNotificationAvecSucces() {
        NotificationRequest request = new NotificationRequest();
        request.setRendezVousId(101);
        request.setPatientId(10);
        request.setType(TypeNotification.CONFIRMATION);
        request.setMessage("Votre rendez-vous est confirmé");

        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification resultat = service.envoyerNotification(request);

        assertNotNull(resultat);
        assertEquals(10, resultat.getPatientId());
        assertEquals(TypeNotification.CONFIRMATION, resultat.getType());
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void testEnvoyerNotificationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.envoyerNotification(null));
        verify(repository, never()).save(any());
    }

    @Test
    void testConsulterNotificationsPatient() {
        when(repository.findByPatientIdOrderByDateEnvoiDesc(10))
                .thenReturn(List.of(notification));

        List<Notification> resultat = service.consulterNotificationsPatient(10);

        assertEquals(1, resultat.size());
        assertEquals(10, resultat.get(0).getPatientId());
    }

    @Test
    void testConsulterNotificationsPatientIdInvalide() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consulterNotificationsPatient(0));
    }

    @Test
    void testRechercherNotificationExistante() {
        when(repository.findById(1)).thenReturn(Optional.of(notification));

        Notification resultat = service.rechercherNotification(1);

        assertNotNull(resultat);
        assertEquals(TypeNotification.CONFIRMATION, resultat.getType());
    }

    @Test
    void testRechercherNotificationInexistante() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.rechercherNotification(99));
    }

    @Test
    void testRemoveNotificationExistante() {
        when(repository.existsById(1)).thenReturn(true);

        assertTrue(service.removeNotification(1));
        verify(repository).deleteById(1);
    }

    @Test
    void testRemoveNotificationInexistante() {
        when(repository.existsById(99)).thenReturn(false);

        assertFalse(service.removeNotification(99));
        verify(repository, never()).deleteById(99);
    }
}
