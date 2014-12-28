(ns boo.graphics
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [>! <! put! take! chan alts! timeout dropping-buffer]]
   ))

(def aaa 123)

;;---- anim stuff -------------------------
;; 066 294 7584 / 94  ира

(declare game-step)

(defonce anim-flag false)
(defn anim-loop-2 [time]
  (if anim-flag
    (go
      #_(println time)
      (game-step)
      #_(<! (timeout 300))
      (.requestAnimationFrame js/window anim-loop-2))))

(defn anim-start []
  (set! anim-flag true)
  (anim-loop-2 0))

(defn anim-stop []
  (set! anim-flag false))


(defn foo [c]
  (go
    (loop [t (<! c)]
      (.log js/console  t)
      (recur c))))

;;-----------------------------------------

;;(.requestAnimationFrame js/window)

(defn draw-client[x y r]
  (let [canvas (.getElementById js/document "canvas")
        cxt (.getContext canvas "2d")]
    ;;(print canvas)
    (doto cxt
      .beginPath
      (aset "fillStyle" "green")
      (.arc x y r 0 (* 2 js/Math.PI))
      .fill
      )))

(defn clean-context []
  (let [canvas (.getElementById js/document "canvas")
        cxt (.getContext canvas "2d")
        w (.-width canvas)
        h (.-height canvas)]
    (.clearRect cxt 0 0 w h)))

;;---------------------------------------------

(defonce game-state (atom 0))

(defn game-step []
  (swap! game-state (fn [x] (mod (inc x) 100)))
  (clean-context)
  (draw-client @game-state 100 10))

;;-- run -----------------------------------
(anim-start)
