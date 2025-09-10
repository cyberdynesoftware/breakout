(ns breakout.input
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallbackI]))

(def default {:move-left false
              :move-right false
              :ball-stuck true})

(def controls (atom nil))

(def key-callback (reify GLFWKeyCallbackI
                    (invoke [_ window keycode _ action _]
                      (when (= action GLFW/GLFW_PRESS)
                        (condp = keycode
                          GLFW/GLFW_KEY_ESCAPE (GLFW/glfwSetWindowShouldClose window true)
                          GLFW/GLFW_KEY_A (reset! controls (assoc @controls :move-left true))
                          GLFW/GLFW_KEY_D (reset! controls (assoc @controls :move-right true))
                          GLFW/GLFW_KEY_SPACE (reset! controls (assoc @controls :ball-stuck false))
                          nil))
                      (when (= action GLFW/GLFW_RELEASE)
                        (condp = keycode
                          GLFW/GLFW_KEY_A (reset! controls (assoc @controls :move-left false))
                          GLFW/GLFW_KEY_D (reset! controls (assoc @controls :move-right false))
                          nil)))))
