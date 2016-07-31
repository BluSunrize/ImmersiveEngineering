package blusunrize.immersiveengineering.api.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author BluSunrize - 03.06.2015
 *
 * The Handler for the Excavator. Chunk->Ore calculation is done here, as is registration
 */
public class ExcavatorHandler
{
	/**
	 * A HashMap of MineralMixes and their rarity (Integer out of 100)
	 */
	public static LinkedHashMap<MineralMix, Integer> mineralList = new LinkedHashMap<MineralMix, Integer>();
	public static HashMap<DimensionChunkCoords, MineralWorldInfo> mineralCache = new HashMap<DimensionChunkCoords, MineralWorldInfo>();
	private static HashMap<Integer,Integer> dimensionBasedTotalWeight = new HashMap<Integer,Integer>();
	public static int mineralVeinCapacity = 0;
	public static double mineralChance = 0;
	public static int[] defaultDimensionBlacklist = new int[0];

	public static MineralMix addMineral(String name, int mineralWeight, float failChance, String[] ores, float[] chances)
	{
		assert ores.length == chances.length;
		MineralMix mix = new MineralMix(name, failChance, ores, chances);
		mineralList.put(mix, mineralWeight);
		return mix;
	}
	public static void recalculateChances(boolean mutePackets)
	{
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
			e.getKey().recalculateChances();
		dimensionBasedTotalWeight.clear();
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			HashMap<MineralMix,Integer> packetMap = new HashMap<MineralMix,Integer>(); 
			for(Map.Entry<MineralMix,Integer> e: ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null && e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			if(!mutePackets)
				ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync(packetMap));
		}
	}
	public static int getDimensionTotalWeight(int dim)
	{
		if(dimensionBasedTotalWeight.containsKey(dim))
			return dimensionBasedTotalWeight.get(dim);
		int totalWeight = 0;
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
		{
			e.getKey().recalculateChances();
			if(e.getKey().isValid() && e.getKey().validDimension(dim))
				totalWeight += e.getValue();
		}
		dimensionBasedTotalWeight.put(dim,totalWeight);
		return totalWeight;
	}

	public static MineralMix getRandomMineral(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo info = getMineralWorldInfo(world,chunkX,chunkZ);
		if(info==null || (info.mineral==null && info.mineralOverride==null))
			return null;

		if(mineralVeinCapacity>=0 && info.depletion>mineralVeinCapacity)
			return null;

		return info.mineralOverride!=null?info.mineralOverride:info.mineral;
	}
	public static MineralWorldInfo getMineralWorldInfo(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;

		int dim = world.provider.getDimension();
		DimensionChunkCoords coords = new DimensionChunkCoords(dim, chunkX,chunkZ);
		MineralWorldInfo worldInfo = mineralCache.get(coords);
		if(worldInfo==null)
		{
			MineralMix mix = null;
			Random r = world.getChunkFromChunkCoords(chunkX, chunkZ).getRandomWithSeed(940610);
			double dd = r.nextDouble();
			boolean empty = dd>mineralChance;
			int query = r.nextInt(); 
			if(!empty)
			{
				int weight = Math.abs(query%getDimensionTotalWeight(dim));
				for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
					if(e.getKey().isValid()&&e.getKey().validDimension(dim))
					{
						weight -= e.getValue();
						if(weight < 0)
						{
							mix = e.getKey();
							break;
						}
					}
			}
			worldInfo = new MineralWorldInfo();
			worldInfo.mineral = mix;
			mineralCache.put(coords, worldInfo);
		}
		return worldInfo;
	}
	public static void depleteMinerals(World world, int chunkX, int chunkZ)
	{
		MineralWorldInfo info = getMineralWorldInfo(world,chunkX,chunkZ);
		info.depletion++;
		IESaveData.setDirty(world.provider.getDimension());
	}

	public static class MineralMix
	{
		public String name;
		public float failChance;
		public String[] ores;
		public float[] chances;
		public ItemStack[] oreOutput;
		public float[] recalculatedChances;
		boolean isValid = false;
		/**Should an ore given to this mix not be present in the dictionary, it will attempt to draw a replacement from this list*/
		public HashMap<String,String> replacementOres;
		public int[] dimensionWhitelist = new int[0];
		public int[] dimensionBlacklist = new int[0];

		public MineralMix(String name, float failChance, String[] ores, float[] chances)
		{
			this.name = name;
			this.failChance = failChance;
			this.ores = ores;
			this.chances = chances;
			this.dimensionBlacklist = defaultDimensionBlacklist.clone();
		}
		public MineralMix addReplacement(String original, String replacement)
		{
			if(replacementOres==null)
				replacementOres = new HashMap();
			replacementOres.put(original, replacement);
			return this;
		}

		public void recalculateChances()
		{
			double chanceSum = 0;
			ArrayList<ItemStack> existing = new ArrayList();
			ArrayList<Double> reChances = new ArrayList();
			for(int i=0; i<ores.length; i++)
			{
				String ore = ores[i];
				if(replacementOres!=null && !ApiUtils.isExistingOreName(ore) && replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null && !ore.isEmpty() && ApiUtils.isExistingOreName(ore))
				{
					ItemStack preferredOre = IEApi.getPreferredOreStack(ore);
					if(preferredOre!=null)
					{
						existing.add(preferredOre);
						reChances.add((double)chances[i]);
						chanceSum += chances[i];
					}
				}
			}
			isValid = existing.size()>0;
			oreOutput = existing.toArray(new ItemStack[existing.size()]);
			recalculatedChances = new float[reChances.size()];
			for(int i=0; i<reChances.size(); i++)
				recalculatedChances[i] = (float)(reChances.get(i)/chanceSum);
		}

		public ItemStack getRandomOre(Random rand)
		{
			float r = rand.nextFloat();
			for(int i=0; i<recalculatedChances.length; i++)
			{
				r -= recalculatedChances[i];
				if(r < 0)
					return this.oreOutput[i];
			}
			return null;
		}

		public boolean isValid()
		{
			return isValid;
		}
		public boolean validDimension(int dim)
		{
			if(dimensionWhitelist!=null&&dimensionWhitelist.length>0)
			{
				for(int white : dimensionWhitelist)
					if(dim==white)
						return true;
				return false;
			}
			else if(dimensionBlacklist!=null&&dimensionBlacklist.length>0)
			{
				for(int black : dimensionBlacklist)
					if(dim==black)
						return false;
				return true;
			}
			return true;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("name", this.name);
			tag.setFloat("failChance", this.failChance);
			NBTTagList tagList = new NBTTagList();
			for(String ore : this.ores)
				tagList.appendTag(new NBTTagString(ore));
			tag.setTag("ores", tagList);

			tagList = new NBTTagList();
			for(float chance : this.chances)
				tagList.appendTag(new NBTTagFloat(chance));
			tag.setTag("chances", tagList);

			tagList = new NBTTagList();
			if(this.oreOutput!=null)
				for(ItemStack output : this.oreOutput)
					tagList.appendTag(output.writeToNBT(new NBTTagCompound()));
			tag.setTag("oreOutput", tagList);

			tagList = new NBTTagList();
			for(float chance : this.recalculatedChances)
				tagList.appendTag(new NBTTagFloat(chance));
			tag.setTag("recalculatedChances", tagList);
			tag.setBoolean("isValid", isValid);
			tag.setIntArray("dimensionWhitelist", dimensionWhitelist);
			tag.setIntArray("dimensionBlacklist", dimensionBlacklist);
			return tag;
		}

		public static MineralMix readFromNBT(NBTTagCompound tag)
		{
			String name = tag.getString("name");
			float failChance = tag.getFloat("failChance");

			NBTTagList tagList = tag.getTagList("ores", 8);
			String[] ores = new String[tagList.tagCount()];
			for(int i=0; i<ores.length; i++)
				ores[i] = tagList.getStringTagAt(i);

			tagList = tag.getTagList("chances", 5);
			float[] chances = new float[tagList.tagCount()];
			for(int i=0; i<chances.length; i++)
				chances[i] = tagList.getFloatAt(i);

			tagList = tag.getTagList("oreOutput", 10);
			ItemStack[] oreOutput = new ItemStack[tagList.tagCount()];
			for(int i=0; i<oreOutput.length; i++)
				oreOutput[i] = ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i));

			tagList = tag.getTagList("recalculatedChances", 5);
			float[] recalculatedChances = new float[tagList.tagCount()];
			for(int i=0; i<recalculatedChances.length; i++)
				recalculatedChances[i] = tagList.getFloatAt(i);

			boolean isValid = tag.getBoolean("isValid");
			MineralMix mix = new MineralMix(name, failChance, ores, chances);
			mix.oreOutput = oreOutput;
			mix.recalculatedChances = recalculatedChances;
			mix.isValid = isValid;
			mix.dimensionWhitelist = tag.getIntArray("dimensionWhitelist");
			mix.dimensionBlacklist = tag.getIntArray("dimensionBlacklist");
			return mix;
		}
	}

	public static class MineralWorldInfo
	{
		public MineralMix mineral;
		public MineralMix mineralOverride;
		public int depletion;

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if(mineral!=null)
				tag.setString("mineral", mineral.name);
			if(mineralOverride!=null)
				tag.setString("mineralOverride", mineralOverride.name);
			tag.setInteger("depletion", depletion);
			return tag;
		}
		public static MineralWorldInfo readFromNBT(NBTTagCompound tag)
		{
			MineralWorldInfo info = new MineralWorldInfo();
			if(tag.hasKey("mineral"))
			{
				String s = tag.getString("mineral");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineral = mineral;
			}
			if(tag.hasKey("mineralOverride"))
			{
				String s = tag.getString("mineralOverride");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineralOverride = mineral;
			}
			info.depletion = tag.getInteger("depletion");
			return info;
		}
	}
}