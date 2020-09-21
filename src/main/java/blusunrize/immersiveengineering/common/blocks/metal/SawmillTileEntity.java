/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// Todo, replace all the commented out bottling machine code with proper code
public class SawmillTileEntity extends PoweredMultiblockTileEntity<SawmillTileEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds, IPlayerInteraction
{
	public float animation_bladeRotation = 0;
	public ItemStack sawblade = ItemStack.EMPTY;
	public List<SawmillProcess> sawmillProcessQueue = new ArrayList<>();

	public SawmillTileEntity()
	{
		super(IEMultiblocks.SAWMILL, 32000, true, IETileTypes.SAWMILL.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		sawblade = ItemStack.read(nbt.getCompound("sawblade"));

		ListNBT processNBT = nbt.getList("sawmillQueue", 10);
		sawmillProcessQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundNBT tag = processNBT.getCompound(i);
			SawmillProcess process = SawmillProcess.readFromNBT(tag);
			sawmillProcessQueue.add(process);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.sawblade.isEmpty())
			nbt.put("sawblade", this.sawblade.write(new CompoundNBT()));
		ListNBT processNBT = new ListNBT();
		for(SawmillProcess process : this.sawmillProcessQueue)
			processNBT.add(process.writeToNBT());
		nbt.put("sawmillQueue", processNBT);
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(4, 1, 1)).offset(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	private CapabilityReference<IItemHandler> secondaryOutputCap = CapabilityReference.forTileEntity(this, () -> {
		Direction shiftDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(3, 1, 2)).offset(shiftDir), getFacing().getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		super.tick();

		if(isDummy()||isRSDisabled()||world.isRemote)
			return;

		if(shouldRenderAsActive())
		{
			animation_bladeRotation += 36f;
			animation_bladeRotation %= 360f;
		}

		tickedProcesses = 0;
		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<SawmillProcess> processIterator = sawmillProcessQueue.iterator();
		tickedProcesses = 0;
		Set<ItemStack> secondaries = new HashSet<>();
		while(processIterator.hasNext()&&i++ < max)
		{
			SawmillProcess process = processIterator.next();
			if(process.processStep(this, secondaries))
				tickedProcesses++;
			if(process.processFinished)
			{
				doProcessOutput(process.getCurrentStack(!this.sawblade.isEmpty()).copy());
				processIterator.remove();
			}
		}
		for(ItemStack output : secondaries)
			doSecondaryOutput(output.copy());
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		SawmillTileEntity master = master();
		if(master!=null)
			if(player.isSneaking()&&!master.sawblade.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setHeldItem(hand, master.sawblade.copy());
				else if(!world.isRemote)
					player.entityDropItem(master.sawblade.copy(), 0);
				master.sawblade = ItemStack.EMPTY;
				this.updateMasterBlock(null, true);
				return true;
			}
			//todo handle with a tag
			else if(heldItem.getItem() ==Tools.sawblade)
			{
				ItemStack tempMold = !master.sawblade.isEmpty()?master.sawblade.copy(): ItemStack.EMPTY;
				master.sawblade = Utils.copyStackWithAmount(heldItem, 1);
				heldItem.shrink(1);
				if(heldItem.getCount() <= 0)
					heldItem = ItemStack.EMPTY;
				else
					player.setHeldItem(hand, heldItem);
				if(!tempMold.isEmpty())
					if(heldItem.isEmpty())
						player.setHeldItem(hand, tempMold);
					else if(!world.isRemote)
						player.entityDropItem(tempMold, 0);
				this.updateMasterBlock(null, true);
				return true;
			}
		return false;
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES
			= CachedShapesWithTransform.createForMultiblock(SawmillTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock)
	{
		// Slabs
		Set<BlockPos> slabs = ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(4, 0, 0),
				new BlockPos(4, 0, 2)
		);
		if(slabs.contains(posInMultiblock))
			return VoxelShapes.create(0, 0, 0, 1, .5f, 1).toBoundingBoxList();
		// Redstone panel feet
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1));
			list.add(new AxisAlignedBB(.125, .5f, .625, .25, 1, .875));
			list.add(new AxisAlignedBB(.75, .5f, .625, .875, 1, .875));
			return list;
		}
		// Restone panel
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return VoxelShapes.create(0, 0, .5f, 1, 1, 1).toBoundingBoxList();
		// Stripper
		if(new BlockPos(1, 1, 1).equals(posInMultiblock))
			return VoxelShapes.create(.25, 0, 0, .875, 1, 1).toBoundingBoxList();
		// Vacuum
		if(new BlockPos(1, 1, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.25, 0, 0, .875, 1, .125));
			list.add(new AxisAlignedBB(.25, 0, .125, .875, .875, .75));
			list.add(new AxisAlignedBB(.1875, 0, 0, .9375, .125, .8125));
			return list;
		}
		if(new BlockPos(1, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5, 1));
			list.add(new AxisAlignedBB(.1875, .5, 0, .9375, 1, .8125));
			list.add(new AxisAlignedBB(.9375, .5, .25, 1, .875, .625));
			return list;
		}
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5, 1));
			list.add(new AxisAlignedBB(0, .5, .25, 1, .875, .625));
			return list;
		}
		// Conveyors
		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==1)
			return VoxelShapes.create(0, 0, 0, 1, .125, 1).toBoundingBoxList();
		// Rest
		return VoxelShapes.create(0, 0, 0, 1, 1, 1).toBoundingBoxList();
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
				new BlockPos(0, 1, 2)
		);
	}


	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&l==1)
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
			SawmillTileEntity master = master();
			if(master==null)
				return;
			ItemStack stack = ((ItemEntity)entity).getItem();
			if(stack.isEmpty())
				return;

			if(master.sawmillProcessQueue.size() < master.getProcessQueueMaxLength())
			{
				float dist = 1;
				SawmillProcess p = null;
				if(master.sawmillProcessQueue.size() > 0)
				{
					p = master.sawmillProcessQueue.get(master.sawmillProcessQueue.size()-1);
					if(p!=null)
						dist = p.getRelativeProcessStep();
				}
				if(p!=null&&dist < master.getMinProcessDistance(null))
					return;

				p = new SawmillProcess(Utils.copyStackWithAmount(stack, 1));
				master.sawmillProcessQueue.add(p);
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
			BlockPos pos = getPos().offset(outDir, 3);
			Utils.dropStackAtPos(world, pos, output, outDir);
		}
	}

	public void doSecondaryOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(secondaryOutputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
			BlockPos pos = getPos().offset(outDir, 1).offset(getFacing(), -2).down();
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
		return new IFluidTank[0];
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

//	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new BottlingMachineInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
//			SawmillTileEntity master = master();
//			if(master==null)
//				return LazyOptional.empty();
//			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().rotateY(): this.getFacing().rotateYCCW()))
//				return master.insertionHandler.cast();
//			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
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
		if(new BlockPos(4, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()};
		return new Direction[0];
	}

	public static class SawmillProcess
	{
		private final ItemStack input;
		private final SawmillRecipe recipe;
		private final float maxProcessTicks;
		private int processTick;
		private boolean stripped = false;
		private boolean sawed = false;
		private boolean processFinished = false;

		public SawmillProcess(ItemStack input)
		{
			this.input = input;
			this.recipe = SawmillRecipe.findRecipe(input);
			this.maxProcessTicks = this.recipe!=null?this.recipe.getTotalProcessTime(): 80;
		}

		public boolean processStep(SawmillTileEntity tile, Set<ItemStack> secondaries)
		{
			int energyExtracted = (int)(8*IEConfig.MACHINES.bottlingMachineConfig.energyModifier.get());
			if(tile.energyStorage.extractEnergy(energyExtracted, true) >= energyExtracted)
			{
				tile.energyStorage.extractEnergy(energyExtracted, false);
				this.processTick++;
				float relative = getRelativeProcessStep();
				if(this.recipe!=null)
				{
					if(!this.stripped&&relative >= .3125)
					{
						this.stripped = true;
						secondaries.addAll(this.recipe.secondaryStripping);
					}
					if(!this.sawed&&relative >= .8625)
					{
						this.sawed = true;
						secondaries.addAll(this.recipe.secondaryOutputs);
					}
				}
				if(relative>=1)
					this.processFinished = true;
				return true;
			}
			return false;
		}

		public float getRelativeProcessStep(){
			return this.processTick / this.maxProcessTicks;
		}

		public ItemStack getCurrentStack(boolean sawblade)
		{
			if(this.recipe==null)
				return this.input;
			// Early exit before stripping
			if(!this.stripped)
				return this.input;
			// After stripping
			ItemStack stripped = this.recipe.stripped;
			if(stripped.isEmpty())
				stripped = this.input;
			// Before sawing
			if(!this.sawed)
				return stripped;
			// Finally, if there is a sawblade
			return sawblade?this.recipe.output: stripped;
		}

		public CompoundNBT writeToNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("input", this.input.write(new CompoundNBT()));
			nbt.putInt("processTick", this.processTick);
			nbt.putBoolean("stripped", this.stripped);
			nbt.putBoolean("sawed", this.sawed);
			return nbt;
		}

		public static SawmillProcess readFromNBT(CompoundNBT nbt)
		{
			ItemStack input = ItemStack.read(nbt.getCompound("input"));
			SawmillProcess process = new SawmillProcess(input);
			process.processTick = nbt.getInt("processTick");
			process.stripped = nbt.getBoolean("stripped");
			process.sawed = nbt.getBoolean("sawed");
			return process;
		}
	}
}