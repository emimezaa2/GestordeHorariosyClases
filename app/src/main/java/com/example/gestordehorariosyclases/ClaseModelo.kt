package com.example.gestordehorariosyclases

data class ClaseModelo(
    var id: Int = 0,
    var nombre: String = "",
    var profesor: String = "",
    var salon: String = "",
    var dias: String = "",
    var horaInicio: String = "",
    var horaFin: String = "",
    var color: String = "#6200EE" // morado
)