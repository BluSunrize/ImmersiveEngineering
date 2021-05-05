/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.SmartLightingQuad;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Type;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ModelUtils
{

	public static Set<BakedQuad> createBakedBox(Vector3d from, Vector3d to, Matrix4 matrix, Direction facing, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, facing, vertices -> vertices, textureGetter, colour);
	}

	@Nonnull
	public static Set<BakedQuad> createBakedBox(Vector3d from, Vector3d to, Matrix4 matrixIn, Direction facing, Function<Vector3d[], Vector3d[]> vertexTransformer, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		TransformationMatrix matrix = matrixIn.toTransformationMatrix();
		HashSet<BakedQuad> quads = new HashSet<>();
		if(vertexTransformer==null)
			vertexTransformer = v -> v;

		Vector3d[] vertices = {
				new Vector3d(from.x, from.y, from.z),
				new Vector3d(from.x, from.y, to.z),
				new Vector3d(to.x, from.y, to.z),
				new Vector3d(to.x, from.y, from.z)
		};
		TextureAtlasSprite sprite = textureGetter.apply(Direction.DOWN);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), sprite, new double[]{from.x*16, 16-from.z*16, to.x*16, 16-to.z*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vector3d v = vertices[i];
			vertices[i] = new Vector3d(v.x, to.y, v.z);
		}
		sprite = textureGetter.apply(Direction.UP);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.UP, facing), sprite, new double[]{from.x*16, from.z*16, to.x*16, to.z*16}, colour, false));

		vertices = new Vector3d[]{
				new Vector3d(to.x, to.y, from.z),
				new Vector3d(to.x, from.y, from.z),
				new Vector3d(from.x, from.y, from.z),
				new Vector3d(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.NORTH);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, facing), sprite, new double[]{from.x*16, 16-to.y*16, to.x*16, 16-from.y*16}, colour, false));

		for(int i = 0; i < vertices.length; i++)
		{
			Vector3d v = vertices[i];
			vertices[i] = new Vector3d(v.x, v.y, to.z);
		}
		sprite = textureGetter.apply(Direction.SOUTH);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, facing), sprite, new double[]{to.x*16, 16-to.y*16, from.x*16, 16-from.y*16}, colour, true));

		vertices = new Vector3d[]{
				new Vector3d(from.x, to.y, to.z),
				new Vector3d(from.x, from.y, to.z),
				new Vector3d(from.x, from.y, from.z),
				new Vector3d(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.WEST);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.WEST, facing), sprite, new double[]{to.z*16, 16-to.y*16, from.z*16, 16-from.y*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vector3d v = vertices[i];
			vertices[i] = new Vector3d(to.x, v.y, v.z);
		}
		sprite = textureGetter.apply(Direction.EAST);
		if(sprite!=null)
			quads.add(createBakedQuad(DefaultVertexFormats.BLOCK, ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.EAST, facing), sprite, new double[]{16-to.z*16, 16-to.y*16, 16-from.z*16, 16-from.y*16}, colour, false));

		return quads;
	}

	private static final float[] FOUR_ONES = {1, 1, 1, 1};

	public static BakedQuad createSmartLightingBakedQuad(VertexFormat format, Vector3d[] vertices, Direction facing, TextureAtlasSprite sprite, float[] colour, boolean invert, BlockPos base)
	{
		return createBakedQuad(format, vertices, facing, sprite, new double[]{0, 0, 16, 16}, colour, invert, FOUR_ONES, true, base);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, FOUR_ONES);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha)
	{
		return createBakedQuad(format, vertices, facing, sprite, uvs, colour, invert, alpha, false, null);
	}

	public static BakedQuad createBakedQuad(VertexFormat format, Vector3d[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert, float[] alpha, boolean smartLighting, BlockPos basePos)
	{
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
		builder.setQuadOrientation(facing);
		builder.setApplyDiffuseLighting(true);
		Vector3i normalInt = facing.getDirectionVec();
		Vector3d faceNormal = new Vector3d(normalInt.getX(), normalInt.getY(), normalInt.getZ());
		int vId = invert?3: 0;
		int u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
		vId = invert?2: 1;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
		vId = invert?1: 2;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, alpha[vId]);
		vId = invert?0: 3;
		u = vId > 1?2: 0;
		putVertexData(format, builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, alpha[vId]);
		BakedQuad tmp = builder.build();
		return smartLighting?new SmartLightingQuad(tmp.getVertexData(), -1, facing, sprite, basePos): tmp;
	}

	public static void putVertexData(VertexFormat format, BakedQuadBuilder builder, Vector3d pos, Vector3d faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha)
	{
		for(int e = 0; e < format.getElements().size(); e++)
			switch(format.getElements().get(e).getUsage())
			{
				case POSITION:
					builder.put(e, (float)pos.x, (float)pos.y, (float)pos.z);
					break;
				case COLOR:
					float d = 1;//LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
					builder.put(e, d*colour[0], d*colour[1], d*colour[2], 1*colour[3]*alpha);
					break;
				case UV:
					if(format.getElements().get(e).getType()==Type.FLOAT)
					{
						// Actual UVs
						if(sprite==null)//Double Safety. I have no idea how it even happens, but it somehow did .-.
							sprite = getMissingSprite();
						builder.put(e,
								sprite.getInterpolatedU(u),
								sprite.getInterpolatedV(v));
					}
					else
						//Lightmap UVs (0, 0 is "automatic")
						builder.put(e, 0, 0);
					break;
				case NORMAL:
					builder.put(e, (float)faceNormal.getX(), (float)faceNormal.getY(), (float)faceNormal.getZ());
					break;
				default:
					builder.put(e);
			}
	}

	private static TextureAtlasSprite getMissingSprite()
	{
		return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
	}

	public static ResourceLocation getSideTexture(@Nonnull ItemStack stack, Direction side)
	{
		IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, null, null);
		return getSideTexture(model, side, null);
	}

	public static ResourceLocation getSideTexture(@Nonnull BlockState state, Direction side)
	{
		IBakedModel model = mc().getBlockRendererDispatcher().getModelForState(state);
		return getSideTexture(model, side, state);
	}

	public static ResourceLocation getSideTexture(@Nonnull IBakedModel model, Direction side, @Nullable BlockState state)
	{
		List<BakedQuad> quads = model.getQuads(state, side, Utils.RAND, EmptyModelData.INSTANCE);
		if(quads.isEmpty())//no quads for the specified side D:
			quads = model.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE);
		if(quads.isEmpty())//no quads at all D:
			return null;
		return quads.get(0).getSprite().getName();
	}
}
