# cljhash

A [Guava](https://github.com/google/guava) based library for hashing clojure edn. This library is inspired by [aesahaettr
](https://github.com/aphyr/aesahaettr) but with less ambitious goals and easier spelling. The hash of clojure's edn should be
independent of JVM architecture and Object.hashCode implementations.

## Usage

```clojure
(require '[cljhash.core :as h])
(import com.google.common.hash.Hashing)

(def foo {:a {:b #{1 2 3} :c #uuid"3b896c80-ddfc-4e73-98be-554847c0d5be"}})

(def hash-code (h/clj-hash! (Hashing/sha1) foo))

(h/to-hex hash-code)
=> "ce45e1feca96d8594ac4435810ef2037fbe09146"
(h/to-base64 hash-code)
=> "zkXh/sqW2FlKxENYEO8gN/vgkUY="
(.asBytes hash-code)
=> #object["[B" 0x4610b746 "[B@4610b746"]
(into [] (.asBytes hash-code))
=> [-50 69 -31 -2 -54 -106 -40 89 74 -60 67 88 16 -17 32 55 -5 -32 -111 70]
```

If you want your own serliazation strategy you can implement your own cljhash.core.Hashable protocol and
use cljhash.core.hash-obj! function.

## License

http://unlicense.org/
