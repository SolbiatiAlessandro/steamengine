(ns steamengine.physics
  (:require [clojure.core.matrix :as mat]
            [steamengine.game-engine :as game-engine]))

(def diffusion-precision 20)

(defn diffuse [pressure-grid, grid-size, dimensions, diffusion-factor] (+ 1 1))

(defn- neighbours-map [xyz func]
  (map-indexed (fn [i xi] (assoc xyz i (func xi))) xyz))

(defn neighbours [xyz wall]
  (into (neighbours-map xyz inc) (neighbours-map xyz dec)))

