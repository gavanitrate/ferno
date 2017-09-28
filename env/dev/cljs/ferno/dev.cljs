(ns ^:figwheel-no-load ferno.dev
  (:require
    [ferno.client.core :as core]
    [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)