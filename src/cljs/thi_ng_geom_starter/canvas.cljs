(ns thi-ng-geom-starter.canvas
  (:require [thi.ng.geom.gl.webgl.animator :as anim]
            [thi-ng-geom-starter.protocols :as px]
            [reagent.core :as reagent])
  (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(def window-size (reagent/atom nil))

(defn on-window-resize [evt]
  (reset! window-size {:w (.-innerWidth js/window)
                       :h (.-innerHeight js/window)}))

(defn canvas-component
  [app app-state]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (reagent/set-state this {:active true})
      (reset! app-state (px/init-app app))
      (anim/animate (fn [t frame] (px/update-app app this @app-state t frame))))

    :component-did-update
    (fn [this]
      (debug "UPDATE")
      (swap! app-state #(px/resize-app app %)))

    :component-will-unmount
    (fn [this]
      (debug "UNMOUNT")
      (reagent/set-state this {:active false}))

    :reagent-render
    (fn [_]
      @window-size
      [:canvas#main {:width (.-innerWidth js/window)
                     :height (.-innerHeight js/window)}])}))
