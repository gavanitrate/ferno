(ns ferno.txactor.macros)

(def firebase-service-account-file
  (->> "resources/secrets/engg4805-firebase-adminsdk-kaflx-cb111c9ff5.json"
       slurp
       clojure.data.json/read-json))

(def schema-file
  (->> "resources/schema/schema.edn"
       slurp
       clojure.edn/read-string))

(defmacro firebase-service-account
  "Firebase Service Account"
  []
  `~firebase-service-account-file)

(defmacro schema-edn
  "Ferno Schema"
  []
  `~schema-file)