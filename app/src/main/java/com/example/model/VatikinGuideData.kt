package com.example.model

data class VatikinStep(
    val id: String,
    val title: String,
    val minutesBeforeNetz: Int, // e.g. 35 for 35 min before netz
    val description: String,
    val halachicNote: String,
    val isCrucialMoment: Boolean = false
)

object VatikinGuideData {
    val STEPS = listOf(
        VatikinStep(
            id = "alot",
            title = "עלות השחר (השכמה וברכות השחר)",
            minutesBeforeNetz = 72,
            description = "זמן התחלת היום לפי ההלכה. נטילת ידיים, ברכות השחר, ברכות התורה וקרבנות.",
            halachicNote = "מעלות השחר אפשר בדיעבד לקרוא ק״ש, אך לכתחילה לוותיקין ממתינים עד סמוך לנץ."
        ),
        VatikinStep(
            id = "misheyakir",
            title = "משיכיר (הנחת טלית ותפילין)",
            minutesBeforeNetz = 52,
            description = "בדיקת הציצית, עטיפת טלית גדול והנחת תפילין בברכה.",
            halachicNote = "שיעור משיכיר הוא שיראה את חברו ברחוק ד' אמות ויכירנו (שו״ע או״ח סימן י״ח ות״ל)."
        ),
        VatikinStep(
            id = "pesukei",
            title = "תחילת תפילה (הודו וברוך שאמר)",
            minutesBeforeNetz = 32,
            description = "תחילת פסוקי דזמרה בקצב מתון ובכוונה.",
            halachicNote = "במנייני ותיקין מתחילים ברוך שאמר בין 30 ל-35 דקות לפני הנץ (תלוי בעדות ובנוסח)."
        ),
        VatikinStep(
            id = "yishtabach",
            title = "ישתבח וברכו",
            minutesBeforeNetz = 18,
            description = "סיום פסוקי דזמרה, אמירת ישתבח, חצי קדיש וברכו.",
            halachicNote = "בברכו מתחברים לקדושת ברכות קריאת שמע."
        ),
        VatikinStep(
            id = "shema_blessings",
            title = "ברכות קריאת שמע (יוצר אור)",
            minutesBeforeNetz = 14,
            description = "יוצר אור, קדושת היוצר (קדוש קדוש קדוש), ואהבת עולם.",
            halachicNote = "זמן התעוררות האור בעולם לקראת יציאת החמה."
        ),
        VatikinStep(
            id = "kriyat_shema",
            title = "קריאת שמע שעל גבי הנץ",
            minutesBeforeNetz = 5,
            description = "קבלת עול מלכות שמים (שמע ישראל, ברוך שם, פרשיות והציצית).",
            halachicNote = "שנייה לפני הנץ מסיימים את פרשת הציצית כדי לסיים גאל ישראל עם זריחת השמש."
        ),
        VatikinStep(
            id = "gaal_yisrael",
            title = "גאל ישראל וסמיכת גאולה לתפילה",
            minutesBeforeNetz = 1,
            description = "סיום ברכת 'גאל ישראל' בדיוק בשניות שקודם עמידה.",
            halachicNote = "״כדי שיסמוך גאולה לתפילה ונמצא מתפלל ביום״ (ברכות ט:). אין לענות אמן אחר גאל ישראל של עצמו."
        ),
        VatikinStep(
            id = "netz_amida",
            title = "נץ החמה - תחילת תפילת עמידה (שמונה עשרה)!",
            minutesBeforeNetz = 0,
            description = "צעידת שלוש פסיעות קדימה והתחלת ״ה׳ שפתי תפתח״ בדיוק עם זריחת השמש.",
            halachicNote = "זוהי מעלת ותיקין העליונה שעליה נאמר: ״ייראוך עם שמש ולפני ירח דור דורים״ (תהילים ע״ב).",
            isCrucialMoment = true
        )
    )

    val HALACHIC_SOURCES = """
        • תלמוד בבלי, מסכת ברכות ט:
        ״תניא: ותיקין היו גומרין אותה עם הנץ החמה, כדי שיסמוך גאולה לתפילה ונמצא מתפלל ביום. אמר רבי זירא: מאי קראה? 'ייראוך עם שמש ולפני ירח דור דורים'.״

        • שולחן ערוך, אורח חיים, סימן נ״ח סעיף א׳:
        ״מצוה מן המובחר לקרותה כוותיקין, שהיו מכוונים לקרותה מעט קודם הנץ החמה, כדי שיסיים קריאת שמע וברכותיה עם הנץ החמה ויסמוך התפלה מיד בהנץ החמה.״

        • מהו הנץ הנראה לעומת הנץ המישורי?
        נץ מישורי הוא רגע הזריחה באופק גיאומטרי ישר (בגובה פני הים).
        נץ נראה הוא רגע ראיית גוף השמש בפועל על פי האופק הטופוגרפי המקומי (הרים, גבעות, עמקים).
        בירושלים ובמקומות רבים נהוג לפי לוח 'ארץ ישראל' של הגרי״מ טוקצ'ינסקי ולוחות הוותיקין המכוונים לנץ הנראה.
    """.trimIndent()
}
