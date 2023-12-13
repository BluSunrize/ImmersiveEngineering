/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author BluSunrize - 04.07.2017
 */
public class MultiblockAdvancementTrigger implements CriterionTrigger<MultiblockAdvancementTrigger.Instance>
{
	public static final DeferredRegister<CriterionTrigger<?>> REGISTER = DeferredRegister.create(
			BuiltInRegistries.TRIGGER_TYPES, Lib.MODID
	);
	public static Supplier<MultiblockAdvancementTrigger> INSTANCE = REGISTER.register(
			"multiblock_formed", MultiblockAdvancementTrigger::new
	);
	private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener)
	{
		MultiblockAdvancementTrigger.Listeners listeners = this.listeners.get(playerAdvancements);
		if(listeners==null)
		{
			listeners = new MultiblockAdvancementTrigger.Listeners(playerAdvancements);
			this.listeners.put(playerAdvancements, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener)
	{
		MultiblockAdvancementTrigger.Listeners listeners = this.listeners.get(playerAdvancements);

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
	public Codec<Instance> codec()
	{
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, IMultiblock multiblock, ItemStack hammer)
	{
		MultiblockAdvancementTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());
		if(listeners!=null)
			listeners.trigger(multiblock, hammer);
	}

	public static Criterion<?> create(ResourceLocation multiblock, ItemPredicate hammer)
	{
		return INSTANCE.get().createCriterion(new Instance(multiblock, hammer));
	}

	public record Instance(ResourceLocation multiblock, ItemPredicate hammer) implements CriterionTriggerInstance
	{
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				ResourceLocation.CODEC.fieldOf("multiblock").forGetter(Instance::multiblock),
				ItemPredicate.CODEC.fieldOf("hammer").forGetter(Instance::hammer)
		).apply(inst, Instance::new));

		public boolean test(IMultiblock multiblock, ItemStack hammer)
		{
			return this.multiblock.equals(multiblock.getUniqueName())&&this.hammer.matches(hammer);
		}

		@Override
		public void validate(CriterionValidator validator)
		{
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

		public void add(CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener)
		{
			this.listeners.add(listener);
		}

		public void remove(CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener)
		{
			this.listeners.remove(listener);
		}

		public void trigger(IMultiblock multiblock, ItemStack hammer)
		{
			List<Listener<Instance>> list = null;
			for(CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener : this.listeners)
				if(listener.trigger().test(multiblock, hammer))
				{
					if(list==null)
						list = Lists.newArrayList();
					list.add(listener);
				}

			if(list!=null)
				for(CriterionTrigger.Listener<MultiblockAdvancementTrigger.Instance> listener1 : list)
					listener1.run(this.playerAdvancements);
		}
	}
}
