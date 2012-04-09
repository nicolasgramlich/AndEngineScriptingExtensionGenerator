#!/bin/bash

ANDENGINE_SCRIPTINGEXTENSION_GENERATOR_ROOT="/Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngineScriptingExtensionGenerator/"
ANDENGINE_ROOT="/Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngine/"
ANDENGINE_SCRIPTINGEXTENSION_ROOT="/Users/ngramlich/Workspace/gdk/graphic_engines/AndEngine/AndEngineScriptingExtension/"


pushd ${ANDENGINE_SCRIPTINGEXTENSION_GENERATOR_ROOT}bin > /dev/null

####################################
# Generate bindings:
####################################
echo "####################################"
echo "# Generating bindings ..."

java \
	-cp '.:../lib/*' \
	org/andengine/extension/scripting/generator/AndEngineScriptingExtensionGenerator \
		-in-java-root ${ANDENGINE_ROOT}src \
		-in-javabin-root ${ANDENGINE_ROOT}bin \
		-gen-java-root ${ANDENGINE_SCRIPTINGEXTENSION_ROOT}src \
		-gen-cpp-root ${ANDENGINE_SCRIPTINGEXTENSION_ROOT}jni/src \
		-gen-java-formatter jalopy \
		-gen-java-class-suffix Proxy \
		-gen-class-exclude android.hardware.SensorEventListener \
		-gen-class-exclude android.view.View\$OnTouchListener \
		-gen-class-exclude org.andengine.input.touch.controller.ITouchEventCallback \
		-gen-class-exclude android.location.LocationListener \
		-gen-method-include getX \
		-gen-method-include getY \
		-gen-method-include setX \
		-gen-method-include setY \
		-gen-method-include getScaleX \
		-gen-method-include getScaleY \
		-gen-method-include setScale \
		-gen-method-include getRotation \
		-gen-method-include setRotation \
		-gen-method-include getSkewX \
		-gen-method-include getSkewY \
		-gen-method-include setSkew \
		-gen-method-include attachChild \
		-gen-method-include detachChild \
		-gen-method-include onAttached \
		-gen-method-include onDetached \
		-gen-method-include onAreaTouched \
		-gen-method-include getVertexBufferObjectManager \
		-gen-method-include getTextureManager \
		-gen-method-include getFontManager \
		-gen-method-include load \
		-gen-method-include unload \
		-gen-method-include getWidth \
		-gen-method-include getHeight \
		-class org.andengine.engine.Engine \
		-class org.andengine.engine.options.EngineOptions \
		-class org.andengine.engine.options.ScreenOrientation \
		-class org.andengine.engine.options.resolutionpolicy.IResolutionPolicy \
		-class org.andengine.engine.handler.IUpdateHandler \
		-class org.andengine.engine.handler.IDrawHandler \
		-class org.andengine.engine.camera.Camera \
		-class org.andengine.engine.camera.hud.HUD \
		-class org.andengine.entity.scene.ITouchArea \
		-class org.andengine.entity.scene.IOnAreaTouchListener \
		-class org.andengine.entity.scene.IOnSceneTouchListener \
		-class org.andengine.input.touch.TouchEvent \
		-class org.andengine.util.adt.transformation.Transformation \
		-class org.andengine.util.color.Color \
		-class org.andengine.util.IDisposable \
		-class org.andengine.util.IMatcher \
		-class org.andengine.opengl.util.GLState \
		-class org.andengine.opengl.shader.ShaderProgram \
		-class org.andengine.opengl.shader.source.IShaderSource \
		-class org.andengine.opengl.vbo.IVertexBufferObject \
		-class org.andengine.opengl.vbo.VertexBufferObjectManager \
		-class org.andengine.opengl.vbo.DrawType \
		-class org.andengine.opengl.texture.TextureManager \
		-class org.andengine.opengl.texture.ITexture \
		-class org.andengine.opengl.texture.Texture \
		-class org.andengine.opengl.texture.ITextureStateListener \
		-class org.andengine.opengl.texture.TextureOptions \
		-class org.andengine.opengl.texture.PixelFormat \
		-class org.andengine.opengl.texture.bitmap.BitmapTexture \
		-class org.andengine.opengl.texture.bitmap.BitmapTextureFormat \
		-class org.andengine.util.adt.io.in.IInputStreamOpener \
		-class org.andengine.util.adt.io.in.AssetInputStreamOpener \
		-class org.andengine.opengl.texture.bitmap.AssetBitmapTexture \
		-class org.andengine.opengl.texture.region.ITextureRegion \
		-class org.andengine.opengl.texture.region.BaseTextureRegion \
		-class org.andengine.opengl.texture.region.TextureRegion \
		-class org.andengine.opengl.font.FontManager \
		-class org.andengine.entity.Entity \
		-class org.andengine.entity.IEntity \
		-class org.andengine.entity.IEntityMatcher \
		-class org.andengine.entity.scene.background.IBackground \
		-class org.andengine.entity.primitive.Rectangle \
		-class org.andengine.entity.primitive.vbo.IRectangleVertexBufferObject \
		-class org.andengine.entity.primitive.Line \
		-class org.andengine.entity.primitive.vbo.ILineVertexBufferObject \
		-class org.andengine.entity.scene.Scene \
		-class org.andengine.entity.scene.CameraScene \
		-class org.andengine.entity.sprite.Sprite \
		-class org.andengine.entity.sprite.vbo.ISpriteVertexBufferObject \
		-class org.andengine.entity.shape.IShape \
		-class org.andengine.entity.shape.Shape \
		-class org.andengine.entity.shape.IAreaShape \
		-class org.andengine.entity.shape.RectangularShape

echo "# Done."
echo "####################################"

popd > /dev/null



pushd ${ANDENGINE_SCRIPTINGEXTENSION_ROOT} > /dev/null

####################################
# Inject forward declarations:
####################################
echo "####################################"
echo "# Injecting forward declarations ..."


echo -n "Injecting into: src/org/andengine/engine/Engine.h ..."
sed -i -e '/class Engine/ i\
	class VertexBufferObjectManager; // Forward declaration\
	class FontManager; // Forward declaration\
	class TextureManager; // Forward declaration\
	\
	' jni/src/org/andengine/engine/Engine.h
echo " done!"


echo "# Done."
echo "####################################"


####################################
# Fix other stuff:
####################################
echo "####################################"
echo "# Fix other stuff ..."


echo -n "Fixing 'new IEntity(..) => new Entity(..)' ..."
find ./jni -type f -iname "*.cpp" -exec sed -i "" 's/new IEntity/new Entity/' {} \;
echo " done!"


echo "# Done."
echo "####################################"

popd > /dev/null