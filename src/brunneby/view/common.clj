(ns brunneby.view.common
  (:use hiccup.core)
  (:use hiccup.page)
  (:use hiccup.element)
  (:use hiccup.form)
  (:require [brunneby.model.categories :as cat]
            [noir.session :as session]
            [clojure.string :as string]
            [noir.validation :as vali]))


(defn login-form []
  (if-let [user (session/get :username)] 
    [:span "Welcome:" user
     (link-to "/logout" "Logout")
     ]
    (form-to [:post "/login"]
             (label "username" "Username: ")
             (text-field "username")
             (label "password" "Password: ")
             (password-field "password")
             (submit-button "Login")
             (link-to "/register" "Not a User? Register Here"))))



(defn error-item [[errors]]
  [:p.errors errors])


(defn register-form 
  ([{:keys [username password confirm-password email]}]
   (form-to [:post "/register"]
            [:table 
             [:tr
              [:td (label "username" "Username: ")]
              [:td (text-field "username" username)
               (vali/on-error :username error-item)]]
             [:tr
              [:td (label "email" "Email: ")]
              [:td (text-field "email" email)
               (vali/on-error :email error-item)]]
             [:tr
              [:td (label "password" "Password: ")]
              [:td (password-field "password")
               (vali/on-error :password error-item)]]
             [:tr 
              [:td (label "confirm-password" "Confirm Password: ")]
              [:td (password-field "confirm-password")]]
             [:tr [:td (submit-button "Register")]]]))
  ([]
   (form-to [:post "/register"]
            [:table 
             [:tr
              [:td (label "username" "Username: ")]
              [:td (text-field "username")
               ]]
             [:tr
              [:td (label "email" "Email: ")]
              [:td (text-field "email")
               ]]
             [:tr
              [:td (label "password" "Password: ")]
              [:td (password-field "password")
               ]]
             [:tr 
              [:td (label "confirm-password" "Confirm Password: ")]
              [:td (password-field "confirm-password")]]
             [:tr [:td (submit-button "Register")]]])))



(defn layout [title & body] 
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:title title]
     (include-css "/stylesheets/base.css"
                  "/stylesheets/skeleton.css"
                  "/stylesheets/screen.css")
     (include-css "http://fonts.googleapis.com/css?family=Sigmar+One&v1")]
    [:body
     [:header [:h1 "Brunneby"]
      (login-form)      
      ]
     [:nav 
      (unordered-list   (map #(link-to (str "/" (string/lower-case (string/replace %1 " " "-"))) %1) (map :title (cat/get-categories))))

      ]
     [:section body]
     [:footer] 
     ]))

