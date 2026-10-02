package com.zukesito.minigame

import android.content.res.Resources

interface WordProvider {
    fun getWords(): List<String>
}

class ResourceWordProvider(private val resources: Resources) : WordProvider {
    override fun getWords(): List<String> {
        return resources.getStringArray(R.array.hangman_words).toList()
    }
}