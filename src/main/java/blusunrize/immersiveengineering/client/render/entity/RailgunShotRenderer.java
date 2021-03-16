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
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

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
	public void render(RailgunShotEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		double yaw = entity.prevRotationYaw+(entity.rotationYaw-entity.prevRotationYaw)*partialTicks-90.0F;
		double pitch = entity.prevRotationPitch+(entity.rotationPitch-entity.prevRotationPitch)*partialTicks;

		ItemStack ammo = entity.getAmmo();
		RailgunRenderColors colors = DEFAULT_RENDER_COLORS;
		if(!ammo.isEmpty())
		{
			RailgunHandler.IRailgunProjectile prop = RailgunHandler.getProjectile(ammo);
			if(prop instanceof RailgunHandler.StandardRailgunProjectile&&((StandardRailgunProjectile)prop).getColorMap()!=null)
				colors = ((StandardRailgunProjectile)prop).getColorMap();
		}

		renderRailgunProjectile(yaw, pitch, colors, matrixStackIn, bufferIn, packedLightIn);
	}

	public static void renderRailgunProjectile(double yaw, double pitch, RailgunRenderColors colors,
											   MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int light)
	{
		matrixStackIn.push();
		matrixStackIn.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float)yaw, true));
		matrixStackIn.rotate(new Quaternion(new Vector3f(0.0F, 0.0F, 1.0F), (float)pitch, true));

		matrixStackIn.scale(.25f, .25f, .25f);

		float height = .1875f;
		float halfWidth = height/2;
		float length = 2;
		int colWidth = colors.getGradientLength();
		int colLength = colors.getRingCount();
		float widthStep = height/colWidth;
		float lengthStep = length/colLength;

		matrixStackIn.translate(-length*.85f, 0, 0);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(
				bufferIn.getBuffer(IERenderTypes.POSITION_COLOR_LIGHTMAP),
				matrixStackIn
		);
		builder.setLight(light);
		int[] rgb;
		//Front&Back
		for(int i = 0; i < colWidth; i++)
		{
			rgb = colors.getFrontColor(i);
			builder.pos(0, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(0, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(0, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(0, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getBackColor(i);
			builder.pos(length, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(length, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(length, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(length, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Sides
		for(int i = 0; i < colLength; i++)
		{
			rgb = colors.getRingColor(i, 0);
			builder.pos(lengthStep*i, 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*i, height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*(i+1), height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*(i+1), 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getRingColor(i, colWidth-1);
			builder.pos(lengthStep*i, height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*i, 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*(i+1), 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.pos(lengthStep*(i+1), height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Top&Bottom
		for(int i = 0; i < colLength; i++)
			for(int j = 0; j < colWidth; j++)
			{
				rgb = colors.getRingColor(i, j);
				builder.pos(lengthStep*(i+1), height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*i, height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*i, height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*(i+1), height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

				builder.pos(lengthStep*i, 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*(i+1), 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.pos(lengthStep*i, 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			}

		matrixStackIn.pop();
	}

	@Override
	public ResourceLocation getEntityTexture(@Nonnull RailgunShotEntity entity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/white.png");
	}

}
