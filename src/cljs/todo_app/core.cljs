(ns todo-app.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Storage
(defonce app-db (atom {:todos (sorted-map)}))
(defonce counter (atom 0))

;; -------------------------
;; Constants
(defonce enter-key 13)

;; -------------------------
;; Actions

(defn new-item [title]
  {:id @counter
   :title title
   })

(defn add-todo [title]
  (let [item (new-item @title)]
    (pr (str "swapping" title))
    (swap! counter inc)
    (swap! app-db assoc-in [:todos (item :id)] item))
  )

;; -------------------------
;; Components

(defn on-key-down [key title]
  (let [key-pressed (.-which key)]
    (condp = key-pressed
      enter-key (add-todo title)
      nil)))

(defn todo-editor []
  (let [title (atom "")]
   (fn [] 
     [:div [:input#new-todo {:type "text"
                        :value @title
                        :placeholder "What needs to be done"
                        :on-change #(reset! title (-> % .-target .-value))
                        :on-key-down #(on-key-down % title)
                        }]
      [:span {}  @title]])))

(defn todo-item [todo]
  (pr todo)
  [:li (todo :title)])

(defn todo-list [{:keys [todos] :as db}]
  (let [items (vals todos)]
    [:ul (for [todo items] (todo-item todo))]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to todo-app"]
   [todo-editor]
   [todo-list @app-db]])

(defn about-page []
  [:div [:h2 "About todo-app"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
