package com.whatsapp.bot.processing

import com.whatsapp.bot.persistence.Persister
import com.whatsapp.bot.rest.serverapi.ReceivedMessage
import java.text.Collator
import java.util.*

class MessagesProcessor {
    private val messageProcessorData = ServiceLoader.load(MessagesProcessorData::class.java).firstOrNull {
        it.accepts()
    } ?: MockMessagesProcessorData()

    init {
        val allQuestions = messageProcessorData.getUserQuestions("dummy")

        // A question option cannot point to its own question as next question,
        //      the only exception is if it is an option with denial and the question should be done again
        val noDenialQuestionOptionsPointingToItself = allQuestions.mapNotNull { question ->
            val options = question.options.filter {
                it.nextQuestionId == question.id &&
                it.denialAnswer == null
            }.ifEmpty { return@mapNotNull null }

            question to options
        }

        if (noDenialQuestionOptionsPointingToItself.isNotEmpty()) {
            throw RuntimeException("There are no denial question options that points to its own question, this is not allowed:\n" +
                noDenialQuestionOptionsPointingToItself.joinToString("\n\n") { (question, options) ->
                    "Question - $question\n" +
                    options.joinToString("\n") {
                        "Option - $it"
                    }
                }
            )
        }

        val allQuestionsIds = allQuestions.map { it.id }

        val optionsPointingToNoExistingQuestion = allQuestions.mapNotNull { question ->
            val options = question.options.filter {
                it.nextQuestionId != null &&
                it.nextQuestionId !in allQuestionsIds
            }.ifEmpty { return@mapNotNull null }

            question to options
        }

        if (optionsPointingToNoExistingQuestion.isNotEmpty()) {
            throw RuntimeException("There are question options that points to no existing questions as next question:\n" +
                optionsPointingToNoExistingQuestion.joinToString("\n\n") { (question, options) ->
                    "Question - $question\n" +
                    options.joinToString("\n") {
                        "Option - $it"
                    }
                }
            )
        }
    }

    data class MessageAnswer(
        val answer: String,
        val denialAnswer: String?,
        val isGoingBack: Boolean,
        val isLastAnswer: Boolean,
        val isRepeatingLastQuestion: Boolean
    )

    data class Question(
        val id: Int,
        val question: String,
        val options: List<Option>,

        /**
         * This is the specific information that the question is trying to find
         *      Examples:
         *          question - De qual cidade você é ?
         *          desiredInformation - Cidade
         *
         *          question - O que é o seu evento ?
         *          desiredInformation - Evento
         */
        val desiredInformation: String,

        val considerInFinalAnswer: Boolean = true
    )

    data class Option(
        val code: Int,
        val answer: String,
        val nextQuestionId: Int?,
        val denialAnswer: String? = null
    )

    private val collator = Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
    }

    fun getMessageAnswer(message: ReceivedMessage, history: List<Persister.ChatHistoryEntry>): MessageAnswer? {
        val sender = message.notifyName ?: return null

        val userQuestions = messageProcessorData.getUserQuestions(sender)

        var denialAnswer: String? = null
        var isGoingBack = false // This will repeat last but one question, erase last question and erase last user answer
        var isRepeatingLastQuestion = false // This will repeat last question and erase last user answer
        var isLastAnswer = false // This will display the returned answer and then reset chat

        val answer = run {
            if (history.isEmpty()) {
                if (messageProcessorData.initialMessages.any { collator.compare(it, message.body) == 0 }) {
                    messageProcessorData.getInitialQuestion(sender).question
                } else {
                    null
                }
            } else {
                if (
                    messageProcessorData.finishMessageAndFinishAnswer != null &&
                    collator.compare(message.body, messageProcessorData.finishMessageAndFinishAnswer!!.first) == 0
                ) {
                    return MessageAnswer(
                        answer = messageProcessorData.finishMessageAndFinishAnswer!!.second,
                        denialAnswer = null,
                        isGoingBack = false,
                        isLastAnswer = true,
                        isRepeatingLastQuestion = false
                    )
                }

                val isLastMessageFromBot = history.last().fromMe
                val lastButOneQuestion by lazy {
                    try {
                        history.filter { it.fromMe }.let {
                            it[it.size - 2]
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                if (
                    isLastMessageFromBot &&
                    collator.compare(message.body, messageProcessorData.goBackMessage) == 0 &&
                    lastButOneQuestion != null
                ) { // To avoid problems with GO_BACK message being sent in sequence to another, it will only actually go back if it is right after a bot question
                    isGoingBack = true
                    lastButOneQuestion!!.message
                } else {
                    val lastHistoryQuestion = history.lastOrNull { it.fromMe }?.message
                        ?: return@run messageProcessorData.getInitialQuestion(sender).question

                    val lastQuestion = userQuestions.first {
                        it.question == lastHistoryQuestion
                    }

                    val selectedOption = lastQuestion.options.firstOrNull {
                        message.body == it.code.toString() ||
                        collator.compare(message.body, it.answer) == 0
                    } ?: return@run null

                    if (selectedOption.denialAnswer != null) {
                        denialAnswer = selectedOption.denialAnswer

                        if (selectedOption.nextQuestionId == lastQuestion.id) {
                            isRepeatingLastQuestion = true
                            lastQuestion.question
                        } else {
                            isLastAnswer = true
                            selectedOption.denialAnswer
                        }
                    } else {
                        val nextQuestionId = selectedOption.nextQuestionId
                        if (nextQuestionId == null) {
                            isLastAnswer = true
                            return@run getFinishAnswer(message, history, userQuestions)
                        }

                        if (nextQuestionId == lastQuestion.id) {
                            val questionWithOption = listOf(lastQuestion to listOf(selectedOption))
                            throw RuntimeException("There are no denial question options that points to its own question, this is not allowed:\n" +
                                questionWithOption.joinToString("\n\n") { (question, options) ->
                                    "Question - $question\n" +
                                        options.joinToString("\n") {
                                            "Option - $it"
                                        }
                                }
                            )
                        }

                        val nextQuestion = userQuestions.firstOrNull { it.id == nextQuestionId }
                        if (nextQuestion == null) {
                            val questionWithOption = listOf(lastQuestion to listOf(selectedOption))
                            throw RuntimeException("There are question options that points to no existing questions as next question:\n" +
                                questionWithOption.joinToString("\n\n") { (question, options) ->
                                    "Question - $question\n" +
                                        options.joinToString("\n") {
                                            "Option - $it"
                                        }
                                }
                            )
                        }

                        nextQuestion.question
                    }
                }
            }
        } ?: return null

        return MessageAnswer(answer, denialAnswer, isGoingBack, isLastAnswer, isRepeatingLastQuestion)
    }

    private fun getFinishAnswer(
        message: ReceivedMessage,
        history: List<Persister.ChatHistoryEntry>,
        userQuestions: List<Question>
    ): String {
        val chosenOptions = userQuestions.filter {
            it.considerInFinalAnswer
        }.mapNotNull { question ->
            val questionHistoryIndex = history.firstOrNull {
                it.message == question.question
            }?.index ?: return@mapNotNull null

            val answer = if (questionHistoryIndex == history.maxOf { it.index }) {
                message.body
            } else {
                history.first {
                    it.index == questionHistoryIndex + 1
                }.message
            }

            val chosenOption = question.options.first {
                answer == it.code.toString() ||
                collator.compare(answer, it.answer) == 0
            }

            question to chosenOption
        }

        return messageProcessorData.getFinishAnswer(chosenOptions)
    }

    fun isMessageGeneratedByBot(message: ReceivedMessage, history: List<Persister.ChatHistoryEntry>): Boolean {
        val username = history.firstOrNull { !it.fromMe }?.senderName

        val questions = messageProcessorData.getUserQuestions(username).map { it.question }

        return message.body in questions
    }
}