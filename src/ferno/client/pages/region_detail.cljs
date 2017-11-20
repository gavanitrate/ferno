(ns ferno.client.pages.region-detail
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [posh.reagent :as p]

            [ferno.client.ui.components :as ui]
            [ferno.client.db :refer [cnx] :as db]
            [ferno.client.state :as st]))

(defonce filter-atom (r/atom nil))

(defn person [filter [person-id]]
  (let [{:keys [person/name
                person/age person/gender
                person/preferences person/activity] :as pd}
        @(p/pull (cnx) '[*] person-id)]
    [:a.box
     [:div.content
      [:h5 name]
      [:div.columns
       [:div.column "Current Activity"]
       [:div.column (:activity/type activity)]]
      [:div.columns
       [:div.column
        [:div.columns
         [:div.column "Age"]
         [:div.column age]]]
       [:div.column
        [:div.columns
         [:div.column "Gender"]
         [:div.column gender]]]]
      (->> preferences
           (map
             (fn [{:keys [db/id preference/max preference/type]}]
               ^{:key id}
               [:span
                [:span (-> type cljs.core/name str/capitalize)]
                [:span "Max: " max]]))
           (into [:div.user-preferences]))]]))

(defn people-list [people]
  (->> people
       (sort-by first)
       (map #(person filter %))
       (into [:div.people-list])))

(defn region-coordinates [coordinates]
  [:div
   [:h5 "Coordinates"]
   [:div.inline-detail
    [:div.columns
     [:div.column [:label "Latitude"]]
     [:div.column [:span (:coordinate/lat coordinates)]]]]

   [:div.inline-detail
    [:div.columns
     [:div.column [:label "Longitude"]]
     [:div.column [:span (:coordinate/lng coordinates)]]]]])

(defn region-attrs [region filter]
  (let [ftemp  (:region/temperature filter)
        fnoise (:region/noise filter)]
    [:div
     [:h5 "Attributes"]

     [:div.inline-detail
      [:div.columns
       [:div.column [:label "Sheltered"]]
       [:div.column [:span [ui/yorn (-> region :region/sheltered not)]]]]]

     [:div.selectable.inline-detail
      {:class (when ftemp "selected")}
      [:a
       {:on-click #(if ftemp
                     (reset! filter-atom nil)
                     (reset! filter-atom {:region/temperature :temp}))}
       [:div.columns
        [:div.column [:label "Temperature"]]
        [:div.column [:span (str (:region/temperature region) "C")]]]]]


     [:div.selectable.inline-detail
      {:class (when fnoise "selected")}
      [:a
       {:on-click #(if fnoise
                     (reset! filter-atom nil)
                     (reset! filter-atom {:region/noise :noise}))}
       [:div.columns
        [:div.column [:label "Noise"]]
        [:div.column [:span (str (:region/noise region) "dB")]]]]]]))

(defn region-activities [activities filter]
  [:div
   [:h5 "Activities"]
   (->> activities
        (map
          (fn [a]
            ^{:key (:db/id a)}
            (let [activity (-> a :activity/type)
                  factive  (= activity (:activity/type filter))]
              [:a.tag.is-dark.is-medium
               {:class    (when factive "is-primary")
                :on-click #(if factive
                             (reset! filter-atom nil)
                             (reset! filter-atom {:activity/type activity}))}
               (when activity (name activity))])))
        (into [:div.tags]))])

(defn to-q [q-map]
  (into []
        (apply concat
               [(into [:find] (:find q-map))
                (into [:in] (:in q-map))
                (into [:where] (:where q-map))])))

(defn q-add [o a]
  (->> o
       (map (fn [[k v]] {k (concat v (get a k))}))
       (into {})))

(defn people-data [region-id filter]
  (let [people
          {:find  '[?p]
           :in    '[$ ?rid]
           :where '[[?p :person/location ?rid]]}

        actf-q
          {:in    '[?af]
           :where '[[?p :person/location ?rid]
                    [?p :person/activity ?act]
                    [?act :activity/type ?af]]}

        tempf-q
          {:where
           '[[?p :person/location ?rid]
             [?p :person/preferences ?prefs]
             [?prefs :preference/type :region/temperature]
             [?prefs :preference/max ?pmax]
             [?rid :region/temperature ?rt]
             [(> ?rt ?pmax)]]}

        noisef-q
          {:where
           '[[?p :person/location ?rid]
             [?p :person/preferences ?prefs]
             [?prefs :preference/type :region/noise]
             [?prefs :preference/max ?pmax]
             [?rid :region/noise ?rn]
             [(> ?rn ?pmax)]]}

        add-q
          (cond
            (:activity/type filter) actf-q
            (:region/temperature filter) tempf-q
            (:region/noise filter) noisef-q
            :else nil)

        q (to-q (q-add people add-q))]
    @(p/q q (cnx) region-id (-> filter first second))))

(defn region [region-id]
  (let [filter @filter-atom
        p      (people-data region-id filter)
        r      @(p/pull (cnx) '[*] region-id)]
    [:div.box
     [:div.content
      [:h1 (:region/name r)]

      [:h2 "Region Details"]

      [:div.columns
       [:div.column
        [region-coordinates (:region/coordinates r)]]
       [:div.column
        [region-attrs r filter]]
       [:div.column
        [region-activities (:region/activities r) filter]]]

      [:div
       [:h2
        (let [filter-type (-> filter ffirst)
              filter-val  (-> filter first second)]
          (case filter-type
            :region/temperature "People who's temperature preference is being violated"
            :region/noise "People who's noise preference is being violated"
            :activity/type (str "People currently performing: " (name filter-val))
            "People Currently Here"))]
       [people-list p]]]]))

(defn page-component [env]
  (let [{:keys [state]} env]
    [:div.container.page
     [ui/loader (st/region state)
      [region (st/region state)]]]))