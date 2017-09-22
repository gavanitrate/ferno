(ns ferno.txactor.macros)

(def firebase-service-account-file
  (->> "resources/secrets/engg4805-firebase-adminsdk-kaflx-cb111c9ff5.json"
       slurp
       clojure.data.json/read-json))

(defmacro firebase-service-account
  "Firebase Service Account"
  []
  `~firebase-service-account-file)