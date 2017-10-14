(ns ferno.client.pages.region-detail
  (:require [reagent.core :as r]
            [posh.reagent :as p]

            [ferno.client.ui.components :as ui]
            [ferno.client.db :refer [cnx] :as db]
            [ferno.client.state :as st]))

(defn person [[person-id]]
  (let [{:keys [person/name person/activity] :as pd}
        @(p/pull (cnx) '[*] person-id)]
    [:a.box
     [:div.content
      [:h3 name]
      [:span "Current Activity: " (:activity/type activity)]]]))

(defn people-list [people]
  (->> people
       (sort-by first)
       (map person)
       (into [:div.people-list])))

(defn region [region-id]
  (let [q '[:find ?p
            :in $ ?rid
            :where
            [?p :person/location ?rid]]
        p @(p/q q (cnx) region-id)
        r @(p/pull (cnx) '[*] region-id)]
    [:div.box
     [:div.content
      [:h1 (:region/name r)]
      [:div.columns
       [:div.column
        [:h2 "Region Details"]
        [ui/region-coordinates (:region/coordinates r)]
        [ui/region-attrs r]
        [ui/region-activities (:region/activities r)]]
       [:div.column
        [:h2 "People Currently Here"]
        [people-list p]]]]]))

(defn page-component [env]
  (let [{:keys [state]} env]
    [:div.container.page
     [ui/loader (st/region state)
      [region (st/region state)]]
     ]))