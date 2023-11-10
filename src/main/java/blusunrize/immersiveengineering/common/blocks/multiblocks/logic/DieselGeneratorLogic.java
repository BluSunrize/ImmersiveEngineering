/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl.RSState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.shapes.DieselGeneratorShapes;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.sound.MultiblockSound;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DieselGeneratorLogic
		implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<State>
{
	private static final List<BlockPos> ENERGY_OUTPUTS = IntStream.range(0, 3)
			.mapToObj(i -> new BlockPos(i, 2, 4))
			.toList();
	private static final Vec3 SMOKE_POSITION = new Vec3(2.1875, 3.25, 2.1875);
	public static final BlockPos REDSTONE_POS = new BlockPos(2, 1, 2);
	private static final CapabilityPosition FLUID_INPUT_A = new CapabilityPosition(0, 0, 4, RelativeBlockFace.RIGHT);
	private static final CapabilityPosition FLUID_INPUT_B = new CapabilityPosition(2, 0, 4, RelativeBlockFace.LEFT);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		boolean active = context.getState().active;
		if(state.rsState.isEnabled(context)&&!state.tank.getFluid().isEmpty())
		{
			int output = IEServerConfig.MACHINES.dieselGen_output.get();
			List<IEnergyStorage> presentOutputs = state.energyOutputs.stream()
					.map(CapabilityReference::getNullable)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			if(!presentOutputs.isEmpty()&&EnergyHelper.distributeFlux(presentOutputs, output, false) < output)
				state.consumeTick--;
			if(state.consumeTick <= 0) //Consume 10*tick-amount every 10ticks to allow for 1/10th mB amounts
			{
				GeneratorFuel recipe = state.recipeGetter.apply(
						context.getLevel().getRawLevel(), state.tank.getFluid().getFluid()
				);
				if(recipe!=null)
				{
					int burnTime = recipe.getBurnTime();
					int fluidConsumed = (10*FluidType.BUCKET_VOLUME)/burnTime;
					if(state.tank.getFluidAmount() >= fluidConsumed)
					{
						if(!active)
							active = true;
						state.tank.drain(fluidConsumed, FluidAction.EXECUTE);
						state.consumeTick = 10;
					}
					else if(active)
						active = false;
				}
			}
		}
		else if(active)
			active = false;

		if(active!=state.active)
		{
			state.active = active;
			context.markMasterDirty();
			context.requestMasterBESync();
		}
	}

	@Override
	public void tickClient(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		if(state.active||state.animation_fanFadeIn > 0||state.animation_fanFadeOut > 0)
		{
			float base = 18f;
			float step = state.active?base: 0;
			if(state.animation_fanFadeIn > 0)
			{
				step -= (state.animation_fanFadeIn/80f)*base;
				state.animation_fanFadeIn--;
			}
			if(state.animation_fanFadeOut > 0)
			{
				step += (state.animation_fanFadeOut/80f)*base;
				state.animation_fanFadeOut--;
			}
			state.animation_fanRotationStep = step;
			state.animation_fanRotation += step;
			state.animation_fanRotation %= 360;
		}
		if(!state.isSoundPlaying.getAsBoolean())
		{
			final Vec3 soundPos = context.getLevel().toAbsolute(new Vec3(2.5, 1.5, 1.5));
			state.isSoundPlaying = MultiblockSound.startSound(
					() -> state.active, context.isValid(), soundPos, IESounds.dieselGenerator, 0.5f
			);
		}
		if(state.active&&context.getLevel().shouldTickModulo(2))
		{
			final Vec3 absoluteSmokePosition = context.getLevel().toAbsolute(SMOKE_POSITION);
			context.getLevel().getRawLevel().addAlwaysVisibleParticle(
					ParticleTypes.CAMPFIRE_COSY_SMOKE,
					absoluteSmokePosition.x, absoluteSmokePosition.y, absoluteSmokePosition.z,
					particleXZSpeed(), 0.0625, particleXZSpeed()
			);
		}
	}

	private double particleXZSpeed()
	{
		return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625);
	}

	@Override
	public State createInitialState(IInitialMultiblockContext<State> capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T>
	LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap)
	{
		if(cap==ForgeCapabilities.FLUID_HANDLER)
			if(FLUID_INPUT_A.equalsOrNullFace(position)||FLUID_INPUT_B.equalsOrNullFace(position))
				return ctx.getState().fluidCap.cast(ctx);
		if(cap==ForgeCapabilities.ENERGY)
			if(position.side()==null||(
					position.side()==RelativeBlockFace.UP&&ENERGY_OUTPUTS.contains(position.posInMultiblock())
			))
				return ctx.getState().energyView.cast(ctx);
		return LazyOptional.empty();
	}

	@Override
	public Function<BlockPos, VoxelShape> shapeGetter(ShapeType forType)
	{
		if(forType==ShapeType.BLOCK_SUPPORT)
			return DieselGeneratorShapes.GETTER_WITH_REDSTONE_SUPPORT;
		else
			return DieselGeneratorShapes.SHAPE_GETTER;
	}

	public static class State implements IMultiblockState
	{
		// Server fields
		public final FluidTank tank = new FluidTank(24*FluidType.BUCKET_VOLUME);
		private boolean active = false;
		private int consumeTick = 0;
		public final RSState rsState = RSState.enabledByDefault();

		// Client fields
		public float animation_fanRotationStep = 0;
		public float animation_fanRotation = 0;
		private int animation_fanFadeIn = 0;
		private int animation_fanFadeOut = 0;
		private BooleanSupplier isSoundPlaying = () -> false;

		// Utils
		private final BiFunction<Level, Fluid, GeneratorFuel> recipeGetter = CachedRecipe.cached(GeneratorFuel::getRecipeFor);
		private final List<CapabilityReference<IEnergyStorage>> energyOutputs;
		private final StoredCapability<IFluidHandler> fluidCap = new StoredCapability<>(tank);
		private final StoredCapability<IEnergyStorage> energyView;

		public State(IInitialMultiblockContext<State> ctx)
		{
			ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs = ImmutableList.builder();
			for(BlockPos pos : ENERGY_OUTPUTS)
				outputs.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.DOWN));
			this.energyOutputs = outputs.build();
			this.energyView = new StoredCapability<>(NullEnergyStorage.INSTANCE);
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			nbt.put("tank0", tank.writeToNBT(new CompoundTag()));
			nbt.putBoolean("active", active);
			nbt.putInt("consumeTick", consumeTick);
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			tank.readFromNBT(nbt.getCompound("tank0"));
			active = nbt.getBoolean("active");
			consumeTick = nbt.getInt("consumeTick");
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
			nbt.putBoolean("active", active);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
			final boolean oldActive = active;
			active = nbt.getBoolean("active");
			if(active&&!oldActive)
				animation_fanFadeIn = 80;
			else if(!active&&oldActive)
				animation_fanFadeOut = 80;
		}

		public boolean isActive()
		{
			return active;
		}
	}
}
