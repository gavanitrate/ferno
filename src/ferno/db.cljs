(ns ferno.db
  (:require [cljs.pprint :refer [pprint]]
            [ferno.transit :as transit]
            [datascript.core :as d]))

;; datom helpers

(defn ea-hash [datom]
  (let [e (.-e datom)
        a (.-a datom)]
    (hash {:e e :a a})))

(defn datom->clj [datom]
  (let [e (.-e datom)
        a (.-a datom)
        v (.-v datom)]
    {:e e :a a :v v}))

(defn clj->datom [added? clj-datom]
  (let [e         (:e clj-datom)
        a         (:a clj-datom)
        v         (:v clj-datom)
        operation (case added?
                    true :db/add
                    false :db/retract)]
    [operation e a v]))

;; syncing

(defn sync-add-tx [cnx datom]
  (->> datom
       transit/decode
       (ferno.db/clj->datom true)
       vector))

(defn sync-retract-tx [cnx datom]
  (->> datom
       transit/decode
       (ferno.db/clj->datom false)
       vector))

(defn sync-datoms! [cnx datom-list]
  (let [datoms (->> datom-list vals
                    (map (comp #(clj->datom true %) transit/decode))
                    (into []))]
    (d/transact cnx datoms)
    (println "Synced" (count datoms) "datoms")))

(defn full-sync! [fbdb cnx cb]
  (println "Starting sync")
  (-> fbdb
      (.ref "/datoms")
      (.once "value"
             (fn [ss]
               (sync-datoms! cnx (-> ss .toJSON js->clj))
               (cb)))))

;; transaction

(defn transact-datom! [fb [datom-hash datom]]
  (let [encoded (-> datom datom->clj transit/encode)]
    (-> fb
        (.ref (str "/datoms/" datom-hash))
        (.set encoded))))

(defn retract-datom! [fb [datom-hash datom]]
  (-> fb
      (.ref (str "/datoms/" datom-hash))
      .remove))