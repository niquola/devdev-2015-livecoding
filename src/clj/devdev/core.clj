(ns devdev.core
  (:require [org.httpkit.client :as cli]
            [ring.middleware.defaults :as mw]
            [cheshire.core :as json]
            [route-map.core :as rt]
            [ring.middleware.resource :as mwr]
            [hiccup.core :as html]
            [org.httpkit.server :as srv]))



(defn layout [cnt]
  (html/html
   [:html
    [:head
     [:link {:rel "stylesheet" :href "//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"}]]
    [:body
     [:div.container  cnt]
     [:script {:type "text/javascript" :src "js/app.js"}]]]))

(defn index [req]
  {:body (layout [:div#app "loading..."]) 
   :headers {"Content-Type" "text/html"}
   :status 200})


(defonce sockets (atom #{}))

(defn broad-cast [msg]
  (doseq [ch @sockets]
    (srv/send! ch msg)))

(deref sockets)

(defn repl-eval [expr]
  (with-out-str
    (try
     (println (eval (read-string expr)))
     (catch Exception e
       (println (str "Error: " e))))))

(repl-eval "(println 1)")

(repl-eval "ups")

(defonce hx (atom ()))

(defn process [expr]
  (let [res (pr-str {:expr expr
                     :time (java.util.Date.)
                     :id (str (gensym))
                     :result (str (repl-eval expr))})]
    (swap! hx conj res)
    (broad-cast res)))

(defn on-connected [ch]
  (swap! sockets conj ch)
  (doseq [msg @hx] (srv/send! ch msg)))

(defn repl [req]
  (srv/with-channel req ch
    (println "ws client")
    (on-connected ch)
    (srv/on-receive ch
                    (fn [x]
                      (println "received: " x)
                      (process x)))
    (srv/on-close ch (fn [_]
                       (println "closed")
                       (swap! sockets disj ch)))))


(def routes {:GET #'index
            "repl" {:GET #'repl}})

(defn dispatch [{uri :uri meth :request-method :as req}]
  (if-let [match (rt/match [meth uri] routes)]
    ((:match match) req)
    {:headers {"Content-Type" "text/html"}
     :body (layout [:h1 (str uri " not found")])}))



(def app
  (-> #'dispatch
      (mwr/wrap-resource "cljs/build/public")
      (mw/wrap-defaults mw/site-defaults)))


(defn start []
  (def stop (srv/run-server #'app {:port 8080})))

(comment
  (index {})

  (-> "http://localhost:8080"
      cli/get
      deref
      :body
      slurp)

  (stop)
  (start))

