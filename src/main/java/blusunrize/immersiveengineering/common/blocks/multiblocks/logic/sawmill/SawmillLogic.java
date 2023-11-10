/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.SawmillShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.DroppingMultiblockOutput;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.inventory.InsertOnlyInventory;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SawmillLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	private static final int MAX_PROCESSES = 6;
	private static final CapabilityPosition INPUT = new CapabilityPosition(0, 1, 1, RelativeBlockFace.RIGHT);
	private static final MultiblockFace PRIMARY_OUTPUT = new MultiblockFace(5, 1, 1, RelativeBlockFace.RIGHT);
	private static final MultiblockFace SIDE_OUTPUT = new MultiblockFace(3, 0, 3, RelativeBlockFace.FRONT);
	private static final CapabilityPosition ENERGY_INPUT = new CapabilityPosition(2, 1, 0, RelativeBlockFace.UP);
	public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 2);
	private static final AABB SAWBLADE_AABB = new AABB(2.6875, 1, 1.375, 4.3125, 2, 1.625);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		final Level rawLevel = level.getRawLevel();
		final boolean rsAllowed = state.rsState.isEnabled(context);
		int i = 0;
		Iterator<SawmillProcess> processIterator = state.sawmillProcessQueue.iterator();
		Set<ItemStack> secondaries = new HashSet<>();
		while(rsAllowed&&processIterator.hasNext()&&i++ < MAX_PROCESSES)
		{
			SawmillProcess process = processIterator.next();
			if(process.processStep(rawLevel, state.energy, state.sawblade, secondaries))
				context.markMasterDirty();
			if(process.isProcessFinished())
			{
				state.output.insertOrDrop(process.getCurrentStack(rawLevel, !state.sawblade.isEmpty()).copy(), level);
				processIterator.remove();
				if(state.sawblade.hurt(IEServerConfig.MACHINES.sawmill_bladeDamage.get(), ApiUtils.RANDOM_SOURCE, null))
					state.sawblade = ItemStack.EMPTY;
				context.markDirtyAndSync();
			}
		}
		for(ItemStack output : secondaries)
			state.secondaryOutput.insertOrDrop(output.copy(), level);
		ActiveState renderActive;
		if(state.energy.getEnergyStored() <= 0||!rsAllowed||state.sawblade.isEmpty())
			renderActive = ActiveState.DISABLED;
		else if(state.sawmillProcessQueue.isEmpty())
			renderActive = ActiveState.IDLE;
		else
			renderActive = ActiveState.SAWING;
		if(state.active!=renderActive)
		{
			state.active = renderActive;
			context.markDirtyAndSync();
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> ctx)
	{
		final IMultiblockLevel level = ctx.getLevel();
		final State state = ctx.getState();
		//Handle sound
		final boolean shouldPlay = state.active!=ActiveState.DISABLED||state.lastSoundState!=ActiveState.DISABLED;
		if(shouldPlay&&!state.soundPlaying.get(state.active).getAsBoolean())
		{
			final Vec3 soundPos = level.toAbsolute(new Vec3(2.5, 1, 1.5));
			final ActiveState active = state.active;
			RegistryObject<SoundEvent> sound = switch(active)
					{
						case DISABLED -> IESounds.saw_shutdown;
						case IDLE -> IESounds.saw_empty;
						case SAWING -> IESounds.saw_full;
					};
			state.soundPlaying.put(state.active, MultiblockSound.startSound(
					() -> state.active==active, ctx.isValid(), soundPos, sound, state.active!=ActiveState.DISABLED, 0.4f
			));
		}
		state.lastSoundState = state.active;
		if(state.active==ActiveState.DISABLED)
			return;
		state.animation_bladeRotation += 36f;
		state.animation_bladeRotation %= 360f;
		state.sawmillProcessQueue.forEach(SawmillProcess::incrementProcessOnClient);

		final Level rawLevel = level.getRawLevel();
		Optional<SawmillProcess> process = state.sawmillProcessQueue.stream()
				.filter(p -> p.isSawing(rawLevel))
				.findFirst();
		//Handle empty sound
		//Handle particles & full sound
		if(process.isPresent())
		{
			Direction particleDir = level.toAbsolute(RelativeBlockFace.RIGHT);
			AABB aabb = level.toAbsolute(SAWBLADE_AABB);
			double posX = aabb.minX+rawLevel.random.nextDouble()*(aabb.maxX-aabb.minX);
			double posY = aabb.minY+rawLevel.random.nextDouble()*(aabb.maxY-aabb.minY);
			double posZ = aabb.minZ+rawLevel.random.nextDouble()*(aabb.maxZ-aabb.minZ);
			double vX = rawLevel.random.nextDouble()*particleDir.getStepX()*0.3;
			double vY = rawLevel.random.nextDouble()*0.3;
			double vZ = rawLevel.random.nextDouble()*particleDir.getStepZ()*0.3;
			rawLevel.addAlwaysVisibleParticle(
					new ItemParticleOption(ParticleTypes.ITEM, process.get().getCurrentStack(rawLevel, true)),
					posX, posY, posZ, vX, vY, vZ
			);
			//Arbitrary constant is arbitrary, but it's what sounded good in game, so we keep it. Actual length is supposed to be 10t...
			state.count++;
			if(state.count%21==0)
				rawLevel.playSound(
						ImmersiveEngineering.proxy.getClientPlayer(),
						level.toAbsolute(new BlockPos(2, 1, 1)),
						IESounds.saw_full.get(),
						SoundSource.BLOCKS, .4F, 1);
		}
		else if(state.count!=-1)
			state.count = -1;
	}

	@Override
	public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.ENERGY&&ENERGY_INPUT.equalsOrNullFace(position))
			return ctx.getState().energyCap.cast(ctx);
		else if(cap==ForgeCapabilities.ITEM_HANDLER&&INPUT.equals(position))
			return ctx.getState().insertionHandler.cast(ctx);
		else
			return LazyOptional.empty();
	}

	@Override
	public void onEntityCollision(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Entity collided)
	{
		final State state = ctx.getState();
		final IMultiblockLevel level = ctx.getLevel();
		final Level rawLevel = level.getRawLevel();
		if(rawLevel.isClientSide||collided==null||!collided.isAlive()||!state.rsState.isEnabled(ctx))
			return;
		if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&collided instanceof ItemEntity itemEntity)
		{
			ItemStack stack = itemEntity.getItem();
			if(stack.isEmpty())
				return;
			stack = stack.copy();
			if(insertItemToProcess(stack, false, state, rawLevel))
				ctx.markDirtyAndSync();
			if(stack.getCount() <= 0)
				collided.discard();
			else
				itemEntity.setItem(stack);
			return;
		}
		final AABB absoluteBladeBB = level.toAbsolute(SAWBLADE_AABB);
		if(collided instanceof LivingEntity&&!state.sawblade.isEmpty()
				&&absoluteBladeBB.intersects(collided.getBoundingBox()))
			hurtEntity(collided, ctx);
	}

	@Override
	public InteractionResult click(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock,
			Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient
	)
	{
		final State state = ctx.getState();
		final ItemStack heldItem = player.getItemInHand(hand);
		if(state.rsState.isEnabled(ctx)&&!state.sawblade.isEmpty())
		{
			if(!isClient&&player.isShiftKeyDown()&&heldItem.isEmpty())
				hurtEntity(player, ctx);
			return InteractionResult.FAIL;
		}
		if(player.isShiftKeyDown()&&!state.sawblade.isEmpty()&&heldItem.isEmpty())
		{
			player.setItemInHand(hand, state.sawblade.copy());
			state.sawblade = ItemStack.EMPTY;
			ctx.markDirtyAndSync();
			return InteractionResult.SUCCESS;
		}
		else if(heldItem.is(IETags.sawblades))
		{
			ItemStack tempBlade = !state.sawblade.isEmpty()?state.sawblade.copy(): ItemStack.EMPTY;
			state.sawblade = ItemHandlerHelper.copyStackWithSize(heldItem, 1);
			heldItem.shrink(1);
			player.setItemInHand(hand, heldItem);
			if(!tempBlade.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setItemInHand(hand, tempBlade);
				else if(!isClient)
					player.spawnAtLocation(tempBlade, 0);
			}
			ctx.markDirtyAndSync();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private void hurtEntity(Entity toHurt, IMultiblockContext<State> ctx)
	{
		if(toHurt instanceof Player player&&player.getAbilities().invulnerable)
			return;
		if(!ctx.getState().rsState.isEnabled(ctx))
			return;

		int consumed = ctx.getState().energy.extractEnergy(80, false);
		if(consumed > 0)
		{
			toHurt.hurt(IEDamageSources.sawmill(ctx.getLevel().getRawLevel()), 7);
			ctx.markMasterDirty();
		}
	}

	private static boolean insertItemToProcess(
			ItemStack stack, boolean simulate, State state, Level rawLevel
	)
	{
		if(state.sawmillProcessQueue.size() >= MAX_PROCESSES)
			return false;
		float dist = 1;
		float minProcessDist = 0.1f;
		SawmillProcess p = null;
		if(state.sawmillProcessQueue.size() > 0)
		{
			p = state.sawmillProcessQueue.get(state.sawmillProcessQueue.size()-1);
			if(p!=null)
			{
				dist = p.getRelativeProcessStep(rawLevel);
				// either it's a different item or we have 3 together already
				if(!ItemStack.isSameItem(stack, p.getInput())||state.combinedLogs > 2)
				{
					if(!simulate)
						state.combinedLogs = 0;
					minProcessDist = 0.5f;
				}
			}
		}
		else if(state.combinedLogs > 0)
			state.combinedLogs = 0;

		if(p!=null&&dist < minProcessDist)
			return false;
		if(!simulate)
		{
			p = new SawmillProcess(ItemHandlerHelper.copyStackWithSize(stack, 1));
			state.sawmillProcessQueue.add(p);
			state.combinedLogs++;
		}
		stack.shrink(1);
		return true;
	}

	@Override
	public void dropExtraItems(State state, Consumer<ItemStack> drop)
	{
		if(!state.sawblade.isEmpty())
			drop.accept(state.sawblade);
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		return SawmillShapes.SHAPE_GETTER;
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	public static ComparatorManager<State> makeComparator()
	{
		return ComparatorManager.makeSimple(state -> {
			float damage = 1-(state.sawblade.getDamageValue()/(float)state.sawblade.getMaxDamage());
			return Mth.ceil(damage*15);
		}, REDSTONE_POS);
	}

	public static class State implements IMultiblockState
	{
		private final MutableEnergyStorage energy = new MutableEnergyStorage(32000);
		public ItemStack sawblade = ItemStack.EMPTY;
		public final List<SawmillProcess> sawmillProcessQueue = new ArrayList<>();
		// this is a temporary counter to keep track of the "same" kind of log inserted. Allows combining them into threes.
		private int combinedLogs = 0;
		public final RSState rsState = RSState.enabledByDefault();

		private final DroppingMultiblockOutput output;
		private final DroppingMultiblockOutput secondaryOutput;
		private final StoredCapability<IItemHandler> insertionHandler;
		private final StoredCapability<IEnergyStorage> energyCap;

		// Client fields
		public ActiveState active = ActiveState.DISABLED;
		//Temporary counter for making sure sounds tick properly
		private int count = 0;
		public float animation_bladeRotation = 0;
		private final EnumMap<ActiveState, BooleanSupplier> soundPlaying = new EnumMap<>(ActiveState.class);
		private ActiveState lastSoundState = ActiveState.DISABLED;

		public State(IInitialMultiblockContext<State> ctx)
		{
			this.output = new DroppingMultiblockOutput(PRIMARY_OUTPUT, ctx);
			this.secondaryOutput = new DroppingMultiblockOutput(SIDE_OUTPUT, ctx);
			final Supplier<@Nullable Level> levelGetter = ctx.levelSupplier();
			final Runnable markDirty = ctx.getMarkDirtyRunnable();
			final Runnable sync = ctx.getSyncRunnable();
			this.insertionHandler = new StoredCapability<>(new InsertOnlyInventory()
			{
				@Override
				protected ItemStack insert(ItemStack toInsert, boolean simulate)
				{
					toInsert = toInsert.copy();
					if(insertItemToProcess(toInsert, simulate, State.this, levelGetter.get()))
					{
						markDirty.run();
						sync.run();
					}
					return toInsert;
				}
			});
			this.energyCap = new StoredCapability<>(energy);
			for(final ActiveState state : ActiveState.values())
				soundPlaying.put(state, () -> false);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			writeCommonNBT(nbt);
			nbt.put("energy", energy.serializeNBT());
			nbt.putInt("combinedLogs", combinedLogs);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			energy.deserializeNBT(nbt.get("energy"));
			readCommonNBT(nbt);
			combinedLogs = nbt.getInt("combinedLogs");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			writeCommonNBT(nbt);
			nbt.putInt("active", active.ordinal());
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			readCommonNBT(nbt);
			active = ActiveState.values()[nbt.getInt("active")];
		}

		private void writeCommonNBT(CompoundTag nbt)
		{
			nbt.put("sawblade", sawblade.save(new CompoundTag()));
			ListTag processes = new ListTag();
			for(final SawmillProcess process : sawmillProcessQueue)
				processes.add(process.writeToNBT());
			nbt.put("processes", processes);
		}

		private void readCommonNBT(CompoundTag nbt)
		{
			sawblade = ItemStack.of(nbt.getCompound("sawblade"));
			ListTag processes = nbt.getList("processes", Tag.TAG_COMPOUND);
			sawmillProcessQueue.clear();
			for(int i = 0; i < processes.size(); ++i)
				sawmillProcessQueue.add(SawmillProcess.readFromNBT(processes.getCompound(i)));
		}

		public IEnergyStorage getEnergy()
		{
			return energy;
		}
	}

	public enum ActiveState
	{
		DISABLED, IDLE, SAWING;
	}
}
