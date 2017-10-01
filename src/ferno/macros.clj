(ns ferno.macros)

(def firebase-service-account-file
  (->> "resources/secrets/engg4805-firebase-adminsdk-kaflx-cb111c9ff5.json"
       slurp
       clojure.data.json/read-json))

(def schema-file
  (->> "resources/db/schema.edn"
       slurp
       clojure.edn/read-string))

(def seed-file
  (->> "resources/db/seed.edn"
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

(defmacro seed-edn
  "Ferno Seed Data"
  []
  `~seed-file)