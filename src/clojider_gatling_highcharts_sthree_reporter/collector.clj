(ns clojider-gatling-highcharts-sthree-reporter.collector
  (:require [clojider-gatling-highcharts-reporter.reporter :refer [csv-writer]]
            [clojider-gatling-highcharts-sthree-reporter.util :refer [create-dir
                                                                      path-join]]
            [clojider-gatling-highcharts-sthree-reporter.aws :as aws]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:import [org.joda.time LocalDateTime]
           [java.io File]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]))

(defn- start-time []
  (LocalDateTime.))

(defn- s3-writer [bucket-name results-dir start simulation {:keys [node-id batch-id batch]}]
  (let [custom-formatter (f/formatter "yyyyMMddHHmmss")
        timestamp (f/unparse custom-formatter (t/now))
        file-name (first (csv-writer results-dir start simulation batch-id batch))
        s3-object-key (str (:name simulation) "/simulation-" timestamp "-" node-id "-" batch-id ".log")]
    (println "Storing to s3" s3-object-key file-name)
    (.putObject @aws/s3-client bucket-name s3-object-key (File. file-name))
    (.delete (File. file-name))
    [s3-object-key]))

(def collector
  (fn [{:keys [context results-dir]}]
    (let [bucket-name (:bucket-name context)
          region (:regiion context)
          input-dir (path-join results-dir "input")]
      (create-dir input-dir)
      (aws/create-results-bucket bucket-name region)
      {:collect (partial s3-writer bucket-name input-dir (start-time))
       :combine concat})))
