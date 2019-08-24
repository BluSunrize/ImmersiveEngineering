/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

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

import static blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration.FENCE;

public class WoodenDecorationBlock extends IELadderBlock<BlockTypes_WoodenDecoration>
{
	public WoodenDecorationBlock()
	{
		super("wooden_decoration", Material.WOOD, PropertyEnum.create("type", BlockTypes_WoodenDecoration.class), ItemBlockIEBase.class, FenceBlock.NORTH, FenceBlock.SOUTH, FenceBlock.WEST, FenceBlock.EAST);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setBlockLayer(BlockRenderLayer.CUTOUT);
		this.setAllNotNormalBlock();
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
			return "fence";
		return null;
	}

	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, Direction facing)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		if(meta==FENCE.getMeta())
		{
			BlockState connector = world.getBlockState(pos.offset(facing));
			return connector.getBlock() instanceof MetalDecoration1Block&&this.getMetaFromState(connector)==meta;
		}
		return super.canBeConnectedTo(world, pos, facing);
	}

	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		//		int meta = world.getBlockMetadata(x, y, z);
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
			return true;
		//		TileEntity te = world.getTileEntity(x, y, z);
		//		if(te instanceof StructuralArmTileEntity)
		//		{
		//			if(side==UP)
		//				return ((StructuralArmTileEntity)te).inverted;
		//			else if(side==DOWN)
		//				return !((StructuralArmTileEntity)te).inverted;
		//			else
		//				return ((StructuralArmTileEntity)te).facing==side.getOpposite().ordinal();
		//		}
		//		if(meta==META_radiator||meta==META_heavyEngineering||meta==META_generator||meta==META_lightEngineering||meta==META_sheetMetal)
		//			return true;
		//		if(te instanceof TileEntityWallmount)
		//		{
		//			if(side==UP)
		//				return ((TileEntityWallmount)te).inverted;
		//			else if(side==DOWN)
		//				return !((TileEntityWallmount)te).inverted;
		//			else
		//				return true;
		//		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==FENCE.getMeta())
			return side!=Direction.UP&&side!=Direction.DOWN?BlockFaceShape.MIDDLE_POLE: BlockFaceShape.CENTER;
		return BlockFaceShape.SOLID;
	}

	@Override
	public boolean shouldSideBeRendered(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
		{
			BlockState state2 = world.getBlockState(pos.offset(side));
			if(this.equals(state2.getBlock()))
			{
				int meta2 = this.getMetaFromState(state2);
				return meta2!=BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta();
			}
		}
		return super.shouldSideBeRendered(state, world, pos, side);
	}


	@Override
	public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		if(this.getMetaFromState(state)==FENCE.getMeta())
			for(Direction f : Direction.HORIZONTALS)
				state = state.with(f==Direction.NORTH?FenceBlock.NORTH: f==Direction.SOUTH?FenceBlock.SOUTH: f==Direction.WEST?FenceBlock.WEST: FenceBlock.EAST, Utils.canFenceConnectTo(world, pos, f));
		return state;
	}

	//	@Override
//	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
//	{
//		//		TileEntity tileEntity = world.getTileEntity(x, y, z);
//		IBlockState state = world.getBlockState(pos);
//		if(this.getMetaFromState(state)==BlockTypes_WoodenDecoration.FENCE.getMeta())
//			this.setBlockBounds(canConnectFenceTo(world,pos.add(-1,0,0))?0:.375f,0,canConnectFenceTo(world,pos.add(0,0,-1))?0:.375f, canConnectFenceTo(world,pos.add(1,0,0))?1:.625f,1,canConnectFenceTo(world,pos.add(0,0,1))?1:.625f);
//		//		else if(tileEntity instanceof LanternTileEntity)
//		//		{
//		//			int f = ((LanternTileEntity)tileEntity).facing ;
//		//			if(f<2)
//		//				this.setBlockBounds(.25f,f==1?0:.125f,.25f, .75f,f==1?.875f:1f,.75f);
//		//			else
//		//				this.setBlockBounds(f==5?0:.25f,0,f==3?0:.25f, f==4?1:.75f,.875f,f==2?1:.75f);
//		//		}
//		//		else if(tileEntity instanceof ConnectorStructuralTileEntity)
//		//		{
//		//			float length = .5f;
//		//			switch(((ConnectorStructuralTileEntity)tileEntity).facing )
//		//			{
//		//			case 0://UP
//		//				this.setBlockBounds(.25f,0,.25f,  .75f,length,.75f);
//		//				break;
//		//			case 1://DOWN
//		//				this.setBlockBounds(.25f,1-length,.25f,  .75f,1,.75f);
//		//				break;
//		//			case 2://SOUTH
//		//				this.setBlockBounds(.25f,.25f,0,  .75f,.75f,length);
//		//				break;
//		//			case 3://NORTH
//		//				this.setBlockBounds(.25f,.25f,1-length,  .75f,.75f,1);
//		//				break;
//		//			case 4://EAST
//		//				this.setBlockBounds(0,.25f,.25f,  length,.75f,.75f);
//		//				break;
//		//			case 5://WEST
//		//				this.setBlockBounds(1-length,.25f,.25f,  1,.75f,.75f);
//		//				break;
//		//			}
//		//		}
//		//		else if(tileEntity instanceof TileEntityWallmount)
//		//		{
//		//			TileEntityWallmount arm = (TileEntityWallmount)tileEntity;
//		//			int f = arm.facing;
//		//			if(arm.sideAttached>0)
//		//				this.setBlockBounds(f==4?0:f==5?.375f:.3125f,arm.inverted?.3125f:0,f==2?0:f==3?.375f:.3125f, f==5?1:f==4?.625f:.6875f,arm.inverted?1:.6875f,f==3?1:f==2?.625f:.6875f);
//		//			else
//		//				this.setBlockBounds(f==5?0:.3125f,arm.inverted?.375f:0,f==3?0:.3125f, f==4?1:.6875f,arm.inverted?1:.625f,f==2?1:.6875f);
//		//		}
//		else
//			this.setBlockBounds(0,0,0,1,1,1);
//	}

	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
	{
		state = state.getActualState(worldIn, pos);
		if(getMetaFromState(state)==FENCE.getMeta())
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.PILLAR_AABB);
			if(state.getValue(FenceBlock.NORTH))
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.NORTH_AABB);
			if(state.getValue(FenceBlock.EAST))
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.EAST_AABB);
			if(state.getValue(FenceBlock.SOUTH))
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.SOUTH_AABB);
			if(state.getValue(FenceBlock.WEST))
				addCollisionBoxToList(pos, entityBox, collidingBoxes, FenceBlock.WEST_AABB);
		}
		else
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(.0625f, 0, .0625f, .9375f, 1, .9375f));
	}

	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos)
	{
		int meta = this.getMetaFromState(state);
		if(meta==FENCE.getMeta())
			return new AxisAlignedBB(Utils.canFenceConnectTo(world, pos, Direction.WEST)?0: .375f, 0, Utils.canFenceConnectTo(world, pos, Direction.NORTH)?0: .375f, Utils.canFenceConnectTo(world, pos, Direction.EAST)?1: .625f, 1f, Utils.canFenceConnectTo(world, pos, Direction.SOUTH)?1: .625f);
		else if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
			return FULL_BLOCK_AABB;

		return super.getBoundingBox(state, world, pos);
	}

	private static int getBoundingBoxIdx(BlockState state)
	{
		int i = 0;
		if(state.getValue(FenceBlock.NORTH))
			i |= 1<<Direction.NORTH.getHorizontalIndex();
		if(state.getValue(FenceBlock.EAST))
			i |= 1<<Direction.EAST.getHorizontalIndex();
		if(state.getValue(FenceBlock.SOUTH))
			i |= 1<<Direction.SOUTH.getHorizontalIndex();
		if(state.getValue(FenceBlock.WEST))
			i |= 1<<Direction.WEST.getHorizontalIndex();
		return i;
	}

	@Override
	public boolean isLadder(BlockState state, IBlockAccess world, BlockPos pos, LivingEntity entity)
	{
		return world.getBlockState(pos).getValue(property)==BlockTypes_WoodenDecoration.SCAFFOLDING;
	}


	@Nullable
	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockAccess world, BlockPos pos)
	{
		if(state.getValue(property)==FENCE)
			return PathNodeType.FENCE;
		else
			return super.getAiPathNodeType(state, world, pos);
	}

	@Override
	public boolean canPlaceTorchOnTop(BlockState state, IBlockAccess world, BlockPos pos)
	{
		//TODO remove in 1.13 when fences extend BlockFence (Mojang has a special case for fences)
		return state.getValue(property)==FENCE||super.canPlaceTorchOnTop(state, world, pos);
	}
}