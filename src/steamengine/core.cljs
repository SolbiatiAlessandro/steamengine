(ns steamengine.core
    (:require [steamengine.combinatorics]
              [steamengine.game-engine :as game-engine]
              [steamengine.physics :as physics]
              [VRButton :as VRButton]
              [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))

(enable-console-print!)

(def vr-settings
  {:grid-size 6
   :dimensions 3
   :z-offset 2
   :x-offset 0.5
   :y-offset -1
   :scale-factor 2
   })

(def laptop-settings
  {:grid-size 20
   :dimensions 2
   :z-offset 0
   :x-offset 0.5
   :y-offset 0.5
   :scale-factor 1
   })

(def settings laptop-settings)

;; -----------------------
;; GAME PHYSICS
;; -----------------------

(declare game)
;; (def grid-size (+ 4 (rand-int 3)))
(def grid-size (:grid-size settings))
(def dimensions (:dimensions settings))
(def initial-density 0)
(def diffusion-factor 0.001)
(def scaled-diffusion-factor (* diffusion-factor (js/Math.pow grid-size dimensions)))  
(def sources-period 500)
(def sources-duration 20)
(def sources-value 1)
;; we use 1-neighbours for source size
;; (def sources-size (/ grid-size 4))

(defn create-sources[]
  (let [
      ;;source-center (repeat dimensions (rand-int grid-size))
      source-center (repeat dimensions (int (/ grid-size 2)))
        ]
  (zipmap (physics/neighbours-bounded source-center grid-size) (repeat dimensions sources-value))))

(defn update-sources [sources step]
  (cond 
    (= 0 (mod step sources-period )) (create-sources)
    (= sources-duration (mod step sources-period)) {}
    :else sources))

(defn add-density-sources [coordinates current-density]
  (let [source-component (get (:sources, @game) coordinates 0)]
    (do 
      (+ current-density source-component))))

;; -----------------------
;; GAME LOGIC
;; -----------------------

(def step-limit
  "need to figure out here why the browser crashes
  if I make the simulation run for too long
  1 - check javascript games logic (dt)
  2 - check clojure persitent ds are killing the heap"
  2000)
(def step-limit-on false)
(defrecord Game [grid sources step grid-halted max-step seconds velocities])

(defn create-grid [grid-size, dimensions]
  (let [grid (game-engine/grid grid-size dimensions initial-density) ]
    ;;(game-engine/grid-val! grid [2 2] 300)
    grid
  ))

(defn create-velocities [grid-size, dimensions]
  ;;(map (fn [_] (game-engine/grid grid-size dimensions 0.0000000004)) (range dimensions))))
  [(game-engine/grid grid-size dimensions -0.000000000015) (game-engine/grid grid-size dimensions 0)])


(defn step-grid [grid velocities dt]
  (let [
         grid-with-sources (game-engine/grid-apply grid add-density-sources) 
         grid-diffused (physics/diffuse grid-with-sources scaled-diffusion-factor dt)   
         grid-advected (physics/advect grid-diffused velocities dt)
        ]
    (js/console.log dt)
    grid-diffused 
    ;;grid-advected
    ))


(defn create-game[]
  (let [sources (create-sources)
        grid (create-grid grid-size dimensions)
        seconds (game-engine/now-seconds)
        velocities (create-velocities grid-size dimensions)
        ]
    (Game. grid sources 0 false false seconds velocities)))

(def game (atom (create-game)))

;; -----------------------
;; RENDER LOGIC
;; -----------------------

(defn add-light [scene]
  (let 
    [light (js/THREE.DirectionalLight. 0xffffff)]
      (.set (.-position light) 1 1 1)
      (.add scene light)))

(defn create-scene []
  (let [
        scene (js/THREE.Scene.)
        room (js/THREE.LineSegments. ())
        ]
    (set! (.-background scene) (js/THREE.Color. 0x505050))
    (.add scene (js/THREE.HemisphereLight. 0x606060 0x404040))
    (add-light scene)
    scene
    )
  )

(defn create-camera [scene]
  (let [camera (js/THREE.PerspectiveCamera. 50
                                              (/ (.-innerWidth js/window) (.-innerHeight js/window))
                                              0.1
                                              10)]
  (set! (.-x (.-position camera)) 0 )
  (set! (.-y (.-position camera)) 0 )
  (set! (.-z (.-position camera)) 1 )
  (.add scene camera)
  camera
    ))

(def scene (create-scene))

(def z-offset (:z-offset settings))
;;(def x-offset (/ grid-size 2))
(def x-offset (:x-offset settings))
;;(def y-offset (/ grid-size 2))
(def y-offset (:y-offset settings))
(def scale-factor (:scale-factor settings))
(def cube-size (/ scale-factor grid-size))
(def background-color "#ededed")

(defn density-to-opacity [density]
  (let [min-density (* 0.1 sources-value)
        max-density (* 0.2 sources-value)
        min-opacity 0
        max-opacity 0.7
        mid-opacity 0.3
        ]
  (cond
    (<= density min-density) min-opacity
    (>= density max-density) max-opacity
    :else mid-opacity)))


(defn color-cell [cell density]
      ;; https://stackoverflow.com/questions/5137831/map-a-range-of-values-e-g-0-255-to-a-range-of-colours-e-g-rainbow-red-b/5137964
  (let [material (.-material cell)]
    (set! (.-transparent material) true)
    (set! (.-opacity material) (density-to-opacity density))
    (.setHSL (.-color material) 0.6 1 0.5 0.5)))

(defn set-position [cell xyz]
  (let [scale (fn [x] (/ (* x scale-factor) grid-size))] 
    (if (= dimensions 2)
      (.set cell.position (- (scale (first xyz)) x-offset) (- (scale (second xyz)) y-offset))
      (.set cell.position (- (scale (first xyz)) x-offset) (- (scale (second xyz)) y-offset) (- (scale (nth xyz 2)) z-offset)) 
    ))
  )

(defn render-new-cell [xyz density]
  (let [geometry (js/THREE.BoxGeometry. cube-size cube-size cube-size)
        material (js/THREE.MeshLambertMaterial. )
        cube (js/THREE.Mesh. geometry material)]
    (do 
      (set-position cube xyz)
      (color-cell cube density)
      (set! (.-name cube) (str xyz))
      (.add scene cube))))

(defn render-cell [xyz density]
  (if-let [existing-cell (.getObjectByName scene (str xyz))]
    (color-cell existing-cell density)
    (render-new-cell xyz density)))

(defn render-grid [game]
  (game-engine/grid-apply (:grid, game) render-cell))

;; -----------------------
;; GAME LOOP
;; -----------------------

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn step-game [game]
  (if (or (:max-step, game) (:grid-halted, game))
    game
    (let [
          now (game-engine/now-seconds)
          next-grid (step-grid (:grid, game) (:velocities, game) (- now (:seconds, game)))
          ;;grid-halted (game-engine/grids-equal (:grid, game) next-grid)
          grid-halted false
          max-step (and step-limit-on (> (:step, game) step-limit))
          next-step (+ (:step, game) 1)
          next-sources (update-sources (:sources, game) next-step)
          next-velocities (:velocities, game)
          ]
      (do 
        (render-grid game)
        (if grid-halted (do
                          (js/alert "physics engine: equilibrium reached") 
                          (js/console.log (game-engine/print-grid next-grid)) 
                          ))
        ;;(if max-step (js/alert "game engine: limit physics simulation reached"))
        (Game. next-grid next-sources next-step grid-halted max-step now next-velocities)))))

(def stats (atom (js/Stats.)) )
(def dom-root (.getElementById js/document "frame"))

(defn startup-app
  []
  ( let [
          camera (create-camera scene)
          renderer (js/THREE.WebGLRenderer.)
          animation-loop (fn animate []
                   (.begin @stats)
                   (swap! game step-game)  
                   (.render renderer scene camera)
                   (.end @stats)
                   )
         ]
      (set! (.-innerHTML dom-root) "")
      (.showPanel @stats "all")
      (.appendChild dom-root (.-dom @stats))  
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.setClearColor renderer background-color)
      (set! (.-enabled (.-xr renderer)) true)
      (.appendChild dom-root (.-domElement renderer))
      (.appendChild dom-root (.createButton VRButton renderer))
      (render-grid @game)
      (.setAnimationLoop renderer animation-loop)
    )
  )

(startup-app)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
