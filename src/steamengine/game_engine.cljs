(ns steamengine.game-engine
              (:require [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))  

(def float-int-accuracy "e.g. 9.99 is considered as 10" 0.02)

(defn- fill-matrix [value [first-dimension & remaining-dimensions]]
  "Creates a matrix filled with `value` and has dimensions [x y z ..]
  Used as input at core.matrix/matrix. See `test-fill-matrix` for usage."
  (if (some? remaining-dimensions)
    (let [lower-dimensional-matrix (fill-matrix value remaining-dimensions)] 
      (vec (repeat first-dimension lower-dimensional-matrix)))
    (vec (repeat first-dimension value ))))

(defn grid 
  "abstraction for grid, in case we want to change the implementation in the future"
  ([grid-size, dimensions] (grid grid-size dimensions 0))  
  ([grid-size, dimensions, initial-value] 
   (mat/matrix :aljabr (fill-matrix initial-value (repeat dimensions grid-size)))))

(defn grid-size [grid]
  "grid is a square"
  (first (mat/shape grid)))

(defn point-in-grid? [xyz grid]
  (let [s (- (grid-size grid) 1)]
    (every? (fn [x] (and (>= x 0) (<= x s))) xyz)))

(defn grid-apply 
  "calls (function coordinates value arg) for each cell in grid,
  see dummy-grid-apply for example function. Accepts only one arg.
  
  TODO: there is a bug here, arg can be only one and if I pass a {:a 2} it becomes
  nil when is applied. I don't understand enough of clojure yet to make this work.
  
  After some digging looks like I can just use stuff in the local context passing 
  a lambda to emap-indexed, see diffusion code where I use emap-indexed!, I should
  still find a solution using a wrapper in case in the future I want to change
  implementation for grid from core.matrix"
  ([grid function] (grid-apply grid function nil))
  ([grid function arg] (mat/emap-indexed function grid arg)))

(defn grid-val [grid coordinates]
  "get grid value from discrete coordinates xy = [1, 2]"
  (apply mat/mget (into [grid] coordinates)))

(defn- grid-float-to-int [x]
  (let [x0 (int x) x1 (+ x0 1)] [x0 x1]))

(defn- neighbours-float [xyz]
  "get discrete neighbours from a 2d continuous point"
  (let [dim (count xyz)]
    (if (= dim 2)
      (for [x (grid-float-to-int (first xyz)) 
            y (grid-float-to-int (second xyz))] [x y])
      (for [x (grid-float-to-int (first xyz)) 
            y (grid-float-to-int (second xyz))
            z (grid-float-to-int (nth xyz 2))] [x y z]))))

(defn float-approx-int [x] 
  "can this float be approximated by an int within float-int-accuracy?"
  (cond 
    (< (mod x 1) float-int-accuracy) (int x)
    (< (- 1 x) float-int-accuracy) (+ 1 (int x))
    :else false))

(defn- grid-val-float--weight [xyz-int xyz-float]
  (reduce * 
    (map-indexed (fn [i x] (- 1 (js/Math.abs (- x (nth xyz-int i))))) xyz-float)))

(defn grid-val-float [grid coordinates]
  "get grid value from continuous coordinates xy = [1.5, 2.2]"
  (cond 
    (not (point-in-grid? coordinates grid)) (throw (js/Error "game-engine error: trying to access point out of grid!"))   
    (every? float-approx-int coordinates) (grid-val grid (map float-approx-int coordinates))
    :real-float-coordinates (let [neighbours (neighbours-float coordinates)]
        (apply + (map (fn [xyz] (* (grid-val grid xyz) (grid-val-float--weight xyz coordinates))) neighbours)))
    ))


(defn grid-val! [grid coordinates value]
  (apply mat/mset! (concat [grid] coordinates [value])))

(defn print-grid [grid]
  "only 2d, https://github.com/mikera/core.matrix/issues/347"
  (clojure.string/join "\n"
    (map (fn [x] 
     (clojure.string/join ", " 
          (map (fn [y] (mat/mget grid x y)) (range (second (mat/shape grid))))) )
     (range (first (mat/shape grid))))))

(defn grids-equal [first-grid second-grid]
  (mat/equals first-grid second-grid)) 

(defn now-seconds [] (/ (.getTime (js/Date. )) 1000))
