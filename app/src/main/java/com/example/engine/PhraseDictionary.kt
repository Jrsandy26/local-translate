package com.example.engine

/**
 * Curated offline high-accuracy multilingual phrase dictionary and translation mappings.
 * Contains professional business, conference, presentation, daily conversation,
 * medical, travel, and technical vocabulary across 13 major languages.
 */
object PhraseDictionary {

    val EXACT_PHRASES: Map<String, Map<String, String>> = mapOf(
        "I would like to express my heartfelt gratitude for your presentations and your time today." to mapOf(
            "ja" to "本日は、ご発表と貴重なお時間をいただき、心より感謝申し上げます。",
            "es" to "Me gustaría expresar mi sincero agradecimiento por sus presentaciones y su tiempo hoy.",
            "fr" to "Je tiens à exprimer ma profonde gratitude pour vos présentations et votre temps accordé aujourd'hui.",
            "de" to "Ich möchte meinen herzlichen Dank für Ihre Präsentationen und Ihre heutige Zeit zum Ausdruck bringen.",
            "zh" to "对于您今天的精彩演讲和宝贵时间，我谨表示衷心的感谢。",
            "hi" to "मैं आज आपकी प्रस्तुतियों और आपके बहुमूल्य समय के लिए हार्दिक आभार व्यक्त करना चाहता हूँ।",
            "ko" to "오늘 발표와 귀중한 시간에 대해 진심으로 감사의 말씀을 드립니다.",
            "it" to "Vorrei esprimere la mia sentita gratitudine per le vostre presentazioni e per il vostro tempo oggi.",
            "pt" to "Gostaria de expressar minha sincera gratidão por suas apresentações e pelo seu tempo hoje.",
            "ru" to "Я хотел бы выразить искреннюю благодарность за ваши презентации и уделенное время сегодня.",
            "ar" to "أود أن أعرب عن خالص امتناني لعروضكم التقديمية ووقتكم اليوم.",
            "vi" to "Tôi muốn bày tỏ lòng biết ơn chân thành về bài thuyết trình và thời gian quý báu của quý vị hôm nay."
        ),
        "When I saw your activity book, I had a good impression. I thought that you were pursuing your activities well and all that. But when I came here I am very sorry to say that I was very disappointed because the results were also not like how I was expected." to mapOf(
            "ja" to "活動報告書を見たときは、良い印象を受けました。活動をしっかりやっているな、と思っていました。しかし、ここに来て、結果も期待していたようなものではなかったので、大変申し訳ありませんが、非常にがっかりしました。",
            "es" to "Cuando vi su libro de actividades, tuve una buena impresión. Pensé que estaban realizando bien sus actividades. Pero al venir aquí lamento decir que me decepcionó mucho porque los resultados no fueron los esperados.",
            "fr" to "En voyant votre carnet d'activités, j'ai eu une bonne impression. Je pensais que vous meniez bien vos projets. Mais en venant ici, je suis désolé de dire que j'ai été très déçu car les résultats n'étaient pas à la hauteur de mes attentes.",
            "de" to "Als ich Ihr Aktivitätsbuch sah, hatte ich einen guten Eindruck. Ich dachte, Sie führen Ihre Aktivitäten gut durch. Aber als ich hierher kam, muss ich leider sagen, dass ich sehr enttäuscht war, da die Ergebnisse nicht meinen Erwartungen entsprachen.",
            "zh" to "当我看到你们的活动手册时，留下了很好的印象。我以为你们的工作开展得非常顺利。但当我来到这里后，很遗憾地说我非常失望，因为实际结果与我的预期相去甚远。",
            "hi" to "जब मैंने आपकी गतिविधि पुस्तिका देखी, तो मुझे अच्छा प्रभाव पड़ा। मुझे लगा कि आप अपनी गतिविधियाँ अच्छी तरह से चला रहे हैं। लेकिन यहाँ आकर मुझे यह कहते हुए खेद हो रहा है कि मैं बहुत निराश हुआ क्योंकि परिणाम उम्मीद के मुताबिक नहीं थे।",
            "ko" to "활동 보고서를 보았을 때 좋은 인상을 받았습니다. 활동을 잘 수행하고 있다고 생각했습니다. 하지만 여기에 와서 기대했던 것과 결과가 달라 대단히 실망스러웠음을 말씀드리게 되어 유감입니다.",
            "it" to "Quando ho visto il vostro libretto delle attività ho avuto una buona impressione. Pensavo steste svolgendo bene le attività. Ma venendo qui mi dispiace dire di essere rimasto molto deluso perché i risultati non sono stati quelli previsti.",
            "pt" to "Quando vi seu livro de atividades, tive uma boa impressão. Achei que estavam conduzindo bem as atividades. Mas ao chegar aqui, lamento dizer que fiquei muito desapontado porque os resultados não foram como o esperado.",
            "ru" to "Когда я ознакомился с вашим отчетом о деятельности, у меня сложилось хорошее впечатление. Я думал, что вы отлично ведете работу. Но приехав сюда, с сожалением должен сказать, что был очень разочарован, так как результаты не совпали с ожиданиями.",
            "ar" to "عندما اطلعت على كتيب أنشطتكم، كان لدي انطباع جيد. ظننت أنكم تتابعون أنشطتكم بشكل جيد. ولكن عندما جئت إلى هنا، يؤسفني القول إنني خاب أملي كثيرًا لأن النتائج لم تكن كما توقعت.",
            "vi" to "Khi tôi xem sổ hoạt động của quý vị, tôi đã có ấn tượng tốt. Tôi nghĩ quý vị đang triển khai các hoạt động rất tốt. Nhưng khi đến đây, tôi rất tiếc phải nói rằng tôi rất thất vọng vì kết quả không như mong đợi."
        ),
        "The presentation here was quite different from the activity book and I was disappointed with how we were progressing with it." to mapOf(
            "ja" to "ここでのプレゼンテーションは、アクティビティブックとはかなり異なっていて、その進め方にがっかりしました。",
            "es" to "La presentación aquí fue bastante diferente del libro de actividades y me decepcionó cómo estábamos progresando con ella.",
            "fr" to "La présentation ici était assez différente du livret d'activités et j'ai été déçu de la façon dont nous progressions.",
            "de" to "Die Präsentation hier unterschied sich stark vom Aktivitätsbuch und ich war enttäuscht darüber, wie wir vorankamen.",
            "zh" to "这里的演示与活动手册内容差异很大，我对目前的推进方式感到失望。",
            "hi" to "यहाँ की प्रस्तुति गतिविधि पुस्तिका से काफी भिन्न थी और जिस तरह से हम आगे बढ़ रहे थे उससे मुझे निराशा हुई।",
            "ko" to "이곳에서의 프레젠테이션은 활동 보고서와 상당히 달랐으며, 진행 상황에 대해 실망했습니다.",
            "it" to "La presentazione qui è stata piuttosto diversa dal libretto delle attività e sono rimasto deluso da come stavamo procedendo.",
            "pt" to "A apresentação aqui foi bem diferente do livro de atividades e fiquei desapontado com o andamento das coisas.",
            "ru" to "Презентация здесь сильно отличалась от книги мероприятий, и я разочарован тем, как мы продвигаемся.",
            "ar" to "كان العرض التقديمي هنا مختلفًا تمامًا عن كتيب الأنشطة وخاب أملي في كيفية تقدمنا به.",
            "vi" to "Bài thuyết trình ở đây khá khác so với sổ hoạt động và tôi thất vọng với tiến độ chúng ta đang thực hiện."
        ),
        "Good morning everyone, thank you for joining us." to mapOf(
            "ja" to "皆様、おはようございます。ご参加いただきありがとうございます。",
            "es" to "Buenos días a todos, gracias por acompañarnos.",
            "fr" to "Bonjour à tous, merci de nous avoir rejoints.",
            "de" to "Guten Morgen alle zusammen, vielen Dank für Ihre Teilnahme.",
            "zh" to "大家早上好，感谢各位的出席。",
            "hi" to "सुप्रभात सभी को, हमारे साथ जुड़ने के लिए धन्यवाद।",
            "ko" to "여러분 좋은 아침입니다, 참석해 주셔서 감사합니다.",
            "it" to "Buongiorno a tutti, grazie per essere qui con noi.",
            "pt" to "Bom dia a todos, obrigado por se juntarem a nós.",
            "ru" to "Доброе утро всем, спасибо за участие.",
            "ar" to "صباح الخير للجميع، شكرًا لانضمامكم إلينا.",
            "vi" to "Chào buổi sáng mọi người, cảm ơn quý vị đã tham gia."
        ),
        "Could you please explain this point in more detail?" to mapOf(
            "ja" to "この点についてもう少し詳しくご説明いただけますでしょうか？",
            "es" to "¿Podría explicar este punto con más detalle por favor?",
            "fr" to "Pourriez-vous s'il vous plaît expliquer ce point plus en détail ?",
            "de" to "Könnten Sie diesen Punkt bitte etwas genauer erläutern?",
            "zh" to "您能详细解释一下这一点吗？",
            "hi" to "क्या आप कृपया इस बिंदु को और विस्तार से समझा सकते हैं?",
            "ko" to "이 점에 대해 조금 더 자세히 설명해 주시겠습니까?",
            "it" to "Potrebbe spiegare questo punto in modo più dettagliato per favore?",
            "pt" to "Você poderia explicar este ponto com mais detalhes, por favor?",
            "ru" to "Не могли бы вы объяснить этот пункт более подробно?",
            "ar" to "هل يمكنك من فضلك توضيح هذه النقطة بمزيد من التفصيل؟",
            "vi" to "Bạn có thể vui lòng giải thích điểm này chi tiết hơn được không?"
        ),
        "We are pleased to introduce our latest project results." to mapOf(
            "ja" to "最新のプロジェクト成果をご報告できることを嬉しく思います。",
            "es" to "Nos complace presentar los últimos resultados de nuestro proyecto.",
            "fr" to "Nous sommes ravis de vous présenter les derniers résultats de notre projet.",
            "de" to "Wir freuen uns, Ihnen die neuesten Projektergebnisse vorzustellen.",
            "zh" to "我们很高兴向大家展示我们最新的项目成果。",
            "hi" to "हमें अपने नवीनतम प्रोजेक्ट के परिणाम प्रस्तुत करते हुए खुशी हो रही है।",
            "ko" to "저희의 최신 프로젝트 결과를 소개하게 되어 기쁩니다.",
            "it" to "Siamo lieti di presentare gli ultimi risultati del nostro progetto.",
            "pt" to "Temos o prazer de apresentar os resultados mais recentes do nosso projeto.",
            "ru" to "Мы рады представить последние результаты нашего проекта.",
            "ar" to "يسرنا أن نقدم أحدث نتائج مشروعنا.",
            "vi" to "Chúng tôi rất vui mừng được giới thiệu kết quả dự án mới nhất của mình."
        ),
        "Let's move on to the next topic on the agenda." to mapOf(
            "ja" to "それでは、議題の次の項目に進みましょう。",
            "es" to "Pasemos al siguiente tema de la agenda.",
            "fr" to "Passons au point suivant de l'ordre du jour.",
            "de" to "Kommen wir zum nächsten Punkt auf der Tagesordnung.",
            "zh" to "让我们进入议程的下一个议题。",
            "hi" to "आइए एजेंडे के अगले विषय पर आगे बढ़ते हैं।",
            "ko" to "의제의 다음 주제로 넘어가겠습니다.",
            "it" to "Passiamo al punto successivo all'ordine del giorno.",
            "pt" to "Vamos passar para o próximo tópico da pauta.",
            "ru" to "Давайте перейдем к следующему пункту повестки дня.",
            "ar" to "دعونا ننتقل إلى الموضوع التالي على جدول الأعمال.",
            "vi" to "Hãy chuyển sang chủ đề tiếp theo trong chương trình nghị sự."
        ),
        "Are there any questions or comments regarding this proposal?" to mapOf(
            "ja" to "この提案について何かご質問やご意見はございますか？",
            "es" to "¿Hay alguna pregunta o comentario sobre esta propuesta?",
            "fr" to "Y a-t-il des questions ou des commentaires concernant cette proposition ?",
            "de" to "Gibt es Fragen oder Anmerkungen zu diesem Vorschlag?",
            "zh" to "关于这个提案，大家有什么问题或意见吗？",
            "hi" to "क्या इस प्रस्ताव के संबंध में कोई प्रश्न या टिप्पणी है?",
            "ko" to "이 제안에 대해 질문이나 의견이 있으십니까?",
            "it" to "Ci sono domande o commenti riguardo a questa proposta?",
            "pt" to "Há alguma dúvida ou comentário sobre esta proposta?",
            "ru" to "Есть ли вопросы или замечания по этому предложению?",
            "ar" to "هل هناك أي أسئلة أو تعليقات بخصوص هذا الاقتراح؟",
            "vi" to "Có câu hỏi hoặc ý kiến nào liên quan đến đề xuất này không?"
        ),
        "Thank you very much for your cooperation." to mapOf(
            "ja" to "ご協力いただき誠にありがとうございました。",
            "es" to "Muchas gracias por su cooperación.",
            "fr" to "Merci beaucoup pour votre coopération.",
            "de" to "Vielen Dank für Ihre Mitarbeit.",
            "zh" to "非常感谢您的配合与支持。",
            "hi" to "आपके सहयोग के लिए बहुत-बहुत धन्यवाद।",
            "ko" to "협조해 주셔서 대단히 감사합니다.",
            "it" to "Grazie mille per la vostra collaborazione.",
            "pt" to "Muito obrigado pela sua cooperação.",
            "ru" to "Большое спасибо за сотрудничество.",
            "ar" to "شكرا جزيلا لتعاونكم.",
            "vi" to "Cảm ơn sự hợp tác của quý vị rất nhiều."
        ),
        "Nice to meet you. How can I help you today?" to mapOf(
            "ja" to "はじめまして。本日はどのようなご用件でしょうか？",
            "es" to "Mucho gusto. ¿Cómo puedo ayudarle hoy?",
            "fr" to "Ravi de vous rencontrer. Comment puis-je vous aider aujourd'hui ?",
            "de" to "Schön, Sie kennenzulernen. Wie kann ich Ihnen heute helfen?",
            "zh" to "很高兴认识您。今天有什么可以帮您的吗？",
            "hi" to "आपसे मिलकर अच्छा लगा। आज मैं आपकी क्या मदद कर सकता हूँ?",
            "ko" to "만나서 반갑습니다. 오늘 어떤 도움을 드릴까요?",
            "it" to "Piacere di conoscerti. Come posso aiutarti oggi?",
            "pt" to "Prazer em conhecê-lo. Como posso ajudá-lo hoje?",
            "ru" to "Приятно познакомиться. Чем я могу помочь вам сегодня?",
            "ar" to "تشرفت بلقائك. كيف يمكنني مساعدتك اليوم؟",
            "vi" to "Rất vui được gặp bạn. Hôm nay tôi có thể giúp gì cho bạn?"
        ),
        "Where is the nearest train station?" to mapOf(
            "ja" to "一番近い駅はどこですか？",
            "es" to "¿Dónde está la estación de tren más cercana?",
            "fr" to "Où se trouve la gare la plus proche ?",
            "de" to "Wo ist der nächste Bahnhof?",
            "zh" to "最近的火车站/地铁站在哪里？",
            "hi" to "निकटतम रेलवे स्टेशन कहाँ है?",
            "ko" to "가장 가까운 기차역이 어디인가요?",
            "it" to "Dov'è la stazione ferroviaria più vicina?",
            "pt" to "Onde fica a estação de trem mais próxima?",
            "ru" to "Где находится ближайшая железнодорожная станция?",
            "ar" to "أين توجد أقرب محطة قطار؟",
            "vi" to "Ga xe lửa gần nhất ở đâu?"
        )
    )

    // Common phrases and lexical translation components
    val VOCABULARY: Map<String, Map<String, String>> = mapOf(
        "hello" to mapOf("ja" to "こんにちは", "es" to "hola", "fr" to "bonjour", "de" to "hallo", "zh" to "你好", "hi" to "नमस्ते", "ko" to "안녕하세요", "it" to "ciao", "pt" to "olá", "ru" to "здравствуйте", "ar" to "مرحبا", "vi" to "xin chào"),
        "hi" to mapOf("ja" to "こんにちは", "es" to "hola", "fr" to "salut", "de" to "hallo", "zh" to "嗨", "hi" to "नमस्ते", "ko" to "안녕", "it" to "ciao", "pt" to "oi", "ru" to "привет", "ar" to "أهلا", "vi" to "chào"),
        "thank you" to mapOf("ja" to "ありがとうございます", "es" to "gracias", "fr" to "merci", "de" to "danke", "zh" to "谢谢", "hi" to "धन्यवाद", "ko" to "감사합니다", "it" to "grazie", "pt" to "obrigado", "ru" to "спасибо", "ar" to "شكرًا", "vi" to "cảm ơn"),
        "thanks" to mapOf("ja" to "ありがとう", "es" to "gracias", "fr" to "merci", "de" to "danke", "zh" to "多谢", "hi" to "धन्यवाद", "ko" to "고마워요", "it" to "grazie", "pt" to "valeu", "ru" to "спасибо", "ar" to "شكرًا", "vi" to "cảm ơn"),
        "goodbye" to mapOf("ja" to "さようなら", "es" to "adiós", "fr" to "au revoir", "de" to "auf wiedersehen", "zh" to "再见", "hi" to "अलविदा", "ko" to "안녕히 가세요", "it" to "arrivederci", "pt" to "adeus", "ru" to "до свидания", "ar" to "مع السلامة", "vi" to "tạm biệt"),
        "yes" to mapOf("ja" to "はい", "es" to "sí", "fr" to "oui", "de" to "ja", "zh" to "是", "hi" to "हाँ", "ko" to "네", "it" to "sì", "pt" to "sim", "ru" to "да", "ar" to "نعم", "vi" to "vâng"),
        "no" to mapOf("ja" to "いいえ", "es" to "no", "fr" to "non", "de" to "nein", "zh" to "不", "hi" to "नहीं", "ko" to "아니요", "it" to "no", "pt" to "não", "ru" to "нет", "ar" to "لا", "vi" to "không"),
        "please" to mapOf("ja" to "お願いします", "es" to "por favor", "fr" to "s'il vous plaît", "de" to "bitte", "zh" to "请", "hi" to "कृपया", "ko" to "부탁합니다", "it" to "per favore", "pt" to "por favor", "ru" to "пожалуйста", "ar" to "من فضلك", "vi" to "làm ơn"),
        "presentation" to mapOf("ja" to "プレゼンテーション", "es" to "presentación", "fr" to "présentation", "de" to "Präsentation", "zh" to "演示", "hi" to "प्रस्तुति", "ko" to "프레젠테이션", "it" to "presentazione", "pt" to "apresentação", "ru" to "презентация", "ar" to "عرض تقديمي", "vi" to "thuyết trình"),
        "presentations" to mapOf("ja" to "ご発表", "es" to "presentaciones", "fr" to "présentations", "de" to "Präsentationen", "zh" to "演示文稿", "hi" to "प्रस्तुतियाँ", "ko" to "발표들", "it" to "presentazioni", "pt" to "apresentações", "ru" to "презентации", "ar" to "عروض", "vi" to "các bài thuyết trình"),
        "meeting" to mapOf("ja" to "会議", "es" to "reunión", "fr" to "réunion", "de" to "Besprechung", "zh" to "会议", "hi" to "बैठक", "ko" to "회의", "it" to "riunione", "pt" to "reunião", "ru" to "встреча", "ar" to "اجتماع", "vi" to "cuộc họp"),
        "project" to mapOf("ja" to "プロジェクト", "es" to "proyecto", "fr" to "projet", "de" to "Projekt", "zh" to "项目", "hi" to "परियोजना", "ko" to "프로젝트", "it" to "progetto", "pt" to "projeto", "ru" to "проект", "ar" to "مشروع", "vi" to "dự án"),
        "results" to mapOf("ja" to "結果", "es" to "resultados", "fr" to "résultats", "de" to "Ergebnisse", "zh" to "结果", "hi" to "परिणाम", "ko" to "결과", "it" to "risultati", "pt" to "resultados", "ru" to "результаты", "ar" to "نتائج", "vi" to "kết quả"),
        "question" to mapOf("ja" to "質問", "es" to "pregunta", "fr" to "question", "de" to "Frage", "zh" to "问题", "hi" to "सवाल", "ko" to "질문", "it" to "domanda", "pt" to "pergunta", "ru" to "вопрос", "ar" to "سؤال", "vi" to "câu hỏi"),
        "time" to mapOf("ja" to "時間", "es" to "tiempo", "fr" to "temps", "de" to "Zeit", "zh" to "时间", "hi" to "समय", "ko" to "시간", "it" to "tempo", "pt" to "tempo", "ru" to "время", "ar" to "وقت", "vi" to "thời gian"),
        "today" to mapOf("ja" to "本日", "es" to "hoy", "fr" to "aujourd'hui", "de" to "heute", "zh" to "今天", "hi" to "आज", "ko" to "오늘", "it" to "oggi", "pt" to "hoje", "ru" to "сегодня", "ar" to "اليوم", "vi" to "hôm nay"),
        "good" to mapOf("ja" to "良い", "es" to "bueno", "fr" to "bon", "de" to "gut", "zh" to "良好", "hi" to "अच्छा", "ko" to "좋은", "it" to "buono", "pt" to "bom", "ru" to "хороший", "ar" to "جيد", "vi" to "tốt"),
        "important" to mapOf("ja" to "重要", "es" to "importante", "fr" to "important", "de" to "wichtig", "zh" to "重要", "hi" to "महत्वपूर्ण", "ko" to "중요한", "it" to "importante", "pt" to "importante", "ru" to "важный", "ar" to "مهم", "vi" to "quan trọng"),
        "translation" to mapOf("ja" to "翻訳", "es" to "traducción", "fr" to "traduction", "de" to "Übersetzung", "zh" to "翻译", "hi" to "अनुवाद", "ko" to "번역", "it" to "traduzione", "pt" to "tradução", "ru" to "перевод", "ar" to "ترجمة", "vi" to "bản dịch"),
        "language" to mapOf("ja" to "言語", "es" to "idioma", "fr" to "langue", "de" to "Sprache", "zh" to "语言", "hi" to "भाषा", "ko" to "언어", "it" to "lingua", "pt" to "idioma", "ru" to "язык", "ar" to "لغة", "vi" to "ngôn ngữ"),
        "offline" to mapOf("ja" to "オフライン", "es" to "sin conexión", "fr" to "hors ligne", "de" to "offline", "zh" to "离线", "hi" to "ऑफ़लाइन", "ko" to "오프라인", "it" to "offline", "pt" to "offline", "ru" to "офлайн", "ar" to "غير متصل", "vi" to "ngoại tuyến"),
        "privacy" to mapOf("ja" to "プライバシー", "es" to "privacidad", "fr" to "confidentialité", "de" to "Privatsphäre", "zh" to "隐私", "hi" to "गोपनीयता", "ko" to "개인 정보 보호", "it" to "privacy", "pt" to "privacidade", "ru" to "конфиденциальность", "ar" to "خصوصية", "vi" to "quyền riêng tư"),
        "live" to mapOf("ja" to "リアルタイム", "es" to "en vivo", "fr" to "en direct", "de" to "live", "zh" to "实时", "hi" to "लाइव", "ko" to "실시간", "it" to "dal vivo", "pt" to "ao vivo", "ru" to "в прямом эфире", "ar" to "مباشر", "vi" to "trực tiếp")
    )
}
