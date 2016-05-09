(ns user
  (:require [thi-ng-geom-starter.server]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.repl-api :as figwheel]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def http-handler
  (wrap-reload #'thi-ng-geom-starter.server/http-handler))

(defn run []
  (figwheel/start-figwheel!))

(def browser-repl figwheel/cljs-repl)
