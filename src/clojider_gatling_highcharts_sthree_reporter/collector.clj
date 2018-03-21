(ns clojider-gatling-highcharts-sthree-reporter.collector
  (:require [clojider-gatling-highcharts-reporter.reporter :refer [csv-writer]]
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

(defn- start-time []
  (LocalDateTime.))

(defn- s3-writer [bucket-name results-dir start simulation {:keys [node-id batch-id batch]}]
  (let [custom-formatter (f/formatter "yyyyMMddHHmmss")
        timestamp (f/unparse custom-formatter (t/now))
        file-name (first (csv-writer results-dir start simulation batch-id batch))
        s3-object-key (str (:name simulation) "/simulation-" timestamp "-" node-id "-" batch-id ".log")]
    (println "Storing to s3" s3-object-key file-name)
    (.putObject @s3-client bucket-name s3-object-key (File. file-name))
    (.delete (File. file-name))
    [s3-object-key]))

(def collector
  (fn [{:keys [context results-dir]}]
    (let [bucket-name (:bucket-name context)
          region (:regiion context)
          input-dir (path-join results-dir "input")]
      (create-dir input-dir)
      (create-results-bucket bucket-name region)
      {:collect (partial s3-writer bucket-name input-dir (start-time))
       :combine concat})))