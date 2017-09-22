(ns ^:figwheel-no-load ferno.dev
  (:require
    [ferno.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
