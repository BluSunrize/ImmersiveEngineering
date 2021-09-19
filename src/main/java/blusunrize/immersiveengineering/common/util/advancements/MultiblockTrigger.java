/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.advancements;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

/**
 * @author BluSunrize - 04.07.2017
 */
public class MultiblockTrigger implements CriterionTrigger<MultiblockTrigger.Instance>
{
	private static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "multiblock_formed");
	private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
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
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
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
	public void removePlayerListeners(PlayerAdvancements playerAdvancements)
	{
		this.listeners.remove(playerAdvancements);
	}

	@Override
	public Instance createInstance(JsonObject json, DeserializationContext context)
	{
		EntityPredicate.Composite and = EntityPredicate.Composite.fromJson(json, "player", context);
		return new MultiblockTrigger.Instance(
				new ResourceLocation(GsonHelper.getAsString(json, "multiblock")),
				ItemPredicate.fromJson(json.get("item")),
				and
		);
	}

	public void trigger(ServerPlayer player, IMultiblock multiblock, ItemStack hammer)
	{
		MultiblockTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());
		if(listeners!=null)
			listeners.trigger(multiblock, hammer);
	}

	public static Instance create(ResourceLocation multiblock, ItemPredicate hammer)
	{
		return new Instance(multiblock, hammer, Composite.ANY);
	}

	public static class Instance extends AbstractCriterionTriggerInstance
	{
		private final ResourceLocation multiblock;
		private final ItemPredicate hammer;

		public Instance(ResourceLocation multiblock, ItemPredicate hammer, Composite and)
		{
			super(MultiblockTrigger.ID, and);
			this.multiblock = multiblock;
			this.hammer = hammer;
		}

		public boolean test(IMultiblock multiblock, ItemStack hammer)
		{
			return this.multiblock.equals(multiblock.getUniqueName())&&this.hammer.matches(hammer);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext conditions)
		{
			JsonObject jsonobject = super.serializeToJson(conditions);
			jsonobject.addProperty("multiblock", this.multiblock.toString());
			jsonobject.add("item", this.hammer.serializeToJson());
			return jsonobject;
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

		public void add(CriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
		{
			this.listeners.add(listener);
		}

		public void remove(CriterionTrigger.Listener<MultiblockTrigger.Instance> listener)
		{
			this.listeners.remove(listener);
		}

		public void trigger(IMultiblock multiblock, ItemStack hammer)
		{
			List<Listener<Instance>> list = null;
			for(CriterionTrigger.Listener<MultiblockTrigger.Instance> listener : this.listeners)
				if(listener.getTriggerInstance().test(multiblock, hammer))
				{
					if(list==null)
						list = Lists.newArrayList();
					list.add(listener);
				}

			if(list!=null)
				for(CriterionTrigger.Listener<MultiblockTrigger.Instance> listener1 : list)
					listener1.run(this.playerAdvancements);
		}
	}
}
