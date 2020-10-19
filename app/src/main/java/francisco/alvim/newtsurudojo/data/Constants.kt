package francisco.alvim.newtsurudojo.data

object Constants {
    val DAY_VALUES = arrayListOf<String>().apply { for (i in 1..31) add(i.toString()) }
    val MONTH_VALUES = arrayListOf<String>().apply { for (i in 1..12) add(i.toString()) }
    val YEAR_VALUES = arrayListOf<String>().apply { for (i in FIRST_YEAR..LAST_YEAR) add(i.toString()) }
    val FIRST_YEAR = 2000
    val LAST_YEAR = 2100
    val DAN = "Dan"
    val KYU = "Kyu"
    val LEVEL_TYPES = arrayListOf(KYU, DAN)
}