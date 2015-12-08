(ns devdev.core
  (:require
   [cljs.reader :as reader]
   [reagent.core :as reagent :refer [atom]]))

(def state
  (atom {:items ()}))

(defonce socket (atom nil))

(defn open-socket []
  (let [url (.. js/window -location -host)
        ws (js/WebSocket. (str "ws://" url "/repl"))]
    (set! (.-onmessage ws) (fn [msg]
                             (.log js/console "received" (.-data msg))
                             (let [data (reader/read-string (.-data msg))]
                               (swap! state update-in [:items] conj data))))
    (reset! socket ws)))

(defn send [msg]
  (println "SEND: " msg @socket)
  (if-let [sock @socket]
    (.send sock msg)
    (println "no socket")))

(defn submit [ev]
  (when (and (.-shiftKey ev) (= 13 (.-which ev)))
    (.preventDefault ev)
    (let [expr (.. ev -target -value)]
      (send expr)))
  (.log js/console (.-which ev)))

(defn home-page []
  [:div [:h2 "DEVDEV"]
   [:textarea.form-control {:id "inp" :autofocus true :on-key-press submit}]
   [:hr]
   (for [expr (:items @state)]
     [:div  {:key (:id expr)}
      [:b (:user expr)]
      [:pre (:expr expr)]
      [:pre "=>" (:result expr)]
      [:hr]])])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [home-page]
                  (.getElementById js/document "app")))

(defn init! []
  (open-socket)
  (mount-root))
