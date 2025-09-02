(ns breakout.resource-manager
  (:require [util.shader :as shader]
            [util.texture :as tex]))

(defn init
  []
  {:sprite-shader (shader/create-shader-program (slurp "resources/shader/sprite.vert")
                                                (slurp "resources/shader/sprite.frag"))
   :face (tex/load-image "resources/assets/awesomeface.png")
   :background (tex/load-image "resources/assets/background.jpg")})
