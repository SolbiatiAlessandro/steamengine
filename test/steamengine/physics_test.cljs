(ns steamengine.physics-test
   ( :require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.physics :as p]
    ))

(deftest test-diffusion-neighbours-no-boundaries
  (is (= [[4] [6]] (p/nd-neighbours [5] 1))) 
  (is (= [[5 4] [5 6]] (p/nd-neighbours [5 5] 2))) 
  (is (= [[5 5 4] [5 5 6]] (p/nd-neighbours [5 5 5] 3))) 
  (is (= [[4 5] [6 5]] (p/d-plus-1 [[4] [6]] 5 )))
  (is (= [[4 5 5] [6 5 5]] (p/d-plus-1 [[4 5] [6 5]] 5 )))
  (is (= [[5 6] [5 4] [4 5] [6 5]] (p/neighbours [5 5])))
  (is (= [[3 6] [3 4] [2 5] [4 5]] (p/neighbours [3 5])))
  (is (= [[5 5 6] [5 5 4] [5 6 5] [5 4 5] [4 5 5] [6 5 5]] (p/neighbours [5 5 5])))
  (is (= [[5 6] [5 4] [4 5] [6 5]] (p/neighbours [5 5])))
  (is (= (* 4 2) (count (p/neighbours [5 5 5 5]))))
)

(deftest test-diffusion-neighbours-boundaries
  ;;(is (= [[1]] (p/nd-neighbours 0 1))) 
  ;;(is (= [[19]] (p/nd-neighbours 20 1 20))) 
  ;;(is (= [ 1] (p/neighbours [0 5])))
  )

