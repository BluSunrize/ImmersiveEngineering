package blusunrize.immersiveengineering.common.util.compat;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.TinyPotatoRenderEvent;

public class BotaniaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		if(Config.getBoolean("hardmodeBulletRecipes"))
			BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotTerrasteel");
		else
			BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetTerrasteel","nuggetTerrasteel");
		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,8), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder, new ItemStack(IEContent.itemBullet,4,7));
		Config.setBoolean("botaniaBullets", true);

		if(Utils.getModVersion("Botania").startsWith("r1.8"))
		{
			BotaniaAPI.blacklistBlockFromMagnet(IEContent.blockMetalDevice, BlockMetalDevices.META_conveyorBelt);
			BotaniaAPI.blacklistBlockFromMagnet(IEContent.blockMetalDevice, BlockMetalDevices.META_conveyorDropper);
		}

		ShaderRegistry.rarityWeightMap.put(EnumRarity.valueOf("RELIC"),2);
		ShaderRegistry.registerShader("Spectral", "5", EnumRarity.epic, new int[]{26,26,40,220},new int[]{0,70,49,220},new int[]{40,40,50,220},new int[]{5,10,8,180}, null,false,true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void postInit()
	{
		new ThreadContributorToNameFormatter();
	}


	private static ArrayListMultimap<String, ItemRevolver.SpecialRevolver> nameToSpecial = ArrayListMultimap.create();
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
			while(!ImmersiveEngineering.ThreadContributorSpecialsDownloader.downloadComplete)
			{}
			for(String uuid : ItemRevolver.specialRevolvers.keySet())
				nameToSpecial.putAll(ImmersiveEngineering.proxy.getNameFromUUID(uuid).toLowerCase(), ItemRevolver.specialRevolvers.get(uuid));
		}
	}

	@SubscribeEvent(priority=EventPriority.LOW)
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled() && event.entityLiving.getClass().getName().endsWith("EntityDoppleganger"))
		{
			NBTTagCompound tag = new NBTTagCompound();
			event.entityLiving.writeEntityToNBT(tag);
			if(tag.getBoolean("hardMode"))
				for(EntityItem item : event.drops)
					if(item!=null && item.getEntityItem()!=null && IEContent.itemShaderBag.equals(item.getEntityItem().getItem()))
						ItemNBTHelper.setString(item.getEntityItem(), "rarity", "RELIC");
		}
	}

	EntityItem revolverEntity;
	@SubscribeEvent()
	public void onPotatoRender(TinyPotatoRenderEvent event)
	{
		if(event.tile.getWorldObj()==null)
			return;
		if(revolverEntity==null)
		{
			revolverEntity = new EntityItem(event.tile.getWorldObj(), 0.0D, 0.0D, 0.0D, new ItemStack(IEContent.itemRevolver));
			revolverEntity.hoverStart = 0;
		}
		try{
			String formattedName = event.name.replace("_"," ");
			ItemRevolver.SpecialRevolver special = null;
			if(formattedName.equalsIgnoreCase("Mr Damien Hazard")||event.name.equalsIgnoreCase("Mr Hazard"))
				special = ItemRevolver.specialRevolversByTag.get("dev");
			else if(event.name.equalsIgnoreCase("BluSunrize"))
				special = ItemRevolver.specialRevolversByTag.get("fenrir");
			else
			{
				if(nameToSpecial.containsKey(event.name.toLowerCase()))
				{
					List<ItemRevolver.SpecialRevolver> list = nameToSpecial.get(event.name.toLowerCase());
					if(list!=null && list.size()>0)
					{
						long ticks = event.tile.getWorldObj()!=null?event.tile.getWorldObj().getTotalWorldTime()/100:0;
						special = list.get((int)(ticks%list.size()));
					}
				}
			}

			if(special!=null)
			{
				GL11.glPushMatrix();
				((ItemRevolver)IEContent.itemRevolver).applySpecialCrafting(revolverEntity.getEntityItem(), special);
				GL11.glRotated(200, 1,0,0);
				GL11.glTranslated(-.16,-1.3,.6);
				GL11.glScalef(.625f,.625f,.625f);
				RenderManager.instance.renderEntityWithPosYaw(revolverEntity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
				GL11.glPopMatrix();
			}
		}catch(Exception e){}
	}


}