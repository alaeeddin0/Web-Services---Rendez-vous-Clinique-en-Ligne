<?php

namespace App\Http\Controllers;

use App\Models\RendezVous;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class RendezVousController extends Controller
{
    private string $creneauServiceUrl;
    private ?string $notificationServiceUrl;

    public function __construct()
    {
        $this->creneauServiceUrl = config('services.creneaux.url');
        $this->notificationServiceUrl = config('services.notifications.url');
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

        // Réserver d'abord : aucun rendez-vous ne doit exister si le créneau
        // est absent, déjà réservé, ou si le service Créneaux est indisponible.
        try {
            $creneau = Http::timeout(3)
                ->acceptJson()
                ->put("{$this->creneauServiceUrl}/{$validated['creneau_id']}/reserver")
                ->throw()
                ->json();
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Impossible de réserver le créneau',
                'details' => $e->getMessage(),
            ], 409);
        }

        // Le rendez-vous doit correspondre au médecin propriétaire du créneau.
        if (($creneau['medecinId'] ?? null) !== $validated['medecin_id']) {
            $this->libererCreneau($validated['creneau_id']);

            return response()->json([
                'message' => 'Le créneau ne correspond pas au médecin demandé',
            ], 422);
        }

        try {
            $rdv = RendezVous::create([
                'creneau_id' => $validated['creneau_id'],
                'medecin_id' => $validated['medecin_id'],
                'patient_id' => $validated['patient_id'],
                'statut' => 'EN_ATTENTE',
            ]);
        } catch (\Exception $e) {
            $this->libererCreneau($validated['creneau_id']);
            Log::error('Création du rendez-vous impossible après réservation', [
                'exception' => $e->getMessage(),
            ]);

            return response()->json([
                'message' => 'Impossible de créer le rendez-vous',
            ], 500);
        }

        return response()->json($rdv, 201);
    }

    // 3. Confirmer un RDV (Médecin)
    public function confirmer($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'CONFIRME';
        $rdv->save();

        $this->notifier($rdv->id, $rdv->patient_id, 'CONFIRMATION', "Votre RDV #{$rdv->id} a été confirmé.");

        return response()->json(['message' => 'RDV Confirmé', 'data' => $rdv], 200);
    }

    // 4. Refuser un RDV (Médecin)
    public function refuser($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'REFUSE';
        $rdv->save();

        $this->libererCreneau($rdv->creneau_id);
        $this->notifier($rdv->id, $rdv->patient_id, 'REFUS', "Votre RDV #{$rdv->id} a été refusé.");

        return response()->json(['message' => 'RDV Refusé', 'data' => $rdv], 200);
    }

    // 5. Annuler un RDV (Patient)
    public function annuler($id)
    {
        $rdv = RendezVous::findOrFail($id);
        $rdv->statut = 'ANNULE';
        $rdv->save();

        $this->libererCreneau($rdv->creneau_id);
        $this->notifier($rdv->id, $rdv->patient_id, 'REFUS', "Votre RDV #{$rdv->id} a été annulé.");

        return response()->json(['message' => 'RDV Annulé', 'data' => $rdv], 200);
    }

    private function libererCreneau(int $creneauId): void
    {
        try {
            Http::timeout(3)
                ->put("{$this->creneauServiceUrl}/{$creneauId}/liberer")
                ->throw();
        } catch (\Exception $e) {
            Log::warning('Impossible de libérer le créneau', [
                'creneau_id' => $creneauId,
                'exception' => $e->getMessage(),
            ]);
        }
    }

    private function notifier(int $rendezVousId, int $patientId, string $type, string $message): void
    {
        if (!config('services.notifications.enabled')) {
            return;
        }

        try {
            Http::timeout(3)->post($this->notificationServiceUrl, [
                'rendezVousId' => $rendezVousId,
                'patientId' => $patientId,
                'type' => $type,
                'message' => $message,
            ])->throw();
        } catch (\Exception $e) {
            Log::warning('Impossible de joindre le service Notifications', [
                'exception' => $e->getMessage(),
            ]);
        }
    }
}
