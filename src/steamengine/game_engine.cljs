(ns steamengine.game-engine
              (:require [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))  

(defn- fill-matrix [value [first-dimension & remaining-dimensions]]
  "Creates a matrix filled with `value` and has dimensions [x y z ..]
  Used as input at core.matrix/matrix. See `test-fill-matrix` for usage."
  (if (some? remaining-dimensions)
    (let [lower-dimensional-matrix (fill-matrix value remaining-dimensions)] 
      (vec (repeat first-dimension lower-dimensional-matrix)))
    (vec (repeat first-dimension value ))))

(defn pressure-grid 
  "abstraction for pressure-grid, in case we want to change the implementation in the future"
  ([grid-size, dimensions] (pressure-grid grid-size dimensions 0))  
  ([grid-size, dimensions, initial-pressure] 
   (mat/matrix :aljabr (fill-matrix initial-pressure (repeat dimensions grid-size)))))

(defn pressure-grid-size [grid]
  "pressure-grid is a square"
  (first (mat/shape grid)))

(defn pressure-grid-apply 
  "calls (function coordinates value arg) for each cell in grid,
  see dummy-pressure-grid-apply for example function. Accepts only one arg.
  
  TODO: there is a bug here, arg can be only one and if I pass a {:a 2} it becomes
  nil when is applied. I don't understand enough of clojure yet to make this work.
  
  After some digging looks like I can just use stuff in the local context passing 
  a lambda to emap-indexed, see diffusion code where I use emap-indexed!, I should
  still find a solution using a wrapper in case in the future I want to change
  implementation for pressure-grid from core.matrix"
  ([grid function] (pressure-grid-apply grid function nil))
  ([grid function arg] (mat/emap-indexed function grid arg)))

(defn get-pressure [pressure-grid coordinates]
  (apply mat/mget (into [pressure-grid] coordinates)))

(defn set-pressure! [pressure-grid coordinates value]
  (apply mat/mset! (concat [pressure-grid] coordinates [value])))

(defn print-pressure-grid [pressure-grid]
  "only 2d, https://github.com/mikera/core.matrix/issues/347"
  (clojure.string/join "\n"
    (map (fn [x] (
     (clojure.string/join ", " 
          (map (fn [y] (mat/mget pressure-grid x y)) (range (second (mat/shape pressure-grid))))))
     (range (first (mat/shape pressure-grid)))))))

(defn pressure-grids-equal [first-grid second-grid]
  (mat/equals first-grid second-grid)) 

