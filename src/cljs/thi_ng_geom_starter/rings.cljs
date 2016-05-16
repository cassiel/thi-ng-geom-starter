(ns thi-ng-geom-starter.rings
  (:require
   [reagent.core :as reagent]
   [thi.ng.math.core :as m :refer [PI HALF_PI TWO_PI]]
   [thi.ng.geom.gl.core :as gl]
   [thi.ng.geom.gl.webgl.constants :as glc]
   [thi.ng.geom.gl.webgl.animator :as anim]
   [thi.ng.geom.gl.buffers :as buf]
   [thi.ng.geom.gl.shaders :as sh]
   [thi.ng.geom.gl.shaders.basic :as basic]
   [thi.ng.geom.gl.shaders.phong :as phong]
   [thi.ng.geom.gl.glmesh :as glm]
   [thi.ng.geom.gl.camera :as cam]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.vector :as v :refer [vec2 vec3]]
   [thi.ng.geom.matrix :as mat :refer [M44]]
   [thi.ng.geom.attribs :as attr]
   [thi.ng.geom.circle :as c]
   [thi.ng.geom.ptf :as ptf]
   [thi.ng.glsl.core :as glsl :include-macros true]
   [thi.ng.glsl.vertex :as vertex]
   [thi.ng.glsl.lighting :as light]
   [thi.ng.glsl.fog :as fog]
   [thi.ng.glsl.noise :as noise]
   [thi.ng.color.core :as col]
   [thi.ng.color.gradients :as grad])
  (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(enable-console-print!)

(def shader-spec
  {:vs       (->> "void main() {
                vUV = uv + vec2(0, time * 0.025);
                vPos0 = position.xy;
                vPos = (view * model * vec4(position, 1.0)).xyz;
                vNormal = surfaceNormal(normal, normalMat);
                vLightDir = (view * vec4(lightPos, 1.0)).xyz - vPos;
                gl_Position = proj * vec4(vPos, 1.0);
              }"
                  (glsl/glsl-spec [vertex/surface-normal])
                  (glsl/assemble))
   :fs       (->> "void main() {
                vec3 n = normalize(vNormal);
                vec3 v = normalize(-vPos);
                vec3 l = normalize(vLightDir);
                float NdotL = max(0.0, dot(n, l));
                vec3 specular = Ks * beckmannSpecular(l, v, n, m);
                vec3 att = lightCol / pow(length(vLightDir), lightAtt);
                //vec3 diff = texture2D(tex, vUV).xyz;
                vec3 diff = vec3(0.0);
                //vec3 diff = vec3(0.1 * snoise(vPos0.xy * 10.0));
                //vec3 diff = vec3(mod(1.0 * vPos0.x * vPos0.y * 3.0, 1.0));
                vec3 col = att * NdotL * ((1.0 - s) * diff + s * specular) + Ka * diff;
                float fog = fogLinear(length(vPos), 1.0, 7.5);
                col = mix(col, Kf, fog);
                gl_FragColor = vec4(col, 1.0);
              }"
                  (glsl/glsl-spec [fog/fog-linear light/beckmann-specular noise/snoise])
                  (glsl/assemble))
   :uniforms {:model     :mat4
              :view      :mat4
              :proj      :mat4
              :normalMat :mat4
              :tex       :sampler2D
              :Ks        [:vec3 [1 1 1]]
              :Ka        [:vec3 [0.0 0.0 0.3]]
              :Kf        [:vec3 ((juxt col/red col/green col/blue) col/GRAY)]
              :m         [:float 0.9]
              :s         [:float 0.1]
              :lightCol  [:vec3 #_ [255 255 255] [200 80 40]]
              :lightPos  [:vec3 [0 0 5]]
              :lightAtt  [:float 3.0]
              :time      :float}
   :attribs  {:position :vec3
              :normal   :vec3
              :uv       :vec2}
   :varying  {:vUV       :vec2
              :vPos0     :vec2
              :vPos      :vec3
              :vNormal   :vec3
              :vLightDir :vec3}
   :state    {:depth-test true}})

(defn trajectoryR [t]
  (let [mul 3
        t (* t m/TWO_PI)]
    (-> (v/vec3 (Math/cos t)  (Math.sin t) 0)
        (g/scale mul))))

(defn trajectory-curve [t]
  (cond
    (< t (/ 1 3)) (m/mix (vec3 -1 -3 0)
                         (vec3 -1 0 0)
                         (* t 3))

    (> t (/ 2 3)) (m/mix (vec3 0 1 0)
                         (vec3 3 1 0)
                         (* (- t (/ 2 3)) 3))

    :else (let [t (* (- t (/ 1 3)) 3)
                t (* t m/TWO_PI)
                t (/ t 4)]
            (vec3 (- (Math/cos t))
                    (Math/sin t)
                    0))))

(defn trajectory [t]
  (cond
    (< t (/ 1 2)) (m/mix (vec3 -1 -3 0)
                         (vec3 -1 1 0)
                         (* t 2))

    :else (m/mix (vec3 1 1 0)
                 (vec3 3 1 0)
                 (* (- t (/ 1 2)) 2))))

(defn ring-simple
  []
  (-> (mapv trajectory (butlast (m/norm-range 400)))
      (ptf/sweep-mesh (g/vertices (c/circle 0.5) 40)
                      {:mesh    (glm/gl-mesh 32000 #{:fnorm :uv})
                       :attribs {:uv attr/uv-tube}
                       :align?  true
                       :loop?   false
                       :close?  false})))

(defn gradient-texture
  [gl w h opts]
  (let [canv (.createElement js/document "canvas")
        ctx  (.getContext canv "2d")
        cols (apply grad/cosine-gradient h (:rainbow1 grad/cosine-schemes))]
    (set! (.-width canv) w)
    (set! (.-height canv) h)
    (set! (.-strokeStyle ctx) "none")
    (loop [y 0, cols cols]
      (if cols
        (let [c (first cols)
              c (if (< (mod y 16) 8)
                  (col/adjust-brightness c -0.75)
                  c)]
          (set! (.-fillStyle ctx) @(col/as-css c))
          (.fillRect ctx 0 y w 1)
          (recur (inc y) (next cols)))
        [canv (buf/make-canvas-texture gl canv opts)]))))

(defonce app (reagent/atom {}))

(defn init-app [_]
  (debug "INIT")
  (let [gl          (gl/gl-context "main")
        view-rect   (gl/get-viewport-rect gl)
        model       (-> (ring-simple)
                        (gl/as-gl-buffer-spec {})
                        (assoc :shader (sh/make-shader-from-spec gl shader-spec))
                        (gl/make-buffers-in-spec gl glc/static-draw)
                        (time))
        [tcanv tex] (gradient-texture gl 4 1024 {:wrap [glc/clamp-to-edge glc/repeat]})]
    (reset! app {:gl        gl
                 :view-rect view-rect
                 :model     model
                 :tcanv     tcanv
                 :tex       tex})
    (.appendChild (.-body js/document) tcanv)))

(defn update-app [this]
  (fn [t frame]
    (when (:active (reagent/state this))
      (let [{:keys [gl view-rect model tex]} @app]
        (gl/bind tex 0)
        (doto gl
          (gl/set-viewport view-rect)
          (gl/clear-color-and-depth-buffer col/GRAY 1)

          (gl/draw-with-shader
           (-> model
               (cam/apply (cam/perspective-camera
                           {:eye (vec3 0 0 3) :fov 90 :aspect view-rect}))
               (update :uniforms assoc
                       :time t
                       :m (+ 0.21 (* 0.2 (Math/sin (* t 1))))
                       :model (-> M44 (g/rotate-x (* t 0.1)) (g/rotate-y (* t 0.1))))
                       (gl/inject-normal-matrix :model :view :normalMat)
               ))))
      true)))
