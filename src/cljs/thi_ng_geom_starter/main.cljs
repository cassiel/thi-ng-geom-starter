(ns thi-ng-geom-starter.main
  (:require [reagent.core :as reagent]
            [thi.ng.math.core :as m :refer [PI HALF_PI TWO_PI]]
            [thi.ng.geom.gl.core :as gl]
            [thi.ng.geom.gl.webgl.constants :as glc]
            [thi.ng.geom.gl.webgl.animator :as anim]
            [thi.ng.geom.gl.glmesh :as glm]
            [thi.ng.geom.gl.camera :as cam]
            [thi.ng.geom.gl.shaders :as sh]
            [thi.ng.geom.gl.shaders.lambert :as lambert]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.vector :as v :refer [vec2 vec3]]
            [thi.ng.geom.matrix :as mat :refer [M44]]
            [thi.ng.geom.aabb :as a]
            [thi.ng.geom.attribs :as attr]
            [thi.ng.color.core :as col]
            [thi.ng.strf.core :as f]))

(defn init-app [this]
  (let [gl        (gl/gl-context "main")
        view-rect (gl/get-viewport-rect gl)
        model     (-> (a/aabb 0.8)
                      (g/center)
                      (g/as-mesh
                       {:mesh    (glm/indexed-gl-mesh 12 #{:col :fnorm})
                        ;;:flags   :ewfbs
                        :attribs {:col (->> [[1 0 0] [0 1 0] [0 0 1] [0 1 1] [1 0 1] [1 1 0]]
                                            (map col/rgba)
                                            (attr/const-face-attribs))}})
                      (gl/as-gl-buffer-spec {})
                      (cam/apply (cam/perspective-camera {:aspect view-rect}))
                      (assoc :shader (sh/make-shader-from-spec gl lambert/shader-spec-two-sided-attrib))
                      (gl/make-buffers-in-spec gl glc/static-draw))]
    (assoc this
           :gl gl
           :view-rect view-rect
           :model model)))

(defn update-app [this]
  (fn [t frame]
    (js/console.log (f/format ["Still here, f=" f/int] frame))
    (:active (reagent/state this))))
