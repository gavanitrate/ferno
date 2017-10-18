(ns ferno.client.ui.navbar
  (:require [reagent.core :as r]
            [ferno.client.state :as st]))

(defn navbar []
  [:nav.navbar

   [:div.navbar-brand
    [:a.navbar-item
     {:on-click #(st/change-page :dashboard)}
     [:img {:src "/img/fernotext-120.png"}]]]
   [:div.navbar-menu
    [:div.navbar-start

     ]

    [:div.navbar-end
     [:div.navbar-item.has-dropdown.is-hoverable
      [:a.navbar-link [:span.icon [:i.fa.fa-wrench]]]
      [:div.navbar-dropdown.is-right
       [:a.navbar-item
        {:on-click #(st/change-page :query-tester)}
        [:span
         [:span.icon [:i.fa.fa-database]]
         "Query Testing"]]
       [:a.navbar-item
        {:href   "https://console.firebase.google.com/u/0/project/engg4805/database/data"
         :target "_blank"}
        [:span [:span.icon [:i.fa.fa-database]] "Firebase"]]
       ]]

     (let [txactor-status (-> @st/state-atom :txactor :up)
           up?            (true? txactor-status)]
       [:a.navbar-item
        [:span.icon
         {:class (when up? "has-text-primary")}
         [:i.fa.fa-circle]]])]]])