(ns breakout.core
  (:require [util.window :as window]
            [breakout.game :as game])
  (:gen-class))

(set! *warn-on-reflection* true)

(def num-stack-trace 8)

(defn -main
  [& args]
  (let [context (window/create "Breakout" 800 600)
        resources (game/init 800 600)]
    (try (window/game-loop context resources)
         (catch Exception e
           (println " = EXECUTION ERROR =")
           (println (.toString e))
           (doseq [^java.lang.StackTraceElement stack-trace-element (take num-stack-trace (.getStackTrace e))]
             (println (format " - %s: %s (%s:%d)"
                              (.getClassName stack-trace-element)
                              (.getMethodName stack-trace-element)
                              (.getFileName stack-trace-element)
                              (.getLineNumber stack-trace-element)))))
         (finally (window/destroy)))))
