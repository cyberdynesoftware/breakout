(ns util.texture
  (:import [org.lwjgl.opengl GL33]
           [org.lwjgl.stb STBImage]
           [org.lwjgl BufferUtils]))

(set! *warn-on-reflection* true)

(defn load-texture
  [width height channels ^java.nio.ByteBuffer data]
  (assert channels)
  (let [texture (GL33/glGenTextures)]
    (GL33/glBindTexture GL33/GL_TEXTURE_2D texture)
    (GL33/glTexImage2D GL33/GL_TEXTURE_2D
                       0
                       ^int channels
                       ^int width
                       ^int height
                       0
                       ^int channels
                       GL33/GL_UNSIGNED_BYTE
                       data)
    ;(GL33/glGenerateMipmap GL33/GL_TEXTURE_2D)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_S GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_WRAP_T GL33/GL_REPEAT)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MIN_FILTER GL33/GL_LINEAR)
    (GL33/glTexParameteri GL33/GL_TEXTURE_2D GL33/GL_TEXTURE_MAG_FILTER GL33/GL_LINEAR)
    texture))

(defn load-image
  [^String path]
  ;(STBImage/stbi_set_flip_vertically_on_load true)
  (let [width (BufferUtils/createIntBuffer 1)
        height (BufferUtils/createIntBuffer 1)
        channels (BufferUtils/createIntBuffer 1)
        data (STBImage/stbi_load path width height channels 0)
        texture (load-texture (.get width)
                              (.get height)
                              (condp = (.get channels)
                                3 GL33/GL_RGB
                                4 GL33/GL_RGBA
                                nil)
                              data)]
    (STBImage/stbi_image_free data)
    texture))
