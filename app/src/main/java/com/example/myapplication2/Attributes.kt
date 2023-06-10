package com.example.myapplication2

import kotlinx.serialization.Serializable


@Serializable
data class Attributes(
    val `Turn number`: String,
    val Difficulty: String,
    val `Time period of the day`: String,
    val `Current day number`: String,
    val Weather: String,
    val Health: String,
    val XP: String,
    val AC: String,
    val Level: String,
    val Location: String,
    val Description: String,
    val Gold: String,
    val Inventory: String,
    val Quest: String,
    val Abilities: String,
    val `- Persuasion`: String,
    val `- Strength`: String,
    val `- Intelligence`: String,
    val `- Dexterity`: String,
    val `- Luck`: String,
    val `Possible commands`: String,
    val Story: String?,
    val `Possible Commands`: Map<String, String>
)

