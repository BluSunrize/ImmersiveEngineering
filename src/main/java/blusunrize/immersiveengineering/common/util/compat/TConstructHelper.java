package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.oredict.OreDictionary;

public class TConstructHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
		sendFluidForMelting("Uranium", 0x596552, 600);
		Fluid fluidCons = sendFluidForMelting("Constantan", 0xf7866c, 518);
		sendAlloyForMelting(new FluidStack(fluidCons, 2), "copper",1, "nickel",1);

		FMLInterModComms.sendMessage("tconstruct", "blacklistMelting", new ItemStack(IEContent.itemBullet, 1, OreDictionary.WILDCARD_VALUE));
	}

	@Override
	public void init()
	{
		//		IERecipes.addOreDictAlloyingRecipe("ingotAluminumBrass",4, "Copper", 100,512, "dustAluminum","dustAluminum","dustAluminum");
		//
		//		IERecipes.addOredictRecipe(new ItemStack(IEContent.blockClothDevice, 2,0), " F ","FTF"," S ", 'F',"fabricHemp", 'T',"torchStone", 'S',"slabTreatedWood");
		//
		//		ChemthrowerHandler.registerEffect("glue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		//		ChemthrowerHandler.registerEffect("slime.blue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		//		
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodIron"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodSteel"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("ironRod"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("steelRod"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
	}

	@Override
	public void postInit()
	{
	}

	public static Fluid sendFluidForMelting(String ore, int colour, int temp)
	{
		Fluid fluid = new FluidColouredMetal(ore.toLowerCase(), colour, temp);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("fluid", fluid.getName());
		tag.setString("ore", ore);
		tag.setBoolean("toolforge", true);
		FMLInterModComms.sendMessage("tconstruct", "integrateSmeltery", tag);
		return fluid;
	}

	public static void sendAlloyForMelting(FluidStack output, Object... input)
	{
		assert(input.length%2==0);
		FluidStack[] inputStacks = new FluidStack[input.length/2];
		for(int i=0; i<inputStacks.length; i++)
			if(input[i] instanceof String && input[i+1] instanceof Integer)
			{
				Fluid f = FluidRegistry.getFluid((String)input[i]);
				if(f!=null)
					inputStacks[i] = new FluidStack(f, (Integer)input[i+1]);
			}

		NBTTagList tagList = new NBTTagList();
		tagList.appendTag(output.writeToNBT(new NBTTagCompound()));
		for(FluidStack stack : inputStacks)
			if(stack!=null)
				tagList.appendTag(stack.writeToNBT(new NBTTagCompound()));

		NBTTagCompound message = new NBTTagCompound();
		message.setTag("alloy", tagList);
		FMLInterModComms.sendMessage("tconstruct", "alloy", message);
	}


	public static class FluidColouredMetal extends Fluid
	{
		public static ResourceLocation ICON_MetalStill = new ResourceLocation("tconstruct:blocks/fluids/molten_metal");
		public static ResourceLocation ICON_MetalFlowing = new ResourceLocation("tconstruct:blocks/fluids/molten_metal_flow");

		int colour;
		public FluidColouredMetal(String name, int colour, int temp)
		{
			super(name, ICON_MetalStill, ICON_MetalFlowing);
			this.colour = colour;
			this.setTemperature(temp);
			this.setDensity(2000);
			this.setViscosity(10000);
			FluidRegistry.registerFluid(this);
		}

		@Override
		public int getColor()
		{
			return colour;
		}
	}
}