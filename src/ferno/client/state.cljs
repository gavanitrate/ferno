(ns ferno.client.state
  (:require [reagent.core :as r]))

(defonce state-atom (r/atom {:page :dashboard}))

(defn page
  ([] (page @state-atom))
  ([state] (-> state :page)))

(defn change-page [page]
  (swap! state-atom assoc :page page))

(defn region
  ([] (region @state-atom))
  ([state] (-> state :region)))

(defn move-to-region [region]
  (change-page :region)
  (swap! state-atom assoc :region region))

