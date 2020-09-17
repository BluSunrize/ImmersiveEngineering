/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.ClientUtils.putVertexData;
import static net.minecraft.util.Direction.*;

public class StructuralArmTileEntity extends IEBaseTileEntity implements IOBJModelCallback<BlockState>,
		IDirectionalTile, ICollisionBounds, ISelectionBounds, IBlockBounds
{
	private int totalLength = 1;
	private int slopePosition = 0;
	private Direction facing = NORTH;
	private boolean onCeiling = false;

	public StructuralArmTileEntity()
	{
		super(IETileTypes.STRUCTURAL_ARM.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		int oldLength = totalLength, oldPos = slopePosition;
		totalLength = nbt.getInt("totalLength");
		slopePosition = nbt.getInt("slopePosition");
		onCeiling = nbt.getBoolean("onCeiling");
		if(world!=null&&world.isRemote&&(oldLength!=totalLength||slopePosition!=oldPos))
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
			bounds = null;
		}
		facing = Direction.VALUES[nbt.getInt("facing")];
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("totalLength", totalLength);
		nbt.putInt("slopePosition", slopePosition);
		nbt.putInt("facing", facing.ordinal());
		nbt.putBoolean("onCeiling", onCeiling);
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		if(world.isRemote)
			return;
		boolean positive;
		if(otherPos.equals(pos.offset(facing, 1)))
			positive = true;
		else if(otherPos.equals(pos.offset(facing, -1)))
			positive = false;
		else
			return;
		StructuralArmTileEntity slope = null;
		{
			TileEntity atOther = world.getTileEntity(otherPos);
			if(atOther instanceof StructuralArmTileEntity)
			{
				StructuralArmTileEntity tmp = (StructuralArmTileEntity)atOther;
				BlockState stateHere = world.getBlockState(pos);
				BlockState stateThere = world.getBlockState(otherPos);
				if(tmp.facing==this.facing&&stateHere.getBlock()==stateThere.getBlock()&&tmp.onCeiling==this.onCeiling)
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
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, true, true, other -> {
				other.totalLength = totalLength-toEnd;
				if(!positive)
					other.slopePosition -= this.slopePosition;
				other.bounds = null;
				updateNoNeighbours(other.pos);
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
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			forEachSlopeBlockBeyond(!positive, false, false, other -> {
				other.totalLength = totalLength;
				if(!positive)
					other.slopePosition += totalLength-oldLength;
				other.bounds = null;
				updateNoNeighbours(other.pos);
			});
			bounds = null;
		}
		updateNoNeighbours(pos);
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
		BlockPos posI = pos.offset(facing, offsetToHere);
		TileEntity teAtI = world.getTileEntity(posI);
		if(teAtI instanceof StructuralArmTileEntity)
		{
			StructuralArmTileEntity slope = (StructuralArmTileEntity)teAtI;
			int offsetAtPos = slopePosition+offsetToHere;
			BlockState stateHere = world.getBlockState(pos);
			BlockState stateThere = world.getBlockState(posI);
			if((!removing||(slope.totalLength==this.totalLength&&slope.slopePosition==offsetAtPos))
					&&slope.onCeiling==this.onCeiling
					&&stateHere.getBlock()==stateThere.getBlock()
					&&slope.facing==this.facing)
				out.accept(slope);
		}
	}

	private void updateNoNeighbours(BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	@Override
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
		totalLength = 1;
		slopePosition = 0;
		bounds = null;
		if(world!=null)
			world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
	}

	@Override
	public Direction getFacingForPlacement(LivingEntity placer, BlockPos pos, Direction side, float hitX, float hitY,
										   float hitZ)
	{
		onCeiling = (side==DOWN)||(side!=UP&&hitY > .5);
		return IDirectionalTile.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
	{
		return side.getAxis()==Axis.Y;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return axis.getAxis()==Axis.Y;
	}

	private VoxelShape bounds = null;

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(bounds==null)
		{
			double lowerH = (slopePosition+.5)/totalLength;
			double upperH = (slopePosition+1.)/totalLength;
			List<AxisAlignedBB> basic;
			if(!onCeiling)
				basic = ImmutableList.of(
						new AxisAlignedBB(0, 0, 0, 1, lowerH, 1),
						new AxisAlignedBB(0, lowerH, 0, 1, upperH, .5)
				);
			else
				basic = ImmutableList.of(
						new AxisAlignedBB(0, 1-lowerH, 0, 1, 1, 1),
						new AxisAlignedBB(0, 1-upperH, 0, 1, 1-lowerH, .5)
				);
			bounds = VoxelShapes.empty();
			for(AxisAlignedBB aabb : basic)
			{
				AxisAlignedBB transformed = Utils.transformAABB(aabb, facing);
				VoxelShape subShape = VoxelShapes.create(transformed);
				bounds = VoxelShapes.combine(bounds, subShape, IBooleanFunction.OR);
			}
			bounds = bounds.simplify();
		}
		return bounds;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(BlockState object, List<BakedQuad> quads)
	{
		float lowerHeight = slopePosition/(float)totalLength;
		float upperHeight = (slopePosition+1F)/totalLength;
		double lowerV = 16*lowerHeight;
		double upperV = 16*upperHeight;
		TextureAtlasSprite tas = quads.get(0).func_187508_a();
		VertexFormat format = DefaultVertexFormats.BLOCK;
		quads = new ArrayList<>();
		Matrix4 mat = new Matrix4(facing);

		Vector3d[] vertices;
		{
			float y03 = onCeiling?1: upperHeight;
			float y12 = onCeiling?1: lowerHeight;
			float y47 = onCeiling?1-upperHeight: 0;
			float y56 = onCeiling?1-lowerHeight: 0;
			vertices = new Vector3d[]{
					new Vector3d(0, y03, 0),//0
					new Vector3d(0, y12, 1),//1
					new Vector3d(1, y12, 1),//2
					new Vector3d(1, y03, 0),//3
					new Vector3d(0, y47, 0),//4
					new Vector3d(0, y56, 1),//5
					new Vector3d(1, y56, 1),//6
					new Vector3d(1, y47, 0),//7
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

	private void addCulledQuad(List<BakedQuad> quads, VertexFormat format, Vector3d[] vertices, Direction side,
							   TextureAtlasSprite tas, double[] uvs, float[] alpha)
	{
		side = Utils.rotateFacingTowardsDir(side, this.facing);
		quads.add(ClientUtils.createBakedQuad(format, vertices, side, tas, uvs, alpha, false));
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = SHRINK.apply(vertices[i]);
		quads.add(ClientUtils.createBakedQuad(format, vertices, side.getOpposite(), tas, uvs, alpha, true));
	}

	private void addSides(List<BakedQuad> quads, Vector3d[] vertices, TextureAtlasSprite tas, double lowerV,
						  double upperV, boolean invert)
	{
		if(invert)
		{
			for(int i = 0; i < vertices.length; i++)
				vertices[i] = SHRINK.apply(vertices[i]);
		}
		quads.add(createSide(DefaultVertexFormats.BLOCK, getArrayByIndices(vertices, 5, 1, 0, 4),
				WEST, tas, lowerV, upperV, invert));
		quads.add(createSide(DefaultVertexFormats.BLOCK, getArrayByIndices(vertices, 7, 3, 2, 6),
				EAST, tas, upperV, lowerV, invert));
	}

	@OnlyIn(Dist.CLIENT)
	private BakedQuad createSide(VertexFormat format, Vector3d[] vertices, Direction facing, TextureAtlasSprite sprite,
								 double leftV, double rightV, boolean invert)
	{
		facing = Utils.rotateFacingTowardsDir(facing, this.facing);
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
		Vector3d faceNormal = Vector3d.func_237491_b_(facing.getDirectionVec());
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

	private Vector3d[] getArrayByIndices(Vector3d[] in, int... indices)
	{
		Vector3d[] ret = new Vector3d[indices.length];
		for(int i = 0; i < indices.length; i++)
			ret[i] = in[indices[i]];
		return ret;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return totalLength+","+slopePosition+","+facing.name()+","+(onCeiling?"1": "0");
	}
}
