package com.example.service_creneaux.client;

import com.example.service_creneaux.exception.RessourceIntrouvableException;
import com.example.service_creneaux.exception.ServiceIndisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MedecinClient {

    private final RestClient restClient;
    

    public MedecinClient(
            @Value("${services.medecins.url}") String medecinsServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(medecinsServiceUrl)
                .build();
    }

    public void verifierExistence(Long medecinId) {
        try {
            restClient.get()
                    .uri("/medecins/{id}", medecinId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound exception) {
            throw new RessourceIntrouvableException(
                    "Médecin introuvable avec l'id : " + medecinId);

        } catch (ResourceAccessException | RestClientResponseException exception) {
            throw new ServiceIndisponibleException(
                    "Le service Médecins est indisponible");
        }
    }
}