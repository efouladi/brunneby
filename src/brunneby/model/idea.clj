(ns brunneby.model.idea
  (:require [clojure.string :as string])
  (:use korma.db)
  (:use korma.core))

(defdb db (System/getenv "DATABASE_URL"))
(defentity users)
(defentity comments (belongs-to users))
(defentity campaigns)
(defentity ideas
  (has-many comments)
  (belongs-to campaigns))

(defentity votes)

(defn get-votes-by-idea [id]
  (- (:sum (first (select votes (aggregate (count :*) :sum) (where (and (= :vote true) (= :ideas_id id))))))
     (:sum (first (select votes (aggregate (count :*) :sum) (where (and (= :vote false) (= :ideas_id id))))))
     ))

(defn get-ideas-summery []
  (let [ideas (select ideas (fields :id :title))]
    (map #(assoc % :votes (get-votes-by-idea (:id %))) ideas)))

(defn get-ideas-summery-by-campaign [campaign-id]
  (let [ideas (select ideas (fields :id :title) (where (= :campaigns_id campaign-id)))]
    (map #(assoc % :votes (get-votes-by-idea (:id %))) ideas)))

(defn get-by-id [id]
  (assoc (first (select ideas 
                 (with comments (with users) 
                   (fields :content :created_at :users.username)
                   )
                 (with campaigns (fields [:title :campaign]))
                 (where (= :id id))
                 (fields :id :title :description [:campaigns.title :campaign]) 
                 ))
          :votes (get-votes-by-idea id)))

(defn save [idea]
  (insert ideas
          (values idea)))

(defn place-comment [user-id idea-id cm]
  (insert comments (values {:content cm :users_id user-id :ideas_id idea-id})))
