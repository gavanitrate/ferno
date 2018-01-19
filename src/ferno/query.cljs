(ns ferno.query)

(defn to-q [q-map]
  (into []
        (apply concat
               [(into [:find] (:find q-map))
                (into [:in] (:in q-map))
                (into [:where] (:where q-map))])))

(defn q-add [o a]
  (->> o
       (map (fn [[k v]] {k (concat v (get a k))}))
       (into {})))