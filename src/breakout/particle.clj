(ns breakout.particle
  (:require [breakout.game-object :as game-object])
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defn create-particle
  [ball offset]
  (let [random (- (rand 10) 5)
        rcolor (+ (rand) 0.5)
        value (+ (/ (:radius ball) 2) random)
        ^org.joml.Vector3f offset (game-object/vector3f value value 0)
        velocity (new Vector3f ^org.joml.Vector3f (:velocity ball))]
    (-> (game-object/map->game-object {:position (.add offset (:position ball))
                                       :color (game-object/vector3f rcolor rcolor rcolor)})
        (assoc :velocity (.mul velocity (float 0.1)))
        (assoc :life 1))))
