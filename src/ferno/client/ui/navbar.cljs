(ns ferno.client.ui.navbar
  (:require [reagent.core :as r]))

(defn navbar []
  [:nav.navbar
   [:div.navbar-brand
    [:a.navbar-item
     {:on-click #(println "home")}
     [:img {:src "/img/fernotext-120.png"}]]]
   [:div.navbar-menu
    [:div.navbar-start]

    [:div.navbar-end
     [:div.navbar-item.has-dropdown.is-hoverable
      [:a.navbar-link [:span.icon [:i.fa.fa-wrench]]]
      [:div.navbar-dropdown.is-right
       [:a.navbar-item
        {:href   "https://console.firebase.google.com/u/0/project/engg4805/database/data"
         :target "_blank"}
        [:span [:span.icon [:i.fa.fa-database]] "Firebase"]]]]

     [:a.navbar-item [:span.icon.has-text-success [:i.fa.fa-circle]]]]]])