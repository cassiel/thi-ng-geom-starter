(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent]
            [thi-ng-geom-starter.canvas :as canvas]
            [thi-ng-geom-starter.main :as main]
            [thi-ng-geom-starter.knots :as knots]
            [thi-ng-geom-starter.rings :as rings]))

(enable-console-print!)

(defn app-component
  []
  [:div
   [canvas/canvas-component {:init rings/init-app
                             :loop rings/update-app
                             :update #(swap! rings/app rings/rebuild-viewport)}
                             ]
   #_ [controls]])

(reagent/render [app-component] (js/document.getElementById "app"))
(.addEventListener js/window "resize" canvas/on-window-resize)
