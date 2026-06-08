package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.api.GeminiApiManager
import com.example.data.db.AppDatabase
import com.example.data.db.ThesisSlide
import com.example.data.db.ThesisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Main ViewModel regulating the state of the Disaster Risk Reduction Advisor.
 * Combines dynamic simulator states, law guidance systems, Gemini academic assistant,
 * and presentation slides stored locally in SQLite using Room.
 */

data class ChatMessage(
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Sender {
    STUDENT, ADVISOR
}

enum class Prefecture(val labelAr: String, val labelFr: String) {
    MOHAMMEDIA("عمالة المحمدية", "Préfecture de Mohammedia"),
    AIN_SEBAA("عين السبع - الحي المحمدي", "Districts d'Ain Sebaâ-Hay Mohammadi"),
    SIDI_BERNOUSSI("مقاطعات سيدي البرنوصي", "Districts de Sidi Bernoussi")
}

class ThesisViewModel(context: Context) : ViewModel() {

    // --- Database Initialization ---
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "drr_thesis_advisor_db"
    ).build()

    private val repository = ThesisRepository(database.thesisSlideDao())
    val savedSlides = repository.allSlides.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- API Setup ---
    private val apiManager = GeminiApiManager()

    // --- Simulator State ---
    private val _selectedPrefecture = MutableStateFlow(Prefecture.MOHAMMEDIA)
    val selectedPrefecture = _selectedPrefecture.asStateFlow()

    private val _threatLevel = MutableStateFlow(0.4f) // Flood flow or Storm waves level
    val threatLevel = _threatLevel.asStateFlow()

    private val _resilienceLevel = MutableStateFlow(0.2f) // Sponge techniques level and FLCN assets
    val resilienceLevel = _resilienceLevel.asStateFlow()

    // --- Assistant Chat State ---
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = Sender.ADVISOR,
                text = """
                    مرحباً بك يا باحث المستقبل. أنا مرشدك الأكاديمي المتقدم المختص في **«الاستراتيجيات والتشريعات الوطنية والدولية للحد من مخاطر الكوارث الطبيعية»**.

                    تم ضبط توجيهاتي بدقة لدعم أطروحة الماستر الخاصة بك، والتي تركز على المجال الترابي لـ **(عمالة المحمدية، وعين السبع الحي المحمدي، وسيدي البرنوصي)** في المملكة المغربية.

                    أنا مجهز بالكامل لربط دراستك بـ:
                    1. **إطار سنداي الدولي (2015-2030)**: تحديداً «الأولوية الأولى» (فهم المخاطر) و«الأولوية الرابعة» (المدن الإسفنجية والتأهب للبناء بشكل أفضل).
                    2. **الاستراتيجية الوطنية لتدبير مخاطر الكوارث الطبيعية (2020-2030)** وتنزيلها الجهوي.
                    3. **قانون الماء 36.15**: حماية الفيضانات ومسؤوليات وكالة الحوض المائي (**ABH-OER**).
                    4. **قانون التعمير 12.90**: وثائق التعمير (SDAU) ومنع البناء في المناطق الطبوغرافية الهشة تحت 15 مترًا.
                    5. **صندوق FLCN**: كآلية وطنية مرنة لتمويل الوقاية والبنيات الإسفنجية.

                    *ملاحظة: تقتصر تحليلاتي بدقة على الكوارث الطبيعية (الفيضانات والانجراف الساحلي) وتستبعد أي مخاطر تكنولوجية.*

                    اختر أحد التساؤلات المقترحة بالأسفل أو اطرح سؤالك الأكاديمي المباشر!
                """.trimIndent()
            )
        )
    )
    val chatHistory = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // --- Dynamic Legal Alerts Generator ---
    val activeAlerts: StateFlow<List<String>> = combine(
        _selectedPrefecture,
        _threatLevel,
        _resilienceLevel
    ) { pref, threat, resilience ->
        val alerts = mutableListOf<String>()

        when (pref) {
            Prefecture.MOHAMMEDIA -> {
                if (threat > 0.5f) {
                    alerts.add("🔴 خطر فيضان نشط لواد المالح: التهديد يطال المناطق المنخفضة بشكل فوري ومباشر.")
                    alerts.add("⚖️ قانون الماء 36.15 (المادة 115): حيز مجرى واد المالح المحمي تقع مسؤولية تهيئته على عاتق وكالة الحوض المائي ABH-OER لحماية المحيط السكاني.")
                }
                if (threat > 0.2f && resilience < 0.3f) {
                    alerts.add("⚠️ قانون التعمير 12.90: ارتفاع منسوب التهديد المائي يكشف ضعف خطة تدبير النطاق الساحلي والسهلي بالمحمدية لعدم احترام الحظر الصارم للبناء تحت 15 مترًا.")
                }
                if (resilience > 0.6f) {
                    alerts.add("🟢 استجابة مفعّلة لأولوية سنداي 4: حقول الامتصاص وبنيات المدن الإسفنجية تنجح في تمديد زمن تدفق مياه السيل وحماية المحيط.")
                    alerts.add("💸 معيار FLCN لتمويل الوقاية: تصنيف التدخل الإسفنجي كتدبير وقائي بنيوي مستدام يموله صندوق مكافحة الكوارث الطبيعية بنجاح.")
                } else {
                    alerts.add("💡 اقتراح أكاديمي: ينبغي تفعيل تمويلات FLCN لدعم الجدران الإسفنجية وتوسيع مجرى Oued El Maleh لتنفيذ أولوية سنداي 4.")
                }
            }
            Prefecture.AIN_SEBAA -> {
                if (threat > 0.5f) {
                    alerts.add("🔴 عجز شبكة الصرف الحضري بالحي المحمدي: تجمعات مائية كبرى تهدد المنشآت الحيوية في عين السبع.")
                    alerts.add("⚖️ الاستراتيجية الوطنية لتدبير المخاطر 2020-2030: تفرض تسريع إنجاز قنوات صرف سيول الأمطار وتأهيل البنية التحتية المتهالكة.")
                }
                if (threat > 0.2f && resilience < 0.4f) {
                    alerts.add("⚠️ وثيقة التصميم للمخطط التوجيهي للتهيئة الحضرية (SDAU): الإصرار على ترخيص مناطق لوجستيكية في أحواض غمر مائية تحت 15 متر يخالف قانون التعمير 12.90.")
                }
                if (resilience > 0.5f) {
                    alerts.add("🟢 تقنية Sponge Cities في العمل: دمج الحدائق الحيوية الرطبة والمواقف النفاذة للمياه يقلل حجم الجريان السطحي بـ 45% ويشكل حماية طبيعية.")
                }
            }
            Prefecture.SIDI_BERNOUSSI -> {
                if (threat > 0.6f) {
                    alerts.add("🔴 تآكل وانجراف الشاطئ سيدي البرنوصي: أمواج عاتية تقضم الخط الساحلي وتهدد المنشآت المتاخمة للبحر.")
                    alerts.add("⚖️ المشرّع المغربي وقانون التعمير 12.90: يقتضي فرض حزام سياحي مانع للبناء بطول الساحل (ارتفاق البحر) صيانة لطبيعة المنطقة الديناميكية.")
                }
                if (resilience > 0.5f) {
                    alerts.add("🟢 تدابير تأهيل الساحل الديناميكية: بناء حواجز تصفية الأمواج المائعة والشعاب الاسفنجية يصد الهجوم البحري تماشياً مع أولوية سنداي الرابعة البناء بشكل أفضل.")
                    alerts.add("💸 تدخل تمويلي نوعي: تفعيل تمويل FLCN لتنفيذ مشاريع هجينة لتهيئة الواجهة البحرية ضد زحف المد البحري.")
                } else {
                    alerts.add("💡 توصية لجنة المناقشة: نقص البنيات الصادة للتعرية يحتاج دمج عاجل للمشاريع البيئية ضمن الميزانية الحضرية للبيضاء بتمكين من صندوق FLCN.")
                }
            }
        }
        alerts
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    // --- Set State Actions ---
    fun selectPrefecture(pref: Prefecture) {
        _selectedPrefecture.value = pref
    }

    fun setThreatLevel(level: Float) {
        _threatLevel.value = level
    }

    fun setResilienceLevel(level: Float) {
        _resilienceLevel.value = level
    }

    // --- Chat Operations ---
    fun sendMessage(studentText: String) {
        if (studentText.isBlank()) return

        val newMessage = ChatMessage(sender = Sender.STUDENT, text = studentText)
        _chatHistory.value = _chatHistory.value + newMessage

        _isChatLoading.value = true

        viewModelScope.launch {
            val systemPrompt = """
                أنت خبير قانوني ومستشار أكاديمي متقدم متخصص في «الاستراتيجيات والتشريعات الوطنية والدولية للحد من مخاطر الكوارث الطبيعية».
                مهمتك الأساسية هي الإجابة الحصرية والدقيقة باللغة العربية الإدارية الأكاديمية الرصينة بأسلوب ملائم للمناقشات الجامعية ودعم أطروحة ماستر.
                
                المجال الترابي للأطروحة: عمالة المحمدية، عمالة مقاطعات عين السبع-الحي المحمدي، ومقاطعات سيدي البرنوصي بالمغرب.
                
                الارتكازات المنهجية الصارمة:
                1. إطار سنداي الدولي 2015-2030:
                   - ربط الأولوية الأولى (فهم المخاطر) بالطبوغرافيا وخرائط الارتفاعات ومجاري الفيضان.
                   - ربط الأولوية الرابعة (البناء بشكل أفضل Build Back Better) بالحلول الإيكولوجية وتقنيات المدن الإسفنجية (Sponge Cities) لإبطاء الامتصاص السطحي وحقن الفرشاة المائية.
                2. التشريع والمخططات المغربية:
                   - الاستراتيجية الوطنية لتدبير مخاطر الكوارث الطبيعية 2020-2030 وتطبيقاتها المحلية بجهة الدار البيضاء-سطات.
                   - قانون الماء 36.15: دور وكالة الحوض المائي (ABH-OER) في مجرى واد المالح (المحمدية)، وإقرار الملك العام المائي وسيلانه الطبيعي.
                   - قانون التعمير 12.90: المخطط التوجيهي للتهيئة الحضرية (SDAU)، وضوابط البناء، والحظر التام للبناء في الأراضي الطبوغرافية المنخفضة تحت عتبة 15 متراً للمحافظة على أمن الأرواح.
                   - صندوق مكافحة آثار الكوارث الطبيعية (FLCN): آليات التمويل المشترك للمشروعات الوقائية ومكافحة السيول وحماية خط الساحل سيدي البرنوصي/المحمدية.
                   
                تنبيهات الاستجابة:
                - اقتصر حصراً على الكوارث الطبيعية كالفيضانات والانجراف الساحلي.
                - صغ إجاباتك بتنسيق واضح ذو خطوط عريضة، عناوين فرعية منبثقة، وتوجيهات عملية تناسب العرض أمام لجنة مناقشة أطروحة الماستر.
                - إذا سألك الطالب عن دراسات كمية تفصيلية غير متوفرة في القوانين، قل بمهنية أكاديمية: "البيانات الإحصائية الرقمية الدقيقة الخاصة بهذا الجانب المحدود غير واردة بالوثائق الأساسية الحالية".
            """.trimIndent()

            val response = apiManager.generateResponse(prompt = studentText, systemPrompt = systemPrompt)
            
            _chatHistory.value = _chatHistory.value + ChatMessage(sender = Sender.ADVISOR, text = response)
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                sender = Sender.ADVISOR,
                text = "تمت إعادة تهيئة قنوات الاتصال الأكاديمي. اسألني عن أي نقطة منهجية أو تشريعية تهم أطروحتك المغربية مجدداً."
            )
        )
    }

    // --- Presentation Outline Builder (SQLite Room Operations) ---
    fun saveSlideToThesis(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insert(
                ThesisSlide(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun updateSlide(slide: ThesisSlide) {
        viewModelScope.launch {
            repository.update(slide)
        }
    }

    fun deleteSlide(slideId: Int) {
        viewModelScope.launch {
            repository.deleteById(slideId)
        }
    }

    fun preInstallStandardSlides() {
        viewModelScope.launch {
            repository.clear()
            // Let's add 3 pre-configured academic thesis slides demonstrating the system context for the user to start with.
            repository.insert(
                ThesisSlide(
                    title = "مقدمة الأطروحة: الإسناد القانوني لدرء مخاطر الكوارث",
                    content = """
                        - الإطار الإقليمي: عمالة المحمدية، عين السبع - الحي المحمدي، وسيدي البرنوصي.
                        - المرجعية الدولية: أولوية سنداي الأولى (فهم المخاطر) والرابعة (تعزيز الاستعداد والبناء بشكل أفضل عبر مشاريع Sponge City).
                        - الترسانة التشريعية الوطنية: الاستراتيجية الوطنية لتدبير المخاطر 2020-2030 وقوانين 36.15 و12.90 وتكامل الصندوق الوقائي FLCN.
                    """.trimIndent(),
                    category = "عام"
                )
            )
            repository.insert(
                ThesisSlide(
                    title = "محور واد المالح بموجب قانون الماء 36.15",
                    content = """
                        - الإشكالية: فيضان واد المالح الموسمي يهدد النسيج السكاني عمالة المحمدية في المناطق الهشة.
                        - فاعل السياسة المائية: وكالة الحوض المائي لأم الربيع (ABH-OER) تمتلك سلطة الشرطة المائية ورصد الجريان.
                        - الحل التشريعي المقترح: تفعيل آلية صندوق FLCN لإعادة تصميم ضفاف الوادي وتضمين الماتريكس الإسفنجي لضمان الارتشاح الباطني السليم.
                    """.trimIndent(),
                    category = "قانون الماء"
                )
            )
            repository.insert(
                ThesisSlide(
                    title = "محددات التعمير 12.90 والمناطق تحت ارتفاع 15m",
                    content = """
                        - القاعدة القانونية: يمنع المخطط التوجيهي SDAU وقانون 12.90 الترخيص بالبناء في المناطق ذات الارتفاعات الضحلة والمنخفضة.
                        - رصد الميدان: أحياء عين السبع وسيدي البرنوصي تحتوي على مسطحات مائية لوجستيكية وصناعية معرضة للمد والجزر المرتفع والانجراف الساحلي.
                        - البديل الهندسي: تحويل المناطق غير الصالحة للبناء كمتنزهات إسفنجية خضراء (Green Sponges Buffer) بتمكين ودعم من FLCN.
                    """.trimIndent(),
                    category = "قانون التعمير"
                )
            )
        }
    }
}

class ThesisViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThesisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThesisViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
