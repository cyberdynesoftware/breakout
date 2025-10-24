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
  (int-array
    [-1 -1 -1
     -1 8 -1
     -1 -1 -1]))

(def blur-kernel 
  (float-array
    (->> [1 2 1
          2 4 2
          1 2 2]
         (mapv #(/ % 16)))))

(defn location
  [shader id]
  (GL33/glGetUniformLocation ^int shader ^String id))

(defn init-shader
  [shader]
  (GL33/glUseProgram shader)
  (shader/load-int shader "scene" 0)
  (GL33/glUniform2fv ^int (location shader "offsets") ^floats offsets)
  (GL33/glUniform1iv ^int (location shader "edge_kernel") ^ints edge-kernel)
  (GL33/glUniform1fv ^int (location shader "blur_kernel") ^floats blur-kernel))

(defn gen-texture
  [width height]
  (let [tex (GL33/glGenTextures)]
    (GL33/glBindTexture GL33/GL_TEXTURE_2D tex)
    (GL33/glTexImage2D GL33/GL_TEXTURE_2D
                       (int 0)
                       GL33/GL_RGB 
                       ^int width
                       ^int height
                       (int 0)
                       GL33/GL_RGB 
                       GL33/GL_UNSIGNED_BYTE
                       0)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_S GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_T GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MIN_FILTER GL33/GL_LINEAR)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MAG_FILTER GL33/GL_LINEAR)
    tex))

(defn init-framebuffer
  [width height]
  (let [fb {:msvbo (GL33/glGenFramebuffers)
            :vbo (GL33/glGenFramebuffers)
            :rbo (GL33/glGenRenderbuffers)
            :texture (gen-texture width height)}]
    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER (:msvbo fb))
    (GL33/glBindRenderbuffer GL33/GL_RENDERBUFFER (:rbo fb))

    (GL33/glRenderbufferStorageMultisample GL33/GL_RENDERBUFFER 4 GL33/GL_RGB width height)
    (GL33/glFramebufferRenderbuffer GL33/GL_FRAMEBUFFER GL33/GL_COLOR_ATTACHMENT0 GL33/GL_RENDERBUFFER (:rbo fb))

    (when (not= (GL33/glCheckFramebufferStatus GL33/GL_FRAMEBUFFER) GL33/GL_FRAMEBUFFER_COMPLETE)
      (println "ERROR: fx: Failed to init msvbo"))

    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER (:vbo fb))
    (GL33/glFramebufferTexture2D GL33/GL_FRAMEBUFFER GL33/GL_COLOR_ATTACHMENT0 GL33/GL_TEXTURE_2D (:texture fb) 0)

    (when (not= (GL33/glCheckFramebufferStatus GL33/GL_FRAMEBUFFER) GL33/GL_FRAMEBUFFER_COMPLETE)
      (println "ERROR: fx: Failed to init vbo"))

    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER 0)
    fb))
