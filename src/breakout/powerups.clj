(ns breakout.powerups
  (:require [breakout.game-object :refer :all])
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defn spawn-powerup
  [block resources]
  (let [^Vector3f block-position (:position block)]
    (condp = (rand-int 12)
      0 {:type :speed
         :color [0.5 0.5 1]
         :active 0
         :duration 0
         :position (new Vector3f block-position)
         :texture (:powerup-speed resources)}
      1 {:type :sticky
         :color [1 0.5 1]
         :active 0
         :duration 20
         :position (new Vector3f block-position)
         :texture (:powerup-sticky resources)}
      2 {:type :passthrough
         :color [0.5 1 0.5]
         :active 0
         :duration 10
         :position (new Vector3f block-position)
         :texture (:powerup-passthrough resources)}
      3 {:type :increase
         :color [1 0.6 0.4]
         :active 0
         :duration 0
         :position (new Vector3f block-position)
         :texture (:powerup-increase resources)}
      4 {:type :confuse
         :color [1 0.3 0.3]
         :active 0
         :duration 15
         :position (new Vector3f block-position)
         :texture (:powerup-confuse resources)}
      5 {:type :chaos
         :color [0.9 0.25 0.25]
         :active 0
         :duration 15
         :position (new Vector3f block-position)
         :texture (:powerup-chaos resources)}
      nil)))

(def ^Vector3f velocity (vector3f 0 150 0))
(def ^Vector3f temp-vel (vector3f 0 0 0))

(defn update-powerups
  [powerups delta]
  (doseq [powerup powerups]
    (let [^Vector3f position (:position powerup)]
      (when (:active powerup)
        (- :duration delta))
      (.add position (.mul velocity (float delta) temp-vel)))))
