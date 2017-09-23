(ns ferno.client.core
  (:require [reagent.core :as r]
            [ferno.client.ui.navbar :refer [navbar]]
            [ferno.client.pages.query-tester :as query-tester]))

(defn main []
  [:div
   [navbar]
   [query-tester/page-component]])

(defn mount-root []
  (r/render [main] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
