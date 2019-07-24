/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock.IELadderBlock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1.ALUMINUM_FENCE;
import static blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1.STEEL_FENCE;

public class MetalDecoration1Block extends IELadderBlock<BlockTypes_MetalDecoration1>
{
	public MetalDecoration1Block()
	{
		super("metal_decoration1", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration1.class), ItemBlockIEBase.class, FenceBlock.NORTH, FenceBlock.SOUTH, FenceBlock.WEST, FenceBlock.EAST);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		this.setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
		this.setNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(meta==0&&!itemBlock)
			return "steel_fence";
		else if(meta==4&&!itemBlock)
			return "aluminum_fence";
		return null;
	}

	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, Direction facing)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		if(meta==STEEL_FENCE.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
		{
			BlockState connector = world.getBlockState(pos.offset(facing));
			return connector.getBlock() instanceof MetalDecoration1Block&&this.getMetaFromState(connector)==meta;
		}
		return super.canBeConnectedTo(world, pos, facing);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==STEEL_FENCE.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			return side!=Direction.UP&&side!=Direction.DOWN?BlockFaceShape.MIDDLE_POLE: BlockFaceShape.CENTER;
		return BlockFaceShape.SOLID;
	}

	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		if(state.getValue(this.property).isScaffold())
			return true;
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean shouldSideBeRendered(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		int meta = this.getMetaFromState(state);
		if(state.getValue(this.property).isScaffold())
		{
			BlockState state2 = world.getBlockState(pos.offset(side));
			if(this.equals(state2.getBlock()))
				return this.getMetaFromState(state2)!=meta;
		}
		return super.shouldSideBeRendered(state, world, pos, side);
	}


	@Override
	public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		if(this.getMetaFromState(state)==STEEL_FENCE.getMeta()||this.getMetaFromState(state)==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			for(Direction f : Direction.HORIZONTALS)
				state = state.with(f==Direction.NORTH?FenceBlock.NORTH: f==Direction.SOUTH?FenceBlock.SOUTH: f==Direction.WEST?FenceBlock.WEST: FenceBlock.EAST, Utils.canFenceConnectTo(world, pos, f));
		return state;
	}

	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
	{
		state = state.getActualState(worldIn, pos);
		int meta = this.getMetaFromState(state);
		if(meta==STEEL_FENCE.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.PILLAR_AABB);
			if(state.getValue(FenceBlock.NORTH).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.NORTH_AABB);
			if(state.getValue(FenceBlock.EAST).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.EAST_AABB);
			if(state.getValue(FenceBlock.SOUTH).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.SOUTH_AABB);
			if(state.getValue(FenceBlock.WEST).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.WEST_AABB);
		}
		else
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(.0625f, 0, .0625f, .9375f, 1, .9375f));
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos)
	{
		int meta = this.getMetaFromState(state);
		if(meta==STEEL_FENCE.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			return new AxisAlignedBB(Utils.canFenceConnectTo(world, pos, Direction.WEST)?0: .375f, 0, Utils.canFenceConnectTo(world, pos, Direction.NORTH)?0: .375f, Utils.canFenceConnectTo(world, pos, Direction.EAST)?1: .625f, 1f, Utils.canFenceConnectTo(world, pos, Direction.SOUTH)?1: .625f);
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public boolean isLadder(BlockState state, IBlockAccess world, BlockPos pos, LivingEntity entity)
	{
		return world.getBlockState(pos).getValue(property).getMeta()%4!=0;
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockAccess world, BlockPos pos)
	{
		switch(state.getValue(property))
		{
			case STEEL_FENCE:
			case ALUMINUM_FENCE:
				return PathNodeType.FENCE;
			default:
				return super.getAiPathNodeType(state, world, pos);
		}
	}


	@Override
	public boolean canPlaceTorchOnTop(BlockState state, IBlockAccess world, BlockPos pos)
	{
		//TODO remove in 1.13 when fences extend BlockFence (Mojang has a special case for instanceof BlockFence)
		BlockTypes_MetalDecoration1 type = state.getValue(property);
		return type==STEEL_FENCE||type==ALUMINUM_FENCE||super.canPlaceTorchOnTop(state, world, pos);
	}
}