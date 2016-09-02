package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemRevolver.SpecialRevolver;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

public class BotaniaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
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

		ShaderRegistry.rarityWeightMap.put(EnumRarity.valueOf("RELIC"), 2);
		ShaderRegistry.registerShader("Spectral", "5", EnumRarity.EPIC, new int[]{26, 26, 40, 220}, new int[]{0, 70, 49, 220}, new int[]{40, 40, 50, 220}, new int[]{5, 10, 8, 180}, null, false, true);
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			MinecraftForge.EVENT_BUS.register(this);
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
				nameToSpecial.putAll(ImmersiveEngineering.proxy.getNameFromUUID(uuid).toLowerCase(), ItemRevolver.specialRevolvers.get(uuid));
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
					if(item != null && item.getEntityItem() != null && IEContent.itemShaderBag.equals(item.getEntityItem().getItem()))
						ItemNBTHelper.setString(item.getEntityItem(), "rarity", "RELIC");
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
				if(nameToSpecial.containsKey(event.name.toLowerCase()))
				{
					List<SpecialRevolver> list = nameToSpecial.get(event.name.toLowerCase());
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
				((ItemRevolver)IEContent.itemRevolver).applySpecialCrafting(revolverEntity.getEntityItem(), special);
				GlStateManager.translate(-.16, -1.45, -.2);
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