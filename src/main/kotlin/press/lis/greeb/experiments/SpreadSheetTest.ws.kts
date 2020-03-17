package press.lis.greeb.experiments

import press.lis.greeb.SheetsClient

// You can try to run this in REPL mode to experiment with spreadsheets
print("Test")

val x = SheetsClient.getSheetService()

val s_id = "1q3pPL_PMhnXbaAOsQ4EU96hAn7ZJuZQJguEEyvONipY"
val response = x.spreadsheets().values()
        .get(s_id,
                "A:AM")
        .execute()

//x.spreadsheets().values().update(
//        s_id,
//        "AJ9",
//        ValueRange().setValues(listOf(listOf(true)))
//).execute()

val values = response.getValues()

values[0]

fun columnNumberToA1(columnNumber: Int): String {
    var columnNumberInternal: Int = columnNumber
    val sb = StringBuilder()
    val numberOfLetters = 26

    while (true) {
        sb.append('A' + columnNumberInternal % numberOfLetters)

        if (columnNumberInternal < numberOfLetters) {
            break
        }

        columnNumberInternal /= numberOfLetters
        columnNumberInternal--
    }

    return sb.reverse().toString()
}

columnNumberToA1(1)
columnNumberToA1(28)
columnNumberToA1(26)
columnNumberToA1(27)
columnNumberToA1(270)
columnNumberToA1(676)
columnNumberToA1(702)
columnNumberToA1(702)
columnNumberToA1(701)
columnNumberToA1(25)
columnNumberToA1(701)
