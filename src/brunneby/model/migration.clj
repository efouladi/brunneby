(ns brunneby.model.migration
  (:require [clojure.java.jdbc :as j]))

(defn create-campaigns []
  (j/with-connection (System/getenv "DATABASE_URL")
    (j/create-table :campaigns
                    [:id :serial "PRIMARY KEY"]
                    [:title :varchar "NOT NULL"]
                    [:description :text "NOT NULL"]
                    [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

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
                       [:cat_id :integer "REFERENCES users_categories"])))


(defn -main []
  (print "Creating database tables") (flush)
  (create-campaigns)
  (create-users-categories)
(create-users)
  (println " done")) 
