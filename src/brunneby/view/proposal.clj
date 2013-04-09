(ns brunneby.view.proposal
  (:use hiccup.core)
  (:use hiccup.element)
  (:require [brunneby.model.proposal :as prop])
  )


(defn proposal []
  (html [:table
         [:tr
          [:th "Title"]
          [:th "Votes"]
          ]
         (for [row (reverse (sort-by :votes (prop/get-proposals)))]
           [:tr
            [:td (link-to "#" (:title row))]
            [:td (link-to "#" (:votes row))]] 
           )
         ])
  )
