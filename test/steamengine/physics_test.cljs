(ns steamengine.physics-test
   ( :require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.physics :as p]
    [steamengine.game-engine :as ge]
    [clojure.stacktrace :as stacktrace]))

(def test-pressure 10)


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

(deftest test-diffusion-neighbour-diffusion
  (let [current-pressure 10
        test-grid (ge/pressure-grid 4 2)
        test-grid-3d (ge/pressure-grid 4 3)
        test-grid-4d (ge/pressure-grid 4 4)]
    ;; basic test
    (is (< 0 (p/neighbours-diffusion [2 2] test-grid 0.3 current-pressure)))  
    (is (< 0 (p/neighbours-diffusion [2 2] test-grid 0.1 current-pressure)))
    ;; following are tests on the boundary diffusion
    (is (= 4 (count (p/neighbours [0 0]))))
    (is (= 2 (count (p/neighbours-bounded [0 0] (ge/pressure-grid-size test-grid)))))
    (is (> (p/neighbours-diffusion [0 0] test-grid 0.1 current-pressure) (p/neighbours-diffusion [3 2] test-grid 0.1 current-pressure)))
    (is (> (p/neighbours-diffusion [3 3] test-grid 0.1 current-pressure) (p/neighbours-diffusion [3 2] test-grid 0.1 current-pressure)))
    (is (> (p/neighbours-diffusion [3 2] test-grid 0.1 current-pressure) (p/neighbours-diffusion [2 2] test-grid 0.1 current-pressure)))
    ;; more dimensions stronger diffusion
    (is (< 0 (p/neighbours-diffusion [2 2 2] test-grid-3d 0.1 current-pressure)))  
    (is (< 0 (p/neighbours-diffusion [2 2 2 2] test-grid-4d 0.1 current-pressure)))  
  ))

(deftest test-diffusion-diffuse 
   (let [
         test-grid (ge/pressure-grid 5 2 test-pressure)
         test-grid-2 (ge/pressure-grid 5 2 test-pressure)]
     ;; setup
     (ge/set-pressure! test-grid [2 2] (* 2 test-pressure))
     (is (= (ge/get-pressure test-grid [2 2]) (* 2 test-pressure)))
     (is (= (ge/get-pressure test-grid [1 1]) test-pressure))
     (is (not (= test-grid-2 test-grid)))
     (is (= test-grid test-grid) )
     ;; diffuse
     ;; 
     (is (= test-pressure (ge/get-pressure test-grid [1 2])))
     (is (< test-pressure (ge/get-pressure (p/diffuse test-grid 0.1) [1 2])))
     ))
