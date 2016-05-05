(ns thi-ng-geom-starter.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [thi-ng-geom-starter.core-test]))

(enable-console-print!)

(doo-tests 'thi-ng-geom-starter.core-test)
