package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Damage;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fluids.Fluid;

public class FoundryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
//		ChemthrowerHandler.registerEffect("coal", new ChemthrowerEffect_Potion(null,0, IEPotions.flammable,100,0));
//		ChemthrowerHandler.registerFlammable("coal");
//		ChemthrowerHandler.registerEffect("cryotheum", new ChemthrowerEffect_Potion(DamageHelper.cryotheum,2, Potion.moveSlowdown,50,3));
//		ChemthrowerHandler.registerEffect("pyrotheum", new ChemthrowerEffect_Damage(DamageHelper.pyrotheum,3));
		ChemthrowerHandler.registerEffect("liquidenderpearl", new ChemthrowerEffect_RandomTeleport(null,0, .25f));
//		ChemthrowerHandler.registerEffect("mana", new ChemthrowerEffect_RandomTeleport(null,0, .05f));
		ChemthrowerHandler.registerEffect("liquidredstone", new ChemthrowerEffect_Potion(null,0, IEPotions.conductive,100,1));
		ChemthrowerHandler.registerEffect("liquidglowstone", new ChemthrowerEffect_Potion(null,0, Potion.getPotionFromResourceLocation("glowing"),200,0));
	}

	@Override
	public void postInit()
	{
	}

	public static class ChemthrowerEffect_RandomTeleport extends ChemthrowerEffect_Damage
	{
		float chance;
		public ChemthrowerEffect_RandomTeleport(DamageSource source, float damage, float chance)
		{
			super(source, damage);
			this.chance = chance;
		}
		@Override
		public void applyToEntity(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(target.worldObj.rand.nextFloat()<chance)
			{
				double x = target.posX - 8 + target.worldObj.rand.nextInt(17);
				double y = target.posY + target.worldObj.rand.nextInt(8);
				double z = target.posZ - 8 + target.worldObj.rand.nextInt(17);
				if(!target.worldObj.getBlockState(new BlockPos(x,y,z)).getMaterial().isSolid())
				{
					EnderTeleportEvent event = new EnderTeleportEvent(target, x, y, z, 0);
					if(MinecraftForge.EVENT_BUS.post(event))
						return;
					target.setPositionAndUpdate(event.getTargetX(), event.getTargetY()	, event.getTargetZ());
					target.worldObj.playSound(target.posX,target.posY,target.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
				}
			}
		}
	}
}