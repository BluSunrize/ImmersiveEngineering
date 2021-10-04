/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.StructuralArmBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static blusunrize.immersiveengineering.client.utils.ModelUtils.putVertexData;
import static net.minecraft.core.Direction.*;

public class StructuralArmCallbacks implements BlockCallback<StructuralArmCallbacks.Key>
{
	public static final StructuralArmCallbacks INSTANCE = new StructuralArmCallbacks();
	private static final Matrix4 SHRINK = Util.make(new Matrix4(), mat -> {
		mat.translate(.5, .5, .5);
		mat.scale(.999, .999, .999);
		mat.translate(-.5, -.5, -.5);
	});
	private static final Key INVALID = new Key(0, 1, false, NORTH);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof StructuralArmBlockEntity structuralArm))
			return getDefaultKey();
		return new Key(
				structuralArm.getSlopePosition(),
				structuralArm.getTotalLength(),
				structuralArm.isOnCeiling(),
				structuralArm.getFacing()
		);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public List<BakedQuad> modifyQuads(Key object, List<BakedQuad> quads)
	{
		boolean onCeiling = object.onCeiling();
		int slopePosition = object.slopePosition();
		int totalLength = object.totalLength();
		float lowerHeight = slopePosition/(float)totalLength;
		float upperHeight = (slopePosition+1F)/totalLength;
		double lowerV = 16*lowerHeight;
		double upperV = 16*upperHeight;
		TextureAtlasSprite tas = quads.get(0).getSprite();
		VertexFormat format = DefaultVertexFormat.BLOCK;
		quads = new ArrayList<>();
		Matrix4 mat = new Matrix4(object.facing());

		Vec3[] vertices;
		{
			float y03 = onCeiling?1: upperHeight;
			float y12 = onCeiling?1: lowerHeight;
			float y47 = onCeiling?1-upperHeight: 0;
			float y56 = onCeiling?1-lowerHeight: 0;
			vertices = new Vec3[]{
					new Vec3(0, y03, 0),//0
					new Vec3(0, y12, 1),//1
					new Vec3(1, y12, 1),//2
					new Vec3(1, y03, 0),//3
					new Vec3(0, y47, 0),//4
					new Vec3(0, y56, 1),//5
					new Vec3(1, y56, 1),//6
					new Vec3(1, y47, 0),//7
			};
		}
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = mat.apply(vertices[i]);
		//TOP
		addCulledQuad(quads, format, Arrays.copyOf(vertices, 4),
				UP, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1}, object.facing());
		//BOTTOM
		addCulledQuad(quads, format, getArrayByIndices(vertices, 7, 6, 5, 4),
				DOWN, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1}, object.facing());
		//SIDES
		addSides(quads, vertices, tas, lowerV, upperV, false, object.facing(), object.onCeiling());
		addSides(quads, vertices, tas, lowerV, upperV, true, object.facing(), object.onCeiling());
		if(object.slopePosition()+1==object.totalLength())
			addCulledQuad(quads, format, getArrayByIndices(vertices, 0, 3, 7, 4),
					NORTH, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1}, object.facing);
		return quads;
	}

	private void addCulledQuad(List<BakedQuad> quads, VertexFormat format, Vec3[] vertices, Direction side,
							   TextureAtlasSprite tas, double[] uvs, float[] alpha, Direction facing)
	{
		side = Utils.rotateFacingTowardsDir(side, facing);
		quads.add(ModelUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = SHRINK.apply(vertices[i]);
		quads.add(ModelUtils.createBakedQuad(format, vertices, side.getOpposite(), tas, uvs, alpha, true));
	}

	private void addSides(List<BakedQuad> quads, Vec3[] vertices, TextureAtlasSprite tas, double lowerV,
						  double upperV, boolean invert, Direction facing, boolean onCeiling)
	{
		if(invert)
		{
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = SHRINK.apply(vertices[i]);
		}
		quads.add(createSide(getArrayByIndices(vertices, 5, 1, 0, 4),
				WEST, tas, lowerV, upperV, invert, facing, onCeiling));
		quads.add(createSide(getArrayByIndices(vertices, 7, 3, 2, 6),
				EAST, tas, upperV, lowerV, invert, facing, onCeiling));
	}

	private BakedQuad createSide(Vec3[] vertices, Direction facing, TextureAtlasSprite sprite,
								 double leftV, double rightV, boolean invert, Direction blockFacing, boolean onCeiling)
	{
		facing = Utils.rotateFacingTowardsDir(facing, blockFacing);
		if(invert)
		{
			double tmp = leftV;
			leftV = rightV;
			rightV = tmp;
		}
		if(invert)
			facing = facing.getOpposite();
		float[] colour = {1, 1, 1, 1};
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
		builder.setQuadOrientation(facing);
		Vec3 faceNormal = Vec3.atLowerCornerOf(facing.getNormal());
		int vertexId = invert?3: 0;
		double v = onCeiling?16-leftV: 0;
		putVertexData(DefaultVertexFormat.BLOCK, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?2: 1;
		v = onCeiling?16: leftV;
		putVertexData(DefaultVertexFormat.BLOCK, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?1: 2;
		v = onCeiling?16: rightV;
		putVertexData(DefaultVertexFormat.BLOCK, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?0: 3;
		v = onCeiling?16-rightV: 0;
		putVertexData(DefaultVertexFormat.BLOCK, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		return builder.build();
	}

	private Vec3[] getArrayByIndices(Vec3[] in, int... indices)
	{
		Vec3[] ret = new Vec3[indices.length];
		for(int i = 0; i < indices.length; i++)
			ret[i] = in[indices[i]];
		return ret;
	}

	public record Key(
			int slopePosition, int totalLength, boolean onCeiling, Direction facing
	)
	{
	}
}
