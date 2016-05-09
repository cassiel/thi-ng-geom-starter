(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn canvas []
  [:canvas {:id "main" :width 1280 :height 720}])

(reagent/render [canvas] (js/document.getElementById "app"))
