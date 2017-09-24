(ns ferno.client.db
  (:require [ferno.transit :as transit]
            [ferno.db :as ferno.db]
            [datascript.core :as d]
            [ferno.client.firebase :as fb]
            [posh.reagent :as p]))

(defonce cnx (d/create-conn))
(p/posh! cnx)

(defonce synced? (atom nil))

(defn transact [tx-data]
  (let [encoded (transit/encode tx-data)]
    (-> fb/database
        (.ref "/tx-queue")
        (.push encoded))))

(defn listen-to-adds! []
  (when-not (:add @synced?)
    (swap! synced? assoc :add
           (-> fb/database
               (.ref "/datoms")
               (.on "child_added"
                    (fn [ss]
                      (p/transact! cnx (ferno.db/sync-add-tx cnx (-> ss .toJSON js->clj)))))))))

(defn listen-to-changes! []
  (when-not (:change @synced?)
    (swap! synced? assoc :change
           (-> fb/database
               (.ref "/datoms")
               (.on "child_changed"
                    (fn [ss]
                      (p/transact! cnx (ferno.db/sync-add-tx cnx (-> ss .toJSON js->clj)))))))))

(defn listen-to-retracts! []
  (when-not (:retract @synced?)
    (swap! synced? assoc :retract
           (-> fb/database
               (.ref "/datoms")
               (.on "child_removed"
                    (fn [ss]
                      (p/transact! cnx (ferno.db/sync-retract-tx cnx (-> ss .toJSON js->clj)))))))))

(listen-to-adds!)
(listen-to-changes!)
(listen-to-retracts!)