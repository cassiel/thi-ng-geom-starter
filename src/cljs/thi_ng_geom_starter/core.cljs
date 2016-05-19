(ns thi-ng-geom-starter.core
  (:require [reagent.core :as reagent]
            [thi-ng-geom-starter.canvas :as canvas]
            [thi-ng-geom-starter.main :as main]
            [thi-ng-geom-starter.knots :as knots]
            [thi-ng-geom-starter.rings :as rings]
            [thi-ng-geom-starter.video :as video]
            [thi-ng-geom-starter.turtle :as turtle]
            ))

(enable-console-print!)

(defn app-component
  []
  [:div
   [canvas/canvas-component {:init turtle/init-app
                             :loop turtle/update-app
                             :update #(swap! turtle/app turtle/rebuild-viewport)}
                             ]
   #_ [controls]])

(reagent/render [app-component] (js/document.getElementById "app"))
(.addEventListener js/window "resize" canvas/on-window-resize)
