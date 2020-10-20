(ns steamengine.game-engine
              (:require [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))  

(def initial-pressure 0.1)

(defn fill-matrix [value [first-dimension & remaining-dimensions]]
  "Creates a matrix filled with `value` and has dimensions [x y z ..]
  Used as input at core.matrix/matrix. See `test-fill-matrix` for usage."
  (if (some? remaining-dimensions)
    (let [lower-dimensional-matrix (fill-matrix value remaining-dimensions)] 
      (vec (repeat first-dimension lower-dimensional-matrix)))
    (vec (repeat first-dimension value ))))

(defn pressure-grid [grid-size, dimensions]
  (mat/matrix :aljabr (fill-matrix initial-pressure (repeat dimensions grid-size))))

(defn get-pressure [pressure-grid coordinates]
  (apply mat/mget (into [pressure-grid] coordinates)))

(defn set-pressure! [pressure-grid coordinates value]
  (apply mat/mset! (concat [pressure-grid] coordinates [value])))
