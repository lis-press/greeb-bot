(ns press.lis.greeb.PlayGround
  (:require [clojure.java.io :as io])
  (:import (press.lis.greeb SheetsClient)))

(-> (SheetsClient/getSheetService)
    (.spreadsheets)
    (.values)
    (.get "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M" "A:Z")
    (.execute))