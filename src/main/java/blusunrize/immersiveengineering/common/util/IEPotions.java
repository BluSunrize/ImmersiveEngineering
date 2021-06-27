/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import com.google.common.collect.ImmutableList;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Set;

public class IEPotions
{
	public static final DeferredRegister<Effect> REGISTER = DeferredRegister.create(ForgeRegistries.POTIONS, Lib.MODID);

	public static RegistryObject<Effect> flammable = REGISTER.register(
			"flammable", () -> new IEPotion(EffectType.HARMFUL, 0x8f3f1f, 0, false, 0, true, true)
	);
	public static RegistryObject<Effect> slippery = REGISTER.register(
			"slippery", () -> new IEPotion(EffectType.HARMFUL, 0x171003, 0, false, 1, true, true)
	);
	public static RegistryObject<Effect> conductive = REGISTER.register(
			"conductive", () -> new IEPotion(EffectType.HARMFUL, 0x690000, 0, false, 2, true, true)
	);
	public static RegistryObject<Effect> sticky = REGISTER.register(
			"sticky", () -> new IEPotion(EffectType.HARMFUL, 0x9c6800, 0, false, 3, true, true)
					.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.5, Operation.MULTIPLY_TOTAL)
	);
	public static RegistryObject<Effect> stunned = REGISTER.register(
			"stunned", () -> new IEPotion(EffectType.HARMFUL, 0x624a98, 0, false, 4, true, true)
	);
	public static RegistryObject<Effect> concreteFeet = REGISTER.register(
			"concrete_feet", () -> new IEPotion(EffectType.HARMFUL, 0x624a98, 0, false, 5, true, true)
					.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, Operation.MULTIPLY_TOTAL)
	);
	public static RegistryObject<Effect> flashed = REGISTER.register(
			"flashed", () -> new IEPotion(EffectType.HARMFUL, 0x624a98, 0, false, 6, true, true)
					.addAttributesModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15, Operation.MULTIPLY_TOTAL)
	);

	static
	{
		IEApi.potions = ImmutableList.of(flammable, slippery, conductive, sticky, stunned, concreteFeet, flashed);
	}

	public static class IEPotion extends Effect
	{
		private static final Set<Block> concrete = ImmutableSet.<Block>builder()
				.add(StoneDecoration.concrete.get())
				.add(StoneDecoration.concreteTile.get())
				.add(StoneDecoration.concreteSprayed.get())
				.add(IEBlocks.toStairs.get(StoneDecoration.concrete.getId()).get())
				.add(StoneDecoration.concreteThreeQuarter.get())
				.add(StoneDecoration.concreteSheet.get())
				.add(StoneDecoration.concreteQuarter.get())
				.add(StoneDecoration.concreteLeaded.get())
				.build();
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;

		public IEPotion(EffectType isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
		{
			super(isBad, colour);
			this.showInInventory = showInInventory;
			this.showInHud = showInHud;
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;
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
			if(this==IEPotions.slippery.get())
			{
				if(living.isOnGround())
					living.moveRelative(0, new Vector3d(0, 1, 0.005));
				EquipmentSlotType hand = living.getRNG().nextBoolean()?EquipmentSlotType.MAINHAND: EquipmentSlotType.OFFHAND;
				if(!living.world.isRemote&&living.getRNG().nextInt(300)==0&&!living.getItemStackFromSlot(hand).isEmpty())
				{
					ItemEntity dropped = living.entityDropItem(living.getItemStackFromSlot(hand).copy(), 1);
					dropped.setPickupDelay(20);
					living.setItemStackToSlot(hand, ItemStack.EMPTY);
				}
			}
			else if(this==IEPotions.concreteFeet.get()&&!living.world.isRemote)
			{
				BlockState state = living.world.getBlockState(living.getPosition());
				if(!concrete.contains(state.getBlock())&&
						concrete.stream()
								.map(Block::getRegistryName)
								.map(IEBlocks.toSlab::get)
								.filter(Objects::nonNull)
								.noneMatch(b -> b.get()==state.getBlock()))
				{
					living.removePotionEffect(this);
					IELogger.logger.info("Removing concrete feet");
				}
			}
		}
	}
}