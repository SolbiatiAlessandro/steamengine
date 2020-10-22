(ns steamengine.core
    (:require [steamengine.combinatorics]
              [steamengine.game-engine :as game-engine]
              [VRButton :as VRButton]
              [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))

(enable-console-print!)

;; -----------------------
;; GAME PHYSICS
;; -----------------------

(declare sources)
(def initial-pressure 0.1)

(defn step-pressure [coordinates current-pressure]
  (let [source-component (get @sources coordinates 0)]
    (do 
      (+ current-pressure source-component))))

(defn create-sources[]
  (assoc {} [2 2] 0.0005))

(def sources (atom (create-sources)))

;; -----------------------
;; GAME LOGIC
;; -----------------------

(def grid-size 5)
(def dimensions 2)
(declare game)
(defrecord Game [grid sources step])

(defn create-grid [grid-size, dimensions]
  (game-engine/pressure-grid grid-size dimensions))

(defn step-grid [grid]
  (game-engine/pressure-grid-apply grid step-pressure))

(defn create-game[]
  (let [sources (create-sources)
        grid (create-grid grid-size dimensions)]
    (Game. grid sources 0)))

(def game (atom (create-game)))

;; -----------------------
;; RENDER LOGIC
;; -----------------------

(def scene (js/THREE.Scene.))
(def z-offset -5)
(def x-offset (/ grid-size 2))
(def y-offset (/ grid-size 2))
(def cube-size 0.8)
(def background-color "#939393")

(defn render-cell-2d [xy pressure]
  (let [geometry (js/THREE.BoxGeometry. cube-size cube-size cube-size)
        material (js/THREE.MeshBasicMaterial. )
        cube (js/THREE.Mesh. geometry material)]
    (do 
      ;; https://stackoverflow.com/questions/5137831/map-a-range-of-values-e-g-0-255-to-a-range-of-colours-e-g-rainbow-red-b/5137964
      (.setHSL material.color pressure 1 0.5)
      (.set cube.position (- (first xy) x-offset) (- (second xy) y-offset) z-offset) 
      (.add scene cube))))

(defn add-light []
  (let 
    [light (js/THREE.PointLight. 0XFFFFFF 2 1000)]
      (.set (.-position light) 0 0 0)
      (.add scene light)))

(defn render-grid [game]
  (game-engine/pressure-grid-apply (:grid, game) render-cell-2d))

;; -----------------------
;; GAME LOOP
;; -----------------------

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn step-game [game]
  (let [stepped-grid (step-grid (:grid, game) (:sources, game))]
    (do 
      (render-grid game)
      (Game. stepped-grid (:sources, game) (+ (:step, game) 1)))))

(defn startup-app
  []
  ( let [
          camera (js/THREE.PerspectiveCamera. 75
                                              (/ (.-innerWidth js/window) (.-innerHeight js/window))
                                              0.1
                                              1000)
          renderer (js/THREE.WebGLRenderer.)
          animation-loop (fn animate []
                   (swap! game step-game)  
                   (.render renderer scene camera))
         ]
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.setClearColor renderer background-color)
      (set! (.-enabled (.-xr renderer)) true)
      (.appendChild (.-body js/document) (.-domElement renderer))
      (.appendChild (.-body js/document) (.createButton VRButton renderer))
      (render-grid @game)
      (set! (.-z (.-position camera))  5)
      (.setAnimationLoop renderer animation-loop)
    )
  )

(startup-app)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
