/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.SorterBlockEntity.FilterConfig;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.SorterMenu;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class SorterScreen extends IEContainerScreen<SorterMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("sorter");

	public static final Map<FilterBit, ButtonTexture> BUTTON_TEXTURE_TRUE = Map.of(
			FilterBit.DAMAGE, new ButtonTexture(ieLoc("sorter/damage")),
			FilterBit.COMPONENTS, new ButtonTexture(ieLoc("sorter/components")),
			FilterBit.TAG, new ButtonTexture(ieLoc("sorter/tags"))
	);
	public static final Map<FilterBit, ButtonTexture> BUTTON_TEXTURE_FALSE = Map.of(
			FilterBit.DAMAGE, new ButtonTexture(ieLoc("sorter/no_damage")),
			FilterBit.COMPONENTS, new ButtonTexture(ieLoc("sorter/no_components")),
			FilterBit.TAG, new ButtonTexture(ieLoc("sorter/no_tags"))
	);

	public SorterScreen(SorterMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 244;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int side = 0; side < 6; side++)
		{
			int x = leftPos+30+(side/2)*58;
			int y = topPos+44+(side%2)*76;
			String s = I18n.get(Lib.DESC_INFO+"blockSide."+Direction.from3DDataValue(side)).substring(0, 1);
			RenderSystem.enableBlend();
			graphics.drawString(ClientUtils.font(), s, x-(ClientUtils.font().width(s)/2), y, 0xaacccccc, true);
		}
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();
		for(Direction side : Direction.values())
			for(final FilterBit bit : FilterBit.values())
			{
				int sideId = side.ordinal();
				int x = leftPos+3+(sideId/2)*58+bit.ordinal()*18;
				int y = topPos+3+(sideId%2)*76;
				final int sideFinal = sideId;
				final GetterAndSetter<FilterConfig> value = menu.filterMasks.get(side);
				GuiButtonBoolean b = new GuiButtonBoolean(
						x, y, 18, 18, Component.empty(), () -> bit.get(value.get()),
						BUTTON_TEXTURE_FALSE.get(bit), BUTTON_TEXTURE_TRUE.get(bit),
						btn -> {
							CompoundTag tag = new CompoundTag();
							tag.put("sideConfigVal", FilterConfig.CODEC.toNBT(bit.toggle(value.get())));
							tag.putInt("sideConfigId", sideFinal);
							sendUpdateToServer(tag);
							fullInit();
						},
						(components, aBoolean) -> {
							String[] split = I18n.get(bit.getTranslationKey()).split("<br>");
							for(int i = 0; i < split.length; i++)
								if(i==0)
									components.add(Component.literal(split[i]));
								else
									components.add(TextUtils.applyFormat(Component.literal(split[i]), ChatFormatting.GRAY));
						}
				);
				this.addRenderableWidget(b);
			}
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int x, int y)
	{
		if(!this.menu.getCarried().isEmpty())
			return;
		if(this.hoveredSlot instanceof IESlot.ItemHandlerGhost ghostSlot&&ghostSlot.hasItem())
		{
			int side = ghostSlot.getSlotIndex()/SorterBlockEntity.FILTER_SLOTS_PER_SIDE;
			if(menu.filterMasks.get(Direction.from3DDataValue(side)).get().allowTags())
			{
				ItemStack item = ghostSlot.getItem();
				List<Component> tagTooltip = Lists.newArrayList();
				// Add name
				MutableComponent name = Component.empty().append(item.getHoverName()).withStyle(item.getRarity().getStyleModifier());
				if(item.has(DataComponents.CUSTOM_NAME))
					name.withStyle(ChatFormatting.ITALIC);
				tagTooltip.add(name);

				// Add tags
				List<TagKey<Item>> tags = item.getTags().sorted(TAG_SORTER).toList();
				if(tags.isEmpty())
					tagTooltip.add(Component.translatable(Lib.DESC_INFO+"filter.tag.none_available"));
				else
				{
					tagTooltip.add(Component.translatable(Lib.DESC_INFO+"filter.tag.selected_scroll"));
					Optional<ResourceLocation> selected = this.menu.selectedTags.get(ghostSlot.getSlotIndex()).get();
					tags.forEach(tagKey -> {
						boolean isSelected = selected.isPresent()&&selected.get().equals(tagKey.location());
						String tagTranslationKey = Tags.getTagTranslationKey(tagKey);
						tagTooltip.add(Component.literal(isSelected?" -> ": " > ")
								.append(Component.translatableWithFallback(tagTranslationKey, "#"+tagKey.location()))
								.withStyle(isSelected?ChatFormatting.GRAY: ChatFormatting.DARK_GRAY)
						);
					});
				}
				guiGraphics.renderTooltip(this.font, tagTooltip, item.getTooltipImage(), item, x, y);
				return;
			}
		}
		super.renderTooltip(guiGraphics, x, y);
	}


	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		if(this.menu.getCarried().isEmpty()&&this.hoveredSlot instanceof IESlot.ItemHandlerGhost ghostSlot)
		{
			int side = ghostSlot.getSlotIndex()/SorterBlockEntity.FILTER_SLOTS_PER_SIDE;
			if(menu.filterMasks.get(Direction.from3DDataValue(side)).get().allowTags())
			{
				ItemStack item = ghostSlot.getItem();
				List<ResourceLocation> tags = item.getTags().sorted(TAG_SORTER).map(TagKey::location).toList();
				if(tags.isEmpty())
					return false;
				// get current selected tag
				GetterAndSetter<Optional<ResourceLocation>> selected = this.menu.selectedTags.get(ghostSlot.getSlotIndex());
				int index = selected.get().map(tags::indexOf).orElse(scrollY < 0?-1: 0);
				// scroll and wrap around with modulo, fetching the new tag
				ResourceLocation newTag = tags.get(
						Math.floorMod(index+(scrollY < 0?1: -1), tags.size())
				);
				// write newly selected tag & sync to server
				selected.set(Optional.of(newTag));
				CompoundTag tag = new CompoundTag();
				tag.putInt("tagSlot", ghostSlot.getSlotIndex());
				tag.putString("selectedTag", newTag.toString());
				sendUpdateToServer(tag);
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private static final String COMMON_NAMESPACE = "c";
	Comparator<TagKey<Item>> TAG_SORTER = (o1, o2) -> {
		ResourceLocation rl1 = o1.location();
		ResourceLocation rl2 = o2.location();
		// common namespace always comes first
		if(COMMON_NAMESPACE.equals(rl1.getNamespace())&&!COMMON_NAMESPACE.equals(rl2.getNamespace()))
			return -1;
		if(!COMMON_NAMESPACE.equals(rl1.getNamespace())&&COMMON_NAMESPACE.equals(rl2.getNamespace()))
			return 1;
		return rl1.compareNamespaced(rl2);
	};

	public enum FilterBit
	{
		TAG, DAMAGE, COMPONENTS;

		public String getTranslationKey()
		{
			return Lib.DESC_INFO+"filter."+name().toLowerCase(Locale.ROOT);
		}

		public boolean get(FilterConfig config)
		{
			return switch(this)
			{
				case TAG -> config.allowTags();
				case COMPONENTS -> config.considerComponents();
				case DAMAGE -> config.ignoreDamage();
			};
		}

		public FilterConfig toggle(FilterConfig config)
		{
			return switch(this)
			{
				case TAG -> new FilterConfig(!config.allowTags(), config.considerComponents(), config.ignoreDamage());
				case COMPONENTS ->
						new FilterConfig(config.allowTags(), !config.considerComponents(), config.ignoreDamage());
				case DAMAGE ->
						new FilterConfig(config.allowTags(), config.considerComponents(), !config.ignoreDamage());
			};
		}
	}
}
