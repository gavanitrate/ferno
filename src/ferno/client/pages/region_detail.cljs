(ns ferno.client.pages.region-detail
  (:require [reagent.core :as r]
            [posh.reagent :as p]

            [ferno.client.ui.components :as ui]
            [ferno.client.db :refer [cnx] :as db]
            [ferno.client.state :as st]))

(defonce activity-filter (r/atom nil))

(defn person [filter [person-id]]
  (let [{:keys [person/name person/activity] :as pd}
        @(p/pull (cnx) '[*] person-id)]
    [:a.box
     [:div.content
      [:h3 name]
      [:span "Current Activity: " (:activity/type activity)]]]))

(defn people-list [people]
  (->> people
       (sort-by first)
       (map #(person filter %))
       (into [:div.people-list])))

(defn filterable-activities [activities]
  (let [filter @activity-filter]
    [:div.notification
     [:label.label "Activities"]
     [:div.field.is-grouped.is-multiline
      (->> activities
           (map
             (fn [a]
               ^{:key (:db/id a)}
               (let [activity (-> a :activity/type)
                     factive  (= activity filter)]
                 [:a.tag.is-dark.is-medium
                  {:class    (when factive "is-primary")
                   :on-click #(if factive
                                (reset! activity-filter nil)
                                (reset! activity-filter activity))}
                  (when activity (name activity))])))
           (into [:div.tags]))]]))


(defn region [region-id]
  (let [q      '[:find ?p
                 :in $ ?rid ?af
                 :where
                 [?p :person/location ?rid]
                 [?p :person/activity ?act]]
        filter @activity-filter
        query  (if filter (conj q '[?act :activity/type ?af]) q)
        p      @(p/q query (cnx) region-id filter)
        r      @(p/pull (cnx) '[*] region-id)]
    [:div.box
     [:div.content
      [:h1 (:region/name r)]
      [:div.columns
       [:div.column
        [:h2 "Region Details"]
        [ui/region-coordinates (:region/coordinates r)]
        [ui/region-attrs r]
        [filterable-activities (:region/activities r)]]
       [:div.column
        [:h2
         (if filter
           (str "People With Current Activity: " (-> filter name clojure.string/capitalize))
           "People Currently Here")]
        [people-list p]]]]]))

(defn page-component [env]
  (let [{:keys [state]} env]
    [:div.container.page
     [ui/loader (st/region state)
      [region (st/region state)]]]))