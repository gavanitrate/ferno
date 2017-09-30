(ns ferno.txactor.db
  (:require [cljs.pprint :refer [pprint]]
            [cljs.core.async :as async]
            [datascript.core :as d]
            [ferno.txactor.firebase :as fb]
            [ferno.transit :as transit]
            [ferno.db :as ferno.db])
  (:require-macros
    [ferno.txactor.macros :refer [schema-edn]]))

;; async channel
;; used to pass messages around to several components

(defonce db-chan (async/chan))

;; database connection

(defonce cnx (atom nil))


;; schema

(defn upload-schema!
  "Encode schema and upload to Firebase ref: /schema."
  [schema]
  (-> fb/database
      (.ref "/schema")
      (.set (transit/encode schema))))

(defn process-schema
  "Take :schema-retrieved event from async channel.
  Then extract schema (retrieved from Firebase).
  If schema is nil, upload schema stored on disk.
  Then install the schema that exists.
  Put completion event on async channel."
  []
  (async/take!
    db-chan
    (fn [{:keys [schema]}]
      (let [schema' (or schema (schema-edn))]
        ;; upload schema if it doesn't already exist
        (when-not schema (upload-schema! schema'))
        ;; install schema
        (reset! cnx (d/create-conn schema'))
        (println "Installed schema")
        (println (keys schema'))
        (async/put! db-chan {:type :schema-installed})))))

(defn get-schema
  "Retrieve schema stored in Firebase ref: /schema.
  Decode and put it on async channel."
  []
  (-> fb/database
      (.ref "/schema")
      (.once "value"
             (fn [ss]
               (async/put!
                 db-chan
                 {:type   :schema-retrieved
                  :schema (->> ss .toJSON js->clj transit/decode)})))))

;; initial datoms list

(defn process-datoms-list
  "Take :datoms-retrieved event from async channel.
  Extract datoms (retrieved from Firebase).
  Then install them into db."
  []
  (async/take!
    db-chan
    (fn [{:keys [datoms]}]
      (ferno.db/sync-datoms! @cnx datoms)
      (async/put! db-chan {:type :datoms-installed}))))

(defn get-datoms-list []
  "Take :schema-installed event from async channel.
  Retrieve all datoms store in Firebase ref: /datoms.
  Then put them on the async channel."
  (async/take!
    db-chan
    (fn []
      (-> fb/database
          (.ref "/datoms")
          (.once "value"
                 (fn [ss]
                   (async/put!
                     db-chan
                     {:type   :datoms-retrieved
                      :datoms (-> ss .toJSON js->clj)})))))))

;; listen to tx-queue

(defn flatten-tx-data
  "Return a reduced list of datoms.
  Each datom is hashed according to it's entity ID, attribute and value (eav-hash).
  This hash is used as the key and the datom is stored as the value.
  Following datoms with the same entity ID and attribute will overwrite previous ones.
  The result returned is a map of datoms corresponding to their ea-hashs.
  This map contains only the last transacted datoms;
  essentially representing the latest view of the database."
  [tx-data]
  (reduce
    (fn [snapshot-map datom]
      (assoc snapshot-map
        (ferno.db/eav-hash datom)
        datom))
    {} tx-data))

(defn process-datom!
  "Process an individual datom.
  Add it to the list of datoms in Firebase if it was an additive datom.
  Or remove it if it was a retraction."
  [fbdb datom]
  (case (-> datom second .-added)
    true (ferno.db/transact-datom! fb/database datom)
    false (ferno.db/retract-datom! fb/database datom)))

(defn process-tx-data!
  "Decode Firebase event and extract raw transaction data.
  Transact raw data, extract returned datoms and flatten them.
  Process each datom individually, then remove the Firebase reference to the data."
  [ss]
  (let [txs       (->> ss .val transit/decode)
        flattened (->> txs
                       (d/transact! @cnx)
                       :tx-data
                       flatten-tx-data)]

    (println "Processing" (-> ss .-key))
    ;; process each datom
    (doseq [datom flattened]
      (println datom)
      (process-datom! fb/database datom))

    ;; remove the data from Firebase
    (-> ss .-ref .remove)))

(defn listen-tx-queue
  "Take :datoms-installed event from async channel.
  Begin listening to Firebase ref: /tx-queue.
  Attach a callback to the child_added event.
  Decode all incoming data and pass it to callback."
  []
  (async/take!
    db-chan
    (fn []
      (let [txq-ref (.ref fb/database "/tx-queue")]
        (.on txq-ref "child_added"
             (fn [ss] (process-tx-data! ss)))
        (println "Started listening to" txq-ref)))))


(defn start []
  (get-schema)
  (process-schema)
  (get-datoms-list)
  (process-datoms-list)
  (listen-tx-queue)
  )