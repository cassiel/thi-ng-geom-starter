(ns thi-ng-geom-starter.turtle
  (:require [reagent.core :as reagent]
            [thi.ng.math.core :as m :refer [PI HALF_PI TWO_PI]]
            [thi.ng.geom.gl.core :as gl]
            [thi.ng.geom.gl.webgl.constants :as glc]
            [thi.ng.geom.gl.webgl.animator :as anim]
            [thi.ng.geom.gl.buffers :as buf]
            [thi.ng.geom.gl.fx :as fx]
            [thi.ng.geom.gl.shaders :as sh]
            [thi.ng.geom.gl.glmesh :as glm]
            [thi.ng.geom.gl.camera :as cam]
            [thi.ng.geom.gl.shaders :as sh]
            [thi.ng.geom.gl.shaders.lambert :as lambert]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as v :refer [vec2 vec3]]
            [thi.ng.geom.matrix :as mat :refer [M44]]
            [thi.ng.geom.aabb :as a]
            [thi.ng.geom.attribs :as attr]
            [thi.ng.domus.core :as dom]
            [thi.ng.color.core :as col]
            [thi.ng.strf.core :as f]
            [thi-ng-geom-starter.shaders :as shaders])
    (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(defonce app (reagent/atom {}))

(defn make-model [gl]
  (-> (a/aabb 0.8)
      (g/center)
      (g/as-mesh
       {:mesh    (glm/indexed-gl-mesh 12 #{:col :fnorm})
        ;;:flags   :ewfbs
        :attribs {:col (->> [[1 0 0] [0 1 0] [0 0 1] [0 1 1] [1 0 1] [1 1 0]]
                            (map col/rgba)
                            (attr/const-face-attribs))}})
      (gl/as-gl-buffer-spec {})
      (assoc :shader (sh/make-shader-from-spec gl lambert/shader-spec-two-sided-attrib))
      (gl/make-buffers-in-spec gl glc/static-draw)))

(defn rebuild-viewport [app]
  (let [gl (:gl app)
        _  (gl/set-viewport gl {:p [0 0] :size [(.-innerWidth js/window) (.-innerHeight js/window)]})
        vr (gl/get-viewport-rect gl)]
    (assoc app
           :view-rect vr
           ;; :model (make-model gl vr)
           )))

(defn init-app [_]
  (debug "INIT")
  (let [gl        (gl/gl-context "main")
        view-rect (gl/get-viewport-rect gl)
        model     (make-model gl)]
    (swap! app merge {:gl        gl
                      :view-rect view-rect
                      :model     model})))

(defn update-app [this]
  (fn [t frame]
    (when (:active (reagent/state this))
      (let [{:keys [gl view-rect model]} @app]
        (doto gl
          (gl/set-viewport view-rect)
          (gl/clear-color-and-depth-buffer col/GRAY 1)
          (gl/draw-with-shader
           (-> model
               (cam/apply
                (cam/perspective-camera
                 {:eye (vec3 0 0 1.25)
                  ;;:up (m/normalize (vec3 (Math/sin t) 1 0))
                  :fov 90
                  :aspect view-rect}))
               (assoc-in [:uniforms :model]
                         (-> M44 (g/rotate-x t) (g/rotate-y (* t 2))))))))

      true)))
