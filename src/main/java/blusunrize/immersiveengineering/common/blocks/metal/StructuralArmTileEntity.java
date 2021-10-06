/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.utils.ModelUtils.putVertexData;
import static net.minecraft.core.Direction.*;

public class StructuralArmTileEntity extends IEBaseTileEntity implements IOBJModelCallback<BlockState>,
		IStateBasedDirectional, ICollisionBounds, ISelectionBounds, IBlockBounds
{
	private int totalLength = 1;
	private int slopePosition = 0;
	private Direction facing = null;
	private boolean onCeiling = false;

	public StructuralArmTileEntity()
	{
		super(IETileTypes.STRUCTURAL_ARM.get());
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int oldLength = totalLength, oldPos = slopePosition;
		totalLength = nbt.getInt("totalLength");
		slopePosition = nbt.getInt("slopePosition");
		onCeiling = nbt.getBoolean("onCeiling");
		if(level!=null&&level.isClientSide&&(oldLength!=totalLength||slopePosition!=oldPos))
		{
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
		// In IE 134 and below the tile field is used instead of the blockstate property. The TE field is now only used
		// to handle worlds saved with those versions and should be removed once compat is no longer a concern.
		// Note that the blockstate is not actively replaced, so this will be the next MC version break (1.17).
		if(nbt.contains("facing", NBT.TAG_INT))
			this.facing = DirectionUtils.VALUES[nbt.getInt("facing")];
		else
			this.facing = null;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("totalLength", totalLength);
		nbt.putInt("slopePosition", slopePosition);
		if(this.facing!=null)
			nbt.putInt("facing", this.facing.ordinal());
		nbt.putBoolean("onCeiling", onCeiling);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		if(level.isClientSide)
			return;
		boolean positive;
		if(otherPos.equals(worldPosition.relative(getFacing(), 1)))
			positive = true;
		else if(otherPos.equals(worldPosition.relative(getFacing(), -1)))
			positive = false;
		else
			return;
		StructuralArmTileEntity slope = null;
		{
			BlockEntity atOther = level.getBlockEntity(otherPos);
			if(atOther instanceof StructuralArmTileEntity)
			{
				StructuralArmTileEntity tmp = (StructuralArmTileEntity)atOther;
				BlockState stateHere = level.getBlockState(worldPosition);
				BlockState stateThere = level.getBlockState(otherPos);
				if(tmp.getFacing()==this.getFacing()&&stateHere.getBlock()==stateThere.getBlock()&&tmp.onCeiling==this.onCeiling)
					slope = (StructuralArmTileEntity)atOther;
			}
		}
		boolean atEnd = isAtEnd(positive);
		if(atEnd==(slope==null))
			return;
		if(slope==null)
		{
			int toEnd = blocksToEnd(positive);
			forEachSlopeBlockBeyond(positive, false, true, other -> {
				other.totalLength = toEnd-1;
				if(positive)
					other.slopePosition -= slopePosition+2;
				updateNoNeighbours(other.worldPosition);
			});
			forEachSlopeBlockBeyond(!positive, true, true, other -> {
				other.totalLength = totalLength-toEnd;
				if(!positive)
					other.slopePosition -= this.slopePosition;
				updateNoNeighbours(other.worldPosition);
			});

		}
		else
		{
			int oldLength = totalLength;
			if(!positive)
				slopePosition += slope.totalLength;
			totalLength += slope.totalLength;
			forEachSlopeBlockBeyond(positive, false, false, other -> {
				other.totalLength = totalLength;
				if(positive)
					other.slopePosition += oldLength;
				updateNoNeighbours(other.worldPosition);
			});
			forEachSlopeBlockBeyond(!positive, false, false, other -> {
				other.totalLength = totalLength;
				if(!positive)
					other.slopePosition += totalLength-oldLength;
				updateNoNeighbours(other.worldPosition);
			});
		}
		updateNoNeighbours(worldPosition);
	}

	private boolean isAtEnd(boolean positive)
	{
		if(positive)
			return slopePosition==totalLength-1;
		else
			return slopePosition==0;
	}

	private int blocksToEnd(boolean positive)
	{
		if(positive)
			return totalLength-slopePosition-1;
		else
			return slopePosition;
	}

	private void forEachSlopeBlockBeyond(boolean positive, boolean includeThis, boolean removing,
										 Consumer<StructuralArmTileEntity> out)
	{
		if(positive)
			for(int i = 1; i < totalLength-slopePosition; i++)
				acceptIfValid(i, removing, out);
		else
			for(int i = -1; i >= -slopePosition; i--)
				acceptIfValid(i, removing, out);
		if(includeThis)
			out.accept(this);
	}

	private void acceptIfValid(int offsetToHere, boolean removing, Consumer<StructuralArmTileEntity> out)
	{
		BlockPos posI = worldPosition.relative(getFacing(), offsetToHere);
		BlockEntity teAtI = level.getBlockEntity(posI);
		if(teAtI instanceof StructuralArmTileEntity)
		{
			StructuralArmTileEntity slope = (StructuralArmTileEntity)teAtI;
			int offsetAtPos = slopePosition+offsetToHere;
			BlockState stateHere = level.getBlockState(worldPosition);
			BlockState stateThere = level.getBlockState(posI);
			if((!removing||(slope.totalLength==this.totalLength&&slope.slopePosition==offsetAtPos))
					&&slope.onCeiling==this.onCeiling
					&&stateHere.getBlock()==stateThere.getBlock()
					&&slope.getFacing()==this.getFacing())
				out.accept(slope);
		}
	}

	private void updateNoNeighbours(BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		level.sendBlockUpdated(pos, state, state, 3);
	}

	@Override
	public Direction getFacing()
	{
		if(this.facing!=null)
			return facing;
		else
			return IStateBasedDirectional.super.getFacing();
	}

	@Override
	public void setFacing(Direction facing)
	{
		IStateBasedDirectional.super.setFacing(facing);
		this.facing = null;
		totalLength = 1;
		slopePosition = 0;
		if(level!=null)
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return StructuralArmBlock.FACING;
	}

	@Override
	public Direction getFacingForPlacement(LivingEntity placer, BlockPos pos, Direction side, float hitX, float hitY,
										   float hitZ)
	{
		onCeiling = (side==DOWN)||(side!=UP&&hitY > .5);
		return IStateBasedDirectional.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	private static final CachedShapesWithTransform<Triple<Integer, Integer, Boolean>, Direction> SHAPES =
			CachedShapesWithTransform.createDirectional(
					triple -> getBounds(triple.getLeft(), triple.getMiddle(), triple.getRight())
			);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(Triple.of(slopePosition, totalLength, onCeiling), getFacing());
	}

	private static List<AABB> getBounds(int slopePosition, int totalLength, boolean onCeiling)
	{
		double lowerH = (slopePosition+.5)/totalLength;
		double upperH = (slopePosition+1.)/totalLength;
		if(!onCeiling)
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, lowerH, 1),
					new AABB(0, lowerH, 0, 1, upperH, .5)
			);
		else
			return ImmutableList.of(
					new AABB(0, 1-lowerH, 0, 1, 1, 1),
					new AABB(0, 1-upperH, 0, 1, 1-lowerH, .5)
			);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(BlockState object, List<BakedQuad> quads)
	{
		float lowerHeight = slopePosition/(float)totalLength;
		float upperHeight = (slopePosition+1F)/totalLength;
		double lowerV = 16*lowerHeight;
		double upperV = 16*upperHeight;
		TextureAtlasSprite tas = quads.get(0).a();
		VertexFormat format = DefaultVertexFormat.BLOCK;
		quads = new ArrayList<>();
		Matrix4 mat = new Matrix4(getFacing());

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
				UP, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//BOTTOM
		addCulledQuad(quads, format, getArrayByIndices(vertices, 7, 6, 5, 4),
				DOWN, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		//SIDES
		addSides(quads, vertices, tas, lowerV, upperV, false);
		addSides(quads, vertices, tas, lowerV, upperV, true);
		if(isAtEnd(true))
			//HIGHER END
			addCulledQuad(quads, format, getArrayByIndices(vertices, 0, 3, 7, 4),
					NORTH, tas, new double[]{0, 0, 16, 16}, new float[]{1, 1, 1, 1});
		return quads;
	}

	private static final Matrix4 SHRINK;

	static
	{
		SHRINK = new Matrix4();
		SHRINK.translate(.5, .5, .5);
		SHRINK.scale(.999, .999, .999);
		SHRINK.translate(-.5, -.5, -.5);
	}

	private void addCulledQuad(List<BakedQuad> quads, VertexFormat format, Vec3[] vertices, Direction side,
							   TextureAtlasSprite tas, double[] uvs, float[] alpha)
	{
		side = Utils.rotateFacingTowardsDir(side, this.getFacing());
		quads.add(ModelUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = SHRINK.apply(vertices[i]);
		quads.add(ModelUtils.createBakedQuad(format, vertices, side.getOpposite(), tas, uvs, alpha, true));
	}

	private void addSides(List<BakedQuad> quads, Vec3[] vertices, TextureAtlasSprite tas, double lowerV,
						  double upperV, boolean invert)
	{
		if(invert)
		{
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = SHRINK.apply(vertices[i]);
		}
		quads.add(createSide(DefaultVertexFormat.BLOCK, getArrayByIndices(vertices, 5, 1, 0, 4),
				WEST, tas, lowerV, upperV, invert));
		quads.add(createSide(DefaultVertexFormat.BLOCK, getArrayByIndices(vertices, 7, 3, 2, 6),
				EAST, tas, upperV, lowerV, invert));
	}

	@OnlyIn(Dist.CLIENT)
	private BakedQuad createSide(VertexFormat format, Vec3[] vertices, Direction facing, TextureAtlasSprite sprite,
								 double leftV, double rightV, boolean invert)
	{
		facing = Utils.rotateFacingTowardsDir(facing, this.getFacing());
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
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?2: 1;
		v = onCeiling?16: leftV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?1: 2;
		v = onCeiling?16: rightV;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		vertexId = invert?0: 3;
		v = onCeiling?16-rightV: 0;
		putVertexData(format, builder, vertices[vertexId], faceNormal, vertexId > 1?16: 0, v, sprite, colour, 1);
		return builder.build();
	}

	private Vec3[] getArrayByIndices(Vec3[] in, int... indices)
	{
		Vec3[] ret = new Vec3[indices.length];
		for(int i = 0; i < indices.length; i++)
			ret[i] = in[indices[i]];
		return ret;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return totalLength+","+slopePosition+","+getFacing().name()+","+(onCeiling?"1": "0");
	}
}
