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
   [:div.field.is-grouped
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-warning.is-large
       [:span.icon [:i.fa.fa-sun-o]]]
      [:span.tag.is-dark.is-large
       [:span.icon
        (if (-> region :region/sheltered not)
          [:i.fa.fa-check] [:i.fa.fa-times])]]]]
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-danger.is-large
       [:span.icon [:i.fa.fa-thermometer-half]]]
      [:span.tag.is-dark.is-large
       (str (:region/temperature region) "C")]]]
    [:div.control
     [:div.tags.has-addons
      [:span.tag.is-info.is-large
       [:span.icon [:i.fa.fa-volume-up]]]
      [:span.tag.is-dark.is-large
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

(defn grid-tile [region]
  (let []
    ^{:key (:db/id region)}
    [:div.tile.is-child.box
     [:div.content
      [region-header region]
      [region-activities (:region/activities region)]
      [region-attrs region]]]))

(defn grid-row [[lat row]]
  ^{:key lat}
  [:div.tile.is-parent.is-vertical
   (->> row
        (sort-by (fn [r] (-> r :region/coordinates :coordinate/lat)))
        (map grid-tile))])

(defn grid-map [env cnx]
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

    [:div.box [:h1.title "Map"]
     (->> regions
          (group-by (fn [r] (-> r :region/coordinates :coordinate/lng)))
          (map grid-row)
          (into [:div.tile.is-ancestor]))]))

(defn page-component [env]
  (let [{:keys [state]} env
        data @page-state
        cnx  @db/cnx-atom]
    [:div.container.page
     [ui/loader cnx
      [grid-map env cnx]]]))