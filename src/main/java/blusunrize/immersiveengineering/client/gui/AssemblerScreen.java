/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.metal.AssemblerBlockEntity;
import blusunrize.immersiveengineering.common.gui.AssemblerContainer;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.client.RenderProperties;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.IntConsumer;

public class AssemblerScreen extends IEContainerScreen<AssemblerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("assembler");
	public AssemblerBlockEntity tile;

	public AssemblerScreen(AssemblerContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.imageWidth = 230;
		this.imageHeight = 218;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = Lists.newArrayList(
				new FluidInfoArea(tile.tanks[0], new Rect2i(leftPos+204, topPos+13, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(tile.tanks[1], new Rect2i(leftPos+182, topPos+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(tile.tanks[2], new Rect2i(leftPos+204, topPos+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new EnergyInfoArea(leftPos+187, topPos+13, tile.energyStorage),
				new TooltipArea(
						new Rect2i(leftPos+162, topPos+69, 16, 16),
						() -> new TranslatableComponent(Lib.GUI_CONFIG+"assembler."+(tile.recursiveIngredients?"recursiveIngredients": "nonRecursiveIngredients"))
				)
		);
		for(int i = 0; i < tile.patterns.length; i++)
		{
			final int offset = 58 * i;
			areas.add(new TooltipArea(
					new Rect2i(leftPos+11 + offset, topPos+67, 10, 10),
					new TranslatableComponent(Lib.GUI_CONFIG+"assembler.clearRecipe")
			));
			int finalI = i;
			areas.add(new TooltipArea(
					new Rect2i(leftPos+27+offset, topPos+64, 16, 16), l -> addRecipeOutputTooltip(finalI, l)
			));
		}
		return areas;
	}

	private void addRecipeOutputTooltip(int i, List<Component> tooltip)
	{
		ItemStack recipeOutput = tile.patterns[i].inv.get(9);
		if(tile.inventory.get(18+i).isEmpty()&&!recipeOutput.isEmpty())
		{
			tooltip.add(tile.patterns[i].inv.get(9).getHoverName());
			recipeOutput.getItem().appendHoverText(recipeOutput, ClientUtils.mc().level, tooltip, Default.NORMAL);
		}
	}

	@Override
	public void init()
	{
		super.init();
		IntConsumer sendButtonClick = id -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("buttonID", id);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile.master(), tag));
		};
		for(int i = 0; i < 3; ++i)
		{
			final int id = i;
			this.addRenderableWidget(new GuiButtonIE(leftPos+11+i*59, topPos+67, 10, 10, TextComponent.EMPTY, TEXTURE, 230, 50,
					btn -> sendButtonClick.accept(id))
					.setHoverOffset(0, 10));
		}
		this.addRenderableWidget(new GuiButtonBoolean(leftPos+162, topPos+69, 16, 16, "", tile.recursiveIngredients, TEXTURE, 240, 66, 3,
				btn -> {
					sendButtonClick.accept(3);
					tile.recursiveIngredients = !tile.recursiveIngredients;
					fullInit();
				}));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull PoseStack transform, float f, int mx, int my)
	{
		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
			{
				ItemStack stack = tile.patterns[i].inv.get(9);
				transform.pushPose();
				Font font = null;
				if(!stack.isEmpty())
					font = RenderProperties.get(stack.getItem()).getFont(stack);
				if(font==null)
					font = this.font;
				itemRenderer.renderAndDecorateItem(stack, leftPos+27+i*58, topPos+64);
				itemRenderer.renderGuiItemDecorations(font, stack, leftPos+27+i*58, topPos+64, ChatFormatting.GRAY.toString()+stack.getCount());

				RenderSystem.disableDepthTest();
				fill(transform, leftPos+27+i*58, topPos+64, leftPos+27+i*74, topPos+80, 0x77444444);
				RenderSystem.enableDepthTest();

				transform.popPose();
			}
	}
}