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
            [brunneby.model.campaign :as camp]
            [net.cgrand.enlive-html :as html]
            [noir.validation :as vali]))

(defn error-item [[errors]]
  (html/html [:span#inline-help errors]))

(html/defsnippet camp-links "brunneby/view/index.html" [:#side-nav] [])

(html/defsnippet reg-form "brunneby/view/register.html" [:.form-horizontal] 
  []
  [:.form-horizontal] (html/set-attr :action "/register" :method "POST")
  [:#users-categories :option] (html/clone-for 
                                 [item (user/get-cats)] 
                                 (html/do-> 
                                   (html/content (:title item))
                                   (html/set-attr :value (:id item))))
  [:#inputUsername :.controls] (html/append (vali/on-error :username error-item))
  [:#inputEmail :.controls] (html/append (vali/on-error :email error-item))
  [:#inputPassword :.controls] (html/append (vali/on-error :password error-item))
  [:#inputPasswordConfirm :.controls] (html/append (vali/on-error :confirm-password error-item))
  )


(html/defsnippet signin-form "brunneby/view/index.html" [:#signin] []
;TODO: how to change the attrubute of the node   
           ; [html/any-node] (html/set-attr :action "/login")
            ;(html/set-attr :method "POST") 
  )

(html/deftemplate layout "brunneby/view/index.html"
  [{:keys [content]}]
  [:#signin] (if-let [username (session/get :username)]
               (html/content (html/html [:span (str "Welcome: " username)] [:a {:href "/logout"} "Sign Out"]))
                (html/content (signin-form))) 
  [:#side-nav :li] (html/clone-for [item (user/get-camp)]
                                              (html/content 
                                                  (html/html [:a {:href (str "campaign" "?id="(:id item))} (:title item)])))
  [:#center] (html/content content))

(defn index []
  (layout {:content "center"}))

(defn login [username pass]
  (if (user/auth? username pass)
    (do (session/put! :username username)
        (println username pass)
        (response/redirect "/"))
    (layout {:content (html/html [:h2 {:class "alert alert-error"}"Username/password is wrong"])})))

(defn logout []
  (session/remove! :username)
  (response/redirect "/"))

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
             [:password "Passwords do not match"])
  (not (vali/errors? :email :username :password)))

(defn register 
  ([] (layout {:content (reg-form)}))
   
  ([userinfo]
   (if (valid? userinfo)
     (do (user/save (update-in (dissoc userinfo :confirm-password) [:cat_id] #(Integer/parseInt %)))
         (layout {:content  (html/html [:h2 "You are sucessfully registred!"])}))
     (layout {:content (reg-form)})))) 

(defn campaign [id]
  (let [camp (camp/get-by-id (Integer/parseInt id))] 
    (layout {:content 
             (html/html [:h1 (:title camp)] [:p (:description camp)])
             })
   )
  )

(def app-routes
  [(GET "/" [] (index))
  (POST "/login" [username password] (login username password))
  (GET "/logout" [] (logout))
  (GET "/register" [] (register))
  (POST "/register" [username password confirm-password email cat-id] 
        (register {:username username :password password :confirm-password confirm-password :email email :cat_id cat-id}))
  (GET "/campaign" [id] (campaign id))
  (route/files "/")])
  (route/not-found "Not Found")  

(def app
  (md/app-handler app-routes))
