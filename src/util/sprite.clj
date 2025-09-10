(ns util.sprite
  (:require [util.shader :as shader])
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl BufferUtils]
           [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(def ^java.nio.FloatBuffer vertex-buffer
  (doto (BufferUtils/createFloatBuffer 24)
    (.put (float-array [0 1 0 1]))
    (.put (float-array [1 0 1 0]))
    (.put (float-array [0 0 0 0]))
    (.put (float-array [0 1 0 1]))
    (.put (float-array [1 1 1 1]))
    (.put (float-array [1 0 1 0]))
    (.flip)))

(defn vertices
  []
  (let [vao (GL33/glGenVertexArrays)
        vbo (GL33/glGenBuffers)]
    (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER vertex-buffer GL33/GL_STATIC_DRAW)
    ;(.rewind vertex-buffer)

    (GL33/glBindVertexArray vao)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 0 4 GL33/GL_FLOAT false 16 0)
    vao))

(def ^org.joml.Vector3f vec3 (new Vector3f (float 0) (float 0) (float 0)))
(def ^org.joml.Vector3f axis (new Vector3f (float 0) (float 0) (float 1)))

(defn rotate-around
  [^org.joml.Matrix4f model rotate x y]
  (let [origin (.set vec3 (float (* x 0.5)) (float (* y 0.5)) (float 0))]
    (.translate model origin)
    (.rotate model (org.joml.Math/toRadians (float rotate)) axis)
    (.translate model (.mul origin (float -1)))))

(defn transform
  ([^org.joml.Matrix4f model position ^org.joml.Vector3f size]
   (.translation model position)
   (.scale model size))
  ([^org.joml.Matrix4f model position ^org.joml.Vector3f size rotate]
   (.translation model position)
   (rotate-around model rotate (.x size) (.y size))
   (.scale model size)))
