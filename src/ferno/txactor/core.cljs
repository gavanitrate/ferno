(ns ferno.txactor.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]

            [datascript.core :as ds]
            [ferno.transit :as transit]
            [ferno.txactor.firebase]
            ))

(nodejs/enable-util-print!)

(defonce cnx (ds/create-conn))

(defn -main [& args]
  (pprint "test"))

(set! *main-cli-fn* -main)


































;admin.initializeApp({
;                     credential: admin.credential.cert(serviceAccount),
;                                 databaseURL: "https://<DATABASE_NAME>.firebaseio.com"
;                     });


;(ns ferno.txactor.core
;  (:require [cljs.pprint :refer [pprint]]
;            [datascript.core :as ds]
;
;            [ferno.firebase :as fb]
;            [ferno.transit :as transit]))
;
;
;
;(defonce tx-queue-listener (atom nil))
;
;(defn datom->clj [datom]
;  (let [e (.-e datom)
;        a (.-a datom)
;        v (.-v datom)]
;    {:e e :a a :v v}))
;
;(defn ea-hash [datom]
;  (let [e (.-e datom)
;        a (.-a datom)]
;    (hash {:e e :a a})))
;
;(defn transact-datom [datom]
;  (let [datom-hash (ea-hash datom)
;        encoded    (-> datom datom->clj transit/encode)]
;    (-> fb/database
;        (.ref (str "/datoms/" datom-hash))
;        (.set encoded))))
;
;(defn start-listening []
;  (when-not @tx-queue-listener
;    (reset! tx-queue-listener
;            (-> fb/database
;                (.ref "/tx-queue")
;                (.on "child_added"
;                     (fn [ss]
;                       (let [received (->> ss .val transit/decode)
;                             tx-data  (:tx-data (ds/transact! cnx received))]
;                         (doseq [datom tx-data]
;                           (transact-datom datom)))
;                       (-> ss .-ref .remove)))))))
;
;(start-listening)