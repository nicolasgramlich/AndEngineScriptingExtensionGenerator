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
-class org.andengine.engine.handler.IUpdateHandler
-class org.andengine.engine.handler.IDrawHandler
-class org.andengine.util.IDisposable
-class org.andengine.util.IMatcher
-class org.andengine.entity.Entity
-class org.andengine.entity.IEntity
-class org.andengine.entity.IEntityMatcher
-class org.andengine.entity.primitive.Rectangle
-class org.andengine.entity.sprite.Sprite
-class org.andengine.entity.shape.Shape
```