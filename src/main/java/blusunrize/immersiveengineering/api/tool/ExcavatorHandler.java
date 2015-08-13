package blusunrize.immersiveengineering.api.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEApi;

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
	public static HashMap<DimensionChunkCoords, Integer> mineralDepletion = new HashMap<DimensionChunkCoords, Integer>();
	public static int totalWeight = 0;
	public static int mineralVeinCapacity = 0;
	
	public static MineralMix addMineral(String name, int mineralChance, float failChance, String[] ores, float[] chances)
	{
		assert ores.length == chances.length;
		MineralMix mix = new MineralMix(name, failChance, ores, chances);
		mineralList.put(mix, mineralChance);
		return mix;
	}
	public static void recalculateChances()
	{
		totalWeight = 0;
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
		{
			totalWeight += e.getValue();
			e.getKey().recalculateChances();
		}
	}


	public static MineralMix getRandomMineral(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;

		if(mineralVeinCapacity>=0)
		{
			if(!mineralDepletion.containsKey(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ)))
				mineralDepletion.put(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ), 0);
			int dep = mineralDepletion.get(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ));
			if(dep>mineralVeinCapacity)
				return null;
		}

		long seed = world.getSeed();
		boolean empty = ((seed+(chunkX*chunkX + chunkZ*chunkZ))^seed)%8!=0; //Used to be 1 in 4
		int query = (int) ((seed+((chunkX*chunkX*71862)+(chunkZ*chunkZ*31261)))^seed);
		if(empty)
			return null;

		int weight = Math.abs(query%totalWeight);
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
		{
			weight -= e.getValue();
			if(weight < 0)
				return e.getKey();
		}
		return null;
	}
	public static void depleteMinerals(World world, int chunkX, int chunkZ)
	{
		if(!mineralDepletion.containsKey(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ)))
			mineralDepletion.put(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ), 0);
		int dep = mineralDepletion.get(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ));
		mineralDepletion.put(new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ), dep+1);
	}

	public static class MineralMix
	{
		public String name;
		public float failChance;
		public String[] ores;
		public float[] chances;
		public ItemStack[] oreOutput;
		//		public String[] recalculatedOres;
		public float[] recalculatedChances;
		/**Should an ore given to this mix not be present in the dictionary, it will attempt to draw a replacement from this list*/
		public HashMap<String,String> replacementOres;

		public MineralMix(String name, float failChance, String[] ores, float[] chances)
		{
			this.name = name;
			this.failChance = failChance;
			this.ores = ores;
			this.chances = chances;

			recalculateChances();
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
			float chanceSum = 0;
			ArrayList<ItemStack> existing = new ArrayList();
			ArrayList<Float> reChances = new ArrayList();
			for(int i=0; i<ores.length; i++)
			{
				String ore = ores[i];
				if(replacementOres!=null && OreDictionary.getOres(ore).size()<=0 && replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null && !ore.isEmpty() && OreDictionary.getOres(ore).size()>0)
				{
					ItemStack preferredOre = IEApi.getPreferredOreStack(ore);
					if(preferredOre!=null)
					{
						existing.add(preferredOre);
						reChances.add(chances[i]);
						chanceSum += chances[i];
					}
				}
			}
			//			recalculatedOres = existing.toArray(new String[existing.size()]);
			oreOutput = existing.toArray(new ItemStack[existing.size()]);
			recalculatedChances = new float[reChances.size()];
			for(int i=0; i<reChances.size(); i++)
				recalculatedChances[i] = reChances.get(i)/chanceSum;
			//			int j=0;
			//			for(int i=0; i<ores.length; i++)
			//			{
			//				String ore = ores[i];
			//				if(replacementOres!=null && OreDictionary.getOres(ore).size()<=0 && replacementOres.containsKey(ore))
			//					ores[i] = ore = replacementOres.get(ore);
			//				if(ore!=null && !ore.isEmpty() && OreDictionary.getOres(ore).size()>0)
			//					this.recalculatedChances[j++] = chances[i]/chanceSum;
			//			}
		}

		public ItemStack getRandomOre(Random rand)
		{
			float r = rand.nextFloat();
			//			for(int i=0; i<chances.length; i++)
			//			{
			//				r -= chances[i];
			//				if(r < 0)
			//					return this.ores[i];
			//			}
			//			return "";
			for(int i=0; i<recalculatedChances.length; i++)
			{
				r -= recalculatedChances[i];
				if(r < 0)
					return this.oreOutput[i];
			}
			return null;
		}
	}
}