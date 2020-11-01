(ns steamengine.physics
  (:require [clojure.core.matrix :as mat]
            [steamengine.game-engine :as game-engine]))

(def diffusion-precision 5)

(defn- neighbours-map [xyz func]
  (map-indexed (fn [i xi] (assoc (vec xyz) i (func xi))) xyz))

(defn- neighbours [xyz]
  (into (neighbours-map xyz inc) (neighbours-map xyz dec)))

(defn- neighbour-wall [xyz wall]
  (every? true? (map (fn [x] (and (> x -1) (< x wall))) xyz)))

(defn neighbours-bounded [xyz wall]
  (filter (fn [xyz] (neighbour-wall xyz wall)) (neighbours xyz)))

(defn neighbours-diffusion [xyz next-grid diffusion-factor curr-density]
  (let [nns (neighbours-bounded xyz (game-engine/grid-size next-grid))
        denom (+ 1 (* (count nns) diffusion-factor))
        numer (+ curr-density (reduce + (map (fn [xyz] (game-engine/grid-val next-grid xyz)) nns)))]
    (/ numer denom)))

(defn diffuse [curr-grid, diffusion-factor]
  (let [next-grid (mat/clone curr-grid)]
    (doseq [k (range diffusion-precision)]
      (mat/emap-indexed! 
                         (fn [xyz _] (neighbours-diffusion xyz next-grid diffusion-factor (game-engine/grid-val curr-grid xyz) ) )
                         next-grid  
                         ))
    next-grid 
    ))

(defn neighbours-advection [ijk velocities grid-size]
  """
  ijk = [1, 2, 3] velocities = [u_grid, v_grid, w_grid], variable naming from paper
  """
  (let [xyz (map-indexed (fn [i, x] (- x (game-engine/get-pressure (nth velocities i) ijk))) ijk)
        bounded-xyz (map (fn [x] (cond (< x 0.5) 0.5 (> x grid-size) (+ grid-size 0.5) :else x)) xyz)
        ijk0 (map int bounded-xyz)
        ijk1 (map (comp inc int bounded-xyz))
        stu1 (map-indexed (fn [i, x] (- x (nth ijk0 i))) bounded-xyz)
        stu0 (map (fn [x] (- 1 x)) stu1)
        ]

    
    ))


(defn advect [xyz]
   (let [ proj (map-indexed (fn [i, x] (- x (game-engine/get-pressure (nth velocities i) xyz))) xyz)
          bounded-proj (map (fn [x] (cond (< x 0.5) 0.5 (> x grid-size) (+ grid-size 0.5) :else x)) proj)])
  )

