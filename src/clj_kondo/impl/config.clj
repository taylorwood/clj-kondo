(ns clj-kondo.impl.config
  {:no-doc true}
  (:require [clj-kondo.impl.profiler :as profiler]
            [clj-kondo.impl.utils :refer [vconj deep-merge]]))

(def default-config
  '{;; no linting inside calls to these functions/macros
    ;; note that you will still get an arity error when calling the fn/macro itself incorrectly
    :skip-args [#_clojure.core/comment #_cljs.core/comment]
    :skip-comments false ;; convient shorthand for :skip-args [clojure.core/comment cljs.core/comment]
    ;; linter level can be tweaked by setting :level to :error, :warn or :info (or any other keyword)
    ;; all linters are enabled by default, but can be turned off by setting :level to :off.
    :linters {:invalid-arity {:level :error
                              :skip-args [#_riemann.test/test-stream]}
              :private-call {:level :error}
              :inline-def {:level :warning}
              :redundant-do {:level :warning}
              :redundant-let {:level :warning}
              :cond-without-else {:level :warning}
              :missing-test-assertion {:level :warning}
              :duplicate-map-key {:level :error}
              :duplicate-set-key {:level :error}
              :missing-map-value {:level :error}
              :invalid-bindings {:level :error}
              :unused-namespace {:level :warning
                                 ;; don't warn about these namespaces:
                                 :exclude [#_clj-kondo.impl.var-info-gen]
                                 }}
    :lint-as {cats.core/->= clojure.core/->
              cats.core/->>= clojure.core/->>
              rewrite-clj.custom-zipper.core/defn-switchable clojure.core/defn
              clojure.core.async/go-loop clojure.core/loop
              cljs.core.async/go-loop clojure.core/loop
              cljs.core.async.macros/go-loop clojure.core/loop}
    :output {;; set to truthy to print progress while linting
             :show-progress false
             ;; output can be filtered and removed by regex on filename. empty options leave the output untouched.
             :include-files [] #_["^src" "^test"]
             :exclude-files [] #_["^cljs/core"]
             ;; the output pattern can be altered using a template. use {{LEVEL}} to print the level in capitals.
             ;; the default template looks like this:
             ;; :pattern "{{filename}}:{{row}}:{{col}}: {{level}}: {{message}}"
             }})

(def config (atom default-config))

(defn merge-config! [cfg]
  (profiler/profile
   :merge-config
   (let [cfg (cond-> cfg
               (:skip-comments cfg)
               (-> (update :skip-args vconj 'clojure.core/comment 'cljs.core/comment)))]
     (swap! config deep-merge cfg))))

(defn fq-syms->vecs [fq-syms]
  (map (fn [fq-sym]
         [(symbol (namespace fq-sym)) (symbol (name fq-sym))])
       fq-syms))

(defn skip-args*
  ([]
   (fq-syms->vecs (get @config :skip-args)))
  ([linter]
   (fq-syms->vecs (get-in @config [:linters linter :skip-args]))))

(def skip-args (memoize skip-args*))

(defn skip?
  "we optimize for the case that disable-within returns an empty sequence"
  ([parents]
   (profiler/profile
    :disabled?
    (when-let [disabled (seq (skip-args))]
      (some (fn [disabled-sym]
              (some #(= disabled-sym %) parents))
            disabled))))
  ([linter parents]
   (profiler/profile
    :disabled?
    (when-let [disabled (seq (skip-args linter))]
      (some (fn [disabled-sym]
              (some #(= disabled-sym %) parents))
            disabled)))))

(defn lint-as-config* []
  (let [m (get @config :lint-as)]
    (zipmap (fq-syms->vecs (keys m))
            (fq-syms->vecs (vals m)))))

(def lint-as-config (memoize lint-as-config*))

(defn lint-as [v] (get (lint-as-config) v))

(defn unused-namespace-excluded* []
  (set (get-in @config [:linters :unused-namespace :exclude])))

(def unused-namespace-excluded (memoize unused-namespace-excluded*))

;;;; Scratch

(comment
  (run! merge-config! [default-config (clojure.edn/read-string (slurp ".clj-kondo/config.edn"))])
  @config
  (unused-namespace-excluded)
  )
