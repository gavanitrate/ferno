(ns ferno.transit
  (:require [cognitect.transit :as t]))

(defonce transit-writer (t/writer :json))
(defonce transit-reader (t/reader :json))

(defn encode [x] (t/write transit-writer x))
(defn decode [x] (when x (t/read transit-reader x)))
