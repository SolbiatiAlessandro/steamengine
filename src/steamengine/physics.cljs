(ns steamengine.physics
  (:require [clojure.core.matrix :as mat]
            [steamengine.game-engine :as game-engine]))

(def diffusion-precision 20)

(defn diffuse [pressure-grid, grid-size, dimensions, diffusion-factor] (+ 1 1))

(defn- neighbours-map [xyz func]
  (map-indexed (fn [i xi] (assoc xyz i (func xi))) xyz))

(defn- neighbours [xyz]
  (into (neighbours-map xyz inc) (neighbours-map xyz dec)))

(defn- neighbour-wall [xyz wall]
  (every? true? (map (fn [x] (and (> x -1) (< x wall))) xyz)))

(defn neighbours-bounded [xyz wall]
  (filter (fn [xyz] (neighbour-wall xyz wall)) (neighbours xyz)))
