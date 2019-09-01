/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.blocks.BlockIEFluid;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.tools.TinkerTraits;

public class TConstructHelper extends IECompatModule
{
	public static final Material treatedWood = new Material("treatedwood", 0x653522);
	public static final Material constantan = new Material("constantan", 0xf0816a);
	public static final Material hemp = new Material("hemp", 0xa68b78);
	public static final AbstractTrait thermalInversion = new TraitThermalInversion();

	public static Fluid fluidUranium;
	public static Block blockMoltenUranium;
	public static Fluid fluidConstantan;
	public static Block blockMoltenConstantan;

	static
	{
		fluidUranium = IEContent.setupFluid(new FluidColouredMetal("uranium", 0x596552, 600));
		sendFluidForMelting("Uranium", fluidUranium);
		blockMoltenUranium = new BlockIEFluid("molten_uranium", fluidUranium, net.minecraft.block.material.Material.LAVA);

		fluidConstantan = IEContent.setupFluid(new FluidColouredMetal("constantan", 0xf7866c, 518));
		sendFluidForMelting("Constantan", fluidConstantan);
		blockMoltenConstantan = new BlockIEFluid("molten_constantan", fluidConstantan, net.minecraft.block.material.Material.LAVA);
	}

	@Override
	public void preInit()
	{
		sendAlloyForMelting(new FluidStack(fluidConstantan, 2), "copper", 1, "nickel", 1);

		FMLInterModComms.sendMessage("tconstruct", "blacklistMelting", new ItemStack(IEContent.itemBullet, 1, OreDictionary.WILDCARD_VALUE));
		FMLInterModComms.sendMessage("tconstruct", "blacklistMelting", new ItemStack(IEContent.itemDrillhead, 1, OreDictionary.WILDCARD_VALUE));

		TinkerRegistry.addMaterialStats(treatedWood,
				new HeadMaterialStats(25, 2.00f, 2.00f, HarvestLevels.STONE),
				new HandleMaterialStats(1.0f, 35),
				new ExtraMaterialStats(20));
		TinkerRegistry.addMaterialStats(treatedWood, new BowMaterialStats(1f, 1.125f, 0), new ArrowShaftMaterialStats(1.2f, 0));
		try
		{
			TinkerRegistry.integrate(treatedWood, "plankTreatedWood").preInit();
		} catch(Exception e)
		{
			IELogger.logger.error("[TCon] Material 'treatedWood' has already been registered");
		}

		TinkerRegistry.addMaterialStats(hemp, new BowStringMaterialStats(1f));
		TinkerRegistry.integrate(hemp).preInit();

		TinkerRegistry.addMaterialStats(constantan,
				new HeadMaterialStats(25, 4.70f, 4.00f, HarvestLevels.DIAMOND),
				new HandleMaterialStats(0.8f, 60),
				new ExtraMaterialStats(60));
		TinkerRegistry.addMaterialStats(constantan, new BowMaterialStats(.55f, 1.5f, 5f));
		try
		{
			TinkerRegistry.integrate(constantan, fluidConstantan, "Constantan").toolforge().preInit();
		} catch(Exception e)
		{
			IELogger.logger.error("[TCon] Material 'constantan' has already been registered");
		}
		ToolboxHandler.addToolType((s) -> (s.getItem() instanceof TinkersItem));
	}

	@Override
	public void registerRecipes()
	{
		if(ApiUtils.isExistingOreName("ingotAlubrass"))
			IERecipes.addOreDictArcAlloyingRecipe("ingotAlubrass", 4, "Copper", 100, 512, "dustAluminum", "dustAluminum", "dustAluminum");
		IERecipes.addOreDictArcAlloyingRecipe("ingotManyullyn", 1, "Cobalt", 200, 512, "ingotArdite");
		IERecipes.addOreDictArcAlloyingRecipe("ingotManyullyn", 1, "Ardite", 200, 512, "ingotCobalt");

		Fluid fluidClay = FluidRegistry.getFluid("clay");
		if(fluidClay!=null)
			MixerRecipe.addRecipe(new FluidStack(IEContent.fluidConcrete, 500), new FluidStack(fluidClay, 500), new Object[]{"sand", "sand", "gravel"}, 3200);
	}

	@Override
	public void init()
	{
		treatedWood.setCraftable(true);
		treatedWood.addItem("stickTreatedWood", 1, Material.VALUE_Shard);
		treatedWood.addItem("plankTreatedWood", 1, Material.VALUE_Ingot);
		treatedWood.addTrait(TinkerTraits.ecological, MaterialTypes.HEAD);
		treatedWood.addTrait(TinkerTraits.ecological);

		hemp.addItemIngot("fiberHemp");
		hemp.setRepresentativeItem(new ItemStack(IEContent.itemMaterial, 1, 4));

		constantan.setCastable(true);
		constantan.addItem("nuggetConstantan", 1, Material.VALUE_Nugget);
		constantan.addItem("ingotConstantan", 1, Material.VALUE_Ingot);
		constantan.addTrait(thermalInversion);

		//		ChemthrowerHandler.registerEffect("glue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		ChemthrowerHandler.registerEffect("blueslime", new ChemthrowerEffect_Potion(null, 0, IEPotions.sticky, 100, 1));
		ChemthrowerHandler.registerEffect("purpleslime", new ChemthrowerEffect_Potion(null, 0, IEPotions.sticky, 100, 2));
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
		treatedWood.setRenderInfo(new MaterialRenderInfo.BlockTexture(new ResourceLocation(ImmersiveEngineering.MODID, "blocks/treatedWood_horizontal")));
		constantan.setRenderInfo(new MaterialRenderInfo.Metal(0xae5d4c, 0.1f, 0.2f, 0f));
		hemp.setRenderInfo(0xa68b78);
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
		assert (input.length%2==0);
		FluidStack[] inputStacks = new FluidStack[input.length/2];
		for(int i = 0; i < inputStacks.length; i++)
			if(input[i*2] instanceof String&&input[i*2+1] instanceof Integer)
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
			if(target.isEntityAlive()&&wasHit)
			{
				BlockPos pos = player.getPosition();
				Biome biome = player.world.getBiomeForCoordsBody(pos);
				float tempDif = biome.getTemperature(pos)-0.5f;
				if(tempDif!=0)
				{
					if(tempDif < 0&&!target.isImmuneToFire())
						target.setFire((int)Math.floor(tempDif*3));
					else if(tempDif > 0)
						target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 4, (int)Math.floor(tempDif*2)));
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
