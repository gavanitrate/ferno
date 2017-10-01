(ns ferno.client.core
  (:require [reagent.core :as r]
            [ferno.client.db :as db]
            [ferno.client.state :as st]
            [ferno.client.ui.navbar :refer [navbar]]

            [ferno.client.pages.dashboard :as dashboard]
            [ferno.client.pages.query-tester :as query-tester]))

(defn pages [env page]
  (case page
    :dashboard [dashboard/page-component env]
    :query-tester [query-tester/page-component env]
    nil [:h1 "Unknown"]))

(defn footer []
  [:footer])

(defn main []
  (let [state @st/state-atom
        env   {:state state}]
    [:div
     [navbar env]
     [pages env (st/page state)]
     [footer]
     ]))

(defn mount-root []
  (r/render [main] (.getElementById js/document "app")))

(defn init! []
  (db/start)
  (mount-root))
