package com.example.gladiateur

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val text_title = MutableLiveData<String>().apply {
        value = "Bienvenue dans Gladiator Maze"
    }
    val textTitle: LiveData<String> = text_title

    private val text_description = MutableLiveData<String>().apply {
        value = "Gladiateur Maze est un jeu de labyrinthe avec plusieurs niveaux. \nVous avez deux " +
                "modes de jeu : \n - Le mode histoire : Chaque défaite c'est un retour au niveau 1 \n" +
                " - Le mode arcade : Mode entrainement \n\n " +
                "Le but du jeu c'est d'échapper au Gladiateur ! Mais attention il se déplace deux " +
                "fois plus vite que vous !"
    }
    val textDescription: LiveData<String> = text_description

    private val text_player = MutableLiveData<String>().apply {
        value = "Votre joueur"
    }
    val textPlayer: LiveData<String> = text_player

    private val text_glad = MutableLiveData<String>().apply {
        value = "Votre ennemie"
    }
    val textGlad: LiveData<String> = text_glad

    private val text_good_luck = MutableLiveData<String>().apply {
        value = "Bon Courage \nRendez-vous à la sortie !"
    }
    val textGoodLuck: LiveData<String> = text_good_luck
}