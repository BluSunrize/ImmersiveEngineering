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
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class RailgunShotRenderer extends EntityRenderer<RailgunShotEntity>
{
	private static final RailgunRenderColors DEFAULT_RENDER_COLORS = new RailgunRenderColors(
			0x686868, 0xa4a4a4, 0xa4a4a4, 0xa4a4a4, 0x686868
	);

	public RailgunShotRenderer(Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(RailgunShotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		double yaw = entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks-90.0F;
		double pitch = entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks;

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
											   PoseStack matrixStackIn, MultiBufferSource bufferIn, int light)
	{
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(new Quaternionf()
				.rotateY((float)Math.toRadians(yaw))
				.rotateZ((float) Math.toRadians(pitch))
		);

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
				matrixStackIn,
				IERenderTypes.POSITION_COLOR_LIGHTMAP.format()
		);
		builder.setLight(light);
		int[] rgb;
		//Front&Back
		for(int i = 0; i < colWidth; i++)
		{
			rgb = colors.getFrontColor(i);
			builder.vertex(0, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(0, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(0, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(0, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getBackColor(i);
			builder.vertex(length, 0, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(length, height, -halfWidth+widthStep*i).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(length, height, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(length, 0, -halfWidth+widthStep*(i+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Sides
		for(int i = 0; i < colLength; i++)
		{
			rgb = colors.getRingColor(i, 0);
			builder.vertex(lengthStep*i, 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*i, height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*(i+1), height, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*(i+1), 0, -halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

			rgb = colors.getRingColor(i, colWidth-1);
			builder.vertex(lengthStep*i, height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*i, 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*(i+1), 0, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			builder.vertex(lengthStep*(i+1), height, halfWidth).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
		}
		//Top&Bottom
		for(int i = 0; i < colLength; i++)
			for(int j = 0; j < colWidth; j++)
			{
				rgb = colors.getRingColor(i, j);
				builder.vertex(lengthStep*(i+1), height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*i, height, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*i, height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*(i+1), height, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();

				builder.vertex(lengthStep*i, 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*(i+1), 0, -halfWidth+widthStep*j).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*(i+1), 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
				builder.vertex(lengthStep*i, 0, -halfWidth+widthStep*(j+1)).color(rgb[0], rgb[1], rgb[2], 255).endVertex();
			}

		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(@Nonnull RailgunShotEntity entity)
	{
		return new ResourceLocation("immersiveengineering:textures/models/white.png");
	}

}
