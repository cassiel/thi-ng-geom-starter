(ns geometer.macros
  (:require [thi.ng.geom.aabb   :refer [aabb]]
            [thi.ng.geom.core   :as g]
            [thi.ng.geom.matrix :refer [M44]]
            [thi.ng.geom.vector :refer [vec3]]
            [thi.ng.math.core   :as m]))

(defmacro defmove
  [op x y z]
  `(defn ~op [t#]
     (let [~'length ((:length-fn t#) t#)]
       (update (assoc t# :last-length ~'length) :matrix g/translate ~x ~y ~z))))

(defmacro defrot
  [op axis expr]
  (let [rot-fn (case axis
                 x 'thi.ng.geom.core/rotate-x
                 y 'thi.ng.geom.core/rotate-y
                 z 'thi.ng.geom.core/rotate-z)]
    `(defn ~op [t#]
       (let [~'angle ((:angle-fn t#) t#)]
         (update (assoc t# :last-angle ~'angle)
                 :matrix ~rot-fn (m/radians ~expr))))))

(defmacro defshape [name body]
  `(defn ~name [t#]
     (let [~'length ((:length-fn t#) t#)]
       (assoc t#
              :last-length ~'length
              :matrix (g/translate (:matrix t#) 0 0 ~'length)
              :mesh   (g/into (:mesh t#)
                              (-> ~body
                                  (g/transform (:matrix t#))
                                  (g/as-mesh)))))))
