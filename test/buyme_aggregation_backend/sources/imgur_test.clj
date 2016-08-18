(ns buyme-aggregation-backend.sources.imgur-test
  (:require [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [cemerick.url :refer [url]]
            [cheshire.core :as json]
            [buyme-aggregation-backend.types :refer [fetch parse]]
            [buyme-aggregation-backend.sources.imgur :refer :all]))


(def fake-gallery-response
  (json/generate-string {"data" {"name" "wallpaper"
                                 "items" [{"id" "image1"
                                           "title" "Image 1 Name."
                                           "description" "Image 1 Description"
                                           "width" 800
                                           "height" 300
                                           "account_id" 234
                                           "is_album" false}

                                          {"id" "image2"
                                           "title" "Image 2 Name."
                                           "description" "Image 2 Description"
                                           "width" 600
                                           "height" 400
                                           "account_id" 235
                                           "is_album" false}

                                          {"id" "album1"
                                           "title" "Album 1 Name"
                                           "description" "Album 1 Description"
                                           "account_id" 123
                                           "is_album" true}]}}))

(def fake-album-response
  (json/generate-string {"data" {"id" "album1"
                                 "title" "Album 1 Name"
                                 "description" "Album 1 Description"
                                 "account_id" 123
                                 "nsfw" true

                                 "images" [{"id" "image3"
                                            "title" "Image 3 Name."
                                            "description" "Image 3 Description"
                                            "width" 800
                                            "height" 300
                                            "account_id" 456
                                            "is_album" false}

                                           {"id" "image4"
                                            "title" "Image 4 Name."
                                            "description" "Image 4 Description"
                                            "width" 600
                                            "height" 400
                                            "account_id" 457
                                            "is_album" false}]}}))


(deftest imgur-source
  (testing "fetching returns the correct data"
    (let [source (make-source nil)]
      (with-fake-http [(str (url api-root tag-gallery-path "wallpaper")) fake-gallery-response
                       (str (url api-root album-path "album1")) fake-album-response]
                      #_(println (fetch source))
                      (is (= (fetch source)
                             {:tag-items []
                              :albums []}))))))
;{:tag-items [{:id image1, :title Image 1 Name., :description Image 1 Description, :width 800, :height 300, :account_id 234, :is_album false} {:id image2, :title Image 2 Name., :description Image 2 Description, :width 600, :height 400, :account_id 235, :is_album false} {:id album1, :title Album 1 Name, :description Album 1 Description, :account_id 123, :is_album true}], :albums {nil {:opts {:headers {Authorization Client-ID , Accept application/json}, :method :get, :url https://api.imgur.com/3/album/album1}, :status 200, :headers {:content-type text/html, :server org.httpkit.fake}, :body {"data":{"id":"album1","title":"Album 1 Name","description":"Album 1 Description","account_id":123,"nsfw":true,"images":[{"id":"image3","title":"Image 3 Name.","description":"Image 3 Description","width":800,"height":300,"account_id":456,"is_album":false},{"id":"image4","title":"Image 4 Name.","description":"Image 4 Description","width":600,"height":400,"account_id":457,"is_album":false}]}}}}}