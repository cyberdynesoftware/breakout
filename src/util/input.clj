(ns util.input
  (:import [org.lwjgl.glfw GLFW GLFWKeyCallbackI]))

(def controls (atom {:move-left false
                     :move-right false}))

(def key-callback (reify GLFWKeyCallbackI
                    (invoke [_ window keycode _ action _]
                      (when (= action GLFW/GLFW_PRESS)
                        (condp = keycode
                          GLFW/GLFW_KEY_ESCAPE (GLFW/glfwSetWindowShouldClose window true)
                          GLFW/GLFW_KEY_A (reset! controls (assoc @controls :move-left true))
                          GLFW/GLFW_KEY_D (reset! controls (assoc @controls :move-right true))
                          nil))
                      (when (= action GLFW/GLFW_RELEASE)
                        (condp = keycode
                          GLFW/GLFW_KEY_A (reset! controls (assoc @controls :move-left false))
                          GLFW/GLFW_KEY_D (reset! controls (assoc @controls :move-right false))
                          nil)))))
