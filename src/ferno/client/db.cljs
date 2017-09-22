(ns ferno.client.db
  (:require [ferno.transit :as transit]
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

;; TODO datom helper
(defn clj->datom [clj-datom]
  (let [e (:e clj-datom)
        a (:a clj-datom)
        v (:v clj-datom)]
    ;(d/datom e a v)
    [:db/add e a v]
    ))

(defn sync-datoms [datom-list]
  (->> datom-list vals
       (map (comp clj->datom transit/decode))
       (into [])
       (d/transact cnx)))

(defn sync-datom [datom]
  (->> datom
       transit/decode
       clj->datom
       vector
       (d/transact cnx)))

(defn initial-sync []
  (when-not @synced?
    (reset! synced?
            (-> fb/database
                (.ref "/datoms")
                (.on "child_added" #(sync-datom (-> % .toJSON js->clj)))))))

(initial-sync)