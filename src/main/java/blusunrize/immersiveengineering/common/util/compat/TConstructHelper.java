package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.BlockIEFluid;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.tconstruct.TinkerIntegration;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.tools.TinkerMaterials;

public class TConstructHelper extends IECompatModule
{
	public static final Material treatedWood = new Material("treatedwood", 0x653522);
	public static final Material constantan = new Material("constantan", 0xf0816a);
	public static final AbstractTrait thermalInversion = new TraitThermalInversion();

	public static Fluid fluidUranium;
	public static Block blockMoltenUranium;
	public static Fluid fluidConstantan;
	public static Block blockMoltenConstantan;

	@Override
	public void preInit()
	{
		//		sendFluidForMelting("Uranium", 0x596552, 600);
		fluidUranium = new FluidColouredMetal("uranium", 0x596552, 600);
		sendFluidForMelting("Uranium", fluidUranium);
		blockMoltenUranium = new BlockIEFluid("molten_uranium", fluidUranium, net.minecraft.block.material.Material.LAVA);

		//		Fluid fluidCons = sendFluidForMelting("Constantan", 0xf7866c, 518);
		fluidConstantan = new FluidColouredMetal("constantan", 0xf7866c, 518);
		sendFluidForMelting("Constantan", fluidUranium);
		blockMoltenConstantan = new BlockIEFluid("molten_constantan", fluidConstantan, net.minecraft.block.material.Material.LAVA);


		sendAlloyForMelting(new FluidStack(fluidConstantan, 2), "copper",1, "nickel",1);
		//		FluidStack output = fluids.get(0);
		//		FluidStack[] input = new FluidStack[fluids.size()-1];
		//		input = fluids.subList(1, fluids.size()).toArray(input);
		//		TinkerRegistry.registerAlloy(new FluidStack(fluidCons, 2), new FluidStack[]{new FluidStack(fluidCons, 2)});

		FMLInterModComms.sendMessage("tconstruct", "blacklistMelting", new ItemStack(IEContent.itemBullet, 1, OreDictionary.WILDCARD_VALUE));


		treatedWood.setCraftable(true);
		treatedWood.addItem("stickTreatedWood", 1, Material.VALUE_Shard);
		treatedWood.addItem("plankTreatedWood", 1, Material.VALUE_Ingot);
		treatedWood.addTrait(TinkerMaterials.ecological, HeadMaterialStats.TYPE);
		treatedWood.addTrait(TinkerMaterials.ecological);
		TinkerRegistry.addMaterialStats(treatedWood,
				new HeadMaterialStats(25, 2.00f, 2.00f, HarvestLevels.STONE),
				new HandleMaterialStats(1.0f, 35),
				new ExtraMaterialStats(20));
		TinkerIntegration.integrate(treatedWood, "plankTreatedWood").integrate();

		constantan.setCastable(true);
		constantan.addItem("nuggetConstantan", 1, Material.VALUE_Nugget);
		constantan.addItem("ingotConstantan", 1, Material.VALUE_Ingot);
		constantan.addTrait(thermalInversion);
		TinkerRegistry.addMaterialStats(constantan,
				new HeadMaterialStats(25, 4.70f, 4.00f, HarvestLevels.DIAMOND),
				new HandleMaterialStats(0.8f, 60),
				new ExtraMaterialStats(60));
		TinkerIntegration.integrate(constantan, fluidConstantan, "Constantan").toolforge().integrate();
		ToolboxHandler.addToolType((s)->(s.getItem() instanceof TinkersItem));
	}

	@Override
	public void init()
	{
		if(ApiUtils.isExistingOreName("ingotAluBrass"))
			IERecipes.addOreDictAlloyingRecipe("ingotAluBrass", 4, "Copper", 100, 512, "dustAluminum", "dustAluminum", "dustAluminum");
		IERecipes.addOreDictAlloyingRecipe("ingotManyullyn", 1, "Cobalt", 200, 512, "ingotArdite");
		IERecipes.addOreDictAlloyingRecipe("ingotManyullyn", 1, "Ardite", 200, 512, "ingotCobalt");
		//		ChemthrowerHandler.registerEffect("glue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		ChemthrowerHandler.registerEffect("blueslime", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		ChemthrowerHandler.registerEffect("purpleslime", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,2));
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodIron"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodSteel"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("ironRod"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		//		RailgunHandler.registerProjectileProperties(new ComparableItemStack("steelRod"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
	}

	@Override
	public void postInit()
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPostInit()
	{
		treatedWood.setRenderInfo(new MaterialRenderInfo.BlockTexture("immersiveengineering:blocks/treatedWood_horizontal"));
		constantan.setRenderInfo(new MaterialRenderInfo.Metal(0xae5d4c, 0.1f, 0.2f, 0f));
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

	public static Fluid sendFluidForMelting(String ore, Fluid fluid)
	{
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
			if(input[i*2] instanceof String && input[i*2+1] instanceof Integer)
			{
				Fluid f = FluidRegistry.getFluid((String)input[i*2]);
				if(f!=null)
					inputStacks[i] = new FluidStack(f, (Integer)input[i*2+1]);
			}

		NBTTagList tagList = new NBTTagList();
		tagList.appendTag(output.writeToNBT(new NBTTagCompound()));
		for(FluidStack stack : inputStacks)
			if(stack!=null)
				tagList.appendTag(stack.writeToNBT(new NBTTagCompound()));

		NBTTagCompound message = new NBTTagCompound();
		message.setTag("alloy", tagList);
		//		FMLInterModComms.sendMessage("tconstruct", "alloy", message);
		//	For some reason IMC on this is broken? So direct interaction is required. Oh well.
		TinkerRegistry.registerAlloy(output, inputStacks);
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
			return colour|0xff000000;
		}
	}

	public static class TraitThermalInversion extends AbstractTrait
	{
		public TraitThermalInversion()
		{
			super("thermalinversion", 0xf3826a);
		}

		@Override
		public void afterHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damageDealt, boolean wasCritical, boolean wasHit)
		{
			if(target.isEntityAlive() && wasHit)
			{
				BlockPos pos = player.getPosition();
				Biome biome = player.worldObj.getBiomeForCoordsBody(pos);
				float tempDif = biome.getFloatTemperature(pos)-0.5f;
				if(tempDif!=0)
				{
					if(tempDif<0 && !target.isImmuneToFire())
						target.setFire((int)Math.floor(tempDif*3));
					else if(tempDif>0)
						target.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("slowness"),4,(int)Math.floor(tempDif*2)));
				}
			}
		}
	}
//
//
//	@SideOnly(Side.CLIENT)
//	@Override
//	public void clientPreInit()
//	{
//		mapFluidState(blockMoltenUranium, fluidUranium);
//		mapFluidState(blockMoltenConstantan, fluidConstantan);
//	}
}