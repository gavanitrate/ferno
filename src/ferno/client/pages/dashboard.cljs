(ns ferno.client.pages.dashboard
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [posh.reagent :as p]
            [datascript.core :as d]

            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]
            [ferno.client.ui.charts :refer [updating-timeline]]
            [ferno.client.state :as state]))

(defn region-header [region-name region-people]
  [:div.level
   [:div.level-left
    [:div.level-item [:p.subtitle region-name]]]
   [:div.level-right
    [:div.level-item
     [:div.tags.has-addons
      [:span.tag.is-large [:span.icon [:i.fa.fa-user]]]
      [:span.tag.is-dark.is-large (or region-people 0)]]]]])

(defn region [cnx region-id]
  (let [region-pull
        @(p/pull cnx '[*] region-id)

        region-people
        @(p/q '[:find (count ?e) .
                :in $ ?rid
                :where
                [?rid :region/coordinates ?c]
                [?e :person/location ?c]]
              cnx region-id)]
    [:div.column
     [:a.box
      {:key      region-id
       :on-click (fn [e]
                   (.preventDefault e)
                   (state/move-to-region region-id))}
      [:div.content
       [region-header
        (:region/name region-pull)
        region-people]]]]))

(defn region-map [env cnx]
  (let [regions
        @(p/q '[:find [?e ...]
                :where
                [?e :region/name]]
              cnx)]
    [:div.region-map
     [:h1.title "Regions"]
     (->> regions
          (map (fn [r] [region cnx r]))
          (into [:div.columns]))]))

(defn temp-graph [cnx]
  [:div
   [updating-timeline "dashboard-temp-chart"
    {:data         {:datasets [{:fill            true
                                :pointRadius     0
                                :borderColor     "#FF3860"
                                :backgroundColor "rgba(255, 56, 96, 0.1)"}]}
     :options      {:title      {:display true
                                 :text    "Average Temperature Graph"}
                    :scales     {:yAxes [{:scaleLabel {:display     true
                                                       :labelString "Temperature (C)"}
                                          :ticks      {:suggestedMin 17
                                                       :suggestedMax 38}}]
                                 :xAxes [{:scaleLabel {:display     true
                                                       :labelString "Time"}}]}
                    :tooltips   {:enabled false}
                    :legend     {:display false}
                    :responsive true}
     :interval     500
     :max-interval 41
     :update-fn    (fn [chart]
                     (let [chart-instance (:chart-instance chart)]
                       (-> (aget chart-instance "data" "datasets" 0 "data")
                           (.push @(p/q '[:find (avg ?t) .
                                          :where
                                          [?e :region/temperature ?t]]
                                        cnx)))))}]])

(defn noise-graph [cnx]
  [:div
   [updating-timeline "dashboard-noise-chart"
    {:data         {:datasets [{:fill            true
                                :pointRadius     0
                                :borderColor     "#3273DC"
                                :backgroundColor "rgba(50, 115, 220, 0.1)"}]}
     :options      {:title      {:display true
                                 :text    "Average Noise Graph"}
                    :scales     {:yAxes [{:scaleLabel {:display     true
                                                       :labelString "Noise (dBA)"}
                                          :ticks      {:suggestedMin 24
                                                       :suggestedMax 60}}]
                                 :xAxes [{:scaleLabel {:display     true
                                                       :labelString "Time"}}]}
                    :tooltips   {:enabled false}
                    :legend     {:display false}
                    :responsive true}
     :interval     200
     :max-interval 101
     :update-fn    (fn [chart]
                     (let [chart-instance (:chart-instance chart)]
                       (-> (aget chart-instance "data" "datasets" 0 "data")
                           (.push @(p/q '[:find (avg ?n) .
                                          :where
                                          [?e :region/noise ?n]]
                                        cnx)))))}]])

(defn stats [env cnx]
  [:div
   [:h1.title "Statistics"]
   [:div.columns
    [:div.column [temp-graph cnx]]
    [:div.column [noise-graph cnx]]]])

(defn page-component [env]
  (let [{:keys [state]} env
        cnx @db/cnx-atom]
    [:div.container.page
     [ui/loader cnx
      [:div
       [stats env cnx]
       [region-map env cnx]]]]))