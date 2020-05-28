/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public class ManualElementMultiblock extends SpecialManualElements
{
	private IMultiblock multiblock;

	private boolean canTick = true;
	private boolean showCompleted = false;
	private int tick = 0;

	private float scale = 50f;
	private float transX = 0;
	private float transY = 0;
	private TransformationMatrix additionalTransform;
	private List<ITextComponent> componentTooltip;
	private MultiblockRenderInfo renderInfo;
	private MultiblockBlockAccess blockAccess;
	private int yOffTotal;

	private long lastStep = -1;

	public ManualElementMultiblock(ManualInstance manual, IMultiblock multiblock)
	{
		super(manual);
		this.multiblock = multiblock;
		renderInfo = new MultiblockRenderInfo(multiblock);
		float diagLength = (float)Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+
				renderInfo.structureWidth*renderInfo.structureWidth+
				renderInfo.structureLength*renderInfo.structureLength);
		blockAccess = new MultiblockBlockAccess(renderInfo);
		transX = 60+renderInfo.structureWidth/2F;
		transY = 35+diagLength/2;
		additionalTransform = forRotation(25, -45);
		scale = multiblock.getManualScale();
		yOffTotal = (int)(transY+scale*diagLength/2);
	}

	private static final ITextComponent greenTick = new StringTextComponent("\u2713")
			.setStyle(new Style().setColor(TextFormatting.GREEN).setBold(true))
			.appendSibling(new StringTextComponent(" "));

	@Override
	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		int yOff = 0;
		if(multiblock.getStructure()!=null)
		{
			boolean canRenderFormed = multiblock.canRenderFormedStructure();

			yOff = (int)(transY+scale*Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+renderInfo.structureWidth*renderInfo.structureWidth+renderInfo.structureLength*renderInfo.structureLength)/2);
			pageButtons.add(new GuiButtonManualNavigation(gui, x+4, (int)transY-(canRenderFormed?11: 5), 10, 10, 4, btn -> {
				GuiButtonManualNavigation btnNav = (GuiButtonManualNavigation)btn;
				canTick = !canTick;
				lastStep = -1;
				btnNav.type = btnNav.type==4?5: 4;
			}));
			if(this.renderInfo.structureHeight > 1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, (int)transY-(canRenderFormed?14: 8)-16, 10, 16, 3,
						btn -> renderInfo.setShowLayer(Math.min(renderInfo.showLayer+1, renderInfo.structureHeight-1))
				));
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, (int)transY+(canRenderFormed?14: 8), 10, 16, 2,
						btn -> renderInfo.setShowLayer(Math.max(renderInfo.showLayer-1, -1))));
			}
			if(canRenderFormed)
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, (int)transY+1, 10, 10, 6,
						btn -> showCompleted = !showCompleted));
		}

		checkMaterials();
		super.onOpened(gui, x, yOff, pageButtons);
	}

	private void checkMaterials()
	{
		ItemStack[] totalMaterials = this.multiblock.getTotalMaterials();
		if(totalMaterials!=null)
		{
			componentTooltip = new ArrayList<>();
			componentTooltip.add(new TranslationTextComponent("desc.immersiveengineering.info.reqMaterial"));
			int maxOff = 1;
			boolean hasAnyItems = false;
			boolean[] hasItems = new boolean[totalMaterials.length];
			for(int ss = 0; ss < totalMaterials.length; ss++)
				if(totalMaterials[ss]!=null)
				{
					ItemStack req = totalMaterials[ss];
					int reqSize = req.getCount();
					for(int slot = 0; slot < ManualUtils.mc().player.inventory.getSizeInventory(); slot++)
					{
						ItemStack inSlot = ManualUtils.mc().player.inventory.getStackInSlot(slot);
						if(!inSlot.isEmpty()&&ItemStack.areItemsEqual(inSlot, req))
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
			for(int ss = 0; ss < totalMaterials.length; ss++)
				if(totalMaterials[ss]!=null)
				{
					ItemStack req = totalMaterials[ss];
					int indent = maxOff-(""+req.getCount()).length();
					StringBuilder sIndent = new StringBuilder();
					if(indent > 0)
						for(int ii = 0; ii < indent; ii++)
							sIndent.append("0");
					ITextComponent s;
					if(hasItems[ss])
						s = greenTick.deepCopy();
					else
						s = new StringTextComponent(hasAnyItems?"   ": "");
					s.appendSibling(
							new StringTextComponent(sIndent.toString()+req.getCount()+"x ")
									.setStyle(new Style().setColor(TextFormatting.GRAY))
					);
					if(!req.isEmpty())
						s.appendSibling(
								req.getDisplayName().deepCopy()
										.setStyle(new Style().setColor(req.getRarity().color))
						);
					else
						s.appendSibling(new StringTextComponent("???"));
					componentTooltip.add(s);
				}
		}
	}

	@Override
	public void render(ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		if(multiblock.getStructure()!=null)
		{
			MatrixStack transform = new MatrixStack();
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
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

				transform.push();

				final BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRendererDispatcher();

				transform.translate(transX, transY, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
				transform.scale(scale, -scale, 1);
				additionalTransform.push(transform);
				transform.rotate(new Quaternion(0, 90, 0, true));

				transform.translate(structureLength/-2f, structureHeight/-2f, structureWidth/-2f);

				int idx = 0;
				if(showCompleted&&multiblock.canRenderFormedStructure())
				{
					transform.push();
					multiblock.renderFormedStructure(transform, IERenderTypes.disableLighting(buffer));
					transform.pop();
				}
				else
					for(int h = 0; h < structureHeight; h++)
						for(int l = 0; l < structureLength; l++)
							for(int w = 0; w < structureWidth; w++)
							{
								BlockPos pos = new BlockPos(l, h, w);
								BlockState state = blockAccess.getBlockState(pos);
								if(!state.isAir(blockAccess, pos))
								{
									transform.push();
									transform.translate(l, h, w);
									boolean b = multiblock.overwriteBlockRender(state, idx++);
									if(!b)
									{
										int overlay;
										if(pos.equals(multiblock.getTriggerOffset()))
											overlay = OverlayTexture.getPackedUV(0, true);
										else
											overlay = OverlayTexture.NO_OVERLAY;
										IModelData modelData = EmptyModelData.INSTANCE;
										TileEntity te = blockAccess.getTileEntity(pos);
										if(te!=null)
											modelData = te.getModelData();

										blockRender.renderBlock(state, transform,
												IERenderTypes.disableLighting(buffer),
												0xf000f0, overlay, modelData);
									}
									transform.pop();
								}
							}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			transform.pop();
			transform.pop();
			buffer.finish();

			if(componentTooltip!=null)
			{
				manual.fontRenderer().drawString("?", 116, yOffTotal/2-4, manual.getTextColour());
				if(mouseX >= 116&&mouseX < 122&&mouseY >= yOffTotal/2-4&&mouseY < yOffTotal/2+4)
				{
					List<String> asStrings = new ArrayList<>();
					for(ITextComponent iTextComponent : componentTooltip)
					{
						String formattedText = iTextComponent.getFormattedText();
						asStrings.add(formattedText);
					}
					gui.renderTooltip(asStrings, mouseX, mouseY, manual.fontRenderer());
				}
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

	private TransformationMatrix forRotation(double rX, double rY)
	{
		Vector3f axis = new Vector3f((float)rY, (float)rX, 0);
		float angle = (float)Math.sqrt(axis.dot(axis));
		if(!axis.normalize())
			return TransformationMatrix.identity();
		return new TransformationMatrix(
				null,
				new Quaternion(axis, angle, true),
				null,
				null
		);
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

	static class MultiblockBlockAccess implements IBlockReader
	{
		private final MultiblockRenderInfo data;
		private final Map<BlockPos, TileEntity> tiles;

		MultiblockBlockAccess(MultiblockRenderInfo data)
		{
			this.data = data;
			this.tiles = new HashMap<>();
			for(Entry<BlockPos, BlockInfo> p : data.data.entrySet())
				if(p.getValue().nbt!=null&&!p.getValue().nbt.isEmpty())
				{
					TileEntity te = TileEntity.create(p.getValue().nbt);
					if(te!=null)
					{
						te.cachedBlockState = p.getValue().state;
						tiles.put(p.getKey(), te);
					}
				}
		}

		@Nullable
		@Override
		public TileEntity getTileEntity(BlockPos pos)
		{
			if(data.data.containsKey(pos))
			{
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				int index = y*(data.structureLength*data.structureWidth)+x*data.structureWidth+z;
				if(index <= data.getLimiter())
					return tiles.get(pos);
			}
			return null;
		}

		@Override
		public int getLightValue(BlockPos pos)
		{
			// full brightness always
			return 15<<20|15<<4;
		}

		@Override
		public BlockState getBlockState(BlockPos pos)
		{
			if(data.data.containsKey(pos))
			{
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				int index = y*(data.structureLength*data.structureWidth)+x*data.structureWidth+z;
				if(index <= data.getLimiter())
					return data.data.get(pos).state;
			}
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public IFluidState getFluidState(BlockPos pos)
		{
			return getBlockState(pos).getFluidState();
		}
	}

	//Stolen back from boni's StructureInfo
	static class MultiblockRenderInfo
	{
		public IMultiblock multiblock;
		public Map<BlockPos, BlockInfo> data = new HashMap<>();
		int blockCount = 0;
		int[] countPerLevel;
		int structureHeight = 0;
		int structureLength = 0;
		int structureWidth = 0;
		int showLayer = -1;

		private int blockIndex = -1;
		private int maxBlockIndex;

		MultiblockRenderInfo(IMultiblock multiblock)
		{
			this.multiblock = multiblock;
			init(multiblock.getStructure());
			maxBlockIndex = blockIndex = structureHeight*structureLength*structureWidth;
		}

		public void init(List<BlockInfo> structure)
		{
			structureHeight = 0;
			structureWidth = 0;
			structureLength = 0;

			countPerLevel = new int[structureHeight];
			blockCount = structure.size();
			for(BlockInfo block : structure)
			{
				structureHeight = Math.max(structureHeight, block.pos.getY()+1);
				structureWidth = Math.max(structureWidth, block.pos.getZ()+1);
				structureLength = Math.max(structureLength, block.pos.getX()+1);
				if(structureHeight!=countPerLevel.length)
					countPerLevel = Arrays.copyOf(countPerLevel, structureHeight);
				++countPerLevel[block.pos.getY()];
				data.put(block.pos, block);
			}
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
			int start = blockIndex;
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
	}
}