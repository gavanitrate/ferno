(ns ferno.client.db
  (:require [ferno.transit :as transit]
            [ferno.client.firebase :as fb]))

(defn ferno-transact [tx-data]
  (let [encoded (transit/encode tx-data)]
    (-> fb/database
        (.ref "/tx-queue")
        (.push encoded))))