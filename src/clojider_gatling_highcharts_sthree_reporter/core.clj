(ns clojider-gatling-highcharts-sthree-reporter.core
  (:require [clojider-gatling-highcharts-reporter.reporter :refer [csv-writer]]
            [clojider-gatling-highcharts-reporter.generator :refer [create-chart]]
            [clj-gatling.simulation-util :refer [create-dir]]
            [clojure.string :refer [split]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.java.io :as io])
  (:import [org.joda.time LocalDateTime]
           [java.io File]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]))

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

(defn start-time []
  (LocalDateTime.))

(defn s3-writer [bucket-name results-dir start simulation {:keys [node-id batch-id batch]}]
  ;TODO We need executor node id here
  (let [custom-formatter (f/formatter "yyyyMMddHHmmss")
        timestamp (f/unparse custom-formatter (t/now))
        file-name (first (csv-writer results-dir start simulation batch-id batch))
        s3-object-key (str (:name simulation) "/simulation-" timestamp "-" node-id "-" batch-id ".log")]
    (println "Storing to s3" s3-object-key file-name)
    (.putObject @s3-client bucket-name s3-object-key (File. file-name))
    (.delete (File. file-name))
    [s3-object-key]))

(defn download-file [results-dir bucket object-key]
  (io/copy (.getObjectContent (.getObject @s3-client bucket object-key))
           (io/file (str results-dir "/" (last (split object-key #"/"))))))

(defn download-logs-and-create-chart [results bucket-name folder-name]
  (let [input-dir (str "tmp/" folder-name "/input")]
    (create-dir input-dir)
    (println "Downloading" results "from" bucket-name)
    (doseq [result results]
      (download-file input-dir bucket-name result))
    (create-chart (str "tmp/" folder-name))))

(defn gatling-highcharts-s3-reporter [bucket-name region results-dir]
  (println "Using S3 reporter")
  (create-results-bucket bucket-name region)
  (let [log-dir (path-join results-dir "input")]
    (create-dir log-dir)
    {:reporter-key :highcharts-s3
     :parser (partial s3-writer bucket-name log-dir (start-time))
     :combiner concat
     :generator (fn [results]
                  (download-logs-and-create-chart results bucket-name "testing")
                  (println (str "Open " results-dir "/index.html with your browser to see a detailed report." )))}))
