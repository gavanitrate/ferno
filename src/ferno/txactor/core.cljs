(ns ferno.txactor.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]

            [ferno.txactor.db :as db])
  (:import [goog.date DateTime]))

(nodejs/enable-util-print!)

(defn -main [& args]
      (println "---------------------------------------------")
      (println "TXACTOR")
      (println "STARTUP -" (-> (DateTime.) .toUTCIsoString))
      (println "---------------------------------------------")
      (db/start-listening))

(set! *main-cli-fn* -main)