(ns breakout.game
  (:require [breakout.resource-manager :as rm]
            [util.shader :as shader]
            [util.sprite :as sprite])
  (:import [org.joml Matrix4f Vector3f]
           [org.lwjgl.opengl GL33]))

(defn init
  [width height]
  (let [resources (rm/init)
        projection (doto (new Matrix4f)
                     (.ortho (float 0) (float width) (float height) (float 0) (float -1) (float 1)))
        sprite (sprite/init (:face resources) (:sprite-shader resources))]
    (GL33/glUseProgram (:sprite-shader resources))
    (shader/load-matrix (:sprite-shader resources) "projection" projection)
    (shader/load-int (:sprite-shader resources) "image" 0)
    (assoc resources :sprite sprite)))

(def position (new Vector3f (float 200) (float 200) (float 0)))
(def size (new Vector3f (float 300) (float 400) (float 1)))
(def green (new Vector3f (float 0) (float 1) (float 0)))
(def model (new Matrix4f))

(defn draw
  [resources delta]
  (sprite/transform model position 45 size)
  (sprite/draw (:sprite resources) model green))
