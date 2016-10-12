package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class ShaderRegistry
{
	/**A list of shader names */
	public static ArrayList<String> shaderList = new ArrayList<String>();
	/**A map of shader name to ShaderRegistryEntry, which contains ShaderCases, rarity, weight and loot specifics */
	public static HashMap<String,ShaderRegistryEntry> shaderRegistry = new HashMap<String, ShaderRegistryEntry>();

	/**A list of shader names that can generate in chests/crates. Names are added multiple times depending on their weight */
	public static ArrayList<String> chestLootShaders = new ArrayList<String>();

	/**A map of EnumRarities to weight for grab bag distribution */
	public static HashMap<EnumRarity, Integer> rarityWeightMap = new HashMap<EnumRarity, Integer>();
	static{
		rarityWeightMap.put(EnumRarity.COMMON, 9);
		rarityWeightMap.put(EnumRarity.UNCOMMON, 7);
		rarityWeightMap.put(EnumRarity.RARE, 5);
		rarityWeightMap.put(EnumRarity.EPIC, 3);
	}
	/**A list of EnumRarities sorted by their weight*/
	public static ArrayList<EnumRarity> sortedRarityMap = new ArrayList<EnumRarity>();


	/**A map of player names to received shaders. Saved with worlddata. Designed to prioritize shaders the player has not yet received */
	public static ArrayListMultimap<String, String> receivedShaders = ArrayListMultimap.create();
	/**The map of EnumRarities to the total weight of all shaders of that rarity or rarer*/
	public static HashMap<EnumRarity,Integer> totalWeight = new HashMap<EnumRarity,Integer>();
	/**The total weight in relation to the player. This takes into account shaders the player has gotten, which then result in less weight*/
	public static HashMap<String, HashMap<EnumRarity,Integer>> playerTotalWeight = new HashMap<String, HashMap<EnumRarity,Integer>>();

	public static ShaderCase getShader(String name, String shaderType)
	{
		if(shaderRegistry.containsKey(name))
			return shaderRegistry.get(name).getCase(shaderType);
		return null;
	}

	public static ShaderRegistryEntry registerShader(String name, String overlayType, EnumRarity rarity, int[] colourPrimary, int[] colourSecondary, int[] colourBackground, int[] colourBlade, String additionalTexture, boolean loot, boolean bags)
	{
		registerShader_Revolver(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		registerShader_Chemthrower(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, true,false, additionalTexture);
		registerShader_Drill(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture);
		registerShader_Railgun(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture);
		registerShader_Minecart(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, additionalTexture);
		//registerShader_Balloon(name, overlayType, rarity, colourPrimary, colourSecondary, additionalTexture);
		return shaderRegistry.get(name).setCrateLoot(loot).setBagLoot(bags);
	}

	public static ShaderCaseRevolver registerShader_Revolver(String name, String overlayType, EnumRarity rarity, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseRevolver shader = new ShaderCaseRevolver(overlayType, colourGrip, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);
		return shader;
	}
	public static ShaderCaseChemthrower registerShader_Chemthrower(String name, String overlayType, EnumRarity rarity, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, boolean cageOnBase, boolean tanksUncoloured, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseChemthrower shader = new ShaderCaseChemthrower(overlayType, colourGrip, colourPrimary, colourSecondary, cageOnBase, tanksUncoloured, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);

		return shader;
	}
	public static ShaderCaseDrill registerShader_Drill(String name, String overlayType, EnumRarity rarity, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseDrill shader = new ShaderCaseDrill(overlayType, colourGrip, colourPrimary, colourSecondary, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);

		return shader;
	}
	public static ShaderCaseRailgun registerShader_Railgun(String name, String overlayType, EnumRarity rarity, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseRailgun shader = new ShaderCaseRailgun(overlayType, colourGrip, colourPrimary, colourSecondary, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);

		return shader;
	}
	/**@param colourUnderlying is never used but is needed to colour the shader item*/
	public static ShaderCaseMinecart registerShader_Minecart(String name, String overlayType, EnumRarity rarity, int[] colourUnderlying, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseMinecart shader = new ShaderCaseMinecart(overlayType, colourUnderlying, colourPrimary, colourSecondary, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);

		return shader;
	}
	/*
	 * Balloon Shaders are disabled for now, it'S too much work to get it running with OBJ models
	public static ShaderCaseBalloon registerShader_Balloon(String name, String overlayType, EnumRarity rarity, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseBalloon shader = new ShaderCaseBalloon(overlayType, colourPrimary, colourSecondary, additionalTexture);
		if(!shaderRegistry.containsKey(name))
			shaderRegistry.put(name, new ShaderRegistryEntry(name, rarity, shader));
		else
			shaderRegistry.get(name).addCase(shader);

		return shader;
	}
	*/
	public static ManualEntry manualEntry;
	public static Item itemShader;
	public static Item itemShaderBag;
	public static void compileWeight()
	{
		totalWeight.clear();
		chestLootShaders.clear();
		for(ShaderRegistryEntry entry : shaderRegistry.values())
		{
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				for(Map.Entry<EnumRarity,Integer> weightedRarity : rarityWeightMap.entrySet())
					if(entry.getIsInLowerBags()?(weightedRarity.getValue()>=entryRarityWeight):(weightedRarity.getValue()==entryRarityWeight))
					{
						int i = totalWeight.containsKey(weightedRarity.getKey())?totalWeight.get(weightedRarity.getKey()):0;
						totalWeight.put(weightedRarity.getKey(), i+entry.getWeight() );
					}
			}
			if(entry.getIsCrateLoot())
				for(int i=0; i<entry.getWeight(); i++)
					chestLootShaders.add(entry.getName());
		}


		sortedRarityMap.clear();
		sortedRarityMap.addAll(ShaderRegistry.rarityWeightMap.keySet());
		Collections.sort(sortedRarityMap, new Comparator<EnumRarity>(){
			@Override
			public int compare(EnumRarity enum0, EnumRarity enum1)
			{
				return Integer.compare(ShaderRegistry.rarityWeightMap.get(enum0), ShaderRegistry.rarityWeightMap.get(enum1));
			}});

		if(manualEntry!=null)
		{
			ArrayList<PositionedItemStack[]> recipes = new ArrayList();
			ItemStack[] shaderBags = new ItemStack[ShaderRegistry.sortedRarityMap.size()];
			recipes = new ArrayList();
			for(int i=0; i<ShaderRegistry.sortedRarityMap.size(); i++)
			{
				EnumRarity outputRarity = ShaderRegistry.sortedRarityMap.get(i);
				shaderBags[i] = new ItemStack(itemShaderBag);
				shaderBags[i].setTagCompound(new NBTTagCompound());
				shaderBags[i].getTagCompound().setString("rarity", outputRarity.toString());
				ArrayList<EnumRarity> upperRarities = ShaderRegistry.getHigherRarities(outputRarity);
				if(!upperRarities.isEmpty())
				{
					ArrayList<ItemStack> inputList = new ArrayList();
					for(EnumRarity r : upperRarities)
					{
						ItemStack bag = new ItemStack(itemShaderBag);
						bag.setTagCompound(new NBTTagCompound());
						bag.getTagCompound().setString("rarity",  r.toString());
						inputList.add(bag);
					}
					ItemStack s0 = new ItemStack(itemShaderBag,2);
					s0.setTagCompound(new NBTTagCompound());
					s0.getTagCompound().setString("rarity", outputRarity.toString());
					if(!inputList.isEmpty())
						recipes.add(new PositionedItemStack[]{ new PositionedItemStack(inputList, 33, 0), new PositionedItemStack(s0, 69, 0)});
					inputList = new ArrayList();
					for(ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
						if(upperRarities.contains(entry.getRarity()))
						{
							ItemStack shader = new ItemStack(itemShader);
							shader.setTagCompound(new NBTTagCompound());
							shader.getTagCompound().setString("shader_name",  entry.getName());
							inputList.add(shader);
						}
					ItemStack s1 = new ItemStack(itemShaderBag);
					s1.setTagCompound(new NBTTagCompound());
					s1.getTagCompound().setString("rarity", outputRarity.toString());
					if(!inputList.isEmpty())
						recipes.add(new PositionedItemStack[]{ new PositionedItemStack(inputList, 33, 0), new PositionedItemStack(s1, 69, 0)});
				}
			}
			manualEntry.getPages()[1] = new ManualPages.ItemDisplay(ManualHelper.getManual(), "shader1", shaderBags);
			manualEntry.getPages()[2] = new ManualPages.CraftingMulti(ManualHelper.getManual(), "shader2", (Object[])recipes.toArray(new PositionedItemStack[recipes.size()][3]));
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
				for(Map.Entry<EnumRarity,Integer> weightedRarity : rarityWeightMap.entrySet())
					if(entry.getIsInLowerBags()?(weightedRarity.getValue()>=entryRarityWeight):(weightedRarity.getValue()==entryRarityWeight))
					{
						int weight = playerTotalWeight.get(player).containsKey(weightedRarity.getKey())? playerTotalWeight.get(player).get(weightedRarity.getKey()):0;
						int value =  entry.getWeight();
						if(received.contains(entry.getName()))
							value = 1;
						playerTotalWeight.get(player).put(weightedRarity.getKey(), weight+value );
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
		int weight = total<1?total:rand.nextInt(total);
		for(ShaderRegistryEntry entry : shaderRegistry.values())
			if(entry.getIsBagLoot())
			{
				int entryRarityWeight = rarityWeightMap.get(entry.getRarity());
				if(entry.getIsInLowerBags()?(minWeight>=entryRarityWeight):(minWeight==entryRarityWeight))
				{
					int value = entry.getWeight();
					if(receivedShaders.get(player).contains(entry.getName()))
						value = 1;
					weight -=value;
					if(weight<=0)
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
		for(int next=idx+1; next<sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next))>weight)
				return sortedRarityMap.get(next);
		return null;
	}
	public static ArrayList<EnumRarity> getAllLowerRarities(EnumRarity rarity)
	{
		ArrayList<EnumRarity> list = new ArrayList<EnumRarity>();
		int idx = sortedRarityMap.indexOf(rarity);
		int weight = rarityWeightMap.get(rarity);
		for(int next=idx+1; next<sortedRarityMap.size(); next++)
			if(rarityWeightMap.get(sortedRarityMap.get(next))>weight)
				list.add(sortedRarityMap.get(next));
		return list;
	}
	public static ArrayList<EnumRarity> getHigherRarities(EnumRarity rarity)
	{
		ArrayList<EnumRarity> list = new ArrayList<EnumRarity>();
		int idx = sortedRarityMap.indexOf(rarity);
		if(idx<=0)
			return list;
		int next=idx-1;
		int weight = rarityWeightMap.get(rarity);
		int lowerWeight = -1;
		for(; next>=0; next--)
		{
			EnumRarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight<weight && (lowerWeight==-1 || rWeight>=lowerWeight))
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
		if(idx<=0)
			return list;
		int next=idx-1;
		int weight = rarityWeightMap.get(rarity);
		for(; next>=0; next--)
		{
			EnumRarity r = sortedRarityMap.get(next);
			int rWeight = rarityWeightMap.get(r);
			if(rWeight<weight)
				list.add(r);
		}
		return list;
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

		public ShaderRegistryEntry(String name, EnumRarity rarity, List<ShaderCase> cases)
		{
			this.name = name;
			this.rarity = rarity;
			this.weight = rarityWeightMap.get(rarity);
			for(ShaderCase sCase :  cases)
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
	}
}