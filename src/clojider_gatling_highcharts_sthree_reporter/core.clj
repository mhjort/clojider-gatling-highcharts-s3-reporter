(ns clojider-gatling-highcharts-sthree-reporter.core)

(def reporter
  {:reporter-key :highcharts-s3
   :collector 'clojider-gatling-highcharts-sthree-reporter.collector/collector
   :generator 'clojider-gatling-highcharts-sthree-reporter.generator/generator})
