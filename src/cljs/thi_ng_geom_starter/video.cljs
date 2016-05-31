(ns thi-ng-geom-starter.video
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
            [thi.ng.geom.plane :as p]
            [thi.ng.geom.attribs :as attr]
            [thi.ng.domus.core :as dom]
            [thi.ng.color.core :as col]
            [thi.ng.strf.core :as f]
            [thi-ng-geom-starter.shaders :as shaders])
    (:require-macros [cljs-log.core :refer [debug info warn severe]]))

(defonce app (reagent/atom {:stream {:state :wait}
                            :curr-shader :twirl}))

(defn add-video-container [w h]
  (dom/create-dom! [:video {:width w :height h :hidden false :autoplay true}]
                   (.-body js/document)))

(defn add-image-container [w h url loaded-fn]
  (let [d (dom/create-dom! [:img {:width w
                                  :height h}]
                           (.-body js/document))]

    (set! (.-onload d) #(loaded-fn d))
    (set! (.-src d) url)
    d))

(defn init-texture [container]
  (let [tex (buf/make-canvas-texture
             (:gl @app)
             container
             {:filter      glc/linear
              :wrap        glc/clamp-to-edge
              :width       (.-width container)
              :height      (.-height container)
              :flip        true
              :premultiply false})]
    (swap! app assoc-in [:scene :img :shader :state :tex] tex)))

(defn set-stream-state! [state]
  (swap! app assoc-in [:stream :state] state))

(defn activate-rtc-stream [video stream]
  (swap! app assoc-in [:stream :video] video)
  (set! (.-onerror video)
        (fn [] (.stop stream) (set-stream-state! :error)))
  (set! (.-onended stream)
        (fn [] (.stop stream) (set-stream-state! :stopped)))
  (set! (.-src video)
        (.createObjectURL (or (aget js/window "URL") (aget js/window "webkitURL")) stream))
  (set-stream-state! :ready)
  (init-texture video))

(defn init-rtc-stream [w h]
  (let [video (add-video-container w h)]
    (cond
      (aget js/navigator "webkitGetUserMedia")
      (.webkitGetUserMedia js/navigator #js {:video true}
                           #(activate-rtc-stream video %)
                           #(set-stream-state! :forbidden))

      (aget js/navigator "mozGetUserMedia")
      (.mozGetUserMedia js/navigator #js {:video true}
                        #(activate-rtc-stream video %)
                        #(set-stream-state! :forbidden))

      :else
      (set-stream-state! :unavailable))))

(defn init-image [w h]
  (let [url "img/chocolate.jpg"
        image (add-image-container w h url
                                   #(do
                                      (init-texture %)
                                      (set-stream-state! :image)))]
    ;; This is the line that kills the custom shader. (We need this to update
    ;; frames from video or camera.)
    (swap! app assoc-in [:stream :video] image)))

(defn init-video [w h]
  (let [c (dom/create-dom! [:video {:width w :height h :hidden false :autoplay true}
                            [:source {:src "video/s.mov"}]]
                           (.-body js/document))]
    (set! (.-oncanplay c) #(do (js/console.log "VIDEO onload")
                                  (init-texture c)
                                  (set-stream-state! :video)))
    (swap! app assoc-in [:stream :video] c)))

(def shader-spec
  {:vs "void main() {
    vUV = uv;
    gl_Position = proj * view * model * vec4(position, 1.0);
    }"
   :fs "void main() {
    gl_FragColor = texture2D(tex, vUV);
    }"
   :uniforms {:model    [:mat4 M44]
              :view     :mat4
              :proj     :mat4
              :tex      :sampler2D}
   :attribs  {:position :vec3
              :uv       :vec2}
   :varying  {:vUV      :vec2}
   :state    {:depth-test false
              :blend      true
              :blend-fn   [glc/src-alpha glc/one]}})

(defn make-model [gl]
  (-> (p/plane v/V3X 0.5)
  ;;(g/center)
      (g/as-mesh {:mesh (glm/gl-mesh (* 2 2) #{:uv})
                  :attribs {:uv attr/uv-faces}})
      (g/into (-> (p/plane v/V3X -0.5)
                  (g/as-mesh)))
      (gl/as-gl-buffer-spec {})
      (assoc :shader (sh/make-shader-from-spec gl shader-spec))
      (gl/make-buffers-in-spec gl glc/static-draw)))

(defn rebuild-viewport [app]
  (let [gl (:gl app)
        _  (gl/set-viewport gl {:p [0 0] :size [(.-innerWidth js/window) (.-innerHeight js/window)]})
        vr (gl/get-viewport-rect gl)]
    (assoc app
           :view-rect vr
           ;; :model (make-model gl vr)
           )))

(defn init-app
  [this]
  (let [vw        640
        vh        480
        gl        (gl/gl-context (reagent/dom-node this))
        view-rect (gl/get-viewport-rect gl)
        thresh    (sh/make-shader-from-spec gl shaders/threshold-shader-spec)
        hue-shift (sh/make-shader-from-spec gl shaders/hueshift-shader-spec)
        twirl     (sh/make-shader-from-spec gl shaders/twirl-shader-spec)
        pixelate  (sh/make-shader-from-spec gl shaders/pixelate-shader-spec)
        tile      (sh/make-shader-from-spec gl shaders/tile-shader-spec)
        fbo-tex   (buf/make-texture
                   gl {:width  512
                       :height 512
                       :filter glc/linear
                       :wrap   glc/clamp-to-edge})
        fbo       (buf/make-fbo-with-attachments
                   gl {:tex    fbo-tex
                       :width  512
                       :height 512
                       :depth? true})]
    (swap! app merge
           {:gl          gl
            :view        view-rect
            :shaders     {:thresh    thresh
                          :hue-shift hue-shift
                          :twirl     twirl
                          :tile      tile
                          :pixelate  pixelate}
            :scene       {:fbo     fbo
                          :fbo-tex fbo-tex
                          :model   (make-model gl)
                          :img     (-> (fx/init-fx-quad gl)
                                       #_ (assoc :shader thresh))}})
    ;;(init-rtc-stream vw vh)
    ;;(init-video vw vh)
    (init-image vw vh)
    ))

(def try-it true)

(defn update-app
  [this]
  (fn [t frame]
    (let [{:keys [gl view scene stream shaders curr-shader]} @app]
      ;;(debug "frame with tex?" (str (get-in scene [:img :shader])))
      (when-let [tex (get-in scene [:img :shader :state :tex])]
        ;;(gl/configure tex {:image (:video stream)})
        (gl/bind tex)
        ;; render to texture
        (when try-it (gl/bind (:fbo scene)))
        (doto gl
          (gl/set-viewport 0 0 512 512)
          (gl/clear-color-and-depth-buffer col/BLACK 1)
          (gl/draw-with-shader
           (-> (:img scene)
               (assoc-in [:uniforms :time] t)
               (assoc :shader (shaders curr-shader)))))
        (when try-it (gl/unbind (:fbo scene)))
        ;; render model to main canvas
        (when try-it
          (gl/bind (:fbo-tex scene) 0)
          (doto gl
            (gl/set-viewport view)
            (gl/draw-with-shader
             (-> (:model scene)
                 (cam/apply
                  (cam/perspective-camera
                   {:eye (vec3 0 0 1.0) :fov 90 :aspect view}))
                 (assoc-in [:uniforms :model] (-> M44 (g/rotate-x t) (g/rotate-y (* t 2)))))))))
      (:active (reagent/state this)))))
