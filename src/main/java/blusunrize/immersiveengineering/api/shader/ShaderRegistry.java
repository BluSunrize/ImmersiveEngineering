/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ShaderRegistry
{
	/**
	 * A map of shader name to ShaderRegistryEntry, which contains ShaderCases, rarity, weight and loot specifics
	 */
	public static LinkedHashMap<String, ShaderRegistryEntry> shaderRegistry = new LinkedHashMap<String, ShaderRegistryEntry>();

	/**
	 * A list of shader names that can generate in chests/crates. Names are added multiple times depending on their weight
	 */
	public static ArrayList<String> chestLootShaders = new ArrayList<String>();

	/**
	 * A map of EnumRarities to weight for grab bag distribution
	 */
	public static HashMap<EnumRarity, Integer> rarityWeightMap = new HashMap<EnumRarity, Integer>();

	static
	{
		rarityWeightMap.put(EnumRarity.COMMON, 9);
		rarityWeightMap.put(EnumRarity.UNCOMMON, 7);
		rarityWeightMap.put(EnumRarity.RARE, 5);
		rarityWeightMap.put(EnumRarity.EPIC, 3);
		rarityWeightMap.put(Lib.RARITY_Masterwork, 1);
	}

	/**
	 * A list of EnumRarities sorted by their weight
	 */
	public static ArrayList<EnumRarity> sortedRarityMap = new ArrayList<EnumRarity>();


	/**
	 * A map of player names to received shaders. Saved with worlddata. Designed to prioritize shaders the player has not yet received
	 */
	public static ArrayListMultimap<String, String> receivedShaders = ArrayListMultimap.create();
	/**
	 * The map of EnumRarities to the total weight of all shaders of that rarity or rarer
	 */
	public static HashMap<EnumRarity, Integer> totalWeight = new HashMap<EnumRarity, Integer>();
	/**
	 * The total weight in relation to the player. This takes into account shaders the player has gotten, which then result in less weight
	 */
	public static HashMap<String, HashMap<EnumRarity, Integer>> playerTotalWeight = new HashMap<String, HashMap<EnumRarity, Integer>>();
	/**
	 * The deafault cost for replicating a shader. Prices are multiplied with 10-rarity level. Prices can be adjusted for every registry entry
	 */
	public static IngredientStack defaultReplicationCost = new IngredientStack("dustSilver");
	/**
	 * A HashMap to set default texture bounds for the additional layers of a shadercase. Saves you the trouble of redfining them for every shader. See {@link ShaderLayer#setTextureBounds(double... bounds)}.
	 */
	public static HashMap<ResourceLocation, double[]> defaultLayerBounds = new HashMap<ResourceLocation, double[]>();

	public static ShaderCase getShader(String name, String shaderType)
	{
		if(shaderRegistry.containsKey(name))
			return shaderRegistry.get(name).getCase(shaderType);
		return null;
	}

	public static ShaderRegistryEntry registerShader(String name, String overlayType, EnumRarity rarity, int colourPrimary, int colourSecondary, int colourBackground, int colourBlade, String additionalTexture, int colourAdditional, boolean loot, boolean bags)
	{
		registerShader_Item(name, rarity, colourBackground, colourPrimary, colourSecondary);
		registerShader_Revolver(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, additionalTexture, colourAdditional);
		registerShader_Chemthrower(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Drill(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Railgun(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Shield(name, overlayType, rarity, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Minecart(name, overlayType, rarity, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Balloon(name, overlayType, rarity, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		registerShader_Banner(name, overlayType, rarity, colourPrimary, colourSecondary, additionalTexture, colourAdditional);
		for(IShaderRegistryMethod method : shaderRegistrationMethods)
			method.apply(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, additionalTexture, colourAdditional);
		return shaderRegistry.get(name).setCrateLoot(loot).setBagLoot(bags).setReplicationCost(defaultReplicationCost.copyWithMultipliedSize(10-rarityWeightMap.get(rarity)));
	}

	public static <T extends ShaderCase> T registerShaderCase(String name, T shader, EnumRarity rarity)
	{
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);
		return shader;
	}

	/**
	 * Method to register a default implementation of Item Shaders<br>
	 * It is used for the colour and layers of the base shader item
	 *
	 * @param name    name of the shader
	 * @param rarity  Rarity of the shader item
	 * @param colour0 grip colour
	 * @param colour1 base colour
	 * @param colour2 design colour
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseItem registerShader_Item(String name, EnumRarity rarity, int colour0, int colour1, int colour2)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shader_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shader_1"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shader_2"), colour2));
		ShaderCaseItem shader = new ShaderCaseItem(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Chemthrower Shaders<br>
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           grip colour
	 * @param colour1           base colour
	 * @param colour2           design colour
	 * @param colourBlade       colour of the bayonet blade
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseRevolver registerShader_Revolver(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, int colour2, int colourBlade, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_grip"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_0"), colourBlade));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_uncoloured"), 0xffffffff));
		ShaderCaseRevolver shader = new ShaderCaseRevolver(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Chemthrower Shaders<br>
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           grip colour
	 * @param colour1           base colour
	 * @param colour2           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseChemthrower registerShader_Chemthrower(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/chemthrower_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/chemthrower_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/chemthrower_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:items/shaders/chemthrower_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/chemthrower_uncoloured"), 0xffffffff));
		ShaderCaseChemthrower shader = new ShaderCaseChemthrower(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Drill Shaders<br>
	 * Note that they have an extra layer with null for the ResourceLocation, for the drill head and augers
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           grip colour
	 * @param colour1           base colour
	 * @param colour2           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseDrill registerShader_Drill(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/drill_diesel_uncoloured"), 0xffffffff));
		list.add(new ShaderLayer(null, 0xffffffff));//final pass is for drill head and augers
		ShaderCaseDrill shader = new ShaderCaseDrill(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Railgun Shaders
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           grip colour
	 * @param colour1           base colour
	 * @param colour2           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseRailgun registerShader_Railgun(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/railgun_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/railgun_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/railgun_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:items/shaders/railgun_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/railgun_uncoloured"), 0xffffffff));

		ShaderCaseRailgun shader = new ShaderCaseRailgun(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Shield Shaders
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           base colour
	 * @param colour1           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseShield registerShader_Shield(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/shield_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/shield_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:items/shaders/shield_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:items/shaders/shield_uncoloured"), 0xffffffff));

		ShaderCaseShield shader = new ShaderCaseShield(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Minecart Shaders
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           base colour
	 * @param colour1           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseMinecart registerShader_Minecart(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		//Minecart textures need .png behind them, since they are used for direct binding, not stitching >_>
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_0.png"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_1_"+overlayType+".png"), colour1));
		if(additionalTexture!=null)
		{
			if(additionalTexture.indexOf(58) >= 0)
				list.add(new ShaderLayer(new ResourceLocation(additionalTexture+".png"), colourAddtional));
			else
				list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_"+additionalTexture+".png"), colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_uncoloured.png"), 0xffffffff));

		ShaderCaseMinecart shader = new ShaderCaseMinecart(list);
		if(overlayType.equals("1")||overlayType.equals("2")||overlayType.equals("7"))
		{
			shader.renderSides[1][1] = false;
			shader.renderSides[1][2] = false;
		}
		if(additionalTexture!=null)
		{
			shader.renderSides[2][1] = false;
			shader.renderSides[2][2] = false;
		}
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Balloon Shaders
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           base colour
	 * @param colour1           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseBalloon registerShader_Balloon(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/balloon_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/balloon_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:blocks/shaders/balloon_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/balloon_uncoloured"), 0xffffffff));

		ShaderCaseBalloon shader = new ShaderCaseBalloon(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * Method to register a default implementation of Banner Shaders
	 *
	 * @param name              name of the shader
	 * @param overlayType       uses IE's existing overlays. To use custom ones, you'll need your own method.
	 * @param rarity            Rarity of the shader item
	 * @param colour0           base colour
	 * @param colour1           design colour
	 * @param additionalTexture additional overlay texture. Null if not needed.
	 * @param colourAddtional   colour for the additional texture, if present
	 * @return the registered ShaderCase
	 */
	public static ShaderCaseBanner registerShader_Banner(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/banner_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/banner_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:blocks/shaders/banner_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:blocks/shaders/banner_uncoloured"), 0xffffffff));

		ShaderCaseBanner shader = new ShaderCaseBanner(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * A map of shader name to ShaderRegistryEntry, which contains ShaderCases, rarity, weight and loot specifics
	 */
	public static Set<IShaderRegistryMethod> shaderRegistrationMethods = new HashSet<IShaderRegistryMethod>();

	public static void addRegistrationMethod(IShaderRegistryMethod method)
	{
		shaderRegistrationMethods.add(method);
	}

	public interface IShaderRegistryMethod<T extends ShaderCase>
	{
		/**
		 * Method to register shaders for new item types<br>
		 * When adding a new shader-accepting item, use this to automatically register all of IE's default shaders for it
		 *
		 * @param name              name of the shader
		 * @param overlayType       overlay name
		 * @param colour0           grip colour
		 * @param colour1           base colour
		 * @param colour2           design colour
		 * @param colour3           colour used for bayonet blade
		 * @param additionalTexture additional overlay texture. Null if not needed.
		 * @param colourAddtional   colour for the additional texture, if present
		 * @return the registered ShaderCase
		 */
		T apply(String name, String overlayType, EnumRarity rarity, int colour0, int colour1, int colour2, int colour3, String additionalTexture, int colourAddtional);
	}

	public static ManualEntry manualEntry;
	public static Item itemShader;
	public static Item itemShaderBag;
	/**
	 * List of example items for shader manual entries
	 */
	public static List<ItemStack> itemExamples = new ArrayList();

	public static void compileWeight()
	{
		totalWeight.clear();
		chestLootShaders.clear();
		for(ShaderRegistryEntry entry : shaderRegistry.values())
		{
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				for(Map.Entry<EnumRarity, Integer> weightedRarity : rarityWeightMap.entrySet())
					if(entry.getIsInLowerBags()?(weightedRarity.getValue() >= entryRarityWeight): (weightedRarity.getValue()==entryRarityWeight))
					{
						int i = totalWeight.containsKey(weightedRarity.getKey())?totalWeight.get(weightedRarity.getKey()): 0;
						totalWeight.put(weightedRarity.getKey(), i+entry.getWeight());
					}
			}
			if(entry.getIsCrateLoot())
				for(int i = 0; i < entry.getWeight(); i++)
					chestLootShaders.add(entry.getName());
		}


		sortedRarityMap.clear();
		sortedRarityMap.addAll(ShaderRegistry.rarityWeightMap.keySet());
		Collections.sort(sortedRarityMap, new Comparator<EnumRarity>()
		{
			@Override
			public int compare(EnumRarity enum0, EnumRarity enum1)
			{
				return Integer.compare(ShaderRegistry.rarityWeightMap.get(enum0), ShaderRegistry.rarityWeightMap.get(enum1));
			}
		});

		if(manualEntry!=null)
		{
			ArrayList<PositionedItemStack[]> recipes = new ArrayList();
			NonNullList<ItemStack> shaderBags = NonNullList.withSize(ShaderRegistry.sortedRarityMap.size(), ItemStack.EMPTY);
			recipes = new ArrayList();
			for(int i = 0; i < ShaderRegistry.sortedRarityMap.size(); i++)
			{
				EnumRarity outputRarity = ShaderRegistry.sortedRarityMap.get(i);
				shaderBags.set(i, new ItemStack(itemShaderBag));
				shaderBags.get(i).setTagCompound(new NBTTagCompound());
				shaderBags.get(i).getTagCompound().setString("rarity", outputRarity.toString());
				ArrayList<EnumRarity> upperRarities = ShaderRegistry.getHigherRarities(outputRarity);
				if(!upperRarities.isEmpty())
				{
					ArrayList<ItemStack> inputList = new ArrayList();
					for(EnumRarity r : upperRarities)
					{
						ItemStack bag = new ItemStack(itemShaderBag);
						bag.setTagCompound(new NBTTagCompound());
						bag.getTagCompound().setString("rarity", r.toString());
						inputList.add(bag);
					}
					ItemStack s0 = new ItemStack(itemShaderBag, 2);
					s0.setTagCompound(new NBTTagCompound());
					s0.getTagCompound().setString("rarity", outputRarity.toString());
					if(!inputList.isEmpty())
						recipes.add(new PositionedItemStack[]{new PositionedItemStack(inputList, 33, 0), new PositionedItemStack(s0, 69, 0)});
//					inputList = new ArrayList();
//					for(ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
//						if(upperRarities.contains(entry.getRarity()))
//						{
//							ItemStack shader = new ItemStack(itemShader);
//							shader.setTagCompound(new NBTTagCompound());
//							shader.getTagCompound().setString("shader_name",  entry.getName());
//							inputList.add(shader);
//						}
//					ItemStack s1 = new ItemStack(itemShaderBag);
//					s1.setTagCompound(new NBTTagCompound());
//					s1.getTagCompound().setString("rarity", outputRarity.toString());
//					if(!inputList.isEmpty())
//						recipes.add(new PositionedItemStack[]{ new PositionedItemStack(inputList, 33, 0), new PositionedItemStack(s1, 69, 0)});
				}
			}
			manualEntry.getPages()[2] = new ManualPages.ItemDisplay(ManualHelper.getManual(), "shader2", shaderBags);
			manualEntry.getPages()[3] = new ManualPages.CraftingMulti(ManualHelper.getManual(), "shader3", (Object[])recipes.toArray(new PositionedItemStack[recipes.size()][3]));
		}
	}

	public static void recalculatePlayerTotalWeight(String player)
	{
		if(!playerTotalWeight.containsKey(player))
			playerTotalWeight.put(player, new HashMap<EnumRarity, Integer>());
		else
			playerTotalWeight.get(player).clear();
		List<String> received = receivedShaders.get(player);
		for(ShaderRegistryEntry entry : shaderRegistry.values())
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				for(Map.Entry<EnumRarity, Integer> weightedRarity : rarityWeightMap.entrySet())
					if(entry.getIsInLowerBags()?(weightedRarity.getValue() >= entryRarityWeight): (weightedRarity.getValue()==entryRarityWeight))
					{
						int weight = playerTotalWeight.get(player).containsKey(weightedRarity.getKey())?playerTotalWeight.get(player).get(weightedRarity.getKey()): 0;
						int value = entry.getWeight();
						if(received.contains(entry.getName()))
							value = 1;
						playerTotalWeight.get(player).put(weightedRarity.getKey(), weight+value);
					}
			}
	}

	public static String getRandomShader(String player, Random rand, EnumRarity minRarity, boolean addToReceived)
	{
		int total = 0;
		if(!playerTotalWeight.containsKey(player))
			playerTotalWeight.put(player, totalWeight);
		total = playerTotalWeight.get(player).get(minRarity);

		String shader = null;
		int minWeight = rarityWeightMap.get(minRarity);
		int weight = total < 1?total: rand.nextInt(total);
		for(ShaderRegistryEntry entry : shaderRegistry.values())
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				if(entry.getIsInLowerBags()?(minWeight >= entryRarityWeight): (minWeight==entryRarityWeight))
				{
					int value = entry.getWeight();
					if(receivedShaders.get(player).contains(entry.getName()))
						value = 1;
					weight -= value;
					if(weight <= 0)
					{
						shader = entry.getName();
						break;
					}
				}
			}
		if(addToReceived)
		{
			if(!receivedShaders.get(player).contains(shader))
				receivedShaders.put(player, shader);
			recalculatePlayerTotalWeight(player);
		}
		return shader;
	}

	public static EnumRarity getLowerRarity(EnumRarity rarity)
	{
		int idx = sortedRarityMap.indexOf(rarity);
		int weight = rarityWeightMap.get(rarity);
		for(int next = idx+1; next < sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next)) > weight)
				return sortedRarityMap.get(next);
		return null;
	}

	public static ArrayList<EnumRarity> getAllLowerRarities(EnumRarity rarity)
	{
		ArrayList<EnumRarity> list = new ArrayList<EnumRarity>();
		int idx = sortedRarityMap.indexOf(rarity);
		int weight = rarityWeightMap.get(rarity);
		for(int next = idx+1; next < sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next)) > weight)
				list.add(sortedRarityMap.get(next));
		return list;
	}

	public static ArrayList<EnumRarity> getHigherRarities(EnumRarity rarity)
	{
		ArrayList<EnumRarity> list = new ArrayList<EnumRarity>();
		int idx = sortedRarityMap.indexOf(rarity);
		if(idx <= 0)
			return list;
		int next = idx-1;
		int weight = rarityWeightMap.get(rarity);
		int lowerWeight = -1;
		for(; next >= 0; next--)
		{
			EnumRarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight < weight&&(lowerWeight==-1||rWeight >= lowerWeight))
			{
				list.add(r);
				lowerWeight = rWeight;
			}
		}
		return list;
	}

	public static ArrayList<EnumRarity> getAllHigherRarities(EnumRarity rarity)
	{
		ArrayList<EnumRarity> list = new ArrayList<EnumRarity>();
		int idx = sortedRarityMap.indexOf(rarity);
		if(idx <= 0)
			return list;
		int next = idx-1;
		int weight = rarityWeightMap.get(rarity);
		for(; next >= 0; next--)
		{
			EnumRarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight < weight)
				list.add(r);
		}
		return list;
	}

	public static Triple<ItemStack, ShaderRegistryEntry, ShaderCase> getStoredShaderAndCase(ItemStack itemStack)
	{
		if(!itemStack.isEmpty()&&itemStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			CapabilityShader.ShaderWrapper wrapper = itemStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			if(wrapper!=null)
				return getStoredShaderAndCase(wrapper);
		}
		return null;
	}

	public static Triple<ItemStack, ShaderRegistryEntry, ShaderCase> getStoredShaderAndCase(CapabilityShader.ShaderWrapper wrapper)
	{
		ItemStack shader = wrapper.getShaderItem();
		if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
		{
			IShaderItem iShaderItem = ((IShaderItem)shader.getItem());
			ShaderRegistryEntry registryEntry = shaderRegistry.get(iShaderItem.getShaderName(shader));
			if(registryEntry!=null)
				return Triple.of(shader, registryEntry, registryEntry.getCase(wrapper.getShaderType()));
		}
		return null;
	}

	public static class ShaderRegistryEntry
	{
		public String name;
		public HashMap<String, ShaderCase> cases = new HashMap<String, ShaderCase>();
		public EnumRarity rarity;
		public int weight;
		public boolean isCrateLoot;
		public boolean isBagLoot;
		public boolean isInLowerBags = true;

		public String info_set;
		public String info_reference;
		public String info_details;
		public IngredientStack replicationCost;

		public IShaderEffectFunction effectFunction;
		private static final IShaderEffectFunction DEFAULT_EFFECT = (world, shader, item, shaderType, pos, dir, scale) -> {
		};

		public ShaderRegistryEntry(String name, EnumRarity rarity, List<ShaderCase> cases)
		{
			this.name = name;
			this.rarity = rarity;
			this.weight = rarityWeightMap.get(rarity);
			for(ShaderCase sCase : cases)
				this.cases.put(sCase.getShaderType(), sCase);
		}

		public ShaderRegistryEntry(String name, EnumRarity rarity, ShaderCase... cases)
		{
			this(name, rarity, Arrays.asList(cases));
		}

		public ShaderRegistryEntry addCase(String type, ShaderCase sCase)
		{
			this.cases.put(type, sCase);
			return this;
		}

		public ShaderRegistryEntry addCase(ShaderCase sCase)
		{
			return this.addCase(sCase.getShaderType(), sCase);
		}

		public ShaderCase getCase(String type)
		{
			return this.cases.get(type);
		}

		public List<ShaderCase> getCases()
		{
			return new ArrayList(this.cases.values());
		}

		public String getName()
		{
			return this.name;
		}

		public EnumRarity getRarity()
		{
			return this.rarity;
		}

		public ShaderRegistryEntry setWeight(int weight)
		{
			this.weight = weight;
			return this;
		}

		public int getWeight()
		{
			return this.weight;
		}

		public ShaderRegistryEntry setCrateLoot(boolean b)
		{
			this.isCrateLoot = b;
			return this;
		}

		public boolean getIsCrateLoot()
		{
			return this.isCrateLoot;
		}

		public ShaderRegistryEntry setBagLoot(boolean b)
		{
			this.isBagLoot = b;
			return this;
		}

		public boolean getIsBagLoot()
		{
			return this.isBagLoot;
		}

		public ShaderRegistryEntry setInLowerBags(boolean b)
		{
			this.isInLowerBags = b;
			return this;
		}

		public boolean getIsInLowerBags()
		{
			return this.isInLowerBags;
		}

		public ShaderRegistryEntry setInfo(@Nullable String set, @Nullable String reference, @Nullable String details)
		{
			this.info_set = set;
			this.info_reference = reference;
			this.info_details = details;
			return this;
		}

		public ShaderRegistryEntry setReplicationCost(@Nonnull IngredientStack replicationCost)
		{
			this.replicationCost = replicationCost;
			return this;
		}

		public ShaderRegistryEntry setEffectFunction(@Nonnull IShaderEffectFunction effectFunction)
		{
			this.effectFunction = effectFunction;
			return this;
		}

		@Nonnull
		public IShaderEffectFunction getEffectFunction()
		{
			if(effectFunction!=null)
				return effectFunction;
			return DEFAULT_EFFECT;
		}
	}
}