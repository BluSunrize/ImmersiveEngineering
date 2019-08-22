/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.generic.PostTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WoodenDevice1Block extends IETileProviderBlock<BlockTypes_WoodenDevice1> implements IPostBlock
{
	public WoodenDevice1Block()
	{
		super("wooden_device1", Material.WOOD, PropertyEnum.create("type", BlockTypes_WoodenDevice1.class), ItemBlockIEBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.INT_4, Properties.AnimationProperty);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
		this.setMetaMobilityFlag(BlockTypes_WoodenDevice1.POST.getMeta(), PushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_WoodenDevice1.WATERMILL.getMeta(), PushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_WoodenDevice1.WINDMILL.getMeta(), PushReaction.BLOCK);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune)
	{
		if(this.getMetaFromState(state)==BlockTypes_WoodenDevice1.POST.getMeta())
			return;
		super.getDrops(drops, world, pos, state, fortune);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof PostTileEntity)
		{
			if(!((PostTileEntity)tileEntity).isDummy()&&!world.isRemote&&world.getGameRules().getBoolean("doTileDrops")&&!world.restoringBlockSnapshots)
				world.addEntity(new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, new ItemStack(this, 1, this.getMetaFromState(state))));
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, Direction facing)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		if(meta==BlockTypes_WoodenDevice1.WALLMOUNT.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityWallmount)
			{
				if(facing==Direction.UP)
					return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2;
				else if(facing==Direction.DOWN)
					return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3;
				else
					return facing==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing);
			}
		}
		return super.canBeConnectedTo(world, pos, facing);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction side)
	{
		int meta = this.getMetaFromState(state);
		if(meta==BlockTypes_WoodenDevice1.WALLMOUNT.getMeta())
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityWallmount)
			{
				if(side==Direction.UP)
					return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
				else if(side==Direction.DOWN)
					return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
				else
					return side==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing)?BlockFaceShape.CENTER: BlockFaceShape.UNDEFINED;
			}
		}
		return BlockFaceShape.SOLID;
	}

	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof PostTileEntity)
		{
			return ((PostTileEntity)te).dummy==0?side==Direction.DOWN: ((PostTileEntity)te).dummy==3?side==Direction.UP: ((PostTileEntity)te).dummy > 3?side.getAxis()==Axis.Y: side.getAxis()!=Axis.Y;
		}
		if(te instanceof TileEntityWallmount)
		{
			if(side==Direction.UP)
				return ((TileEntityWallmount)te).orientation==0||((TileEntityWallmount)te).orientation==2;
			else if(side==Direction.DOWN)
				return ((TileEntityWallmount)te).orientation==1||((TileEntityWallmount)te).orientation==3;
			else
				return side==(((TileEntityWallmount)te).orientation > 1?((TileEntityWallmount)te).facing.getOpposite(): ((TileEntityWallmount)te).facing);
		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean isLadder(BlockState state, IBlockAccess world, BlockPos pos, LivingEntity entity)
	{
		return (world.getTileEntity(pos) instanceof PostTileEntity);
	}

	//	@Override
	//	public boolean canConnectTransformer(IBlockAccess world, BlockPos pos)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(pos);
	//		return tileEntity instanceof PostTileEntity && ((PostTileEntity) tileEntity).dummy>0&&((PostTileEntity)tileEntity).dummy<=3;
	//	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		if(stack.getItemDamage()==BlockTypes_WoodenDevice1.WATERMILL.getMeta())
		{
			Direction f = Direction.fromAngle(player.rotationYaw);
			for(int hh = -2; hh <= 2; hh++)
				for(int ww = -2; ww <= 2; ww++)
					if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
					{
						BlockPos pos2 = pos.add(f.getAxis()==Axis.Z?ww: 0, hh, f.getAxis()==Axis.Z?0: ww);
						if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
							return false;
					}
		}
		else if(stack.getItemDamage()==BlockTypes_WoodenDevice1.POST.getMeta())
		{
			for(int hh = 1; hh <= 3; hh++)
			{
				BlockPos pos2 = pos.add(0, hh, 0);
				if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
					return false;
			}
		}
		return true;
	}

	//	@Override
	//	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof WoodenBarrelTileEntity)
	//		{
	//			ItemStack stack = new ItemStack(this,1,world.getBlockMetadata(x, y, z));
	//			NBTTagCompound tag = new NBTTagCompound();
	//			((WoodenBarrelTileEntity) te).writeTank(tag, true);
	//			if(!tag.isEmpty())
	//				stack.setTagCompound(tag);
	//			return stack;
	//		}
	//		return super.getPickBlock(target, world, x, y, z, player);
	//	}

	//	@Override
	//	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(!world.isRemote)
	//		{
	//			if(te instanceof WoodenCrateTileEntity)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, meta);
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((WoodenCrateTileEntity) te).writeInv(tag, true);
	//				if(!tag.isEmpty())
	//					stack.setTagCompound(tag);
	//				world.addEntity(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//
	//			if(te instanceof WoodenBarrelTileEntity)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, meta);
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((WoodenBarrelTileEntity) te).writeTank(tag, true);
	//				if(!tag.isEmpty())
	//					stack.setTagCompound(tag);
	//				world.addEntity(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//		}
	//	}

	//	@Override
	//	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	//	{
	//		if(!world.isRemote)
	//		{
	//			TileEntity te = world.getTileEntity(x, y, z);
	//			if(te instanceof WoodenCrateTileEntity)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((WoodenCrateTileEntity) te).writeInv(tag, true);
	//				if(!tag.isEmpty())
	//					stack.setTagCompound(tag);
	//				world.addEntity(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//
	//			if(te instanceof WoodenBarrelTileEntity)
	//			{
	//				ItemStack stack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	//				NBTTagCompound tag = new NBTTagCompound();
	//				((WoodenBarrelTileEntity) te).writeTank(tag, true);
	//				if(!tag.isEmpty())
	//					stack.setTagCompound(tag);
	//				world.addEntity(new EntityItem(world, x+.5, y+.5, z+.5, stack));
	//			}
	//		}
	//		super.onBlockExploded(world, x, y, z, explosion);
	//	}

	//	@Override
	//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	//	{
	//		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
	//		if(metadata==0 || metadata==4 || metadata==6)
	//			return ret;
	//
	//		int count = quantityDropped(metadata, fortune, Utils.RAND);
	//		for(int i = 0; i < count; i++)
	//		{
	//			Item item = getItemDropped(metadata, Utils.RAND, fortune);
	//			if (item != null)
	//			{
	//				ret.add(new ItemStack(item, 1, damageDropped(metadata)));
	//			}
	//		}
	//		return ret;
	//	}
	//	@Override
	//	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	//	{
	//		TileEntity tileEntity = world.getTileEntity(x, y, z);
	//		if(tileEntity instanceof PostTileEntity)
	//		{
	//			int yy=y;
	//			byte type = ((PostTileEntity)tileEntity).type;
	//			switch(type)
	//			{
	//			case 4:
	//			case 5:
	//			case 6:
	//			case 7:
	//				return;
	//			default:
	//				yy-= ((PostTileEntity)tileEntity).type;
	//				break;
	//			}
	//
	//			for(int i=0;i<=3;i++)
	//			{
	//				world.removeBlock(x,yy+i,z);
	//				if(i==3)
	//				{
	//					TileEntity te;
	//					for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST})
	//					{
	//						te = world.getTileEntity(x+fd.offsetX, yy+i, z+fd.offsetZ);
	//						if(te instanceof PostTileEntity && ((PostTileEntity) te).type==(2+fd.ordinal()))
	//							world.removeBlock(x+fd.offsetX, yy+i, z+fd.offsetZ);
	//					}
	//				}
	//			}
	//			if(type==0 && !world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && !world.restoringBlockSnapshots)
	//				world.addEntity(new EntityItem(world, x+.5,y+.5,z+.5, new ItemStack(this,1,0)));
	//		}
	//		if(tileEntity instanceof WatermillTileEntity)
	//		{
	//			int[] off = ((WatermillTileEntity)tileEntity).offset;
	//			int f = ((WatermillTileEntity)tileEntity).facing;
	//			int xx = x - ((f==2||f==3)?off[0]:0);
	//			int yy = y - off[1];
	//			int zz = z - ((f==2||f==3)?0:off[0]);
	//
	//			if(!(off[0]==0&&off[1]==0) && world.isAirBlock(xx, yy, zz))
	//				return;
	//			world.removeBlock(xx, yy, zz);
	//			for(int hh=-2;hh<=2;hh++)
	//			{
	//				int r=hh<-1||hh>1?1:2;
	//				for(int ww=-r;ww<=r;ww++)
	//					world.removeBlock(xx+((f==2||f==3)?ww:0), yy+hh, zz+((f==2||f==3)?0:ww));
	//			}
	//		}
	//		if(tileEntity instanceof ModWorkbenchTileEntity)
	//		{
	//			ModWorkbenchTileEntity tile = (ModWorkbenchTileEntity)tileEntity;
	//			int f = tile.facing;
	//			int off = tile.dummyOffset;
	//			if(tile.dummy)
	//				off *= -1;
	//			int xx = x+(f<4?off:0);
	//			int zz = z+(f>3?off:0);
	//
	//			if(world.getTileEntity(xx, y, zz) instanceof ModWorkbenchTileEntity)
	//				world.removeBlock(xx, y, zz);
	//			if(!world.isRemote && !tile.dummy && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
	//				for(int i=0; i<tile.getSizeInventory(); i++)
	//				{
	//					ItemStack stack = tile.getStackInSlot(i);
	//					if(stack!=null)
	//					{
	//						float fx = Utils.RAND.nextFloat() * 0.8F + 0.1F;
	//						float fz = Utils.RAND.nextFloat() * 0.8F + 0.1F;
	//
	//						EntityItem entityitem = new EntityItem(world, x+fx, y+.5, z+fz, stack);
	//						entityitem.motionX = Utils.RAND.nextGaussian()*.05;
	//						entityitem.motionY = Utils.RAND.nextGaussian()*.05+.2;
	//						entityitem.motionZ = Utils.RAND.nextGaussian()*.05;
	//						if(stack.hasTag())
	//							entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
	//						world.addEntity(entityitem);
	//					}
	//				}
	//		}
	//		super.breakBlock(world, x, y, z, par5, par6);
	//	}


	@Override
	public TileEntity createBasicTE(World world, BlockTypes_WoodenDevice1 type)
	{
		switch(type)
		{
			case WATERMILL:
				return new WatermillTileEntity();
			case WINDMILL:
				return new WindmillTileEntity();
//		case WINDMILL_ADVANCED:
//			return new TileEntityWindmillAdvanced();
			case POST:
				return new PostTileEntity();
			case WALLMOUNT:
				return new TileEntityWallmount();
		}
		return null;
	}

	@Override
	public boolean canConnectTransformer(IBlockAccess world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		BlockTypes_WoodenDevice1 type = state.getValue(property);
		boolean slave = state.getValue(IEProperties.MULTIBLOCKSLAVE);
		return slave&&type==BlockTypes_WoodenDevice1.POST;
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}
}