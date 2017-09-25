(defproject ferno "0.1.0-SNAPSHOT"
  :description "engg4805 project"
  :url "https://github.com/gavanitrate/ferno"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;[org.clojure/clojure "1.9.0-beta1"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/data.json "0.2.6"]

                 [reagent "0.8.0-alpha1"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [datascript "0.16.2"]
                 [posh "0.5.5"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.13"]]

  :min-lein-version "2.5.0"

  :clean-targets
  ^{:protect false}
  [:target-path
   "resources/public/css"
   "resources/public/js"
   "resources/build"]

  :figwheel {:http-server-root "public"
             :server-port      3964
             :css-dirs         ["resources/public/css"]
             :repl             false}

  :cljsbuild {:builds
              {:client-dev
               {:source-paths ["src/ferno/client" "env/dev/cljs"]
                :compiler     {:main          ferno.dev
                               :output-to     "resources/public/js/app.js"
                               :output-dir    "resources/public/js/out"
                               :asset-path    "js/out"
                               :source-map    true
                               :optimizations :none
                               :pretty-print  true}
                :figwheel     {:on-jsload "ferno.client.core/mount-root"
                               :open-urls ["http://localhost:3964/index.html"]}}

               :client-release
               {:source-paths ["src/ferno/client" "env/prod/cljs"]
                :compiler     {:main          ferno.prod
                               :output-to     "resources/public/js/app.js"
                               :output-dir    "target/public/js/release"
                               :optimizations :advanced
                               :pretty-print  false}}

               :txactor
               {:source-paths ["src/ferno/txactor"]
                :compiler     {:target        :nodejs
                               :main          ferno.txactor.core
                               :output-to     "resources/build/ferno-txactor/txactor.js"
                               :output-dir    "resources/build/ferno-txactor"
                               :npm-deps      {:firebase-admin "5.2.1"}
                               :install-deps  true
                               :optimizations :simple}}}}

  :aliases {"package-client"  ["do" "clean" ["cljsbuild" "once" "client-release"]]
            "package-txactor" ["do" "clean" ["cljsbuild" "once" "txactor"]]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.13"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]]}})
