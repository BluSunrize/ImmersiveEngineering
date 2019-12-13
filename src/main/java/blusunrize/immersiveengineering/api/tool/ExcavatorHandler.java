/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.network.MessageMineralListSync;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;

/**
 * @author BluSunrize - 03.06.2015
 * <p>
 * The Handler for the Excavator. Chunk->Ore calculation is done here, as is registration
 */
public class ExcavatorHandler
{
	/**
	 * A HashMap of MineralMixes and their rarity (Integer out of 100)
	 */
	public static LinkedHashMap<MineralMix, Integer> mineralList = new LinkedHashMap<MineralMix, Integer>();
	public static HashMap<DimensionChunkCoords, MineralWorldInfo> mineralCache = new HashMap<DimensionChunkCoords, MineralWorldInfo>();
	private static HashMap<Integer, Set<MineralMix>> dimensionPermittedMinerals = new HashMap<Integer, Set<MineralMix>>();
	public static int mineralVeinCapacity = 0;
	public static double mineralChance = 0;
	public static Set<DimensionType> defaultDimensionBlacklist = new HashSet<>();
	public static Set<UUID> allowPacketsToPlayer = new HashSet<>();

	public static MineralMix addMineral(String name, int mineralWeight, float failChance, ResourceLocation[] ores, float[] chances)
	{
		assert ores.length==chances.length;
		MineralMix mix = new MineralMix(name, failChance, ores, chances);
		mineralList.put(mix, mineralWeight);
		return mix;
	}

	public static void recalculateChances(boolean mutePackets)
	{
		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
			e.getKey().recalculateChances();
		dimensionPermittedMinerals.clear();
		if(EffectiveSide.get()==LogicalSide.SERVER&&!mutePackets)
		{
			HashMap<MineralMix, Integer> packetMap = new HashMap<MineralMix, Integer>();
			for(Map.Entry<MineralMix, Integer> e : ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null&&e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			for(ServerPlayerEntity p : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
				if(allowPacketsToPlayer.contains(p.getUniqueID()))
					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> p), new MessageMineralListSync(packetMap));
		}
	}

	public static MineralMix getRandomMineral(World world, int chunkX, int chunkZ)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo info = getMineralWorldInfo(world, chunkX, chunkZ);
		if(info==null||(info.mineral==null&&info.mineralOverride==null))
			return null;

		if(mineralVeinCapacity >= 0&&info.depletion > mineralVeinCapacity)
			return null;

		return info.mineralOverride!=null?info.mineralOverride: info.mineral;
	}

	public static MineralWorldInfo getMineralWorldInfo(World world, int chunkX, int chunkZ)
	{
		return getMineralWorldInfo(world, new DimensionChunkCoords(world.getDimension().getType(), chunkX, chunkZ), false);
	}

	public static MineralWorldInfo getMineralWorldInfo(World world, DimensionChunkCoords chunkCoords, boolean guaranteed)
	{
		if(world.isRemote)
			return null;
		MineralWorldInfo worldInfo = mineralCache.get(chunkCoords);
		if(worldInfo==null)
		{
			MineralMix mix = null;
			Random r = SharedSeedRandom.seedSlimeChunk(chunkCoords.x, chunkCoords.z, world.getSeed(), 940610990913L);
			double dd = r.nextDouble();

			boolean empty = !guaranteed&&dd > mineralChance;
			if(!empty)
			{
				MineralSelection selection = new MineralSelection(world, chunkCoords, 2);
				if(selection.getTotalWeight() > 0)
				{
					int weight = selection.getRandomWeight(r);
					for(Map.Entry<MineralMix, Integer> e : selection.getMinerals())
					{
						weight -= e.getValue();
						if(weight < 0)
						{
							mix = e.getKey();
							break;
						}
					}
				}
			}
			worldInfo = new MineralWorldInfo();
			worldInfo.mineral = mix;
			mineralCache.put(chunkCoords, worldInfo);
			IESaveData.setDirty();
		}
		return worldInfo;
	}

	public static void depleteMinerals(World world, int chunkX, int chunkZ)
	{
		MineralWorldInfo info = getMineralWorldInfo(world, chunkX, chunkZ);
		info.depletion++;
		IESaveData.setDirty();
	}

	public static class MineralMix
	{
		public String name;
		public float failChance;
		public ResourceLocation[] ores;
		public float[] chances;
		public NonNullList<ItemStack> oreOutput;
		public float[] recalculatedChances;
		boolean isValid = false;
		/**
		 * Should an ore given to this mix not be present in the dictionary, it will attempt to draw a replacement from this list
		 */
		public HashMap<ResourceLocation, ResourceLocation> replacementOres;
		public Set<DimensionType> dimensionWhitelist = new HashSet<>();
		public Set<DimensionType> dimensionBlacklist;

		public MineralMix(String name, float failChance, ResourceLocation[] ores, float[] chances)
		{
			this.name = name;
			this.failChance = failChance;
			this.ores = ores;
			this.chances = chances;
			this.dimensionBlacklist = new HashSet<>(defaultDimensionBlacklist);
		}

		public MineralMix addReplacement(ResourceLocation original, ResourceLocation replacement)
		{
			if(replacementOres==null)
				replacementOres = new HashMap<>();
			replacementOres.put(original, replacement);
			return this;
		}

		public void recalculateChances()
		{
			double chanceSum = 0;
			NonNullList<ItemStack> existing = NonNullList.create();
			ArrayList<Double> reChances = new ArrayList<>();
			for(int i = 0; i < ores.length; i++)
			{
				ResourceLocation ore = ores[i];
				if(replacementOres!=null&&!ApiUtils.isNonemptyItemTag(ore)&&replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null&&ApiUtils.isNonemptyBlockOrItemTag(ore))
				{
					ItemStack preferredOre = IEApi.getPreferredTagStack(ore);
					if(!preferredOre.isEmpty())
					{
						existing.add(preferredOre);
						reChances.add((double)chances[i]);
						chanceSum += chances[i];
					}
				}
			}
			isValid = existing.size() > 0;
			oreOutput = existing;
			recalculatedChances = new float[reChances.size()];
			for(int i = 0; i < reChances.size(); i++)
				recalculatedChances[i] = (float)(reChances.get(i)/chanceSum);
		}

		public ItemStack getRandomOre(Random rand)
		{
			float r = rand.nextFloat();
			for(int i = 0; i < recalculatedChances.length; i++)
			{
				r -= recalculatedChances[i];
				if(r < 0)
					return this.oreOutput.get(i);
			}
			return ItemStack.EMPTY;
		}

		public boolean isValid()
		{
			return isValid;
		}

		public boolean validDimension(DimensionType dim)
		{
			if(dimensionWhitelist!=null&&!dimensionWhitelist.isEmpty())
				return dimensionWhitelist.contains(dim);
			else if(dimensionBlacklist!=null&&!dimensionBlacklist.isEmpty())
				return !dimensionBlacklist.contains(dim);
			return true;
		}

		public CompoundNBT writeToNBT()
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putString("name", this.name);
			tag.putFloat("failChance", this.failChance);
			ListNBT tagList = new ListNBT();
			for(ResourceLocation ore : this.ores)
				tagList.add(new StringNBT(ore.toString()));
			tag.put("ores", tagList);

			tagList = new ListNBT();
			for(float chance : this.chances)
				tagList.add(new FloatNBT(chance));
			tag.put("chances", tagList);

			tagList = new ListNBT();
			if(this.oreOutput!=null)
				for(ItemStack output : this.oreOutput)
					tagList.add(output.write(new CompoundNBT()));
			tag.put("oreOutput", tagList);

			tagList = new ListNBT();
			for(float chance : this.recalculatedChances)
				tagList.add(new FloatNBT(chance));
			tag.put("recalculatedChances", tagList);
			tag.putBoolean("isValid", isValid);
			tag.put("dimensionWhitelist", toNBT(dimensionWhitelist));
			tag.put("dimensionBlacklist", toNBT(dimensionBlacklist));
			return tag;
		}

		private static ListNBT toNBT(Set<DimensionType> types)
		{
			ListNBT ret = new ListNBT();
			for(DimensionType t : types)
				ret.add(new StringNBT(DimensionType.getKey(t).toString()));
			return ret;
		}

		private static Set<DimensionType> fromNBT(ListNBT nbt)
		{
			Set<DimensionType> ret = new HashSet<>();
			for(INBT entry : nbt)
				ret.add(DimensionType.byName(new ResourceLocation(entry.getString())));
			return ret;
		}

		public static MineralMix readFromNBT(CompoundNBT tag)
		{
			String name = tag.getString("name");
			float failChance = tag.getFloat("failChance");

			ListNBT tagList = tag.getList("ores", 8);
			ResourceLocation[] ores = new ResourceLocation[tagList.size()];
			for(int i = 0; i < ores.length; i++)
				ores[i] = new ResourceLocation(tagList.getString(i));

			tagList = tag.getList("chances", 5);
			float[] chances = new float[tagList.size()];
			for(int i = 0; i < chances.length; i++)
				chances[i] = tagList.getFloat(i);

			tagList = tag.getList("oreOutput", 10);
			NonNullList<ItemStack> oreOutput = NonNullList.withSize(tagList.size(), ItemStack.EMPTY);
			for(int i = 0; i < oreOutput.size(); i++)
				oreOutput.set(i, ItemStack.read(tagList.getCompound(i)));

			tagList = tag.getList("recalculatedChances", 5);
			float[] recalculatedChances = new float[tagList.size()];
			for(int i = 0; i < recalculatedChances.length; i++)
				recalculatedChances[i] = tagList.getFloat(i);

			boolean isValid = tag.getBoolean("isValid");
			MineralMix mix = new MineralMix(name, failChance, ores, chances);
			mix.oreOutput = oreOutput;
			mix.recalculatedChances = recalculatedChances;
			mix.isValid = isValid;
			mix.dimensionWhitelist = fromNBT(tag.getList("dimensionWhitelist", NBT.TAG_STRING));
			mix.dimensionBlacklist = fromNBT(tag.getList("dimensionBlacklist", NBT.TAG_STRING));
			return mix;
		}
	}

	public static class MineralWorldInfo
	{
		public MineralMix mineral;
		public MineralMix mineralOverride;
		public int depletion;

		public CompoundNBT writeToNBT()
		{
			CompoundNBT tag = new CompoundNBT();
			if(mineral!=null)
				tag.putString("mineral", mineral.name);
			if(mineralOverride!=null)
				tag.putString("mineralOverride", mineralOverride.name);
			tag.putInt("depletion", depletion);
			return tag;
		}

		public static MineralWorldInfo readFromNBT(CompoundNBT tag)
		{
			MineralWorldInfo info = new MineralWorldInfo();
			if(tag.contains("mineral"))
			{
				String s = tag.getString("mineral");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineral = mineral;
			}
			if(tag.contains("mineralOverride"))
			{
				String s = tag.getString("mineralOverride");
				for(MineralMix mineral : mineralList.keySet())
					if(s.equalsIgnoreCase(mineral.name))
						info.mineralOverride = mineral;
			}
			info.depletion = tag.getInt("depletion");
			return info;
		}
	}

	public static class MineralSelection
	{
		private final int totalWeight;
		private final Set<Map.Entry<MineralMix, Integer>> validMinerals;

		public MineralSelection(World world, DimensionChunkCoords chunkCoords, int radius)
		{
			Set<MineralMix> surrounding = new HashSet<>();
			for(int xx = -radius; xx <= radius; xx++)
				for(int zz = -radius; zz <= radius; zz++)
					if(xx!=0||zz!=0)
					{
						DimensionChunkCoords offset = chunkCoords.withOffset(xx, zz);
						MineralWorldInfo worldInfo = mineralCache.get(offset);
						if(worldInfo!=null&&worldInfo.mineral!=null)
							surrounding.add(worldInfo.mineral);
					}

			int weight = 0;
			this.validMinerals = new HashSet<>();
			for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
				if(e.getKey().isValid()&&e.getKey().validDimension(chunkCoords.dimension)&&!surrounding.contains(e.getKey()))
				{
					validMinerals.add(e);
					weight += e.getValue();
				}
			this.totalWeight = weight;
		}

		public int getTotalWeight()
		{
			return this.totalWeight;
		}

		public int getRandomWeight(Random random)
		{
			return Math.abs(random.nextInt()%this.totalWeight);
		}

		public Set<Map.Entry<MineralMix, Integer>> getMinerals()
		{
			return this.validMinerals;
		}
	}

}