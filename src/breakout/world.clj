(ns breakout.world
  (:import [org.joml Vector3f]))

(set! *warn-on-reflection* true)

(defn dimen
  [lvl]
  (let [lines (clojure.string/split-lines lvl)]
    {:rows (count lines)
     :columns (count (filter (fn [c] (some #(= c %)  [\0 \1 \2 \3 \4 \5])) (first lines)))}))

(defn vector3f
  [x y z]
  (new Vector3f (float x) (float y) (float z)))

(defn brick
  [width height dimen x y]
  {:position (vector3f (* (/ width (:columns dimen)) x)
                       (* (/ height (:rows dimen)) y)
                       0)
   :size (vector3f (/ width (:columns dimen))
                   (/ height (:rows dimen))
                   1)})

(defn update-game-objects
  [objs col-key brick color]
  (-> objs
      (update col-key conj (assoc brick :color color))
      (update-in [:temp :x] inc)))

(defn init
  [lvl width height]
  (let [dimen (dimen lvl)]
    (-> (reduce (fn [result item]
                  (let [x (get-in result [:temp :x])
                        y (get-in result [:temp :y])
                        brick (brick width (/ height 2) dimen x y)]
                    (condp = item
                      \newline (assoc result :temp {:x 0 :y (inc y)})
                      \0 (update-in result [:temp :x] inc)
                      \1 (update-game-objects result :solid-bricks brick (vector3f 1 1 1))
                      \2 (update-game-objects result :bricks brick (vector3f 0.2 0.6 1))
                      \3 (update-game-objects result :bricks brick (vector3f 0 0.7 0))
                      \4 (update-game-objects result :bricks brick (vector3f 0.8 0.8 0.4))
                      \5 (update-game-objects result :bricks brick (vector3f 1 0.5 0))
                      result)))
                {:bricks []
                 :solid-bricks []
                 :temp {:x 0 :y 0}}
                lvl)
        (dissoc :temp)
        (assoc :size {:width width :height height}))))
