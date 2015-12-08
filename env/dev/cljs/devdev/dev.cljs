(ns ^:figwheel-no-load devdev.dev
  (:require [devdev.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
 :websocket-url (str "ws://" (.. js/window -location -hostname) ":3449/figwheel-ws")
  :jsload-callback core/mount-root)

(core/init!)
