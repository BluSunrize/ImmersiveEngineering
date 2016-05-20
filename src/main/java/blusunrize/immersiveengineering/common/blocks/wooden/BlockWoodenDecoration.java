package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWoodenDecoration extends BlockIEBase<BlockTypes_WoodenDecoration>
{
	public BlockWoodenDecoration()
	{
		super("woodenDecoration",Material.wood, PropertyEnum.create("type", BlockTypes_WoodenDecoration.class), ItemBlockIEBase.class, IEProperties.SIDECONNECTION[2],IEProperties.SIDECONNECTION[3],IEProperties.SIDECONNECTION[4],IEProperties.SIDECONNECTION[5]);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setBlockLayer(EnumWorldBlockLayer.CUTOUT);
	}

	@Override
	public boolean isFullBlock()
	{
		return false;
	}
	@Override
	public boolean isFullCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		//		int meta = world.getBlockMetadata(x, y, z);
		IBlockState state = world.getBlockState(pos);
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDecoration.FENCE.getMeta())
			return side==EnumFacing.UP;
		if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
			return true;
		//		TileEntity te = world.getTileEntity(x, y, z);
		//		if(te instanceof TileEntityStructuralArm)
		//		{
		//			if(side==UP)
		//				return ((TileEntityStructuralArm)te).inverted;
		//			else if(side==DOWN)
		//				return !((TileEntityStructuralArm)te).inverted;
		//			else
		//				return ((TileEntityStructuralArm)te).facing==side.getOpposite().ordinal();
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
		return super.isSideSolid(world, pos, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos.offset(side.getOpposite()));
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
		{
			IBlockState state2 = world.getBlockState(pos);
			if(this.equals(state2.getBlock()))
			{
				int meta2 = this.getMetaFromState(state2);
				return meta2!=BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta();
			}
		}
		return super.shouldSideBeRendered(world, pos, side);
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state  = super.getActualState(state, world, pos);
		if(this.getMetaFromState(state)==BlockTypes_WoodenDecoration.FENCE.getMeta())
			for(EnumFacing f : EnumFacing.HORIZONTALS)
				if(this.canConnectFenceTo(world, pos.offset(f)))
					state = state.withProperty(IEProperties.SIDECONNECTION[f.ordinal()], true);
		return state;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		//		TileEntity tileEntity = world.getTileEntity(x, y, z);
		IBlockState state = world.getBlockState(pos);
		if(this.getMetaFromState(state)==BlockTypes_WoodenDecoration.FENCE.getMeta())
			this.setBlockBounds(canConnectFenceTo(world,pos.add(-1,0,0))?0:.375f,0,canConnectFenceTo(world,pos.add(0,0,-1))?0:.375f, canConnectFenceTo(world,pos.add(1,0,0))?1:.625f,1,canConnectFenceTo(world,pos.add(0,0,1))?1:.625f);
		//		else if(tileEntity instanceof TileEntityLantern)
		//		{
		//			int f = ((TileEntityLantern)tileEntity).facing ;
		//			if(f<2)
		//				this.setBlockBounds(.25f,f==1?0:.125f,.25f, .75f,f==1?.875f:1f,.75f);
		//			else
		//				this.setBlockBounds(f==5?0:.25f,0,f==3?0:.25f, f==4?1:.75f,.875f,f==2?1:.75f);
		//		}
		//		else if(tileEntity instanceof TileEntityConnectorStructural)
		//		{
		//			float length = .5f;
		//			switch(((TileEntityConnectorStructural)tileEntity).facing )
		//			{
		//			case 0://UP
		//				this.setBlockBounds(.25f,0,.25f,  .75f,length,.75f);
		//				break;
		//			case 1://DOWN
		//				this.setBlockBounds(.25f,1-length,.25f,  .75f,1,.75f);
		//				break;
		//			case 2://SOUTH
		//				this.setBlockBounds(.25f,.25f,0,  .75f,.75f,length);
		//				break;
		//			case 3://NORTH
		//				this.setBlockBounds(.25f,.25f,1-length,  .75f,.75f,1);
		//				break;
		//			case 4://EAST
		//				this.setBlockBounds(0,.25f,.25f,  length,.75f,.75f);
		//				break;
		//			case 5://WEST
		//				this.setBlockBounds(1-length,.25f,.25f,  1,.75f,.75f);
		//				break;
		//			}
		//		}
		//		else if(tileEntity instanceof TileEntityWallmount)
		//		{
		//			TileEntityWallmount arm = (TileEntityWallmount)tileEntity;
		//			int f = arm.facing;
		//			if(arm.sideAttached>0)
		//				this.setBlockBounds(f==4?0:f==5?.375f:.3125f,arm.inverted?.3125f:0,f==2?0:f==3?.375f:.3125f, f==5?1:f==4?.625f:.6875f,arm.inverted?1:.6875f,f==3?1:f==2?.625f:.6875f);
		//			else
		//				this.setBlockBounds(f==5?0:.3125f,arm.inverted?.375f:0,f==3?0:.3125f, f==4?1:.6875f,arm.inverted?1:.625f,f==2?1:.6875f);
		//		}
		else
			this.setBlockBounds(0,0,0,1,1,1);
	}
	public boolean canConnectFenceTo(IBlockAccess world, BlockPos pos)
	{
		Block block = world.getBlockState(pos).getBlock();
		return block == Blocks.barrier ? false : (( !(block instanceof BlockFence || block.equals(this)) || block.getMaterial() != this.blockMaterial) && !(block instanceof BlockFenceGate) ? (block.getMaterial().isOpaque() && block.isFullCube() ? block.getMaterial() != Material.gourd : false) : true);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDecoration.FENCE.getMeta())
			this.setBlockBounds(canConnectFenceTo(world,pos.add(-1,0,0))?0:.375f,0,canConnectFenceTo(world,pos.add(0,0,-1))?0:.375f, canConnectFenceTo(world,pos.add(1,0,0))?1:.625f,1.5f,canConnectFenceTo(world,pos.add(0,0,1))?1:.625f);
		else if(meta==BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta())
			this.setBlockBounds(.0625f, 0, .0625f, .9375f, 1, .9375f);
		else
			this.setBlockBoundsBasedOnState(world, pos);
		return super.getCollisionBoundingBox(world, pos, state);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		this.setBlockBoundsBasedOnState(world, pos);
		return super.getSelectedBoundingBox(world, pos);
	}
	@Override
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return world.getBlockState(pos).getValue(property)==BlockTypes_WoodenDecoration.SCAFFOLDING;
	}
}