(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent]
            [thi-ng-geom-starter.canvas :as canvas]
            [thi-ng-geom-starter.main :as main]
            [thi-ng-geom-starter.knots :as knots]
            [thi-ng-geom-starter.rings :as rings]
            [thi-ng-geom-starter.video :as video]
            ;; [thi-ng-geom-starter.ex04 :as ex04]
            [thi-ng-geom-starter.raycast :as raycast]
            [thi-ng-geom-starter.t-demo :as t-demo]))

(enable-console-print!)

(defn app-component
  []
  [:div
   [canvas/canvas-component {:init raycast/init-app
                             :loop raycast/update-app
                             :update #(swap! raycast/app raycast/rebuild-viewport)}
                             ]
   #_ [controls]])

(reagent/render [app-component] (js/document.getElementById "app"))
(.addEventListener js/window "resize" canvas/on-window-resize)
