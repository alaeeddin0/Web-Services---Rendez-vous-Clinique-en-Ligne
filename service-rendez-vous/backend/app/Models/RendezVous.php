<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class RendezVous extends Model
{
    use HasFactory;

    protected $table = 'rendez_vous';
    
    // Désactiver les timestamps automatiques de Laravel
    public $timestamps = false;

    protected $fillable = [
        'creneau_id',
        'medecin_id',
        'patient_id',
        'statut',
        'date_creation'
    ];
}