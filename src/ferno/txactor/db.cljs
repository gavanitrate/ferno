(ns ferno.txactor.db
  (:require [cljs.pprint :refer [pprint]]
            [datascript.core :as d]
            [ferno.txactor.firebase :as fb]
            [ferno.transit :as transit]))

(defonce tx-queue-listener (atom nil))

(defonce cnx (d/create-conn))

;; TODO datom helper

(defn datom->clj [datom]
  (let [e (.-e datom)
        a (.-a datom)
        v (.-v datom)]
    {:e e :a a :v v}))

;; TODO datom helper

(defn ea-hash [datom]
  (let [e (.-e datom)
        a (.-a datom)]
    (hash {:e e :a a})))

(defn transact-datom [datom]
  (let [datom-hash (ea-hash datom)
        encoded    (-> datom datom->clj transit/encode)]
    (-> fb/database
        (.ref (str "/datoms/" datom-hash))
        (.set encoded))))

(defn take-txq [ss]
  (let [txs     (->> ss .val transit/decode)
        tx-data (:tx-data (d/transact! cnx txs))]

    ;; log it out
    (pprint tx-data)

    ;; Process each datom
    (doseq [datom tx-data]
      (transact-datom datom))

    ;; remove the data from Firebase
    (-> ss .-ref .remove)))

(defn start-listening []
  (when-not @tx-queue-listener
    (let [txq-ref    (.ref fb/database "/tx-queue")
          listening? (.on txq-ref "child_added" #(take-txq %))]
      (println "Started listening to" txq-ref)
      (reset! tx-queue-listener listening?))))