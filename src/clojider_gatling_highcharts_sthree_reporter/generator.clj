(ns clojider-gatling-highcharts-sthree-reporter.generator
  (:require [clojider-gatling-highcharts-reporter.generator :refer [create-chart]]
            [clj-gatling.simulation-util :refer [create-dir]]
            [clojure.string :refer [split]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.java.io :as io])
  (:import [org.joda.time LocalDateTime]
           [java.io File]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]))

;TODO Add a way to use other than default credentials
(def aws-credentials
  (delay (.getCredentials (DefaultAWSCredentialsProviderChain.))))

(defonce s3-client
  (delay (AmazonS3Client. @aws-credentials)))

(defn- create-results-bucket [bucket-name region]
  (if (.doesBucketExist @s3-client bucket-name)
    (println bucket-name "already exists. Skipping creation.")
    (do (println "Creating bucket" bucket-name "for the results.")
        (if (= "us-east-1" region)
          (.createBucket @s3-client bucket-name)
          (.createBucket @s3-client bucket-name region)))))

(defn- path-join [& paths]
  (.getCanonicalPath (apply io/file paths)))

(defn- download-file [results-dir bucket object-key]
  (io/copy (.getObjectContent (.getObject @s3-client bucket object-key))
           (io/file (str results-dir "/" (last (split object-key #"/"))))))

(defn- download-logs-and-create-chart [results bucket-name input-dir results-dir]
  (println "Downloading" results "from" bucket-name)
  ;TODO Read these in parallel
  (doseq [result results]
    (download-file input-dir bucket-name result))
  (create-chart results-dir))

(def generator
  (fn [{:keys [context results-dir]}]
    (let [bucket-name (:bucket-name context)
          region (:regiion context)
          input-dir (path-join results-dir "input")]
      (create-dir input-dir)
      (create-results-bucket bucket-name region)
      {:generate (fn [results]
                   (download-logs-and-create-chart results bucket-name input-dir results-dir)
                   (println (str "Open " results-dir "/index.html with your browser to see a detailed report." )))})))