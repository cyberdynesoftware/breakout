(ns breakout.game
  (:require [breakout.resource-manager :as rm]
            [breakout.world :as world]
            [util.shader :as shader]
            [util.sprite :as sprite]
            [breakout.input :as input])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(set! *warn-on-reflection* true)

(defn vector3f
  [x y z]
  (new Vector3f (float x) (float y) (float z)))

(defn init
  [width height]
  (let [resources (rm/init)
        projection (doto (new Matrix4f)
                     (.ortho2D (float 0) (float width) (float height) (float 0)))
        vertices (sprite/vertices)]
    (reset! input/controls input/default)
    (GL33/glUseProgram (:sprite-shader resources))
    (shader/load-matrix (:sprite-shader resources) "projection" projection)
    (shader/load-int (:sprite-shader resources) "image" 0)
    {:resources (assoc resources :vertices vertices)
     :background {:position (vector3f 0 0 0)
                  :size (vector3f width height 1)
                  :color (vector3f 1 1 1)}
     :world (world/init (:standard-lvl resources) width height)
     :paddle {:position (vector3f (- (/ width 2) 50) (- height 20) 0)
              :size (vector3f 100 20 1)
              :color (vector3f 1 1 1)}
     :ball {:radius 12.5
            :velocity (vector3f 100 -350 0)
            :position (vector3f (- (/ width 2) 12.5) (- height 20 25) 0)
            :size (vector3f 25 25 1)
            :color (vector3f 1 1 1)}}))

(defn move-paddle
  [paddle delta right-limit]
  (let [velocity (float (* 500 delta))
        ^org.joml.Vector3f position (:position paddle)]
    (when (:move-left @input/controls)
      (.sub position velocity (float 0) (float 0))
      (when (< (.x position) 0)
        (.setComponent position (int 0) (float 0))))
    (when (:move-right @input/controls)
      (.add position velocity (float 0) (float 0))
      (when (> (.x position) right-limit)
        (.setComponent position (int 0) right-limit)))))

(def ^org.joml.Vector3f speed (vector3f 0 0 0))

(defn move-ball
  [ball delta world-width]
  (let [right-limit (float (- world-width (* (:radius ball) 2)))
        ^org.joml.Vector3f position (:position ball)
        ^org.joml.Vector3f velocity (:velocity ball)]
    (.mul velocity (float delta) speed)
    (.add position speed)

    (when (< (.x position) 0)
      (.setComponent position (int 0) (float 0))
      (.setComponent velocity (int 0) (float (- (.x velocity)))))

    (when (> (.x position) right-limit)
      (.setComponent position (int 0) right-limit)
      (.setComponent velocity (int 0) (float (- (.x velocity)))))

    (when (< (.y position) 0)
      (.setComponent position (int 1) (float 0))
      (.setComponent velocity (int 1) (float (- (.y velocity)))))))

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

(defn check-collisions
  [game]
  (update-in game [:world :bricks]
             (fn [bricks] (filter #(not (aabb-collision? (:ball game) %)) bricks))))

(defn update-game
  [game delta]
  (let [paddle-width (.x ^org.joml.Vector3f (get-in game [:paddle :size]))
        world-width (get-in game [:world :size :width])
        ball (:ball game)]
    (move-paddle (:paddle game)
                 delta
                 (float (- world-width paddle-width)))
    (if (:ball-stuck @input/controls)
      (.setComponent ^org.joml.Vector3f (:position ball)
                     (int 0)
                     (float (+ (.x ^org.joml.Vector3f (get-in game [:paddle :position])) (- (/ paddle-width 2) (:radius ball)))))
      (move-ball ball delta world-width)))
  (check-collisions game))

(def model (new Matrix4f))

(defn draw-game-object
  [obj shader]
  (sprite/transform model (:position obj) (:size obj))
  (shader/load-matrix shader "model" model)

  (shader/load-vector3 shader "spriteColor" (:color obj))
  (GL33/glDrawArrays GL33/GL_TRIANGLES 0 6))

(defn draw
  [game delta]
  (let [shader (get-in game [:resources :sprite-shader])]
    (GL33/glUseProgram shader)
    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (GL33/glBindVertexArray (get-in game [:resources :vertices]))

    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :background]))
    (draw-game-object (:background game) shader)

    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :solid-brick]))
    (doseq [brick (get-in game [:world :solid-bricks])]
      (draw-game-object brick shader))

    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :brick]))
    (doseq [brick (get-in game [:world :bricks])]
      (draw-game-object brick shader))

    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :paddle]))
    (draw-game-object (:paddle game) shader)

    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :face]))
    (draw-game-object (:ball game) shader)))
