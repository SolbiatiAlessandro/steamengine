(ns steamengine.test-runner
  (:require [doo.runner :refer-macros [doo-tests] ]
             [cljs.test :refer-macros [run-tests]]
            [steamengine.core-test]))

;; This isn't strictly necessary, but is a good idea depending
;; upon your application's ultimate runtime engine.
(enable-console-print!)

(doo-tests 'steamengine.core-test)
