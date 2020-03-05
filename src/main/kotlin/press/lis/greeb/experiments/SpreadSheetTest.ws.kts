package press.lis.greeb.experiments

import press.lis.greeb.SheetsClient

// You can try to run this in REPL mode to experiment with spreadsheets
print("Testss")

val x = SheetsClient.getSheetService()

val response = x.spreadsheets().values()
        .get("1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M",
                "A:Z")
        .execute()

val values = response.getValues()

values[0]
values[1]
