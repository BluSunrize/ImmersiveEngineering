package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fluids.Fluid;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.IEPotions;
import cofh.lib.util.helpers.DamageHelper;

public class ThermalFoundationHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		CrusherRecipe r;

		r=IERecipes.addItemToOreDictCrusherRecipe("dustBasalz",4, "rodBasalz", 3200);
		if(r!=null)
			r.addToSecondaryOutput("dustObsidian",.5f);

		r=IERecipes.addItemToOreDictCrusherRecipe("dustBlitz",4, "rodBlitz", 3200);
		if(r!=null)
			r.addToSecondaryOutput("dustSaltpeter",.5f);

		r=IERecipes.addItemToOreDictCrusherRecipe("dustBlizz",4, "rodBlizz", 3200);
		if(r!=null)
			r.addToSecondaryOutput(Items.snowball,.5f);

		ChemthrowerHandler.registerEffect("coal", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,100,0));
		ChemthrowerHandler.registerFlammable("coal");
		ChemthrowerHandler.registerEffect("cryotheum", new ChemthrowerEffect_Potion(DamageHelper.cryotheum,2, Potion.moveSlowdown,50,3));
		ChemthrowerHandler.registerEffect("pyrotheum", new ChemthrowerEffect_Damage(DamageHelper.pyrotheum,3));
		ChemthrowerHandler.registerEffect("ender", new ChemthrowerEffect_Teleport(null,0, .25f));
		ChemthrowerHandler.registerEffect("mana", new ChemthrowerEffect_Teleport(null,0, .05f));
		ChemthrowerHandler.registerEffect("redstone", new ChemthrowerEffect_Potion(null,0, IEPotions.conductive,100,1));
	}

	@Override
	public void postInit()
	{
	}

	public static class ChemthrowerEffect_Teleport extends ChemthrowerEffect_Damage
	{
		float chance;
		public ChemthrowerEffect_Teleport(DamageSource source, float damage, float chance)
		{
			super(source, damage);
			this.chance = chance;
		}
		@Override
		public void apply(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			super.apply(target, shooter, thrower, fluid);
			if(target.worldObj.rand.nextFloat()<chance)
			{
				double x = target.posX - 8 + target.worldObj.rand.nextInt(17);
				double y = target.posY + target.worldObj.rand.nextInt(8);
				double z = target.posZ - 8 + target.worldObj.rand.nextInt(17);

				if(!target.worldObj.getBlock((int)x, (int)y, (int)z).getMaterial().isSolid())
				{
					EnderTeleportEvent event = new EnderTeleportEvent(target, x, y, z, 0);
					if(MinecraftForge.EVENT_BUS.post(event))
						return;
					target.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ);
					target.worldObj.playSoundAtEntity(target, "mob.endermen.portal", 1.0F, 1.0F);
				}
			}
		}
	}
}