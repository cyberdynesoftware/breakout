(ns breakout.core
  (:require [util.window :as window]
            [breakout.game :as game])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main
  [& args]
  (let [context (window/create "Breakout" 800 600)
        resources (game/init 800 600)]
    (window/game-loop context resources)
    (window/destroy)))
