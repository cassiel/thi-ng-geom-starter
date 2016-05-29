(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent]
            [thi-ng-geom-starter.canvas :as canvas]
            [thi-ng-geom-starter.main :as main]
            [thi-ng-geom-starter.knots :as knots]
            [thi-ng-geom-starter.rings :as rings]
            [thi-ng-geom-starter.video :as video]
            [thi-ng-geom-starter.t-demo :as t-demo]))

(enable-console-print!)

(defn app-component
  []
  [:div
   [canvas/canvas-component {:init video/init-app
                             :loop video/update-app
                             :update #(swap! video/app video/rebuild-viewport)}
                             ]
   #_ [controls]])

(reagent/render [app-component] (js/document.getElementById "app"))
(.addEventListener js/window "resize" canvas/on-window-resize)
