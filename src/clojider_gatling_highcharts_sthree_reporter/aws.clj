(ns clojider-gatling-highcharts-sthree-reporter.aws
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split]])
  (:import [java.io File]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]))

;TODO Add a way to use other than default credentials
(def credentials
  (delay (.getCredentials (DefaultAWSCredentialsProviderChain.))))

(defonce s3-client
  (delay (AmazonS3Client. @credentials)))

(defn create-results-bucket [bucket-name region]
  (if (.doesBucketExist @s3-client bucket-name)
    (println bucket-name "already exists. Skipping creation.")
    (do (println "Creating bucket" bucket-name "for the results.")
        (if (= "us-east-1" region)
          (.createBucket @s3-client bucket-name)
          (.createBucket @s3-client bucket-name region)))))

(defn download-file [results-dir bucket object-key]
  (io/copy (.getObjectContent (.getObject @s3-client bucket object-key))
           (io/file (str results-dir "/" (last (split object-key #"/"))))))
