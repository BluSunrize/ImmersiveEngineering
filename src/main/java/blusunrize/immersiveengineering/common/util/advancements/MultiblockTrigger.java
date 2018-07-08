/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.advancements;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author BluSunrize - 04.07.2017
 */
public class MultiblockTrigger implements ICriterionTrigger<MultiblockTrigger.Instance>
{
	private static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "multiblock_formed");
	private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	public void addListener(PlayerAdvancements playerAdvancements, ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
	{
		MultiblockTrigger.Listeners listeners = this.listeners.get(playerAdvancements);
		if(listeners==null)
		{
			listeners = new MultiblockTrigger.Listeners(playerAdvancements);
			this.listeners.put(playerAdvancements, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(PlayerAdvancements playerAdvancements, ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
	{
		MultiblockTrigger.Listeners listeners = this.listeners.get(playerAdvancements);

		if(listeners!=null)
		{
			listeners.remove(listener);
			if(listeners.isEmpty())
				this.listeners.remove(playerAdvancements);
		}
	}

	@Override
	public void removeAllListeners(PlayerAdvancements playerAdvancements)
	{
		this.listeners.remove(playerAdvancements);
	}

	@Override
	public MultiblockTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
	{
		return new MultiblockTrigger.Instance(JsonUtils.getString(json, "multiblock"), ItemPredicate.deserialize(json.get("item")));
	}

	public void trigger(EntityPlayerMP player, IMultiblock multiblock, ItemStack hammer)
	{
		MultiblockTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());
		if(listeners!=null)
			listeners.trigger(multiblock, hammer);
	}

	public static class Instance extends AbstractCriterionInstance
	{
		private final String multiblock;
		private final ItemPredicate hammer;

		public Instance(String multiblock, ItemPredicate hammer)
		{
			super(MultiblockTrigger.ID);
			this.multiblock = multiblock;
			this.hammer = hammer;
		}

		public boolean test(IMultiblock multiblock, ItemStack hammer)
		{
			return this.multiblock.equals(multiblock.getUniqueName())&&this.hammer.test(hammer);
		}
	}

	static class Listeners
	{
		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements playerAdvancementsIn)
		{
			this.playerAdvancements = playerAdvancementsIn;
		}

		public boolean isEmpty()
		{
			return this.listeners.isEmpty();
		}

		public void add(ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
		{
			this.listeners.add(listener);
		}

		public void remove(ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
		{
			this.listeners.remove(listener);
		}

		public void trigger(IMultiblock multiblock, ItemStack hammer)
		{
			List<Listener<Instance>> list = null;
			for(ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener : this.listeners)
				if(listener.getCriterionInstance().test(multiblock, hammer))
				{
					if(list==null)
						list = Lists.newArrayList();
					list.add(listener);
				}

			if(list!=null)
				for(ICriterionTrigger.Listener<MultiblockTrigger.Instance> listener1 : list)
					listener1.grantCriterion(this.playerAdvancements);
		}
	}
}
