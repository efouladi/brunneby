(ns brunneby.model.campaign
  (:use korma.db)
  (:use korma.core))


(defdb db (System/getenv "DATABASE_URL"))
(defentity campaigns)


(defn get-all []
  (select campaigns))
 
(defn get-by-id [id]
  (first (select campaigns (where (= :id id)))))
