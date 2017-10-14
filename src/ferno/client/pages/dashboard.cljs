(ns ferno.client.pages.dashboard
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [posh.reagent :as p]
            [datascript.core :as d]

            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]
            [ferno.client.state :as state]))

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
    [:a.box.region
     {:key      (:db/id region-data)
      :on-click (fn [e]
                  (.preventDefault e)
                  (state/move-to-region (:db/id region-data)))}
     [:div.content
      [region-header region-data]
      [ui/region-activities (:region/activities region-data)]
      [ui/region-attrs region-data]]]))

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
        cnx @db/cnx-atom]
    [:div.container.page
     [ui/loader cnx
      [region-map env cnx]]]))