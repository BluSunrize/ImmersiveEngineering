/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
	public RailgunShotRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(RailgunShotEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		double yaw = entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*partialTicks-90.0F;
		double pitch = entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*partialTicks;

		ItemStack ammo = entity.getAmmo();
		int[][] colourMap = {{0x777777, 0xa4a4a4}};
		if(!ammo.isEmpty())
		{
			RailgunHandler.RailgunProjectileProperties prop = RailgunHandler.getProjectileProperties(ammo);
			colourMap = prop!=null?prop.colourMap: colourMap;
		}

		renderRailgunProjectile(yaw, pitch, colourMap);
	}

	public static void renderRailgunProjectile(double yaw, double pitch, int[][] colourMap)
	{
		GlStateManager.pushMatrix();
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

		if(colourMap.length==1)
		{
			colourMap = new int[][]{colourMap[0], colourMap[0]};
		}

		float height = .1875f;
		float halfWidth = height/2;
		float length = 2;
		int colWidth = colourMap[0].length;
		for(int i = 0; i < colourMap.length; i++)
			colWidth = Math.min(colWidth, colourMap[i].length);
		int colLength = colourMap.length;
		float widthStep = height/colWidth;
		float lengthStep = length/colLength;

		GlStateManager.translated(-length*.85f, 0, 0);
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		int colR;
		int colG;
		int colB;
		//Front&Back
		GlStateManager.color3f(1, 1, 1);
		for(int i = 0; i < colWidth; i++)
		{
			colR = (colourMap[0][i] >> 16)&255;
			colG = (colourMap[0][i] >> 8)&255;
			colB = colourMap[0][i]&255;
			worldrenderer.pos(0, height, -halfWidth+widthStep*i).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(0, 0, -halfWidth+widthStep*i).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(0, 0, -halfWidth+widthStep*(i+1)).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(0, height, -halfWidth+widthStep*(i+1)).color(colR, colG, colB, 255).endVertex();

			colR = colourMap[colLength-1][i] >> 16&255;
			colG = colourMap[colLength-1][i] >> 8&255;
			colB = colourMap[colLength-1][i]&255;
			worldrenderer.pos(length, 0, -halfWidth+widthStep*i).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(length, height, -halfWidth+widthStep*i).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(length, height, -halfWidth+widthStep*(i+1)).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(length, 0, -halfWidth+widthStep*(i+1)).color(colR, colG, colB, 255).endVertex();
		}
		//Sides
		for(int i = 0; i < colLength; i++)
		{
			colR = colourMap[i][0] >> 16&255;
			colG = colourMap[i][0] >> 8&255;
			colB = colourMap[i][0]&255;
			worldrenderer.pos(lengthStep*i, 0, -halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*i, height, -halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), height, -halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth).color(colR, colG, colB, 255).endVertex();

			colR = colourMap[i][colWidth-1] >> 16&255;
			colG = colourMap[i][colWidth-1] >> 8&255;
			colB = colourMap[i][colWidth-1]&255;
			worldrenderer.pos(lengthStep*i, height, halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*i, 0, halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), 0, halfWidth).color(colR, colG, colB, 255).endVertex();
			worldrenderer.pos(lengthStep*(i+1), height, halfWidth).color(colR, colG, colB, 255).endVertex();
		}
		//Top&Bottom
		for(int i = 0; i < colLength; i++)
			for(int j = 0; j < colWidth; j++)
			{
				colR = colourMap[i][j] >> 16&255;
				colG = colourMap[i][j] >> 8&255;
				colB = colourMap[i][j]&255;
				worldrenderer.pos(lengthStep*(i+1), height, -halfWidth+widthStep*j).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*i, height, -halfWidth+widthStep*j).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*i, height, -halfWidth+widthStep*(j+1)).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), height, -halfWidth+widthStep*(j+1)).color(colR, colG, colB, 255).endVertex();

				worldrenderer.pos(lengthStep*i, 0, -halfWidth+widthStep*j).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*j).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*(j+1)).color(colR, colG, colB, 255).endVertex();
				worldrenderer.pos(lengthStep*i, 0, -halfWidth+widthStep*(j+1)).color(colR, colG, colB, 255).endVertex();
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
	public ResourceLocation getEntityTexture(@Nonnull RailgunShotEntity p_110775_1_)
	{
		return new ResourceLocation("immersiveengineering:textures/models/white.png");
	}

}
