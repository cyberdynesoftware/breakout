(ns breakout.core
  (:require [util.window :as window])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main
  [& args]
  (let [context (window/create "Breakout" 800 600)]
    (window/game-loop context)
    (window/destroy)))
