(ns ferno.client.core
  (:require [reagent.core :as r]))

(defn home-page []
  [:div.container
   [:h2 "Ferno"]])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
