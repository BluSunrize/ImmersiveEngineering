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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class IEPotions
{
	public static Potion flammable;
	public static Potion slippery;
	public static Potion conductive;
	public static Potion sticky;
	public static Potion stunned;
	public static Potion concreteFeet;
	public static Potion flashed;

	public static void init()
	{
		flammable = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flammable"), true, 0x8f3f1f, 0, false, 0, true, true).setPotionName("immersiveengineering.potion.flammable");
		slippery = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "slippery"), true, 0x171003, 0, false, 1, true, true).setPotionName("immersiveengineering.potion.slippery");
		conductive = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "conductive"), true, 0x690000, 0, false, 2, true, true).setPotionName("immersiveengineering.potion.conductive");
		sticky = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "sticky"), true, 0x9c6800, 0, false, 3, true, true).setPotionName("immersiveengineering.potion.sticky").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.50000000298023224D, 2);
		stunned = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "stunned"), true, 0x624a98, 0, false, 4, true, true).setPotionName("immersiveengineering.potion.stunned");
		concreteFeet = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "concreteFeet"), true, 0x624a98, 0, false, 5, true, true).setPotionName("immersiveengineering.potion.concreteFeet").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -2D, 2);
		flashed = new IEPotion(new ResourceLocation(ImmersiveEngineering.MODID, "flashed"), true, 0x624a98, 0, false, 6, true, true).setPotionName("immersiveengineering.potion.flashed").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, Utils.generateNewUUID().toString(), -0.15000000596046448D, 2);

		IEApi.potions = new Potion[]{flammable, slippery, conductive, sticky, stunned, concreteFeet, flashed};
	}

	public static class IEPotion extends Potion
	{
		static ResourceLocation tex = new ResourceLocation("immersiveengineering", "textures/gui/potioneffects.png");
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		boolean showInInventory = true;
		boolean showInHud = true;

		public IEPotion(ResourceLocation resource, boolean isBad, int colour, int tick, boolean halveTick, int icon, boolean showInInventory, boolean showInHud)
		{
			super(isBad, colour);
			this.setPotionName("potion."+resource.getPath());
			this.showInInventory = showInInventory;
			this.showInHud = showInHud;
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;
			this.setIconIndex(icon%8, icon/8);

			ForgeRegistries.POTIONS.register(this.setRegistryName(resource));
		}

		@Override
		public boolean shouldRender(PotionEffect effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderInvText(PotionEffect effect)
		{
			return showInInventory;
		}

		@Override
		public boolean shouldRenderHUD(PotionEffect effect)
		{
			return showInHud;
		}

		@Override
		public int getStatusIconIndex()
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
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
		public void performEffect(EntityLivingBase living, int amplifier)
		{
			if(this==IEPotions.slippery)
			{
				if(living.onGround)
					living.moveRelative(0, 0, 1, 0.005F);
				EntityEquipmentSlot hand = living.getRNG().nextBoolean()?EntityEquipmentSlot.MAINHAND: EntityEquipmentSlot.OFFHAND;
				if(!living.world.isRemote&&living.getRNG().nextInt(300)==0&&!living.getItemStackFromSlot(hand).isEmpty())
				{
					EntityItem dropped = living.entityDropItem(living.getItemStackFromSlot(hand).copy(), 1);
					dropped.setPickupDelay(20);
					living.setItemStackToSlot(hand, ItemStack.EMPTY);
				}
			}
			else if(this==IEPotions.concreteFeet&&!living.world.isRemote)
			{
				IBlockState state = living.world.getBlockState(living.getPosition());
				if(state.getBlock()!=IEContent.blockStoneDecoration&&state.getBlock()!=IEContent.blockStoneDecorationSlabs&&state.getBlock()!=IEContent.blockStoneDevice)
				{
					PotionEffect effect = living.getActivePotionEffect(this);
					if(effect!=null)
						effect.duration = 0;
				}
			}
		}
	}
}