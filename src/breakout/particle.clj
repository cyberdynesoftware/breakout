(ns breakout.particle
  (:require [breakout.game-object :as game-object])
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defn create-particle
  [ball]
  (let [random (- (rand 10) 5)
        rcolor (+ (rand) 0.5)
        value (+ (/ (:radius ball) 2) random)
        ^org.joml.Vector3f offset (game-object/vector3f value value 0)
        velocity (new Vector3f ^org.joml.Vector3f (:velocity ball))]
    (-> (game-object/map->game-object {:position (.add offset (:position ball))
                                       :color (game-object/vector3f rcolor rcolor rcolor)})
        (assoc :velocity (.mul velocity (float 0.1)))
        (assoc :life 1))))

(def ^org.joml.Vector3f temp-vec (new Vector3f))

(defn color-progress
  [^org.joml.Vector3f color
   delta]
  (let [value (float (* delta 2.5))]
    (.sub color (.set temp-vec value value value))))

(defn move-particle
  [^org.joml.Vector3f position
   ^org.joml.Vector3f velocity
   delta]
  (.sub position (.mul velocity (float delta) temp-vec)))

(defn update-particles
  [particles delta]
  (->> particles
       (map (fn [particle] (update particle :life - delta)))
       (filter #(> (:life %) 0))
       (map (fn [particle] (-> particle
                               (update :position move-particle (:velocity particle) delta)
                               (update :color color-progress delta)))))) 
