(ns clojider-gatling-highcharts-sthree-reporter.util
  (:require [clojure.java.io :as io])
  (:import [java.io File]))

(defn path-join [& paths]
  (.getCanonicalPath (apply io/file paths)))

(defn create-dir [dir]
  (.mkdirs (File. dir)))
