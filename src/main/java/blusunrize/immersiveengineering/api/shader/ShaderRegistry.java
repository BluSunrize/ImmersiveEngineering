/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.common.IERecipes;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
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
	public static LinkedHashMap<ResourceLocation, ShaderRegistryEntry> shaderRegistry = new LinkedHashMap<>();

	/**
	 * A list of shader names that can generate in chests/crates. Names are added multiple times depending on their weight
	 */
	public static ArrayList<ResourceLocation> chestLootShaders = new ArrayList<>();

	/**
	 * A map of EnumRarities to weight for grab bag distribution
	 */
	public static HashMap<Rarity, Integer> rarityWeightMap = new HashMap<>();

	static
	{
		rarityWeightMap.put(Rarity.COMMON, 9);
		rarityWeightMap.put(Rarity.UNCOMMON, 7);
		rarityWeightMap.put(Rarity.RARE, 5);
		rarityWeightMap.put(Rarity.EPIC, 3);
		rarityWeightMap.put(Lib.RARITY_Masterwork, 1);
	}

	/**
	 * A list of EnumRarities sorted by their weight
	 */
	public static ArrayList<Rarity> sortedRarityMap = new ArrayList<>();


	/**
	 * A map of player names to received shaders. Saved with worlddata. Designed to prioritize shaders the player has not yet received
	 */
	public static ArrayListMultimap<UUID, ResourceLocation> receivedShaders = ArrayListMultimap.create();
	/**
	 * The map of EnumRarities to the total weight of all shaders of that rarity or rarer
	 */
	public static HashMap<Rarity, Integer> totalWeight = new HashMap<>();
	/**
	 * The total weight in relation to the player. This takes into account shaders the player has gotten, which then result in less weight
	 */
	public static HashMap<UUID, HashMap<Rarity, Integer>> playerTotalWeight = new HashMap<>();
	/**
	 * The deafault cost for replicating a shader. Prices are multiplied with 10-rarity level. Prices can be adjusted for every registry entry
	 */
	public static IngredientStack defaultReplicationCost = new IngredientStack(IERecipes.getDust("silver"));
	/**
	 * A HashMap to set default texture bounds for the additional layers of a shadercase. Saves you the trouble of redfining them for every shader. See {@link ShaderLayer#setTextureBounds(double... bounds)}.
	 */
	public static HashMap<ResourceLocation, double[]> defaultLayerBounds = new HashMap<>();

	public static ShaderCase getShader(ResourceLocation name, ResourceLocation shaderCase)
	{
		if(shaderRegistry.containsKey(name))
			return shaderRegistry.get(name).getCase(shaderCase);
		return null;
	}

	public static ShaderRegistryEntry registerShader(ResourceLocation name, String overlayType, Rarity rarity, int colourPrimary, int colourSecondary, int colourBackground, int colourBlade, String additionalTexture, int colourAdditional, boolean loot, boolean bags)
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

	public static <T extends ShaderCase> T registerShaderCase(ResourceLocation name, T shader, Rarity rarity)
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
	public static ShaderCaseItem registerShader_Item(ResourceLocation name, Rarity rarity, int colour0, int colour1, int colour2)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shader_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shader_1"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shader_2"), colour2));
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
	public static ShaderCaseRevolver registerShader_Revolver(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, int colour2, int colourBlade, String additionalTexture, int colourAddtional)
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
	public static ShaderCaseChemthrower registerShader_Chemthrower(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:item/shaders/chemthrower_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_uncoloured"), 0xffffffff));
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
	public static ShaderCaseDrill registerShader_Drill(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_uncoloured"), 0xffffffff));
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
	public static ShaderCaseRailgun registerShader_Railgun(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, int colour2, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_0"), colour1));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_1_"+overlayType), colour2));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:item/shaders/railgun_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_uncoloured"), 0xffffffff));

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
	public static ShaderCaseShield registerShader_Shield(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/shield_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/shield_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:item/shaders/shield_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/shield_uncoloured"), 0xffffffff));

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
	public static ShaderCaseMinecart registerShader_Minecart(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
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
	public static ShaderCaseBalloon registerShader_Balloon(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/balloon_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/balloon_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:block/shaders/balloon_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/balloon_uncoloured"), 0xffffffff));

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
	public static ShaderCaseBanner registerShader_Banner(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, String additionalTexture, int colourAddtional)
	{
		ArrayList<ShaderLayer> list = new ArrayList<>();
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/banner_0"), colour0));
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/banner_1_"+overlayType), colour1));
		if(additionalTexture!=null)
		{
			ResourceLocation rl = additionalTexture.indexOf(58) >= 0?new ResourceLocation(additionalTexture): new ResourceLocation("immersiveengineering:block/shaders/banner_"+additionalTexture);
			list.add(new ShaderLayer(rl, colourAddtional));
		}
		list.add(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/banner_uncoloured"), 0xffffffff));

		ShaderCaseBanner shader = new ShaderCaseBanner(list);
		return registerShaderCase(name, shader, rarity);
	}

	/**
	 * A map of shader name to ShaderRegistryEntry, which contains ShaderCases, rarity, weight and loot specifics
	 */
	public static Set<IShaderRegistryMethod> shaderRegistrationMethods = new HashSet<>();

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
		T apply(ResourceLocation name, String overlayType, Rarity rarity, int colour0, int colour1, int colour2, int colour3, String additionalTexture, int colourAddtional);
	}

	public static Item itemShader;
	public static Map<Rarity, Item> itemShaderBag;
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
				for(Map.Entry<Rarity, Integer> weightedRarity : rarityWeightMap.entrySet())
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
		sortedRarityMap.sort(Comparator.comparingInt(enum0 -> ShaderRegistry.rarityWeightMap.get(enum0)));

		//TODO manual entry
	}

	public static void recalculatePlayerTotalWeight(UUID player)
	{
		if(!playerTotalWeight.containsKey(player))
			playerTotalWeight.put(player, new HashMap<>());
		else
			playerTotalWeight.get(player).clear();
		List<ResourceLocation> received = receivedShaders.get(player);
		for(ShaderRegistryEntry entry : shaderRegistry.values())
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				for(Map.Entry<Rarity, Integer> weightedRarity : rarityWeightMap.entrySet())
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

	public static ResourceLocation getRandomShader(UUID player, Random rand, Rarity minRarity, boolean addToReceived)
	{
		int total = 0;
		if(!playerTotalWeight.containsKey(player))
			playerTotalWeight.put(player, totalWeight);
		total = playerTotalWeight.get(player).get(minRarity);

		ResourceLocation shader = null;
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

	public static Rarity getLowerRarity(Rarity rarity)
	{
		int idx = sortedRarityMap.indexOf(rarity);
		int weight = rarityWeightMap.get(rarity);
		for(int next = idx+1; next < sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next)) > weight)
				return sortedRarityMap.get(next);
		return null;
	}

	public static ArrayList<Rarity> getAllLowerRarities(Rarity rarity)
	{
		ArrayList<Rarity> list = new ArrayList<>();
		int idx = sortedRarityMap.indexOf(rarity);
		int weight = rarityWeightMap.get(rarity);
		for(int next = idx+1; next < sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next)) > weight)
				list.add(sortedRarityMap.get(next));
		return list;
	}

	public static ArrayList<Rarity> getHigherRarities(Rarity rarity)
	{
		ArrayList<Rarity> list = new ArrayList<>();
		int idx = sortedRarityMap.indexOf(rarity);
		if(idx <= 0)
			return list;
		int next = idx-1;
		int weight = rarityWeightMap.get(rarity);
		int lowerWeight = -1;
		for(; next >= 0; next--)
		{
			Rarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight < weight&&(lowerWeight==-1||rWeight >= lowerWeight))
			{
				list.add(r);
				lowerWeight = rWeight;
			}
		}
		return list;
	}

	public static ArrayList<Rarity> getAllHigherRarities(Rarity rarity)
	{
		ArrayList<Rarity> list = new ArrayList<>();
		int idx = sortedRarityMap.indexOf(rarity);
		if(idx <= 0)
			return list;
		int next = idx-1;
		int weight = rarityWeightMap.get(rarity);
		for(; next >= 0; next--)
		{
			Rarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight < weight)
				list.add(r);
		}
		return list;
	}

	public static Triple<ItemStack, ShaderRegistryEntry, ShaderCase> getStoredShaderAndCase(ItemStack itemStack)
	{
		ShaderWrapper shaderCap = itemStack.getCapability(CapabilityShader.SHADER_CAPABILITY).orElse(null);
		return shaderCap!=null?getStoredShaderAndCase(shaderCap): null;
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
		public ResourceLocation name;
		public HashMap<ResourceLocation, ShaderCase> cases = new HashMap<>();
		public Rarity rarity;
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

		public ShaderRegistryEntry(ResourceLocation name, Rarity rarity, List<ShaderCase> cases)
		{
			this.name = name;
			this.rarity = rarity;
			this.weight = rarityWeightMap.get(rarity);
			for(ShaderCase sCase : cases)
				this.cases.put(sCase.getShaderType(), sCase);
		}

		public ShaderRegistryEntry(ResourceLocation name, Rarity rarity, ShaderCase... cases)
		{
			this(name, rarity, Arrays.asList(cases));
		}

		public ShaderRegistryEntry addCase(ResourceLocation type, ShaderCase sCase)
		{
			this.cases.put(type, sCase);
			return this;
		}

		public ShaderRegistryEntry addCase(ShaderCase sCase)
		{
			return this.addCase(sCase.getShaderType(), sCase);
		}

		public ShaderCase getCase(ResourceLocation type)
		{
			return this.cases.get(type);
		}

		public List<ShaderCase> getCases()
		{
			return new ArrayList(this.cases.values());
		}

		public ResourceLocation getName()
		{
			return this.name;
		}

		public Rarity getRarity()
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