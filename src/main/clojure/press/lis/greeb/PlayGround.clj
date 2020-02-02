(ns press.lis.greeb.PlayGround
  (:require [clojure.java.io :as io])
  (:require [clojure.reflect :as r])
  (:import (press.lis.greeb SheetsClient)))

(def sheet (-> (SheetsClient/getSheetService)
               (.spreadsheets)
               (.values)
               (.get "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M" "A:Z")
               (.execute)))

; Calendar Sorting
(doseq [x (.getValues sheet)]
  (println (.size x)))

(reverse
  (sort (map #(vector (.get % 4) (.get % 0))
             (filter #(> (.size %) 4) (.getValues sheet)))))


(.get (.getValues sheet) 0)

; https://cursive-ide.com/userguide/paredit.html
; It's better to use shift-shift to navigate across keymap