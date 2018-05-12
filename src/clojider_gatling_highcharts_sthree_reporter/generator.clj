(ns clojider-gatling-highcharts-sthree-reporter.generator
  (:require [clojider-gatling-highcharts-reporter.generator :refer [create-chart]]
            [clojider-gatling-highcharts-sthree-reporter.util :refer [create-dir
                                                                      path-join]]
            [clojider-gatling-highcharts-sthree-reporter.aws :as aws])
  (:import [org.joda.time LocalDateTime]
           [java.io File]
           [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]))

(defn- download-logs-and-create-chart [results bucket-name input-dir results-dir]
  (println "Downloading" results "from" bucket-name)
  ;TODO Read these in parallel
  (doseq [result results]
    (aws/download-file input-dir bucket-name result))
  (create-chart results-dir))

(def generator
  (fn [{:keys [context results-dir]}]
    (let [bucket-name (:bucket-name context)
          region (:regiion context)
          input-dir (path-join results-dir "input")]
      (create-dir input-dir)
      (aws/create-results-bucket bucket-name region)
      {:generate (fn [results]
                   (download-logs-and-create-chart results bucket-name input-dir results-dir))
       :as-str (fn [summary]
                 (str "Open " results-dir "/index.html with your browser to see a detailed report."))})))
