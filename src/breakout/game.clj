(ns breakout.game
  (:require [breakout.resource-manager :as rm]
            [breakout.world :as world]
            [breakout.collision :as collision]
            [breakout.particle :as emitter]
            [breakout.game-object :refer :all]
            [util.shader :as shader]
            [util.sprite :as sprite]
            [breakout.input :as input]
            [breakout.fx :as fx])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(set! *warn-on-reflection* true)

(defn reset
  [game width height]
  (reset! input/controls input/default)
  (-> game
      (assoc :ball (-> (map->game-object {:position (vector3f (- (/ width 2) 12.5) (- height 20 25) 0)
                                          :size (vector3f 25 25 1)
                                          :color (vector3f 1 1 1)})
                       (assoc :radius 12.5)
                       (assoc :velocity (vector3f 100 -350 0))))
      (assoc :paddle (map->game-object {:position (vector3f (- (/ width 2) 50) (- height 20) 0)
                                        :size (vector3f 100 20 1)
                                        :color (vector3f 1 1 1)}))
      (assoc :world (world/init (get-in game [:resources :standard-lvl]) width height))))

(defn init-shader
  [shader projection image]
  (GL33/glUseProgram shader)
  (shader/load-matrix shader "projection" projection)
  (shader/load-int shader "image" image))

(defn init
  [width height]
  (let [resources (rm/init)
        projection (doto (new Matrix4f)
                     (.ortho2D (float 0) (float width) (float height) (float 0)))
        game {:resources (assoc resources :vertices (sprite/vertices))
              :background (map->game-object {:position (vector3f 0 0 0)
                                             :size (vector3f width height 1)
                                             :color (vector3f 1 1 1)})
              :particles []
              :framebuffer (fx/init-framebuffer width height)}]
    (init-shader (:sprite-shader resources) projection 0)
    (init-shader (:particle-shader resources) projection 0)
    (fx/init-shader (:fx-shader resources))
    (reset game width height)))

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

(defn update-component
  [^org.joml.Vector3f value
   component
   op]
  (.setComponent value component (float (op (.get value ^int component)))))

(defn penetration
  [^org.joml.Vector3f value
   component
   radius]
  (- radius (java.lang.Math/abs (.get value ^int component))))

(defn collision-resolution
  [ball distance]
  (let [direction (collision/vector-direction distance)
        params (condp = direction
                 :north {:component 1 :op +}
                 :south {:component 1 :op -}
                 :west {:component 0 :op +}
                 :east {:component 0 :op -}
                 (println "WARNING: distance is 0x0"))]
    (when params
      (update-component (:velocity ball) (:component params) -))))

(defn resolve-paddle-collision
  [ball paddle]
  (let [^org.joml.Vector3f paddle-pos (:position paddle)
        ^org.joml.Vector3f paddle-size (:size paddle)
        ^org.joml.Vector3f ball-pos (:position ball)
        ^org.joml.Vector3f ball-velocity (:velocity ball)
        paddle-center (+ (.x paddle-pos) (/ (.x paddle-size) 2))
        distance (- (+ (.x ball-pos) (:radius ball)) paddle-center)
        percentage (/ distance (/ (.x paddle-size) 2))
        speed (.length ball-velocity)]
    (.setComponent ball-velocity (int 0) (float (* percentage 200)))
    (.setComponent ball-velocity (int 1) (float (- (java.lang.Math/abs (.get ball-velocity (int 1))))))
    (.normalize ball-velocity)
    (.mul ball-velocity speed)))

(defn check-game-over
  [game]
  (let [^org.joml.Vector3f ball-pos (get-in game [:ball :position])
        size (get-in game [:world :size])]
    (if (>= (.y ball-pos) (:height size))
      (reset game (:width size) (:height size))
      game)))

(defn check-collision
  [game]
  (let [ball (:ball game)]
    (doseq [brick (get-in game [:world :solid-bricks])]
      (let [result (collision/ball-collision? ball brick)]
        (when (:collision? result)
          (collision-resolution ball (:distance result)))))

    (let [result (collision/ball-collision? ball (:paddle game))]
      (when (:collision? result)
        (resolve-paddle-collision ball (:paddle game))))

    (-> game
        (update-in [:world :bricks]
                   (fn [bricks]
                     (filter #(let [result (collision/ball-collision? ball %)]
                                (when (:collision? result)
                                  (collision-resolution ball (:distance result)))
                                (not (:collision? result)))
                             bricks)))
        (check-game-over))))

(defn update-particles
  [particles ball delta]
  (-> particles
      (conj (emitter/create-particle ball))
      (conj (emitter/create-particle ball))
      (emitter/update-particles delta)))

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
                     (float (+ (.x ^org.joml.Vector3f (get-in game [:paddle :position]))
                               (- (/ paddle-width 2) (:radius ball)))))
      (move-ball ball delta world-width))
    (-> game
        (update :particles update-particles ball delta)
        (check-collision))))

(def model (new Matrix4f))

(defn draw-game-object
  [obj shader]
  (sprite/transform model (:position obj) (:size obj))
  (shader/load-matrix shader "model" model)
  (shader/load-vector3 shader "spriteColor" (:color obj))
  (GL33/glDrawArrays GL33/GL_TRIANGLES 0 6))

(defn draw-particle
  [particle shader]
  (shader/load-vector3 shader "offset" (:position particle))
  (shader/load-vector3 shader "color" (:color particle))
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

    (GL33/glUseProgram (get-in game [:resources :particle-shader]))
    (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :particle]))
    (doseq [particle (:particles game)]
      (draw-particle particle (get-in game [:resources :particle-shader])))
    (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)

    (GL33/glUseProgram shader)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (get-in game [:resources :face]))
    (draw-game-object (:ball game) shader)))
