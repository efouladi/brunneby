(ns brunneby.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as cresp]
            [noir.session :as session]
            [noir.util.middleware :as md]
            [brunneby.view.common :as common]
            [brunneby.view.proposal :as prop]
            [ring.util.response :as response]
            [brunneby.model.user :as user]
            [noir.validation :as vali]
            ))


(defn index []
  (common/layout "Brunneby" 
                 (prop/proposal)))

(defn login [username pass]
  (if (user/auth? username pass)
    (do (session/put! :username username)
        (response/redirect "/"))
    (common/layout "Brunneby - Login"
                   [:h2 "Username/password is wrong"]
                   )
    ) 
  )

(defn logout []
  (session/remove! :username)
  (response/redirect "/")
  )

(defn valid? [{:keys [username password email confirm-password]}]
  (vali/rule (vali/has-value? username)
             [:username "Username is required"])
  (vali/rule (vali/has-value? password)
             [:password "Password is required"])
  (vali/rule (vali/has-value? email)
             [:email "Email is required"])
  (when-not (nil? email) (vali/rule (vali/is-email? email)
             [:email "Email is not valid"]))
  (vali/rule (user/is-unique? username)
             [:username "Username already exists"])
  (vali/rule (= password confirm-password)
             [:password "Passwords do not match"]
             )
  (not (vali/errors? :email :username :password))
  )

(defn register 
  ([] (common/layout "Brunneby - Register User" (common/register-form)))
  ([userinfo]
   (if (valid? userinfo)
     (do (user/save [userinfo])
         (common/layout "Brunneby - Succesfully Registered!" [:h2 "You are sucessfully registred!"]))
     (common/layout "Brunneby - Register User" (common/register-form userinfo))
     ) 
   )
  )

(def app-routes
  [(GET "/" [] (index))
  (POST "/login" [username password] (login username password))
  (GET "/logout" [] (logout))
  (GET "/register" [] (register))
  (POST "/register" [username password confirm-password email] (register {:username username :password password :confirm-password confirm-password :email email}))
  (route/not-found "Not Found")])

(def app
  (md/app-handler app-routes))
