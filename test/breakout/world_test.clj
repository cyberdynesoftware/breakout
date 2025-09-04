(ns breakout.world-test
  (:require [clojure.test :refer :all]
            [breakout.world :refer :all]))

(deftest world-test
  (testing "dimen"
    (is (= (dimen "") {:rows 1 :columns 0}) "empty string")
    (is (= (dimen "0") {:rows 1 :columns 1}))
    (is (= (dimen "0 1") {:rows 1 :columns 2}))
    (is (= (dimen "0 \n 2") {:rows 2 :columns 1})))
  (testing "world initialization"
    (is (= (init "" 0 0) {:bricks [] :solid-bricks []}) "empty string")
    (is (= (init "0" 0 0) {:bricks [] :solid-bricks []}) "0 string")
    (is (= (count (:solid-bricks (init "1" 10 10))) 1) "1 string")
    (is (= (count (:bricks (init "2 3" 10 10))) 2))
    (is (= (init "4" 10 10) {:solid-bricks []
                             :bricks [{:position (vector3f 0 0 0)
                                       :size (vector3f 10 10 1)
                                       :color (vector3f 0.8 0.8 0.4)}]}))))
