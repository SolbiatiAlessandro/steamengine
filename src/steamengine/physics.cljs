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
        numer (+ curr-density (* diffusion-factor (reduce + (map (fn [xyz] (game-engine/grid-val next-grid xyz)) nns))))]
    (/ numer denom)))

(defn diffuse [curr-grid, diffusion-factor, dt]
  (let [next-grid (mat/clone curr-grid)
        diffusion-factor (* diffusion-factor dt)]
    (doseq [k (range diffusion-precision)]
      (mat/emap-indexed! 
                         (fn [xyz _] (neighbours-diffusion xyz next-grid diffusion-factor (game-engine/grid-val curr-grid xyz) ) )
                         next-grid  
                         ))
    next-grid 
    ))

(defn project [xyz velocities grid-size dt]
  "project point back in space given static velocity vector"
  (let [proj (map-indexed (fn [i, x] (- x (* dt (game-engine/grid-val (nth velocities i) xyz)))) xyz) 
        lower-boundary 0
        upper-boundary (- grid-size 1)]
          (map (fn [x] (cond 
                         (< x lower-boundary) lower-boundary
                         (> x upper-boundary) upper-boundary
                         :else x)) proj)))


(defn advect-point [xyz density velocities dt]
   "trace backwards xyz through velocities to float positioned density"
   (let [xyz-p (project xyz velocities (game-engine/grid-size density) dt)]
     ;;(js/console.log "advect-point")
     ;;(js/console.log (clj->js xyz))
     ;;(js/console.log (clj->js xyz-p))
     (game-engine/grid-val-float density xyz-p)))

(defn advect [density velocities dt]
  "advection of density given a static velocity field"
  (mat/emap-indexed (fn [xyz] (advect-point xyz density velocities dt)) density))
