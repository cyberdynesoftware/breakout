(ns util.window
  (:require [util.error :as error]
            [util.input :as input]
            [breakout.game :as game])
  (:import [org.lwjgl.glfw GLFW GLFWFramebufferSizeCallbackI]
           [org.lwjgl.opengl GL GL33]
           [org.lwjgl.system MemoryUtil])
  (:gen-class))

(def window-resize-callback
  (reify GLFWFramebufferSizeCallbackI
    (invoke [_ _ x y]
      (GL33/glViewport (float 0) (float 0) (float x) (float y)))))

(defn create
  [title width height]
  (GLFW/glfwInit)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MAJOR 3)
  (GLFW/glfwWindowHint GLFW/GLFW_CONTEXT_VERSION_MINOR 3)
  (GLFW/glfwWindowHint GLFW/GLFW_OPENGL_PROFILE GLFW/GLFW_OPENGL_CORE_PROFILE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_FALSE)

  (let [window (GLFW/glfwCreateWindow width height title MemoryUtil/NULL MemoryUtil/NULL)]
    (GLFW/glfwMakeContextCurrent window)
    (GL/createCapabilities)
    (println (format "OpenGL version: %s (%s)" (GL33/glGetString GL33/GL_VERSION) (GL33/glGetString GL33/GL_VENDOR)))

    (GLFW/glfwSetKeyCallback window input/key-callback)
    (GLFW/glfwSetFramebufferSizeCallback window window-resize-callback)
    window))

(defn game-loop
  [window resources]
  (let [last-frame (atom 0)]
    (while (not (GLFW/glfwWindowShouldClose window))
      (let [now (GLFW/glfwGetTime)
            delta (- now @last-frame)]
        (reset! last-frame now)

        (GL33/glClearColor (float 0) (float 0) (float 0) (float 1))
        (GL33/glClear GL33/GL_COLOR_BUFFER_BIT)

        (game/draw resources delta)

        (error/check-error)

        (GLFW/glfwSwapBuffers window)
        (GLFW/glfwPollEvents)))))

(defn destroy
  []
  (GLFW/glfwTerminate))
