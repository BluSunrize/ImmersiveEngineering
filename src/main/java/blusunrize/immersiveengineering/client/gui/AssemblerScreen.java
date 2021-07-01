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
import blusunrize.immersiveengineering.common.blocks.metal.AssemblerTileEntity;
import blusunrize.immersiveengineering.common.gui.AssemblerContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.IntConsumer;

public class AssemblerScreen extends IEContainerScreen<AssemblerContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("assembler");
	public AssemblerTileEntity tile;

	public AssemblerScreen(AssemblerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.xSize = 230;
		this.ySize = 218;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = Lists.newArrayList(
				new FluidInfoArea(tile.tanks[0], new Rectangle2d(guiLeft+204, guiTop+13, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(tile.tanks[1], new Rectangle2d(guiLeft+182, guiTop+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new FluidInfoArea(tile.tanks[2], new Rectangle2d(guiLeft+204, guiTop+70, 16, 46), 230, 0, 20, 50, TEXTURE),
				new EnergyInfoArea(guiLeft+187, guiTop+13, tile),
				new TooltipArea(
						new Rectangle2d(guiLeft+162, guiTop+69, 16, 16),
						() -> new TranslationTextComponent(Lib.GUI_CONFIG+"assembler."+(tile.recursiveIngredients?"recursiveIngredients": "nonRecursiveIngredients"))
				)
		);
		for(int i = 0; i < tile.patterns.length; i++)
		{
			final int offset = 58 * i;
			areas.add(new TooltipArea(
					new Rectangle2d(guiLeft+11 + offset, guiTop+67, 10, 10),
					new TranslationTextComponent(Lib.GUI_CONFIG+"assembler.clearRecipe")
			));
			int finalI = i;
			areas.add(new TooltipArea(
					new Rectangle2d(guiLeft+27+offset, guiTop+64, 16, 16), l -> addRecipeOutputTooltip(finalI, l)
			));
		}
		return areas;
	}

	private void addRecipeOutputTooltip(int i, List<ITextComponent> tooltip)
	{
		ItemStack recipeOutput = tile.patterns[i].inv.get(9);
		if(tile.inventory.get(18+i).isEmpty()&&!recipeOutput.isEmpty())
		{
			tooltip.add(tile.patterns[i].inv.get(9).getDisplayName());
			recipeOutput.getItem().addInformation(recipeOutput, ClientUtils.mc().world, tooltip, TooltipFlags.NORMAL);
		}
	}

	@Override
	public void init()
	{
		super.init();
		IntConsumer sendButtonClick = id -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("buttonID", id);
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile.master(), tag));
		};
		for(int i = 0; i < 3; ++i)
		{
			final int id = i;
			this.addButton(new GuiButtonIE(guiLeft+11+i*59, guiTop+67, 10, 10, StringTextComponent.EMPTY, TEXTURE, 230, 50,
					btn -> sendButtonClick.accept(id))
					.setHoverOffset(0, 10));
		}
		this.addButton(new GuiButtonBoolean(guiLeft+162, guiTop+69, 16, 16, "", tile.recursiveIngredients, TEXTURE, 240, 66, 3,
				btn -> {
					sendButtonClick.accept(3);
					tile.recursiveIngredients = !tile.recursiveIngredients;
					fullInit();
				}));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
			{
				ItemStack stack = tile.patterns[i].inv.get(9);
				transform.push();
				FontRenderer font = null;
				if(!stack.isEmpty())
					font = stack.getItem().getFontRenderer(stack);
				if(font==null)
					font = this.font;
				itemRenderer.renderItemAndEffectIntoGUI(stack, guiLeft+27+i*58, guiTop+64);
				itemRenderer.renderItemOverlayIntoGUI(font, stack, guiLeft+27+i*58, guiTop+64, TextFormatting.GRAY.toString()+stack.getCount());

				RenderSystem.disableDepthTest();
				fill(transform, guiLeft+27+i*58, guiTop+64, guiLeft+27+i*74, guiTop+80, 0x77444444);
				RenderSystem.enableDepthTest();

				transform.pop();
				RenderHelper.disableStandardItemLighting();
			}
	}
}