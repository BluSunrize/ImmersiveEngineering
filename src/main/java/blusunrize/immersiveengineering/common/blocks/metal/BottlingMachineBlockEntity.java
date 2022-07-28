/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BottlingMachineBlockEntity extends PoweredMultiblockBlockEntity<BottlingMachineBlockEntity, BottlingMachineRecipe>
		implements IConveyorAttachable, IBlockBounds, IEClientTickableBE, IHammerInteraction
{
	public static final float TRANSLATION_DISTANCE = 2.5f;
	private static final float STANDARD_TRANSPORT_TIME = 16f*(TRANSLATION_DISTANCE/2); //16 frames in conveyor animation, 1 frame/tick, 2.5 blocks of total translation distance, halved because transport time just affects half the distance
	private static final float STANDARD_LIFT_TIME = 3.75f;
	private static final float MIN_CYCLE_TIME = 60f; //set >= 2*(STANDARD_LIFT_TIME+STANDARD_TRANSPORT_TIME)
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(8*FluidAttributes.BUCKET_VOLUME)};
	private boolean allowPartialFill = false;

	public BottlingMachineBlockEntity(BlockEntityType<BottlingMachineBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.BOTTLING_MACHINE, 16000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank"));
		allowPartialFill = nbt.getBoolean("allowPartialFill");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank", tanks[0].writeToNBT(new CompoundTag()));
		nbt.putBoolean("allowPartialFill", allowPartialFill);
	}

	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(2, 1, 1)).relative(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tickClient()
	{
		if(!shouldRenderAsActive())
			return;
		// Todo: Maybe do sounds here?
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(new BlockPos(1, 0, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		if(posInMultiblock.getY()==0||new BlockPos(2, 1, 0).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		if(posInMultiblock.getZ()==1&&posInMultiblock.getY()==1)
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
			return Shapes.box(.0625f, 0, .0625f, .9375f, 1, .9375f);
		if(new BlockPos(1, 1, 0).equals(posInMultiblock))
		{
			Direction f = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
			float xMin = f==Direction.EAST?-.0625f: f==Direction.WEST?.25f: getFacing()==Direction.WEST?.125f: getFacing()==Direction.EAST?.25f: 0;
			float zMin = getFacing()==Direction.NORTH?.125f: getFacing()==Direction.SOUTH?.25f: f==Direction.SOUTH?-.0625f: f==Direction.NORTH?.25f: 0;
			float xMax = f==Direction.EAST?.75f: f==Direction.WEST?1.0625f: getFacing()==Direction.WEST?.75f: getFacing()==Direction.EAST?.875f: 1;
			float zMax = getFacing()==Direction.NORTH?.75f: getFacing()==Direction.SOUTH?.875f: f==Direction.SOUTH?.75f: f==Direction.NORTH?1.0625f: 1;
			return Shapes.box(xMin, .0625f, zMin, xMax, .6875f, zMax);
		}
		if(new BlockPos(1, 2, 1).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?0: .21875f;
			float zMin = getFacing()==Direction.NORTH?0: .21875f;
			float xMax = getFacing()==Direction.EAST?1: .78125f;
			float zMax = getFacing()==Direction.SOUTH?1: .78125f;
			return Shapes.box(xMin, -.4375f, zMin, xMax, .5625f, zMax);
		}
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
		{
			float xMin = getFacing()==Direction.WEST?.8125f: getFacing()==Direction.EAST?0: .125f;
			float zMin = getFacing()==Direction.NORTH?.8125f: getFacing()==Direction.SOUTH?0: .125f;
			float xMax = getFacing()==Direction.WEST?1: getFacing()==Direction.EAST?.1875f: .875f;
			float zMax = getFacing()==Direction.NORTH?1: getFacing()==Direction.SOUTH?.1875f: .875f;
			return Shapes.box(xMin, -1, zMin, xMax, .25f, zMax);
		}
		return Shapes.box(0, 0, 0, 1, 1, 1);
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(2, 1, 0, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(new BlockPos(1, 0, 1));
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&!world.isClientSide&&entity instanceof ItemEntity itemEntity&&entity.isAlive())
		{
			BottlingMachineBlockEntity master = master();
			if(master==null)
				return;

			List<Tuple<ItemEntity, ItemStack>> itemsOnConveyor = level.getEntitiesOfClass(
					ItemEntity.class, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(getBlockPos()))
			).stream().map(itemEntity1 -> new Tuple<>(itemEntity1, itemEntity1.getItem())).toList();
			if(itemsOnConveyor.isEmpty())
				return;

			ItemStack[] stacks = itemsOnConveyor.stream().map(Tuple::getB).toArray(ItemStack[]::new);
			BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(
					master.level,
					stacks,
					master.tanks[0].getFluid()
			);

			MultiblockProcess<BottlingMachineRecipe> process;
			NonNullList<ItemStack> displayStacks;
			if(recipe==null)
			{
				displayStacks = Utils.createNonNullItemStackListFromItemStack(stacks[0]);
				process = new MultiblockProcessBottling(displayStacks);
			}
			else
			{
				displayStacks = recipe.getDisplayStacks(stacks);
				process = new MultiblockProcessBottling(
						recipe.getId(),
						this::getRecipeForId,
						displayStacks
				);
			}

			if(master.addProcessToQueue(process, true))
			{
				master.addProcessToQueue(process, false);
				for(ItemStack stack : displayStacks)
					itemsOnConveyor.stream().filter(t -> ItemStack.isSameItemSameTags(t.getB(), stack))
							.findFirst().ifPresent(t -> {
								ItemStack remaining = t.getB().copy();
								remaining.shrink(stack.getCount());
								t.getA().setItem(remaining);
								if(remaining.isEmpty())
									t.getA().discard();
							});
			}
		}
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<BottlingMachineRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
			BlockPos pos = getBlockPos().relative(outDir, 2);
			Utils.dropStackAtPos(level, pos, output, outDir);
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<BottlingMachineRecipe> process)
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

	public static float getTransportTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_TRANSPORT_TIME;
		else
			return processMaxTicks*STANDARD_TRANSPORT_TIME/MIN_CYCLE_TIME;
	}

	public static float getLiftTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_LIFT_TIME;
		else
			return processMaxTicks*STANDARD_LIFT_TIME/MIN_CYCLE_TIME;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<BottlingMachineRecipe> process)
	{
		float maxTicks = process.getMaxTicks(level);
		return 1f-(getTransportTime(maxTicks)+getLiftTime(maxTicks))/maxTicks;
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public BottlingMachineRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected BottlingMachineRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return BottlingMachineRecipe.RECIPES.getById(level, id);
	}

	@Nullable
	protected MultiblockProcess<BottlingMachineRecipe> loadProcessFromNBT(CompoundTag tag)
	{
		NonNullList<ItemStack> inputs = NonNullList.withSize(tag.getInt("numInputs"), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, inputs);
		if(tag.getBoolean("isFilling"))
			return new MultiblockProcessBottling(inputs);
		ResourceLocation id = new ResourceLocation(tag.getString("recipe"));
		return new MultiblockProcessBottling(id, this::getRecipeForId, inputs);
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, BottlingMachineBlockEntity::master,
			registerCapability(new BottlingMachineInventoryHandler(this))
	);
	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			this, be -> be.fluidCap, BottlingMachineBlockEntity::master, registerFluidInput(tanks)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			if(facing==null||(BlockPos.ZERO.equals(posInMultiblock)&&facing.getAxis().isHorizontal()))
				return fluidCap.getAndCast();
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().getClockWise(): this.getFacing().getCounterClockWise()))
				return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()};
		return new Direction[0];
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(player.isCrouching())
		{
			if(!level.isClientSide)
			{
				BottlingMachineBlockEntity master = master();
				if(master!=null)
				{
					master.allowPartialFill = !master.allowPartialFill;
					ChatUtils.sendServerNoSpamMessages(player, new TranslatableComponent(Lib.CHAT_INFO
							+"bottling_machine."+(master.allowPartialFill?"partialFill": "completeFill")));
					this.updateMasterBlock(null, true);
				}
			}
			return true;
		}
		return false;
	}

	public static class MultiblockProcessBottling extends MultiblockProcessInWorld<BottlingMachineRecipe>
	{
		private boolean isFilling = false;
		private final NonNullList<ItemStack> filledContainer = Utils.createNonNullItemStackListFromItemStack(ItemStack.EMPTY);
		private static final BottlingMachineRecipe DUMMY_RECIPE = new BottlingMachineRecipe(
				new ResourceLocation(Lib.MODID, "bottling_dummy_recipe"),
				List.of(Lazy.of(() -> ItemStack.EMPTY)), IngredientWithSize.of(ItemStack.EMPTY),
				new FluidTagInput(FluidTags.WATER, 0)
		);

		public MultiblockProcessBottling(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, BottlingMachineRecipe> getRecipe, NonNullList<ItemStack> inputItem)
		{
			super(recipeId, getRecipe, 0.45f, inputItem);
		}

		public MultiblockProcessBottling(NonNullList<ItemStack> inputItem)
		{
			this(DUMMY_RECIPE.getId(), (level, resourceLocation) -> DUMMY_RECIPE, inputItem);
			this.isFilling = true;
			// copy item into output already, to be filled later
			this.filledContainer.set(0, inputItem.get(0));
		}

		@Override
		public void doProcessTick(PoweredMultiblockBlockEntity<?, BottlingMachineRecipe> multiblock)
		{
			super.doProcessTick(multiblock);
			BottlingMachineBlockEntity bottlingMachine = (BottlingMachineBlockEntity)multiblock;

			float transPoint = getMaxTicks(multiblock.getLevel())*transformationPoint;
			if(processTick >= transPoint&&processTick < 1+transPoint)
			{
				FluidStack fs = bottlingMachine.tanks[0].getFluid();
				if(!fs.isEmpty())
				{
					// filling recipes use custom logic
					if(isFilling)
					{
						ItemStack ret = FluidUtils.fillFluidContainer(bottlingMachine.tanks[0], filledContainer.get(0), ItemStack.EMPTY, null);
						if(!ret.isEmpty())
							filledContainer.set(0, ret);
						// reduce process tick, if the item should be held in place
						if(!bottlingMachine.allowPartialFill&&!FluidUtils.isFluidContainerFull(ret))
							processTick--;
					}
					// normal recipes just consume the fluid at this point
					else
						bottlingMachine.tanks[0].drain(getRecipe(multiblock.getLevel()).fluidInput.getAmount(), FluidAction.EXECUTE);
				}
				multiblock.markContainingBlockForUpdate(null);
			}
		}

		@Override
		public List<ItemStack> getDisplayItem(Level level)
		{
			if(isFilling)
				return filledContainer;
			return super.getDisplayItem(level);
		}

		@Override
		protected List<ItemStack> getRecipeItemOutputs(PoweredMultiblockBlockEntity<?, BottlingMachineRecipe> multiblock)
		{
			if(isFilling)
				return filledContainer;
			return super.getRecipeItemOutputs(multiblock);
		}

		@Override
		public void writeExtraDataToNBT(CompoundTag nbt)
		{
			super.writeExtraDataToNBT(nbt);
			nbt.putBoolean("isFilling", isFilling);
		}
	}

	public static class BottlingMachineInventoryHandler
			extends MultiblockInventoryHandler_DirectProcessing<BottlingMachineBlockEntity, BottlingMachineRecipe>
	{
		public BottlingMachineInventoryHandler(BottlingMachineBlockEntity multiblock)
		{
			super(multiblock);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			BottlingMachineRecipe recipe = BottlingMachineRecipe.findRecipe(multiblock.level, new ItemStack[]{stack}, multiblock.tanks[0].getFluid());
			MultiblockProcess<BottlingMachineRecipe> process;
			int inputAmount = 1;
			if(recipe==null)
				process = new MultiblockProcessBottling(Utils.createNonNullItemStackListFromItemStack(stack.copy()));
			else
			{
				ItemStack displayStack = recipe.getDisplayStack(stack);
				process = new MultiblockProcessBottling(
						recipe.getId(),
						multiblock::getRecipeForId,
						Utils.createNonNullItemStackListFromItemStack(displayStack)
				);
				inputAmount = displayStack.getCount();
			}

			if(multiblock.addProcessToQueue(process, simulate))
			{
				multiblock.setChanged();
				multiblock.markContainingBlockForUpdate(null);
				stack.shrink(inputAmount);
				if(stack.getCount() <= 0)
					stack = ItemStack.EMPTY;
			}
			return stack;
		}
	}
}