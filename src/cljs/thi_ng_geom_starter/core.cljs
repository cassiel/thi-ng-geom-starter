(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent]
            [thi-ng-geom-starter.canvas :as canvas]
            [thi-ng-geom-starter.main :as main]
            [thi-ng-geom-starter.knots :as knots]))

(enable-console-print!)

(defn app-component
  []
  [:div
   [canvas/canvas-component {:init main/init-app
                             :loop main/update-app}]
   #_ [controls]])

(reagent/render [app-component] (js/document.getElementById "app"))
(.addEventListener js/window "resize" canvas/on-window-resize)
