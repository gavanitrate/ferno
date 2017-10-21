(ns ferno.client.ui.charts
  (:require [reagent.core :as r]
            [goog.dom :as dom]
            [cljsjs.moment]
            [vendor.Chart]))

(defn chart-component
  [id data {:keys [update-data-fn update-data-interval]}]
  (when (nil? data)
    (throw (js/Error "No Chart.js data provided.")))
  (let [chart-atom (r/atom nil)
        chart-data (clj->js data)]
    (r/create-class
      {:component-will-mount
       (fn []
         (when (and update-data-fn update-data-interval)
           (swap! chart-atom assoc
                  :original-data data
                  :chart-update-interval-id
                  (js/setInterval
                    (fn [] (update-data-fn chart-atom))
                    update-data-interval))))

       :component-did-mount
       (fn []
         (let [ctx            (dom/getElement id)
               chart-instance (js/Chart. ctx chart-data)]
           (swap! chart-atom assoc
                  :chart-instance chart-instance)))

       :component-will-unmount
       (fn []
         (let [chart                @chart-atom
               chart-instance       (:chart-instance chart)
               update-data-instance (:chart-update-interval-id chart)]
           (when update-data-instance
             (js/clearInterval update-data-instance))
           (when chart-instance
             (.destroy chart-instance))))

       :reagent-render
       (fn [id data options]
         [:div.chartjs-container
          [:canvas.chartjs-canvas
           {:id id}]])})))

(defn time-interval-labels
  "interval is the interval in ms in between label ticks.
  max-interval is the maximum number of ticks to be shown."
  [interval max-interval]
  (->> (range max-interval)
       (map (fn [i] (str "-" (/ (* i interval) 1000) "s")))
       (into [])))

(defn updating-timeline [id {:keys [data options interval max-interval update-fn]}]
  (let [data-atom (r/atom nil)
        intervals (time-interval-labels interval max-interval)]
    (r/create-class
      {:reagent-render
       (fn [id]
         [chart-component id
          {:type    "line"
           :data    data
           :options options}
          {:update-data-fn
           (fn [chart-atom]
             (let [chart          @chart-atom
                   chart-instance (:chart-instance chart)
                   chart-data     (aget chart-instance "data" "datasets" 0 "data")
                   chart-labels   (aget chart-instance "data" "labels")]
               (update-fn chart)
               (if (>= (count chart-data) max-interval)
                 (.shift chart-data)
                 (.unshift chart-labels (nth intervals (count chart-data))))
               (.update chart-instance #js {:duration 100})))
           :update-data-interval
           interval}])})))