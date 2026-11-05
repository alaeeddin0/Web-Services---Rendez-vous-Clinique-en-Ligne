<?php

namespace App\Http\Controllers;

use App\Models\RendezVous;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class RendezVousController extends Controller
{
    private string $creneauServiceUrl;
    private string $notificationServiceUrl;

    public function __construct()
    {
        // Récupération des URLs depuis le fichier .env (avec valeurs par défaut)
        $this->creneauServiceUrl = env('SERVICE_CRENEAUX_URL', 'http://localhost:8001/api/creneaux');
        $this->notificationServiceUrl = env('SERVICE_NOTIFICATIONS_URL', 'http://localhost:8003/api/notifications');
    }

    // 1. Lister tous les rendez-vous
    public function index()
    {
        return response()->json(RendezVous::all(), 200);
    }

    // 2. Créer un rendez-vous (Patient)
    public function store(Request $request)
    {
        $validated = $request->validate([
            'creneau_id' => 'required|integer',
            'medecin_id' => 'required|integer',
            'patient_id' => 'required|integer',
        ]);

        // A. Création du RDV dans ta base de données
        $rdv = RendezVous::create([
            'creneau_id' => $validated['creneau_id'],
            'medecin_id' => $validated['medecin_id'],
            'patient_id' => $validated['patient_id'],
            'statut'     => 'EN_ATTENTE',
        ]);

        // B. Demander au Service Créneaux de marquer le créneau comme réservé
        try {
            Http::timeout(3)->put("{$this->creneauServiceUrl}/{$validated['creneau_id']}/reserver");
        } catch (\Exception $e) {
            logger()->warning("Impossible de joindre le Service Créneaux: " . $e->getMessage());
        }

        // C. Demander au Service Notifications d'avertir le médecin
        try {
            Http::timeout(3)->post($this->notificationServiceUrl, [
                'destinataire_id' => $validated['medecin_id'],
                'type'            => 'DEMANDE_RDV',
                'message'         => "Nouvelle demande de RDV #{$rdv->id}",
            ]);
        } catch (\Exception $e) {
            logger()->warning("Impossible de joindre le Service Notifications: " . $e->getMessage());
        }

        return response()->json($rdv, 201);
    }

    // 3. Confirmer un RDV (Médecin)
    public function confirmer($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'CONFIRME';
        $rdv->save();

        // Notifier le patient que son RDV a été accepté
        try {
            Http::timeout(3)->post($this->notificationServiceUrl, [
                'destinataire_id' => $rdv->patient_id,
                'type'            => 'RDV_CONFIRME',
                'message'         => "Votre RDV #{$rdv->id} a été confirmé.",
            ]);
        } catch (\Exception $e) {
            logger()->warning("Impossible de joindre le Service Notifications: " . $e->getMessage());
        }

        return response()->json(['message' => 'RDV Confirmé', 'data' => $rdv], 200);
    }

    // 4. Refuser un RDV (Médecin)
    public function refuser($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'REFUSE';
        $rdv->save();

        // Libérer le créneau dans le Service Créneaux
        try {
            Http::timeout(3)->put("{$this->creneauServiceUrl}/{$rdv->creneau_id}/liberer");
        } catch (\Exception $e) {
            logger()->warning("Impossible de joindre le Service Créneaux: " . $e->getMessage());
        }

        return response()->json(['message' => 'RDV Refusé', 'data' => $rdv], 200);
    }

    // 5. Annuler un RDV (Patient)
    public function annuler($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'ANNULE';
        $rdv->save();

        // Libérer le créneau dans le Service Créneaux
        try {
            Http::timeout(3)->put("{$this->creneauServiceUrl}/{$rdv->creneau_id}/liberer");
        } catch (\Exception $e) {
            logger()->warning("Impossible de joindre le Service Créneaux: " . $e->getMessage());
        }

        return response()->json(['message' => 'RDV Annulé', 'data' => $rdv], 200);
    }
}