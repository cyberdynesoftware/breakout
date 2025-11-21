(ns breakout.resource-manager
  (:require [util.shader :as shader]
            [util.texture :as tex]))

(set! *warn-on-reflection* true)

(defn init
  []
  {:sprite-shader (shader/create-shader-program (slurp "resources/shader/sprite.vert")
                                                (slurp "resources/shader/sprite.frag"))
   :particle-shader (shader/create-shader-program (slurp "resources/shader/particle.vert")
                                                  (slurp "resources/shader/particle.frag"))
   :fx-shader (shader/create-shader-program (slurp "resources/shader/fx.vert")
                                            (slurp "resources/shader/fx.frag"))
   :face (tex/load-image "resources/assets/awesomeface.png")
   :background (tex/load-image "resources/assets/background.jpg")
   :brick (tex/load-image "resources/assets/block.png")
   :solid-brick (tex/load-image "resources/assets/block_solid.png")
   :paddle (tex/load-image "resources/assets/paddle.png")
   :particle (tex/load-image "resources/assets/particle.png")
   :powerup-speed (tex/load-image "resources/assets/powerup_speed.png")
   :powerup-sticky (tex/load-image "resources/assets/powerup_sticky.png")
   :powerup-passthrough (tex/load-image "resources/assets/powerup_passthrough.png")
   :powerup-increase (tex/load-image "resources/assets/powerup_increase.png")
   :powerup-confuse (tex/load-image "resources/assets/powerup_confuse.png")
   :powerup-chaos (tex/load-image "resources/assets/powerup_chaos.png")
   :standard-lvl (slurp "resources/levels/standard.lvl")})
