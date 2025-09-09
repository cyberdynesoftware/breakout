(ns breakout.game
  (:require [breakout.resource-manager :as rm]
            [breakout.world :as world]
            [util.shader :as shader]
            [util.sprite :as sprite]
            [util.input :as input])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(defn vector3f
  [x y z]
  (new Vector3f (float x) (float y) (float z)))

(defn init
  [width height]
  (let [resources (rm/init)
        projection (doto (new Matrix4f)
                     (.ortho2D (float 0) (float width) (float height) (float 0)))
        vertices (sprite/vertices)]
    (GL33/glUseProgram (:sprite-shader resources))
    (shader/load-matrix (:sprite-shader resources) "projection" projection)
    (shader/load-int (:sprite-shader resources) "image" 0)
    {:resources (assoc resources :vertices vertices)
     :background {:position (vector3f 0 0 0)
                  :size (vector3f width height 1)
                  :color (vector3f 1 1 1)}
     :world (world/init (:standard-lvl resources) width height)
     :paddle {:position (vector3f (- (/ width 2) 50)
                                  (- height 20) 0)
              :size (vector3f 100 20 1)
              :color (vector3f 1 1 1)}
     :ball {:radius 12.5
            :stuck (atom false)
            :position (vector3f (- (/ width 2) 12.5)
                                (- height 20 25) 0)
            :size (vector3f 25 25 1)
            :color (vector3f 1 1 1)}}))

(defn update-game
  [game delta]
  (let [velocity (float (* 500 delta))
        paddle (get-in game [:paddle :position])
        right-limit (float (- (get-in game [:world :size :width]) (.x (get-in game [:paddle :size]))))]
    (when (:move-left @input/controls)
      (.sub paddle velocity (float 0) (float 0))
      (when (< (.x paddle) 0)
        (.set paddle (float 0) (.y paddle) (.z paddle))))
    (when (:move-right @input/controls)
      (.add paddle velocity (float 0) (float 0))
      (when (> (.x paddle) right-limit)
        (.set paddle right-limit (.y paddle) (.z paddle))))))

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
