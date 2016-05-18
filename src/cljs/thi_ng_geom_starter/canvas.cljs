(ns thi-ng-geom-starter.canvas
  (:require [thi.ng.geom.gl.webgl.animator :as anim]
            [thi-ng-geom-starter.main :as main]
            [reagent.core :as reagent])
  (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(def window-size (reagent/atom nil))

(defn on-window-resize [evt]
  (reset! window-size {:w (.-innerWidth js/window)
                       :h (.-innerHeight js/window)}))

(defn canvas-component
  [props]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (reagent/set-state this {:active true})
      ((:init props) this)
      (anim/animate ((:loop props) this)))

    :component-did-update
    (fn [this]
      (debug "UPDATE")
      ((:update props) this))

    :component-will-unmount
    (fn [this]
      (debug "UNMOUNT")
      (reagent/set-state this {:active false}))

    :reagent-render
    (fn [_]
      @window-size
      [:canvas#main
       (merge
        {:width (.-innerWidth js/window)
         :height (.-innerHeight js/window)}
        props)])}))
