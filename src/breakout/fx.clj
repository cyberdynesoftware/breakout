(ns breakout.fx
  (:require [util.shader :as shader])
  (:import [org.lwjgl.opengl GL33]))

(set! *warn-on-reflection* true)

(def offsets
  (float-array
    (let [o (/ 1 300)]
      [(- o) o
       0 o
       o o
       (- o) 0
       0 0
       o 0
       (- o) (- o)
       0 (- o)
       o (- o)])))

(def edge-kernel
  (mapv int
        [-1 -1 -1
         -1 8 -1
         -1 -1 -1]))

(def blur-kernel
  (->> [1 2 1
        2 4 2
        1 2 2]
       (mapv #(/ % 16))
       (mapv float)))

(defn location
  [shader id]
  (GL33/glGetUniformLocation ^int shader ^String id))

(defn init-shader
  [shader]
  (GL33/glUniform2fv ^int (location shader "offsets") ^float[] offsets)
  (GL33/glUniform1iv ^int (location shader "edge_kernel") ^int [] edge-kernel)
  (GL33/glUniform1fv ^int (location shader "blur_kernel") ^float [] blur-kernel))
