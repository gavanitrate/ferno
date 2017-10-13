(ns ferno.client.firebase
  (:require [vendor.firebase.app]
            [vendor.firebase.database]
            [ferno.client.state :as st]))

(defonce fb-config
         {:apiKey            "AIzaSyAzl-3YjRh4kF4dQ7wz6pkVSZtMhuV1u5g"
          :authDomain        "engg4805.firebaseapp.com"
          :databaseURL       "https://engg4805.firebaseio.com"
          :projectId         "engg4805"
          :storageBucket     "engg4805.appspot.com"
          :messagingSenderId "280090039025"})

(defonce fb-init (js/firebase.initializeApp (clj->js fb-config) "[DEFAULT]"))

(defonce fb-app js/firebase)

(def database (-> fb-app .database))

(defonce tx-status
         (-> database
             (.ref "txactor/up")
             (.on "value" (fn [ss] (swap! st/state-atom
                                          assoc-in [:txactor :up] (.val ss))))))