(ns brunneby.model.migration
  (:require [clojure.java.jdbc :as j]))

(defn create-campaigns []
  (j/with-connection (System/getenv "DATABASE_URL")
    (j/create-table :campaigns
                    [:id :serial "PRIMARY KEY"]
                    [:title :varchar "NOT NULL"]
                    [:description :text "NOT NULL"])))

(defn create-users-categories []
  (j/with-connection (System/getenv "DATABASE_URL")
    (j/create-table :users_categories
                    [:id :serial "PRIMARY KEY"]
                    [:title :varchar "NOT NULL"])))

(defn create-users []
     (j/with-connection (System/getenv "DATABASE_URL")
       (j/create-table :users
                       [:id :serial "PRIMARY KEY"]
                       [:username :varchar "NOT NULL"]
                       [:password :varchar "NOT NULL"]
                       [:email :varchar "NOT NULL"]
                       [:users_categories_id :integer "REFERENCES users_categories"])))

(defn create-ideas []
  (j/with-connection (System/getenv "DATABASE_URL")
    (j/create-table :ideas
                    [:id :serial "PRIMARY KEY"]
                    [:title :varchar "NOT NULL"]
                    [:description :text "NOT NULL"]
                    [:users_id :integer "REFERENCES users"]
                    [:campaigns_id :integer "REFERENCES campaigns"]
                    )))

(defn create-comments []
  (j/with-connection (System/getenv "DATABASE_URL")
    (j/create-table :comments
                    [:id :serial "PRIMARY KEY"]
                    [:content :text "NOT NULL"]
                    [:created_at :timestamp "NOT NULL DEFAULT CURRENT_TIMESTAMP"]
                    [:users_id :integer "REFERENCES users"]
                    [:ideas_id :integer "REFERENCES ideas"])))
(defn create-votes []
   (j/with-connection (System/getenv "DATABASE_URL")
     (j/create-table :votes
                     [:users_id :integer "REFERENCES users"]
                     [:ideas_id :integer "REFERENCES ideas"]
                     [:vote :boolean "NOT NULL"]
                     ["PRIMARY KEY (user_id, idea_id)"]))
  )
(defn -main []
  (print "Creating database tables") (flush)
  (create-campaigns)
  (create-users-categories)
(create-users)
  (println " done")) 
