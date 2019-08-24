/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat112;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaublesHelper extends IECompatModule
{
	private static final IBauble BAUBLE_POWERPACK = new IBauble()
	{
		@Override
		public BaubleType getBaubleType(ItemStack itemStack)
		{
			return BaubleType.BODY;
		}

		@Override
		public void onWornTick(ItemStack itemstack, LivingEntity player)
		{
			if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
				IEBipedLayerRenderer.addWornPowerpack(player, itemstack);
			if(player instanceof PlayerEntity)
				IEContent.itemPowerpack.onArmorTick(player.world, (PlayerEntity)player, itemstack);
		}
	};

	@Override
	public void preInit()
	{
		Lib.BAUBLES = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void registerRecipes()
	{

	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}

	@SubscribeEvent
	public void onCapabilitiesAttach(AttachCapabilitiesEvent<ItemStack> event)
	{
		if(event.getObject().getItem()==IEContent.itemPowerpack)
		{
			event.addCapability(new ResourceLocation("baubles", "bauble_cap"), new ICapabilityProvider()
			{
				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing)
				{
					return capability==BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
				}

				@Nullable
				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
				{
					return capability==BaublesCapabilities.CAPABILITY_ITEM_BAUBLE?BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(BAUBLE_POWERPACK): null;
				}
			});
		}
	}

}