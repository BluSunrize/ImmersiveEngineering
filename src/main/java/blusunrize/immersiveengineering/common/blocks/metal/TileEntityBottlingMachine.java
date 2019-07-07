/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBottlingMachine;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityBottlingMachine extends TileEntityPoweredMultiblock<TileEntityBottlingMachine, IMultiblockRecipe>
		implements IConveyorAttachable
{
	public static TileEntityType<TileEntityBottlingMachine> TYPE;
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(8000)};
	public List<BottlingProcess> bottlingProcessQueue = new ArrayList<>();

	public TileEntityBottlingMachine()
	{
		super(MultiblockBottlingMachine.instance, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		ListNBT processNBT = nbt.getList("bottlingQueue", 10);
		bottlingProcessQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundNBT tag = processNBT.getCompound(i);
			BottlingProcess process = BottlingProcess.readFromNBT(tag);
			bottlingProcessQueue.add(process);
		}
		tanks[0].readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		ListNBT processNBT = new ListNBT();
		for(BottlingProcess process : this.bottlingProcessQueue)
			processNBT.add(process.writeToNBT());
		nbt.setTag("bottlingQueue", processNBT);
		nbt.setTag("tank", tanks[0].writeToNBT(new CompoundNBT()));
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, () -> {
		Direction outDir = mirrored?facing.rotateYCCW(): facing.rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(8).offset(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		super.tick();

		if(isDummy()||isRSDisabled()||world.isRemote)
			return;

		tickedProcesses = 0;

		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<BottlingProcess> processIterator = bottlingProcessQueue.iterator();
		tickedProcesses = 0;
		while(processIterator.hasNext()&&i++ < max)
		{
			BottlingProcess process = processIterator.next();
			if(process.processStep(this))
				tickedProcesses++;
			if(process.processFinished)
			{
				ItemStack output = !process.items.get(1).isEmpty()?process.items.get(1): process.items.get(0);
				doProcessOutput(output);
				processIterator.remove();
			}
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==4)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(posInMultiblock < 6||posInMultiblock==11)
			return new float[]{0, 0, 0, 1, 1, 1};
		if(posInMultiblock >= 6&&posInMultiblock <= 8)
			return new float[]{0, 0, 0, 1, .125f, 1};
		if(posInMultiblock==9)
			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};
		if(posInMultiblock==10)
		{
			Direction f = mirrored?facing.rotateYCCW(): facing.rotateY();
			float xMin = f==Direction.EAST?-.0625f: f==Direction.WEST?.25f: facing==Direction.WEST?.125f: facing==Direction.EAST?.25f: 0;
			float zMin = facing==Direction.NORTH?.125f: facing==Direction.SOUTH?.25f: f==Direction.SOUTH?-.0625f: f==Direction.NORTH?.25f: 0;
			float xMax = f==Direction.EAST?.75f: f==Direction.WEST?1.0625f: facing==Direction.WEST?.75f: facing==Direction.EAST?.875f: 1;
			float zMax = facing==Direction.NORTH?.75f: facing==Direction.SOUTH?.875f: f==Direction.SOUTH?.75f: f==Direction.NORTH?1.0625f: 1;
			return new float[]{xMin, .0625f, zMin, xMax, .6875f, zMax};
		}
		if(posInMultiblock==13)
		{
			float xMin = facing==Direction.WEST?0: .21875f;
			float zMin = facing==Direction.NORTH?0: .21875f;
			float xMax = facing==Direction.EAST?1: .78125f;
			float zMax = facing==Direction.SOUTH?1: .78125f;
			return new float[]{xMin, -.4375f, zMin, xMax, .5625f, zMax};
		}
		if(posInMultiblock==16)
		{
			float xMin = facing==Direction.WEST?.8125f: facing==Direction.EAST?0: .125f;
			float zMin = facing==Direction.NORTH?.8125f: facing==Direction.SOUTH?0: .125f;
			float xMax = facing==Direction.WEST?1: facing==Direction.EAST?.1875f: .875f;
			float zMax = facing==Direction.NORTH?1: facing==Direction.SOUTH?.1875f: .875f;
			return new float[]{xMin, -1, zMin, xMax, .25f, zMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{11};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{1};
	}


	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==2&&l==1&&w==1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityFluidPump)
				((TileEntityFluidPump)tile).dummy = true;
		}
		else if(h==1&&l==0)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityConveyorBelt)
				((TileEntityConveyorBelt)tile).setFacing(this.mirrored?this.facing.rotateYCCW(): this.facing.rotateY());
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(posInMultiblock==6&&!world.isRemote&&entity!=null&&entity.isAlive()&&entity instanceof ItemEntity)
		{
			TileEntityBottlingMachine master = master();
			if(master==null)
				return;
			ItemStack stack = ((ItemEntity)entity).getItem();
			if(stack.isEmpty())
				return;

			if(master.bottlingProcessQueue.size() < master.getProcessQueueMaxLength())
			{
				float dist = 1;
				BottlingProcess p = null;
				if(master.bottlingProcessQueue.size() > 0)
				{
					p = master.bottlingProcessQueue.get(master.bottlingProcessQueue.size()-1);
					if(p!=null)
						dist = p.processTick/(float)p.maxProcessTick;
				}
				if(p!=null&&dist < master.getMinProcessDistance(null))
					return;

				p = new BottlingProcess(Utils.copyStackWithAmount(stack, 1));
				master.bottlingProcessQueue.add(p);
				master.markDirty();
				master.markContainingBlockForUpdate(null);
				stack.shrink(1);
				if(stack.getCount() <= 0)
					entity.remove();
			}
		}
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = mirrored?facing.rotateYCCW(): facing.rotateY();
			BlockPos pos = getPos().offset(outDir, 2);
			Utils.dropStackAtPos(world, pos, output, outDir);
		}
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
		return 2;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 2;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return .5f;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
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
	protected IMultiblockRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return null;
	}

	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new BottlingMachineInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityBottlingMachine master = master();
			if(master==null)
				return null;
			if(posInMultiblock==6&&facing==(mirrored?this.facing.rotateY(): this.facing.rotateYCCW()))
				return master.insertionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		TileEntityBottlingMachine master = this.master();
		if(master!=null)
		{
			if(posInMultiblock==3&&(side==null||side.getAxis()!=Axis.Y))
				return master.tanks;
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		if(posInMultiblock==3&&(side==null||side.getAxis()!=Axis.Y))
		{
			TileEntityBottlingMachine master = this.master();
			return !(master==null||master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity());
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(posInMultiblock==8)
			return new Direction[]{mirrored?facing.rotateYCCW(): facing.rotateY()};
		return new Direction[0];
	}

	public static class BottlingProcess
	{
		public NonNullList<ItemStack> items;
		public int processTick;
		public int maxProcessTick = (int)(120*Config.IEConfig.Machines.bottlingMachine_timeModifier);
		boolean processFinished = false;

		public BottlingProcess(ItemStack input)
		{
			this.items = NonNullList.withSize(2, ItemStack.EMPTY);
			this.items.set(0, input);
		}

		public boolean processStep(TileEntityBottlingMachine tile)
		{
			int energyExtracted = (int)(8*Config.IEConfig.Machines.bottlingMachine_energyModifier);
			if(tile.energyStorage.extractEnergy(energyExtracted, true) >= energyExtracted)
			{
				tile.energyStorage.extractEnergy(energyExtracted, false);
				if(++processTick==(int)(maxProcessTick*.4375))
				{
					FluidStack fs = tile.tanks[0].getFluid();
					if(fs!=null)
					{
						BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(items.get(0), fs);
						if(recipe!=null)
						{
							if(tile.tanks[0].drainInternal(recipe.fluidInput, false).amount==recipe.fluidInput.amount)
							{
								items.set(1, recipe.getActualItemOutputs(tile).get(0));
								tile.tanks[0].drainInternal(recipe.fluidInput, true);
							}
						}
						else
						{
							ItemStack ret = Utils.fillFluidContainer(tile.tanks[0], items.get(0), ItemStack.EMPTY, null);
							if(!ret.isEmpty())
								items.set(1, ret);
						}
						if(items.get(1).isEmpty())
							items.set(1, items.get(0));
					}
				}
				if(processTick >= maxProcessTick)
					processFinished = true;
				return true;
			}
			return false;
		}

		public CompoundNBT writeToNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			if(!items.get(0).isEmpty())
				nbt.setTag("input", items.get(0).write(new CompoundNBT()));
			if(!items.get(1).isEmpty())
				nbt.setTag("output", items.get(1).write(new CompoundNBT()));
			nbt.setInt("processTick", processTick);
			return nbt;
		}

		public static BottlingProcess readFromNBT(CompoundNBT nbt)
		{
			ItemStack input = ItemStack.read(nbt.getCompound("input"));
			BottlingProcess process = new BottlingProcess(input);
			if(nbt.hasKey("output"))
				process.items.set(1, ItemStack.read(nbt.getCompound("output")));
			process.processTick = nbt.getInt("processTick");
			return process;
		}
	}

	public static class BottlingMachineInventoryHandler implements IItemHandlerModifiable
	{
		TileEntityBottlingMachine multiblock;

		public BottlingMachineInventoryHandler(TileEntityBottlingMachine multiblock)
		{
			this.multiblock = multiblock;
		}

		@Override
		public int getSlots()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(multiblock.bottlingProcessQueue.size() < multiblock.getProcessQueueMaxLength())
			{
				stack = stack.copy();
				float dist = 1;
				BottlingProcess p = null;
				if(multiblock.bottlingProcessQueue.size() > 0)
				{
					p = multiblock.bottlingProcessQueue.get(multiblock.bottlingProcessQueue.size()-1);
					if(p!=null)
						dist = p.processTick/(float)p.maxProcessTick;
				}
				if(p!=null&&dist < multiblock.getMinProcessDistance(null))
					return stack;
				if(!simulate)
				{
					p = new BottlingProcess(Utils.copyStackWithAmount(stack, 1));
					multiblock.bottlingProcessQueue.add(p);
					multiblock.markDirty();
					multiblock.markContainingBlockForUpdate(null);
				}
				stack.shrink(1);
				if(stack.getCount() <= 0)
					stack = ItemStack.EMPTY;
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot)
		{
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}
}