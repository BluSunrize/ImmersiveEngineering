/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.mixin.accessors.client.PlayerControllerAccess;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class RenderUtils
{
	// The coordinates for each vertex of a quad
	private static final float[][] quadCoords = new float[4][3];
	// the brighnesses of the surrounding blocks. the first dimension indicates block (1) vs sky (0) light
	// These are used to create different light direction vectors depending on the direction of a quads normal vector.
	private static final int[][] neighbourBrightness = new int[2][6];
	// The light vectors created from neighbourBrightness aren't "normalized" (to length 255), the length needs to be divided by this factor to normalize it.
	// The indices are generated as follows: a 1 bit indicates a positive facing normal, a 0 a negative one. 1=x, 2=y, 4=z
	private static final float[][] normalizationFactors = new float[2][8];

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
	public static void renderModelTESRFancy(List<BakedQuad> quads, IVertexBuilder renderer, World world, BlockPos pos,
											boolean useCached, int color, int light)
	{//TODO include matrix transformations?, cache normals?
		if(IEClientConfig.disableFancyTESR.get())
			renderModelTESRFast(quads, renderer, new MatrixStack(), world.getLightSubtracted(pos, 0), color);
		else
		{
			if(!useCached)
			{
				// Calculate surrounding brighness and split into block and sky light
				for(Direction f : DirectionUtils.VALUES)
				{
					int val = WorldRenderer.getCombinedLight(world, pos.offset(f));
					neighbourBrightness[0][f.getIndex()] = (val >> 16)&255;
					neighbourBrightness[1][f.getIndex()] = val&255;
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
				rgba[0] = color >> 16&255;
				rgba[1] = color >> 8&255;
				rgba[2] = color&255;
			}
			for(BakedQuad quad : quads)
			{
				int[] vData = quad.getVertexData();
				VertexFormat format = DefaultVertexFormats.BLOCK;
				int size = format.getIntegerSize();
				int uvOffset = ClientUtils.findTextureOffset(format);
				int posOffset = ClientUtils.findPositionOffset(format);
				// extract position info from the quad
				for(int i = 0; i < 4; i++)
				{
					quadCoords[i][0] = Float.intBitsToFloat(vData[size*i+posOffset]);
					quadCoords[i][1] = Float.intBitsToFloat(vData[size*i+posOffset+1]);
					quadCoords[i][2] = Float.intBitsToFloat(vData[size*i+posOffset+2]);
				}
				//generate the normal vector
				Vector3d side1 = new Vector3d(quadCoords[1][0]-quadCoords[3][0],
						quadCoords[1][1]-quadCoords[3][1],
						quadCoords[1][2]-quadCoords[3][2]);
				Vector3d side2 = new Vector3d(quadCoords[2][0]-quadCoords[0][0],
						quadCoords[2][1]-quadCoords[0][1],
						quadCoords[2][2]-quadCoords[0][2]);
				Vector3d normal = side1.crossProduct(side2);
				normal = normal.normalize();
				// calculate the final light values and do the rendering
				int l1 = getLightValue(neighbourBrightness[1], normalizationFactors[1], light&255, normal);
				int l2 = getLightValue(neighbourBrightness[0], normalizationFactors[0], (light >> 16)&255, normal);
				for(int i = 0; i < 4; ++i)
				{
					renderer
							.pos(quadCoords[i][0], quadCoords[i][1], quadCoords[i][2])
							.color(rgba[0], rgba[1], rgba[2], rgba[3])
							.tex(Float.intBitsToFloat(vData[size*i+uvOffset]), Float.intBitsToFloat(vData[size*i+uvOffset+1]))
							.lightmap(l1, l2)
							.normal((float)normal.x, (float)normal.y, (float)normal.z)
							.endVertex();
				}
			}
		}
	}

	private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness, Vector3d normal)
	{
		//calculate the dot product between the required light vector and the normal of the quad
		// quad brightness is proportional to this value, see https://github.com/ssloy/tinyrenderer/wiki/Lesson-2:-Triangle-rasterization-and-back-face-culling#flat-shading-render
		double sideBrightness;
		byte type = 0;
		if(normal.x > 0)
		{
			sideBrightness = normal.x*neighbourBrightness[5];
			type |= 1;
		}
		else
			sideBrightness = -normal.x*neighbourBrightness[4];
		if(normal.y > 0)
		{
			sideBrightness += normal.y*neighbourBrightness[1];
			type |= 2;
		}
		else
			sideBrightness += -normal.y*neighbourBrightness[0];
		if(normal.z > 0)
		{
			sideBrightness += normal.z*neighbourBrightness[3];
			type |= 4;
		}
		else
			sideBrightness += -normal.z*neighbourBrightness[2];
		// the final light value is the aritmethic mean of the local brighness and the normalized "dot-product-brightness"
		return (int)((localBrightness+sideBrightness/normalizationFactors[type])/2);
	}

	private static float scaledSquared(int val, float scale)
	{
		return (val/scale)*(val/scale);
	}

	public static void renderModelTESRFast(List<BakedQuad> quads, IVertexBuilder renderer, MatrixStack transform,
										   int light, int overlay)
	{
		renderModelTESRFast(quads, renderer, transform, -1, light, overlay);
	}

	public static void renderModelTESRFast(List<BakedQuad> quads, IVertexBuilder renderer, MatrixStack transform,
										   int color, int light, int overlay)
	{
		float red = 1;
		float green = 1;
		float blue = 1;
		if(color >= 0)
		{
			red = (color >> 16&255)/255F;
			green = (color >> 8&255)/255F;
			blue = (color&255)/255F;
		}
		for(BakedQuad quad : quads)
			renderer.addQuad(transform.getLast(), quad, red, green, blue, light, overlay);
	}

	//Cheers boni =P
	public static void drawBlockDamageTexture(MatrixStack matrix, IRenderTypeBuffer buffers, World world, Collection<BlockPos> blocks)
	{
		PlayerController controller = Minecraft.getInstance().playerController;
		int progress = (int)(((PlayerControllerAccess)controller).getCurBlockDamageMP()*10f)-1; // 0-10
		if(progress < 0||progress >= ModelBakery.DESTROY_RENDER_TYPES.size())
			return;
		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		for(BlockPos blockpos : blocks)
		{
			matrix.push();
			matrix.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());
			IVertexBuilder worldRendererIn = buffers.getBuffer(ModelBakery.DESTROY_RENDER_TYPES.get(progress));
			worldRendererIn = new MatrixApplyingVertexBuilder(worldRendererIn, matrix.getLast().getMatrix(), matrix.getLast().getNormal());
			Block block = world.getBlockState(blockpos).getBlock();
			boolean hasBreak = block instanceof ChestBlock||block instanceof EnderChestBlock
					||block instanceof AbstractSignBlock||block instanceof SkullBlock;
			if(!hasBreak)
			{
				BlockState iblockstate = world.getBlockState(blockpos);
				if(iblockstate.getMaterial()!=Material.AIR)
					blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, world, matrix, worldRendererIn);
			}
			matrix.pop();
		}
	}

	public static void renderBox(IVertexBuilder wr, MatrixStack m, float x0, float y0, float z0, float x1, float y1, float z1)
	{
		Matrix4f transform = m.getLast().getMatrix();
		wr.pos(transform, x0, y0, z1).endVertex();
		wr.pos(transform, x1, y0, z1).endVertex();
		wr.pos(transform, x1, y1, z1).endVertex();
		wr.pos(transform, x0, y1, z1).endVertex();

		wr.pos(transform, x0, y1, z0).endVertex();
		wr.pos(transform, x1, y1, z0).endVertex();
		wr.pos(transform, x1, y0, z0).endVertex();
		wr.pos(transform, x0, y0, z0).endVertex();

		wr.pos(transform, x0, y0, z0).endVertex();
		wr.pos(transform, x1, y0, z0).endVertex();
		wr.pos(transform, x1, y0, z1).endVertex();
		wr.pos(transform, x0, y0, z1).endVertex();

		wr.pos(transform, x0, y1, z1).endVertex();
		wr.pos(transform, x1, y1, z1).endVertex();
		wr.pos(transform, x1, y1, z0).endVertex();
		wr.pos(transform, x0, y1, z0).endVertex();

		wr.pos(transform, x0, y0, z0).endVertex();
		wr.pos(transform, x0, y0, z1).endVertex();
		wr.pos(transform, x0, y1, z1).endVertex();
		wr.pos(transform, x0, y1, z0).endVertex();

		wr.pos(transform, x1, y1, z0).endVertex();
		wr.pos(transform, x1, y1, z1).endVertex();
		wr.pos(transform, x1, y0, z1).endVertex();
		wr.pos(transform, x1, y0, z0).endVertex();
	}

	public static void renderTexturedBox(IVertexBuilder wr, MatrixStack stack, float x0, float y0, float z0, float x1, float y1, float z1, TextureAtlasSprite tex, boolean yForV)
	{
		float minU = tex.getInterpolatedU(x0*16);
		float maxU = tex.getInterpolatedU(x1*16);
		float minV = tex.getInterpolatedV((yForV?y1: z0)*16);
		float maxV = tex.getInterpolatedV((yForV?y0: z1)*16);
		renderTexturedBox(wr, stack, x0, y0, z0, x1, y1, z1, minU, minV, maxU, maxV);
	}

	public static void renderTexturedBox(IVertexBuilder wr, MatrixStack stack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1)
	{
		float normalX = 0;
		float normalY = 0;
		float normalZ = 1;

		putVertex(wr, stack, x0, y0, z1, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z1, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y1, z1, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y1, z1, u0, v1, normalX, normalY, normalZ);
		normalZ = -1;
		putVertex(wr, stack, x0, y1, z0, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y1, z0, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z0, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y0, z0, u0, v1, normalX, normalY, normalZ);

		normalZ = 0;
		normalY = -1;
		putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z0, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y0, z1, u0, v1, normalX, normalY, normalZ);
		normalY = 1;
		putVertex(wr, stack, x0, y1, z1, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y1, z0, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ);

		normalY = 0;
		normalX = -1;
		putVertex(wr, stack, x0, y0, z0, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y0, z1, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y1, z1, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x0, y1, z0, u0, v1, normalX, normalY, normalZ);
		normalX = 1;
		putVertex(wr, stack, x1, y1, z0, u0, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y1, z1, u1, v0, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z1, u1, v1, normalX, normalY, normalZ);
		putVertex(wr, stack, x1, y0, z0, u0, v1, normalX, normalY, normalZ);
	}

	private static void putVertex(IVertexBuilder b, MatrixStack mat, float x, float y, float z, float u, float v, float nX, float nY, float nZ)
	{
		b.pos(mat.getLast().getMatrix(), x, y, z)
				.color(1F, 1F, 1F, 1F)
				.tex(u, v)
				.lightmap(0, 0)
				.normal(mat.getLast().getNormal(), nX, nY, nZ)
				.endVertex();
	}
}
