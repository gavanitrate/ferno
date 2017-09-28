(ns ferno.transit
  (:require [cognitect.transit :as t]))

(defonce transit-writer (t/writer :json))
(defonce transit-reader (t/reader :json))

(defn encode
  "Encode data into transit format."
  [x] (t/write transit-writer x))
(defn decode
  "Decode data form transit format back to Clojure structures."
  [x] (when x (t/read transit-reader x)))
