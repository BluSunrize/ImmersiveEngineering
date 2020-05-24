/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.font;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class NixieFontRender extends IEFontRender
{
	private static final ResourceLocation TUBE_OVERLAY = new ResourceLocation(ImmersiveEngineering.MODID,
			"textures/misc/nixie_tube.png");
	private static final float BACKGROUND_HEIGHT = 7.99F;
	private static final float BACKGROUND_WIDTH = 8;
	private static final float NIXIE_HEIGHT = 13.99F;
	private static final float NIXIE_WIDTH = 10;
	private static final float NIXIE_Y_OFFSET = -3;
	private static final float NIXIE_X_OFFSET = -1;

	public boolean drawTube = true;

	public NixieFontRender(boolean unicode, ResourceLocation id)
	{
		super(unicode, id);
	}

	@Override
	public float getCharWidthIE(char character, boolean bold)
	{
		//TODO maybe try something less absurd?
		int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"
				.indexOf(character);
		if(character > 0&&i!=-1)
			return 10;
		else if(super.getCharWidthIE(character, bold) > 0)
			return 6;
		else
			return 0;
	}

	@Override
	protected void renderGlyph(TexturedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y,
							   BufferBuilder bufferBuilder, float red, float green, float blue, float alpha, char orig)
	{
		float baseCharWidth = super.getCharWidthIE(orig, bold);
		x = (float)Math.floor(x+NIXIE_X_OFFSET+(NIXIE_WIDTH-baseCharWidth)/2);
		super.renderGlyph(glyph, bold, italic, boldOffset, x, y, bufferBuilder, red, green, blue, alpha, orig);
		final float backgroundFactor = 0.875F;
		final float alphaFactor = 0.375F;
		red *= backgroundFactor;
		green *= backgroundFactor;
		blue *= backgroundFactor;
		alpha *= alphaFactor;
		super.renderGlyph(glyph, bold, italic, boldOffset, x-.5F, y, bufferBuilder, red, green, blue, alpha, orig);
		super.renderGlyph(glyph, bold, italic, boldOffset, x+.5F, y, bufferBuilder, red, green, blue, alpha, orig);
	}

	@Override
	protected void postStringRender(String text, FloatList charPositions, BufferBuilder bb, Tessellator tes, float y)
	{
		super.postStringRender(text, charPositions, bb, tes, y);
		if(this.drawTube)
			for(float x : charPositions)
				drawTube(bb, tes, x, y);
	}

	//TODO pre-draw?
	private void drawBackground(BufferBuilder bb, Tessellator tes, float x, float y)
	{
		bb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x, y, 0.0F).tex(0, .125f).endVertex();
		bb.pos(x, y+BACKGROUND_HEIGHT, 0.0F).tex(0, .1874f).endVertex();
		bb.pos(x+BACKGROUND_WIDTH, y+BACKGROUND_HEIGHT, 0.0F).tex(.0625f, .1874f).endVertex();
		bb.pos(x+BACKGROUND_WIDTH, y, 0.0F).tex(.0625f, .125f).endVertex();
		tes.draw();
	}

	private void drawTube(BufferBuilder bb, Tessellator tes, float x, float y)
	{
		y += NIXIE_Y_OFFSET;
		x += NIXIE_X_OFFSET;
		GlStateManager.color3f(1, 1, 1);
		texManager.bindTexture(TUBE_OVERLAY);
		bb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bb.pos(x, y, 0.0F).tex(0, 0).endVertex();
		bb.pos(x, y+NIXIE_HEIGHT, 0.0F).tex(0, .874f).endVertex();
		bb.pos(x+NIXIE_WIDTH, y+NIXIE_HEIGHT, 0.0F).tex(.625f, .874f).endVertex();
		bb.pos(x+NIXIE_WIDTH, y, 0.0F).tex(.625f, 0).endVertex();
		tes.draw();
	}

	public void setDrawTubeFlag(boolean flag)
	{
		this.drawTube = flag;
	}

	@Override
	public int getFontHeight()
	{
		return 12;
	}
}
