/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.IETags.getIngot;

public class ApiUtils
{
	public static final SetRestrictedField<Consumer<BlockEntity>> disableTicking = SetRestrictedField.common();
	/**
	 * Random instance for general use. The "usual" per-world random instance can have unexpected behavior with
	 * ghostloading (in some cases the seed is set directly), this instance does not have this problem.
	 */
	public static final Random RANDOM = new Random();

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
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(jsonObject, "fluid")));
		int amount = GsonHelper.getAsInt(jsonObject, "amount");
		FluidStack fluidStack = new FluidStack(fluid, amount);
		if(GsonHelper.isValidNode(jsonObject, "tag"))
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
				return ItemUtils.copyStackWithAmount(IEApi.getPreferredTagStack(getIngot(type[1])), (int)val);
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

	public static double getDim(Vec3 vec, int dim)
	{
		return dim==0?vec.x: (dim==1?vec.y: vec.z);
	}

	public static <T extends Comparable<T>> Map<T, Integer> sortMap(Map<T, Integer> map, boolean inverse)
	{
		TreeMap<T, Integer> sortedMap = new TreeMap<>(new ValueComparator<T>(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}

	public static <T extends BlockEntity> void checkForNeedlessTicking(T te, Predicate<T> shouldDisable)
	{
		if(!te.getLevel().isClientSide&&shouldDisable.test(te))
			disableTicking.getValue().accept(te);
	}

	//Based on net.minecraft.entity.EntityLivingBase.knockBack
	public static void knockbackNoSource(LivingEntity entity, double strength, double xRatio, double zRatio)
	{
		entity.hasImpulse = true;
		Vec3 motionOld = entity.getDeltaMovement();
		Vec3 toAdd = (new Vec3(xRatio, 0.0D, zRatio)).normalize().scale(strength);
		entity.setDeltaMovement(
				motionOld.x/2.0D-toAdd.x,
				entity.isOnGround()?Math.min(0.4D, motionOld.y/2.0D+strength): motionOld.y,
				motionOld.z/2.0D-toAdd.z);
	}

	public static void addFutureServerTask(Level world, Runnable task, boolean forceFuture)
	{
		LogicalSide side = world.isClientSide?LogicalSide.CLIENT: LogicalSide.SERVER;
		//TODO this sometimes causes NPEs?
		BlockableEventLoop<? super TickTask> tmp = LogicalSidedProvider.WORKQUEUE.get(side);
		if(forceFuture)
		{
			int tick;
			if(world.isClientSide)
				tick = 0;
			else
				tick = ((MinecraftServer)tmp).getTickCount();
			tmp.tell(new TickTask(tick, task));
		}
		else
			tmp.submitAsync(task);
	}

	public static void addFutureServerTask(Level world, Runnable task)
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
