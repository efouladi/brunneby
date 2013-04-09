(ns brunneby.model.user
  (:require [clojure.string :as string])
  )

(defn save [{:keys [username password email]}]
;  {:pre [(not (string/blank? username))
 ;        (not (string/blank? password))
  ;       (not (string/blank? email))
   ;      ]}
  (println username password email)
  true
  )


(defn auth? [username pass]
  false
  )

(defn is-unique? [username]
  true
  )
