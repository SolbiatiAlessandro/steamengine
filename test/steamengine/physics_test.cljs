(ns steamengine.physics-test
   ( :require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.physics :as p]
    ))

(deftest test-diffusion-neighbours
  (is (= (set [[5 6] [5 4] [4 5] [6 5]]) (set (p/neighbours [5 5]))))
  (is (= (set [[2 6] [2 4] [1 5] [3 5]]) (set (p/neighbours [2 5]))))
  (is (= (set [[2 6 8] [2 4 8] [1 5 8] [3 5 8] [2 5 7] [2 5 9]]) (set (p/neighbours [2 5 8]))))
)

(deftest test-diffusion-neighbours-wall
  (is (= false (p/neighbour-wall [5 2 6] 6)))
  (is (= false (p/neighbour-wall [5 -1 6] 6)))
  (is (= true (p/neighbour-wall [5 3 2] 6)))
  (is (= true (p/neighbour-wall [5 0] 6)))
  )

(deftest test-diffusion-neighbours-bounded
  (is (= (set [[0 6] [0 4] [1 5]]) (set (p/neighbours-bounded [0 5] 20))))
  (is (= (set [[19 6] [19 4] [18 5]]) (set (p/neighbours-bounded [19 5] 20))))
  (is (= (set [[1 19] [0 18]]) (set (p/neighbours-bounded [0 19] 20))))
  (is (= (set [[1 19 19] [0 18 19] [0 19 18]]) (set (p/neighbours-bounded [0 19 19] 20))))
)

