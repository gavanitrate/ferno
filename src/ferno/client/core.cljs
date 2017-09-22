(ns ferno.client.core
  (:require [reagent.core :as r]
            [ferno.client.db :as db]
            [posh.reagent :as p]))

(defn home-page []
  [:div.container
   [:h2 "Ferno"]
   [:button.btn {:on-click #(db/transact [{:person/email "gagan.lives@live.com"}])} "add"]

   (let [es @(p/q '[:find [?e ...]
                    :where [?e :person/email ?email]] db/cnx)]
     [:div
      (doall (map (fn [e]
                    ^{:key e}
                    [:div
                     (str @(p/pull db/cnx '[*] e))]) es))])])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
