/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.mixin.accessors.client.SimpleModelAccess;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ModelUtils
{
	public static RenderTypeGroup copyTypes(SimpleBakedModel simpleModel)
	{
		SimpleModelAccess access = (SimpleModelAccess)simpleModel;
		ChunkRenderTypeSet blockTypes = access.getBlockRenderTypes();
		if(blockTypes==null||blockTypes.isEmpty())
			return RenderTypeGroup.EMPTY;
		List<RenderType> itemTypes = access.getItemRenderTypes();
		List<RenderType> fabulousItemTypes = access.getFabulousItemRenderTypes();
		return new RenderTypeGroup(blockTypes.iterator().next(), itemTypes.get(0), fabulousItemTypes.get(0));
	}

	public static Transformation fromItemTransform(ItemTransform transform, boolean leftHand)
	{
		Vector3f translate = transform.translation;
		if(leftHand)
		{
			translate = new Vector3f(translate);
			translate.setComponent(0, -translate.x());
		}

		float leftRX = transform.rotation.x();
		float leftRY = transform.rotation.y();
		float leftRZ = transform.rotation.z();
		if(leftHand)
		{
			leftRY = -leftRY;
			leftRZ = -leftRZ;
		}
		Quaternionf leftRotation = new Quaternionf().rotateXYZ(
				Mth.DEG_TO_RAD * leftRX, Mth.DEG_TO_RAD * leftRY, Mth.DEG_TO_RAD * leftRZ
		);

		float rightRX = transform.rightRotation.x();
		float rightRY = transform.rightRotation.y()*(leftHand?-1: 1);
		float rightRZ = transform.rightRotation.z()*(leftHand?-1: 1);
		Quaternionf rightRotation = new Quaternionf().rotateXYZ(
				Mth.DEG_TO_RAD * rightRX, Mth.DEG_TO_RAD * rightRY, Mth.DEG_TO_RAD * rightRZ
		);

		return new Transformation(translate, leftRotation, transform.scale, rightRotation);
	}

	public static Set<BakedQuad> createBakedBox(Vec3 from, Vec3 to, Matrix4 matrix, Direction facing, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		return createBakedBox(from, to, matrix, facing, vertices -> vertices, textureGetter, colour);
	}

	@Nonnull
	public static Set<BakedQuad> createBakedBox(Vec3 from, Vec3 to, Matrix4 matrixIn, Direction facing, Function<Vec3[], Vec3[]> vertexTransformer, Function<Direction, TextureAtlasSprite> textureGetter, float[] colour)
	{
		Transformation matrix = matrixIn.toTransformationMatrix();
		HashSet<BakedQuad> quads = new HashSet<>();
		if(vertexTransformer==null)
			vertexTransformer = v -> v;

		Vec3[] vertices = {
				new Vec3(from.x, from.y, from.z),
				new Vec3(from.x, from.y, to.z),
				new Vec3(to.x, from.y, to.z),
				new Vec3(to.x, from.y, from.z)
		};
		TextureAtlasSprite sprite = textureGetter.apply(Direction.DOWN);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.DOWN, facing), sprite, new double[]{from.x*16, 16-from.z*16, to.x*16, 16-to.z*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(v.x, to.y, v.z);
		}
		sprite = textureGetter.apply(Direction.UP);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.UP, facing), sprite, new double[]{from.x*16, from.z*16, to.x*16, to.z*16}, colour, false));

		vertices = new Vec3[]{
				new Vec3(to.x, to.y, from.z),
				new Vec3(to.x, from.y, from.z),
				new Vec3(from.x, from.y, from.z),
				new Vec3(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.NORTH);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.NORTH, facing), sprite, new double[]{from.x*16, 16-to.y*16, to.x*16, 16-from.y*16}, colour, false));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(v.x, v.y, to.z);
		}
		sprite = textureGetter.apply(Direction.SOUTH);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.SOUTH, facing), sprite, new double[]{to.x*16, 16-to.y*16, from.x*16, 16-from.y*16}, colour, true));

		vertices = new Vec3[]{
				new Vec3(from.x, to.y, to.z),
				new Vec3(from.x, from.y, to.z),
				new Vec3(from.x, from.y, from.z),
				new Vec3(from.x, to.y, from.z)
		};
		sprite = textureGetter.apply(Direction.WEST);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.WEST, facing), sprite, new double[]{to.z*16, 16-to.y*16, from.z*16, 16-from.y*16}, colour, true));

		for(int i = 0; i < vertices.length; i++)
		{
			Vec3 v = vertices[i];
			vertices[i] = new Vec3(to.x, v.y, v.z);
		}
		sprite = textureGetter.apply(Direction.EAST);
		if(sprite!=null)
			quads.add(createBakedQuad(ClientUtils.applyMatrixToVertices(matrix, vertexTransformer.apply(vertices)), Utils.rotateFacingTowardsDir(Direction.EAST, facing), sprite, new double[]{16-to.z*16, 16-to.y*16, 16-from.z*16, 16-from.y*16}, colour, false));

		return quads;
	}

	public static BakedQuad createBakedQuad(Vec3[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert)
	{
		BakedQuadBuilder builder = new BakedQuadBuilder();
		Vec3i normalInt = facing.getNormal();
		Vec3 faceNormal = new Vec3(normalInt.getX(), normalInt.getY(), normalInt.getZ());
		int vId = invert?3: 0;
		int u = vId > 1?2: 0;
		builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
		vId = invert?2: 1;
		u = vId > 1?2: 0;
		builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
		vId = invert?1: 2;
		u = vId > 1?2: 0;
		builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
		vId = invert?0: 3;
		u = vId > 1?2: 0;
		builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
		return builder.bake(-1, facing, sprite, true);
	}

	public static ResourceLocation getSideTexture(@Nonnull ItemStack stack, Direction side)
	{
		BakedModel model = mc().getItemRenderer().getModel(stack, null, null, 0);
		return getSideTexture(model, side, null);
	}

	public static ResourceLocation getSideTexture(@Nonnull BlockState state, Direction side)
	{
		BakedModel model = mc().getBlockRenderer().getBlockModel(state);
		return getSideTexture(model, side, state);
	}

	public static ResourceLocation getSideTexture(@Nonnull BakedModel model, Direction side, @Nullable BlockState state)
	{
		List<BakedQuad> quads = model.getQuads(state, side, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
		if(quads.isEmpty())//no quads for the specified side D:
			quads = model.getQuads(state, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
		if(quads.isEmpty())//no quads at all D:
			return null;
		return quads.get(0).getSprite().contents().name();
	}

	public static BakedQuad reverseOrder(BakedQuad in)
	{
		int[] oldData = in.getVertices();
		int[] newData = new int[oldData.length];
		final int vertexLength = oldData.length/4;
		for(int i = 0; i < 4; ++i)
			System.arraycopy(oldData, i*vertexLength, newData, (3-i)*vertexLength, vertexLength);
		return new BakedQuad(newData, in.getTintIndex(), in.getDirection(), in.getSprite(), in.isShade());
	}
}
