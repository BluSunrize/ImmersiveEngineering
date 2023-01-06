package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.MetalPressLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.DirectProcessingItemHandler;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.MetalPressShapes;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipes;
import blusunrize.immersiveengineering.common.crafting.MetalPressPackingRecipes.RecipeDelegate;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

import static blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity.getPressTime;
import static blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity.getTransportTime;

public class MetalPressLogic implements IServerTickableMultiblock<State>, IClientTickableMultiblock<State>
{
	private static final BlockPos REDSTONE_POS = new BlockPos(1, 0, 0);
	private static final MultiblockFace OUTPUT_POS = new MultiblockFace(3, 1, 0, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition INPUT_POS = new CapabilityPosition(0, 1, 0, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition ENERGY_POS = new CapabilityPosition(1, 2, 0, RelativeBlockFace.UP);

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		if(!state.renderAsActive)
			return;
		final var soundPos = context.getLevel().toAbsolute(REDSTONE_POS);
		final var level = context.getLevel().getRawLevel();
		for(MultiblockProcess<MetalPressRecipe, ?> process : state.processor.getQueue())
		{
			float maxTicks = process.getMaxTicks(level);
			float transportTime = getTransportTime(maxTicks);
			float pressTime = getPressTime(maxTicks);
			float fProcess = process.processTick;
			Player localPlayer = ImmersiveEngineering.proxy.getClientPlayer();
			//Note: the >= and < check instead of a single == is because fProcess is an int and transportTime and pressTime are floats. Because of that it has to be windowed
			if(fProcess >= transportTime&&fProcess < transportTime+1f)
				level.playSound(localPlayer, soundPos, IESounds.metalpress_piston.get(), SoundSource.BLOCKS, .3F, 1);
			if(fProcess >= (transportTime+pressTime)&&fProcess < (transportTime+pressTime+1f))
				level.playSound(localPlayer, soundPos, IESounds.metalpress_smash.get(), SoundSource.BLOCKS, .3F, 1);
			if(fProcess >= (maxTicks-transportTime)&&fProcess < (maxTicks-transportTime+1f))
				level.playSound(localPlayer, soundPos, IESounds.metalpress_piston.get(), SoundSource.BLOCKS, .3F, 1);
			++process.processTick;
		}
	}

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final var state = context.getState();
		final var active = state.processor.tickServer(
				state, context.getLevel(), context.getRedstoneInputValue(REDSTONE_POS, 0) <= 0
		);
		if(active!=state.renderAsActive)
		{
			state.renderAsActive = active;
			context.requestMasterBESync();
		}
	}

	@Override
	public void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity entity)
	{
		final var world = ctx.getLevel().getRawLevel();
		if(world.isClientSide||!INPUT_POS.posInMultiblock().equals(posInMultiblock))
			return;
		if(entity instanceof ItemEntity itemEntity&&entity.isAlive()&&!itemEntity.getItem().isEmpty())
		{
			ItemStack stack = itemEntity.getItem();
			if(stack.isEmpty())
				return;
			final var state = ctx.getState();
			MetalPressRecipe recipe = MetalPressRecipe.findRecipe(state.mold, stack, world);
			if(recipe==null)
				return;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			MultiblockProcessInWorld<MetalPressRecipe> process;
			if(recipe instanceof RecipeDelegate delegate)
				process = new SpecialMetalPressProcess(delegate, displayStack);
			else
				process = new MultiblockProcessInWorld<>(recipe, displayStack);

			if(state.processor.addProcessToQueue(process, world, false))
			{
				ItemStack remaining = stack.copy();
				remaining.shrink(displayStack.getCount());
				itemEntity.setItem(remaining);
				if(remaining.isEmpty())
					entity.discard();
			}
		}
	}

	@Override
	public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient)
	{
		final var state = ctx.getState();
		final var level = ctx.getLevel().getRawLevel();
		final var heldItem = player.getItemInHand(hand);
		ItemStack newMold = null;
		if(player.isShiftKeyDown()&&!state.mold.isEmpty())
			newMold = ItemStack.EMPTY;
		else if(MetalPressRecipe.isValidMold(level, heldItem))
			newMold = heldItem;
		if(newMold==null)
			return InteractionResult.FAIL;

		ItemStack oldMold = state.mold;
		state.mold = ItemHandlerHelper.copyStackWithSize(newMold, 1);
		newMold.shrink(1);
		if(!oldMold.isEmpty())
		{
			if(heldItem.isEmpty())
				player.setItemInHand(hand, oldMold);
			else if(!isClient)
				player.spawnAtLocation(oldMold, 0);
		}
		ctx.markMasterDirty();
		ctx.requestMasterBESync();
		return InteractionResult.SUCCESS;
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.ITEM_HANDLER&&INPUT_POS.equals(position))
			return ctx.getState().inputCap.cast(ctx);
		else if(cap==ForgeCapabilities.ENERGY&&ENERGY_POS.equalsOrNullFace(position))
			return ctx.getState().energyCap.cast(ctx);
		else
			return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter()
	{
		return MetalPressShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState, ProcessContextInWorld<MetalPressRecipe>
	{
		public ItemStack mold = ItemStack.EMPTY;
		private final AveragingEnergyStorage energy = new AveragingEnergyStorage(16000);
		public final MultiblockProcessor<MetalPressRecipe, ProcessContextInWorld<MetalPressRecipe>> processor;
		private boolean renderAsActive;

		private final CapabilityReference<IItemHandler> outputCap;
		private final StoredCapability<IItemHandler> inputCap;
		private final StoredCapability<IEnergyStorage> energyCap = new StoredCapability<>(energy);

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.processor = new MultiblockProcessor<>(
					3, maxTicks -> 1f-(getTransportTime(maxTicks)+getPressTime(maxTicks))/maxTicks, 3,
					ctx.getMarkDirtyRunnable(),
					ctx.getSyncRunnable(),
					MetalPressRecipe.STANDARD_RECIPES::getById
			);
			this.outputCap = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, OUTPUT_POS);
			this.inputCap = new StoredCapability<>(new DirectProcessingItemHandler<>(
					ctx.levelSupplier(), processor, (level, input) -> MetalPressRecipe.findRecipe(mold, input, level)
			));
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			writeCommonNBT(nbt);
			energy.deserializeNBT(nbt.get("energy"));
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			readCommonNBT(nbt);
			nbt.put("energy", energy.serializeNBT());
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			writeCommonNBT(nbt);
			nbt.putBoolean("active", renderAsActive);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			readCommonNBT(nbt);
			renderAsActive = nbt.getBoolean("active");
		}

		private void writeCommonNBT(CompoundTag nbt)
		{
			if(!mold.isEmpty())
				nbt.put("mold", mold.save(new CompoundTag()));
			// TODO write a bit less than this?
			nbt.put("processor", processor.toNBT());
		}

		private void readCommonNBT(CompoundTag nbt)
		{
			mold = ItemStack.of(nbt.getCompound("mold"));
			processor.fromNBT(nbt.get("processor"), State::loadProcess);
		}

		@Override
		public AveragingEnergyStorage getEnergy()
		{
			return energy;
		}

		@Override
		public void doProcessOutput(ItemStack result, IMultiblockLevel level)
		{
			result = Utils.insertStackIntoInventory(this.outputCap, result, false);
			if(!result.isEmpty())
				Utils.dropStackAtPos(
						level.getRawLevel(),
						level.toAbsolute(OUTPUT_POS.posInMultiblock()),
						result,
						OUTPUT_POS.face().forFront(level.getOrientation()).getOpposite()
				);
		}

		public static MultiblockProcessInWorld<MetalPressRecipe> loadProcess(
				BiFunction<Level, ResourceLocation, MetalPressRecipe> getRecipe, CompoundTag tag
		)
		{
			if(tag.contains("baseRecipe", Tag.TAG_STRING))
				return new SpecialMetalPressProcess(tag, new ResourceLocation(tag.getString("baseRecipe")));
			else
				return new MultiblockProcessInWorld<>(getRecipe, tag);
		}
	}

	private static class SpecialMetalPressProcess extends MultiblockProcessInWorld<MetalPressRecipe>
	{
		private final ResourceLocation baseRecipeLocation;

		public SpecialMetalPressProcess(CompoundTag data, ResourceLocation baseRecipeLocation)
		{
			super((level, name) -> {
				CraftingRecipe baseRecipe = MetalPressPackingRecipes.CRAFTING_RECIPE_MAP.getById(level, baseRecipeLocation);
				if(baseRecipe!=null)
					return MetalPressPackingRecipes.getRecipeDelegate(baseRecipe, name);
				else
					return null;
			}, data);
			this.baseRecipeLocation = baseRecipeLocation;
		}

		public SpecialMetalPressProcess(RecipeDelegate recipe, ItemStack inputItem)
		{
			super(recipe, inputItem);
			this.baseRecipeLocation = recipe.baseRecipe.getId();
		}

		@Override
		public void writeExtraDataToNBT(CompoundTag nbt)
		{
			super.writeExtraDataToNBT(nbt);
			nbt.putString("baseRecipe", baseRecipeLocation.toString());
		}
	}
}
