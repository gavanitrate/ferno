(ns ferno.client.pages.query-tester
  (:require [cljs.reader :as reader]
            [reagent.core :as r]
            [posh.reagent :as p]
            [ferno.client.db :as db]
            [ferno.client.ui.components :as ui]))

(defonce state (r/atom nil))

(defn transaction-tester [data]
  [:div.card.query-tester-component
   [:div.card-header [:p.card-header-title "Transaction Tester"]]
   [:div.card-content
    [:div.message
     [:div.message-header "TX Data"]
     [:div.message-body
      [ui/textarea (:tx-data data)
       {:on-change (fn [v] (swap! state assoc :tx-data v))}]

      [:div.level.push-up
       [:div.level-left
        [:label.level-item "Prefill"]]
       [:div.level-right
        [:button.button.is-small
         {:on-click #(swap! state assoc :tx-data "[{:person/name \"Greg Davies\"}]")}
         ":person/name"]]]]]

    (try
      (when-let [tx-data (-> data :tx-data reader/read-string)]
        [:div.level.push-up
         [:div.level-left]
         [:div.level-right
          [:div.field.is-grouped
           [:div.control
            [:button.button.is-primary
             {:on-click #(db/transact tx-data)}
             "Transact Remotely"]]
           [:div.control
            [:button.button.is-danger
             {:on-click #(p/transact! db/cnx tx-data)}
             "Transact Locally"]]]]])
      (catch js/Error e))]])

(defn query-tester [data]
  [:div.card.query-tester-component
   [:div.card-header [:p.card-header-title "Query Tester"]]
   [:div.card-content
    [:div.message
     [:div.message-header "Query String"]
     [:div.message-body
      [ui/textarea (:query data)
       {:on-change (fn [v] (swap! state assoc :query v))}]

      [:div.level.push-up
       [:div.level-left
        [:label.level-item "Prefill"]]
       [:div.level-right
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
           [:div.message-body [ui/inspect e]]])))]])

(defn pull-tester [data]
  [:div.card.query-tester-component
   [:div.card-header [:p.card-header-title "Pull Tester"]]
   [:div.card-content

    [:div.message
     [:div.message-header "Entity ID"]
     [:div.message-body
      [ui/input (:pull-id data)
       {:on-change (fn [v] (swap! state assoc :pull-id v))}]]]

    [:div.message
     [:div.message-header "Pull Query"]
     [:div.message-body
      [ui/textarea (:pull-query data)
       {:on-change (fn [v] (swap! state assoc :pull-query v))}]

      [:div.level.push-up
       [:div.level-left
        [:label.level-item "Prefill"]]
       [:div.level-right
        [:button.button.is-small
         {:on-click #(swap! state assoc :pull-query "[*]")}
         "*"]]]]]

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
           [:div.message-body [ui/inspect e]]])))]])

(defn page-component []
  (let [data @state]
    [:div.container.page
     [transaction-tester data]
     [query-tester data]
     [pull-tester data]]))