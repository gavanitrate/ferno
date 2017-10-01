(ns ferno.client.state
  (:require [reagent.core :as r]))

(defonce state-atom (r/atom {:page :dashboard}))

(defn page
  ([] (-> @state-atom :page))
  ([state] (-> state :page)))

(defn change-page [page]
  (swap! state-atom assoc :page page))

