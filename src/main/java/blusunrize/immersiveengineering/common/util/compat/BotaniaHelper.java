package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemBullet.HomingBullet;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemRevolver.SpecialRevolver;
import blusunrize.immersiveengineering.common.items.ItemShader;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.item.TinyPotatoRenderEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

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
	public void init()
	{
		BlueprintCraftingRecipe.addRecipe("specialBullet", BulletHandler.getBulletStack("terrasteel"), new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, "nuggetTerrasteel", "nuggetTerrasteel");

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

			ShaderRegistryEntry entry = ItemShader.addShader("Terra", 1, rariryRelic, 0xff3e2d14, 0xff2b1108, 0xff41bd1a, 0xff2e120a).setInfo(null,"Botania","terra");
			entry.getCase("immersiveengineering:revolver").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/livingwood5"),0xffffffff).setTextureBounds(17/128d,24/128d,33/128d,40/128d));
			entry.getCase("immersiveengineering:drill").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/alfheim_portal_swirl"),0xffffffff).setTextureBounds(14/64d,10/64d, 26/64d,22/64d));
			entry.getCase("immersiveengineering:railgun").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/storage1"),0xff9e83eb).setTextureBounds(55/64d,42/64d,1,58/64d).setCutoutBounds(.1875,0,.75,1));
			entry.getCase("immersiveengineering:shield").addLayers(new ShaderLayer(new ResourceLocation("botania:blocks/crate_open"),0xffffffff).setTextureBounds(0/32f,9/32f, 14/32f,26/32f).setCutoutBounds(.0625,0, .9375,1));

		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
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
		new ThreadContributorToNameFormatter();
	}


	private static ArrayListMultimap<String, SpecialRevolver> nameToSpecial = ArrayListMultimap.create();

	public static class ThreadContributorToNameFormatter extends Thread
	{
		public ThreadContributorToNameFormatter()
		{
			setName("Immersive Engineering Contributors Name Finder Thread");
			setDaemon(true);
			start();
		}

		@Override
		public void run()
		{
			try
			{
				if(ImmersiveEngineering.ThreadContributorSpecialsDownloader.activeThread != null)
					ImmersiveEngineering.ThreadContributorSpecialsDownloader.activeThread.join();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			for(String uuid : ItemRevolver.specialRevolvers.keySet())
				nameToSpecial.putAll(ImmersiveEngineering.proxy.getNameFromUUID(uuid).toLowerCase(Locale.ENGLISH), ItemRevolver.specialRevolvers.get(uuid));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled() && event.getEntityLiving().getClass().getName().endsWith("EntityDoppleganger"))
		{
			NBTTagCompound tag = new NBTTagCompound();
			event.getEntityLiving().writeEntityToNBT(tag);
			if(tag.getBoolean("hardMode"))
				for(EntityItem item : event.getDrops())
					if(item != null && !item.getItem().isEmpty() && IEContent.itemShaderBag.equals(item.getItem().getItem()))
						ItemNBTHelper.setString(item.getItem(), "rarity", "RELIC");
		}
	}

	EntityItem revolverEntity;

	@SubscribeEvent()
	@SideOnly(Side.CLIENT)
	public void onPotatoRender(TinyPotatoRenderEvent event)
	{
		if(event.tile.getWorld() == null)
			return;
		if(revolverEntity == null)
		{
			revolverEntity = new EntityItem(event.tile.getWorld(), 0.0D, 0.0D, 0.0D, new ItemStack(IEContent.itemRevolver));
			revolverEntity.hoverStart = 0;
		}
		try
		{
			String formattedName = event.name.replace("_", " ");
			ItemRevolver.SpecialRevolver special = null;
			if(formattedName.equalsIgnoreCase("Mr Damien Hazard") || formattedName.equalsIgnoreCase("Mr Hazard"))
				special = ItemRevolver.specialRevolversByTag.get("dev");
			else if(event.name.equalsIgnoreCase("BluSunrize"))
				special = ItemRevolver.specialRevolversByTag.get("fenrir");
			else
			{
				if(nameToSpecial.containsKey(event.name.toLowerCase(Locale.ENGLISH)))
				{
					List<SpecialRevolver> list = nameToSpecial.get(event.name.toLowerCase(Locale.ENGLISH));
					if(list != null && list.size() > 0)
					{
						long ticks = event.tile.getWorld() != null ? event.tile.getWorld().getTotalWorldTime() / 100 : 0;
						special = list.get((int)(ticks % list.size()));
					}
				}
			}

			if(special != null)
			{
				GlStateManager.pushMatrix();
				((ItemRevolver)IEContent.itemRevolver).applySpecialCrafting(revolverEntity.getItem(), special);
				GlStateManager.translate(-.16, 1.45, -.2);
				GlStateManager.rotate(-90, 0, 1, 0);
				GlStateManager.rotate(15, 0, 0, 1);
				GlStateManager.rotate(180, 1, 0, 0);
				GlStateManager.scale(.625f, .625f, .625f);
				ClientUtils.mc().getRenderManager().doRenderEntity(revolverEntity, 0, 0, 0, 0, 0, false);
				GlStateManager.popMatrix();
			}
		} catch(Exception e)
		{
		}
	}
}