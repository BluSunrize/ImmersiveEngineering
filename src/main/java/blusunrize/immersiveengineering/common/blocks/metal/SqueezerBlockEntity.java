/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class SqueezerBlockEntity extends PoweredMultiblockBlockEntity<SqueezerBlockEntity, SqueezerRecipe> implements
		ISelectionBounds, ICollisionBounds, IInteractionObjectIE<SqueezerBlockEntity>, IBlockBounds,
		IEClientTickableBE
{
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24*FluidType.BUCKET_VOLUME)};
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);
	public float animation_piston = 0;
	public boolean animation_down = true;
	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(
			this, this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	public SqueezerBlockEntity(BlockEntityType<SqueezerBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.SQUEEZER, 16000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			tanks[0].readFromNBT(nbt.getCompound("tank"));
			ContainerHelper.loadAllItems(nbt, inventory);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			nbt.put("tank", tanks[0].writeToNBT(new CompoundTag()));
			ContainerHelper.saveAllItems(nbt, inventory);
		}
	}

	@Override
	public boolean canTickAny()
	{
		return super.canTickAny()&&!isRSDisabled();
	}

	@Override
	public void tickClient()
	{
		if(this.processQueue.isEmpty()&&animation_piston < .6875)
			animation_piston = Math.min(.6875f, animation_piston+.03125f);
		else if(shouldRenderAsActive())
		{
			if(animation_down)
				animation_piston = Math.max(0, animation_piston-.03125f);
			else
				animation_piston = Math.min(.6875f, animation_piston+.03125f);
			if(animation_piston <= 0&&animation_down)
				animation_down = false;
			else if(animation_piston >= .6875&&!animation_down)
				animation_down = true;
		}
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		boolean update = false;
		if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
		{
			final int[] usedInvSlots = new int[8];
			for(MultiblockProcess<?> process : processQueue)
				if(process instanceof MultiblockProcessInMachine)
					for(int i : ((MultiblockProcessInMachine<?>)process).getInputSlots())
						usedInvSlots[i]++;

			Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
			Arrays.sort(preferredSlots, 0, 8, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));
			for(int slot : preferredSlots)
			{
				ItemStack stack = this.getInventory().get(slot);
				if(!stack.isEmpty())
				{
					stack = stack.copy();
					stack.shrink(usedInvSlots[slot]);
				}
				if(!stack.isEmpty()&&stack.getCount() > 0)
				{
					SqueezerRecipe recipe = this.findRecipeForInsertion(stack);
					if(recipe!=null)
					{
						MultiblockProcessInMachine<SqueezerRecipe> process = new MultiblockProcessInMachine<>(recipe, this::getRecipeForId, slot);
						if(this.addProcessToQueue(process, true))
						{
							this.addProcessToQueue(process, false);
							update = true;
						}
					}
				}
			}
		}

		Direction fw = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		update |= FluidUtils.multiblockFluidOutput(
				level, this.getBlockPos().offset(0, -1, 0).relative(fw, 2), fw, this.tanks[0],
				9, 10, inventory::get, inventory::set
		);
		if(!inventory.get(8).isEmpty()&&level.getGameTime()%8==0)
		{
			BlockPos outputPos = this.getBlockPos();
			BlockEntity outputTile = Utils.getExistingTileEntity(level, outputPos);
			if(outputTile!=null)
			{
				ItemStack stack = ItemHandlerHelper.copyStackWithSize(inventory.get(8), 1);
				stack = Utils.insertStackIntoInventory(outputCap, stack, false);
				if(stack.isEmpty())
				{
					this.inventory.get(8).shrink(1);
					if(this.inventory.get(8).getCount() <= 0)
						this.inventory.set(8, ItemStack.EMPTY);
				}
			}
		}

		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	private DirectionalBlockPos getOutputPos()
	{
		Direction fw = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		return new DirectionalBlockPos(worldPosition.relative(fw), fw.getOpposite());
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(SqueezerBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875),
					new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		if(new BoundingBox(0, 0, 0, 1, 0, 1)
				.isInside(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, .5f, 1));
			list.add(new AABB(0.0625, .5f, 0.6875, 0.3125, 1, 0.9375));

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				list.add(new AABB(0, .5f, 0.375, 1.125, .75f, 0.625));
				list.add(new AABB(0.875, .5f, -0.125, 1.125, .75f, 0.375));
				list.add(new AABB(0.875, .75f, -0.125, 1.125, 1, 0.125));
			}

			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1, list);
		}
		if(new BoundingBox(0, 1, 0, 1, 2, 1).isInside(posInMultiblock))
		{
			List<AABB> list = new ArrayList<>(2);
			if(posInMultiblock.getY()==1)
				list.add(new AABB(0, 0, 0, 1, .125f, 1));
			float minY = posInMultiblock.getY()==1?.125f: -.875f;
			float maxY = posInMultiblock.getY()==1?1.125f: .125f;

			list.add(new AABB(0, minY, 0.84375, 0.15625, maxY, 1));
			list.add(new AABB(0.0625, minY, 0, 0.1875, maxY, 0.84375));
			list.add(new AABB(0.15625, minY, 0.8125, 1, maxY, 0.9375));

			if(posInMultiblock.getY()==2)
				list.add(new AABB(0.75, .375f, -0.25, 1.25, .9375f, 0.25));
			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1, list);
		}
		else if(posInMultiblock.getY()==0&&!ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(2, 0, 1)
		).contains(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .5f, 1));
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0.5, 1, 1, 1));
		else
			return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(0, 1, 2, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SqueezerRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(level, getOutputPos(), output);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SqueezerRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 8;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 8;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<SqueezerRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 8;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
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
		return new int[]{8};
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{0};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, SqueezerBlockEntity::master,
			registerCapability(new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8]))
	);
	private final MultiblockCapability<IItemHandler> extractionHandler = MultiblockCapability.make(
			this, be -> be.extractionHandler, SqueezerBlockEntity::master,
			registerCapability(new IEInventoryHandler(1, this, 8, new boolean[1], new boolean[]{true}))
	);
	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			this, be -> be.fluidCap, SqueezerBlockEntity::master, registerFluidOutput(tanks)
	);

	private static final BlockPos inputOffset = new BlockPos(0, 1, 0);
	private static final BlockPos outputOffset = new BlockPos(1, 1, 1);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(2, 0, 1, RelativeBlockFace.RIGHT);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||FLUID_OUTPUT.equals(asRelativeFace(facing))))
			return fluidCap.getAndCast();
		if((inputOffset.equals(posInMultiblock)||outputOffset.equals(posInMultiblock))&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if(inputOffset.equals(posInMultiblock))
				return insertionHandler.getAndCast();
			if(outputOffset.equals(posInMultiblock))
				return extractionHandler.getAndCast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public SqueezerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return SqueezerRecipe.findRecipe(level, inserting);
	}

	@Override
	protected SqueezerRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return SqueezerRecipe.RECIPES.getById(level, id);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public SqueezerBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<SqueezerBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.SQUEEZER;
	}
}