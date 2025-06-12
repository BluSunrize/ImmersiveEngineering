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
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.register.IEPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
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
			public void applyToEntity(LivingEntity target, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, FluidStack fluid)
			{
				if(fluid.has(DataComponents.POTION_CONTENTS))
				{
					PotionContents potionContents = fluid.get(DataComponents.POTION_CONTENTS);
					for(MobEffectInstance e : potionContents.getAllEffects())
					{
						MobEffect effect = e.getEffect().value();
						if(effect.isInstantenous())
							effect.applyInstantenousEffect(projectile, shooter, target, e.getAmplifier(), 1);
						else
						{
							MobEffectInstance newEffect = new MobEffectInstance(e.getEffect(), (int)Math.ceil(e.getDuration()*.05), e.getAmplifier());
							target.addEffect(newEffect);
						}
					}
				}
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, FluidStack fluid)
			{

			}

			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
			}
		});

		ChemthrowerHandler.registerEffect(fluidConcrete, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, FluidStack fluid)
			{
				hit(target.level(), target.blockPosition(), Direction.UP);
			}

			@Override
			public void applyToEntity(LivingEntity target, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, FluidStack fluid)
			{
				if(!(mop instanceof BlockHitResult))
					return;
				BlockHitResult brtr = (BlockHitResult)mop;
				BlockState hit = world.getBlockState(brtr.getBlockPos());
				if(hit.getBlock()!=StoneDecoration.CONCRETE_SPRAYED.get())
				{
					BlockPos pos = brtr.getBlockPos().relative(brtr.getDirection());
					if(!world.isEmptyBlock(pos))
						return;
					AABB aabb = new AABB(pos);
					List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesOfClass(ChemthrowerShotEntity.class, aabb);
					if(otherProjectiles.size() >= 8)
						hit(world, pos, brtr.getDirection());
				}
			}

			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
			}

			private void hit(Level world, BlockPos pos, Direction side)
			{
				AABB aabb = new AABB(pos);
				List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesOfClass(ChemthrowerShotEntity.class, aabb);
				for(ChemthrowerShotEntity shot : otherProjectiles)
					shot.discard();
				world.setBlockAndUpdate(pos, StoneDecoration.CONCRETE_SPRAYED.defaultBlockState());
				for(LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, aabb))
					living.addEffect(new MobEffectInstance(IEPotions.CONCRETE_FEET, Integer.MAX_VALUE));
			}
		});

		ChemthrowerHandler.registerEffect(fluidHerbicide, new ChemthrowerEffect()
		{
			@Override
			public void applyToEntity(LivingEntity target, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
			}

			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
				if(!(mop instanceof BlockHitResult))
					return;
				BlockHitResult brtr = (BlockHitResult)mop;
				BlockState hit = world.getBlockState(brtr.getBlockPos());
				// Kill leaves
				if(hit.is(BlockTags.LEAVES))
					world.removeBlock(brtr.getBlockPos(), false);
					// turn grass & farmland to dirt
				else if(hit.getBlock() instanceof SnowyDirtBlock||hit.getBlock()==Blocks.FARMLAND)
				{
					world.setBlockAndUpdate(brtr.getBlockPos(), Blocks.DIRT.defaultBlockState());
					BlockPos above = brtr.getBlockPos().above();
					if(world.getBlockState(above).getBlock() instanceof BushBlock)
						world.removeBlock(above, false);
				}

				// Remove excess particles
				AABB aabb = new AABB(brtr.getBlockPos()).inflate(.25);
				List<ChemthrowerShotEntity> otherProjectiles = world.getEntitiesOfClass(ChemthrowerShotEntity.class, aabb);
				for(ChemthrowerShotEntity shot : otherProjectiles)
					shot.discard();
			}
		});

		ChemthrowerHandler.registerEffect(fluidCreosote, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE, 140, 0));
		ChemthrowerHandler.registerFlammable(fluidCreosote);
		ChemthrowerHandler.registerEffect(fluidBiodiesel, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE, 140, 1));
		ChemthrowerHandler.registerFlammable(fluidBiodiesel);
		ChemthrowerHandler.registerEffect(fluidHighPowerBiodiesel, new ChemthrowerEffect_Potion(null, 0, IEPotions.FLAMMABLE, 140, 1));
		ChemthrowerHandler.registerFlammable(fluidHighPowerBiodiesel);
		ChemthrowerHandler.registerFlammable(fluidEthanol);
		ChemthrowerHandler.registerFlammable(fluidAcetaldehyde);
		ChemthrowerHandler.registerEffect(fluidResin, new ChemthrowerEffect_Potion(null, 0, IEPotions.STICKY, 140, 1));
		ChemthrowerHandler.registerEffect(fluidRedstoneAcid, new ChemthrowerEffect_Potion(null, 0, IEPotions.CONDUCTIVE, 140, 1)
		{
			@Override
			public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, @Nullable Entity projectile, ItemStack thrower, Fluid fluid)
			{
				if(!(mop instanceof BlockHitResult blockHit)||!(world instanceof ServerLevel serverLevel))
					return;
				BlockState hit = world.getBlockState(blockHit.getBlockPos());
				if(hit.getBlock() instanceof WeatheringCopper copperBlock)
					copperBlock.changeOverTime(hit, serverLevel, blockHit.getBlockPos(), world.random);
			}
		});
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
