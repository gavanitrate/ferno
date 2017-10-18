(ns ferno.client.ui.charts
  (:require [reagent.core :as r]
            [goog.dom :as dom]
            [cljsjs.moment]
            [vendor.Chart]))

(defn chart
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
         [chart id
          {:type    "line"
           :data    data
           :options options}
          {:update-data-fn
           (fn [chart-atom]
             (let [ch     @chart-atom
                   chi    (-> @chart-atom :chart-instance)
                   dcount (-> chi .-data .-datasets first .-data count)]
               (if (>= dcount max-interval)
                 (-> chi .-data .-datasets first .-data .shift)
                 (-> chi .-data .-labels (.unshift (nth intervals dcount))))
               (update-fn ch)
               (.update chi (clj->js {:duration 0}))))
           :update-data-interval
           interval}])})))