(ns buyme-aggregation-backend.sources.imgur-test
  (:require [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [cemerick.url :refer [url]]
            [cheshire.core :as json]
            [buyme-aggregation-backend.types :refer [fetch parse]]
            [buyme-aggregation-backend.sources.imgur :refer :all]))


(def gallery-body
  {"name"  "wallpaper"
   "items" [{"id"          "image1"
             "title"       "Image 1 Name."
             "description" "Image 1 Description"
             "width"       800
             "height"      300
             "account_id"  234
             "is_album"    false}

            {"id"          "image2"
             "title"       "Image 2 Name."
             "description" "Image 2 Description"
             "width"       600
             "height"      400
             "account_id"  235
             "is_album"    false}

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
   "images"      [{"id"          "image3"
                   "title"       "Image 3 Name."
                   "description" "Image 3 Description"
                   "width"       800
                   "height"      300
                   "account_id"  456
                   "is_album"    false}

                  {"id"          "image4"
                   "title"       "Image 4 Name."
                   "description" "Image 4 Description"
                   "width"       600
                   "height"      400
                   "account_id"  457
                   "is_album"    false}]})
(def fake-album-response
  (json/generate-string {"data" album-body}))


(deftest imgur-source
  (testing "fetching returns the correct data"
    (let [source (make-source nil)]
      (with-fake-http [(str (url api-root tag-gallery-path "wallpaper")) fake-gallery-response
                       (str (url api-root album-path "album1")) fake-album-response]
                      (is (= {:tag-items (-> (get gallery-body "items") (json/generate-string) (json/parse-string true))
                              :albums    {"album1" (-> album-body (json/generate-string) (json/parse-string true))}}
                             (fetch source))))))

  (testing "parsing returns the expected format"
    (with-fake-http [(str (url api-root tag-gallery-path "wallpaper")) fake-gallery-response
                     (str (url api-root album-path "album1")) fake-album-response]
                    (let [source (make-source nil)
                          fetch-result (fetch source)]
                      (is (= (parse source fetch-result)
                             []))))))