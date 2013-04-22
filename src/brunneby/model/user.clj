(ns brunneby.model.user
  (:require [clojure.string :as string])
  (:use korma.db)
  (:use korma.core))

(defdb db (System/getenv "DATABASE_URL"))

(defentity users)
(defentity votes)
(defentity users_categories)

(defn save [user]
  (insert users (values user)))

(defn get-cats []
  (select users_categories))

(defn auth? [username pass]
  (if-let [user (first (select users (where (and (= :password pass) (= :username username)))))]
    (:id user)
    false)
  )

(defn is-unique? [username]
  (empty? (select users (where (= :username username))))
  )

(defn voted [user-id idea-id] 
  (first (select votes (fields :vote) (where (and (= :users_id user-id) (= :ideas_id idea-id))))))

(defn vote [user-id idea-id vote]
  (insert votes (values {:users_id user-id :ideas_id idea-id :vote vote})))

