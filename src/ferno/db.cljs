(ns ferno.db
  (:require [cljs.pprint :refer [pprint]]
            [ferno.transit :as transit]
            [datascript.core :as d]))

;; datom helpers

(defn eav-hash
  "Create a hash of a datom's entity ID, attribute and value."
  [datom]
  (let [e (.-e datom)
        a (.-a datom)
        v (.-v datom)]
    (hash {:e e :a a :v v})))

(defn datom->clj
  "Transform a Datom object into a Clojure(script) map."
  [datom]
  (let [e (.-e datom)
        a (.-a datom)
        v (.-v datom)]
    {:e e :a a :v v}))

(defn clj->vdatom
  "Transform a datom (in map form) to a vector representation."
  [added? clj-datom]
  (let [e         (:e clj-datom)
        a         (:a clj-datom)
        v         (:v clj-datom)
        operation (case added?
                    true :db/add
                    false :db/retract)]
    [operation e a v]))