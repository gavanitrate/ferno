(ns ferno.txactor.db
  (:require [cljs.pprint :refer [pprint]]
            [datascript.core :as d]
            [ferno.txactor.firebase :as fb]
            [ferno.transit :as transit]
            [ferno.db :as ferno.db]))

(defonce tx-queue-listener (atom nil))

(defonce cnx (d/create-conn))

(defn create-snapshot [tx-data]
  (reduce
    (fn [snapshot-map datom]
      (assoc snapshot-map
        (ferno.db/ea-hash datom)
        datom))
    {} tx-data))

(defn process-datom! [fbdb datom]
  (case (-> datom second .-added)
    true (ferno.db/transact-datom! fb/database datom)
    false (ferno.db/retract-datom! fb/database datom)))

(defn process-tx-data! [ss]
  (let [txs      (->> ss .val transit/decode)
        snapshot (->> txs
                      (d/transact! cnx)
                      :tx-data
                      create-snapshot)]

    ;; log it out
    (println "Processing" (-> ss .-key))
    (pprint snapshot)

    ;; process each datom
    (doseq [datom snapshot]
      (process-datom! fb/database datom))

    ;; remove the data from Firebase
    (-> ss .-ref .remove)))

(defn start-listening []
  (when-not @tx-queue-listener
    (ferno.db/full-sync!
      fb/database cnx
      #(let [txq-ref    (.ref fb/database "/tx-queue")
             listening? (.on txq-ref "child_added" (fn [ss] (process-tx-data! ss)))]
         (println "Started listening to" txq-ref)
         (reset! tx-queue-listener listening?)))))