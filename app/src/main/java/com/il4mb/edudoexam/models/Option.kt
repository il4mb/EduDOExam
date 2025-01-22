package com.il4mb.edudoexam.models

data class Option(
    val label: String,
    var text: String,
    var isCorrect: Boolean = false,
) {

     companion object {
         fun indexLetters(index: Int): String {
             var currentIndex = index
             val letters = StringBuilder()
             do {
                 letters.insert(0, 'A' + (currentIndex % 26))
                 currentIndex = (currentIndex / 26) - 1
             } while (currentIndex >= 0)
             return letters.toString()
         }
     }
}
