(ns ferno.client.ui.components
  (:require [reagent.core :as r]))

(defn e->val [e]
  (.preventDefault e)
  (-> e .-target .-value))

(defn input [value {:keys [on-change]}]
  [:input.input
   {:value     value
    :on-change (fn [e] (when on-change (on-change (e->val e))))}])

(defn textarea [value {:keys [on-change rows cols]}]
  [:textarea.textarea
   {:value     value
    :on-change (fn [e] (when on-change (on-change (e->val e))))
    :cols      (when cols cols)
    :rows      (when rows rows)}])

(defn inspect [x]
  [:div.inspect-block
   [:pre
    [:code (with-out-str (cljs.pprint/pprint x))]]])