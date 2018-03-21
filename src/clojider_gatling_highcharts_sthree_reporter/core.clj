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

(def reporter
  {:reporter-key :highcharts-s3
   :collector 'clojider-gatling-highcharts-sthree-reporter.collector/collector
   :generator 'clojider-gatling-highcharts-sthree-reporter.generator/generator})
