/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.ItemFertilizerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_RandomTeleport;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class ThermalFoundationHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		//TE Compat for smelter recipes. Not worth a separate module.
		OreDictionary.registerOre("crystalSlag", new ItemStack(IEContent.itemMaterial, 1, 7));

		ChemthrowerHandler.registerEffect("coal", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 0));
		ChemthrowerHandler.registerFlammable("coal");

		ChemthrowerHandler.registerEffect("crude_oil", new ChemthrowerEffect_Potion(null, 0, new PotionEffect(IEPotions.flammable, 140, 0), new PotionEffect(MobEffects.BLINDNESS, 80, 1)));
		ChemthrowerHandler.registerFlammable("crude_oil");
		ChemthrowerHandler.registerEffect("refined_oil", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 100, 1));
		ChemthrowerHandler.registerFlammable("refined_oil");

		ChemthrowerHandler.registerEffect("resin", new ChemthrowerEffect_Potion(null, 0, IEPotions.sticky, 100, 1));
		ChemthrowerHandler.registerEffect("tree_oil", new ChemthrowerEffect_Potion(null, 0, IEPotions.flammable, 120, 0));
		ChemthrowerHandler.registerFlammable("tree_oil");

		ChemthrowerHandler.registerEffect("redstone", new ChemthrowerEffect_Potion(null, 0, IEPotions.conductive, 100, 1));
		ChemthrowerHandler.registerEffect("glowstone", new ChemthrowerEffect_Potion(null, 0, new PotionEffect(MobEffects.GLOWING, 120, 0), new PotionEffect(MobEffects.SPEED, 120, 0), new PotionEffect(MobEffects.JUMP_BOOST, 120, 0)));
		ChemthrowerHandler.registerEffect("ender", new ChemthrowerEffect_RandomTeleport(null, 0, 1));
		try
		{
			Class c_DamageHelper = Class.forName("cofh.lib.util.helpers.DamageHelper");

			DamageSource pyrotheum = (DamageSource)c_DamageHelper.getDeclaredField("pyrotheum").get(null);
			Field f_explodeCreepers = Class.forName("cofh.thermalfoundation.fluid.BlockFluidPyrotheum").getDeclaredField("effect");
			f_explodeCreepers.setAccessible(true);
			if((boolean)f_explodeCreepers.get(null))
				ChemthrowerHandler.registerEffect("pyrotheum", new ChemthrowerEffect_Damage(pyrotheum, 3)
				{
					@Override
					public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
					{
						super.applyToEntity(target, shooter, thrower, fluid);
						if(target instanceof EntityCreeper)
						{
							target.getEntityWorld().createExplosion(target, target.posX, target.posY, target.posZ, 6.0F, target.getEntityWorld().getGameRules().getBoolean("mobGriefing"));
							target.setDead();
						}
					}
				});
			else
				ChemthrowerHandler.registerEffect("pyrotheum", new ChemthrowerEffect_Damage(pyrotheum, 3));

			DamageSource cryotheum = (DamageSource)c_DamageHelper.getDeclaredField("cryotheum").get(null);
			ChemthrowerHandler.registerEffect("cryotheum", new ChemthrowerEffect_Potion(cryotheum, 2, MobEffects.SLOWNESS, 50, 3));
		} catch(Exception e)
		{
		}
		ChemthrowerHandler.registerEffect("aerotheum", new ChemthrowerEffect_Potion(null, 0, new PotionEffect(MobEffects.INVISIBILITY, 60, 0), new PotionEffect(MobEffects.WATER_BREATHING, 300, 0)));
		ChemthrowerHandler.registerEffect("petrotheum", new ChemthrowerEffect_Potion(null, 0, new PotionEffect(MobEffects.HASTE, 300, 2), new PotionEffect(MobEffects.NIGHT_VISION, 300, 0), new PotionEffect(MobEffects.RESISTANCE, 300, 1))
		{
			@Override
			public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
			{
				IBlockState state = world.getBlockState(mop.getBlockPos());
				if(state.getBlock()==Blocks.STONE||state.getBlock()==Blocks.COBBLESTONE||state.getBlock()==Blocks.STONEBRICK||state.getBlock()==Blocks.MOSSY_COBBLESTONE)
					world.setBlockState(mop.getBlockPos(), Blocks.GRAVEL.getDefaultState());
			}
		});
		ChemthrowerHandler.registerEffect("mana", new ChemthrowerEffect_RandomTeleport(null, 0, .01f));

		final Item itemPhyto = Item.REGISTRY.getObject(new ResourceLocation("thermalfoundation:fertilizer"));
		if(itemPhyto!=null)
			BelljarHandler.registerItemFertilizer(new ItemFertilizerHandler()
			{
				@Override
				public boolean isValid(@Nullable ItemStack fertilizer)
				{
					return !fertilizer.isEmpty()&&fertilizer.getItem()==itemPhyto;
				}

				@Override
				public float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
				{
					return BelljarHandler.solidFertilizerModifier*(1.5f+(.25f*fertilizer.getMetadata()));
				}
			});
	}

	@Override
	public void postInit()
	{
	}
}