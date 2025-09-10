(ns util.shader
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl.system MemoryStack]))

(set! *warn-on-reflection* true)

(defn compile-shader
  [shader-type ^String source]
  (let [shader (GL33/glCreateShader shader-type)]
    (GL33/glShaderSource shader source)
    (GL33/glCompileShader shader)
    (let [success (GL33/glGetShaderi shader GL33/GL_COMPILE_STATUS)]
      (when (= success 0)
        (println "ERROR: shader compilation failed:")
        (println (GL33/glGetShaderInfoLog shader))))
    shader))

(defn link-shader-program
  [& shader]
  (let [program (GL33/glCreateProgram)]
    (dorun (map #(GL33/glAttachShader program %) shader))
    (GL33/glLinkProgram program)
    (let [success (GL33/glGetProgrami program GL33/GL_LINK_STATUS)]
      (when (= success 0)
        (println "ERROR: shader program linking failed:")
        (println (GL33/glGetProgramInfoLog program))))
    program))

(defn create-shader-program
  [vertex-shader-source
   fragment-shader-source]
  (let [vertex-shader (compile-shader GL33/GL_VERTEX_SHADER vertex-shader-source)
        fragment-shader (compile-shader GL33/GL_FRAGMENT_SHADER fragment-shader-source)
        shader-program (link-shader-program vertex-shader fragment-shader)]
    (GL33/glDeleteShader vertex-shader)
    (GL33/glDeleteShader fragment-shader)
    shader-program))

(defn load-matrix
  [shader-program location-id ^org.joml.Matrix4f matrix]
  (with-open [stack (MemoryStack/stackPush)]
    (GL33/glUniformMatrix4fv
      (GL33/glGetUniformLocation ^int shader-program ^String location-id)
      false
      (.get matrix (.mallocFloat stack 16)))))

(defn load-vector3
  [shader-program location-id ^org.joml.Vector3f vector3]
  (GL33/glUniform3f
    (GL33/glGetUniformLocation ^int shader-program ^String location-id)
    (.x vector3)
    (.y vector3)
    (.z vector3)))

(defn load-float3
  [shader-program location-id x y z]
  (GL33/glUniform3f
    (GL33/glGetUniformLocation ^int shader-program ^String location-id)
    (float x)
    (float y)
    (float z)))

(defn load-float1
  [shader-program location-id value]
  (GL33/glUniform1f
    (GL33/glGetUniformLocation ^int shader-program ^String location-id)
    (float value)))

(defn load-int
  [shader-program location-id value]
  (GL33/glUniform1i
    (GL33/glGetUniformLocation ^int shader-program ^String location-id)
    (int value)))
