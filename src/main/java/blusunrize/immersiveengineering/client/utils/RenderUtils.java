/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.mixin.accessors.client.PlayerControllerAccess;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

import java.lang.Math;
import java.util.Collection;
import java.util.List;

public class RenderUtils
{
	// The coordinates for each vertex of a quad
	private static final Vector4f[] quadCoords = Util.make(new Vector4f[4], a -> {
		for(int i = 0; i < a.length; ++i)
			a[i] = new Vector4f();
	});
	// the brighnesses of the surrounding blocks. the first dimension indicates block (1) vs sky (0) light
	// These are used to create different light direction vectors depending on the direction of a quads normal vector.
	private static final int[][] neighbourBrightness = new int[2][6];
	// The light vectors created from neighbourBrightness aren't "normalized" (to length 255), the length needs to be divided by this factor to normalize it.
	// The indices are generated as follows: a 1 bit indicates a positive facing normal, a 0 a negative one. 1=x, 2=y, 4=z
	private static final float[][] normalizationFactors = new float[2][8];

	private static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;
	private static final int VERTEX_SIZE = FORMAT.getIntegerSize();
	private static final int UV_OFFSET = ClientUtils.findTextureOffset(FORMAT);
	private static final int POSITION_OFFSET = ClientUtils.findPositionOffset(FORMAT);

	/**
	 * Renders the given quads. Uses the local and neighbour brightnesses to calculate lighting
	 *
	 * @param quads     the quads to render
	 * @param renderer  the BufferBuilder to render to
	 * @param world     the world the model is in. Will be used to obtain lighting information
	 * @param pos       the position that this model is in. Use the position the the quads are actually in, not the rendering block
	 * @param useCached Whether to use cached information for world local data. Set to true if the previous call to this method was in the same tick and for the same world+pos
	 * @param color     the render color (mostly used for plants)
	 */
	public static void renderModelTESRFancy(
			List<BakedQuad> quads, VertexConsumer renderer, PoseStack transform,
			Level world, BlockPos pos,
			boolean useCached, int color, int light
	)
	{//TODO include matrix transformations?, cache normals?
		if(IEClientConfig.disableFancyTESR.get())
			renderModelTESRFast(quads, renderer, transform, world.getRawBrightness(pos, 0), color);
		else
		{
			if(!useCached)
			{
				// Calculate surrounding brighness and split into block and sky light
				for(Direction f : DirectionUtils.VALUES)
				{
					int val = LevelRenderer.getLightColor(world, pos.relative(f));
					neighbourBrightness[0][f.get3DDataValue()] = (val>>16)&255;
					neighbourBrightness[1][f.get3DDataValue()] = val&255;
				}
				// calculate the different correction factors for all 8 possible light vectors
				for(int type = 0; type < 2; type++)
					for(int i = 0; i < 8; i++)
					{
						float sSquared = 0;
						if((i&1)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][5], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][4], 255F);
						if((i&2)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][1], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][0], 255F);
						if((i&4)!=0)
							sSquared += scaledSquared(neighbourBrightness[type][3], 255F);
						else
							sSquared += scaledSquared(neighbourBrightness[type][2], 255F);
						normalizationFactors[type][i] = (float)Math.sqrt(sSquared);
					}
			}
			int rgba[] = {255, 255, 255, 255};
			if(color >= 0)
			{
				rgba[0] = color>>16&255;
				rgba[1] = color>>8&255;
				rgba[2] = color&255;
			}
			final Matrix4fc positionTransform = transform.last().pose();
			final Matrix3fc normalTransform = transform.last().normal();
			for(BakedQuad quad : quads)
			{
				int[] vData = quad.getVertices();
				// extract position info from the quad
				for(int i = 0; i < 4; i++)
					quadCoords[i].set(
							Float.intBitsToFloat(vData[VERTEX_SIZE*i+POSITION_OFFSET]),
							Float.intBitsToFloat(vData[VERTEX_SIZE*i+POSITION_OFFSET+1]),
							Float.intBitsToFloat(vData[VERTEX_SIZE*i+POSITION_OFFSET+2]),
							1
					);
				//generate the normal vector
				Vector3f normal = new Vector3f(quadCoords[1].x, quadCoords[1].y, quadCoords[1].z);
				Vector3f side2 = new Vector3f(quadCoords[2].x, quadCoords[2].y, quadCoords[2].z);
				normal.add(-quadCoords[3].x(), -quadCoords[3].y(), -quadCoords[3].z());
				side2.add(-quadCoords[0].x(), -quadCoords[0].y(), -quadCoords[0].z());
				normal.cross(side2);
				normal.normalize();
				// calculate the final light values and do the rendering
				int l1 = getLightValue(neighbourBrightness[1], normalizationFactors[1], light&255, normal);
				int l2 = getLightValue(neighbourBrightness[0], normalizationFactors[0], (light>>16)&255, normal);
				normal.mul(normalTransform);
				for(int i = 0; i < 4; ++i)
				{
					final Vector4f vertexPos = quadCoords[i];
					vertexPos.mul(positionTransform);
					renderer.vertex(
							vertexPos.x(), vertexPos.y(), vertexPos.z(),
							rgba[0]/255f, rgba[1]/255f, rgba[2]/255f, rgba[3]/255f,
							Float.intBitsToFloat(vData[VERTEX_SIZE*i+UV_OFFSET]), Float.intBitsToFloat(vData[VERTEX_SIZE*i+UV_OFFSET+1]),
							OverlayTexture.NO_OVERLAY,
							LightTexture.pack(l1>>4, l2>>4),
							normal.x(), normal.y(), normal.z()
					);
				}
			}
		}
	}

	private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness, Vector3f normal)
	{
		//calculate the dot product between the required light vector and the normal of the quad
		// quad brightness is proportional to this value, see https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling#flat-shading-render
		double sideBrightness;
		byte type = 0;
		if(normal.x() > 0)
		{
			sideBrightness = normal.x()*neighbourBrightness[5];
			type |= 1;
		}
		else
			sideBrightness = -normal.x()*neighbourBrightness[4];
		if(normal.y() > 0)
		{
			sideBrightness += normal.y()*neighbourBrightness[1];
			type |= 2;
		}
		else
			sideBrightness += -normal.y()*neighbourBrightness[0];
		if(normal.z() > 0)
		{
			sideBrightness += normal.z()*neighbourBrightness[3];
			type |= 4;
		}
		else
			sideBrightness += -normal.z()*neighbourBrightness[2];
		// the final light value is the aritmethic mean of the local brighness and the normalized "dot-product-brightness"
		return (int)((localBrightness+sideBrightness/normalizationFactors[type])/2);
	}

	private static float scaledSquared(int val, float scale)
	{
		return (val/scale)*(val/scale);
	}

	public static void renderModelTESRFast(List<BakedQuad> quads, VertexConsumer renderer, PoseStack transform,
										   int light, int overlay)
	{
		renderModelTESRFast(quads, renderer, transform, -1, light, overlay);
	}

	public static void renderModelTESRFast(List<BakedQuad> quads, VertexConsumer renderer, PoseStack transform,
										   int color, int light, int overlay)
	{
		float red = 1;
		float green = 1;
		float blue = 1;
		if(color >= 0)
		{
			red = (color>>16&255)/255F;
			green = (color>>8&255)/255F;
			blue = (color&255)/255F;
		}
		for(BakedQuad quad : quads)
			renderer.putBulkData(transform.last(), quad, red, green, blue, light, overlay);
	}

	//Cheers boni =P
	public static void drawBlockDamageTexture(PoseStack matrix, MultiBufferSource buffers, Level world, Collection<BlockPos> blocks)
	{
		MultiPlayerGameMode controller = Minecraft.getInstance().gameMode;
		int progress = (int)(((PlayerControllerAccess)controller).getDestroyProgress()*10f)-1; // 0-10
		if(progress < 0||progress >= ModelBakery.DESTROY_TYPES.size())
			return;
		BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
		for(BlockPos blockpos : blocks)
		{
			matrix.pushPose();
			matrix.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());
			VertexConsumer worldRendererIn = buffers.getBuffer(ModelBakery.DESTROY_TYPES.get(progress));
			worldRendererIn = new SheetedDecalTextureGenerator(worldRendererIn, matrix.last().pose(), matrix.last().normal(), 1);
			Block block = world.getBlockState(blockpos).getBlock();
			boolean hasBreak = block instanceof ChestBlock||block instanceof EnderChestBlock
					||block instanceof SignBlock||block instanceof SkullBlock;
			if(!hasBreak)
			{
				BlockState iblockstate = world.getBlockState(blockpos);
				if(!iblockstate.isAir())
					blockrendererdispatcher.renderBreakingTexture(iblockstate, blockpos, world, matrix, worldRendererIn);
			}
			matrix.popPose();
		}
	}

	public static void renderBox(VertexConsumer wr, PoseStack m, float x0, float y0, float z0, float x1, float y1, float z1)
	{
		Matrix4f transform = m.last().pose();
		wr.vertex(transform, x0, y0, z1).endVertex();
		wr.vertex(transform, x1, y0, z1).endVertex();
		wr.vertex(transform, x1, y1, z1).endVertex();
		wr.vertex(transform, x0, y1, z1).endVertex();

		wr.vertex(transform, x0, y1, z0).endVertex();
		wr.vertex(transform, x1, y1, z0).endVertex();
		wr.vertex(transform, x1, y0, z0).endVertex();
		wr.vertex(transform, x0, y0, z0).endVertex();

		wr.vertex(transform, x0, y0, z0).endVertex();
		wr.vertex(transform, x1, y0, z0).endVertex();
		wr.vertex(transform, x1, y0, z1).endVertex();
		wr.vertex(transform, x0, y0, z1).endVertex();

		wr.vertex(transform, x0, y1, z1).endVertex();
		wr.vertex(transform, x1, y1, z1).endVertex();
		wr.vertex(transform, x1, y1, z0).endVertex();
		wr.vertex(transform, x0, y1, z0).endVertex();

		wr.vertex(transform, x0, y0, z0).endVertex();
		wr.vertex(transform, x0, y0, z1).endVertex();
		wr.vertex(transform, x0, y1, z1).endVertex();
		wr.vertex(transform, x0, y1, z0).endVertex();

		wr.vertex(transform, x1, y1, z0).endVertex();
		wr.vertex(transform, x1, y1, z1).endVertex();
		wr.vertex(transform, x1, y0, z1).endVertex();
		wr.vertex(transform, x1, y0, z0).endVertex();
	}

	public static void renderTexturedBox(VertexConsumer wr, PoseStack stack, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite tex, boolean yForV)
	{
		float minU = tex.getU(x0*16);
		float maxU = tex.getU(x1*16);
		float minV = tex.getV((yForV?y1: z0)*16);
		float maxV = tex.getV((yForV?y0: z1)*16);
		renderTexturedBox(wr, stack, x0, y0, z0, x1, y1, z1, minU, minV, maxU, maxV, LightTexture.FULL_BRIGHT);
	}

	public static void renderTexturedBox(
			VertexConsumer wr, PoseStack
			stack, float x0, float y0, float z0,
			float x1, float y1, float z1,
			float u0, float v0,
			float u1, float v1,
			int light
	)
	{
		float normalX = 0;
		float normalY = 0;
		float normalZ = 1;

		putVertex(wr, stack, x0, y0, z1, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z1, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y1, z1, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y1, z1, u0, v1, normalX, normalY, normalZ, light);
		normalZ = -1;
		putVertex(wr, stack, x0, y1, z0, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y1, z0, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z0, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y0, z0, u0, v1, normalX, normalY, normalZ, light);

		normalZ = 0;
		normalY = -1;
		putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z0, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y0, z1, u0, v1, normalX, normalY, normalZ, light);
		normalY = 1;
		putVertex(wr, stack, x0, y1, z1, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y1, z0, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ, light);

		normalY = 0;
		normalX = -1;
		putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y0, z1, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y1, z1, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ, light);
		normalX = 1;
		putVertex(wr, stack, x1, y1, z0, u0, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ, light);
		putVertex(wr, stack, x1, y0, z0, u0, v1, normalX, normalY, normalZ, light);
	}

	private static void putVertex(
			VertexConsumer b, PoseStack mat,
			float x, float y, float z,
			float u, float v,
			float nX, float nY, float nZ,
			int light
	)
	{
		b.vertex(mat.last().pose(), x, y, z)
				.color(1F, 1F, 1F, 1F)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(mat.last().normal(), nX, nY, nZ)
				.endVertex();
	}
}
