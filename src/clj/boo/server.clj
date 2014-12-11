(ns boo.server
  (:require [clojure.java.io :as io]
            [clojure.data.json  :refer [write-str read-str]]
            [boo.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [compojure.handler :refer [api]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [ring.middleware.reload :as reload]
            [environ.core :refer [env]]
            [clojure.core.async :as async :refer
             [chan go <! >! <!! >!! alts! close! timeout thread]]
            [org.httpkit.server :refer [run-server with-channel send! on-close on-receive]]))
;;(def my-chan (timeout 100))

(deftemplate page
  (io/resource "index.html") [] [:body] (if is-dev? inject-devmode-html identity))


(def  aaa 100)

;;(json/read-str "{\"a\":1,\"b\":2}" :key-fn keyword)
;;(read-str "{\"a\":1,\"b\":2}")
;;(write-str {"a" 1 "b" 2})
;;(write-str {:a 1 :b 2})
;; @(future (Thread/sleep 1000) :done)

;;--- state -------
(defonce clients (atom {}))
(defonce max-id (atom 0))

;;--- utils -------
(defn send-to-all [msg]
  (doseq [c (keys @clients)]
    (send! c (write-str msg))))

(defn next-id []
    (swap! max-id inc))

;; @(future (Thread/sleep 1000) :well-done)

(defn on-exit [channel]
  (println "on-exit")
  (let [[id _] (channel clients)]
    (swap! clients dissoc channel)
    (send-to-all {:cmd "exit" :id id})))

(defn reset-clients []
  (reset! clients {}))

(defn on-message [data channel]
  (println "on message" data)
  (case (:cmd data)
    "move" (let [{new-coord :coord} data
                 [id _] (@clients channel)]
             (swap! clients assoc channel [id new-coord])
             (send-to-all {:cmd "move" :id id :coord new-coord}))
    ))

(defn init-client [channel]
  (let [id (next-id)]
    (println "new client")
    (swap! clients assoc channel [id [0 0]])
    (send! channel (write-str {:cmd "init" :id id
                               :clients (vals @clients)
                               }))
    (send-to-all {:cmd "new" :id id})
    ))

(defn sock-handler [req]
  (with-channel req channel
    (init-client channel)
    (on-close channel on-exit)
    (on-receive channel (fn [data] (#'on-message (read-str data :key-fn keyword) channel)) )))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/ws" [] sock-handler)
  (GET "/*" req (page)))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (api #'routes))
    (api routes)))



;;--- http server stuff --------------------------------
(defn run [& [port]]
  (defonce ^:private server
    (do
      (if is-dev? (start-figwheel))
      (let [port (Integer. (or port (env :port) 10555))]
        (print "Starting web server on port" port ".\n")
        (run-server http-handler {:port port
                          :join? false}))))
  server)

(defn -main [& [port]]
  (run port))
