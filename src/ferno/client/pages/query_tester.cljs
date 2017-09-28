(ns ferno.client.pages.query-tester
  (:require [cljs.reader :as reader]
            [reagent.core :as r]
            [posh.reagent :as p]
            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]))

(defonce state (r/atom nil))

(defn transaction-tester [data]
  [:div.box
   [:p.subtitle "Transaction Tester"]
   [:div.field
    [:label.label "TX Data"]
    [ui/textarea (:tx-data data)
     {:on-change (fn [v] (swap! state assoc :tx-data v))}]]
   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! state assoc :tx-data "[{:person/name \"Gavan Singh\"\n:person/age 22\n:person/height 179\n:person/eye-colour :brown}]")}
        ":person"]]

      [:div.control
       [:button.button.is-small
        {:on-click #(swap! state assoc :tx-data "[[:db/retract 1 :person/age 22]]")}
        ":db/retract"]]
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! state assoc :tx-data "[[:db.fn/retractEntity 1]]")}
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
           {:on-click #(p/transact! db/cnx tx-data)}
           "Transact Locally"]]]])
     (catch js/Error e))])

(defn query-tester [data]
  [:div.box
   [:p.subtitle "Query Tester"]
   [:div.field
    [:label.label "Query String"]
    [ui/textarea (:query data)
     {:on-change (fn [v] (swap! state assoc :query v))}]]
   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:button.button.is-small
        {:on-click #(swap! state assoc :query "[:find ?e ?name\n:where\n[?e :person/name ?name]]")}
        ":person/name"]]]]]

   (when (:query data)
     (try
       (let [q      (-> data :query reader/read-string)
             result @(p/q q db/cnx)]
         [:div.message.is-success
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
      {:on-change (fn [v] (swap! state assoc :pull-id v))}]]]

   [:div.field
    [:label.label "Pull Query"]
    [ui/textarea (:pull-query data)
     {:on-change (fn [v] (swap! state assoc :pull-query v))}]]

   [:div.field.is-horizontal
    [:label.label "Prefills"]
    [:div.field-body
     [:div.field.is-grouped.is-grouped-right
      [:div.control
       [:div.level-right
        [:button.button.is-small
         {:on-click #(swap! state assoc :pull-query "[*]")}
         "*"]]]]]]

   (when (:pull-query data)
     (try
       (let [pid    (int (:pull-id data))
             q      (-> data :pull-query reader/read-string)
             result @(p/pull db/cnx q pid)]
         [:div.message.is-success
          [:div.message-header "Query Result"]
          [:div.message-body [ui/inspect result]]])

       (catch js/Error e
         [:div.message.is-danger
          [:div.message-header "Query Error"]
          [:div.message-body [ui/inspect e]]])))])

(defn page-component []
  (let [data @state]
    [:div.container.page
     [:div.tile.is-ancestor
      [:div.tile.is-6.is-vertical.is-parent
       [:div.tile.is-child
        [transaction-tester data]]
       [:div.tile.is-child
        [query-tester data]]]
      [:div.tile.is-parent
       [:div.tile.is-child
        [pull-tester data]]]]]))