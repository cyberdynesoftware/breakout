(ns breakout.resource-manager
  (:require [util.shader :as shader]
            [util.texture :as tex]))

(set! *warn-on-reflection* true)

(defn init
  []
  {:sprite-shader (shader/create-shader-program (slurp "resources/shader/sprite.vert")
                                                (slurp "resources/shader/sprite.frag"))
   :face (tex/load-image "resources/assets/awesomeface.png")
   :background (tex/load-image "resources/assets/background.jpg")
   :brick (tex/load-image "resources/assets/block.png")
   :solid-brick (tex/load-image "resources/assets/block_solid.png")
   :paddle (tex/load-image "resources/assets/paddle.png")
   :standard-lvl (slurp "resources/levels/standard.lvl")})
