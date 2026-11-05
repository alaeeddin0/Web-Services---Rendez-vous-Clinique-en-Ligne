<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\RendezVousController;

Route::post('/rendezvous', [RendezVousController::class, 'store']);
Route::get('/rendezvous', [RendezVousController::class, 'index']);
Route::put('/rendezvous/{id}/confirmer', [RendezVousController::class, 'confirmer']);
Route::put('/rendezvous/{id}/refuser', [RendezVousController::class, 'refuser']);
Route::put('/rendezvous/{id}/annuler', [RendezVousController::class, 'annuler']);