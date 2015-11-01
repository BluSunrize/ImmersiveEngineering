package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;

public class ThermalExpansionHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		//Constantan
		addSmelterRecipe(new ItemStack(IEContent.itemMetal,2,5), new ItemStack(IEContent.itemMetal,1,0),new ItemStack(IEContent.itemMetal,1,4), 2400, null,0);
		addSmelterRecipe(new ItemStack(IEContent.itemMetal,2,5), new ItemStack(IEContent.itemMetal,1,10),new ItemStack(IEContent.itemMetal,1,14), 1600, null,0);
	}

	@Override
	public void postInit()
	{
		Block blockFrame = GameRegistry.findBlock("ThermalExpansion", "Frame");
		if(blockFrame!=null)
		{
			Fluid fluidRedstone = FluidRegistry.getFluid("redstone");
			if(fluidRedstone!=null)
			{
				BottlingMachineRecipe.addRecipe(new ItemStack(blockFrame,1,7), new ItemStack(blockFrame,1,6), new FluidStack(fluidRedstone,4000));
				BottlingMachineRecipe.addRecipe(new ItemStack(blockFrame,1,9), new ItemStack(blockFrame,1,8), new FluidStack(fluidRedstone,4000));
			}
			Fluid fluidEnder = FluidRegistry.getFluid("ender");
			if(fluidEnder!=null)
				BottlingMachineRecipe.addRecipe(new ItemStack(blockFrame,1,11), new ItemStack(blockFrame,1,10), new FluidStack(fluidEnder,1000));

			Fluid fluidGlowstone = FluidRegistry.getFluid("glowstone");
			Block blockIlluminator = GameRegistry.findBlock("ThermalExpansion", "Light");
			if(fluidGlowstone!=null && blockIlluminator!=null)
				BottlingMachineRecipe.addRecipe(new ItemStack(blockIlluminator), new ItemStack(blockFrame,1,12), new FluidStack(fluidGlowstone,500));
		}
	}

	public static void addSmelterRecipe(ItemStack output, ItemStack input0, ItemStack input1, int energy, ItemStack secondaryOuput, int secondaryChance)
	{
		if(input0==null || input1==null || output==null)
			return;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("energy", energy);
		tag.setTag("primaryInput", new NBTTagCompound());
		tag.setTag("secondaryInput", new NBTTagCompound());
		tag.setTag("primaryOutput", new NBTTagCompound());
		if (secondaryOuput != null)
			tag.setTag("secondaryOutput", new NBTTagCompound());
		input0.writeToNBT(tag.getCompoundTag("primaryInput"));
		input1.writeToNBT(tag.getCompoundTag("secondaryInput"));
		output.writeToNBT(tag.getCompoundTag("primaryOutput"));
		if (secondaryOuput != null)
		{
			secondaryOuput.writeToNBT(tag.getCompoundTag("secondaryOutput"));
			tag.setInteger("secondaryChance", secondaryChance);
		}
		FMLInterModComms.sendMessage("ThermalExpansion", "SmelterRecipe", tag);
	}
}