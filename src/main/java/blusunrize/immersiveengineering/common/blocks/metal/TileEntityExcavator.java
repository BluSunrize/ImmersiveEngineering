package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class TileEntityExcavator extends TileEntityMultiblockMetal<TileEntityExcavator,IMultiblockRecipe> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds
{
	public TileEntityExcavator()
	{
		super(MultiblockExcavator.instance, new int[]{3,6,3}, 64000, true);
	}

	public boolean active = false;


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public void update()
	{
		super.update();
		if(isDummy())
			return;
		if(!worldObj.isRemote)
		{
			BlockPos wheelPos = this.getBlockPosForPos(31);
			TileEntity center = worldObj.getTileEntity(wheelPos);

			if(center instanceof TileEntityBucketWheel)
			{
				float rot = 0;
				int target = -1;
				TileEntityBucketWheel wheel = ((TileEntityBucketWheel) center);
				EnumFacing fRot = this.facing.rotateYCCW();
				if(wheel.facing==fRot)
				{
					if(active!=wheel.active)
						worldObj.addBlockEvent(wheel.getPos(), wheel.getBlockType(), 0, active? 1: 0);
					rot = wheel.rotation;
					if(rot%45>40)
						target = Math.round(rot/360f*8)%8;
				}

				//Fix the wheel if necessary
				if(wheel.facing!=fRot || wheel.mirrored!=this.mirrored)
					for(int h=-3;h<=3;h++)
						for(int w=-3;w<=3;w++)
						{
							TileEntity te = worldObj.getTileEntity(wheelPos.add((facing.getAxis()==Axis.X?w:0), h, (facing.getAxis()==Axis.Z?w:0)));
							if(te instanceof TileEntityBucketWheel)
							{
								((TileEntityBucketWheel)te).facing = fRot;
								((TileEntityBucketWheel)te).mirrored = this.mirrored;
								te.markDirty();
								this.markContainingBlockForUpdate(null);
							}
						}

				if(!isRSDisabled())
				{
					ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(worldObj, wheelPos.getX()>>4, wheelPos.getZ()>>4);

					int consumed = Config.getInt("excavator_consumption");
					int extracted = energyStorage.extractEnergy(consumed, true);
					if(extracted>=consumed)
					{
						energyStorage.extractEnergy(consumed, false);
						active = true;
						
						if(target>=0 && target<8)
						{
							if(wheel.digStacks[(target+4)%8]==null)
							{
								ItemStack blocking = this.digBlocksInTheWay(wheel);
								BlockPos lowGroundPos = wheelPos.add(0,-5,0);
								if(blocking!=null)
								{
									wheel.digStacks[(target+4)%8] = blocking;
									wheel.markDirty();
									this.markContainingBlockForUpdate(null);
								}
								else if(mineral!=null 
										&& !worldObj.isAirBlock(lowGroundPos.offset(facing, -2))
										&& !worldObj.isAirBlock(lowGroundPos.offset(facing, 2))
										&& !worldObj.isAirBlock(lowGroundPos.offset(facing, -1))
										&& !worldObj.isAirBlock(lowGroundPos.offset(facing, 1))
										&& !worldObj.isAirBlock(lowGroundPos))
								{
									ItemStack ore = mineral.getRandomOre(worldObj.rand);
									float configChance = worldObj.rand.nextFloat();
									float failChance = worldObj.rand.nextFloat();
									if(ore!=null && configChance>Config.getDouble("excavator_chance") && failChance>mineral.failChance)
									{
										wheel.digStacks[(target+4)%8] = ore;
										wheel.markDirty();
										this.markContainingBlockForUpdate(null);
									}
									ExcavatorHandler.depleteMinerals(worldObj, wheelPos.getX()>>4, wheelPos.getZ()>>4);
								}
							}
							if(wheel.digStacks[target]!=null)
							{
								this.doProcessOutput(wheel.digStacks[target].copy());
								Block b = Block.getBlockFromItem(wheel.digStacks[target].getItem());
								if(b!=null&&b!=Blocks.AIR)
									wheel.particleStack = wheel.digStacks[target].copy();
								wheel.digStacks[target] = null;
								wheel.markDirty();
								this.markContainingBlockForUpdate(null);
							}
						}
					}
				}
				else if(active)
				{
					active=false;
//					update = true;
				}
//				if(update)
//				{
//					this.markDirty();
//					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
//				}
			}
		}
	}

	ItemStack digBlocksInTheWay(TileEntityBucketWheel wheel)
	{
		BlockPos pos = wheel.getPos().add(0,-4,0);
		ItemStack s = digBlock(pos);
		if(s!=null)
			return s;
		//Backward 1
		s = digBlock(pos.offset(facing,-1));
		if(s!=null)
			return s;
		//Backward 2
		s = digBlock(pos.offset(facing,-2));
		if(s!=null)
			return s;
		//Forward 1
		s = digBlock(pos.offset(facing,1));
		if(s!=null)
			return s;
		//Forward 2
		s = digBlock(pos.offset(facing,2));
		if(s!=null)
			return s;

		//Backward+Sides
		s = digBlock(pos.offset(facing,-1).offset(facing.rotateY()));
		if(s!=null)
			return s;
		s = digBlock(pos.offset(facing,-1).offset(facing.rotateYCCW()));
		if(s!=null)
			return s;
		//Center Sides
		s = digBlock(pos.offset(facing.rotateY()));
		if(s!=null)
			return s;
		s = digBlock(pos.offset(facing.rotateYCCW()));
		if(s!=null)
			return s;
		//Forward+Sides
		s = digBlock(pos.offset(facing,1).offset(facing.rotateY()));
		if(s!=null)
			return s;
		s = digBlock(pos.offset(facing,1).offset(facing.rotateYCCW()));
		if(s!=null)
			return s;
		return null;
	}


	ItemStack digBlock(BlockPos pos)
	{
		if(!(worldObj instanceof WorldServer))
			return null;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer((WorldServer) worldObj);
		IBlockState blockstate = worldObj.getBlockState(pos);
		Block block = blockstate.getBlock();
		if(block!=null && !worldObj.isAirBlock(pos) && blockstate.getPlayerRelativeBlockHardness(fakePlayer, worldObj, pos)!=0)
		{
			if(!block.canHarvestBlock(worldObj, pos, fakePlayer))
				return null;
			block.onBlockHarvested(worldObj, pos, blockstate, fakePlayer);
			if(block.removedByPlayer(blockstate, worldObj, pos, fakePlayer, true))
			{
				block.onBlockDestroyedByPlayer( worldObj, pos, blockstate);
				if(block.canSilkHarvest(worldObj, pos, blockstate, fakePlayer))
				{
					ArrayList<ItemStack> items = new ArrayList<ItemStack>();
					Item bitem = Item.getItemFromBlock(block);
					if(bitem==null)
						return null;
					ItemStack itemstack = new ItemStack(bitem, 1, block.getMetaFromState(blockstate));
					if (itemstack != null)
						items.add(itemstack);

					ForgeEventFactory.fireBlockHarvesting(items, worldObj, pos, blockstate, 0, 1.0f, true, fakePlayer);

					for(int i=0; i<items.size(); i++)
						if(i!=0)
						{
							EntityItem ei = new EntityItem(worldObj, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, items.get(i).copy());
							this.worldObj.spawnEntityInWorld(ei);
						}
					worldObj.playEvent(2001, pos, Block.getStateId(blockstate));
					if(items.size()>0)
						return items.get(0);
				}
				else
				{
					block.harvestBlock(worldObj, fakePlayer, pos, blockstate, worldObj.getTileEntity(pos), null);
					worldObj.playEvent(2001, pos, Block.getStateId(blockstate));
				}
			}
		}
		return null;
	}

	@Override
	public float[] getBlockBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(pos==45)
			return new float[]{fl==EnumFacing.EAST?.5f:0,0,fl==EnumFacing.SOUTH?.5f:0, fl==EnumFacing.WEST?.5f:1,.5f,fl==EnumFacing.NORTH?.5f:1};
		if(pos==48)
			return new float[]{0,0,0, 1,.5f,1};
		if(pos==51)
			return new float[]{fl==EnumFacing.WEST?.5f:0,0,fl==EnumFacing.NORTH?.5f:0, fl==EnumFacing.EAST?.5f:1,.5f,fl==EnumFacing.SOUTH?.5f:1};

		if(pos==47)
			return new float[]{fl==EnumFacing.EAST?.5f:fl==EnumFacing.WEST?.375f:0,0,fl==EnumFacing.SOUTH?.5f:fl==EnumFacing.NORTH?.375f:0, fl==EnumFacing.WEST?.5f:fl==EnumFacing.EAST?.625f:1,1,fl==EnumFacing.NORTH?.5f:fl==EnumFacing.SOUTH?.625f:1};
		if(pos==50)
			return new float[]{fw==EnumFacing.EAST?.875f:0,0,fw==EnumFacing.SOUTH?.875f:0, fw==EnumFacing.WEST?.125f:1,1,fw==EnumFacing.NORTH?.125f:1};
		if(pos==53)
			return new float[]{fl==EnumFacing.WEST?.5f:fl==EnumFacing.EAST?.375f:0,0,fl==EnumFacing.NORTH?.5f:fl==EnumFacing.SOUTH?.375f:0, fl==EnumFacing.EAST?.5f:fl==EnumFacing.WEST?.625f:1,1,fl==EnumFacing.SOUTH?.5f:fl==EnumFacing.NORTH?.625f:1};

		if(pos==5||pos==23||pos==41)
			return new float[]{fw==EnumFacing.WEST?.5f:0,0,fw==EnumFacing.NORTH?.5f:0, fw==EnumFacing.EAST?.5f:1,1,fw==EnumFacing.SOUTH?.5f:1};
		if(pos==9||pos==12||pos==15)
			return new float[]{fw==EnumFacing.EAST?.5f:0,0,fw==EnumFacing.SOUTH?.5f:0, fw==EnumFacing.WEST?.5f:1,1,fw==EnumFacing.NORTH?.5f:1};
		if(pos==11||pos==14||pos==17)
			return new float[]{fw==EnumFacing.WEST?.5f:0,0,fw==EnumFacing.NORTH?.5f:0, fw==EnumFacing.EAST?.5f:1,1,fw==EnumFacing.SOUTH?.5f:1};




		return new float[]{0,0,0, 1,1,1};
	}
	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(pos==5||pos==23||pos==41)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fw==EnumFacing.WEST?.5f:0,0,fw==EnumFacing.NORTH?.5f:0, fw==EnumFacing.EAST?.5f:1,1,fw==EnumFacing.SOUTH?.5f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.5f:fw==EnumFacing.WEST?0:.25f,.25f,fw==EnumFacing.SOUTH?.5f:fw==EnumFacing.NORTH?0:.25f, fw==EnumFacing.WEST?.5f:fw==EnumFacing.EAST?1:.75f,.75f,fw==EnumFacing.NORTH?.5f:fw==EnumFacing.SOUTH?1:.75f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if(pos==9||pos==12||pos==15)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fw==EnumFacing.EAST?.5f:0,0,fw==EnumFacing.SOUTH?.5f:0, fw==EnumFacing.WEST?.5f:1,1,fw==EnumFacing.NORTH?.5f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(pos==9)
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST||fl==EnumFacing.EAST?.5f:0,.5f,fw==EnumFacing.NORTH||fl==EnumFacing.SOUTH?.5f:0, fw==EnumFacing.EAST||fl==EnumFacing.WEST?.5f:1,1,fw==EnumFacing.SOUTH||fl==EnumFacing.NORTH?.5f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			else if(pos==12)
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST?.5f:0,.5f,fw==EnumFacing.NORTH?.5f:0, fw==EnumFacing.EAST?.5f:1,1,fw==EnumFacing.SOUTH?.5f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			else
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST||fl==EnumFacing.WEST?.5f:0,.5f,fw==EnumFacing.NORTH||fl==EnumFacing.NORTH?.5f:0, fw==EnumFacing.EAST||fl==EnumFacing.EAST?.5f:1,1,fw==EnumFacing.SOUTH||fl==EnumFacing.SOUTH?.5f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if(pos==47)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fl==EnumFacing.EAST?.5f:fl==EnumFacing.WEST?.375f:0,0,fl==EnumFacing.SOUTH?.5f:fl==EnumFacing.NORTH?.375f:0, fl==EnumFacing.WEST?.5f:fl==EnumFacing.EAST?.625f:1,1,fl==EnumFacing.NORTH?.5f:fl==EnumFacing.SOUTH?.625f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			list.add(new AxisAlignedBB(fl==EnumFacing.EAST?.625f:fw==EnumFacing.EAST?.875f:0,0,fl==EnumFacing.SOUTH?.625f:fw==EnumFacing.SOUTH?.875f:0, fl==EnumFacing.WEST?.375f:fw==EnumFacing.WEST?.125f:1,1,fl==EnumFacing.NORTH?.375f:fw==EnumFacing.NORTH?.125f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if(pos==53)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fl==EnumFacing.WEST?.5f:fl==EnumFacing.EAST?.375f:0,0,fl==EnumFacing.NORTH?.5f:fl==EnumFacing.SOUTH?.375f:0, fl==EnumFacing.EAST?.5f:fl==EnumFacing.WEST?.625f:1,1,fl==EnumFacing.SOUTH?.5f:fl==EnumFacing.NORTH?.625f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			list.add(new AxisAlignedBB(fl==EnumFacing.WEST?.625f:fw==EnumFacing.EAST?.875f:0,0,fl==EnumFacing.NORTH?.625f:fw==EnumFacing.SOUTH?.875f:0, fl==EnumFacing.EAST?.375f:fw==EnumFacing.WEST?.125f:1,1,fl==EnumFacing.SOUTH?.375f:fw==EnumFacing.NORTH?.125f:1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		return null;
	}
	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}
	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{5,23,41};
	}
	@Override
	public int[] getRedstonePos()
	{
		return new int[]{18};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}
	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(facing,-1);
		TileEntity inventoryTile = this.worldObj.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output!=null)
			Utils.dropStackAtPos(worldObj, pos, output, facing);
	}
	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}
	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}
	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}
	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}
	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}


	@Override
	public ItemStack[] getInventory()
	{
		return null;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}
	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}
	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}
	@Override
	public FluidTank[] getInternalTanks()
	{
		return null;
	}
	@Override
	protected FluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}
	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}
	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}
	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}
}