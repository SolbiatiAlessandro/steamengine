(ns steamengine.physics-test
   ( :require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.physics :as p]
    ))

(deftest test-diffusion-neighbours-no-boundaries
  (is (= (set [[5 6] [5 4] [4 5] [6 5]]) (set (p/neighbours [5 5]))))
  (is (= (set [[2 6] [2 4] [1 5] [3 5]]) (set (p/neighbours [2 5]))))
  (is (= (set [[2 6 8] [2 4 8] [1 5 8] [3 5 8] [2 5 7] [2 5 9]]) (set (p/neighbours [2 5 8]))))
)

(deftest test-diffusion-neighbours-boundaries
  (is (= (set [[0 6] [0 4] [1 5]]) (set (p/neighbours [0 5]))))
  ;;(is (= (set [[2 6] [2 4] [1 5] [3 5]]) (set (p/neighbours [2 5]))))
  ;;(is (= (set [[2 6 8] [2 4 8] [1 5 8] [3 5 8] [2 5 7] [2 5 9]]) (set (p/neighbours [2 5 8]))))
)

