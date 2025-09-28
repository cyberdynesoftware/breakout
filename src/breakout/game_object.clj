(ns breakout.game-object
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defrecord game-object [position color])

(defn vector3f
  [x y z]
  (new Vector3f (float x) (float y) (float z)))
