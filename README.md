# AndEngineScriptingExtensionGenerator

## Usage

```
-in-java-root /Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngine/src
-in-javabin-root /Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngine/bin
-gen-java-root /Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngineScriptingExtension/src
-gen-cpp-root /Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngineScriptingExtension/jni/src
-gen-java-formatter jalopy
-gen-java-class-suffix ProxyX
-gen-cpp-class-suffix Proxy
-gen-method-exclude notify
-gen-method-exclude notifyAll
-gen-method-exclude wait
-gen-method-exclude equals
-gen-method-exclude toString
-gen-method-exclude onUpdate
-gen-method-exclude onManagedUpdate
-gen-method-exclude onDraw
-class org.andengine.entity.Entity
-class org.andengine.entity.primitive.Rectangle
-class org.andengine.entity.sprite.Sprite
```