/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class TileEntityExcavator extends TileEntityMultiblockMetal<TileEntityExcavator, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public TileEntityExcavator()
	{
		super(MultiblockExcavator.instance, new int[]{3, 6, 3}, 64000, true);
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
	public int getComparatorInputOverride()
	{
		if(!this.isRedstonePos())
			return 0;
		BlockPos wheelPos = this.getBlockPosForPos(31);
		if(world.isBlockLoaded(wheelPos)&&world.getTileEntity(wheelPos) instanceof TileEntityBucketWheel)
		{
			MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);
			if(info==null)
				return 0;
			float remain = (ExcavatorHandler.mineralVeinCapacity-info.depletion)/(float)ExcavatorHandler.mineralVeinCapacity;
			return MathHelper.floor(Math.max(remain, 0)*15);
		}
		return 0;
	}

	@Override
	public void update()
	{
		super.update();
		if(isDummy())
			return;
		BlockPos wheelPos = this.getBlockPosForPos(31);
		if(!world.isRemote&&world.isBlockLoaded(wheelPos))
		{
			TileEntity center = world.getTileEntity(wheelPos);

			if(center instanceof TileEntityBucketWheel)
			{
				float rot = 0;
				int target = -1;
				TileEntityBucketWheel wheel = ((TileEntityBucketWheel)center);
				EnumFacing fRot = this.facing.rotateYCCW();
				if(wheel.facing==fRot)
				{
					if(active!=wheel.active)
						world.addBlockEvent(wheel.getPos(), wheel.getBlockType(), 0, active?1: 0);
					rot = wheel.rotation;
					if(rot%45 > 40)
						target = Math.round(rot/360f*8)%8;
				}

				if(wheel.facing!=fRot||wheel.mirrored!=this.mirrored)
				{
					for(int h = -3; h <= 3; h++)
						for(int w = -3; w <= 3; w++)
						{
							TileEntity te = world.getTileEntity(wheelPos.add(0, h, 0).offset(facing, w));
							if(te instanceof TileEntityBucketWheel)
							{
								((TileEntityBucketWheel)te).facing = fRot;
								((TileEntityBucketWheel)te).mirrored = this.mirrored;
								te.markDirty();
								((TileEntityBucketWheel)te).markContainingBlockForUpdate(null);
								world.addBlockEvent(te.getPos(), te.getBlockType(), 255, 0);
							}
						}
				}

				if(!isRSDisabled())
				{
					ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);

					int consumed = IEConfig.Machines.excavator_consumption;
					int extracted = energyStorage.extractEnergy(consumed, true);
					if(extracted >= consumed)
					{
						energyStorage.extractEnergy(consumed, false);
						active = true;

						if(target >= 0)
						{
							int targetDown = (target+4)%8;
							NBTTagCompound packet = new NBTTagCompound();
							if(wheel.digStacks.get(targetDown).isEmpty())
							{
								ItemStack blocking = this.digBlocksInTheWay(wheel);
								BlockPos lowGroundPos = wheelPos.add(0, -5, 0);
								if(!blocking.isEmpty())
								{
									wheel.digStacks.set(targetDown, blocking);
									wheel.markDirty();
									this.markContainingBlockForUpdate(null);
								}
								else if(mineral!=null
										/*&&!world.isAirBlock(lowGroundPos.offset(facing, -2))
										&&!world.isAirBlock(lowGroundPos.offset(facing, 2))
										&&!world.isAirBlock(lowGroundPos.offset(facing, -1))
										&&!world.isAirBlock(lowGroundPos.offset(facing, 1))
										&&!world.isAirBlock(lowGroundPos)*/
								)
								{
									ItemStack ore = mineral.getRandomOre(Utils.RAND);
									float configChance = Utils.RAND.nextFloat();
									float failChance = Utils.RAND.nextFloat();
									if(!ore.isEmpty()&&configChance > IEConfig.Machines.excavator_fail_chance&&failChance > mineral.failChance)
									{
										wheel.digStacks.set(targetDown, ore);
										wheel.markDirty();
										this.markContainingBlockForUpdate(null);
									}
									ExcavatorHandler.depleteMinerals(world, wheelPos.getX() >> 4, wheelPos.getZ() >> 4);
								}
								if(!wheel.digStacks.get(targetDown).isEmpty())
								{
									packet.setInteger("fill", targetDown);
									packet.setTag("fillStack", wheel.digStacks.get(targetDown).writeToNBT(new NBTTagCompound()));
								}
							}
							if(!wheel.digStacks.get(target).isEmpty())
							{
								this.doProcessOutput(wheel.digStacks.get(target).copy());
								Block b = Block.getBlockFromItem(wheel.digStacks.get(target).getItem());
								if(b!=null&&b!=Blocks.AIR)
									wheel.particleStack = wheel.digStacks.get(target).copy();
								wheel.digStacks.set(target, ItemStack.EMPTY);
								wheel.markDirty();
								this.markContainingBlockForUpdate(null);
								packet.setInteger("empty", target);
							}
							if(!packet.isEmpty())
								ImmersiveEngineering.packetHandler.sendToAll(new MessageTileSync(wheel, packet));
						}
					}
					else if(active)
						active = false;
				}
				else if(active)
				{
					active = false;
//					update = true;
				}
//				if(update)
//				{
//					this.markDirty();
//					world.markBlockForUpdate(xCoord, yCoord, zCoord);
//				}
			}
		}
	}

	ItemStack digBlocksInTheWay(TileEntityBucketWheel wheel)
	{
		BlockPos pos = wheel.getPos().add(0, -4, 0);
		ItemStack s = digBlock(pos);
		if(!s.isEmpty())
			return s;
		//Backward 1
		s = digBlock(pos.offset(facing, -1));
		if(!s.isEmpty())
			return s;
		//Backward 2
		s = digBlock(pos.offset(facing, -2));
		if(!s.isEmpty())
			return s;
		//Forward 1
		s = digBlock(pos.offset(facing, 1));
		if(!s.isEmpty())
			return s;
		//Forward 2
		s = digBlock(pos.offset(facing, 2));
		if(!s.isEmpty())
			return s;

		//Backward+Sides
		s = digBlock(pos.offset(facing, -1).offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing, -1).offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		//Center Sides
		s = digBlock(pos.offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		//Forward+Sides
		s = digBlock(pos.offset(facing, 1).offset(facing.rotateY()));
		if(!s.isEmpty())
			return s;
		s = digBlock(pos.offset(facing, 1).offset(facing.rotateYCCW()));
		if(!s.isEmpty())
			return s;
		return ItemStack.EMPTY;
	}


	ItemStack digBlock(BlockPos pos)
	{
		if(!(world instanceof WorldServer))
			return ItemStack.EMPTY;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer(world);
		IBlockState blockstate = world.getBlockState(pos);
		Block block = blockstate.getBlock();
		if(block!=null&&!world.isAirBlock(pos)&&blockstate.getPlayerRelativeBlockHardness(fakePlayer, world, pos)!=0)
		{
			if(!block.canHarvestBlock(world, pos, fakePlayer))
				return ItemStack.EMPTY;
			block.onBlockHarvested(world, pos, blockstate, fakePlayer);
			if(block.removedByPlayer(blockstate, world, pos, fakePlayer, true))
			{
				block.onPlayerDestroy(world, pos, blockstate);
				if(block.canSilkHarvest(world, pos, blockstate, fakePlayer))
				{
					ArrayList<ItemStack> items = new ArrayList<ItemStack>();
					Item bitem = Item.getItemFromBlock(block);
					if(bitem==Items.AIR)
						return ItemStack.EMPTY;
					ItemStack itemstack = new ItemStack(bitem, 1, block.getMetaFromState(blockstate));
					if(!itemstack.isEmpty())
						items.add(itemstack);

					ForgeEventFactory.fireBlockHarvesting(items, world, pos, blockstate, 0, 1.0f, true, fakePlayer);

					for(int i = 0; i < items.size(); i++)
						if(i!=0)
						{
							EntityItem ei = new EntityItem(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, items.get(i).copy());
							this.world.spawnEntity(ei);
						}
					world.playEvent(2001, pos, Block.getStateId(blockstate));
					if(items.size() > 0)
						return items.get(0);
				}
				else
				{
					block.harvestBlock(world, fakePlayer, pos, blockstate, world.getTileEntity(pos), ItemStack.EMPTY);
					world.playEvent(2001, pos, Block.getStateId(blockstate));
				}
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public float[] getBlockBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(pos==45)
			return new float[]{fl==EnumFacing.EAST?.5f: 0, 0, fl==EnumFacing.SOUTH?.5f: 0, fl==EnumFacing.WEST?.5f: 1, .5f, fl==EnumFacing.NORTH?.5f: 1};
		if(pos==48)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(pos==51)
			return new float[]{fl==EnumFacing.WEST?.5f: 0, 0, fl==EnumFacing.NORTH?.5f: 0, fl==EnumFacing.EAST?.5f: 1, .5f, fl==EnumFacing.SOUTH?.5f: 1};

		if(pos==47)
			return new float[]{fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.375f: 0, 0, fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.375f: 0, fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.625f: 1, 1, fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.625f: 1};
		if(pos==50)
			return new float[]{fw==EnumFacing.EAST?.875f: 0, 0, fw==EnumFacing.SOUTH?.875f: 0, fw==EnumFacing.WEST?.125f: 1, 1, fw==EnumFacing.NORTH?.125f: 1};
		if(pos==53)
			return new float[]{fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: 0, 0, fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: 0, fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.625f: 1, 1, fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.625f: 1};

		if(pos==5||pos==23||pos==41)
			return new float[]{fw==EnumFacing.WEST?.5f: 0, 0, fw==EnumFacing.NORTH?.5f: 0, fw==EnumFacing.EAST?.5f: 1, 1, fw==EnumFacing.SOUTH?.5f: 1};
		if(pos==9||pos==12||pos==15)
			return new float[]{fw==EnumFacing.EAST?.5f: 0, 0, fw==EnumFacing.SOUTH?.5f: 0, fw==EnumFacing.WEST?.5f: 1, 1, fw==EnumFacing.NORTH?.5f: 1};
		if(pos==11||pos==14||pos==17)
			return new float[]{fw==EnumFacing.WEST?.5f: 0, 0, fw==EnumFacing.NORTH?.5f: 0, fw==EnumFacing.EAST?.5f: 1, 1, fw==EnumFacing.SOUTH?.5f: 1};


		return new float[]{0, 0, 0, 1, 1, 1};
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
			List list = Lists.newArrayList(new AxisAlignedBB(fw==EnumFacing.WEST?.5f: 0, 0, fw==EnumFacing.NORTH?.5f: 0, fw==EnumFacing.EAST?.5f: 1, 1, fw==EnumFacing.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?0: .25f, .25f, fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?0: .25f, fw==EnumFacing.WEST?.5f: fw==EnumFacing.EAST?1: .75f, .75f, fw==EnumFacing.NORTH?.5f: fw==EnumFacing.SOUTH?1: .75f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(pos==9||pos==12||pos==15)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fw==EnumFacing.EAST?.5f: 0, 0, fw==EnumFacing.SOUTH?.5f: 0, fw==EnumFacing.WEST?.5f: 1, 1, fw==EnumFacing.NORTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos==9)
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST||fl==EnumFacing.EAST?.5f: 0, .5f, fw==EnumFacing.NORTH||fl==EnumFacing.SOUTH?.5f: 0, fw==EnumFacing.EAST||fl==EnumFacing.WEST?.5f: 1, 1, fw==EnumFacing.SOUTH||fl==EnumFacing.NORTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else if(pos==12)
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST?.5f: 0, .5f, fw==EnumFacing.NORTH?.5f: 0, fw==EnumFacing.EAST?.5f: 1, 1, fw==EnumFacing.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else
				list.add(new AxisAlignedBB(fw==EnumFacing.WEST||fl==EnumFacing.WEST?.5f: 0, .5f, fw==EnumFacing.NORTH||fl==EnumFacing.NORTH?.5f: 0, fw==EnumFacing.EAST||fl==EnumFacing.EAST?.5f: 1, 1, fw==EnumFacing.SOUTH||fl==EnumFacing.SOUTH?.5f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(pos==47)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.375f: 0, 0, fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.375f: 0, fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.625f: 1, 1, fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.625f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fl==EnumFacing.EAST?.625f: fw==EnumFacing.EAST?.875f: 0, 0, fl==EnumFacing.SOUTH?.625f: fw==EnumFacing.SOUTH?.875f: 0, fl==EnumFacing.WEST?.375f: fw==EnumFacing.WEST?.125f: 1, 1, fl==EnumFacing.NORTH?.375f: fw==EnumFacing.NORTH?.125f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(pos==53)
		{
			List list = Lists.newArrayList(new AxisAlignedBB(fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: 0, 0, fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: 0, fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.625f: 1, 1, fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.625f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fl==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?.875f: 0, 0, fl==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?.875f: 0, fl==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.125f: 1, 1, fl==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.125f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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
		return new int[]{5, 23, 41};
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
		BlockPos pos = getPos().offset(facing, -1);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, facing);
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
	public NonNullList<ItemStack> getInventory()
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
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
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

	@Override
	public void disassemble()
	{
		super.disassemble();
		BlockPos wheelPos = this.getBlockPosForPos(31);
		TileEntity center = world.getTileEntity(wheelPos);
		if(center instanceof TileEntityBucketWheel)
			world.addBlockEvent(center.getPos(), center.getBlockType(), 0, 0);
	}
}