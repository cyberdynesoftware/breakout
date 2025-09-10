(ns util.window
  (:require [util.error :as error]
            [breakout.input :as input]
            [breakout.game :as game])
  (:import [org.lwjgl.glfw GLFW GLFWFramebufferSizeCallbackI]
           [org.lwjgl.opengl GL GL33]
           [org.lwjgl.system MemoryUtil])
  (:gen-class))

(set! *warn-on-reflection* true)

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

  (let [window (GLFW/glfwCreateWindow ^int width ^int height ^String title MemoryUtil/NULL MemoryUtil/NULL)]
    (GLFW/glfwMakeContextCurrent window)
    (GL/createCapabilities)
    (println (format "OpenGL version: %s (%s)" (GL33/glGetString GL33/GL_VERSION) (GL33/glGetString GL33/GL_VENDOR)))

    (GLFW/glfwSetKeyCallback window input/key-callback)
    (GLFW/glfwSetFramebufferSizeCallback window window-resize-callback)
    
    (GL33/glEnable GL33/GL_BLEND)
    (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)
    window))

(defn game-loop
  [window game]
  (let [last-frame (volatile! (GLFW/glfwGetTime))]
    (while (not (GLFW/glfwWindowShouldClose window))
      (let [now (GLFW/glfwGetTime)
            delta (- now @last-frame)]
        (vreset! last-frame now)
        (GLFW/glfwPollEvents)

        (GL33/glClearColor (float 0) (float 0) (float 0) (float 1))
        (GL33/glClear GL33/GL_COLOR_BUFFER_BIT)

        (game/update-game game delta)
        (game/draw game delta)

        (error/check-error)

        (GLFW/glfwSwapBuffers window)))))

(defn destroy
  []
  (GLFW/glfwTerminate))
