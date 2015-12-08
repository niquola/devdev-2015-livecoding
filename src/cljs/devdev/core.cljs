(ns devdev.core
  (:require [reagent.core :as reagent :refer [atom]]))

(defn home-page [] [:div [:h2 "DEVDEV"]])

(defn mount-root []
  (reagent/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
