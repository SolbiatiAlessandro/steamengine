(ns steamengine.physics
  (:require [clojure.core.matrix :as mat]
            [steamengine.game-engine :as game-engine]))

(def diffusion-precision 20)

(defn diffuse [pressure-grid, grid-size, dimensions, diffusion-factor] (+ 1 1))

(defn nd-neighbours [xyz dim boundary]
  "n-dimensional neighbours of point x, see test-diffusion-neighbours"
  (let [
        x (last xyz)
        p (vec (take (- dim 1) xyz))
        left-n (into p [(- x 1)])
        right-n (into p [(+ x 1)])]
     (cond
       (= x 0) [right-n]
       (= x boundary) [left-n]
       :else (conj [left-n] right-n))))

(defn d-plus-1 [points ax]
  "project point in next dimension on axis x, see test-diffusion-neighbours"
  (map (fn [p] (into p [ax])) points))

(defn neighbours [xyz & boundary]
  "computes the neighbours (distance = 1) of n-dimensional point xyz"
  (let [dim (count xyz)]
    (if (= dim 1)
      (nd-neighbours [ (first xyz)] dim boundary)
      (let [
            value (nth xyz (- dim 1))
           r (take (- dim 1) xyz) 
           prev-n (d-plus-1 (neighbours r) value)
           curr-n (nd-neighbours r dim boundary)]
      (into prev-n curr-n)))))
