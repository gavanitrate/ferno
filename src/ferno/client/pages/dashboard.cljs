(ns ferno.client.pages.dashboard
  (:require [reagent.core :as r]
            [posh.reagent :as p]
            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]
            [datascript.core :as d]))

(defonce page-state (r/atom nil))

(defn region-attrs [region]
  [:div.field.is-grouped.is-multiline
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
     [:span.tag.is-dark.is-large (str (:region/temperature region) "C")]]]
   [:div.control
    [:div.tags.has-addons
     [:span.tag.is-info.is-large
      [:span.icon [:i.fa.fa-volume-up]]]
     [:span.tag.is-dark.is-large (str (:region/noise region) "dB")]]]])

(defn region-activities [activities]
  [:div.field.is-grouped.is-multiline
   (->> activities
        (map
          (fn [a]
            ^{:key (:db/id a)}
            [:span.tag.is-medium (-> a :activity/type name str)]))
        (into [:div.tags]))])

(defn grid-tile [region]
  (let []
    ^{:key (:db/id region)}
    [:div.box
     [:div.content
      [:h2 (:region/name region)]
      [:span
       [region-attrs region]
       [region-activities (:region/activities region)]]]]))

(defn grid-map [env cnx]
  (let [region-ids
        @(p/q '[:find [?e ...]
                :where
                [?e :region/name]]
              cnx)
        regions
        (->> region-ids
             (map (fn [r] @(p/pull cnx '[*] r)))
             doall)]
    [:div (map grid-tile regions)]))

(defn page-component [env]
  (let [{:keys [state]} env
        data @page-state
        cnx  @db/cnx-atom]
    [:div.container.page
     [ui/loader cnx
      [:div.box
       [:h1.title "Dashboard"]
       [grid-map env cnx]]
      ]]))