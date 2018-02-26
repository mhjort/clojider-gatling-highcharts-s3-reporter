(defproject clojider-gatling-highcharts-s3-reporter "0.1.0"
  :description "Gatling Highcharts AWS S3 Reporter for clj-gatling"
  :url "https://github.com/mhjort/clojider-gatling-highcharts-s3-reporter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.50"]
                 [clojider-gatling-highcharts-reporter "0.2.1"]]
  :profiles {:dev {:dependencies [[clj-containment-matchers "1.0.1"]] }})


