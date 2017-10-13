(ns ferno.client.pages.dashboard
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [posh.reagent :as p]
            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]
            [datascript.core :as d]))

(defonce page-state (r/atom nil))

(defn region-attrs [region]
  [:div.notification
   [:label.label "Attributes"]
   [:div.field.is-grouped.is-multiline
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-warning
       [:span.icon [:i.fa.fa-sun-o]]]
      [:span.tag.is-dark
       [:span.icon
        (if (-> region :region/sheltered not)
          [:i.fa.fa-check] [:i.fa.fa-times])]]]]
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

(defn region-header [region]
  [:div.level
   [:div.level-left
    [:div.level-item [:p.subtitle (:region/name region)]]]
   [:div.level-right
    [:div.level-item
     [:div.tags.has-addons
      [:span.tag.is-large [:span.icon [:i.fa.fa-user]]]
      [:span.tag.is-dark.is-large (or (:region/people region) 0)]]]]])

(defn region [region-data]
  (let []
    ^{:key (:db/id region-data)}
    [:div.box.region
     [:div.content
      [region-header region-data]
      [region-activities (:region/activities region-data)]
      [region-attrs region-data]]]))

(defn region-map [env cnx]
  (let [region-ids
        @(p/q '[:find [?e ...]
                :where
                [?e :region/name]]
              cnx)
        regions
        (->> region-ids
             (map (fn [r] @(p/pull cnx '[*] r)))
             (map (fn [r]
                    (assoc r :region/people @(p/q '[:find (count ?e) .
                                                    :in $ ?rid
                                                    :where
                                                    [?e :person/location ?rid]]
                                                  cnx (:db/id r)))))
             doall)]
    [:div.region-map
     [:h1.title "Regions"]
     (->> regions
          (map region)
          (into [:div.regions]))]))

(defn page-component [env]
  (let [{:keys [state]} env
        data @page-state
        cnx  @db/cnx-atom]
    [:div.container.page
     [ui/loader cnx
      [region-map env cnx]]]))