(ns brunneby.model.user
  (:require [clojure.string :as string])
  (:use korma.db)
  (:use korma.core))

(defdb db (System/getenv "DATABASE_URL"))

(defentity users)
(defentity users_categories)

(defn save [user]
  (insert users (values user)))

(defn get-cats []
  (select users_categories))

(defn auth? [username pass]
  (not (empty? (select users (where (and (= :password pass) (= :username username))))))
  )

(defn is-unique? [username]
  (empty? (select users (where (= :username username))))
  )
