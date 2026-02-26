package com.whatsapp.bot.processing

interface MessagesProcessorData {
    fun accepts(): Boolean

    val initialMessages: List<String>
    val goBackMessage: String?
    val finishMessageAndFinishAnswer: Pair<String, String>?

    fun getUserQuestions(givenUsername: String?): List<MessagesProcessor.Question>
    fun getInitialQuestion(sender: String): MessagesProcessor.Question
    fun getFinishAnswer(chosenOptions: List<Pair<MessagesProcessor.Question, MessagesProcessor.Option>>): String
}