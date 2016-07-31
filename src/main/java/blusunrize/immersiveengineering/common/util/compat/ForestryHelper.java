package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ForestryHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}
	@Override
	public void init()
	{
	}
	@Override
	public void postInit()
	{
		Fluid fluidHoney = FluidRegistry.getFluid("for.honey");
		if(fluidHoney!=null)
		{
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney,100), null, "dropHoney", 6400);
			SqueezerRecipe.addRecipe(new FluidStack(fluidHoney,100), null, "dropHoneydew", 6400);
		}
		ChemthrowerHandler.registerFlammable("bio.ethanol");
		ChemthrowerHandler.registerEffect("for.honey", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,60,1));
		ChemthrowerHandler.registerEffect("juice", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,40,0));
	}
}