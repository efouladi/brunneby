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
            [ring.middleware.keyword-params :as kp]
            [brunneby.model.user :as user]
            [brunneby.model.campaign :as campaign]
            [brunneby.model.idea :as idea]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as st]
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
  [:#side-nav :li] (html/clone-for [item (campaign/get-all)]
                                              (html/content 
                                                  (html/html [:a {:href (str "campaign" "?id="(:id item))} (:title item)])))
  [:#center] (html/content content))

(html/defsnippet ideas-summery "brunneby/view/index.html" [:#ideas-summery] 
  [ideas]
  [[:tr (html/nth-of-type 2)]] (html/clone-for [idea ideas]
                                             (html/content 
                                               (html/html 
                                                 [:td [:a {:href (str "idea?id=" (:id idea))} (:title idea)]] 
                                                 [:td {:style "text-align:center"} (:votes idea)]))))

(html/defsnippet new-idea-form "brunneby/view/snippets.html" [:#new-idea-form]
  [campaigns user-id]
  [:#new-idea-form] (html/set-attr :method "POST" :action "idea")
  [:#new-idea-form] (html/append (html/html [:input {:type "hidden" :value user-id :name "users_id"}]))
  [:#campaign :option] (html/clone-for [camp campaigns]
                                       (html/do-> 
                                         (html/set-attr :value (:id camp))
                                         (html/content (:title camp)))))

(html/defsnippet vote-snippet "brunneby/view/snippets.html" [:#voting]
  [id]
  [:#thumbs-up] (html/set-attr :href (str "/vote?id=" id "&vote=true"))
  [:#thumbs-down] (html/set-attr :href (str "/vote?id=" id "&vote=false"))  
  )
(html/defsnippet voted-snippet "brunneby/view/snippets.html" [:#voting]
  [vt]
  [:#thumbs-up] nil
  [:#thumbs-down] nil
  [:#voting] (html/append (html/html [:div {:style "text-align:center"} 
                                      (if (boolean vt) "You agreed" "you disagreed")
                                      ])))


(html/defsnippet idea-snippet "brunneby/view/snippets.html" [:#idea]
  [{:keys [id title description campaign votes comments]}]
  [:#title] (html/content title)
  [:#campaign] (html/content (str "Campaign: " campaign))
  [:#description] (html/content description)
  [:#voting] (html/content 
               (if-let [vote (user/voted (session/get :user-id) id)]
                (voted-snippet (:vote vote))
                (vote-snippet id)))
  [:#votes] (html/content (str votes))
  [:tr] (html/clone-for [cm comments]
                        (html/content (html/html 
                          [:td (str "User: " (:username cm)) [:br] (str "Created at: " 
                                                                        (:created_at cm)
                                                                        )]
                                      [:td (:content cm)])))
  [:#submit-comment] (when (session/get :username)
                       (html/do->
                         (html/set-attr :method "POST" :action "/comment")
                         (html/append (html/html [:input {:type "hidden" :name "id" :value id}]))))
  )
(defn index []
  (layout {:content [(html/html [:h2 "Most Popular Ideas"]) (ideas-summery 
                        (take 10 (reverse (sort-by :votes (idea/get-ideas-summery)))))]}))

(defn error-page [message]
  (layout {:content (html/html [:h2 {:class "alert alert-error"} message])}))

(defn login [username pass]
  (if-let [id (user/auth? username pass)]
    (do 
      (session/put! :username username)
      (session/put! :user-id id)
      (response/redirect "/"))
    (error-page "Username/password is wrong")))

(defn logout []
  (session/clear!)
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
     (do (user/save (update-in (dissoc userinfo :confirm-password) [:users_categories_id] #(Integer/parseInt %)))
         (layout {:content  (html/html [:h2 "You are sucessfully registred!"])}))
     (layout {:content (reg-form)})))) 

(defn campaign [id]
  (let [iid (Integer/parseInt id) camp (campaign/get-by-id iid)] 
    (layout {:content 
             [(html/html [:h1 (:title camp)] [:p (:description camp)])
              (ideas-summery (reverse (sort-by :votes (idea/get-ideas-summery-by-campaign iid))))]
             })
   )
  )

(defn get-idea 
  [id]
  (layout {:content (idea-snippet (idea/get-by-id (Integer/parseInt id)))}))

(defn save-idea [idea]
  (let [id (idea/save  (reduce #(update-in %1 [%2] (fn [x] (Integer/parseInt x))) idea [:campaigns_id :users_id]))]
    (response/redirect (str "/campaign?id=" (:campaigns_id idea))))
  )

(defn should-login-first []
  (error-page "You should login first"))

(defn do-vote [id vote]
  (if-let [user-id (session/get :user-id)]
    (do 
      (user/vote user-id (Integer/parseInt id) (Boolean/parseBoolean vote))
      (response/redirect (str "/idea?id=" id)))
    (should-login-first)))

(defn place-comment [id cm]
  (if-let [user-id (session/get :user-id)]
    (do 
      (idea/place-comment user-id (Integer/parseInt id) cm)
      (response/redirect (str "/idea?id=" id)))
    (should-login-first)))

(def app-routes
  [(GET "/" [] (index))
  (POST "/login" [username password] (login username password))
  (GET "/logout" [] (logout))
  (GET "/register" [] (register))
  (POST "/register" [username password confirm-password email cat-id] 
        (register {:username username :password password :confirm-password confirm-password :email email :cat_id cat-id}))
  (GET "/campaign" [id] (campaign id))
  (GET "/idea" [id] (if (st/blank? id)
                      (if (session/get :username) 
                        (layout {:content (new-idea-form (campaign/get-all) (session/get :user-id))})
                        (should-login-first))
                      (get-idea id)))
  (POST "/idea" {idea :params} (save-idea idea))
  (GET "/vote" [id vote] (do-vote id vote))
  (POST "/comment" [id cm] (place-comment id cm))
  (route/files "/")])
  (route/not-found "Not Found")  

(def app
  (md/app-handler app-routes))
