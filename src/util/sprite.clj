(ns util.sprite
  (:require [util.shader :as shader])
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl BufferUtils]
           [org.joml Vector3f]))

(def vertex-buffer
  (doto (BufferUtils/createFloatBuffer 24)
    (.put (float-array [0 1 0 1]))
    (.put (float-array [1 0 1 0]))
    (.put (float-array [0 0 0 0]))
    (.put (float-array [0 1 0 1]))
    (.put (float-array [1 1 1 1]))
    (.put (float-array [1 0 1 0]))
    (.flip)))

(defn init
  [texture shader]
  (let [vao (GL33/glGenVertexArrays)
        vbo (GL33/glGenBuffers)]
    (GL33/glBindBuffer GL33/GL_ARRAY_BUFFER vbo)
    (GL33/glBufferData GL33/GL_ARRAY_BUFFER vertex-buffer GL33/GL_STATIC_DRAW)
    ;(.rewind vertex-buffer)

    (GL33/glBindVertexArray vao)
    (GL33/glEnableVertexAttribArray 0)
    (GL33/glVertexAttribPointer 0 4 GL33/GL_FLOAT false 16 0)
    {:vao vao
     :texture texture
     :shader shader}))

(def vec3 (new Vector3f (float 0) (float 0) (float 0)))
(def axis (new Vector3f (float 0) (float 0) (float 1)))

(defn rotate-around
  [model rotate x y]
  (let [origin (.set vec3 (float (* x 0.5)) (float (* y 0.5)) (float 0))]
    (.translate model origin)
    (.rotate model (org.joml.Math/toRadians (float rotate)) axis)
    (.translate model (.mul origin (float -1)))))

(defn transform
  [model position rotate size]
  (.translation model position)
  (rotate-around model rotate (.x size) (.y size))
  ;(.translate model (.set vec3 (float (* (.x size) 0.5)) (float (* (.y size) 0.5)) (float 0)))
  ;(.rotate model (org.joml.Math/toRadians (float rotate)) axis)
  ;(.translate model (.set vec3 (float (* (.x size) -0.5)) (float (* (.y size) -0.5)) (float 0)))
  (.scale model size))

(defn draw
  [sprite model color]
  (let [shader (:shader sprite)]
    (GL33/glUseProgram shader)
    (shader/load-matrix shader "model" model)
    (shader/load-vector3 shader "color" color)

    (GL33/glActiveTexture GL33/GL_TEXTURE0)
    (GL33/glBindTexture GL33/GL_TEXTURE_2D (:texture sprite))

    (GL33/glBindVertexArray (:vao sprite))
    (GL33/glDrawArrays GL33/GL_TRIANGLES 0 6)))
