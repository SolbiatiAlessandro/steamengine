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

(deftest test-get-pressure
  (let [m (steamengine.game-engine/pressure-grid 2 2)]
    (is (steamengine.game-engine/get-pressure m [0 0]) steamengine.game-engine/initial-pressure )
    (is (steamengine.game-engine/get-pressure m [0 1]) steamengine.game-engine/initial-pressure )
    (is (steamengine.game-engine/get-pressure m [1 0]) steamengine.game-engine/initial-pressure )
    (is (steamengine.game-engine/get-pressure m [1 1]) steamengine.game-engine/initial-pressure )
    ))

(deftest test-set-pressure
   (let [m (steamengine.game-engine/pressure-grid 3 2)]
    (steamengine.game-engine/set-pressure! m [0 0] 0.2)
    (is (steamengine.game-engine/get-pressure m [0 0]) 0.2)
    ))
