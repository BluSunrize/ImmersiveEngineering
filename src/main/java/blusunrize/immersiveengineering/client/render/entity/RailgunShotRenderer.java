/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunRenderColors;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.StandardRailgunProjectile;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class RailgunShotRenderer extends EntityRenderer<RailgunShotEntity>
{
	private static final RailgunRenderColors DEFAULT_RENDER_COLORS = new RailgunRenderColors(
			new int[]{0x686868, 0xa4a4a4, 0xa4a4a4, 0xa4a4a4, 0x686868}
	);

	public RailgunShotRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void doRender(RailgunShotEntity entity, double x, double y, double z, float f0, float f1)
	{
		double yaw = entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*f1-90.0F;
		double pitch = entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*f1;

		ItemStack ammo = entity.getAmmo();
		RailgunRenderColors colors = DEFAULT_RENDER_COLORS;
		if(!ammo.isEmpty())
		{
			RailgunHandler.IRailgunProjectile prop = RailgunHandler.getProjectile(ammo);
			if(prop instanceof RailgunHandler.StandardRailgunProjectile)
				colors = ((StandardRailgunProjectile)prop).getColorMap();
		}
		renderRailgunProjectile(x, y, z, yaw, pitch, colors);
	}

	public static void renderRailgunProjectile(double x, double y, double z, double yaw, double pitch, RailgunRenderColors colors)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.enableRescaleNormal();
		Tessellator tes = ClientUtils.tes();
		BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();

		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);

		GlStateManager.disableCull();
		GlStateManager.rotatef((float)yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef((float)pitch, 0.0F, 0.0F, 1.0F);

		GlStateManager.scalef(.25f, .25f, .25f);

		float height = .1875f;
		float halfWidth = height/2;
		float length = 2;
		int colWidth = colors.getGradientLength();
		int colLength = colors.getRingCount();
		float widthStep = height/colWidth;
		float lengthStep = length/colLength;

		GlStateManager.translated(-length*.85f, 0, 0);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		int[] rgb;
		//Front&Back
		GlStateManager.color3f(1, 1, 1);
		for(int i = 0; i < colWidth; i++)
		{
			rgb = colors.getFrontColor(i);
			worldrenderer.pos(0, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(0, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(0, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(0, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getBackColor(i);
			worldrenderer.pos(length, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(length, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(length, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(length, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Sides
		for(int i = 0; i < colLength; i++)
		{
			rgb = colors.getRingColor(i, 0);
			worldrenderer.pos(lengthStep*i, 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*i, height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getRingColor(i, colWidth-1);
			worldrenderer.pos(lengthStep*i, height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*i, 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Top&Bottom
		for(int i = 0; i < colLength; i++)
			for(int j = 0; j < colWidth; j++)
			{
				rgb = colors.getRingColor(i, j);
				worldrenderer.pos(lengthStep*(i+1), height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*i, height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*i, height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

				worldrenderer.pos(lengthStep*i, 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				worldrenderer.pos(lengthStep*i, 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			}
		tes.draw();

		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();

		GlStateManager.enableCull();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}


	@Override
	protected ResourceLocation getEntityTexture(@Nonnull RailgunShotEntity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/white.png");
	}

}
