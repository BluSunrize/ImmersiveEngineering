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
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

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
		flammable = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flammable"), true, 0x8f3f1f, 0, false, 0, true, true);
		slippery = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "slippery"), true, 0x171003, 0, false, 1, true, true);
		conductive = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "conductive"), true, 0x690000, 0, false, 2, true, true);
		sticky = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "sticky"), true, 0x9c6800, 0, false, 3, true, true).registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.50000000298023224D, 2);
		stunned = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "stunned"), true, 0x624a98, 0, false, 4, true, true);
		concreteFeet = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "concreteFeet"), true, 0x624a98, 0, false, 5, true, true).registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, 2);
		flashed = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flashed"), true, 0x624a98, 0, false, 6, true, true).registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15000000596046448D, 2);

		IEApi.potions = new Effect[]{flammable, slippery, conductive, sticky, stunned, concreteFeet, flashed};
	}

	public static class IEPotion extends Effect
	{
		static ResourceLocation tex = new ResourceLocation("immersiveengineering", "textures/gui/potioneffects.png");
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;

		public IEPotion(ResourceLocation resource, boolean isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
		{
			super(isBad, colour);
			this.showInInventory = showInInventory;
			this.showInHud = showInHud;
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;
			this.setIconIndex(icon%8, icon/8);

			ForgeRegistries.POTIONS.register(this.setRegistryName(resource));
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
		public int getStatusIconIndex()
		{
			Minecraft.getInstance().getTextureManager().bindTexture(tex);
			return super.getStatusIconIndex();
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
				if(living.onGround)
					living.moveRelative(0, 0, 1, 0.005F);
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
				BlockState state = living.world.getBlockState(living.getPosition());
				if(state.getBlock()!=IEContent.blockStoneDecoration&&state.getBlock()!=IEContent.blockStoneDecorationSlabs&&state.getBlock()!=IEContent.blockStoneDevice)
					living.removeActivePotionEffect(this);
			}
		}
	}
}