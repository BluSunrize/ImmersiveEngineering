package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalDevice0 extends BlockIETileProvider<BlockTypes_MetalDevice0>
{
	public BlockMetalDevice0()
	{
		super("metalDevice0",Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDevice0.class), ItemBlockIEBase.class, IEProperties.MULTIBLOCKSLAVE,IEProperties.SIDECONFIG[0],IEProperties.SIDECONFIG[1],IEProperties.SIDECONFIG[2],IEProperties.SIDECONFIG[3],IEProperties.SIDECONFIG[4],IEProperties.SIDECONFIG[5]);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setNotNormalBlock(BlockTypes_MetalDevice0.FLUID_PUMP.getMeta());
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_MetalDevice0.values()[meta]==BlockTypes_MetalDevice0.FLUID_PUMP)
			return "fluidPump";
		return null;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if(stack.getItemDamage()==BlockTypes_MetalDevice0.FLUID_PUMP.getMeta())
			return world.isAirBlock(pos.add(0,1,0));
		return true;
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
//		TileEntity te = world.getTileEntity(pos);
//		if(te instanceof TileEntityCapacitorLV)
//		{
//			ItemStack stack = new ItemStack(this,1,this.getMetaFromState(world.getBlockState(pos)));
//			if(((TileEntityCapacitorLV)te).energyStorage.getEnergyStored()>0)
//				ItemNBTHelper.setInt(stack, "energyStorage", ((TileEntityCapacitorLV)te).energyStorage.getEnergyStored());
//			int[] sides = ((TileEntityCapacitorLV)te).sideConfig;
//			ItemNBTHelper.setIntArray(stack, "sideConfig", sides);
//			return stack;
//		}
		return super.getPickBlock(state, target, world, pos, player);
	}
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
//		TileEntity te = world.getTileEntity(pos);
//		if(!world.isRemote && te instanceof TileEntityCapacitorLV && player!=null && !player.capabilities.isCreativeMode)
//		{
//			ItemStack stack = new ItemStack(this,1,this.getMetaFromState(state));
//			if(((TileEntityCapacitorLV)te).energyStorage.getEnergyStored()>0)
//				ItemNBTHelper.setInt(stack, "energyStorage", ((TileEntityCapacitorLV)te).energyStorage.getEnergyStored());
//			int[] sides = ((TileEntityCapacitorLV)te).sideConfig;
//			//			if(sides[0]!=-1 || sides[1]!=0||sides[2]!=0||sides[3]!=0||sides[4]!=0||sides[5]!=0)
//			ItemNBTHelper.setIntArray(stack, "sideConfig", sides);
//			world.spawnEntityInWorld(new EntityItem(world,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,stack));
//		}
	}
	//	@Override
	//	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	//	{
	//		if(metadata==META_capacitorLV||metadata==META_capacitorMV||metadata==META_capacitorHV)
	//			return new ArrayList();
	//		ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, metadata, fortune);
	//		return ret;
	//	}

	//	@Override
	//	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityCapacitorLV && Utils.isHammer(player.getCurrentEquippedItem()))
	//		{
	//			if(player.isSneaking())
	//				side = ForgeDirection.OPPOSITES[side];
	//			if(!world.isRemote)
	//			{
	//				((TileEntityCapacitorLV)te).toggleSide(side);
	//				te.markDirty();
	//				world.markBlockForUpdate(x, y, z);
	//				world.addBlockEvent(x, y, z, te.getBlockType(), 0, 0);
	//			}
	//			return true;
	//		}
	//		if(te instanceof TileEntityDynamo && Utils.isHammer(player.getCurrentEquippedItem()))
	//		{
	//			if(!world.isRemote)
	//			{
	//				int f = ((TileEntityDynamo) te).facing;
	//				f = ForgeDirection.ROTATION_MATRIX[player.isSneaking()? 1: 0][f];
	//				((TileEntityDynamo) te).facing = f;
	//				te.markDirty();
	//				world.func_147451_t(x, y, z);
	//				world.markBlockForUpdate(x, y, z);
	//				world.playSoundEffect(x+.5, y+.5, z+.5, "random.door_open", .5f, 2f);
	//			}
	//			return true;
	//		}
	//		if(te instanceof TileEntityConveyorBelt && Utils.isHammer(player.getCurrentEquippedItem()))
	//		{
	//			if(!world.isRemote)
	//			{
	//				TileEntityConveyorBelt tile = (TileEntityConveyorBelt) te;
	//				if(player.isSneaking())
	//				{
	//					if(tile.transportUp)
	//					{
	//						tile.transportUp = false;
	//						tile.transportDown = true;
	//					}
	//					else if(tile.transportDown)
	//					{
	//						tile.transportDown = false;
	//					}
	//					else
	//						tile.transportUp = true;
	//				}
	//				else
	//					tile.facing = ForgeDirection.ROTATION_MATRIX[1][tile.facing];
	//				world.markBlockForUpdate(x, y, z);
	//			}
	//			return true;
	//		}
	//		if(te instanceof TileEntityFurnaceHeater && Utils.isHammer(player.getCurrentEquippedItem()))
	//		{
	//			if(player.isSneaking())
	//				side = ForgeDirection.OPPOSITES[side];
	//			if(!world.isRemote)
	//			{
	//				((TileEntityFurnaceHeater)te).toggleSide(side);
	//				te.markDirty();
	//				world.func_147451_t(x, y, z);
	//			}
	//			return true;
	//		}
	//		if(te instanceof TileEntityConveyorSorter)
	//		{
	//			if(!player.isSneaking())
	//			{
	//				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Sorter, world, x, y, z);
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_MetalDevice0.values()[meta])
		{
			case CAPACITOR_LV:
				return new TileEntityCapacitorLV();
			case CAPACITOR_MV:
				return new TileEntityCapacitorMV();
			case CAPACITOR_HV:
				return new TileEntityCapacitorHV();
			case CAPACITOR_CREATIVE:
				return new TileEntityCapacitorCreative();
			case BARREL:
				return new TileEntityMetalBarrel();
			case FLUID_PUMP:
				return new TileEntityFluidPump();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
}