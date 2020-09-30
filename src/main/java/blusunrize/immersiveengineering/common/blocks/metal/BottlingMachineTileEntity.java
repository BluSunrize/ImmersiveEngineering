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
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BottlingMachineTileEntity extends PoweredMultiblockTileEntity<BottlingMachineTileEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds
{
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(8*FluidAttributes.BUCKET_VOLUME)};
	public List<BottlingProcess> bottlingProcessQueue = new ArrayList<>();

	public BottlingMachineTileEntity()
	{
		super(IEMultiblocks.BOTTLING_MACHINE, 16000, true, IETileTypes.BOTTLING_MACHINE.get());
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
		nbt.put("bottlingQueue", processNBT);
		nbt.put("tank", tanks[0].writeToNBT(new CompoundNBT()));
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(2, 1, 1)).offset(outDir), outDir.getOpposite());
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
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(new BlockPos(1, 0, 0).equals(posInMultiblock))
			return VoxelShapes.create(0, 0, 0, 1, .5f, 1);
		if(posInMultiblock.getY()==0||new BlockPos(2, 1, 0).equals(posInMultiblock))
			return VoxelShapes.create(0, 0, 0, 1, 1, 1);
		if(posInMultiblock.getZ()==1&&posInMultiblock.getY()==1)
			return VoxelShapes.create(0, 0, 0, 1, .125f, 1);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
			return VoxelShapes.create(.0625f, 0, .0625f, .9375f, 1, .9375f);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
		{
			Direction f = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
			float xMin = f==Direction.EAST?-.0625f: f==Direction.WEST?.25f: getFacing()==Direction.WEST?.125f: getFacing()==Direction.EAST?.25f: 0;
			float zMin = getFacing()==Direction.NORTH?.125f: getFacing()==Direction.SOUTH?.25f: f==Direction.SOUTH?-.0625f: f==Direction.NORTH?.25f: 0;
			float xMax = f==Direction.EAST?.75f: f==Direction.WEST?1.0625f: getFacing()==Direction.WEST?.75f: getFacing()==Direction.EAST?.875f: 1;
			float zMax = getFacing()==Direction.NORTH?.75f: getFacing()==Direction.SOUTH?.875f: f==Direction.SOUTH?.75f: f==Direction.NORTH?1.0625f: 1;
			return VoxelShapes.create(xMin, .0625f, zMin, xMax, .6875f, zMax);
		}
		if(new BlockPos(1, 2, 1).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?0: .21875f;
			float zMin = getFacing()==Direction.NORTH?0: .21875f;
			float xMax = getFacing()==Direction.EAST?1: .78125f;
			float zMax = getFacing()==Direction.SOUTH?1: .78125f;
			return VoxelShapes.create(xMin, -.4375f, zMin, xMax, .5625f, zMax);
		}
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?.8125f: getFacing()==Direction.EAST?0: .125f;
			float zMin = getFacing()==Direction.NORTH?.8125f: getFacing()==Direction.SOUTH?0: .125f;
			float xMax = getFacing()==Direction.WEST?1: getFacing()==Direction.EAST?.1875f: .875f;
			float zMax = getFacing()==Direction.NORTH?1: getFacing()==Direction.SOUTH?.1875f: .875f;
			return VoxelShapes.create(xMin, -1, zMin, xMax, .25f, zMax);
		}
		return VoxelShapes.create(0, 0, 0, 1, 1, 1);
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 0, 1)
		);
	}


	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==2&&l==1&&w==1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof FluidPumpTileEntity)
				((FluidPumpTileEntity)tile).setDummy(true);
		}
		else if(h==1&&l==0)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof ConveyorBeltTileEntity)
				((ConveyorBeltTileEntity)tile).setFacing(this.getIsMirrored()?this.getFacing().rotateYCCW(): this.getFacing().rotateY());
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&!world.isRemote&&entity!=null&&entity.isAlive()&&entity instanceof ItemEntity)
		{
			BottlingMachineTileEntity master = master();
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
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
			BlockPos pos = getPos().offset(outDir, 2);
			Utils.dropStackAtPos(world, pos, output, outDir);
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
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
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MultiblockRecipe getRecipeForId(ResourceLocation id)
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
			BottlingMachineTileEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().rotateY(): this.getFacing().rotateYCCW()))
				return master.insertionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		BottlingMachineTileEntity master = this.master();
		if(master!=null)
		{
			if(BlockPos.ZERO.equals(posInMultiblock)&&(side==null||side.getAxis()!=Axis.Y))
				return master.tanks;
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		if(BlockPos.ZERO.equals(posInMultiblock)&&(side==null||side.getAxis()!=Axis.Y))
		{
			BottlingMachineTileEntity master = this.master();
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
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()};
		return new Direction[0];
	}

	public static class BottlingProcess
	{
		public NonNullList<ItemStack> items;
		public int processTick;
		public int maxProcessTick = (int)(120*IEServerConfig.MACHINES.bottlingMachineConfig.timeModifier.get());
		boolean processFinished = false;

		public BottlingProcess(ItemStack input)
		{
			this.items = NonNullList.withSize(2, ItemStack.EMPTY);
			this.items.set(0, input);
		}

		public boolean processStep(BottlingMachineTileEntity tile)
		{
			int energyExtracted = (int)(8*IEServerConfig.MACHINES.bottlingMachineConfig.energyModifier.get());
			if(tile.energyStorage.extractEnergy(energyExtracted, true) >= energyExtracted)
			{
				tile.energyStorage.extractEnergy(energyExtracted, false);
				if(++processTick==(int)(maxProcessTick*.4375))
				{
					FluidStack fs = tile.tanks[0].getFluid();
					if(!fs.isEmpty())
					{
						BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(items.get(0), fs);
						if(recipe!=null)
						{
							if(recipe.fluidInput.test(tile.tanks[0].getFluid()))
							{
								items.set(1, recipe.getActualItemOutputs(tile).get(0));
								tile.tanks[0].drain(recipe.fluidInput.getAmount(), FluidAction.EXECUTE);
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
				nbt.put("input", items.get(0).write(new CompoundNBT()));
			if(!items.get(1).isEmpty())
				nbt.put("output", items.get(1).write(new CompoundNBT()));
			nbt.putInt("processTick", processTick);
			return nbt;
		}

		public static BottlingProcess readFromNBT(CompoundNBT nbt)
		{
			ItemStack input = ItemStack.read(nbt.getCompound("input"));
			BottlingProcess process = new BottlingProcess(input);
			if(nbt.contains("output", NBT.TAG_COMPOUND))
				process.items.set(1, ItemStack.read(nbt.getCompound("output")));
			process.processTick = nbt.getInt("processTick");
			return process;
		}
	}

	public static class BottlingMachineInventoryHandler implements IItemHandlerModifiable
	{
		BottlingMachineTileEntity multiblock;

		public BottlingMachineInventoryHandler(BottlingMachineTileEntity multiblock)
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