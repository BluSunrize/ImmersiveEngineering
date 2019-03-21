/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.renderer.GlStateManager.DestFactor.ONE;
import static net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
import static net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA;
import static net.minecraft.client.renderer.GlStateManager.SourceFactor.ZERO;
import static org.lwjgl.opengl.GL11.GL_QUADS;

public class IENixieFontRender extends FontRenderer
{
	ResourceLocation tubeOverlay = new ResourceLocation("immersiveengineering:textures/misc/nixie_tube.png");
	public float c_red;
	public float c_green;
	public float c_blue;
	public float c_alpha;
	public boolean drawTube = true;

	public IENixieFontRender()
	{
		super(ClientUtils.mc().gameSettings, new ResourceLocation("immersiveengineering:textures/misc/nixie_ascii.png"), ClientUtils.mc().renderEngine, false);
		if(Minecraft.getMinecraft().gameSettings.language!=null)
		{
			this.setUnicodeFlag(ClientUtils.mc().getLanguageManager().isCurrentLocaleUnicode());
			this.setBidiFlag(ClientUtils.mc().getLanguageManager().isCurrentLanguageBidirectional());
		}
		((IReloadableResourceManager)ClientUtils.mc().getResourceManager()).registerReloadListener(this);
		this.FONT_HEIGHT = 12;
	}

	@Override
	public int drawString(String s, float x, float y, int colour, boolean shadow)
	{
		shadow = false;
		return super.drawString(s, x, y, colour, shadow);
	}

	@Override
	public int getCharWidth(char ic)
	{
		if(ic==167)
		{
			return -1;
		}
		else if(ic==32)
		{
			return 4;
		}
		else
		{
			int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(ic);
			if(ic > 0&&i!=-1&&!this.getUnicodeFlag())
				return 10;
			else if(this.glyphWidth[ic]!=0)
				return 6;
			else
				return 0;
		}
	}

	@Override
	protected void setColor(float r, float g, float b, float a)
	{
		this.c_red = r;
		this.c_green = g;
		this.c_blue = b;
		this.c_alpha = a;
		GlStateManager.color(r, g, b, a);
	}

	@Override
	protected float renderDefaultChar(int ic, boolean italic)
	{
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ZERO, ONE);
		bindTexture(this.locationFontTexture);

		float italicOffset = italic?1.0F: 0.0F;

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder bb = tes.getBuffer();
		drawBackground(bb, tes, italicOffset);

		super.renderDefaultChar(ic, italic);

		GlStateManager.color(c_red*.875f, c_green*.875f, c_blue*.875f, c_alpha*.375f);
		this.posX -= .5f;
		this.posY -= .5f;
		super.renderDefaultChar(ic, italic);
		this.posX += 1;
		this.posY += 1;
		super.renderDefaultChar(ic, italic);
		this.posX -= .5f;
		this.posY -= .5f;

		if(this.drawTube)
			drawTube(bb, tes, italicOffset);
		GlStateManager.color(c_red, c_green, c_blue, c_alpha);

		return 10;
	}

	private void drawBackground(BufferBuilder bb, Tessellator tes, float italicOffset)
	{
		bb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bb.pos(this.posX+italicOffset, this.posY, 0.0F).
				tex(0, .125f).endVertex();
		bb.pos(this.posX-italicOffset, this.posY+7.99F, 0.0F).
				tex(0, .1874f).endVertex();
		bb.pos(this.posX+9-1-italicOffset, this.posY+7.99F, 0.0F).
				tex(.0625f, .1874f).endVertex();
		bb.pos(this.posX+9-1+italicOffset, this.posY, 0.0F).
				tex(.0625f, .125f).endVertex();
		tes.draw();
	}

	private void drawTube(BufferBuilder bb, Tessellator tes, float italicOffset)
	{
		GlStateManager.color(1, 1, 1, c_alpha);
		bindTexture(this.tubeOverlay);
		bb.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bb.pos(this.posX-1+italicOffset, this.posY-3, 0.0F).
				tex(0, 0).endVertex();
		bb.pos(this.posX-1-italicOffset, this.posY+10.99F, 0.0F).
				tex(0, .874f).endVertex();
		bb.pos(this.posX+9-italicOffset, this.posY+10.99F, 0.0F).
				tex(.625f, .874f).endVertex();
		bb.pos(this.posX+9+italicOffset, this.posY-3, 0.0F).
				tex(.625f, 0).endVertex();
		tes.draw();
	}

	@Override
	protected float renderUnicodeChar(char ic, boolean italic)
	{
		GlStateManager.enableBlend();
		OpenGlHelper.glBlendFunc(770, 771, 0, 1);
		bindTexture(this.locationFontTexture);

		float italicOffset = italic?1.0F: 0.0F;

		Tessellator tes = Tessellator.getInstance();
		BufferBuilder bb = tes.getBuffer();
		drawBackground(bb, tes, italicOffset);

		super.renderUnicodeChar(ic, italic);

		GlStateManager.color(c_red*.875f, c_green*.875f, c_blue*.875f, c_alpha*.375f);
		this.posX -= .5f;
		this.posY -= .5f;
		super.renderUnicodeChar(ic, italic);
		this.posX += 1;
		this.posY += 1;
		super.renderUnicodeChar(ic, italic);
		this.posX -= .5f;
		this.posY -= .5f;

		if(this.drawTube)
			drawTube(bb, tes, italicOffset);
		GlStateManager.color(c_red, c_green, c_blue, c_alpha);
		return 6;
	}

	public void setDrawTubeFlag(boolean flag)
	{
		this.drawTube = flag;
	}
}