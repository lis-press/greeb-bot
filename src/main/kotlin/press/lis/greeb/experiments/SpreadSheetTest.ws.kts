package press.lis.greeb.experiments

import com.google.api.services.sheets.v4.model.ValueRange
import press.lis.greeb.SheetsClient

// You can try to run this in REPL mode to experiment with spreadsheets
print("Testss")

val x = SheetsClient.getSheetService()

val s_id = "1q3pPL_PMhnXbaAOsQ4EU96hAn7ZJuZQJguEEyvONipY"
val response = x.spreadsheets().values()
        .get(s_id,
                "A:Z")
        .execute()

val values = response.getValues()

values[0]
values[1]

x.spreadsheets().values().update(
        s_id,
        "AJ9",
        ValueRange().setValues(listOf(listOf(true)))
).execute()

x.spreadsheets().values().update(
        s_id,
        "AJ9",
        ValueRange().setValues(listOf(listOf(true)))
).execute()
