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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public class IEPotions
{
	public static Effect flammable;
	public static Effect slippery;
	public static Effect conductive;
	public static Effect sticky;
	public static Effect stunned;
	public static Effect concreteFeet;
	public static Effect flashed;

	public static void init()
	{
		flammable = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flammable"), EffectType.HARMFUL,
				0x8f3f1f, 0, false, 0, true, true);
		slippery = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "slippery"), EffectType.HARMFUL,
				0x171003, 0, false, 1, true, true);
		conductive = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "conductive"), EffectType.HARMFUL,
				0x690000, 0, false, 2, true, true);
		sticky = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "sticky"), EffectType.HARMFUL,
				0x9c6800, 0, false, 3, true, true)
				.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.5, Operation.MULTIPLY_TOTAL);
		stunned = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "stunned"), EffectType.HARMFUL,
				0x624a98, 0, false, 4, true, true);
		concreteFeet = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "concrete_feet"), EffectType.HARMFUL,
				0x624a98, 0, false, 5, true, true)
				.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, Operation.MULTIPLY_TOTAL);
		flashed = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flashed"), EffectType.HARMFUL,
				0x624a98, 0, false, 6, true, true)
				.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15, Operation.MULTIPLY_TOTAL);

		IEApi.potions = new Effect[]{flammable, slippery, conductive, sticky, stunned, concreteFeet, flashed};
	}

	public static class IEPotion extends Effect
	{
		static ResourceLocation tex = new ResourceLocation("immersiveengineering", "textures/gui/potioneffects.png");
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;
		private final Set<Block> concrete;

		public IEPotion(ResourceLocation resource, EffectType isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
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
		public boolean shouldRender(EffectInstance effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderInvText(EffectInstance effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderHUD(EffectInstance effect)
		{
			return showInHud;
		}

		@Override
		public boolean isReady(int duration, int amplifier)
		{
			if(tickrate < 0)
				return false;
			int k = tickrate >> amplifier;
			return k <= 0||duration%k==0;
		}

		@Override
		public void performEffect(LivingEntity living, int amplifier)
		{
			if(this==IEPotions.slippery)
			{
				if(living.func_233570_aj_())
					living.moveRelative(0, new Vector3d(0, 1, 0.005));
				EquipmentSlotType hand = living.getRNG().nextBoolean()?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND;
				if(!living.world.isRemote&&living.getRNG().nextInt(300)==0&&!living.getItemStackFromSlot(hand).isEmpty())
				{
					ItemEntity dropped = living.entityDropItem(living.getItemStackFromSlot(hand).copy(), 1);
					dropped.setPickupDelay(20);
					living.setItemStackToSlot(hand, ItemStack.EMPTY);
				}
			}
			else if(this==IEPotions.concreteFeet&&!living.world.isRemote)
			{
				BlockState state = living.world.getBlockState(living.func_233580_cy_());
				if(!concrete.contains(state.getBlock())&&
						concrete.stream()
								.map(IEBlocks.toSlab::get)
								.noneMatch(b -> b==state.getBlock()))
				{
					living.removePotionEffect(this);
					IELogger.logger.info("Removing concrete feet");
				}
			}
		}
	}
}