/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.common.IESaveData;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.RegistryObject;

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
	//public static LinkedHashMap<MineralMix, Integer> mineralList = new LinkedHashMap<MineralMix, Integer>();
	public static Map<ResourceLocation, MineralMix> mineralList = new HashMap<>();
	public static HashMap<DimensionChunkCoords, MineralWorldInfo> mineralCache = new HashMap<DimensionChunkCoords, MineralWorldInfo>();
	//private static HashMap<Integer, Set<MineralMix>> dimensionPermittedMinerals = new HashMap<Integer, Set<MineralMix>>();
	public static int mineralVeinCapacity = 0;
	public static double mineralChance = 0;
	//public static Set<DimensionType> defaultDimensionBlacklist = new HashSet<>();
	public static Set<UUID> allowPacketsToPlayer = new HashSet<>();

	public static MineralMix addMineral(String name, int mineralWeight, float failChance, ResourceLocation[] ores, float[] chances)
	{
		//todo: remove
//		assert ores.length==chances.length;
//		MineralMix mix = new MineralMix(name, failChance, ores, chances);
//		mineralList.put(mix, mineralWeight);
//		return mix;
		return null;
	}

	public static void recalculateChances(boolean mutePackets)
	{
		//todo remove entirely??
//		for(Map.Entry<MineralMix, Integer> e : mineralList.entrySet())
//			e.getKey().recalculateChances();
//		dimensionPermittedMinerals.clear();
//		if(EffectiveSide.get()==LogicalSide.SERVER&&!mutePackets)
//		{
//			HashMap<MineralMix, Integer> packetMap = new HashMap<>();
//			for(Map.Entry<MineralMix, Integer> e : ExcavatorHandler.mineralList.entrySet())
//				if(e.getKey()!=null&&e.getValue()!=null)
//					packetMap.put(e.getKey(), e.getValue());
//			for(ServerPlayerEntity p : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
//				if(allowPacketsToPlayer.contains(p.getUniqueID()))
//					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> p), new MessageMineralListSync(packetMap));
//		}
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
					for(MineralMix e : selection.getMinerals())
					{
						weight -= e.weight;
						if(weight < 0)
						{
							mix = e;
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

	public static class MineralMix extends IESerializableRecipe
	{
		public static IRecipeType<MineralMix> TYPE = IRecipeType.register(Lib.MODID+":mineral_mix");
		public static RegistryObject<IERecipeSerializer<MineralMix>> SERIALIZER;

		public final StackWithChance[] outputs;
		public final int weight;
		public final float failChance;
		//boolean isValid = false;
		/**
		 * Should an ore given to this mix not be present in the dictionary, it will attempt to draw a replacement from this list
		 */
		public Map<ResourceLocation, ResourceLocation> replacementOres;
		public final ImmutableSet<DimensionType> dimensions;
//		public Set<DimensionType> dimensionBlacklist;

		public MineralMix(ResourceLocation id, StackWithChance[] outputs, int weight, float failChance, DimensionType[] dimensions)
		{
			super(ItemStack.EMPTY, TYPE, id);
			this.weight = weight;
			this.failChance = failChance;
			this.outputs = outputs;
			this.dimensions = ImmutableSet.copyOf(dimensions);
//			this.dimensionBlacklist = new HashSet<>(defaultDimensionBlacklist);
		}

//		public MineralMix(String name, float failChance, ResourceLocation[] ores, float[] chances)
//		{
//			this.name = name;
//			this.failChance = failChance;
//			Preconditions.checkArgument(ores.length==chances.length);
//			outputs = new ArrayList<>();
//			for(int i = 0; i < ores.length; ++i)
//				outputs.add(new OreOutput(ores[i], chances[i]));
//			this.dimensionBlacklist = new HashSet<>(defaultDimensionBlacklist);
//		}

		@Override
		protected IERecipeSerializer<MineralMix> getIESerializer()
		{
			return SERIALIZER.get();
		}

		@Override
		public ItemStack getRecipeOutput()
		{
			return ItemStack.EMPTY;
		}

//		public MineralMix addReplacement(ResourceLocation original, ResourceLocation replacement)
//		{
//			if(replacementOres==null)
//				replacementOres = new HashMap<>();
//			replacementOres.put(original, replacement);
//			return this;
//		}

		public String getPlainName()
		{
			String path = getId().getPath();
			return path.substring(path.lastIndexOf("/")+1);
		}

		public String getTranslationKey()
		{
			return Lib.DESC_INFO+"mineral."+getPlainName();
		}

		public void recalculateChances()
		{
			/* TODO: remove
			double chanceSum = 0;
			for(StackWithChance output : outputs)
			{
				ResourceLocation ore = output.tag;
				if(replacementOres!=null&&!ApiUtils.isNonemptyItemTag(ore)&&replacementOres.containsKey(ore))
					ore = replacementOres.get(ore);
				if(ore!=null)
				{
					ItemStack preferredOre;
					if(ApiUtils.isNonemptyBlockOrItemTag(ore))
						preferredOre = IEApi.getPreferredTagStack(ore);
					else
						preferredOre = new ItemStack(ForgeRegistries.ITEMS.getValue(ore));

					if(!preferredOre.isEmpty())
					{
						output.stack = preferredOre;
						isValid = true;
						chanceSum += output.baseChance;
					}
					else
						output.stack = ItemStack.EMPTY;
				}
			}
			for(OreOutput output : outputs)
				if(output.stack.isEmpty())
					output.recalculatedChance = -1;
				else
					output.recalculatedChance = output.baseChance/chanceSum;
			 */
		}

		public ItemStack getRandomOre(Random rand)
		{
			float r = rand.nextFloat();
			for(StackWithChance o : outputs)
				if(o.getChance() >= 0)
				{
					r -= o.getChance();
					if(r < 0)
						return o.getStack();
				}
			return ItemStack.EMPTY;
		}

		public boolean isValid()
		{
			return true;//isValid;
		}

		public boolean validDimension(DimensionType dim)
		{
			if(dimensions!=null&&!dimensions.isEmpty())
				return dimensions.contains(dim);
			/* todo remove
			else if(dimensionBlacklist!=null&&!dimensionBlacklist.isEmpty())
				return !dimensionBlacklist.contains(dim);
			 */
			return true;
		}

		public CompoundNBT writeToNBT()
		{
			CompoundNBT tag = new CompoundNBT();
			tag.putString("id", this.id.toString());
			tag.putFloat("failChance", this.failChance);
			ListNBT tagList = new ListNBT();
			for(StackWithChance o : outputs)
				tagList.add(o.writeToNBT());
			tag.put("output", tagList);

			tagList = new ListNBT();
			for(DimensionType d : dimensions)
				tagList.add(new StringNBT(DimensionType.getKey(d).toString()));
			tag.put("dimensions", tagList);
			//tag.putBoolean("isValid", isValid);
			//tag.put("dimensionWhitelist", toNBT(dimensions));
			//tag.put("dimensionBlacklist", toNBT(dimensionBlacklist));
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
			ResourceLocation id = new ResourceLocation(tag.getString("id"));

			ListNBT list = tag.getList("output", NBT.TAG_COMPOUND);
			StackWithChance[] outputs = new StackWithChance[list.size()];
			for(int i = 0; i < list.size(); ++i)
				outputs[i] = StackWithChance.readFromNBT(list.getCompound(i));

			int weight = tag.getInt("weight");
			float failChance = tag.getFloat("failChance");
//			boolean isValid = tag.getBoolean("isValid");
			list = tag.getList("dimensions", NBT.TAG_STRING);
			DimensionType[] dimensions = new DimensionType[list.size()];
			for(int i = 0; i < list.size(); ++i)
				dimensions[i] = DimensionType.byName(new ResourceLocation(list.getString(i)));

			MineralMix mix = new MineralMix(id, outputs, weight, failChance, dimensions);
			//mix.isValid = isValid;
//			mix.dimensions = fromNBT();
			//mix.dimensionBlacklist = fromNBT(tag.getList("dimensionBlacklist", NBT.TAG_STRING));
			return mix;
		}
	}

	/*
		public static class OreOutput
		{
			public final ResourceLocation tag;
			public final double baseChance;
			@Nonnull
			public ItemStack stack = ItemStack.EMPTY;
			public double recalculatedChance;

			public OreOutput(ResourceLocation tag, double baseChance)
			{
				this.tag = tag;
				this.baseChance = baseChance;
			}

			public OreOutput(CompoundNBT nbt)
			{
				this(new ResourceLocation(nbt.getString("tag")), nbt.getDouble("baseChance"));
				stack = ItemStack.read(nbt.getCompound("stack"));
				recalculatedChance = nbt.getDouble("recalculatedChance");
			}

			public CompoundNBT toNBT()
			{
				CompoundNBT ret = new CompoundNBT();
				ret.putString("tag", tag.toString());
				ret.putDouble("baseChance", baseChance);
				CompoundNBT stackData = new CompoundNBT();
				stack.write(stackData);
				ret.put("stack", stackData);
				ret.putDouble("recalculatedChance", recalculatedChance);
				return ret;
			}
		}
	*/
	public static class MineralWorldInfo
	{
		public MineralMix mineral;
		public MineralMix mineralOverride;
		public int depletion;

		public CompoundNBT writeToNBT()
		{
			CompoundNBT tag = new CompoundNBT();
			if(mineral!=null)
				tag.putString("mineral", mineral.getId().toString());
			if(mineralOverride!=null)
				tag.putString("mineralOverride", mineralOverride.getId().toString());
			tag.putInt("depletion", depletion);
			return tag;
		}

		public static MineralWorldInfo readFromNBT(CompoundNBT tag)
		{
			MineralWorldInfo info = new MineralWorldInfo();
			if(tag.contains("mineral"))
			{
				ResourceLocation id = new ResourceLocation(tag.getString("mineral"));
				info.mineral = mineralList.get(id);
			}
			if(tag.contains("mineralOverride"))
			{
				ResourceLocation id = new ResourceLocation(tag.getString("mineralOverride"));
				info.mineralOverride = mineralList.get(id);
			}
			info.depletion = tag.getInt("depletion");
			return info;
		}
	}

	public static class MineralSelection
	{
		private final int totalWeight;
		private final Set<MineralMix> validMinerals;

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
			for(MineralMix e : mineralList.values())
				if(e.isValid()&&e.validDimension(chunkCoords.dimension)&&!surrounding.contains(e))
				{
					validMinerals.add(e);
					weight += e.weight;
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

		public Set<MineralMix> getMinerals()
		{
			return this.validMinerals;
		}
	}

}