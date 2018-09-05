(ns cljhash.core
  (:import [com.google.common.hash PrimitiveSink HashFunction Funnel HashCode]
           [com.google.common.base Charsets]
           [clojure.lang MapEntry ISeq Seqable Sequential Associative IMapEntry Named BigInt]
           [java.time Instant]
           [java.util Date UUID]
           [com.google.common.io BaseEncoding]))

(defn hash-obj! [^HashFunction hf ^Funnel funnel obj]
  (.hashObject hf obj funnel))

(defprotocol Hashable
  (hash! [obj ^PrimitiveSink ps]))

(defprotocol Encodable
  (to-hex [_])
  (to-base64 [_]))

(def ^:private cljhash-funnel
  (reify Funnel
    (^void funnel [_ from ^PrimitiveSink into]
      (hash! from into))))

(defn clj-hash [^HashFunction hf obj]
  (hash-obj! hf cljhash-funnel obj))

(extend-protocol Hashable
  nil         (hash! [_ ps] (.putBoolean ps false))
  Boolean     (hash! [^Boolean obj ^PrimitiveSink ps] (.putBoolean ps obj))
  Byte        (hash! [^Byte obj ^PrimitiveSink ps]    (.putByte ps obj))
  Short       (hash! [^Short obj ^PrimitiveSink ps]   (.putShort ps obj))
  Integer     (hash! [^Integer obj ^PrimitiveSink ps] (.putInt ps obj))
  Long        (hash! [^Long obj ^PrimitiveSink ps]    (.putLong ps obj))
  BigInt      (hash! [^BigInt obj ^PrimitiveSink ps]  (.putBytes ps (-> obj .toBigInteger .toByteArray)))
  BigDecimal  (hash! [^BigDecimal obj ^PrimitiveSink ps] (.putString ps (.toEngineeringString obj) Charsets/US_ASCII))
  Float       (hash! [^Float obj ^PrimitiveSink ps]   (.putFloat ps obj))
  Double      (hash! [^Double obj ^PrimitiveSink ps]  (.putDouble ps obj))
  Character   (hash! [^Character obj ^PrimitiveSink ps] (.putChar ps obj))
  String      (hash! [^String obj ^PrimitiveSink ps]  (.putString ps obj Charsets/UTF_8))
  UUID        (hash! [^UUID obj ^PrimitiveSink ps]
                (.putLong ps (.getMostSignificantBits obj))
                (.putLong ps (.getLeastSignificantBits obj)))
  Date        (hash! [^Date obj ^PrimitiveSink ps]    (.putLong ps (.getTime obj)))
  Instant     (hash! [^Instant obj ^PrimitiveSink ps] (.putLong ps (.toEpochMilli obj)))
  Named       (hash! [^Named obj ^PrimitiveSink ps]
                (when-let [ns (.getNamespace obj)]    (.putString ps ns Charsets/UTF_8))
                (.putString ps (.getName obj) Charsets/UTF_8))
  IMapEntry   (hash! [^MapEntry obj ^PrimitiveSink ps] (hash! (.key obj) ps) (hash! (.val obj) ps))
  ISeq        (hash! [^ISeq obj ^PrimitiveSink ps]    (doseq [elem obj] (hash! elem ps)))
  Seqable     (hash! [^Seqable obj ^PrimitiveSink ps] (hash! (.seq obj) ps))
  Sequential  (hash! [^Sequential obj ^PrimitiveSink ps]  (doseq [elem obj] (hash! elem ps)))
  Associative (hash! [^Associative obj ^PrimitiveSink ps] (doseq [entry obj] (hash! ^IMapEntry entry ps))))

(extend-type HashCode
  Encodable
  (to-hex [this] (-> (BaseEncoding/base16) .lowerCase (.encode (.asBytes this))))
  (to-base64 [this] (-> (BaseEncoding/base64) (.encode (.asBytes this)))))
