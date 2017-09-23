(ns ferno.client.ui.navbar
  (:require [reagent.core :as r]))

(defn navbar []
  [:nav.navbar.is-transparent
   [:div.navbar-brand
    [:a.navbar-item
     {:on-click #(println "home")}
     "Ferno"]]
   [:div.navbar-menu
    [:div.navbar-start
     ]
    [:div.navbar-end
     [:div.navbar-item.has-dropdown.is-hoverable
      [:div.navbar-link "Tools"]
      [:div.navbar-dropdown
       [:a.navbar-item
        {:href   "https://console.firebase.google.com/u/0/project/engg4805/database/data"
         :target "_blank"}
        "Firebase"]
       [:a.navbar-item "Test"]
       [:a.navbar-item "Test"]
       [:a.navbar-item "Test"]]]]]])