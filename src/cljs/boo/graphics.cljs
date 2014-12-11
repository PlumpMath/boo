(ns boo.graphics
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async :refer [>! <! put! take! chan alts! timeout]]
    ))
   
(def aaa 123)

;;---- anim stuff -------------------------
;; 066 294 7584 / 94  ира

(defonce anim-chan (chan (dropping-buffer 1)))

(defn anim-loop [c t]
  (if c
    (go
      (<! (timeout 1000))
      (>! c t)
      (.requestAnimationFrame js/window (partial anim-loop c))
      )))

(defn anim-start [c]
  (anim-loop c 0))

(defn anim-stop []
  (anim-loop nil 0))


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




