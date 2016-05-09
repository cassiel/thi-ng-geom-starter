(ns thi-ng-geom-starter.main
  (:require [reagent.core :as reagent]
            [thi.ng.strf.core :as f]))

(defn init-app [this]
  this)

(defn update-app [this]
  (fn [t frame]
    (js/console.log (f/format ["Still here, f=" f/int] frame))
    (:active (reagent/state this))))
