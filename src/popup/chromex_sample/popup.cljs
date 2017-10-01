(ns ^:figwheel-no-load chromex-sample.popup
  (:require [chromex-sample.popup.core :as core]
            [reagent.core :as r :refer [atom]]))

(js/console.log "popup main check...")

(def $tagline "background color changer")

(defn change-background-color [color]
  (let [script (str "document.body.style.backgroundColor=\""
                    color
                    "\";")]
    (-> (aget js/chrome "tabs")
        (.executeScript (clj->js {:code script})))))

(defn main []
  (r/render
   (let []
     (fn []
       [:div
        [:h3
         (-> $tagline
             (str "... says HELLO!"))]
        [:div
         {:id "container"}
         [:span "choose a color"]
         [:select
          {:id "dropdown"
           :on-change (fn [evt]
                        (-> evt
                            (aget "target" "value")
                            (change-background-color)))}
          (->> [""
                "white"
                "purple"
                "pink"
                "green"
                "yellow"]
               (map (fn [color]
                      ^{:key color}
                      [:option
                       {:value color}
                       color])))]]]))
   (js/document.getElementById "app")))


(if (= (aget js/document "readyState") "loading")
  (do
    (js/console.warn "loading -- adding event DOMContentLoaded")
    (.addEventListener js/document "DOMContentLoaded" main)
    )
  (do
    (js/console.warn "already loaded. starting main()")
    (main)))

(core/init!)
