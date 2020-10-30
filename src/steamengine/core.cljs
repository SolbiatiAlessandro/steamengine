(ns steamengine.core
    (:require [steamengine.combinatorics]
              [steamengine.game-engine :as game-engine]
              [steamengine.physics :as physics]
              [VRButton :as VRButton]
              [clojure.core.matrix :as mat]
              [thinktopic.aljabr.core :as imp]))

(enable-console-print!)

;; -----------------------
;; GAME PHYSICS
;; -----------------------

(declare game)
;; (def grid-size (+ 4 (rand-int 3)))
(def grid-size 5)
(def dimensions 2)
(def initial-pressure 0)
(def diffusion-factor 1.01)
(def sources-period 400)
(def sources-duration 100)

(defn create-sources[]
  (assoc {} (repeat dimensions (rand-int grid-size)) 1000))

(defn update-sources [sources step]
  (cond 
    (= 0 (mod step sources-period )) (create-sources)
    (= sources-duration (mod step sources-period)) {}
    :else sources))

(defn step-pressure [coordinates current-pressure]
  (let [source-component (get (:sources, @game) coordinates 0)]
    (do 
      (+ current-pressure source-component))))

;; -----------------------
;; GAME LOGIC
;; -----------------------

(def step-limit
  "need to figure out here why the browser crashes
  if I make the simulation run for too long
  1 - check javascript games logic (dt)
  2 - check clojure persitent ds are killing the heap"
  2000)
(defrecord Game [grid sources step grid-halted max-step])

(defn create-grid [grid-size, dimensions]
  (let [grid (game-engine/pressure-grid grid-size dimensions initial-pressure) ]
    ;;(game-engine/set-pressure! grid [2 2] 300)
    grid
  ))

(defn step-grid [grid]
  (physics/diffuse (game-engine/pressure-grid-apply grid step-pressure) diffusion-factor))

(defn create-game[]
  (let [sources (create-sources)
        grid (create-grid grid-size dimensions)]
    (Game. grid sources 0 false false)))

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
(def z-offset 0)

;;(def x-offset (/ grid-size 2))
(def x-offset 0.5)
;;(def y-offset (/ grid-size 2))
(def y-offset 0.5)
(def cube-size (/ 1 grid-size))
(def background-color "#ededed")

(defn pressure-to-color [pressure]
  "pressure measured in kPa, color in Hue (0, 0.8)
  max pressure human can survive = 700 kPa
  min pressure human can survive = 40 kPa"
  (let [min-pressure 0
        max-pressure 700
        min-hue 0
        max-hue 1
        ]
  (cond
    (<= pressure min-pressure) 0
    (>= pressure max-pressure) 1
    :else (* max-hue (/ (- pressure min-pressure) max-pressure)))))


(defn color-cell [cell pressure]
      ;; https://stackoverflow.com/questions/5137831/map-a-range-of-values-e-g-0-255-to-a-range-of-colours-e-g-rainbow-red-b/5137964
  (let [material (.-material cell)]
    (set! (.-transparent material) true)
    (set! (.-opacity material) (pressure-to-color pressure))
    (.setHSL (.-color material) 0.6 1 0.5 0.5)))

(defn set-position [cell xyz]
  (let [scale (fn [x] (/ x grid-size))] 
    (if (= dimensions 2)
      (.set cell.position (- (scale (first xyz)) x-offset) (- (scale (second xyz)) y-offset))
      (.set cell.position (- (scale (first xyz)) x-offset) (- (scale (second xyz)) y-offset) (- (scale (nth xyz 2)) z-offset)) 
    ))
  )

(defn render-new-cell [xyz pressure]
  (let [geometry (js/THREE.BoxGeometry. cube-size cube-size cube-size)
        material (js/THREE.MeshLambertMaterial. )
        cube (js/THREE.Mesh. geometry material)]
    (do 
      (set-position cube xyz)
      (color-cell cube pressure)
      (set! (.-name cube) (str xyz))
      (.add scene cube))))

(defn render-cell [xyz pressure]
  (if-let [existing-cell (.getObjectByName scene (str xyz))]
    (color-cell existing-cell pressure)
    (render-new-cell xyz pressure)))

(defn render-grid [game]
  (game-engine/pressure-grid-apply (:grid, game) render-cell))

;; -----------------------
;; GAME LOOP
;; -----------------------

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn step-game [game]
  (if (or (:max-step, game) (:grid-halted, game))
    game
    (let [next-grid (step-grid (:grid, game) )
          grid-halted (game-engine/pressure-grids-equal (:grid, game) next-grid)
          max-step (> (:step, game) step-limit)
          next-step (+ (:step, game) 1)
          next-sources (update-sources (:sources, game) next-step)
          ]
      (do 
        (render-grid game)
        (if grid-halted (do
                          (js/alert "physics engine: equilibrium reached") 
                          (js/console.log (game-engine/print-pressure-grid next-grid)) 
                          ))
        ;;(if max-step (js/alert "game engine: limit physics simulation reached"))
        (Game. next-grid next-sources next-step grid-halted max-step)))))

(def stats (atom (js/Stats.)) )

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
      (.showPanel @stats "all")
      (.appendChild (.-body js/document) (.-dom @stats))  
      (.setSize renderer (.-innerWidth js/window) (.-innerHeight js/window))
      (.setClearColor renderer background-color)
      (set! (.-enabled (.-xr renderer)) true)
      (.appendChild (.-body js/document) (.-domElement renderer))
      (.appendChild (.-body js/document) (.createButton VRButton renderer))
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
