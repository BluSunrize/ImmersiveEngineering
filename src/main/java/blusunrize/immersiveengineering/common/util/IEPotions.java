/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public class IEPotions
{
	public static MobEffect flammable;
	public static MobEffect slippery;
	public static MobEffect conductive;
	public static MobEffect sticky;
	public static MobEffect stunned;
	public static MobEffect concreteFeet;
	public static MobEffect flashed;

	public static void init()
	{
		flammable = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flammable"), MobEffectCategory.HARMFUL,
				0x8f3f1f, 0, false, 0, true, true);
		slippery = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "slippery"), MobEffectCategory.HARMFUL,
				0x171003, 0, false, 1, true, true);
		conductive = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "conductive"), MobEffectCategory.HARMFUL,
				0x690000, 0, false, 2, true, true);
		sticky = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "sticky"), MobEffectCategory.HARMFUL,
				0x9c6800, 0, false, 3, true, true)
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.5, Operation.MULTIPLY_TOTAL);
		stunned = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "stunned"), MobEffectCategory.HARMFUL,
				0x624a98, 0, false, 4, true, true);
		concreteFeet = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "concrete_feet"), MobEffectCategory.HARMFUL,
				0x624a98, 0, false, 5, true, true)
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, Operation.MULTIPLY_TOTAL);
		flashed = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flashed"), MobEffectCategory.HARMFUL,
				0x624a98, 0, false, 6, true, true)
				.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15, Operation.MULTIPLY_TOTAL);

		IEApi.potions = new MobEffect[]{flammable, slippery, conductive, sticky, stunned, concreteFeet, flashed};
	}

	public static class IEPotion extends MobEffect
	{
		static ResourceLocation tex = new ResourceLocation("immersiveengineering", "textures/gui/potioneffects.png");
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;
		private final Set<Block> concrete;

		public IEPotion(ResourceLocation resource, MobEffectCategory isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
		{
			super(isBad, colour);
			this.showInInventory = showInInventory;
			this.showInHud = showInHud;
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;

			ForgeRegistries.POTIONS.register(this.setRegistryName(resource));
			concrete = ImmutableSet.<Block>builder()
					.add(StoneDecoration.concrete)
					.add(StoneDecoration.concreteTile)
					.add(StoneDecoration.concreteSprayed)
					.add(StoneDecoration.concreteStairs)
					.add(StoneDecoration.concreteThreeQuarter)
					.add(StoneDecoration.concreteSheet)
					.add(StoneDecoration.concreteQuarter)
					.add(StoneDecoration.concreteLeaded)
					.build();
		}

		@Override
		public boolean shouldRender(MobEffectInstance effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderInvText(MobEffectInstance effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderHUD(MobEffectInstance effect)
		{
			return showInHud;
		}

		@Override
		public boolean isDurationEffectTick(int duration, int amplifier)
		{
			if(tickrate < 0)
				return false;
			int k = tickrate >> amplifier;
			return k <= 0||duration%k==0;
		}

		@Override
		public void applyEffectTick(LivingEntity living, int amplifier)
		{
			if(this==IEPotions.slippery)
			{
				if(living.isOnGround())
					living.moveRelative(0, new Vec3(0, 1, 0.005));
				EquipmentSlot hand = living.getRandom().nextBoolean()?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND;
				if(!living.level.isClientSide&&living.getRandom().nextInt(300)==0&&!living.getItemBySlot(hand).isEmpty())
				{
					ItemEntity dropped = living.spawnAtLocation(living.getItemBySlot(hand).copy(), 1);
					dropped.setPickUpDelay(20);
					living.setItemSlot(hand, ItemStack.EMPTY);
				}
			}
			else if(this==IEPotions.concreteFeet&&!living.level.isClientSide)
			{
				BlockState state = living.level.getBlockState(living.blockPosition());
				if(!concrete.contains(state.getBlock())&&
						concrete.stream()
								.map(IEBlocks.toSlab::get)
								.noneMatch(b -> b==state.getBlock()))
				{
					living.removeEffect(this);
					IELogger.logger.info("Removing concrete feet");
				}
			}
		}
	}
}