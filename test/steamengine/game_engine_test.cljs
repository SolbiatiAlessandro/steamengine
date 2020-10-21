(ns steamengine.game-engine-test
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [steamengine.game-engine]
    ))

(deftest test-fill-matrix
  (is (= (steamengine.game-engine/fill-matrix 0.1 [2 2]) [[0.1 0.1] [0.1 0.1]]))
  (is (= (steamengine.game-engine/fill-matrix 0.1 [3 3]) [[0.1 0.1 0.1] [0.1 0.1 0.1] [0.1 0.1 0.1]]))
  (is (= (steamengine.game-engine/fill-matrix 0.1 [2 2 2]) [[[0.1 0.1] [0.1 0.1]] [[0.1 0.1] [0.1 0.1]]]))
  )

(deftest test-get-pressure-2d
  (let [m (steamengine.game-engine/pressure-grid 2 2)]
    (is (= (steamengine.game-engine/get-pressure m [0 0]) steamengine.game-engine/initial-pressure))
    (is (= (steamengine.game-engine/get-pressure m [0 1]) steamengine.game-engine/initial-pressure))
    (is (= (steamengine.game-engine/get-pressure m [1 0]) steamengine.game-engine/initial-pressure))
    (is (= (steamengine.game-engine/get-pressure m [1 1]) steamengine.game-engine/initial-pressure))
    ))

(deftest test-get-pressure-3d
  (let [m (steamengine.game-engine/pressure-grid 2 3)]
    (is (= (steamengine.game-engine/get-pressure m [0 0 0]) steamengine.game-engine/initial-pressure))
    (is (= (steamengine.game-engine/get-pressure m [1 1 1]) steamengine.game-engine/initial-pressure))
    ))

(deftest test-set-pressure-2d
   (let [m (steamengine.game-engine/pressure-grid 3 2)]
    (steamengine.game-engine/set-pressure! m [0 0] 0.2)
    (is (= (steamengine.game-engine/get-pressure m [0 0]) 0.2))
    (steamengine.game-engine/set-pressure! m [2 2] 0.2)
    (is (= (steamengine.game-engine/get-pressure m [2 2]) 0.2))
    ))

(deftest test-set-pressure-3d
   (let [m (steamengine.game-engine/pressure-grid 3 3)]
    (steamengine.game-engine/set-pressure! m [0 0 0] 0.2)
    (is (= (steamengine.game-engine/get-pressure m [0 0 0]) 0.2))
    (steamengine.game-engine/set-pressure! m [2 2 1] 0.2)
    (is (= (steamengine.game-engine/get-pressure m [2 2 1]) 0.2))
    ))

(defn dummy-pressure-grid-apply [coordinates value a]
  (+ (reduce + coordinates) value a))

(defn dummy-pressure-grid-apply-no-arg [coordinates value]
  (+ (reduce + coordinates) value))

(deftest pressure-grid-apply-2d
  (let [m (steamengine.game-engine/pressure-grid 2 2)
        n (steamengine.game-engine/pressure-grid-apply m dummy-pressure-grid-apply 2)
        s (steamengine.game-engine/pressure-grid-apply m dummy-pressure-grid-apply-no-arg)] 
    (do 
      (is (= (steamengine.game-engine/get-pressure n [0 0]) (+ 0 0 steamengine.game-engine/initial-pressure 2)))
      (is (= (steamengine.game-engine/get-pressure n [1 1]) (+ 1 1 steamengine.game-engine/initial-pressure 2)))
      (is (= (steamengine.game-engine/get-pressure s [0 0]) (+ 0 0 steamengine.game-engine/initial-pressure)))
      )))

(deftest pressure-grid-apply-3d
  (let [m (steamengine.game-engine/pressure-grid 3 3)
        n (steamengine.game-engine/pressure-grid-apply m dummy-pressure-grid-apply 2)]
    (do 
      (is (= (steamengine.game-engine/get-pressure n [0 0 0]) (+ 0 0 0 steamengine.game-engine/initial-pressure 2)))
      (is (= (steamengine.game-engine/get-pressure n [1 1 1]) (+ 1 1 1 steamengine.game-engine/initial-pressure 2)))
      (is (= (steamengine.game-engine/get-pressure n [2 2 2]) (+ 2 2 2 steamengine.game-engine/initial-pressure 2)))
      )))
