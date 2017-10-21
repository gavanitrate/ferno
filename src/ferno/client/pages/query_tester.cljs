(ns ferno.client.pages.query-tester
  (:require [cljs.reader :as reader]
            [reagent.core :as r]
            [posh.reagent :as p]

            [ferno.client.ui.components :as ui]
            [ferno.client.db :refer [cnx] :as db]
            )
  (:require-macros
    [ferno.macros :refer [seed-edn]]))

(defonce page-state (r/atom nil))

(def prefills
  {:tx/person      [{:person/name   "Gavan Singh"
                     :person/age    22
                     :person/gender :gender/male}]
   :tx/region      [{:region/coordinates {:coordinate/lat 1 :coordinate/lng 1}
                     :region/activities  [{:activity/type :activity/eat}
                                          {:activity/type :activity/shop}
                                          {:activity/type :activity/work}]
                     :region/sheltered   true
                     :region/temperature 24
                     :region/noise       :noise/quiet}]
   :q/person-name  '[:find ?e ?name
                     :where
                     [?e :person/name ?name]]
   :q/region-coord '[:find ?e ?name
                     :where
                     [?e :region/name ?name]]
   :p/all          '[*]})

(defn transaction-tester [data]
  [:div.box
   [:p.subtitle "Transaction Tester"]
   [:div.field
    [:label.label "TX Data"]
    [ui/textarea (:tx-data data)
     {:on-change (fn [v] (swap! page-state assoc :tx-data v))
      :rows      8}]]
   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :tx-data (-> (seed-edn) str))}
        "seed"]]
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :tx-data (-> prefills :tx/person str))}
        ":person"]]
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :tx-data (-> prefills :tx/region str))}
        ":region"]]
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :tx-data "[[:db.fn/retractEntity 1]]")}
        ":db.fn/retractEntity"]]]]]

   (try
     (when-let [tx-data (-> data :tx-data reader/read-string)]
       [:div.field.is-grouped.is-grouped-centered
        [:div.field.is-grouped
         [:div.control
          [:button.button.is-primary
           {:on-click #(db/transact tx-data)}
           "Transact Remotely"]]
         [:div.control
          [:button.button.is-danger
           {:on-click #(p/transact! (cnx) tx-data)}
           "Transact Locally"]]]])
     (catch js/Error e))])

(defn query-tester [data]
  [:div.box
   [:p.subtitle "Query Tester"]
   [:div.field
    [:label.label "Query String"]
    [ui/textarea (:query data)
     {:on-change (fn [v] (swap! page-state assoc :query v))}]]
   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :query (-> prefills :q/person-name str))}
        ":person/name"]]
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! page-state assoc :query (-> prefills :q/region-coord str))}
        ":region"]]]]]

   (when (:query data)
     (try
       (let [q      (-> data :query reader/read-string)
             result @(p/q q (cnx))]
         [:div.message.is-warning
          [:div.message-header "Query Result"]
          [:div.message-body [ui/inspect result]]])

       (catch js/Error e
         [:div.message.is-danger
          [:div.message-header "Query Error"]
          [:div.message-body [ui/inspect e]]])))])

(defn pull-tester [data]
  [:div.box
   [:p.subtitle "Pull Tester"]

   [:div.field
    [:label.label "Entity ID"]
    [:div.control
     [ui/input (:pull-id data)
      {:on-change (fn [v] (swap! page-state assoc :pull-id v))}]]]

   [:div.field
    [:label.label "Pull Query"]
    [ui/textarea (:pull-query data)
     {:on-change (fn [v] (swap! page-state assoc :pull-query v))}]]

   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:div.level-right
        [:button.button.is-small
         {:on-click #(swap! page-state assoc :pull-query (-> prefills :p/all str))}
         "*"]]]]]]

   (when (:pull-query data)
     (try
       (let [pid    (int (:pull-id data))
             q      (-> data :pull-query reader/read-string)
             result @(p/pull (cnx) q pid)]
         [:div.message.is-warning
          [:div.message-header "Query Result"]
          [:div.message-body [ui/inspect result]]])

       (catch js/Error e
         [:div.message.is-danger
          [:div.message-header "Query Error"]
          [:div.message-body [ui/inspect e]]])))])

(defonce generators-atom (r/atom nil))

(defn clamp [x min max]
  (cond
    (<= min x max) x

    (< x min) min

    (> x max) max))

(defn generate-region-temp []
  (let [regions @(p/q
                   '[:find ?e ?t
                     :where
                     [?e :region/temperature ?t]]
                   (cnx))]
    (swap! generators-atom assoc :region/temperature
           (js/setInterval
             #(doseq [r regions]
                (let [region-id    (first r)
                      region-temp  (second r)
                      updated-temp (-> region-temp
                                       (+ (- (rand-int 6) 3))
                                       (clamp 17 38))]
                  (db/transact
                    [{:db/id              region-id
                      :region/temperature updated-temp}])))
             800))))

(defn generate-region-noise []
  (let [regions @(p/q
                   '[:find ?e ?n
                     :where
                     [?e :region/noise ?n]]
                   (cnx))]
    (swap! generators-atom assoc :region/noise
           (js/setInterval
             #(doseq [r regions]
                (let [region-id     (first r)
                      region-noise  (second r)
                      updated-noise (-> region-noise
                                        (+ (- (rand-int 10) 5))
                                        (clamp 24 125))]
                  (db/transact
                    [{:db/id        region-id
                      :region/noise updated-noise}])))
             800))))



(defn stop-generator [gen-data gen-key]
  (js/clearInterval (get gen-data gen-key))
  (swap! generators-atom dissoc gen-key))

(defn generators [data gen-data]
  [:div.box
   [:p.subtitle "Generators"]
   [:div.field.is-horizontal
    [:div.field-body
     [:div.field.is-grouped
      [:div.control
       [:div.level-right
        (let [temp-gen (:region/temperature gen-data)]
          [:button.button.is-small
           {:class    (when temp-gen "is-active is-danger")
            :on-click #(if temp-gen
                         (stop-generator gen-data :region/temperature)
                         (generate-region-temp))}
           ":region/temp"])
        (let [noise-gen (:region/noise gen-data)]
          [:button.button.is-small
           {:class    (when noise-gen "is-active is-danger")
            :on-click #(if noise-gen
                         (stop-generator gen-data :region/noise)
                         (generate-region-noise))}
           ":region/noise"])
        ]]]]]])

(defn page-component [env]
  (let [{:keys [state]} env
        data            @page-state
        generators-data @generators-atom]
    [:div.container.page
     [ui/loader (cnx)
      [:div.tile.is-ancestor
       [:div.tile.is-6.is-vertical.is-parent
        [:div.tile.is-child
         [transaction-tester data]]
        [:div.tile.is-child
         [query-tester data]]
        [:div.tile.is-child
         [generators data generators-data]]]
       [:div.tile.is-parent
        [:div.tile.is-child
         [pull-tester data]]]]]]))