(ns breakout.fx
  (:require [util.shader :as shader]
            [util.sprite :as sprite])
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl BufferUtils]))

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

(def ^java.nio.FloatBuffer vertex-buffer
  (doto (BufferUtils/createFloatBuffer 24)
    (.put (float-array [-1 -1 0 0]))
    (.put (float-array [1 1 1 1]))
    (.put (float-array [-1 1 0 1]))
    (.put (float-array [-1 -1 0 0]))
    (.put (float-array [1 -1 1 0]))
    (.put (float-array [1 1 1 1]))
    (.flip)))

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
  (let [fb {:dimen {:width width :height height}
            :msfbo (GL33/glGenFramebuffers)
            :fbo (GL33/glGenFramebuffers)
            :rbo (GL33/glGenRenderbuffers)
            :vao (sprite/init-quad vertex-buffer)
            :texture (gen-texture width height)}]
    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER (:msfbo fb))
    (GL33/glBindRenderbuffer GL33/GL_RENDERBUFFER (:rbo fb))

    (GL33/glRenderbufferStorageMultisample GL33/GL_RENDERBUFFER 4 GL33/GL_RGB width height)
    (GL33/glFramebufferRenderbuffer GL33/GL_FRAMEBUFFER GL33/GL_COLOR_ATTACHMENT0 GL33/GL_RENDERBUFFER (:rbo fb))

    (when (not= (GL33/glCheckFramebufferStatus GL33/GL_FRAMEBUFFER) GL33/GL_FRAMEBUFFER_COMPLETE)
      (println "ERROR: fx: Failed to init msfbo"))

    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER (:fbo fb))
    (GL33/glFramebufferTexture2D GL33/GL_FRAMEBUFFER GL33/GL_COLOR_ATTACHMENT0 GL33/GL_TEXTURE_2D (:texture fb) 0)

    (when (not= (GL33/glCheckFramebufferStatus GL33/GL_FRAMEBUFFER) GL33/GL_FRAMEBUFFER_COMPLETE)
      (println "ERROR: fx: Failed to init fbo"))

    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER 0)
    fb))

(defn begin-render
  [fb]
  (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER (:msfbo fb))
  (GL33/glClearColor (float 0) (float 0) (float 0) (float 1))
  (GL33/glClear GL33/GL_COLOR_BUFFER_BIT))

(defn end-render
  [fb]
  (let [width (get-in fb [:dimen :width])
        height (get-in fb [:dimen :height])]
    (GL33/glBindFramebuffer GL33/GL_READ_FRAMEBUFFER (:msfbo fb))
    (GL33/glBindFramebuffer GL33/GL_DRAW_FRAMEBUFFER (:fbo fb))
    (GL33/glBlitFramebuffer 0 0 width height 0 0 width height GL33/GL_COLOR_BUFFER_BIT GL33/GL_NEAREST)
    (GL33/glBindFramebuffer GL33/GL_FRAMEBUFFER 0)))

(defn render
  [shader fb delta effects]
  (GL33/glUseProgram shader)
  (shader/load-float1 shader "time" delta)
  (shader/load-int shader "confuse" (:confuse effects))
  (shader/load-int shader "chaos" (:chaos effects))
  (shader/load-int shader "shake" (if (> (:shake effects) 0) 1 0))

  (GL33/glActiveTexture GL33/GL_TEXTURE0)
  (GL33/glBindTexture GL33/GL_TEXTURE_2D (:texture fb))
  (GL33/glBindVertexArray (:vao fb))
  (GL33/glDrawArrays GL33/GL_TRIANGLES 0 6)
  (GL33/glBindVertexArray 0))
