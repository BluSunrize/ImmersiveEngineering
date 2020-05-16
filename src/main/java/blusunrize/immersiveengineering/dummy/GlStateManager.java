package blusunrize.immersiveengineering.dummy;

/**
 * This class needs to be removed once porting is complete! Its only purpose is to reach a compiling/launching state
 * earlier (and thereby allow rendering changes to be tested), by providing stubs for all methods in GlStateManager
 * used by IE
 */
public class GlStateManager
{
	public static void enableRescaleNormal()
	{
		throw new UnsupportedOperationException();
	}

	public static void pushMatrix()
	{
		throw new UnsupportedOperationException();
	}

	public static void translatef(float x, float y, float z)
	{
		throw new UnsupportedOperationException();
	}

	public static void translated(double x, double y, double z)
	{
		throw new UnsupportedOperationException();
	}

	public static void scalef(float x, float y, float z)
	{
		throw new UnsupportedOperationException();
	}

	public static void scaled(double x, double y, double z)
	{
		throw new UnsupportedOperationException();
	}

	public static void rotated(double angle, double x, double y, double z)
	{
		throw new UnsupportedOperationException();
	}

	public static void disableLighting()
	{
		throw new UnsupportedOperationException();
	}

	public static void shadeModel(int model)
	{
		throw new UnsupportedOperationException();
	}

	public static void color3f(float r, float g, float b)
	{
		throw new UnsupportedOperationException();
	}

	public static void popMatrix()
	{
		throw new UnsupportedOperationException();
	}

	public static void disableRescaleNormal()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableBlend()
	{
		throw new UnsupportedOperationException();
	}

	public static void lineWidth(float v)
	{
		throw new UnsupportedOperationException();
	}

	public static void blendFuncSeparate(SourceFactor srcAlpha, DestFactor oneMinusSrcAlpha, SourceFactor one, DestFactor zero)
	{
		throw new UnsupportedOperationException();
	}

	public static void blendFunc(int i, int i1)
	{
		throw new UnsupportedOperationException();
	}

	public static void color4f(float r, float g, float b, float a)
	{
		throw new UnsupportedOperationException();
	}

	public static void blendFuncSeparate(int i, int i1, int i2, int i3)
	{
		throw new UnsupportedOperationException();
	}

	public static void disableDepthTest()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableDepthTest()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableLighting()
	{
		throw new UnsupportedOperationException();
	}

	public static void disableBlend()
	{
		throw new UnsupportedOperationException();
	}

	public static void depthFunc(int glGreater)
	{
		throw new UnsupportedOperationException();
	}

	public static void rotatef(float deg, float x, float y, float t)
	{
		throw new UnsupportedOperationException();
	}

	public static void disableCull()
	{
		throw new UnsupportedOperationException();
	}

	public static void colorMask(boolean b, boolean b1, boolean b2, boolean b3)
	{
		throw new UnsupportedOperationException();
	}

	public static void depthMask(boolean b)
	{
		throw new UnsupportedOperationException();
	}

	public static void clear(int glStencilBufferBit, boolean b)
	{
		throw new UnsupportedOperationException();
	}

	public static void disableTexture()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableTexture()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableCull()
	{
		throw new UnsupportedOperationException();
	}

	public static void enableAlphaTest()
	{
		throw new UnsupportedOperationException();
	}

	public static void disableAlphaTest()
	{
		throw new UnsupportedOperationException();
	}

	public static void blendFunc(SourceFactor srcAlpha, DestFactor oneMinusSrcAlpha)
	{
		throw new UnsupportedOperationException();
	}

	public static void alphaFunc(int i, float v)
	{
		throw new UnsupportedOperationException();
	}

	public static void pushLightingAttributes()
	{
		throw new UnsupportedOperationException();
	}

	public static void popAttributes()
	{
		throw new UnsupportedOperationException();
	}

	public static void polygonOffset(float v, float v1)
	{
		throw new UnsupportedOperationException();
	}

	public static void enablePolygonOffset()
	{
		throw new UnsupportedOperationException();
	}

	public static void disablePolygonOffset()
	{
		throw new UnsupportedOperationException();
	}

	public static void fogStart(float v)
	{
		throw new UnsupportedOperationException();
	}

	public static void fogMode(FogMode linear)
	{
		throw new UnsupportedOperationException();
	}

	public static void fogEnd(float f1)
	{
		throw new UnsupportedOperationException();
	}

	public static void fogDensity(float v)
	{
		throw new UnsupportedOperationException();
	}

	public static void fogi(int i, int i1)
	{
		throw new UnsupportedOperationException();
	}

	public static void activeTexture(String gl_texture1)
	{
		throw new UnsupportedOperationException();
	}

	public enum SourceFactor
	{
		SRC_ALPHA,
		ONE
	}

	public enum DestFactor
	{
		ONE_MINUS_SRC_ALPHA,
		ZERO
	}

	public enum FogMode
	{
		LINEAR
	}
}
