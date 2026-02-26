package com.whatsapp.bot.processing

import com.whatsapp.bot.processing.MessagesProcessor.Question
import com.whatsapp.bot.processing.MessagesProcessor.Option

class MockMessagesProcessorData: MessagesProcessorData {
    private val defaultDenialAnswer = "Nossa equipe já irá te atender!"

    override val initialMessages = listOf(
        "Olá",
        "Oi",
        "Bom dia",
        "Boa tarde",
        "Boa noite",
        "Gostaria de um orçamento"
    )

    override val goBackMessage = "Voltar"

    override val finishMessageAndFinishAnswer = "Encerrar" to "Chat automático encerrado."

    private val manualComboOption = Option(2, "Montar o próprio cardápio", null)

    override fun accepts(): Boolean {
        return System.getenv("MessagesProcessorData").let {
            it == null ||
            it.equals("MockMessagesProcessorData", ignoreCase = true)
        }
    }

    override fun getUserQuestions(givenUsername: String?): List<Question> {
        val username = givenUsername ?: "Prezado(a)"

        return listOf(
            getInitialQuestion(username),
            getSecondQuestion(username),
            getThirdQuestion(username),
            getFourthQuestion(username),
            getFifthQuestion(username),
            getSixthDotOneQuestion(username),
            getSixthDotOneDotOneQuestion(username),
            getSixthDotOneDotTwoQuestion(username),
            getSixthDotOneDotThreeQuestion(username)
        )
    }

    override fun getInitialQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "Sim", 2, null),
            Option(2, "Já tenho um evento marcado", null, defaultDenialAnswer)
        )

        val question =
            "*Bem-vindo a Caravan Drinks 🍸*\n\n" +
            "Gostaria de fazer um orçamento, $sender ?\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(1, question, options, "", considerInFinalAnswer = false)
    }

    private fun getSecondQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "Sorocaba", 3),
            Option(2, "Votorantim", 3),
            Option(3, "Araçoiaba da Serra", 3),
            Option(4, "Salto", 3),
            Option(5, "Iperó", 3),
            Option(6, "Boituva", 3),
            Option(7, "Tatuí", 3),
            Option(8, "Porto Feliz", 3),
            Option(9, "Itu", 3),
            Option(10, "Outras localidades", null, defaultDenialAnswer)
        )

        val question = "*Qual seria o local do evento ?*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(2, question, options, "Local")
    }

    private fun getThirdQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "30 a 40", 4),
            Option(2, "50 a 60", 4),
            Option(3, "70 a 80", 4),
            Option(4, "90 a 100", 4),
            Option(5, "110 a 120", 4),
            Option(6, "130 a 140", 4),
            Option(7, "150 a 160", 4),
            Option(8, "170 a 180", 4),
            Option(9, "190 a 200", 4),
            Option(10, "Acima de 200", 4)
        )

        val question = "*Número de convidados:*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(3, question, options, "Número de convidados")
    }

    private fun getFourthQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "*Serviço completo:*\n- Balcão\n- Bartenders\n- Equipamento completo\n- Frutas\n- Destilados\n- Copos e insumos em geral", 5),
            Option(2, "*Serviço de bartender:*\n- Bartenders\n- Materiais por conta do contratante", null),
            Option(3, "*Serviço de bar com balcão:*\n- Balcão\n- Bartenders\n- Materiais por conta do contratante", null),
            Option(4, "*Serviço de bartender com materiais:*\n- Bartenders\n- Equipamento completo\n- Frutas\n- Destilados\n- Copos e insumos no geral", 5)
        )

        val question = "*Tipos de serviço: 4h de serviço e todos acompanham menu personalizado*\n" +
            options.joinToString("\n\n") { "${it.code} - ${it.answer}" }

        return Question(4, question, options, "Tipo de serviço")
    }

    private fun getFifthQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "Combos", 61),
            manualComboOption
        )

        val question = "*Gostaria de ver nossos combos ou montar o próprio cardápio ?*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(5, question, options, "Combo ou cardápio montado")
    }

    private fun getSixthDotOneQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "Com e sem álcool", 611),
            Option(2, "Somente com álcool", 612),
            Option(3, "Somente sem álcool", 613)
        )

        val question = "*Tipos de drinks:*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(61, question, options, "", considerInFinalAnswer = false)
    }

    private fun getSixthDotOneDotOneQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "*Combo Sunset:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Gin tropical\n- Sex on the beach\n- Tinto de verano\n_Sem álcool:_\n- Batida de morango,\n- Brisa do mar\n- Soda italiana", null),
            Option(2, "*Combo Clássico:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Sex on the beach\n- Espanhola\n_Sem álcool:_\n- Soda italiana\n- Brisa do mar\n- Espanhola zero", null),
            Option(3, "*Combo Favoritos:*\n_Com álcool:_\n- Sakerita, Caipiroska ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Sex on the beach\n- Gin tropical\n- Espanhola\n_Sem álcool:_\n- Soda italiana\n- Brisa do mar\n- Espanhola zero", null),
            Option(4, "*Festival de Caipirinhas:*\n_Com álcool:_\n- Sakerita, Caipiroska ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Caipirinha cremosa\n_Sem álcool:_\n- Soda italiana\n- Brisa do mar\n- Espanhola zero", null),
            Option(5, "*Combo Top:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Gin tropical\n- Aperol spritz\n- Moscow mule\n_Sem álcool:_\n- Pinã zero\n- Brisa do mar\n- Moscow mule zero", null),
            Option(6, "*Combo Party:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Sex on the beach\n- Gin sensação\n- Pink\n_Sem álcool:_\n- Batida de morango\n- Brisa do mar\n- Mojito zero", null),
            Option(7, "*Combo Super:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Negroni\n- Moscow mule\n- Tinto de verano\n_Sem álcool:_\n- Pinã zero\n- Soda italiana\n- Moscow mule zero", null),
            Option(8, "*Combo Tropical:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Mojito\n- Pinã colada\n- Tequila sunrise\n_Sem álcool:_\n- Pinã zero\n- Brisa do mar\n- Mojito zero", null),
            Option(9, "*Combo Ouro:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Clericot\n- Pinã colada\n- Moscow mule\n_Sem álcool:_\n- Batida de morango\n- Soda italiana\n- Moscow mule zero", null),
            Option(10, "*Combo Prata:*\n_Com álcool:_\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Margarita\n- Tequila sunrise\n- Tinto de verano\n_Sem álcool:_\n- Mojito zero\n- Limonada suíça\n- Soda italiana", null)
        )

        val question = "*Tipos de combos com e sem álcool:*\n" +
            options.joinToString("\n\n") { "${it.code} - ${it.answer}" }

        return Question(611, question, options, "Combo")
    }

    private fun getSixthDotOneDotTwoQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "*Combo Festa:*\n- Sakerita, Caipiroska ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Sex on the beach\n- Espanhola", null),
            Option(2, "*Combo Verão:*\n- Caipirinha (morango, limão, abacaxi e maracujá)\n- Mojito\n- Pinã colada\n- Tequila sunrise\n- Tinto de verano", null),
            Option(3, "*Combo Luxo:*\n- Sakerita ou Caipirinha (morango, limão, abacaxi e maracujá)\n- Gin tropical\n- Aperol spritz\n- Moscow mule", null)
        )

        val question = "*Tipos de combos com álcool:*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(612, question, options, "Combo")
    }

    private fun getSixthDotOneDotThreeQuestion(sender: String): Question {
        val options = listOf(
            Option(1, "*Combo Kids:*\n- Soda italiana\n- Pinã descolada\n- Brisa do mar\n- Batida de morango\n- Tropical\n- Limonada suíça", null),
            Option(2, "*Combo Zero:*\n- Soda italiana\n- Pink limonade\n- Brisa do mar\n- Espanhola\n- Mojito zero\n- Batida de morango", null),
            Option(3, "*Combo 15 Anos:*\n- Soda italiana\n- Pinã descolada\n- Brisa do mar\n- Espanhola\n- Mojito zero\n- Power", null)
        )

        val question = "*Tipos de combos sem álcool:*\n" +
            options.joinToString("\n") { "${it.code} - ${it.answer}" }

        return Question(613, question, options, "Combo")
    }

    override fun getFinishAnswer(chosenOptions: List<Pair<Question, Option>>): String {
        val comboOrManualQuestionId = getFifthQuestion("").id
        val comboOrManualChosenOption = chosenOptions.firstOrNull {
            it.first.id == comboOrManualQuestionId
        }?.second

        val additionalMsg = if (comboOrManualChosenOption?.code != manualComboOption.code) {
            ""
        } else {
            "\n\n" +
            "*Monte seu próprio cardápio com essas opções enquanto isso:*\n" +
            "_Drinks com álcool:_\n" +
            "- Sakerita, Caipiroska ou Caipirinha (morango, limão, abacaxi e maracujá)\n" +
            "- Gin Tropical\n" +
            "- Aperol Spritz\n" +
            "- Moscow mule\n" +
            "- Clericot\n" +
            "- Pink\n" +
            "- Gin tônica\n" +
            "- Gin sensação\n" +
            "- Blue lagoon\n" +
            "- Tinto de verano\n" +
            "- Caipirinha caldo de cana\n" +
            "- Caipirinha cremosa\n" +
            "- Pinã colada\n" +
            "- Margarita\n" +
            "- Tequila sunrise\n" +
            "- Mojito\n" +
            "- Sex on the beach\n" +
            "- Espanhola\n" +
            "- Negroni\n" +
            "- Cosmopolitan" +
            "\n\n" +
            "_Drinks sem álcool:_\n" +
            "- Soda italiana\n" +
            "- Pinã descolada\n" +
            "- Brisa do mar\n" +
            "- Espanhola zero\n" +
            "- Mojito zero\n" +
            "- Limonada suíça\n" +
            "- Batida de morango\n" +
            "- Pink lemonade\n" +
            "- Moscow mule zero\n" +
            "- Power"
        }

        return "*RESUMO*\n" +
            chosenOptions.joinToString("\n\n") {
                "*${it.first.desiredInformation}:*\n" +
                it.second.answer
            } +
            "\n\n" +
            defaultDenialAnswer +
            additionalMsg
    }
}