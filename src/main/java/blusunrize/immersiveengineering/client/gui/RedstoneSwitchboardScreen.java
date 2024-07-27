/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneSwitchboardBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneSwitchboardBlockEntity.SwitchboardSetting;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.client.gui.IEContainerScreen.makeTextureLocation;

public class RedstoneSwitchboardScreen extends ClientBlockEntityScreen<RedstoneSwitchboardBlockEntity>
{

	public static final ResourceLocation TEXTURE = makeTextureLocation("switchboard");

	public RedstoneSwitchboardScreen(RedstoneSwitchboardBlockEntity tileEntity, Component title)
	{
		super(tileEntity, title);
		this.xSize = 240;
		this.ySize = 138;
	}

	private final boolean[] inverterStates = new boolean[16];

	private DyeColor clickedInput;

	@Override
	public void init()
	{
		super.init();
		clearWidgets();

		for(int i = 0; i < 16; i++)
		{
			int finalIndex = i;
			// invert button
			this.addRenderableWidget(new GuiButtonBoolean(
					guiLeft+9+i*14, guiTop+105, 12, 11, Component.empty(),
					() -> inverterStates[finalIndex],
					TEXTURE, 0, 138, 0,
					btn -> {
						this.inverterStates[finalIndex] = btn.getNextState();
						findConnectionWidget(w -> w.setting.output().getId()==finalIndex).ifPresent(oldWidget ->
								addSetting(new SwitchboardSetting(
										oldWidget.setting.input(),
										inverterStates[finalIndex],
										oldWidget.setting.output()
								))
						);
					}
			));

			// input socket
			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+9+i*14, guiTop+30, 12, 12, Component.empty(),
					TEXTURE, 24, 138,
					btn -> this.clickedInput = DyeColor.byId(finalIndex)
			));

			// output socket
			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+9+i*14, guiTop+86, 12, 12, Component.empty(),
					TEXTURE, 24, 138,
					btn -> {
						if(clickedInput!=null)
						{
							addSetting(new SwitchboardSetting(
									clickedInput,
									inverterStates[finalIndex],
									DyeColor.byId(finalIndex)
							));
							this.clickedInput = null;
						}
						else
							removeSetting(DyeColor.byId(finalIndex), true);
					}
			));
		}

		// load values from blockEntity
		this.blockEntity.settings.forEach(setting -> {
			inverterStates[setting.output().getId()] = setting.invert();
			addConnectionWidget(setting);
		});
	}

	private void addConnectionWidget(SwitchboardSetting setting)
	{
		this.addRenderableWidget(new ConnectionWidget(this, guiLeft, guiTop, setting));
	}

	private Optional<ConnectionWidget> findConnectionWidget(Predicate<ConnectionWidget> predicate)
	{
		for(Renderable widget : this.renderables)
			if(widget instanceof ConnectionWidget connectionWidget&&predicate.test(connectionWidget))
				return Optional.of(connectionWidget);
		return Optional.empty();
	}

	private void addSetting(SwitchboardSetting newSetting)
	{
		// remove existing widgets
		findConnectionWidget(w -> w.setting.input()==newSetting.input()).ifPresent(this::removeWidget);
		findConnectionWidget(w -> w.setting.output()==newSetting.output()).ifPresent(this::removeWidget);
		// add to entity
		this.blockEntity.addSetting(newSetting);
		// create new widget
		addConnectionWidget(newSetting);
		// send to server
		PacketDistributor.sendToServer(new MessageBlockEntitySync(blockEntity, newSetting.writeToNBT()));
	}

	private void removeSetting(DyeColor output, boolean selectInput)
	{
		// remove widget
		findConnectionWidget(w -> w.setting.output()==output).ifPresent(widget -> {
			if(selectInput)
				clickedInput = widget.setting.input();
			removeWidget(widget);
		});
		// remove from entity
		this.blockEntity.removeSetting(output);
		// send to server
		CompoundTag msg = new CompoundTag();
		msg.putInt("remove", output.getId());
		PacketDistributor.sendToServer(new MessageBlockEntitySync(blockEntity, msg));
	}


	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.renderBackground(graphics, mouseX, mouseY, partialTicks);
		graphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);

		if(this.clickedInput!=null)
		{
			// input plug
			graphics.blit(TEXTURE, guiLeft+7+clickedInput.getId()*14, guiTop+31, 0, 149, 16, 16);
			CableQuad.build(this, clickedInput, new Vec2(mouseX, mouseY)).draw(graphics);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int modifier)
	{
		boolean ret = super.mouseClicked(mouseX, mouseY, modifier);
		if(!ret&&this.clickedInput!=null)
		{
			this.clickedInput = null;
			return true;
		}
		return ret;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.drawString(this.font, Component.translatable(Lib.DESC_INFO+"blockSide.io.input"), guiLeft+8, guiTop+4, DyeColor.GRAY.getTextColor());
		graphics.drawString(this.font, Component.translatable(Lib.DESC_INFO+"blockSide.io.output"), guiLeft+8, guiTop+74, DyeColor.GRAY.getTextColor());

		ArrayList<Component> tooltip = new ArrayList<>();
		if(mouseX > guiLeft+8&&mouseX < guiLeft+232&&((mouseY > guiTop+14&&mouseY < guiTop+48)||(mouseY > guiTop+84&&mouseY < guiTop+132)))
		{
			int dyeIdx = (mouseX-guiLeft-8)/14;
			boolean output = mouseY > guiTop+48;
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"redstone_color_"+(output?"output": "input"))
					.append(" ")
					.append(Component.translatable("color.minecraft."+DyeColor.byId(dyeIdx).getName()))
					.withStyle(ChatFormatting.GRAY)
			);
			if(output)
				tooltip.add(Component.translatable(Lib.CHAT_INFO+"rsSignal."+(inverterStates[dyeIdx]?"invertedOn": "invertedOff")).withStyle(ChatFormatting.DARK_GRAY));
		}

		if(!tooltip.isEmpty())
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}


	private record CableQuad(Vec2 topLeft, Vec2 topRight, Vec2 botLeft, Vec2 botRight, int colour)
	{
		public static CableQuad build(Vec2 fromPos, Vec2 toPos, int color)
		{
			Vec3 dir = new Vec3(toPos.x-fromPos.x, toPos.y-fromPos.y, 0);
			Vec3 orthL = dir.zRot(90).normalize();
			Vec3 orthR = dir.zRot(-90).normalize();
			Vec2 offsetL = new Vec2((float)(orthL.x*2.5), (float)(orthL.y*2.5));
			Vec2 offsetR = new Vec2((float)(orthR.x*2.5), (float)(orthR.y*2.5));
			return new CableQuad(
					fromPos.add(offsetL),
					fromPos.add(offsetR),
					toPos.add(offsetL),
					toPos.add(offsetR),
					color
			);
		}

		public static CableQuad build(RedstoneSwitchboardScreen screen, DyeColor input, Vec2 toPos)
		{
			return build(new Vec2(screen.guiLeft+17.5f+input.getId()*14, screen.guiTop+42), toPos, 0xff000000|input.getFireworkColor()
			);
		}

		public static CableQuad build(RedstoneSwitchboardScreen screen, DyeColor input, DyeColor output)
		{
			return build(screen, input, new Vec2(screen.guiLeft+17.5f+output.getId()*14, screen.guiTop+100));
		}

		public void draw(GuiGraphics graphics)
		{
			Matrix4f matrix4f = graphics.pose().last().pose();
			// Quad
			VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(RenderType.debugQuads());
			vertexconsumer.addVertex(matrix4f, topLeft.x, topLeft.y, 1)
					.setColor(colour);
			vertexconsumer.addVertex(matrix4f, botLeft.x, botLeft.y, 1)
					.setColor(colour);
			vertexconsumer.addVertex(matrix4f, botRight.x, botRight.y, 1)
					.setColor(colour);
			vertexconsumer.addVertex(matrix4f, topRight.x, topRight.y, 1)
					.setColor(colour);
			graphics.flush();
		}
	}

	private static class ConnectionWidget extends AbstractWidget
	{
		final SwitchboardSetting setting;

		final CableQuad quad;

		public ConnectionWidget(RedstoneSwitchboardScreen screen, int guiLeft, int guiTop, SwitchboardSetting setting)
		{
			super(guiLeft, guiTop, 240, 138, Component.empty());
			this.setting = setting;
			this.quad = CableQuad.build(screen, setting.input(), setting.output());
			this.active = false;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
		{
			// input plug
			graphics.blit(TEXTURE, getX()+7+setting.input().getId()*14, getY()+31, 0, 149, 16, 16);
			// output plug
			graphics.blit(TEXTURE, getX()+7+setting.output().getId()*14, getY()+87, 0, 149, 16, 16);
			// cable
			this.quad.draw(graphics);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput p_259858_)
		{
		}
	}

}