/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemBullet.HomingBullet;
import blusunrize.immersiveengineering.common.items.ItemShader;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Method;

public class BotaniaHelper extends IECompatModule
{
	EnumRarity rariryRelic;

	@Override
	public void preInit()
	{
		BulletHandler.registerBullet("terrasteel", new HomingBullet(IEConfig.Tools.bulletDamage_Homing, new ResourceLocation("immersiveengineering:items/bullet_terrasteel")));
		BulletHandler.homingCartridges.add("terrasteel");
	}

	@Override
	public void registerRecipes()
	{
		BlueprintCraftingRecipe.addRecipe("specialBullet", BulletHandler.getBulletStack("terrasteel"), new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetTerrasteel", "nuggetTerrasteel");
	}

	@Override
	public void init()
	{
		try
		{
			Class c_BotaniaAPI = Class.forName("vazkii.botania.api.BotaniaAPI");
			Method m_blacklistBlockFromMagnet = c_BotaniaAPI.getDeclaredMethod("blacklistBlockFromMagnet", Block.class, int.class);
			m_blacklistBlockFromMagnet.invoke(null, IEContent.blockConveyor, 0);
		} catch(Exception e)
		{
			IELogger.error("[Botania] Failed to protect IE conveyors against Botania's magnets");
			e.printStackTrace();
		}
		rariryRelic = EnumRarity.valueOf("RELIC");
		if(rariryRelic!=null)
		{
			ShaderRegistry.rarityWeightMap.put(rariryRelic, 2);
			makeShaderRelic("The Kindled");
			makeShaderRelic("Dark Fire");

			ShaderRegistryEntry entry = ItemShader.addShader("Terra", 1, rariryRelic, 0xff3e2d14, 0xff2b1108, 0xff41bd1a, 0xff2e120a).setInfo(null, "Botania", "terra");
			entry.getCase("immersiveengineering:revolver").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/livingwood5"), 0xffffffff).setTextureBounds(17/128d, 24/128d, 33/128d, 40/128d));
			entry.getCase("immersiveengineering:drill").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/alfheim_portal_swirl"), 0xffffffff).setTextureBounds(14/64d, 10/64d, 26/64d, 22/64d));
			entry.getCase("immersiveengineering:railgun").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/storage1"), 0xff9e83eb).setTextureBounds(55/64d, 42/64d, 1, 58/64d).setCutoutBounds(.1875, 0, .75, 1));
			entry.getCase("immersiveengineering:shield").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/crate_open"), 0xffffffff).setTextureBounds(0/32f, 9/32f, 14/32f, 26/32f).setCutoutBounds(.0625, 0, .9375, 1));

		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			MinecraftForge.EVENT_BUS.register(this);
	}

	void makeShaderRelic(String shader)
	{
		ShaderRegistryEntry entry = ShaderRegistry.shaderRegistry.get(shader);
		entry.rarity = rariryRelic;
		entry.setReplicationCost(ShaderRegistry.defaultReplicationCost.copyWithMultipliedSize(10-2));
	}

	@Override
	public void postInit()
	{
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&event.getEntityLiving().getClass().getName().endsWith("EntityDoppleganger"))
		{
			NBTTagCompound tag = new NBTTagCompound();
			event.getEntityLiving().writeEntityToNBT(tag);
			if(tag.getBoolean("hardMode"))
				for(EntityItem item : event.getDrops())
					if(item!=null&&!item.getItem().isEmpty()&&IEContent.itemShaderBag.equals(item.getItem().getItem()))
						ItemNBTHelper.setString(item.getItem(), "rarity", "RELIC");
		}
	}
}