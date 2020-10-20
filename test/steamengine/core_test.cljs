(ns steamengine.core-test
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    ;;[steamengine.core] to import core we need to run test not on node but somewhere with headless browser
    ))

(deftest test-fill-matrix
  (is (= 1 1)))
