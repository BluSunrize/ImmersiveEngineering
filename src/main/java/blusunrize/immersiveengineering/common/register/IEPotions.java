/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class IEPotions
{
	public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Lib.MODID);

	public static final RegistryObject<MobEffect> FLAMMABLE = REGISTER.register(
			"flammable", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x8f3f1f, 0, false, 0, true, true)
	);
	public static final RegistryObject<MobEffect> SLIPPERY = REGISTER.register(
			"slippery", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x171003, 0, false, 1, true, true)
	);
	public static final RegistryObject<MobEffect> CONDUCTIVE = REGISTER.register(
			"conductive", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x690000, 0, false, 2, true, true)
	);
	public static final RegistryObject<MobEffect> STICKY = REGISTER.register(
			"sticky", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x9c6800, 0, false, 3, true, true)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.2, Operation.MULTIPLY_TOTAL)
	);
	public static final RegistryObject<MobEffect> STUNNED = REGISTER.register(
			"stunned", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x624a98, 0, false, 4, true, true)
	);
	public static final RegistryObject<MobEffect> CONCRETE_FEET = REGISTER.register(
			"concrete_feet", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x624a98, 0, false, 5, true, true)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, Operation.MULTIPLY_TOTAL)
	);
	public static final RegistryObject<MobEffect> FLASHED = REGISTER.register(
			"flashed", () -> new IEPotion(MobEffectCategory.HARMFUL, 0x624a98, 0, false, 6, true, true)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15, Operation.MULTIPLY_TOTAL)
	);

	static
	{
		IEApi.potions = ImmutableList.of(FLAMMABLE, SLIPPERY, CONDUCTIVE, STICKY, STUNNED, CONCRETE_FEET, FLASHED);
	}

	public static class IEPotion extends MobEffect
	{
		private static final Set<Block> concrete = ImmutableSet.<Block>builder()
				.add(StoneDecoration.CONCRETE.get())
				.add(StoneDecoration.CONCRETE_TILE.get())
				.add(StoneDecoration.CONCRETE_SPRAYED.get())
				.add(IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE.getId()).get())
				.add(StoneDecoration.CONCRETE_THREE_QUARTER.get())
				.add(StoneDecoration.CONCRETE_SHEET.get())
				.add(StoneDecoration.CONCRETE_QUARTER.get())
				.add(StoneDecoration.CONCRETE_LEADED.get())
				.build();
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;

		public IEPotion(MobEffectCategory isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
		{
			super(isBad, colour);
			this.showInInventory = showInInventory;
			this.showInHud = showInHud;
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;
		}

		@Override
		public void initializeClient(Consumer<IClientMobEffectExtensions> consumer)
		{
			consumer.accept(new IClientMobEffectExtensions()
			{
				@Override
				public boolean isVisibleInGui(MobEffectInstance instance)
				{
					return showInHud;
				}

				@Override
				public boolean isVisibleInInventory(MobEffectInstance instance)
				{
					return showInInventory;
				}
			});
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
			if(this==IEPotions.SLIPPERY.get())
			{
				if(living.onGround())
					living.moveRelative(0, new Vec3(0, 1, 0.005));
				EquipmentSlot hand = living.getRandom().nextBoolean()?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND;
				if(!living.level().isClientSide&&living.getRandom().nextInt(300)==0&&!living.getItemBySlot(hand).isEmpty())
				{
					ItemEntity dropped = living.spawnAtLocation(living.getItemBySlot(hand).copy(), 1);
					dropped.setPickUpDelay(20);
					living.setItemSlot(hand, ItemStack.EMPTY);
				}
			}
			else if(this==IEPotions.CONCRETE_FEET.get()&&!living.level().isClientSide)
			{
				BlockState state = living.level().getBlockState(living.blockPosition());
				if(!concrete.contains(state.getBlock())&&
						concrete.stream()
								.map(BuiltInRegistries.BLOCK::getKey)
								.map(IEBlocks.TO_SLAB::get)
								.filter(Objects::nonNull)
								.noneMatch(b -> b.get()==state.getBlock()))
					living.removeEffect(this);
			}
		}
	}
}
