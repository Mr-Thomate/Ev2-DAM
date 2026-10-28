package com.fabiancg.ejemplosfuncionesnativas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fabiancg.ejemplosfuncionesnativas.data.DocumentoRepository
import com.fabiancg.ejemplosfuncionesnativas.data.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BienvenidaViewModel(repositorio: DocumentoRepository) : ViewModel() {

    /**
     * null = todavía cargando; lista = ya llegaron los perfiles.
     * Distinguir "cargando" de "lista vacía" evita mostrar un mensaje falso durante el arranque.
     */
    val usuarios: StateFlow<List<Usuario>?> = repositorio.observarUsuarios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    class Factory(private val repositorio: DocumentoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BienvenidaViewModel(repositorio) as T
    }
}
