(ns steamengine.core
    (:require [steamengine.combinatorics]
              [VRButton :as VRButton]
              [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))

(enable-console-print!)

;; -----------------------
;; GAME PHYSICS
;; -----------------------


(def initial-pressure 0.1)

(defn step-pressure [x y sources previous-pressures]
  (let [previous-pressure (get previous-pressures [x y] initial-pressure)
        source-component (get sources [x y] 0)]
    (+ previous-pressure source-component)))

;; -----------------------
;; GAME LOGIC
;; -----------------------

(def grid-size 5)
(def dimensions 2)

(defrecord Game [grid sources step])

(defn create-sources[]
  (assoc {} [0 0] 0.0005))

(defn grid-tuples [grid-size dimensions]
    (doall (steamengine.combinatorics/selections (range grid-size) dimensions))  
)

;; https://clojuredocs.org/clojure.core/reduce#example-542692ccc026201cdc326c3b
;; https://stackoverflow.com/questions/4053845/idiomatic-way-to-iterate-through-all-pairs-of-a-collection-in-clojure
(defn create-grid [grid-size, dimensions]
  (reduce #(assoc %1 %2 initial-pressure)
          {}
          (grid-tuples grid-size dimensions)))

(defn step-grid [game, grid-size, dimensions]
  (reduce #(assoc %1 %2 (step-pressure (first %2) (second %2) (:sources, game) (:grid, game)))
          {}
          (grid-tuples grid-size dimensions)))

(defn create-game[]
  (let [sources (create-sources)
        grid (create-grid grid-size dimensions)]
    (Game. grid sources 0)))

;; -----------------------
;; RENDER LOGIC
;; -----------------------

(def z-offset -5)
(def x-offset (/ grid-size 2))
(def y-offset (/ grid-size 2))
(def cube-size 0.8)
(def background-color "#939393")

(defn render-cell [scene xy pressure]
  (let [geometry (js/THREE.BoxGeometry. cube-size cube-size cube-size)
        material (js/THREE.MeshBasicMaterial. )
        cube (js/THREE.Mesh. geometry material)]
    (do 
      ;; https://stackoverflow.com/questions/5137831/map-a-range-of-values-e-g-0-255-to-a-range-of-colours-e-g-rainbow-red-b/5137964
      (.setHSL material.color pressure 1 0.5)
      (.set cube.position (- (first xy) x-offset) (- (second xy) y-offset) z-offset) 
      (.add scene cube))))

(defn add-light [scene]
  (let 
    [light (js/THREE.PointLight. 0XFFFFFF 2 1000)]
      (.set (.-position light) 0 0 0)
      (.add scene light)))

(defn render-grid [scene game]
  (do 
    ;;(add-light scene)
    (doseq [position_pressure (:grid, game)]
      (render-cell scene (first position_pressure) (second position_pressure)))))

;; -----------------------
;; GAME LOOP
;; -----------------------

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def game (atom (create-game)))

(defn step-game [game scene]
  (let [stepped-grid (step-grid game grid-size dimensions)]
    (do 
      (render-grid scene game)
      (Game. stepped-grid (:sources, game) (+ (:step, game) 1)))))

(defn startup-app
  []
  ( let [
          scene (js/THREE.Scene.)
          camera (js/THREE.PerspectiveCamera. 75
                                              (/ (.-innerWidth js/window) (.-innerHeight js/window))
                                              0.1
                                              1000)
          renderer (js/THREE.WebGLRenderer.)
          animation-loop (fn animate []
                   (swap! game step-game scene)  
                   (.render renderer scene camera))
         ]
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.setClearColor renderer background-color)
      (set! (.-enabled (.-xr renderer)) true)
      (.appendChild (.-body js/document) (.-domElement renderer))
      (.appendChild (.-body js/document) (.createButton VRButton renderer))
      (render-grid scene @game)
      (set! (.-z (.-position camera))  5)
      (js/console.log scene)
      (.setAnimationLoop renderer animation-loop)
    )
  )

(startup-app)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
