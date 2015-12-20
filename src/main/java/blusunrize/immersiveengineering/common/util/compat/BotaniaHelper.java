package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import vazkii.botania.api.BotaniaAPI;

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
}