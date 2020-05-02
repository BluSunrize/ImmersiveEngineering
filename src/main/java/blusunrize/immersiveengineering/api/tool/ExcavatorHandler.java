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
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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
	public static Map<ResourceLocation, MineralMix> mineralList = new HashMap<>();
	public static HashMap<DimensionChunkCoords, MineralWorldInfo> mineralCache = new HashMap<DimensionChunkCoords, MineralWorldInfo>();
	public static int mineralVeinCapacity = 0;
	public static double mineralChance = 0;

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
				MineralSelection selection = new MineralSelection(chunkCoords, 2);
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
		public final ImmutableSet<DimensionType> dimensions;
		public final Block background;

		public MineralMix(ResourceLocation id, StackWithChance[] outputs, int weight, float failChance, DimensionType[] dimensions, Block background)
		{
			super(ItemStack.EMPTY, TYPE, id);
			this.weight = weight;
			this.failChance = failChance;
			this.outputs = outputs;
			this.dimensions = ImmutableSet.copyOf(dimensions);
			this.background = background;
		}

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

		public String getPlainName()
		{
			String path = getId().getPath();
			return path.substring(path.lastIndexOf("/")+1);
		}

		public String getTranslationKey()
		{
			return Lib.DESC_INFO+"mineral."+getPlainName();
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

		public boolean validDimension(DimensionType dim)
		{
			if(dimensions!=null&&!dimensions.isEmpty())
				return dimensions.contains(dim);
			return true;
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

		public MineralSelection(DimensionChunkCoords chunkCoords, int radius)
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
				if(e.validDimension(chunkCoords.dimension)&&!surrounding.contains(e))
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