(ns ferno.firebase
  (:require [vendor.firebase.app]
            [vendor.firebase.database]))

;; init
(defonce fb-config
         {:apiKey            "AIzaSyAzl-3YjRh4kF4dQ7wz6pkVSZtMhuV1u5g"
          :authDomain        "engg4805.firebaseapp.com"
          :databaseURL       "https://engg4805.firebaseio.com"
          :projectId         "engg4805"
          :storageBucket     "engg4805.appspot.com"
          :messagingSenderId "280090039025"})

(defonce fbinit (js/firebase.initializeApp (clj->js fb-config) "[DEFAULT]"))


(defonce fb-app js/firebase)

(def database (-> fb-app .database))