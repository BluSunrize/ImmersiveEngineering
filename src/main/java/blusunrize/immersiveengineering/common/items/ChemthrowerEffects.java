/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Extinguish;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static blusunrize.immersiveengineering.api.IETags.*;

public class ChemthrowerEffects
{
	public static void register()
	{
		ChemthrowerHandler.registerEffect(FluidTags.WATER, new ChemthrowerEffect_Extinguish());

		ChemthrowerHandler.registerEffect(fluidPotion, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				if(fluid.hasTag())
				{
					List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluid.getOrCreateTag());
					for(EffectInstance e : effects)
					{
						EffectInstance newEffect = new EffectInstance(e.getPotion(), (int)Math.ceil(e.getDuration()*.05), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList<>(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
				}
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{

			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}
		});

		ChemthrowerHandler.registerEffect(fluidConcrete, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				hit(target.world, target.getPosition(), Direction.UP);
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
			{
				if(!(mop instanceof BlockRayTraceResult))
					return;
				BlockRayTraceResult brtr = (BlockRayTraceResult)mop;
				BlockState hit = world.getBlockState(brtr.getPos());
				if(hit.getBlock()!=StoneDecoration.concreteSprayed)
				{
					BlockPos pos = brtr.getPos().offset(brtr.getFace());
					if(!world.isAirBlock(pos))
						return;
					AxisAlignedBB aabb = new AxisAlignedBB(pos);
					List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesWithinAABB(ChemthrowerShotEntity.class, aabb);
					if(otherProjectiles.size() >= 8)
						hit(world, pos, brtr.getFace());
				}
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			private void hit(World world, BlockPos pos, Direction side)
			{
				AxisAlignedBB aabb = new AxisAlignedBB(pos);
				List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesWithinAABB(ChemthrowerShotEntity.class, aabb);
				for(ChemthrowerShotEntity shot : otherProjectiles)
					shot.remove();
				world.setBlockState(pos, StoneDecoration.concreteSprayed.getDefaultState());
				for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, aabb))
					living.addPotionEffect(new EffectInstance(IEPotions.concreteFeet, Integer.MAX_VALUE));
			}
		});

		ChemthrowerHandler.registerEffect(fluidHerbicide, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
			{
				if(!(mop instanceof BlockRayTraceResult))
					return;
				BlockRayTraceResult brtr = (BlockRayTraceResult)mop;
				BlockState hit = world.getBlockState(brtr.getPos());
				// Kill leaves
				if(hit.isIn(BlockTags.LEAVES))
					world.removeBlock(brtr.getPos(), false);
					// turn grass & farmland to dirt
				else if(hit.getBlock() instanceof SnowyDirtBlock||hit.getBlock() instanceof FarmlandBlock)
				{
					world.setBlockState(brtr.getPos(), Blocks.DIRT.getDefaultState());
					BlockPos above = brtr.getPos().up();
					if(world.getBlockState(above).getBlock() instanceof BushBlock)
						world.removeBlock(above, false);
				}

				// Remove excess particles
				AxisAlignedBB aabb = new AxisAlignedBB(brtr.getPos()).grow(.25);
				List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesWithinAABB(ChemthrowerShotEntity.class, aabb);
				for(ChemthrowerShotEntity shot : otherProjectiles)
					shot.remove();
			}
		});

		ChemthrowerHandler.registerEffect(fluidCreosote, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 0));
		ChemthrowerHandler.registerFlammable(fluidCreosote);
		ChemthrowerHandler.registerEffect(fluidBiodiesel, new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable(fluidBiodiesel);
		ChemthrowerHandler.registerFlammable(fluidEthanol);
		/*TODO
		ChemthrowerHandler.registerEffect("oil", new ChemthrowerEffect_Potion(null, 0, new EffectInstance(IEPotions.flammable, 140, 0), new EffectInstance(Effects.BLINDNESS, 80, 1)));
		ChemthrowerHandler.registerFlammable("oil");
		ChemthrowerHandler.registerEffect("fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("fuel");
		ChemthrowerHandler.registerEffect("diesel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("diesel");
		ChemthrowerHandler.registerEffect("kerosene", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("kerosene");
		ChemthrowerHandler.registerEffect("biofuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 140, 1));
		ChemthrowerHandler.registerFlammable("biofuel");
		ChemthrowerHandler.registerEffect("rocket_fuel", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 60, 2));
		ChemthrowerHandler.registerFlammable("rocket_fuel");
		 */
	}
}
