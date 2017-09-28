(ns ferno.client.db
  (:require [ferno.transit :as transit]
            [ferno.db :as ferno.db]
            [datascript.core :as d]
            [ferno.client.firebase :as fb]
            [posh.reagent :as p]))

;; create database
;; then connect it with posh

(defonce cnx (d/create-conn))
(p/posh! cnx)

;; state of syncing

(defonce syncing? (atom nil))

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
                      (let [txs (ferno.db/sync-add-tx cnx (-> ss .toJSON js->clj))]
                        (p/transact! cnx txs)
                        (println "Added ::" txs))))))))

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
                      (let [txs (ferno.db/sync-add-tx cnx (-> ss .toJSON js->clj))]
                        (p/transact! cnx txs)
                        (println "Added ::" txs))))))))

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
                      (let [txs (ferno.db/sync-retract-tx cnx (-> ss .toJSON js->clj))]
                        (p/transact! cnx txs)
                        (println "Removed ::" txs))))))))

(defn start []
  (listen-to-adds!)
  (listen-to-changes!)
  (listen-to-retracts!))