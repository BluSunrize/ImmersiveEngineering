package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase.IELadderBlock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockMetalDecoration1 extends IELadderBlock<BlockTypes_MetalDecoration1>
{
	public BlockMetalDecoration1()
	{
		super("metalDecoration1", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDecoration1.class), ItemBlockIEBase.class, BlockFence.NORTH,BlockFence.SOUTH,BlockFence.WEST,BlockFence.EAST);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		this.setBlockLayer(BlockRenderLayer.CUTOUT_MIPPED);
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
		if(meta==0 && !itemBlock)
			return "steelFence";
		else if(meta==4 && !itemBlock)
			return "aluminumFence";
		return null;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta() || meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			return side==EnumFacing.UP;
		if(meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()||meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()||meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()
				||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta())
			return true;
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()||meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()||meta==BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()
				||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()||meta==BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta())
		{
			IBlockState state2 = world.getBlockState(pos.offset(side));
			if(this.equals(state2.getBlock()))
			{
				int meta2 = this.getMetaFromState(state2);
				return meta2!=BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta();
			}
		}
		return super.shouldSideBeRendered(state, world, pos, side);
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state  = super.getActualState(state, world, pos);
		if(this.getMetaFromState(state)==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta() || this.getMetaFromState(state)==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			for(EnumFacing f : EnumFacing.HORIZONTALS)
				state = state.withProperty(f==EnumFacing.NORTH?BlockFence.NORTH:f==EnumFacing.SOUTH?BlockFence.SOUTH:f==EnumFacing.WEST?BlockFence.WEST:BlockFence.EAST, this.canConnectFenceTo(world, pos.offset(f)));
		return state;
	}

	//	@Override
//	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
//	{
//		//		TileEntity tileEntity = world.getTileEntity(x, y, z);
//		IBlockState state = world.getBlockState(pos);
//		if(this.getMetaFromState(state)==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta() || this.getMetaFromState(state)==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
//			this.setBlockBounds(canConnectFenceTo(world,pos.add(-1,0,0))?0:.375f,0,canConnectFenceTo(world,pos.add(0,0,-1))?0:.375f, canConnectFenceTo(world,pos.add(1,0,0))?1:.625f,1,canConnectFenceTo(world,pos.add(0,0,1))?1:.625f);
//		//		else if(tileEntity instanceof TileEntityLantern)
//		//		{
//		//			int f = ((TileEntityLantern)tileEntity).facing ;
//		//			if(f<2)
//		//				this.setBlockBounds(.25f,f==1?0:.125f,.25f, .75f,f==1?.875f:1f,.75f);
//		//			else
//		//				this.setBlockBounds(f==5?0:.25f,0,f==3?0:.25f, f==4?1:.75f,.875f,f==2?1:.75f);
//		//		}
//		//		else if(tileEntity instanceof TileEntityConnectorStructural)
//		//		{
//		//			float length = .5f;
//		//			switch(((TileEntityConnectorStructural)tileEntity).facing )
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
	public boolean canConnectFenceTo(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		return block != Blocks.BARRIER && (!((!(block instanceof BlockFence || block.equals(this)) || state.getMaterial() != this.blockMaterial) && !(block instanceof BlockFenceGate)) || ((state.getMaterial().isOpaque() && state.isFullCube()) && state.getMaterial() != Material.GOURD));
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
	{
		state = state.getActualState(worldIn, pos);
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta() || meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
		{
			addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFence.PILLAR_AABB);
			if(state.getValue(BlockFence.NORTH).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFence.NORTH_AABB);
			if(state.getValue(BlockFence.EAST).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFence.EAST_AABB);
			if(state.getValue(BlockFence.SOUTH).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFence.SOUTH_AABB);
			if(state.getValue(BlockFence.WEST).booleanValue())
				addCollisionBoxToList(pos, entityBox, collidingBoxes, BlockFence.WEST_AABB);
		}
		else
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(.0625f, 0, .0625f, .9375f, 1, .9375f));
	}
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta() || meta==BlockTypes_MetalDecoration1.ALUMINUM_FENCE.getMeta())
			return new AxisAlignedBB(canConnectFenceTo(world,pos.add(-1,0,0))?0:.375f,0,canConnectFenceTo(world,pos.add(0,0,-1))?0:.375f, canConnectFenceTo(world,pos.add(1,0,0))?1:.625f,1f,canConnectFenceTo(world,pos.add(0,0,1))?1:.625f);
		return super.getBoundingBox(state, world, pos);
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return world.getBlockState(pos).getValue(property).getMeta()%4!=0;
	}
}