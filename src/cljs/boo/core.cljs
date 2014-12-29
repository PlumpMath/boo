(ns  boo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [boo.graphics :as g]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :as async :refer [>! <! put! chan alts!]]
   [goog.events :as events]
   [goog.dom.classes :as classes])
  (:import [goog.events EventType]))


(def nnn g/aaa)

;;--- state ----------------
(defonce app-state (atom {:text "Hello Chestnut!"}))
;; [id [x,y], id [x,y]]
(defonce my-id (atom 0))
(def clients (atom {}))
;;(defn foo [] (println "hello andrw"))


(defn on-message [msg]
  (print (.-data msg))
  (let [data (.-data msg)
       data* (js->clj (.parse js/JSON data) :keywordize-keys true)
       ;; data* (-> data (. js/JSON.parse) (js->clj :keywordize-keys true))
        {cmd :cmd id :id client-list :clients coord :coord} data*
        ]
   ;; (println  data*)
      (case cmd
        "init" (do
                 (print "init cmd")
                 (reset! my-id id)
                 (reset! clients client-list))

        "move" (do
                 (print "move client :" id " coord" coord )
                 (swap! clients assoc id coord))
        "new" (do
                (print "new cmd")
                (swap! clients assoc id [0 0]))
        "exit" (do
                 (print "exit cmd")
                 (swap! clients dissoc id))
        "echo" (print (str "echo " data*))
        (print (str "unknown command" cmd))
        )))

(defn create-ws [url]
  (let [ws (js/WebSocket. url)]
    (doto ws
      (aset "onerror" (fn [error] (print "some error" (.-data error))))
      (aset "onopen" (fn [evt] (print "connection OK")))
      (aset "onmessage" on-message)
      (aset "onclose" (fn [evt] (print "Socket has been closed: " (.-data evt)))))))

(defn create-ws-local [] (create-ws "ws://localhost:10555/ws"))

;; --- move --- (.stringify js/JSON *1)
(defn move [ws coord]
  (.send ws (.stringify js/JSON  (clj->js {:cmd "move" :id @my-id  :coord coord}))))



(def aaa 23)


(defn main []
  (om/root
   (fn [app owner]
     (reify
       om/IRender
       (render [_]
         (dom/h1 nil (:text app)))))
   app-state
   {:target (. js/document (getElementById "app"))}))
(defn main []
  (om/root
   (fn [app owner]
     (reify
       om/IRender
       (render [_]
         (dom/h1 nil (:text app)))))
   app-state
   {:target (. js/document (getElementById "app"))}))
