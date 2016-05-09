(ns thi-ng-geom-starter.canvas
  (:require
    [thi.ng.geom.gl.webgl.animator :as anim]
    [reagent.core :as reagent]))

(defn canvas-component
  [props]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (reagent/set-state this {:active true})
      ((:init props) this)
      (anim/animate ((:loop props) this)))

    :component-will-unmount
    (fn [this]
      (reagent/set-state this {:active false}))

    :reagent-render
    (fn [_]
      [:canvas
       (merge
        {:width (.-innerWidth js/window)
         :height (.-innerHeight js/window)}
        props)])}))
