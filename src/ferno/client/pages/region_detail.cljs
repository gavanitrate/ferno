(ns ferno.client.pages.region-detail
  (:require [reagent.core :as r]
            [posh.reagent :as p]

            [ferno.client.ui.components :as ui]
            [ferno.client.db :refer [cnx] :as db]
            [ferno.client.state :as st]))

(defonce filter-atom (r/atom nil))

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

(defn filterable-activities [activities filter]
  [:div.notification
   [:label.label "Activities"]
   [:div.field.is-grouped.is-multiline
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
         (into [:div.tags]))]])

(defn filterable-attrs [region filter]
  [:div.notification
   [:label.label "Attributes"]
   (let [ftemp  (:region/temperature filter)
         fnoise (:region/noise filter)]
     [:div.field.is-grouped.is-multiline
      [:div.control
       [:div.tags.has-addons
        [:span.tag.is-warning
         [:span.icon [:i.fa.fa-sun-o]]]
        [:span.tag.is-dark
         [ui/yorn (-> region :region/sheltered not)]]]]
      [:div.control
       [:div.tags.has-addons
        [:span.tag.is-danger
         [:span.icon [:i.fa.fa-thermometer-half]]]
        [:a.tag
         {:class    (if ftemp "is-success" "is-dark")
          :on-click #(if ftemp
                       (reset! filter-atom nil)
                       (reset! filter-atom {:region/temperature :temp}))}
         (str (:region/temperature region) "C")]]]
      [:div.control
       [:div.tags.has-addons
        [:span.tag.is-info
         [:span.icon [:i.fa.fa-volume-up]]]
        [:a.tag.is-dark
         {:class    (if fnoise "is-success" "is-dark")
          :on-click #(if fnoise
                       (reset! filter-atom nil)
                       (reset! filter-atom {:region/noise :noise}))}
         (str (:region/noise region) "dB")]]]])])

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
      [:div.columns
       [:div.column
        [:h2 "Region Details"]
        [ui/region-coordinates (:region/coordinates r)]
        [filterable-attrs r filter]
        [filterable-activities (:region/activities r) filter]]
       [:div.column
        [:h2
         (if filter
           (str "People With Current Activity: "
                (-> filter first second
                    name clojure.string/capitalize))
           "People Currently Here")]
        [people-list p]]]]]))

(defn page-component [env]
  (let [{:keys [state]} env]
    [:div.container.page
     [ui/loader (st/region state)
      [region (st/region state)]]]))