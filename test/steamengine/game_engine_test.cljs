(ns steamengine.game-engine-test
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.game-engine]
    ))

(def test-value 0)

(deftest test-fill-matrix
  (is (= (steamengine.game-engine/fill-matrix 0.1 [2 2]) [[0.1 0.1] [0.1 0.1]]))
  (is (= (steamengine.game-engine/fill-matrix 0.1 [3 3]) [[0.1 0.1 0.1] [0.1 0.1 0.1] [0.1 0.1 0.1]]))
  (is (= (steamengine.game-engine/fill-matrix 0.1 [2 2 2]) [[[0.1 0.1] [0.1 0.1]] [[0.1 0.1] [0.1 0.1]]]))
  )

(deftest test-grid-val-2d
  (let [test-value 1.251259
        m (steamengine.game-engine/grid 2 2 test-value)] 
    (is (= (steamengine.game-engine/grid-val m [0 0]) test-value))
    (is (= (steamengine.game-engine/grid-val m [0 1]) test-value))
    (is (= (steamengine.game-engine/grid-val m [1 0]) test-value))
    (is (= (steamengine.game-engine/grid-val m [1 1]) test-value))
    ))

(deftest test-grid-val-3d
  (let [m (steamengine.game-engine/grid 2 3)]
    (is (= (steamengine.game-engine/grid-val m [0 0 0]) test-value))
    (is (= (steamengine.game-engine/grid-val m [1 1 1]) test-value))
    ))

(deftest test-set-value-2d
   (let [m (steamengine.game-engine/grid 3 2)]
    (steamengine.game-engine/grid-val! m [0 0] 0.2)
    (is (= (steamengine.game-engine/grid-val m [0 0]) 0.2))
    (steamengine.game-engine/grid-val! m [2 2] 0.2)
    (is (= (steamengine.game-engine/grid-val m [2 2]) 0.2))
    ))

(deftest test-set-value-3d
   (let [m (steamengine.game-engine/grid 3 3)]
    (steamengine.game-engine/grid-val! m [0 0 0] 0.2)
    (is (= (steamengine.game-engine/grid-val m [0 0 0]) 0.2))
    (steamengine.game-engine/grid-val! m [2 2 1] 0.2)
    (is (= (steamengine.game-engine/grid-val m [2 2 1]) 0.2))
    ))

(defn dummy-grid-apply [coordinates value a]
  (+ (reduce + coordinates) value a))

(defn dummy-grid-apply-no-arg [coordinates value]
  (+ (reduce + coordinates) value))

(deftest grid-apply-2d
  (let [m (steamengine.game-engine/grid 2 2)
        n (steamengine.game-engine/grid-apply m dummy-grid-apply 2)
        s (steamengine.game-engine/grid-apply m dummy-grid-apply-no-arg)] 
    (do 
      (is (= (steamengine.game-engine/grid-val n [0 0]) (+ 0 0 test-value 2)))
      (is (= (steamengine.game-engine/grid-val n [1 1]) (+ 1 1 test-value 2)))
      (is (= (steamengine.game-engine/grid-val s [0 0]) (+ 0 0 test-value)))
      )))

(deftest grid-apply-3d
  (let [m (steamengine.game-engine/grid 3 3)
        n (steamengine.game-engine/grid-apply m dummy-grid-apply 2)]
    (do 
      (is (= (steamengine.game-engine/grid-val n [0 0 0]) (+ 0 0 0 test-value 2)))
      (is (= (steamengine.game-engine/grid-val n [1 1 1]) (+ 1 1 1 test-value 2)))
      (is (= (steamengine.game-engine/grid-val n [2 2 2]) (+ 2 2 2 test-value 2)))
      )))
