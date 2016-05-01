package blusunrize.immersiveengineering.common.items;

import java.util.List;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxContainerItem;
import blusunrize.immersiveengineering.client.models.ModelManeuverGear;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.entities.EntityGrapplingHook;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ManeuverGearHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.BaublesHelper;
import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class ItemManeuverGear extends ItemArmor implements baubles.api.IBauble, IFluxContainerItem, IEnergyContainerItem, ISpecialArmor
{
	public static int hookCost = 80;
	public static int rechargeCooldown = 5*20;
	public static int rechargeFlux = 20;
	public static double jumpCost = .125;

	public ItemManeuverGear()
	{
		super(ArmorMaterial.LEATHER, 0, 2);
		String name = "maneuverGear";
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		GameRegistry.registerItem(this, name);
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "immersiveengineering:textures/models/maneuverGear.png";
	}

	@SideOnly(Side.CLIENT)
	ModelBiped armorModel;
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot, ModelBiped _default)
	{
		ModelManeuverGear model = ModelManeuverGear.getModel();
		return model;
	}
	
	@Override
    public int getColor(ItemStack stack)
    {
		return 0xffffff;
    }

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		list.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.energyStored", ItemNBTHelper.getInt(stack, "energy")));
		list.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.gasStored", Utils.formatDouble(ItemNBTHelper.getFloat(stack, "gas")*100,"0.###")+"%"));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(Lib.BAUBLES)
		{
			ItemStack belt = BaublesHelper.getBauble(player, 3);
			if(belt==null)
			{
				BaublesHelper.setBauble(player, 3, stack.copy());
				stack.stackSize = 0;
				return stack;
			}
		}

		int i = EntityLiving.getArmorPosition(stack) - 1;
		ItemStack itemstack = player.getCurrentArmor(i);
		if(itemstack == null)
		{
			player.setCurrentItemOrArmor(i + 1, stack.copy());
			stack.stackSize = 0;
		}
		return stack;
	}
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
	{
		onEquippedTick(player, stack);
	}

	public void onEquippedTick(EntityLivingBase player, ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "cooldown"))
		{
			EntityGrapplingHook[] hooks = ManeuverGearHelper.getHooks((EntityPlayer)player);
			if((hooks[0]==null||hooks[0].isDead) && (hooks[1]==null||hooks[1].isDead))
			{
				int cooldown = ItemNBTHelper.getInt(stack, "cooldown");
				if(--cooldown<=0)
					ItemNBTHelper.remove(stack, "cooldown");
				else
					ItemNBTHelper.setInt(stack, "cooldown", cooldown);
			}
		}
		else
		{
			float gas = ItemNBTHelper.getFloat(stack, "gas");
			if(gas<1 && extractEnergy(stack, rechargeFlux, true)==rechargeFlux)
			{
				extractEnergy(stack, rechargeFlux, false);
				gas = Math.min(1, gas+.03125f);
				ItemNBTHelper.setFloat(stack, "gas", gas);
			}
		}
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
	{
		return this.armorType == armorType && entity instanceof EntityPlayer && !ManeuverGearHelper.isPlayerWearing3DMG((EntityPlayer)entity);
	}

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity)
	{
		return entity instanceof EntityPlayer && !ManeuverGearHelper.isPlayerWearing3DMG((EntityPlayer)entity);
	}
	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity)
	{
		return true;
	}
	@Override
	public baubles.api.BaubleType getBaubleType(ItemStack stack)
	{
		return baubles.api.BaubleType.BELT;
	}
	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity)
	{
	}
	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity)
	{
	}
	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity)
	{
		onEquippedTick(entity, stack);
	}


	@Override
	public int receiveEnergy(ItemStack container, int energy, boolean simulate)
	{
		return ItemNBTHelper.insertFluxItem(container, energy, getMaxEnergyStored(container), simulate);
	}
	@Override
	public int extractEnergy(ItemStack container, int energy, boolean simulate)
	{
		return ItemNBTHelper.extractFluxFromItem(container, energy, simulate);
	}
	@Override
	public int getEnergyStored(ItemStack container)
	{
		return ItemNBTHelper.getFluxStoredInItem(container);
	}
	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 8000;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot)
	{
		return new ArmorProperties(0,0,0);
	}
	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot)
	{
		return 0;
	}
	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot)
	{
	}
}