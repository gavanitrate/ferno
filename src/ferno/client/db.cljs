(ns ferno.client.db
  (:require [ferno.transit :as transit]
            [ferno.db :as ferno.db]
            [datascript.core :as d]
            [ferno.client.firebase :as fb]
            [posh.reagent :as p]
            [reagent.core :as r]))


(defonce cnx-atom (r/atom nil))

(defn cnx []
  @cnx-atom)

;; state of syncing

(defonce syncing? (atom nil))

(defn get-schema
  "Retrieve schema stored in Firebase ref: /schema.
  Decode and initialise database with it."
  []
  (-> fb/database
      (.ref "/schema")
      (.once "value"
             (fn [ss]
               (let [schema (->> ss .toJSON js->clj transit/decode)]
                 (reset! cnx-atom (d/create-conn (or schema {})))
                 (p/posh! @cnx-atom))))))

(defn transact
  "Push transaction data to Firebase ref: /tx-queue."
  [tx-data]
  (let [encoded (transit/encode tx-data)]
    (-> fb/database
        (.ref "/tx-queue")
        (.push encoded))))

(defn listen-to-adds!
  "Start listening to child_added events on Firebase ref: /datoms.
  Add the retrieved data into the database."
  []
  (when-not (:add @syncing?)
    (println "Started listening for adds")
    (swap! syncing? assoc :add
           (-> fb/database
               (.ref "/datoms")
               (.on "child_added"
                    (fn [ss]
                      (let [txs (ferno.db/sync-add-tx @cnx-atom (-> ss .toJSON js->clj))]
                        (p/transact! @cnx-atom txs)
                        (println "Addition ::" txs))))))))

(defn listen-to-changes!
  "Start listening to child_changed events on Firebase ref: /datoms
  Add the retrieved data into the database."
  []
  (when-not (:change @syncing?)
    (println "Started listening for changes")
    (swap! syncing? assoc :change
           (-> fb/database
               (.ref "/datoms")
               (.on "child_changed"
                    (fn [ss]
                      (let [txs (ferno.db/sync-add-tx @cnx-atom (-> ss .toJSON js->clj))]
                        (p/transact! @cnx-atom txs)
                        (println "Change ::" txs))))))))

(defn listen-to-retracts!
  "Start listening to child_removed events on Firebase ref: /datoms
  Retract the retrieved data from the database."
  []
  (when-not (:retract @syncing?)
    (println "Started listening for retractions")
    (swap! syncing? assoc :retract
           (-> fb/database
               (.ref "/datoms")
               (.on "child_removed"
                    (fn [ss]
                      (let [txs (ferno.db/sync-retract-tx @cnx-atom (-> ss .toJSON js->clj))]
                        (p/transact! @cnx-atom txs)
                        (println "Retraction ::" txs))))))))

(defn start []
  (get-schema)
  (listen-to-adds!)
  (listen-to-changes!)
  (listen-to-retracts!))