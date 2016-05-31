(ns thi-ng-geom-starter.dummy
  (:require [thi-ng-geom-starter.protocols :as px])
  (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(defn app []
  (reify px/APP
    (init-app [_]
      (debug "dummy: INIT")
      {:junk1 "JUNK" :junk2 "JUNK "})

    (update-app [_ component app-state t frame]
      (debug "dummy: UPDATE t=" t " frame=" frame)
      true)

    (resize-app [_ app-state]
      (debug "RESIZE")
      app-state)))
