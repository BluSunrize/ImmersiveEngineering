package blusunrize.immersiveengineering.api.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualPages;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	public static int totalWeight = 0;
	public static int mineralVeinCapacity = 0;

	public static MineralMix addMineral(String name, int mineralWeight, float failChance, String[] ores, float[] chances)
	{
		assert ores.length == chances.length;
		MineralMix mix = new MineralMix(name, failChance, ores, chances);
		mineralList.put(mix, mineralWeight);
		return mix;
	}
	public static void recalculateChances()
	{
		totalWeight = 0;
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
		{
			e.getKey().recalculateChances();
			if(e.getKey().isValid())
				totalWeight += e.getValue();
		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
			ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync());
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

		DimensionChunkCoords coords = new DimensionChunkCoords(world.provider.dimensionId, chunkX,chunkZ);
		MineralWorldInfo worldInfo = mineralCache.get(coords);
		if(worldInfo==null)
		{
			MineralMix mix = null;
			Random r = world.getChunkFromChunkCoords(chunkX, chunkZ).getRandomWithSeed(940610);
			double dd = r.nextDouble();
			boolean empty = dd>.125;
			int query = r.nextInt(); 
			if(!empty)
			{
				int weight = Math.abs(query%totalWeight);
				for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
					if(e.getKey().isValid())
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
		IESaveData.setDirty(world.provider.dimensionId);
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

		public MineralMix(String name, float failChance, String[] ores, float[] chances)
		{
			this.name = name;
			this.failChance = failChance;
			this.ores = ores;
			this.chances = chances;
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
				if(replacementOres!=null && OreDictionary.getOres(ore).size()<=0 && replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null && !ore.isEmpty() && OreDictionary.getOres(ore).size()>0)
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
			for(ItemStack output : this.oreOutput)
				tagList.appendTag(output.writeToNBT(new NBTTagCompound()));
			tag.setTag("oreOutput", tagList);

			tagList = new NBTTagList();
			for(float chance : this.recalculatedChances)
				tagList.appendTag(new NBTTagFloat(chance));
			tag.setTag("recalculatedChances", tagList);
			tag.setBoolean("isValid", isValid);

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
				chances[i] = tagList.func_150308_e(i);

			tagList = tag.getTagList("oreOutput", 10);
			ItemStack[] oreOutput = new ItemStack[tagList.tagCount()];
			for(int i=0; i<oreOutput.length; i++)
				oreOutput[i] = ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i));

			tagList = tag.getTagList("recalculatedChances", 5);
			float[] recalculatedChances = new float[tagList.tagCount()];
			for(int i=0; i<recalculatedChances.length; i++)
				recalculatedChances[i] = tagList.func_150308_e(i);

			boolean isValid = tag.getBoolean("isValid");
			MineralMix mix = new MineralMix(name, failChance, ores, chances);
			mix.oreOutput = oreOutput;
			mix.recalculatedChances = recalculatedChances;
			mix.isValid = isValid;
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


	@SideOnly(Side.CLIENT)
	static ManualEntry mineralEntry;
	@SideOnly(Side.CLIENT)
	public static void handleMineralManual()
	{
		if(ManualHelper.getManual()!=null)
		{
			ArrayList<IManualPage> pages = new ArrayList();
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals0"));
			pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "minerals1", new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_sampleDrill)));
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals2"));
			String[][][] multiTables = formatToTable_ExcavatorMinerals();
			for(String[][] minTable : multiTables)
				pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
			if(mineralEntry!=null)
				mineralEntry.setPages(pages.toArray(new IManualPage[pages.size()]));
			else
			{
				ManualHelper.addEntry("minerals", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
				mineralEntry = ManualHelper.getManual().getEntry("minerals");
			}
		}
	}	
	@SideOnly(Side.CLIENT)
	static String[][][] formatToTable_ExcavatorMinerals()
	{
		ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);
		String[][][] multiTables = new String[1][ExcavatorHandler.mineralList.size()][2];
		int curTable = 0;
		int totalLines = 0;
		for(int i=0; i<minerals.length; i++)
			if(minerals[i].isValid())
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				if(StatCollector.translateToLocal(name)==name)
					name = minerals[i].name;
				multiTables[curTable][i][0] = name;
				multiTables[curTable][i][1] = "";
				for(int j=0; j<minerals[i].oreOutput.length; j++)
					if(minerals[i].oreOutput[j]!=null)
					{
						multiTables[curTable][i][1] += minerals[i].oreOutput[j].getDisplayName()+" "+( Utils.formatDouble(minerals[i].recalculatedChances[j]*100,"#.00")+"%" )+(j<minerals[i].oreOutput.length-1?"\n":"");
						totalLines++;
					}
				if(i<minerals.length-1 && totalLines+minerals[i+1].oreOutput.length>=13)
				{
					String[][][] newMultiTables = new String[multiTables.length+1][ExcavatorHandler.mineralList.size()][2];
					System.arraycopy(multiTables,0, newMultiTables,0, multiTables.length);
					multiTables = newMultiTables;
					totalLines = 0;
					curTable++;
				}
			}
		return multiTables;
	}
}