/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Transformation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;

public class ManualElementMultiblock extends SpecialManualElements
{
	private final IMultiblock multiblock;
	private final MultiblockManualData renderProperties;

	private boolean canTick = true;
	private boolean showCompleted = false;

	private float scale = 50f;
	private float transX = 0;
	private float transY = 0;
	private Transformation additionalTransform;
	private List<Component> componentTooltip;
	private final MultiblockRenderInfo renderInfo;
	private final TemplateWorld structureWorld;
	private final int yOffTotal;
	private final ClientLevel level;

	private long lastStep = -1;
	private long lastPrintedErrorTimeMs = -1;

	public ManualElementMultiblock(ManualInstance manual, IMultiblock multiblock)
	{
		super(manual);
		level = Objects.requireNonNull(Minecraft.getInstance().level);
		this.multiblock = multiblock;
		this.renderProperties = ClientMultiblocks.get(multiblock);
		List<StructureBlockInfo> structure = multiblock.getStructure(level);
		renderInfo = new MultiblockRenderInfo(structure);
		float diagLength = (float)Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+
				renderInfo.structureWidth*renderInfo.structureWidth+
				renderInfo.structureLength*renderInfo.structureLength);
		structureWorld = new TemplateWorld(structure, renderInfo, level.registryAccess());
		transX = 60+renderInfo.structureWidth/2F;
		transY = 35+diagLength/2;
		additionalTransform = new Transformation(
				null,
				new Quaternionf().rotateXYZ((float)Math.toRadians(25), 0, 0),
				null,
				new Quaternionf().rotateXYZ(0, (float)Math.toRadians(-45), 0)
		);
		scale = multiblock.getManualScale();
		yOffTotal = (int)(transY+scale*diagLength/2);
	}

	private static final Component greenTick = applyFormat(
			Component.literal("\u2713"), ChatFormatting.GREEN, ChatFormatting.BOLD
	).append(" ");

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		int yOff = 0;
		if(multiblock.getStructure(level)!=null)
		{
			boolean canRenderFormed = renderProperties.canRenderFormedStructure();

			yOff = (int)(transY+scale*Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+renderInfo.structureWidth*renderInfo.structureWidth+renderInfo.structureLength*renderInfo.structureLength)/2);
			pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY-(canRenderFormed?11: 5), 10, 10, 4, btn -> {
				GuiButtonManualNavigation btnNav = (GuiButtonManualNavigation)btn;
				canTick = !canTick;
				lastStep = -1;
				btnNav.type = btnNav.type==4?5: 4;
			}));
			if(this.renderInfo.structureHeight > 1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY-(canRenderFormed?14: 8)-16, 10, 16, 3,
						btn -> renderInfo.setShowLayer(Math.min(renderInfo.showLayer+1, renderInfo.structureHeight-1))
				));
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY+(canRenderFormed?14: 8), 10, 16, 2,
						btn -> renderInfo.setShowLayer(Math.max(renderInfo.showLayer-1, -1))));
			}
			if(canRenderFormed)
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY+1, 10, 10, 6,
						btn -> showCompleted = !showCompleted));
		}

		checkMaterials();
		super.onOpened(gui, x, yOff, pageButtons);
	}

	private void checkMaterials()
	{
		NonNullList<ItemStack> totalMaterials = this.renderProperties.getTotalMaterials();
		if(totalMaterials!=null)
		{
			componentTooltip = new ArrayList<>();
			componentTooltip.add(Component.translatable("desc.immersiveengineering.info.reqMaterial"));
			int maxOff = 1;
			boolean hasAnyItems = false;
			boolean[] hasItems = new boolean[totalMaterials.size()];
			for(int ss = 0; ss < totalMaterials.size(); ss++)
			{
				ItemStack req = totalMaterials.get(ss);
				int reqSize = req.getCount();
				for(int slot = 0; slot < ManualUtils.mc().player.getInventory().getContainerSize(); slot++)
				{
					ItemStack inSlot = ManualUtils.mc().player.getInventory().getItem(slot);
					if(!inSlot.isEmpty()&&ItemStack.isSameItem(inSlot, req))
						if((reqSize -= inSlot.getCount()) <= 0)
							break;
				}
				if(reqSize <= 0)
				{
					hasItems[ss] = true;
					if(!hasAnyItems)
						hasAnyItems = true;
				}
				maxOff = Math.max(maxOff, (""+req.getCount()).length());
			}
			for(int ss = 0; ss < totalMaterials.size(); ss++)
			{
				ItemStack req = totalMaterials.get(ss);
				int indent = maxOff-(""+req.getCount()).length();
				StringBuilder sIndent = new StringBuilder();
				if(indent > 0)
					sIndent.append("0".repeat(indent));
				MutableComponent s;
				if(hasItems[ss])
					s = greenTick.copy();
				else
					s = Component.literal(hasAnyItems?"   ": "");
				s.append(applyFormat(
						Component.literal(sIndent.toString()+req.getCount()+"x "), ChatFormatting.GRAY
				));
				if(!req.isEmpty())
					s.append(applyFormat(req.getHoverName().copy(), req.getRarity().color));
				else
					s.append("???");
				componentTooltip.add(s);
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		if(multiblock.getStructure(level)!=null)
		{
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			PoseStack transform = graphics.pose();
			PoseStack.Pose lastEntryBeforeTry = transform.last();
			try
			{
				long currentTime = System.currentTimeMillis();
				if(lastStep < 0)
					lastStep = currentTime;
				else if(canTick&&currentTime-lastStep > 500)
				{
					renderInfo.step();
					lastStep = currentTime;
				}

				int structureLength = renderInfo.structureLength;
				int structureWidth = renderInfo.structureWidth;
				int structureHeight = renderInfo.structureHeight;

				transform.pushPose();

				final BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();

				transform.translate(transX, transY, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
				transform.scale(scale, -scale, 1);
				transform.pushTransformation(additionalTransform);
				transform.mulPose(new Quaternionf().rotateXYZ(0, Mth.HALF_PI, 0));

				transform.translate(structureLength/-2f, structureHeight/-2f, structureWidth/-2f);

				if(showCompleted&&renderProperties.canRenderFormedStructure())
				{
					transform.pushPose();
					renderProperties.renderFormedStructure(transform, buffer);
					transform.popPose();
				}
				else
				{
					TransformingVertexBuilder translucentFullbright = new TransformingVertexBuilder(
							buffer, IERenderTypes.TRANSLUCENT_FULLBRIGHT
					);
					for(int h = 0; h < structureHeight; h++)
						for(int l = 0; l < structureLength; l++)
							for(int w = 0; w < structureWidth; w++)
							{
								BlockPos pos = new BlockPos(l, h, w);
								BlockState state = structureWorld.getBlockState(pos);
								if(!state.isAir())
								{
									transform.pushPose();
									transform.translate(l, h, w);
									int overlay;
									if(pos.equals(multiblock.getTriggerOffset()))
										overlay = OverlayTexture.pack(0, true);
									else
										overlay = OverlayTexture.NO_OVERLAY;
									translucentFullbright.setOverlay(overlay);
									ModelData modelData = ModelData.EMPTY;
									BlockEntity te = structureWorld.getBlockEntity(pos);
									if(te!=null)
										modelData = te.getModelData();
									final BakedModel model = blockRender.getBlockModel(state);
									modelData = model.getModelData(structureWorld, pos, state, modelData);
									blockRender.getModelRenderer().tesselateBlock(
											structureWorld, model, state, pos, transform,
											translucentFullbright, false, structureWorld.random, state.getSeed(pos),
											overlay, modelData, null
									);
									transform.popPose();
								}
							}
				}
				transform.popPose();
				transform.popPose();
			} catch(Exception e)
			{
				final long now = System.currentTimeMillis();
				if(now > lastPrintedErrorTimeMs+1000)
				{
					e.printStackTrace();
					lastPrintedErrorTimeMs = now;
				}
				while(lastEntryBeforeTry!=transform.last())
					transform.popPose();
			}
			buffer.endBatch();

			if(componentTooltip!=null)
			{
				graphics.drawString(manual.fontRenderer(), "?", 116, yOffTotal/2-4, manual.getTextColour());
				if(mouseX >= 116&&mouseX < 122&&mouseY >= yOffTotal/2-4&&mouseY < yOffTotal/2+4)
					graphics.renderTooltip(manual.fontRenderer(), Language.getInstance().getVisualOrder(
							Collections.unmodifiableList(componentTooltip)
					), mouseX, mouseY);
			}
		}
	}

	@Override
	public void mouseDragged(int x, int y, double clickX, double clickY, double mouseX, double mouseY, double lastX, double lastY, int mouseButton)
	{
		if((clickX >= 40&&clickX < 144&&mouseX >= 20&&mouseX < 164)&&(clickY >= 30&&clickY < 130&&mouseY >= 30&&mouseY < 180))
		{
			double dx = mouseX-lastX;
			double dy = mouseY-lastY;
			additionalTransform = forRotation(dx*80D/104, dy*0.8).compose(additionalTransform);
		}
	}

	private Transformation forRotation(double rX, double rY)
	{
		Vector3f axis = new Vector3f((float)rY, (float)rX, 0);
		if(axis.lengthSquared() < 1e-3)
			return Transformation.identity();
		float angle = (float)Math.sqrt(axis.dot(axis));
		axis.normalize();
		return new Transformation(null, new Quaternionf().rotateAxis((float)Math.toRadians(angle), axis), null, null);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return yOffTotal;
	}

	public IMultiblock getMultiblock()
	{
		return this.multiblock;
	}

	//Stolen back from boni's StructureInfo
	static class MultiblockRenderInfo implements Predicate<BlockPos>
	{
		public Map<BlockPos, StructureBlockInfo> data = new HashMap<>();
		private final int structureHeight;
		private final int structureLength;
		private final int structureWidth;
		private final int maxBlockIndex;

		private int showLayer = -1;
		private int blockIndex;

		MultiblockRenderInfo(List<StructureBlockInfo> structure)
		{
			int structureHeight = 0;
			int structureWidth = 0;
			int structureLength = 0;
			for(StructureBlockInfo block : structure)
			{
				structureHeight = Math.max(structureHeight, block.pos().getY()+1);
				structureWidth = Math.max(structureWidth, block.pos().getZ()+1);
				structureLength = Math.max(structureLength, block.pos().getX()+1);
				data.put(block.pos(), block);
			}
			this.maxBlockIndex = this.blockIndex = structureHeight*structureLength*structureWidth;
			this.structureHeight = structureHeight;
			this.structureLength = structureLength;
			this.structureWidth = structureWidth;
		}

		void setShowLayer(int layer)
		{
			showLayer = layer;
			if(layer < 0)
				reset();
			else
				blockIndex = (layer+1)*(structureLength*structureWidth)-1;
		}

		public void reset()
		{
			blockIndex = maxBlockIndex;
		}

		void step()
		{
			final int start = blockIndex;
			do
			{
				if(++blockIndex >= maxBlockIndex)
					blockIndex = 0;
			}
			while(isEmpty(blockIndex)&&blockIndex!=start);
		}

		private boolean isEmpty(int index)
		{
			int y = index/(structureLength*structureWidth);
			int r = index%(structureLength*structureWidth);
			int x = r/structureWidth;
			int z = r%structureWidth;

			return !data.containsKey(new BlockPos(x, y, z));
		}

		int getLimiter()
		{
			return blockIndex;
		}

		@Override
		public boolean test(BlockPos blockPos)
		{
			int index = blockPos.getZ()+structureWidth*(blockPos.getX()+structureLength*blockPos.getY());
			return index <= getLimiter();
		}
	}
}
