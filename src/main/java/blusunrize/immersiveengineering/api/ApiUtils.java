/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.TreeMap;

import static blusunrize.immersiveengineering.api.IETags.getIngot;
import static blusunrize.immersiveengineering.api.utils.ItemUtils.copyStackWithAmount;

public class ApiUtils
{
	public static JsonElement jsonSerializeFluidStack(FluidStack fluidStack)
	{
		if(fluidStack==null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("fluid", fluidStack.getFluid().getRegistryName().toString());
		jsonObject.addProperty("amount", fluidStack.getAmount());
		if(fluidStack.hasTag())
			jsonObject.addProperty("tag", fluidStack.getTag().toString());
		return jsonObject;
	}

	public static FluidStack jsonDeserializeFluidStack(JsonObject jsonObject)
	{
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(JSONUtils.getString(jsonObject, "fluid")));
		int amount = JSONUtils.getInt(jsonObject, "amount");
		FluidStack fluidStack = new FluidStack(fluid, amount);
		if(JSONUtils.hasField(jsonObject, "tag"))
			fluidStack.setTag(JsonUtils.readNBT(jsonObject, "tag"));
		return fluidStack;
	}

	public static int getComponentIngotWorth(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String key = TagUtils.getMatchingPrefix(stack, keys);
		if(key!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(key);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return (int)val;
			}
		}
		return 0;
	}

	public static ItemStack breakStackIntoIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String[] type = TagUtils.getMatchingPrefixAndRemaining(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return copyStackWithAmount(IEApi.getPreferredTagStack(getIngot(type[1])), (int)val);
			}
		}
		return ItemStack.EMPTY;
	}

	public static Pair<ItemStack, Double> breakStackIntoPreciseIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String[] type = TagUtils.getMatchingPrefixAndRemaining(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return new ImmutablePair<>(IEApi.getPreferredTagStack(getIngot(type[1])), val);
			}
		}
		return null;
	}

	public static double getDim(Vector3d vec, int dim)
	{
		return dim==0?vec.x: (dim==1?vec.y: vec.z);
	}

	public static <T extends Comparable<T>> Map<T, Integer> sortMap(Map<T, Integer> map, boolean inverse)
	{
		TreeMap<T, Integer> sortedMap = new TreeMap<>(new ValueComparator<T>(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}

	//TODO move to EventHandler(?), since IGeneeralMultiblock isn't API
	public static <T extends TileEntity & IGeneralMultiblock> void checkForNeedlessTicking(T te)
	{
		if(!te.getWorld().isRemote&&te.isDummy())
			EventHandler.REMOVE_FROM_TICKING.add(te);
	}

	//Based on net.minecraft.entity.EntityLivingBase.knockBack
	public static void knockbackNoSource(LivingEntity entity, double strength, double xRatio, double zRatio)
	{
		entity.isAirBorne = true;
		Vector3d motionOld = entity.getMotion();
		Vector3d toAdd = (new Vector3d(xRatio, 0.0D, zRatio)).normalize().scale(strength);
		entity.setMotion(
				motionOld.x/2.0D-toAdd.x,
				entity.func_233570_aj_()?Math.min(0.4D, motionOld.y/2.0D+strength): motionOld.y,
				motionOld.z/2.0D-toAdd.z);
	}
	public static void addFutureServerTask(World world, Runnable task, boolean forceFuture)
	{
		LogicalSide side = world.isRemote?LogicalSide.CLIENT: LogicalSide.SERVER;
		//TODO this sometimes causes NPEs?
		ThreadTaskExecutor<? super TickDelayedTask> tmp = LogicalSidedProvider.WORKQUEUE.get(side);
		if(forceFuture)
		{
			int tick;
			if(world.isRemote)
				tick = 0;
			else
				tick = ((MinecraftServer)tmp).getTickCounter();
			tmp.enqueue(new TickDelayedTask(tick, task));
		}
		else
			tmp.deferTask(task);
	}

	public static void addFutureServerTask(World world, Runnable task)
	{
		addFutureServerTask(world, task, false);
	}

	public static class ValueComparator<T extends Comparable<T>> implements java.util.Comparator<T>
	{
		Map<T, Integer> base;
		boolean inverse;

		public ValueComparator(Map<T, Integer> base, boolean inverse)
		{
			this.base = base;
			this.inverse = inverse;
		}

		@Override
		public int compare(T s0, T s1)//Cant return equal to keys separate
		{
			int v0 = base.get(s0);
			int v1 = base.get(s1);
			int ret;
			if(v0 > v1)
				ret = -1;
			else if(v0 < v1)
				ret = 1;
			else
				ret = s0.compareTo(s1);
			return ret*(inverse?-1: 1);
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof ValueComparator))
				return false;
			ValueComparator other = (ValueComparator)obj;
			return other.base==base&&other.inverse==inverse;
		}
	}
}
