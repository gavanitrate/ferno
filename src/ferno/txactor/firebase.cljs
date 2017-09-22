(ns ferno.txactor.firebase
  (:require [firebase-admin :as fb])
  (:require-macros
    [ferno.txactor.macros :refer [firebase-service-account]]))

(defonce fb-cred (-> fb .-credential
                     (.cert (clj->js (firebase-service-account)))))

(.initializeApp fb #js {:credential  fb-cred
                        :databaseURL "https://engg4805.firebaseio.com"})