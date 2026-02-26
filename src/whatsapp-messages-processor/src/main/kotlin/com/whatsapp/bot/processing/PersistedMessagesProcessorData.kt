package com.whatsapp.bot.processing

class PersistedMessagesProcessorData: MessagesProcessorData {
    override val initialMessages = TODO("Not yet implemented")
    override val goBackMessage = TODO("Not yet implemented")
    override val finishMessageAndFinishAnswer = TODO()

    override fun accepts(): Boolean {
        return System.getenv("MessagesProcessorData").equals("PersistedMessagesProcessorData", ignoreCase = true)
    }

    override fun getUserQuestions(givenUsername: String?): List<MessagesProcessor.Question> {
        TODO("Not yet implemented")
    }

    override fun getInitialQuestion(sender: String): MessagesProcessor.Question {
        TODO("Not yet implemented")
    }

    override fun getFinishAnswer(chosenOptions: List<Pair<MessagesProcessor.Question, MessagesProcessor.Option>>): String {
        TODO("Not yet implemented")
    }
}