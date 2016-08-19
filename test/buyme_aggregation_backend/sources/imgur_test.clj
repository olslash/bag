(ns buyme-aggregation-backend.sources.imgur-test
  (:require [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [cemerick.url :refer [url]]
            [cheshire.core :as json]
            [buyme-aggregation-backend.types :refer [fetch parse]]
            [buyme-aggregation-backend.sources.imgur :refer :all]))


(defn image [n]
  {"id"          (str "image " n)
   "title"       (str "image " n " name")
   "description" (str "image " n " description")
   "width"       n
   "height"      n
   "account_id"  n
   "is_album"    false})



(def gallery-body
  {"name"  "wallpaper"
   "items" [(image 1)
            (image 2)

            {"id"          "album1"
             "title"       "Album 1 Name"
             "description" "Album 1 Description"
             "account_id"  123
             "is_album"    true}]})

(def fake-gallery-response
  (json/generate-string {"data" gallery-body}))

(def album-body
  {"id"          "album1"
   "title"       "Album 1 Name"
   "description" "Album 1 Description"
   "account_id"  123
   "nsfw"        true
   "images"      [(image 3)
                  (image 4)]})


(def fake-album-response
  (json/generate-string {"data" album-body}))


(deftest imgur-source
  (testing "fetching returns the correct data"
    (let [source (make-source nil)]
      (with-fake-http [(str (url api-root tag-gallery-path "wallpaper")) fake-gallery-response
                       (str (url api-root album-path "album1")) fake-album-response]
                      (is (= (fetch source)
                             {:tag-items (-> (get gallery-body "items") (json/generate-string) (json/parse-string true))
                              :albums    {"album1" (-> album-body (json/generate-string) (json/parse-string true))}})))))


  (testing "parsing returns the expected format"
    (with-fake-http [(str (url api-root tag-gallery-path "wallpaper")) fake-gallery-response
                     (str (url api-root album-path "album1")) fake-album-response]
                    (let [source (make-source nil)
                          fetch-result (fetch source)]
                      (is (= (parse source fetch-result)
                             [(-> (image 1) (json/generate-string) (json/parse-string true) (api-image->image))
                              (-> (image 2) (json/generate-string) (json/parse-string true) (api-image->image))
                              (-> (image 3) (json/generate-string) (json/parse-string true) (assoc :nsfw true) (api-image->image))
                              (-> (image 4) (json/generate-string) (json/parse-string true) (assoc :nsfw true) (api-image->image))]))))))