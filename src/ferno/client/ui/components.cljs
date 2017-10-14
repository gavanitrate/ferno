(ns ferno.client.ui.components
  (:require [reagent.core :as r]))

;; form
;; ---------------------------------------------------------------------------------

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

;; form
;; ---------------------------------------------------------------------------------

(defn inspect [x]
  [:div.inspect-component
   [:pre
    [:code
     (with-out-str (cljs.pprint/pprint x))]]])

(defn loader
  ([loaded? child]
   (loader
     loaded? child
     {:loader-content
      [:div.default-loader-content
       "Loading"]}))

  ([loaded? child {:keys [loader-content]}]
   (if loaded?
     child
     [:div.loader-component loader-content])))

(defn yorn [yes]
  [:span.icon
   (if yes
     [:i.fa.fa-check]
     [:i.fa.fa-times])])


;; region
;; ---------------------------------------------------------------------------------

(defn region-coordinates [coordinates]
  [:div.notification
   [:div.tags
    [:span.tag.is-dark.is-large
     "Lat: " (:coordinate/lat coordinates)]
    [:span.tag.is-dark.is-large
     "Long: " (:coordinate/lng coordinates)]]])

(defn region-attrs [region]
  [:div.notification
   [:label.label "Attributes"]
   [:div.field.is-grouped.is-multiline
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-warning
       [:span.icon [:i.fa.fa-sun-o]]]
      [:span.tag.is-dark
       [yorn (-> region :region/sheltered not)]]]]
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-danger
       [:span.icon [:i.fa.fa-thermometer-half]]]
      [:span.tag.is-dark
       (str (:region/temperature region) "C")]]]
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-info
       [:span.icon [:i.fa.fa-volume-up]]]
      [:span.tag.is-dark
       (str (:region/noise region) "dB")]]]]])

(defn region-activities [activities]
  [:div.notification
   [:label.label "Activities"]
   [:div.field.is-grouped.is-multiline
    (->> activities
         (map
           (fn [a]
             ^{:key (:db/id a)}
             [:span.tag.is-dark.is-medium
              (let [activity (-> a :activity/type)]
                (when activity
                  (name activity)))]))
         (into [:div.tags]))]])