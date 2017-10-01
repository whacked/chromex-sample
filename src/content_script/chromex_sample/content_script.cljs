(ns ^:figwheel-no-load chromex-sample.content-script
  (:require [chromex-sample.content-script.core :as core]
            [reagent.core :as r :refer [atom]]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            ))

(defn set-element-style! [dom-element style-map]
  (doto dom-element
    (-> (aget "style")
        (js/Object.assign
         (clj->js style-map)))))

(let []
  (defstyle my-style
    [".my-tabs"
     {:display "table"
      :list-style-type "none"
      :margin 0
      :padding 0}

     ["li"
      {:float "left"
       :padding "0.5emx"
       :margin-right "0.5em"
       :background-color "gray"}]

     ["li:hover"
      {:background-color "gold"}]

     ["li.selected"
      {:background-color "lightgray"}]
     ]))


(js/console.log "content_script main check...")

;; TODO move to utility ns
(defn clj->hiccup
  "
  example:
  (reagent/render
   [(fn []
      (clj->hiccup @data))]
   container)
  "
  [orig-data & custom-args]
  (let [{:keys [base-style
                object-table-style
                object-row-style
                object-key-style
                object-value-style
                array-list-style
                array-item-style
                content-renderer]
         :or {base-style {:font-size "10pt"
                          :font-family "Mono"}
              object-table-style {:display "table"
                                  :border "1px solid green"
                                  :padding "0.25em"
                                  :margin "0.25em"}
              object-row-style {:display "table-row"
                                :margin "4px"
                                :padding "80px"}
              object-key-style {:display "table-cell"
                                :padding "0.1em"
                                :background "#CFC"}
              object-value-style {:display "table-cell"
                                  :background "#FFC"}
              array-list-style {:border-left "4px double #999"}
              array-item-style {:padding "0.25em"
                                :margin "0.25em"
                                :border "1px dashed #CCC"}
              content-renderer str}} (->> custom-args
                                          (partition 2)
                                          (map vec)
                                          (into {}))

        ->attr (fn ->attr
                 ([ext-style]
                  (->attr ext-style {}))
                 ([ext-style ext-attr]
                  (merge {:style (merge base-style
                                        ext-style)}
                         ext-attr)))
        recurse (fn recurse [data]
                  [:div
                   (cond
                     (map? data)
                     [:div
                      (->attr object-table-style)
                      (->> data
                           (map
                            (fn [[k sub-data]]
                              ^{:key sub-data}
                              [:div
                               (->attr object-row-style)
                               [:div
                                (->attr object-key-style)
                                k]
                               [:div
                                (->attr object-value-style)
                                (apply clj->hiccup (cons sub-data custom-args))]])))]

                     (sequential? data)
                     [:ol
                      (->attr array-list-style)
                      (->> data
                           (map-indexed
                            (fn [i sub-data]
                              ^{:key sub-data}
                              [:li
                               (->attr array-item-style)
                               (recurse sub-data)])))]

                     :else
                     [:span
                      {:style (cond (boolean? data)
                                    (merge {:font-family "Monospace"}
                                           (if (= true data)
                                             {:background "red"
                                              :color "white"}
                                             {:background "red"
                                              :color "white"}))

                                    (number? data)
                                    {:font-family "Monospace"
                                     :color "red"}

                                    :else
                                    {})}
                      (content-renderer data)
                      ])])]
    (recurse orig-data)))


(defn px [val]
  (str val "px"))

(defn main []
  (js/console.warn "--> main renderer")
  (let [target-el (let [target-id "my-custom-element"]
                    (or (js/document.getElementById target-id)
                        (let [el (doto (js/document.createElement "div")
                                   (.setAttribute "id" target-id))]
                          (js/console.warn "creating element with id"
                                           target-id)
                          (js/console.warn el)
                          (-> js/document.body
                              (.appendChild el))
                          (js/console.warn "APPENDED!")
                          el)))]
      (js/console.warn "--> REAGENT FIRE!")

      (r/render
       (let [state (r/atom {:minimized? true})


             button-size 20]
         [(fn []
            [:div
             {:style (merge {:position "fixed"
                             :top "0"
                             :left "0"
                             :background "skyblue"
                             :opacity 0.8
                             :border "2px solid black"
                             :overflow "hidden"
                             }
                            (if (@state :minimized?)
                              {:width (px 800)
                               :height (px 600)}
                              {:width (px button-size)
                               :height (px button-size)}))}
             [:div
              [:button
               {:type "button"
                :on-click (fn []
                            (swap! state update :minimized? not))
                :style {:position "absolute"
                        :top 0
                        :left 0
                        :border "none"
                        :background (if (@state :minimized?)
                                      "red"
                                      "gray")
                        :width (px button-size)
                        :height (px button-size)}}
               ]]

             ;; a simple tabbed interface
             [:div
              [:ul
               {:class (:my-tabs my-style)}
               [:li "helicopter"]
               [:li "stork"]
               [:li "magic carpet"]

               ]
              ]

             [:h1 "hello from content_script renderer"]
             [:div
              [:ol
               [:li "fancy paint"]
               [:li "unique pattern"]]]])])
       target-el)

      (js/console.warn "--> main renderer done")))

(if (= (aget js/document "readyState") "loading")
  (do
    (js/console.warn "loading -- adding event DOMContentLoaded")
    (.addEventListener js/document "DOMContentLoaded" main))
  (do
    (js/console.warn "already loaded. starting main()")
    (main)))

(core/init!)
