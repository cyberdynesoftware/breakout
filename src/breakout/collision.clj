(ns breakout.collision
  (:require [breakout.game-object :refer :all])
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defn aabb-collision?
  [game-object1 game-object2]
  (let [^org.joml.Vector3f p1 (:position game-object1)
        ^org.joml.Vector3f s1 (:size game-object1)
        ^org.joml.Vector3f p2 (:position game-object2)
        ^org.joml.Vector3f s2 (:size game-object2)]
    (and (and (>= (+ (.x p1) (.x s1)) (.x p2))
              (>= (+ (.x p2) (.x s2)) (.x p1)))
         (and (>= (+ (.y p1) (.y s1)) (.y p2))
              (>= (+ (.y p2) (.y s2)) (.y p1))))))

(def ball-center (new Vector3f))
(def ^org.joml.Vector3f brick-half-size (new Vector3f))
(def brick-center (new Vector3f))
(def center-distance (new Vector3f))
(def clamped (new Vector3f))

(defn clamp
  [^org.joml.Vector3f value
   ^org.joml.Vector3f clamp-value
   ^org.joml.Vector3f dest]
  (.set dest
        (org.joml.Math/clamp (float (- (.x clamp-value))) (.x clamp-value) (.x value))
        (org.joml.Math/clamp (float (- (.y clamp-value))) (.y clamp-value) (.y value))
        (org.joml.Math/clamp (float (- (.z clamp-value))) (.z clamp-value) (.z value))))

(defn ball-collision?
  [ball brick]
  (let [^org.joml.Vector3f ball-pos (:position ball)
        ^org.joml.Vector3f brick-pos (:position brick)
        ^org.joml.Vector3f brick-size (:size brick)
        radius (float (:radius ball))
        ball-center (.add ball-pos radius radius (float 0) ball-center)
        brick-half-size (.div brick-size (float 2) brick-half-size)
        brick-center (.add brick-pos brick-half-size brick-center)
        center-distance (.sub ball-center brick-center center-distance)
        clamped (clamp center-distance brick-half-size clamped)
        closest (.add brick-center clamped)
        distance (.sub ball-center closest)]
    {:collision? (< (.length distance) radius)
     :distance distance}))

(def directions
  {:north (vector3f 0 1 0)
   :east (vector3f 1 0 0)
   :south (vector3f 0 -1 0)
   :west (vector3f -1 0 0)})

(def ^org.joml.Vector3f dir-vec-temp (new Vector3f))

(defn vector-direction
  [^org.joml.Vector3f dir-vec]
  (let [^org.joml.Vector3f dir-vec (.normalize dir-vec dir-vec-temp)]
    (:direction
      (reduce (fn [previous dir-key]
                (let [dot-product (.dot dir-vec (get directions dir-key))]
                  (if (> dot-product (:value previous))
                    {:direction dir-key :value dot-product}
                    previous)))
              {:direction nil :value -1}
              (keys directions)))))
