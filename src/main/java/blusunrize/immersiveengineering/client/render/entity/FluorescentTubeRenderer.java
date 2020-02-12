/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.entities.FluorescentTubeEntity;
import blusunrize.immersiveengineering.common.items.FluorescentTubeItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class FluorescentTubeRenderer extends EntityRenderer<FluorescentTubeEntity>
{
	static double sqrt2Half = Math.sqrt(2)/2;
	public static final double[][] octagon = {
			{1, 0}, {sqrt2Half, sqrt2Half}, {0, 1}, {-sqrt2Half, sqrt2Half},
			{-1, 0}, {-sqrt2Half, -sqrt2Half}, {0, -1}, {sqrt2Half, -sqrt2Half}
	};
	TextureAtlasSprite tex;

	public FluorescentTubeRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
		shadowOpaque = 0;
		shadowSize = 0;
	}

	@Override
	protected ResourceLocation getEntityTexture(FluorescentTubeEntity entity)
	{
		return null;
	}

	@Override
	public void doRender(FluorescentTubeEntity entity, double x, double y, double z, float entityYaw,
						 float partialTicks)
	{
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder wr = tes.getBuffer();
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y+1, z);
		GlStateManager.rotatef(entityYaw+90, 0, 1, 0);
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, .03125);
		GlStateManager.rotatef(entity.angleHorizontal, 1, 0, 0);
		GlStateManager.translated(0, -entity.TUBE_LENGTH/2, 0);
		drawTube(entity.active, entity.rgb);
		GlStateManager.popMatrix();
		GlStateManager.translated(-0.25, -1, 0);
		GlStateManager.color3f(1, 1, 1);
		if(tex==null)
			tex = Minecraft.getInstance().getTextureMap().getAtlasSprite("minecraft:block/iron_block");

		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		ClientUtils.renderTexturedBox(wr, 0, 0, 0, .0625, 1, .0625, tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());
		ClientUtils.renderTexturedBox(wr, .0625, .9375, 0, .25, 1, .0625, tex.getMinU(), tex.getMinV(), tex.getMaxU(), tex.getMaxV());
		tes.draw();

		GlStateManager.popMatrix();
	}

	private static ItemStack tube = ItemStack.EMPTY;
	private static ItemStack tubeActive = ItemStack.EMPTY;

	static void drawTube(boolean active, float[] rgb)
	{
		if(tube.isEmpty())
			tube = new ItemStack(Misc.fluorescentTube);
		if(tubeActive.isEmpty())
		{
			tubeActive = new ItemStack(Misc.fluorescentTube);
			FluorescentTubeItem.setLit(tubeActive, 1);
		}
		GlStateManager.translated(-.5, .25, -.5);
		ItemStack renderStack = active?tubeActive: tube;
		FluorescentTubeItem.setRGB(renderStack, rgb);
		IEOBJItemRenderer.INSTANCE.renderByItem(renderStack);
	}
}
