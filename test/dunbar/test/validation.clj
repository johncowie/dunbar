

(defn validate [data]
  (validator validations data))

(facts "testing validation framework"
       (validator {:age [mandatory]} {}) => {:errors {[:age] :mandatory}}
       (validator {:age mandatory} {}) => {:errors {[:age] :mandatory}}
       (future-fact
        (validator {:a {:b mandatory :c mandatory}} {}) => {:errors {[:a :b] :mandatory
                                                                     [:a :c] :mandatory}})
       )

(facts "validate add friend form data"
       (facts "firstname"
              (validate {:firstname (string-of-length 51)}) => (has-error? [:firstname] :max-length)
              (validate {:firstname ""}) => (has-error? [:firstname] :min-length)
              (validate {}) => (has-error? [:firstname] :mandatory)
              (validate {:firstname (string-of-length 50)}) =not=> (has-errors? [:firstname])
              (validate {:firstname (string-of-length 1)}) =not=> (has-errors? [:firstname]))
       (facts "lastname"
              (validate {:lastname (string-of-length 51)}) => (has-error? [:lastname] :max-length)
              (validate {:lastname ""}) => (has-error? [:lastname] :min-length)
              (validate {}) => (has-error? [:lastname] :mandatory)
              (validate {:lastname (string-of-length 50)}) =not=> (has-errors? [:lastname])
              (validate {:lastname (string-of-length 1)}) =not=> (has-errors? [:lastname]))
       )
