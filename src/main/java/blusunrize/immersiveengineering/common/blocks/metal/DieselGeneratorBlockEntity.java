/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.generic.ScaffoldingBlock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class DieselGeneratorBlockEntity extends MultiblockPartBlockEntity<DieselGeneratorBlockEntity>
		implements IBlockBounds, ISoundBE, IEClientTickableBE
{
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24*FluidType.BUCKET_VOLUME)};
	public boolean active = false;

	public float animation_fanRotationStep = 0;
	public float animation_fanRotation = 0;
	public int animation_fanFadeIn = 0;
	public int animation_fanFadeOut = 0;

	public DieselGeneratorBlockEntity(BlockEntityType<DieselGeneratorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.DIESEL_GENERATOR, type, true, pos, state);
		tanks[0].setValidator(fs -> recipeGetter.apply(level, fs.getFluid())!=null);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		active = nbt.getBoolean("active");
		animation_fanRotation = nbt.getFloat("animation_fanRotation");
		animation_fanFadeIn = nbt.getInt("animation_fanFadeIn");
		animation_fanFadeOut = nbt.getInt("animation_fanFadeOut");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundTag()));
		nbt.putBoolean("active", active);
		nbt.putFloat("animation_fanRotation", animation_fanRotation);
		nbt.putInt("animation_fanFadeIn", animation_fanFadeIn);
		nbt.putInt("animation_fanFadeOut", animation_fanFadeOut);
	}


	private final List<CapabilityReference<IEnergyStorage>> outputs = Arrays.asList(
			CapabilityReference.forBlockEntityAt(this,
					() -> new DirectionalBlockPos(this.getBlockPosForPos(new BlockPos(0, 1, 4)).offset(0, 1, 0), Direction.DOWN),
					CapabilityEnergy.ENERGY),
			CapabilityReference.forBlockEntityAt(this,
					() -> new DirectionalBlockPos(this.getBlockPosForPos(new BlockPos(1, 1, 4)).offset(0, 1, 0), Direction.DOWN),
					CapabilityEnergy.ENERGY),
			CapabilityReference.forBlockEntityAt(this,
					() -> new DirectionalBlockPos(this.getBlockPosForPos(new BlockPos(2, 1, 4)).offset(0, 1, 0), Direction.DOWN),
					CapabilityEnergy.ENERGY)
	);

	public void tickCommon()
	{
		if(active||animation_fanFadeIn > 0||animation_fanFadeOut > 0)
		{
			float base = 18f;
			float step = active?base: 0;
			if(animation_fanFadeIn > 0)
			{
				step -= (animation_fanFadeIn/80f)*base;
				animation_fanFadeIn--;
			}
			if(animation_fanFadeOut > 0)
			{
				step += (animation_fanFadeOut/80f)*base;
				animation_fanFadeOut--;
			}
			animation_fanRotationStep = step;
			animation_fanRotation += step;
			animation_fanRotation %= 360;
		}
	}

	@Override
	public void tickClient()
	{
		tickCommon();
		ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, active, .5f, 1);
		if(active&&level.getGameTime()%4==0)
		{
			BlockPos exhaust = this.getBlockPosForPos(new BlockPos(2, 2, 2));
			Direction fl = getFacing();
			Direction fw = getFacing().getClockWise();
			if(getIsMirrored())
				fw = fw.getOpposite();
			level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					exhaust.getX()+.5+(fl.getStepX()*.3125f)+(-fw.getStepX()*.3125f), exhaust.getY()+1.25, exhaust.getZ()+.5+(fl.getStepZ()*.3125f)+(-fw.getStepZ()*.3125f), 0.015625-(0.03125*Math.random()), 0.0625, 0.015625-(0.03125*Math.random()));
		}
	}

	private final BiFunction<Level, Fluid, GeneratorFuel> recipeGetter = CachedRecipe.cached(GeneratorFuel::getRecipeFor);

	@Override
	public void tickServer()
	{
		tickCommon();
		final boolean prevActive = active;

		if(!isRSDisabled()&&!tanks[0].getFluid().isEmpty())
		{
			GeneratorFuel recipe = recipeGetter.apply(level, tanks[0].getFluid().getFluid());
			if(recipe!=null)
			{
				int burnTime = recipe.getBurnTime();
				int fluidConsumed = FluidType.BUCKET_VOLUME/burnTime;
				int output = IEServerConfig.MACHINES.dieselGen_output.get();
				List<IEnergyStorage> presentOutputs = outputs.stream()
						.map(CapabilityReference::getNullable)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				if(!presentOutputs.isEmpty()&&
						tanks[0].getFluidAmount() >= fluidConsumed&&
						// Sort receivers by lowest input
						EnergyHelper.distributeFlux(presentOutputs, output, false) < output)
				{
					if(!active)
					{
						active = true;
						animation_fanFadeIn = 80;
					}
					tanks[0].drain(fluidConsumed, FluidAction.EXECUTE);
				}
				else if(active)
				{
					active = false;
					animation_fanFadeOut = 80;
				}
			}
		}
		else if(active)
		{
			active = false;
			animation_fanFadeOut = 80;
		}

		if(prevActive!=active)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	public static AABB getBlockBounds(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 0, 4).equals(posInMultiblock))
			return new AABB(0, .5f, -.625f, 1, 1.5f, 1);
		if(ImmutableSet.of(
				new BlockPos(0, 0, 4),
				new BlockPos(2, 1, 0),
				new BlockPos(2, 2, 0)
		).contains(posInMultiblock))
			return new AABB(0, 0, 0, .5f, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(ImmutableSet.of(
				new BlockPos(2, 0, 4),
				new BlockPos(0, 1, 0),
				new BlockPos(0, 2, 0)
		).contains(posInMultiblock))
			return new AABB(.5f, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
			return new AABB(0, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?.625f: 1);

		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return new AABB(0, .5f, 0, 1, 1, 1);

		if(posInMultiblock.getX()==1&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()==3)
			return new AABB(.0625f, 0, 0, .9375f, posInMultiblock.getY()==2?.3125f: 1, .625f);
		if(new BoundingBox(1, 2, 1, 1, 2, 2).isInside(posInMultiblock))
			return new AABB(.0625f, 0, 0, .9375f, .3125f, 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0)
			return new AABB(0, 0, 0, 1, .5f, 1);

		//TODO more sensible name
		boolean lessThan21 = posInMultiblock.getY()==0||(posInMultiblock.getY()==1&&posInMultiblock.getZ() > 2);
		if(posInMultiblock.getX()==0&&posInMultiblock.getY() < 2)
			return new AABB(.9375f, -.5f, 0, 1, .625f, lessThan21?.625f: 1);
		if(posInMultiblock.getX()==2&&posInMultiblock.getY() < 2)
			return new AABB(0, -.5f, 0, .0625f, .625f, lessThan21?.625f: 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==2&&posInMultiblock.getZ()==2)
			return Utils.flipBox(false, posInMultiblock.getX()==2,
					new AABB(
							0.5625, 0, -0.0625,
							1.0625, posInMultiblock.getX()==2?1.125f: .75f, 0.4375
					));

		return new AABB(0, 0, 0, 1, 1, 1);
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(DieselGeneratorBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		VoxelShape base = getShape(SHAPES);
		if(isRedstonePos()&&ctx!=null&&!ctx.isAbove(ScaffoldingBlock.CHECK_SHAPE, worldPosition, false))
		{
			AABB box = CachedShapesWithTransform.withFacingAndMirror(
					new AABB(0.9375, 0, 0, 1, 1, 1),
					getFacing(),
					getIsMirrored()
			);
			base = Shapes.or(base, Shapes.create(box));
		}
		return base;
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 1, 4).equals(posInMultiblock))
			return Lists.newArrayList(new AABB(0, .5f, 0, 1, 1, 1),
					new AABB(0, -.5f, -.625f, 1, .5f, 1));
		else if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return Utils.flipBoxes(false, posInMultiblock.getX()==2,
					new AABB(0, .5f, 0, 1, 1, 1),
					new AABB(.125f, 0, .125f, .375f, .5f, .375f),
					new AABB(.125f, 0, .625f, .375f, .5f, .875f)
			);
		if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(getBlockBounds(posInMultiblock),
					new AABB(.5f, .25f, .3125f, 1, .75f, .6875f),
					new AABB(.6875f, -.5f, .4375f, .8125f, .25f, .5625f)
			);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 4)
		{
			List<AABB> list = Lists.newArrayList(getBlockBounds(posInMultiblock));
			if(posInMultiblock.getZ() > 2)
			{
				list.add(new AABB(0.125, .5625f, 0.25, 1, .8125f, 0.5));
				list.add(new AABB(0.125, .5625f, 0.5, 0.375, .8125f, 1));
			}
			else if(posInMultiblock.getZ() > 0)
			{
				final double offset = posInMultiblock.getZ() > 1?0: 1;
				list.add(new AABB(0.4375, .5f, -0.5625+offset, 1, 1, 0.75+offset));
			}
			if(posInMultiblock.getZ() < 2)
			{
				final double offset = posInMultiblock.getZ()==1?0: 1;
				list.add(new AABB(0.375, .5625f, 0.5625+offset, 0.4375, .8125f, 0.8125+offset));
				list.add(new AABB(0.375, .5625f, -0.875+offset, 0.5, .8125f, -0.625+offset));
				list.add(new AABB(0.125, .5625f, -0.875+offset, 0.375, .8125f, 0.8125+offset));
			}
			return Utils.flipBoxes(false, posInMultiblock.getX()==2, list);
		}
		return ImmutableList.of(getBlockBounds(posInMultiblock));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			this, be -> be.fluidCap, DieselGeneratorBlockEntity::master, registerFluidInput(tanks)
	);

	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			if(facing==null||isFluidInput(facing))
				return fluidCap.getAndCast();
		return super.getCapability(capability, facing);
	}

	private boolean isFluidInput(Direction side)
	{
		return (posInMultiblock.getZ()==4&&posInMultiblock.getY()==0&&posInMultiblock.getX()%2==0)
				&&side.getAxis()==getFacing().getCounterClockWise().getAxis();
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return active;
	}

	@Override
	public float getSoundRadiusSq()
	{
		return 1024;
	}
}
