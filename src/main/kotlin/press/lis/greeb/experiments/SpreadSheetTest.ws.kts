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

val subscribed = values.subList(1, values.size - 1).filter {
    it.size > 2 && it[2] != null && it[2] != ""
}

subscribed

println(subscribed)

subscribed.map { it.size }